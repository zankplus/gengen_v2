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

/**
 * Contains a summary of linguistic data about a Phonology.
 * @since	1.1.2
 */
public class LanguageData
{
	public String medialSyllableStructure;
	public String terminalSyllableStructure;
	public String[] consonants;
	public String[] vowels;
	
	/**
	 * Creates a summary of linguistic data for the given Phonology.
	 * @param 	p
	 * @since	1.1.2
	 */
	public LanguageData(Phonology p)
	{
//		// Determine syllable structure
//		medialSyllableStructure = "C";
//		if (p.medialOnsets.maxLength() > 1)
//		{
//			for (int i = 1; i < p.medialOnsets.maxLength(); i++)
//				medialSyllableStructure += "(C";
//			for (int i = 1; i < p.medialOnsets.maxLength(); i++)
//				medialSyllableStructure += ")";
//		}
//		medialSyllableStructure += "V";
//		if (p.nuclei.maxLength() == 2)
//			medialSyllableStructure += "(V)";
//		
//		terminalSyllableStructure = medialSyllableStructure;
//		if (p.medialCodas.maxLength() > 0)
//		{
//			String coda = "";
//			for (int i = 0; i < p.medialCodas.maxLength(); i++)
//				medialSyllableStructure += "(C";
//			for (int i = 0; i < p.medialCodas.maxLength(); i++)
//				medialSyllableStructure += ")";
//			
//			if (p.baseMedialCodaChance > 0)
//				medialSyllableStructure += coda;
//			if (p.baseTerminalCodaChance > 0)
////				if (p.assembly.getEmptyCodaChance() == 0)
////					terminalSyllableStructure += coda.substring(1, coda.length() - 1);
////				else
//					terminalSyllableStructure += coda;
//		}
//		
//		// Summarize consonant inventory
//		consonants = new String[p.consonantInventory.length];
//		for (int i = 0; i < p.consonantInventory.length; i++)
//			consonants[i] = p.consonantInventory[i].segment.expression;
//		
//		// Summarize vowel inventory
//		vowels = new String[p.vowelInventory.length];
//		for (int i = 0; i < p.vowelInventory.length; i++){
//			vowels[i] = p.vowelInventory[i].segment.expression;
//		}
	}
}