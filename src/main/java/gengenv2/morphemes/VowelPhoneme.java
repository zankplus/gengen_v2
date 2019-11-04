package gengenv2.morphemes;

import gengenv2.ConstituentLibrary;
import gengenv2.PhoneticRatings;
import gengenv2.Phonotactics;
import gengenv2.enums.ConstituentLocation;
import gengenv2.enums.ConstituentType;
import gengenv2.enums.VowelProperty;

public class VowelPhoneme extends Phoneme
{
	public final double nucleusLeadProminence;
	public final double nucleusFollowProminence;
	public final double terminalProminence;
	public final double rootProminence;
	
	private double hiatusMedialSyllableEntropy;	// entropy for a medial syllable starting with a vowel
	private double hiatusTerminalSyllableEntropy;
	private double hiatusRootSyllableEntropy;
	private double hiatusTerminalCodaChance;
	
	ConstituentLibrary followers;
	ConstituentLibrary nucleusFollowers;
	ConstituentLibrary terminalFollowers;
	ConstituentLibrary rootFollowers;
	
	private double hiatusChance;
	
	public VowelPhoneme(Segment segment, 
						double medialProminence,
						double wordInitialProminence,
						double terminalProminence,
						double rootProminence,
						double nucleusLeadProminence,
						double nucleusFollowProminence,
						double interludeLeadProminence,
						double interludeFollowProminence,
						int maxNucleusLength)
	{
		super(segment);
		
		this.medialProminence = medialProminence;
		this.wordInitialProminence = wordInitialProminence;
		this.terminalProminence = terminalProminence;
		this.rootProminence = rootProminence;
		this.nucleusLeadProminence = nucleusLeadProminence;
		this.nucleusFollowProminence = nucleusFollowProminence;
		this.interludeLeadProminence = nucleusLeadProminence;
		this.interludeFollowProminence = interludeFollowProminence;
		if (maxNucleusLength > 1)
			nucleusFollowers = new ConstituentLibrary(segment.expression, maxNucleusLength, ConstituentType.NUCLEUS, ConstituentLocation.CLUSTER);
		
		hiatusMedialSyllableEntropy			= 0;
		hiatusTerminalSyllableEntropy		= 0;
		hiatusRootSyllableEntropy			= 0;
		//		System.out.printf("%s\t%.3f\t%.3f\t%.3f\n", segment.expression, onsetInitialProminence, nucleusLeadProminence, nucleusFollowProminence);
	}
	
	public boolean isConsonant()
	{
		return false;
	}
	
	public void printHiatus()
	{
		System.out.println(segment.expression + ":");
		ConstituentLibrary[] libs = new ConstituentLibrary[] { followers, terminalFollowers, 
				rootFollowers };
		
		for (int j = 0; j < libs.length; j++)
		{
			System.out.print(libs[j].getLocation() + "  \t");
			for (Constituent c : libs[j].getMembers())
				System.out.printf("%s %.2f\t", c, c.getProbability());
			System.out.println();
		}
	}
	
	public void createFollowerLists(int maxNucleusLength)
	{
		followers = new ConstituentLibrary(segment.expression, maxNucleusLength, ConstituentType.NUCLEUS, ConstituentLocation.MEDIAL);
		terminalFollowers = new ConstituentLibrary(segment.expression, maxNucleusLength, ConstituentType.NUCLEUS, ConstituentLocation.TERMINAL);
		rootFollowers = new ConstituentLibrary(segment.expression, maxNucleusLength, ConstituentType.NUCLEUS, ConstituentLocation.ROOT);
	}
	
