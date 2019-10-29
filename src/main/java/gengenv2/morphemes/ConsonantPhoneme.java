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
	
	private double onsetClusterLeadChance;	// Chance that another phoneme will follow this one in an onset (compared to a vowel)
	private double codaClusterLeadChance;	// Chance that another phoneme will follow this one in a cluster (compared to a vowel)
	
	// Interlude properties
	private ConstituentLibrary initialOnsetFollowers;
	private ConstituentLibrary medialOnsetFollowers;
	private ConstituentLibrary medialCodaFollowers;
	private ConstituentLibrary terminalCodaFollowers;
	private ConstituentLibrary bridgeFollowers;
	
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
							double interludeFollowProminence)
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
		
		bridgeFollowers = null;
	}
	
	public ConstituentLibrary getBridgeFollowers()
	{
		return bridgeFollowers;
	}
	
	public void createFollowerList(int medialOnsetsLength)
	{
		bridgeFollowers = new ConstituentLibrary(medialOnsetsLength, ConstituentType.ONSET,
				ConstituentLocation.MEDIAL);
	}
	
	public void normalizeAndSortFollowers(double onsetClusterWeight)
	{
		// Normalize probabilities and sort the current coda's interlude list according to them.
		bridgeFollowers.normalizeAll();
		bridgeFollowers.sortAll();
		bridgeFollowers.setLengthProbabilities(onsetClusterWeight);
	}
	
	public boolean isConsonant()
	{
		return true;
	}
}