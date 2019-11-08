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

import gengenv2.enums.SegmentProperty;

/**
 * Abstract class representing a phonetic segment, or a discrete and distinct unit of sound. These are extended 
 * into Consonant and Vowel classes with their own sets of properties, and form the basis of Phonemes in the
 * Phonology class.
 *  
 * @author	Clayton Cooper
 * @version	1.0
 * @since	1.0
 */
public abstract class Segment extends Object
{
	public final String expression;
	public final String ipa;
	public final SegmentProperty[] properties;
	public final int transitionCategory;
	
	/**
	 * A basic constructor that sets the most fundamental variables of the Segment. The id field is not set here
	 * but in the constructors of Segment's child classes, as the id of each Consonant is unique among Consonants,
	 * and just so for Vowels, and so this value depends on the static counts only within those classes.
	 *    
	 * @param expression			The character or characters used to denote this sound orthographically
	 * @param ipa					The IPA character representing this segment
	 * @param transitionCategory	The category to which this sound belongs for the purposes of cluster construction
	 * @param properties			The list of consonant or vowel properties that define this sound
	 */
	public Segment(String expression, String ipa, int transitionCategory, SegmentProperty[] properties)
	{
		this.expression = expression;
		this.ipa = ipa;
		this.transitionCategory = transitionCategory;
		this.properties = properties;
	}
	
	/**
	 * Returns true if this Segment is a consonant. Otherwise it is a vowel and returns falls. 
	 * @return	true if this segment is a consnant, false if it is a vowel
	 */
	abstract public boolean isConsonant();
	abstract public int getID();
}