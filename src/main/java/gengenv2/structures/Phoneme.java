package gengenv2.structures;

import gengenv2.Phonology;
import gengenv2.enums.ConsonantProperty;
import gengenv2.enums.ConstituentLocation;
import gengenv2.enums.ConstituentType;
import gengenv2.enums.SegmentProperty;

/**
 * A phoneme, or basic unit of sound. A Phoneme is an instance of a Segment specific to a given Phonology;
 * the Segment determines what sound it makes, but the Phoneme includes its own rules for how that sound
 * is used in the language. Accordingly, the Phoneme object includes numerous prominence values, as well
 * as (if appropriate) an inventory of interludes available to that sound.
 * @since	1.0
 */
public abstract class Phoneme
{
	public final Segment segment;
	
	// Prominences
	double medialProminence;
	double wordInitialProminence;
	double interludeLeadProminence;		
	double interludeFollowProminence;
	private boolean[][] validPositions;
	
	
	/**
	 * This constructor sets all the appropriate prominence values, according to whether the Phoneme
	 * is a consonant or a vowel.
	 * @param	parent		The phonology to which this Phoneme belongs
	 * @param	segment		The phonetic segment that characterizes this phoneme
	 * @since	1.0 
	 */
	public Phoneme(Segment segment)
	{
		this.segment = segment;
		validPositions = new boolean[ConstituentType.values().length][ConstituentLocation.values().length];
	}
	
	abstract boolean isConsonant();
	
	public double getInterludeLeadProminence()
	{
		return interludeLeadProminence;
	}
	
	public double getInterludeFollowProminence()
	{
		return interludeFollowProminence;
	}
	
	public double getMedialProminence()
	{
		return medialProminence;
	}
	
	public double getInitialProminence()
	{
		return wordInitialProminence;
	}
	
	public String toString()
	{
		if (segment != null)
			return "[" + segment.expression + "]";
		else
			return "";
	}
	
	/**
	 * Returns true if a nasal cluster has unharmonious voicing, i.e.,
	 * 1.  the first segment is a NASAL, and either
	 * 2a. the second segment is a plosive and its place of articulation differs from the first, OR
	 * 2b. the second segment is postalveolar and the first is not nasal
	 * @param	p1	The first segment
	 * @param	p2	The second segment
	 * @since	1.0
	 */
	public static boolean isDissonantNasalCluster(Phoneme p1, Phoneme p2)
	{
		if (p1.segment.properties[0] == ConsonantProperty.NASAL &&
				((p2.segment.properties[0] == ConsonantProperty.PLOSIVE && p1.segment.properties[1] != p2.segment.properties[1]) || 
				 (p2.segment.properties[1] == ConsonantProperty.POSTALVEOLAR && p1.segment.properties[1] != ConsonantProperty.ALVEOLAR)))
			{
				return true;
			}
		
		return false;
	}
	
	/**
	 * Returns true if a cluster disagrees in voicing, i.e.,
	 * 1.  both segments are plosives, affricates, or fricatives, and either
	 * 2a. the first is voiced and the second is unvoiced, or
	 * 2b. vice versa
	 * @param	p1	The first segment
	 * @param	p2	The second segment
	 * @since	1.0
	 * */
	public static boolean isUnequalVoicing(Phoneme p1, Phoneme p2)
	{
		SegmentProperty place1 = p1.segment.properties[0];
		SegmentProperty place2 = p2.segment.properties[0];
		SegmentProperty voice1 = p1.segment.properties[1];
		SegmentProperty voice2 = p2.segment.properties[1];
		
		if ((place1 == ConsonantProperty.PLOSIVE || place1 == ConsonantProperty.AFFRICATE || place1 == ConsonantProperty.FRICATIVE) &&
			(place2 == ConsonantProperty.PLOSIVE || place2 == ConsonantProperty.AFFRICATE || place2 == ConsonantProperty.FRICATIVE) &&
			(voice1 == ConsonantProperty.VOICED  || voice1 == ConsonantProperty.VOICELESS) &&
			(voice2 == ConsonantProperty.VOICED  || voice2 == ConsonantProperty.VOICELESS) &&
			(voice1 != voice2))
			{
				return true;
			}
		
		return false;
	}
	
	public boolean isValidInPosition(ConstituentLocation location, ConstituentType type)
	{
		return validPositions[location.ordinal()][type.ordinal()];
	}
	
	public void setValidInPosition(ConstituentLocation location, ConstituentType type)
	{
		validPositions[location.ordinal()][type.ordinal()] = true;
	}
}



