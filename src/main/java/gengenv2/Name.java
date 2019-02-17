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

package gengenv2;

import java.util.ArrayList;

import gengenv2.Phonology.Constituent;

public class Name implements Comparable<Name>
{
	Phonology language;
	ArrayList<Syllable> syllables;
	String plain;
	String ipa;
	String clear;
	double informationContent;
	
	public Name()
	{
		this.syllables = new ArrayList<Syllable>();
	}
	
	public String toString()
	{
		return getClear() + "\t" + getPlain() + "\t" + getIPA();
	}
	
	public String getPlain()
	{
		if (plain == null)
			renderPlain();
		return plain;
	}
	
	public String getIPA()
	{
		if (ipa == null)
			renderIPA();
		return ipa;
	}
	
	public String getClear()
	{
		if (clear == null)
			renderClear();
		return clear;
	}
	
	/**
	 * Adds a syllable Constituent to the name at the most immediate appropriate position, creating a new syllable
	 * if necessary
	 *  
	 * @param	c		The syllable constituent to be appended to the name
	 * @since	1.1
	 */
	public void add(Constituent c)
	{
		switch(c.type)
		{
			case ONSET:
			{
				Syllable syl = new Syllable();
				syl.constituents[0] = c;
				syllables.add(syl);
				break;
			}
				
			case NUCLEUS:
			{
				// Add a new syllable if this is the first constituent in the word
				if (syllables.size() == 0)
					syllables.add(new Syllable());
				
				// Otherwise, add a new syllable if the current syllable already has a nucleus
				else if (syllables.get(syllables.size() - 1).constituents[1] != null)
					syllables.add(new Syllable());
				
				syllables.get(syllables.size() - 1).constituents[1] = c;
				break;
			}	
			
			case CODA:
			{
				// Codas never start a new syllable, so they can be safely added to the end of the current one
				syllables.get(syllables.size() - 1).constituents[2] = c;
				break;
			}
			
			default:
			{
				System.err.println("Name.add(Constituent) received a Constituent with no type.");
				break;
			}
		}
	}	
	
