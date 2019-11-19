/** Copyright 2018, 2019 Clayton Cooper
 *	
 *	This file is part of gengen2.
 *
 *	gengen2 is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	gengen2 is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with gengen2.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package gengenv2.structures;

import gengenv2.enums.ConstituentType;

/**
 * Represents a Phoneme in a particular positional context in a word - either an onset, a nucleus, or a coda.
 * @version	1.2
 * @since	1.1 (since 1.0 as SyllableSegment)
 */
public class Constituent implements Comparable<Constituent>
{
	public final ConstituentType type;
	private final Phoneme content;
	private double probability;
			
	/**
	 * Constructor sets the Constituent's essential parameters.
	 * @param	type		Constituent's type (onset, nucleus, coda)
	 * @param	content		Phoneme comprising the segment
	 * @param	probability	Probability of this segment appearing out of all syllable segments of same type and length
	 * @since	1.0
	 */
	public Constituent (ConstituentType type, Phoneme content, double probability)
	{
		this.type = type;
		this.content = content;
		this.probability = probability;
	}
	
	/**
	 * Creates a copy of the given Constituent with a new probability value
	 * @param old
	 * @param probability
	 * @since 1.2
	 */
	public Constituent (Constituent old, double probability)
	{
		this.type = old.type;
		this.content = old.content;
		this.probability = probability;
	}
	
	/**
	 * Copy constructor.
	 * @param	other	Constituent to be copied
	 * @since	1.0
	 */
	public Constituent(Constituent other)
	{
		this.type = other.type;
		this.content = other.content;
		this.probability = other.probability;
	}
	
	public Phoneme getContent()
	{
		return content;
	}
	
	
	public double getProbability()
	{
		return probability;
	}
	
	/**
	 * @return	The library of Constituents that might follow this Constituent's final phoneme
	 * 			across a syllable boundary
	 * @since	1.2
	 */
	public ConstituentLibrary followers()
	{
		if (content.isConsonant())
			return null;
		else
			return ((VowelPhoneme) content).getMedialFollowers();
	}
	
	public ConstituentLibrary getBridgePreceders()
	{
		if (content.isConsonant())
			return ((ConsonantPhoneme) content).getBridgePreceders();
		else
			return null;
	}
	
	public ConstituentLibrary followers(ConstituentType type)
	{
		try
		{
			if (type == ConstituentType.ONSET)
				return ((ConsonantPhoneme) content).getOnsetFollowers();
			else if (type == ConstituentType.CODA)
				return ((ConsonantPhoneme) content).getCodaPreceders();
			else if (type == ConstituentType.NUCLEUS)
				return ((VowelPhoneme) content).getNucleusFollowers();
			else
			{
				throw new Exception();
			}
		}
		catch (Exception e)
		{
			System.out.println("Couldn't obtain " + type + " followers for library \"" + content.toString().toUpperCase() + "\"");
			System.exit(1);
		}
		
		return null;
	}
	
	
	/**
	 * Returns a string containing the segment's constituent Phonemes in sequence.
	 * @return	The content of the syllable segment, in string form
	 * @since	1.0 
	 */
	public String toString()
	{
		return content.segment.expression;
	}
	
	/**
	 * Compares this Constituent with another to see if they contain the same sequence of Phonemes
	 * @param 	other	The sequence to be compared
	 * @return	True if both Constituents' content matches completely, otherwise false
	 * @since	1.2
	 */
	public boolean sameSequence(Constituent other)
	{
		return content == other.content;
	}

	/**
	 * Compares this Constituent's probability to that of another; returns 1 if this Constituent's probability is 
	 * higher, -1 if it is lower, and 0 if they are equal.
	 * @return	An int representing the relationship between this Constituent's probability and another's
	 * @since	1.2
	 */
	public int compareTo(Constituent c)
	{
		if (probability > c.probability)
			return 1;
		else if (probability < c.probability)
			return -1;
		return 0;
	}

	public void setProbability(double d) 
	{
		probability = d;
	}
}

/**
 * The structural role of a Constituent (a sequence of Phonemes) in a syllable. 
 * @since	1.0
 */