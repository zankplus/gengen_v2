package gengenv2.morphemes;

import gengenv2.ConstituentLibrary;
import gengenv2.enums.ConsonantProperty;
import gengenv2.enums.ConstituentLocation;
import gengenv2.enums.ConstituentType;

public class ConsonantPhoneme extends Phoneme
{
	public final double onsetClusterLeadProminence;
	public final double onsetClusterFollowProminence;
	public final double medialCodaProminence;
	public final double terminalCodaProminence;
	public final double codaClusterLeadProminence;
	public final double codaClusterFollowProminence;
	private double medialCodaChance; // Chance of a coda appearing before this phoneme in a bridge
	
	private double onsetClusterLeadChance;	// Chance that another phoneme will follow this one in an onset (compared to a vowel)
	private double codaClusterLeadChance;	// Chance that another phoneme will follow this one in a cluster (compared to a vowel)
	
	
	
	// Interlude properties
	private ConstituentLibrary onsetFollowers;
	private ConstituentLibrary codaPreceders;
	private ConstituentLibrary bridgePreceders;
	
	
	public ConsonantPhoneme(Segment segment,
							double medialProminence,
							double wordInitialProminence,
							double medialCodaProminence,
							double terminalCodaProminence,
							double onsetClusterLeadProminence,
							double onsetClusterFollowProminence,
							double codaClusterLeadProminence,
							double codaClusterFollowProminence,
							double interludeLeadProminence,
							double interludeFollowProminence,
							int maxOnsetLength,
							int maxCodaLength)
	{
		super(segment);
		this.medialProminence = medialProminence;
		this.wordInitialProminence = wordInitialProminence;
		this.medialCodaProminence = medialCodaProminence;
		this.terminalCodaProminence = terminalCodaProminence;
		
		this.onsetClusterLeadProminence	= onsetClusterLeadProminence;
		this.onsetClusterFollowProminence = onsetClusterFollowProminence;
		this.codaClusterLeadProminence    = codaClusterLeadProminence;
		this.codaClusterFollowProminence  = codaClusterFollowProminence;
		this.interludeLeadProminence  	= interludeLeadProminence;
		this.interludeFollowProminence	= interludeFollowProminence;
		
		if (maxOnsetLength > 1)
			onsetFollowers = new ConstituentLibrary(segment.expression, maxOnsetLength, ConstituentType.ONSET, ConstituentLocation.CLUSTER);
		if (maxCodaLength > 1)
			codaPreceders = new ConstituentLibrary(segment.expression, maxCodaLength, ConstituentType.CODA, ConstituentLocation.CLUSTER);
		
		bridgePreceders = null;
	}
	
	public ConstituentLibrary getBridgePreceders()
	{
		return bridgePreceders;
	}
	
	public ConstituentLibrary getOnsetFollowers()
	{
		return onsetFollowers;
	}
	
	public ConstituentLibrary getCodaPreceders()
	{
		return codaPreceders;
	}
	
	public void createBridgePrecedersList(int maxCodaLength)
	{
		bridgePreceders = new ConstituentLibrary(segment.expression, maxCodaLength, ConstituentType.CODA, ConstituentLocation.MEDIAL);
	}
	
	public void normalizeAndSortFollowers()
	{
		// Normalize probabilities and sort the current coda's interlude list according to them.
		bridgePreceders.normalizeAll();
		bridgePreceders.sortAll();
	}
	
	public boolean isConsonant()
	{
		return true;
	}
	
	public void calculateMedialCodaChance(double baseMedialCodaChance)
	{
		if (bridgePreceders == null)
			medialCodaChance = 0;
		medialCodaChance = baseMedialCodaChance * Math.log(bridgePreceders.size() + 1);
	}
	
	public double getMedialCodaChance()
	{
		return medialCodaChance;
	}
}