	/**
	 * Renders the name without any diacritics or symbols (besides glottal stops)
	 * @since 1.1
	 */
	public void renderPlain()
	{
		Segment prev, curr = null;
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < syllables.size(); i++)
		{
			Syllable syl = syllables.get(i);
			
			for (int j = 0; j < 3; j++)
			{
				Constituent c = syl.constituents[j];
				if (c != null)
					for (int k = 0; k < c.content.length; k++)
					{
						prev = curr;
						curr = c.content[k].segment;
				
						// 1. Omit initial glottal stops
						if (sb.length() == 0 && curr.expression.equals("'"))
						{
							
						}
						
						// 2. Initial uppercase letter
						else if (sb.length() == 0)
						{
							sb.append(curr.expression.substring(0,1).toUpperCase() + curr.expression.substring(1));
						}
						
						// 2. Replace vowel lengtheners with a second of the corresponding vowel
						else if (curr.expression.equals(":") && j > 0)
						{
							String prevChar = "" + sb.charAt(sb.length() - 1); 
							sb.append(prevChar.toLowerCase());
						}
						else
							sb.append(curr.expression);
					}
			}	
		}
		plain = sb.toString();
	}
	
	public void renderClear()
	{
		Segment prev, curr = null;
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < syllables.size(); i++)
		{
			Syllable syl = syllables.get(i);

			for (int j = 0; j < 3; j++)
			{
				Constituent c = syl.constituents[j];
				if (c != null)
				{
					for (int k = 0; k < c.content.length; k++)
					{
						prev = curr;
						curr = c.content[k].segment;
				
						// 1. Omit initial glottal stops
						if (sb.length() == 0 && curr.expression.equals("'"))
						{
							
						}
						
						// 2. Initial uppercase letter
						else if (sb.length() == 0)
						{
							sb.append(curr.expression.substring(0,1).toUpperCase() + curr.expression.substring(1));
						}
						
						// 2. Add acute accents to mark unexpected emphasis
						else if (j == 1 && k == 0 && syl.stress == Stress.PRIMARY && 
							!((syllables.size() >= 3 && i == syllables.size() - 3 && !syllables.get(syllables.size() - 2).isHeavy()) 
									|| syllables.size() >= 3 && i == syllables.size() - 2 && syllables.get(syllables.size() - 2).isHeavy()
									|| syllables.size() == 2 && i == syllables.size() - 2))
						{
							String vowel = ((Vowel) curr).stress;
							if (sb.length() == 0)
								vowel = vowel.toUpperCase();
							sb.append(vowel);
						}
						
						// 3. Replace vowel lengtheners with a second of the corresponding vowel
						else if (curr.expression.equals(":") && j > 0)
						{
							sb.append(sb.charAt(sb.length() - 1));
						}
						
						// 4. Add diaeresis for applicable hiatus
						else if (j == 1 && k == 0 && prev != null && !prev.isConsonant())
						{
							VowelProperty currClose = (VowelProperty) curr.properties[0];
							VowelProperty prevClose = (VowelProperty) prev.properties[0];
							
							curr = (Vowel) curr;
							if (prev.expression.equals("y"))
								sb.append(((Vowel) curr).diaeresis);
							else if (currClose.ordinal() > prevClose.ordinal() || curr == prev)
								sb.append(((Vowel) curr).diaeresis);
							else
								sb.append(curr.expression);
						}
						else
							sb.append(curr.expression);
					}
				}
			}	
		}
		clear = sb.toString();
	}
	
	public void renderIPA()
	{
		Segment prev, curr = null;
		
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		
		for (int i = 0; i < syllables.size(); i++)
		{
			Syllable syl = syllables.get(i);
			
			// 1. Indicate stress and syllable breaks
			if (syl.stress == Stress.PRIMARY)
				sb.append('ˈ');
			else if (syl.stress == Stress.STRONG)
				sb.append('ˌ');
			else if (i > 0)
				sb.append('.');

			for (int j = 0; j < 3; j++)
			{
				Constituent c = syl.constituents[j];
				if (c != null)
				{
					for (int k = 0; k < c.content.length; k++)
					{
						prev = curr;
						curr = c.content[k].segment;
						sb.append(curr.ipa);
					}
				}
			}	
		}
		
		sb.append("]");
		ipa = sb.toString();
	}
		
	public int compareTo(Name other)
	{
		// return toString().compareTo(other.toString());
		if (informationContent > other.informationContent)
			return 1;
		else if (informationContent < other.informationContent)
			return -1;
		else
			return 0; 
	}

	class Syllable
	{
		Constituent[] constituents; // 0 = onset, 1 = nucleus, 2 = coda
		Stress stress;
		int index;
		
		public Syllable()
		{
			constituents = new Constituent[3];
			stress = Stress.WEAK;
			index = syllables.size();
		}
	
		/**
		 * Returns true if this syllable is considered heavy, depending on its position and its subjection to
		 * rules of extrametricality.
		 * 
		 * @param 	applyConsonantExtrametricality	If true, the last consonant of a terminal syllable is ignored
		 * @return	true if the syllable is heavy, false if it's light
		 */
		public boolean isHeavy(boolean applyConsonantExtrametricality)
		{
			Syllable syl = syllables.get(index);
			
			// If the final syllable is accessed, return true if the syllable includes a complex nucleus or complex coda
			if (index == syllables.size() - 1)
			{
				if (syl.constituents[1].content.length > 1 ||
						((syl.constituents[2] != null) && 
							((syl.constituents[2].content.length >= 2 && !applyConsonantExtrametricality) ||
									syl.constituents[2].content.length >= 3)))
				{
					return true;
				}
			}
			// Otherwise, return true if the syllable includes a complex nucleus or any coda, or the next syllable includes
			// a complex onset
			else if (index < syllables.size() - 1 &&
					(syl.constituents[1].content.length > 1 ||
							syl.constituents[2] != null ||
									syllables.get(index + 1).constituents[0] != null && 
										syllables.get(index + 1).constituents[0].content.length > 1))
				return true;
			
			return false;
		}
		
		/**
		 * 
		 * @return
		 */
		public boolean isHeavy()
		{
			return isHeavy(false);
		}
	}
}

enum Stress { WEAK, STRONG, PRIMARY }