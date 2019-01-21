/** Copyright 2018 Clayton Cooper
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
	String orthography;
	
	public Name()
	{
		this.syllables = new ArrayList<Syllable>();
	}
	
	/**
	 * Scans through the Name syllable by syllable and uses the language's stress rules to apply
	 * primary and secondary stresses.
	 * @since	1.1
	 */
	public void scan()
	{
		if (syllables.size() > 1)
			syllables.get(1).stress = Stress.PRIMARY;
	}
	
	public String toString()
	{
		if (orthography == null)
			parseOrthography();
		return orthography;
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
	
	public void parseOrthography()
	{
		Segment prev, curr = null;
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < syllables.size(); i++)
		{
			Syllable syl = syllables.get(i);
			
			// 1. Indicate stress and syllable breaks
			if (syl.stress == Stress.PRIMARY)
				sb.append('ˈ');
			else if (syl.stress == Stress.SECONDARY)
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
							sb.append(sb.charAt(sb.length() - 1));
						}
						
						// 3. Add diaeresis for applicable hiatus
						else if (!curr.isConsonant() && j == 0 && prev != null && !prev.isConsonant())
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
		orthography = sb.toString();
	}
		
	public int compareTo(Name other)
	{
		return toString().compareTo(other.toString());
	}
	
	
	private class Syllable
	{
		Constituent[] constituents; // 0 = onset, 1 = nucleus, 2 = coda
		Stress stress;
		
		public Syllable()
		{
			constituents = new Constituent[3];
			stress = Stress.NONE;
		}
	}
}

enum Stress { NONE, SECONDARY, PRIMARY }