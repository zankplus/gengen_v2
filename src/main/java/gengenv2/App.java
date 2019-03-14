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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Maven's default program for demonstrating Gengen. Mostly used for testing.
 * @since	1.0
 */

public class App 
{
	/**
	 * Generates a Phonology and generates a number of names, which are then printed along with their
	 * information content measurement and IPA pronunciation.
	 * @param	args	Default parameter
	 * @since	1.0
	 */
    public static void main(String[] args)
    {   
    	// Revisit this
    	Phonology p = new Phonology();
    	
//    	p.printInventory(p.initialOnsets.getMembersOfLength(2));
    	
//		System.out.println("MEDIAL");
//    	for (int i = 1; i <= p.medialOnsets.maxLength(); i++)
//    		p.printInventory(p.medialOnsets.getMembersOfLength(i));
//    	System.out.println("INITIAL");
//    	for (int i = 1; i <= p.initialOnsets.maxLength(); i++)
//    		p.printInventory(p.initialOnsets.getMembersOfLength(i));
		
    	p.compareOnsets();
		
		List<Name> names = p.makeNames(60);
		Collections.sort(names, new Comparator<Name>() {
			public int compare(Name a, Name b)
			{
				// return toString().compareTo(other.toString());
				if (a.getInformationContent() > b.getInformationContent())
					return 1;
				else if (a.getInformationContent() < b.getInformationContent())
					return -1;
				else
					return 0; 
			}
		});
		
		Name langName = p.makeName();
		System.out.println("Language: " + langName.getDefault().toUpperCase());
		
		int columns = 3;
		for (int j = 0; j < names.size() / columns; j++)
		{
			for (int k = 0; k < columns; k++)
			{
				Name name = names.get(k * names.size() / columns + j);    				
				System.out.printf("%.3f\t", name.getInformationContent());
				System.out.print(padString(name.getDefault(), 16) + padString(name.getIPA(), 24));
				
			}
			System.out.println();
		}
    }
    
    /**
     * Pads the given string with tabs and whitespaces until it reaches the given length. Used to format
     * console output.
     * @param s		The string to be padded
     * @param len	The length to which the string should be padded
     * @return
     */
    public static String padString(String s, int len)
    {
    	StringBuilder result = new StringBuilder();
    	result.append(s);
    	
    	len -= s.length();
		while (len % 8 > 0)
		{
			result.append(" ");
			len--;
		}
		while (len > 0)
		{
			result.append("\t");
			len -= 8;
		}
		
		return result.toString();
    }
}
