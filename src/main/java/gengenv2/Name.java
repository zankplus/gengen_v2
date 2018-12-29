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

import gengenv2.Phonology.SyllableSegment;

public class Name implements Comparable<Name>
{
	Phonology language;
	SyllableSegment[] segments;
	String orthography;
	
	public Name(ArrayList<SyllableSegment> content)
	{
		this.segments = content.toArray(new SyllableSegment[content.size()]);
	}
	
	public String toString()
	{
		if (orthography == null)
		{
			Segment prev, curr = null;
			
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < segments.length; i++)
			{
				for (int j = 0; j < segments[i].content.length; j++)
				{
					prev = curr;
					curr = segments[i].content[j].segment;
					
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
			orthography = sb.toString();
		}
		
		return orthography;
	}
	
	public int compareTo(Name other)
	{
		return orthography.compareTo(other.orthography);
	}
}
