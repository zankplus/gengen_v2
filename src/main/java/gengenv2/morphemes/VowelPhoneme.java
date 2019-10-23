package gengenv2.morphemes;

import gengenv2.ConstituentLibrary;
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
	
	public final double strongRootEndChance;			// Chance a root ending in this vowel will be strong
	public final double strongSuffixStartChance;		// Chance a suffix starting with this vowel will be strong
	
	public final double hiatusMedialSyllableEntropy;	// entropy for a medial syllable starting with a vowel
	public final double hiatusTerminalSyllableEntropy;
	public final double hiatusRootSyllableEntropy;
	public final double hiatusTerminalCodaChance;
	
	ConstituentLibrary terminalFollowers;
	ConstituentLibrary rootFollowers;
	
	public VowelPhoneme(Segment segment)
	{
		super(segment);
		
		// Assign initial prominence values
		medialProminence			= 1;	// this functions as the nucleus initial prominence here
		wordInitialProminence		= 1;
		terminalProminence			= 1;
		rootProminence				= 1;
		nucleusLeadProminence 		= 1;
		nucleusFollowProminence 	= 1;
		interludeLeadProminence  	= 1;
		interludeFollowProminence 	= 1;
		
		double hiatusMedialSyllableEntropy			= 0;
		double hiatusTerminalSyllableEntropy		= 0;
		double hiatusRootSyllableEntropy			= 0;
		
		// Apply offsets
		if (segment.expression.equals(":"))
		{
			medialProminence = Integer.MIN_VALUE;
		}
		
		// Calculate prominence values
		for (VowelProperty s : ((Vowel) segment).properties)
		{
			double deviance = baseVowelRatings.getRating(s.ordinal()) - 1;
			deviance /= Math.sqrt(segment.properties.length);
			medialProminence += deviance;
			
			deviance = initialNucleusRatings.getRating(s.ordinal()) - 1;
			deviance /= Math.sqrt(segment.properties.length);
			wordInitialProminence += deviance;
			
			if (features.terminalCodas != Feature.REQUIRED)
			{
				deviance = terminalNucleusRatings.getRating(s.ordinal()) - 1;
				deviance /= Math.sqrt(segment.properties.length);
				terminalProminence += deviance;
			}
			
			deviance = rootNucleusRatings.getRating(s.ordinal()) - 1;
			deviance /= Math.sqrt(segment.properties.length);
			rootProminence += deviance;
			
			deviance = hiatusLeadRatings.getRating(s.ordinal()) - 1;
			deviance /= Math.sqrt(segment.properties.length);
			interludeLeadProminence += deviance;
			
			deviance = hiatusFollowRatings.getRating(s.ordinal()) - 1;
			deviance /= Math.sqrt(segment.properties.length);
			interludeFollowProminence += deviance;
			
			if (maxNucleusLength > 1)
			{
				deviance = diphthongLeadRatings.getRating(s.ordinal()) - 1;
				deviance /= Math.sqrt(segment.properties.length);
				nucleusLeadProminence += deviance;
				
				deviance = diphthongFollowRatings.getRating(s.ordinal()) - 1;
				deviance /= Math.sqrt(segment.properties.length);
				nucleusFollowProminence += deviance;	
			}
			else
			{
				nucleusLeadProminence = 0;
				nucleusFollowProminence = 0;
			}
		}
		
//		System.out.printf("%s\t%.3f\t%.3f\t%.3f\n", segment.expression, onsetInitialProminence, nucleusLeadProminence, nucleusFollowProminence);
	}
	
	public boolean isConsonant()
	{
		return false;
	}
	
	protected void addInterlude(Constituent c, ConstituentLocation location)
	{
		// Calculate interlude's probability.
		// Base probability equals sum of following segment's interludeFollow and onsetInitial prominences
		double probability = c.content[0].interludeFollowProminence;
		
		// Set source and target libraries and probability
		ConstituentLibrary sourceLibrary, targetLibrary;
		switch(location)
		{
			case MEDIAL:
				sourceLibrary = medialNuclei;
				targetLibrary = followers;
				probability += c.content[0].medialProminence - 1;
				break;
			case TERMINAL:
				sourceLibrary = terminalNuclei;
				targetLibrary = terminalFollowers;
				probability += ((VowelPhoneme) c.content[0]).terminalProminence - 1;
				break;
			case ROOT:
				sourceLibrary = rootNuclei;
				targetLibrary = rootFollowers;
				probability += ((VowelPhoneme) c.content[0]).rootProminence - 1;
				break;
			default:
				return;
		}
		
		// If probability is positive, add this interlude
		if (probability > 0)
		{
			targetLibrary.add(new Constituent(c, probability));
		}
		
		
		// Consider adding diphthongs
		if (sourceLibrary.maxLength() == 2)
		{
			for (Constituent diphthong : sourceLibrary.getMembersOfLength(2))
				if (diphthong.content[0] == c.content[0])
				{
					// Set base probability equal to the next segment's prominence
					probability = diphthong.probability;
					
					// If probability is positive, add this interlude
					if (probability > 0)
						targetLibrary.add(new Constituent(diphthong, probability));
				}
		}
//		Print interlude statistics
//		System.out.printf("%.3f (%.3f + %,3f)", probability, p.interludeFollowProminence, p.onsetInitialProminence);
	}
	
	public void printHiatus()
	{
		System.out.println(getSegment().expression + ":");
		ConstituentLibrary[] libs = new ConstituentLibrary[] { followers, terminalFollowers, 
				rootFollowers };
		
		for (int j = 0; j < libs.length; j++)
		{
			System.out.print(libs[j].getLocation() + "  \t");
			for (int i = 1; i <= libs[j].maxLength(); i++)
			{
				for (Constituent c : libs[j].getMembersOfLength(i))
					System.out.printf("%s %.2f\t", c, c.getProbability() * libs[j].getLengthProbability(c.size()));
			}
			System.out.println();
		}
	}
	
	public void createFollowerLists(int medialNucleusLength, int terminalNucleusLength, int rootNucleusLength)
	{
		followers = new ConstituentLibrary(medialNucleusLength, ConstituentType.NUCLEUS, ConstituentLocation.MEDIAL);
		terminalFollowers = new ConstituentLibrary(terminalNucleusLength, ConstituentType.NUCLEUS, 
							ConstituentLocation.TERMINAL);
		rootFollowers = new ConstituentLibrary(rootNucleusLength, ConstituentType.NUCLEUS, ConstituentLocation.ROOT);
	}
	
	public void makeHiatus()
	{
		// TODO: all these references are to fields in Phonology
		makeHiatusForVowel(medialNuclei, leadProbabilities, followProbabilities);
		makeHiatusForVowel(terminalNuclei, leadProbabilities, followProbabilities);
		makeHiatusForVowel(rootNuclei, leadProbabilities, followProbabilities);
		
		// Normalize probabilities and sort the current vowel's interludes according to them
		rootFollowers.normalizeAll();
		rootFollowers.sortAll();
		rootFollowers.setLengthProbabilities(nucleusClusterWeight);
		
		followers.normalizeAll();
		followers.sortAll();
		followers.setLengthProbabilities(nucleusClusterWeight);
		
		terminalFollowers.normalizeAll();
		terminalFollowers.sortAll();
		terminalFollowers.setLengthProbabilities(nucleusClusterWeight);
	}
	
	private void makeHiatusForVowel(VowelPhoneme v1, ConstituentLibrary lib, double[] leadProbabilities,
			double[] followProbabilities, double hiatusOffset)
	{
		for (int j = 0; j < lib.countMembersOfLength(1); j++)
		{
			Phoneme v2 = lib.getMembersOfLength(1).get(j).content[0];
			if (!v1.segment.expression.equals(":"))
			{
				// Chance of representation ~= f(.3 x 4^(commonness - 3))
				// 3 -> .446, 2 -> .155, 1 -> .050
				double probability = 0.3 * Math.pow(4, Phonotactics.hiatusTransitions[v1.segment.getID()][v2.segment.getID()]
						- 3) + hiatusOffset;
				if (leadProbabilities[v1.segment.getID()] * followProbabilities[v2.segment.getID()] < probability)
				v1.addInterlude(lib.getMembersOfLength(1).get(j), lib.getLocation());
			}
		}
	}
}