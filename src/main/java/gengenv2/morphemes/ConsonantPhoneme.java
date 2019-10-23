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
	
	public ConsonantPhoneme(Segment segment)
	{
		super(segment);
		
		// Assign default prominence values
		medialProminence			 = 1;
		wordInitialProminence	 	 = 1;
		medialCodaProminence		 = 1;
		terminalCodaProminence		 = 1;
		
		onsetClusterLeadProminence   = 1;
		onsetClusterFollowProminence = 1;
		codaClusterLeadProminence    = 1;
		codaClusterFollowProminence  = 1;
		interludeLeadProminence  	 = 1;
		interludeFollowProminence 	 = 1;
		
		// Apply offsets
		if (segment.expression.equals("ng"))
		{
			medialProminence   		  -= onsetNgOffset;
			wordInitialProminence	  -= onsetNgOffset;
			interludeFollowProminence -= onsetNgOffset;
		}
		else if (segment.expression.equals("'"))
		{
			medialCodaProminence		-= codaGlottalStopOffset;
			terminalCodaProminence		-= codaGlottalStopOffset;
			codaClusterLeadProminence	-= codaGlottalStopOffset;
			codaClusterFollowProminence -= codaGlottalStopOffset;
		}
		
		/* Calculate prominence values. 
		 * Math note: initialProminence is the result of combining all the prominence values of the
		 * segment's properties, with mean 1. The deviance of each prominence is scaled by the root of
		 * the number of the segment's properties; this ensures that all intialProminence values are
		 * distributed, in effect, with the same standard deviation, regardless of how many values are
		 * added to make it (normally, adding random variables increases the stdev of the sum). The
		 * same is true of the cluster prominence values as well.
		 */
		for (ConsonantProperty s : ((Consonant) segment).properties)
		{
			// Initial properties
			double deviance = baseConsonantRatings.getRating(s.ordinal()) - 1;
			deviance /= Math.sqrt(segment.properties.length);
			medialProminence += deviance;
			
			deviance = initialOnsetRatings.getRating(s.ordinal()) - 1;
			deviance /= Math.sqrt(segment.properties.length);
			wordInitialProminence += deviance;
			
			if (maxCodaLength > 0)
			{
				deviance = baseCodaRatings.getRating(s.ordinal()) - 1;
				deviance /= Math.sqrt(segment.properties.length);
				medialCodaProminence += deviance;
				
				if (features.terminalCodas != Feature.NO)
				{
					deviance = terminalCodaRatings.getRating(s.ordinal()) - 1;
					deviance /= Math.sqrt(segment.properties.length);
					terminalCodaProminence += deviance;
				}
				
			}
			
			// Onset cluster properties
			if (maxOnsetLength > 1)
			{
				deviance = onsetClusterLeadRatings.getRating(s.ordinal()) - 1;
				deviance /= Math.sqrt(segment.properties.length);
				onsetClusterLeadProminence += deviance;
				
				deviance = onsetClusterFollowRatings.getRating(s.ordinal()) - 1;
				deviance /= Math.sqrt(segment.properties.length);
				onsetClusterFollowProminence += deviance;
			}
			
			// Coda cluster properties
			if (maxCodaLength > 1)
			{
				deviance = codaClusterLeadRatings.getRating(s.ordinal()) - 1;
				deviance /= Math.sqrt(segment.properties.length);
				codaClusterLeadProminence += deviance;
				
				deviance = codaClusterFollowRatings.getRating(s.ordinal()) - 1;
				deviance /= Math.sqrt(segment.properties.length);
				codaClusterFollowProminence += deviance;
				
				if (features.medialCodas != Feature.NO)
				{
					deviance = interludeLeadRatings.getRating(s.ordinal()) - 1;
					deviance /= Math.sqrt(segment.properties.length);
					interludeLeadProminence += deviance;
					
					deviance = interludeFollowRatings.getRating(s.ordinal()) - 1;
					deviance /= Math.sqrt(segment.properties.length);
					interludeFollowProminence += deviance;
				}
			}
		}
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