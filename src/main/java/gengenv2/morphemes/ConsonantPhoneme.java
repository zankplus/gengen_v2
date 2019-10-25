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
		
	}
	
	public void createFollowerList(int medialOnsetsLength)
	{
		followers = new ConstituentLibrary(medialOnsetsLength, ConstituentType.ONSET,
				ConstituentLocation.MEDIAL);
	}
	
	public void normalizeAndSortFollowers(double onsetClusterWeight)
	{
		// Normalize probabilities and sort the current coda's interlude list according to them.
		followers.normalizeAll();
		followers.sortAll();
		followers.setLengthProbabilities(onsetClusterWeight);
	}
	
	public boolean isConsonant()
	{
		return true;
	}
}