	public void makeHiatus(ConstituentLibrary lib, double[] leadProbabilities, double[] followProbabilities, double hiatusOffset)
	{
		ConstituentLibrary followerLibrary = null;
		
		if (lib.getLocation() == ConstituentLocation.MEDIAL)
			followerLibrary = followers;
		else if (lib.getLocation() == ConstituentLocation.TERMINAL)
			followerLibrary = terminalFollowers;
		else if (lib.getLocation() == ConstituentLocation.ROOT)
			followerLibrary = rootFollowers;
		
		// 
		for (int j = 0; j < lib.size(); j++)
		{
			Phoneme v2 = lib.getMembers().get(j).getContent();
			if (!segment.expression.equals(":"))
			{
				// Chance of representation ~= f(.3 x 4^(commonness - 3))
				// 3 -> .446, 2 -> .155, 1 -> .050
				double probability = 0.3 * Math.pow(4, Phonotactics.hiatusTransitions[segment.getID()][v2.segment.getID()]
						- 3) + hiatusOffset;
				if (leadProbabilities[segment.getID()] * followProbabilities[v2.segment.getID()] < probability)
				addInterlude(lib.getMembers().get(j), lib.getLocation(), followerLibrary);
			}
		}
		
		// Normalize probabilities and sort the current vowel's interludes according to them
		followerLibrary.normalizeAll();
		followerLibrary.sortAll();
	}
	
	private void addInterlude(Constituent c, ConstituentLocation location, ConstituentLibrary targetLibrary)
	{
		// Calculate interlude's probability.
		// Base probability equals sum of following segment's interludeFollow and onsetInitial prominences
		double probability = c.getContent().interludeFollowProminence;
		
		// Set source and target libraries and probability
		switch(location)
		{
			case MEDIAL:
				probability += c.getContent().medialProminence - 1;
				break;
			case TERMINAL:
				probability += ((VowelPhoneme) c.getContent()).terminalProminence - 1;
				break;
			case ROOT:
				probability += ((VowelPhoneme) c.getContent()).rootProminence - 1;
				break;
			default:
				return;
		}
		
		// If probability is positive, add this interlude
		if (probability > 0)
		{
			targetLibrary.add(new Constituent(c, probability));
		}
	
//		Print interlude statistics
//		System.out.printf("%.3f (%.3f + %,3f)", probability, p.interludeFollowProminence, p.onsetInitialProminence);
	}
	
	public ConstituentLibrary getFollowers()
	{
		return followers;
	}
	
	public ConstituentLibrary getNucleusFollowers()
	{
		return nucleusFollowers;
	}

	public ConstituentLibrary getRootFollowers()
	{
		return rootFollowers;
	}
	
	public ConstituentLibrary getTerminalFollowers()
	{
		return terminalFollowers;
	}
	
	public double getHiatusMedialSyllableEntropy() {
		return hiatusMedialSyllableEntropy;
	}

	public void setHiatusMedialSyllableEntropy(double hiatusMedialSyllableEntropy) {
		this.hiatusMedialSyllableEntropy = hiatusMedialSyllableEntropy;
	}

	public double getHiatusTerminalSyllableEntropy() {
		return hiatusTerminalSyllableEntropy;
	}

	public void setHiatusTerminalSyllableEntropy(double hiatusTerminalSyllableEntropy) {
		this.hiatusTerminalSyllableEntropy = hiatusTerminalSyllableEntropy;
	}

	public double getHiatusRootSyllableEntropy() {
		return hiatusRootSyllableEntropy;
	}

	public void setHiatusRootSyllableEntropy(double hiatusRootSyllableEntropy) {
		this.hiatusRootSyllableEntropy = hiatusRootSyllableEntropy;
	}

	public double getHiatusTerminalCodaChance() {
		return hiatusTerminalCodaChance;
	}

	public void setHiatusTerminalCodaChance(double hiatusTerminalCodaChance) {
		this.hiatusTerminalCodaChance = hiatusTerminalCodaChance;
	}
	
	public void setHiatusChance(double hiatusChance)
	{
		this.hiatusChance = hiatusChance;
	}
	
	public double getHiatusChance()
	{
		return hiatusChance;
	}
}