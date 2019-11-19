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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes.Name;

import gengenv2.structures.GengenName;
import gengenv2.structures.Morpheme;
import gengenv2.structures.MorphemeLibrary;
import gengenv2.structures.Suffix;
import gengenv2.structures.VowelPhoneme;

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
    	Phonology p = new Phonology();
//    	Phonology p = new Phonology(8724563618997116630L);	// lots of hiatus
//    	Phonology p = new Phonology(3582677372517196549L); // practice phonology exhibits every method + compensatory lengthening
//    	Phonology p = new Phonology(-956763732658690744L); // this is the one i posted on twitter about
    	
    	testSuffixes(p);
    	
//    	SuffixLibrary lib = new SuffixLibrary();
//    	for (int i = 0; i < 5; i++)
//    	{
//    		Suffix m = (Suffix) p.makeSuffix();
//    		lib.addSuffix(m);
//    	}
//    	
//    	lib.exaggerate(2);
//    	lib.sort();
//    	lib.zipfScale();
//    	lib.normalize();
//    	System.out.println("Suffixes");
//    	lib.printMembers();
    	
//    	Phonology p = new Phonology(6689165834100475041L);
//    	Phonology p = new Phonology(-2360869903237111562L);
//		Phonology p = new Phonology(1443048374146254679L);// good luck language
//    	Phonology p = new Phonology(-7749542029527716838L);
//    	Phonology p = new Phonology(2474003871113996882L);
//    	Phonology p = new Phonology(2956502808346677711L);
//    	Phonology p = new Phonology(-4686731362303332792L);	// ecko!!!!
    	
//    	p.printInventory(p.initialOnsets.getMembersOfLength(2));
    	
//		System.out.println("MEDIAL");
//    	for (int i = 1; i <= p.medialOnsets.maxLength(); i++)
//    		p.printInventory(p.medialOnsets.getMembersOfLength(i));
//    	System.out.println("INITIAL");
//    	for (int i = 1; i <= p.initialOnsets.maxLength(); i++)
//    		p.printInventory(p.initialOnsets.getMembersOfLength(i));
		
    	
//    	p.compareOnsets();
//    	p.printHiatus();
//    	
//    	System.out.println();
//    	p.compareNuclei();
//    	p.printHiatus();
    	
//    	for (NameEntry ne : p.suffixes.getLibrary())
//    	{
//    		System.out.printf("%s%.3f\t%s\t%.3f\n", padString(ne.getName().getPlain().toLowerCase(), 16), ne.getProbability(), ne.getName().strength, ne.getName().getInformationContent());
//    	}
//    	System.out.println();
		
		
//		testNames(p);
//    	showOffRoots(p);
    }
   
    /**
     * Pads the given string with tabs and whitespaces until it reaches the given length. Used to format
     * console output.
     * @param s		The string to be padded
     * @param len	The length to which the string should be padded
     * @return
     */
   

//	public static void showOffRoots(Phonology p)
//	{
//		List<Name> names = p.makeNames(60);
//		Collections.sort(names, new Comparator<Name>() {
//			public int compare(Name a, Name b)
//			{
//				// return toString().compareTo(other.toString());
//				if (a.getInformationContent() > b.getInformationContent())
//					return 1;
//				else if (a.getInformationContent() < b.getInformationContent())
//					return -1;
//				else
//					return 0; 
//			}
//		});
//		
//		ArrayList<NameEntry> suffixes = p.suffixes.getLibrary();
//		
////		Name[] suffixes = new Name[] { p.suffixes.getLibrary().get(1).getName(),
//////										p.suffixes.getLibrary().get(8).getName(),
//////										p.suffixes.getLibrary().get(4).getName(),
//////										p.suffixes.getLibrary().get(5).getName() };
////				p.suffixes.getLibrary().get(0).getName(),
////				p.suffixes.getLibrary().get(2).getName(),
////				p.suffixes.getLibrary().get(3).getName() };
//		
//		// Suffix labels
//		System.out.print("\t\t");
//		for (NameEntry suffix : suffixes)
//		{
//			String suffText = suffix.getName() == Name.NEW_SUFFIX ? "?????" : suffix.getName().getPlain(); 
//			System.out.print(padString(suffText.toUpperCase(), 16));
//		}
//		System.out.println();
//		
//		for (Name name : names)
//		{
//			System.out.print(padString(name.getPlain().toUpperCase(), 16));
//			for (NameEntry suffix : suffixes)
//			{
//				Name result = p.combinationRules.combine(name, suffix.getName(), true);
//				System.out.print(padString(result == null ? "" : result.getDefault(), 16));
//			}
//			System.out.println();
//		}
//	}
    
    public static void testSuffixes(Phonology p)
    {
    	System.out.println();
    	for (int i = 0; i < p.morphology.nounClasses.length; i++)
		{
			System.out.print(MorphemeLibrary.padString("Noun class " + (i + 1), 40));
		}
    	System.out.println();
    	
    	for (int j = 0; j < 10; j++)
		{
    		for (int i = 0; i < p.morphology.nounClasses.length; i++)
    		{
    			GengenName name = p.makeName(p.morphology.nounClasses[i]);
    			System.out.print(MorphemeLibrary.padString(name.phonemesToString() + " / " + name.toString(), 40));
    		}
    		System.out.println();
    	}
    }
}