package gengenv2.structures;
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

import java.util.ArrayList;

import gengenv2.Phonology;
import gengenv2.structures.Constituent;
import gengenv2.structures.Segment;
import hiatusResolution.DiphthongFormation;
import hiatusResolution.Diaeresis;
import hiatusResolution.HiatusResolutionMethod;
import hiatusResolution.VowelElision;

/**
 * The underlying representation for a phonological word produced by a Phonology. In addition to containing
 * the string representation of a name, the Name class stores the sequence of Phonemes that constitutes the name,
 * as well as alternative string representations.
 * @version	1.2
 * @since	1.1
 */
public class GengenName
{
	private Phonology parentLanguage;
	private ArrayList<Morpheme> constituentMorphemes;
	private ArrayList<PhonemeInstance> phonemes;
	private NounClass nounClass;
	
	private String defaultRep;				// Recommended representation with diacritics and symbols for clarity
	private String plain;					// Representation of the Name free of diacritics and symbols (except ')
	private String ipa;						// IPA representation of Name for unambiguous pronunciation
	private double informationContent;		// Information content, roughly representing the complexity of this Name
	
	
	/**
	 * Creates a new Name with an empty list of Syllables and specifies whether or not it's a suffix.
	 * @since	1.1
	 */
	public GengenName(Phonology parentLanguage)
	{
		this.parentLanguage = parentLanguage;
		constituentMorphemes = new ArrayList<Morpheme>();
		phonemes = new ArrayList<PhonemeInstance>();
	}
	
	public void addRoot(Root root)
	{
		constituentMorphemes.add(root);
		for (Phoneme p : root.phonemes)
			addPhoneme(p);
	}
	
	public void addSuffix(Suffix suffix)
	{
		
		constituentMorphemes.add(suffix);
		for (Phoneme p : suffix.phonemes)
			addPhoneme(p);

		
		if (suffix.phonemes.size() > 1 && suffix.phonemes.get(0) == ConsonantPhoneme.emptyOnset && !suffix.phonemes.get(1).isConsonant())
		{
			int v2Index = phonemes.size() - suffix.phonemes.size() + 1;
			parentLanguage.getMorphology().resolveHiatus(phonemes, v2Index);
			
			
		}
	}
	
	public void addPhoneme(Phoneme p)
	{
		phonemes.add(new PhonemeInstance(p));
	}
	
	public void syllabify()
	{
		
		
	}
	
	/**
	 * Renders the Name with diacritics to mark stress and hiatus, and hyphens to disambiguate clusters, and
	 * saves the result in a variable.
	 * @since 1.1
	 */
	public void renderDefault()
	{
//		Segment prev, curr = null;
//		
//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < syllables.size(); i++)
//		{
//			Syllable syl = syllables.get(i);
//	
//			for (int j = 0; j < 3; j++)
//			{
//				Constituent c = syl.constituents[j];
//				if (c != null)
//				{
//					for (int k = 0; k < c.content.length; k++)
//					{
//						prev = curr;
//						curr = c.content[k].segment;
//				
//						// 1. Omit initial glottal stops
//						if (sb.length() == 0 && curr.expression.equals("'"))
//						{
//							
//						}
//						
//						// 2. Add acute accents to mark unexpected emphasis
//						else if (j == 1 && k == 0 && syl.stress == Stress.PRIMARY && 
//							!((syllables.size() >= 3 && i == syllables.size() - 3 && !syllables.get(syllables.size() - 2).isHeavy()) 
//									|| syllables.size() >= 3 && i == syllables.size() - 2 && syllables.get(syllables.size() - 2).isHeavy()
//									|| syllables.size() == 2 && i == syllables.size() - 2))
//						{
//							String vowel = ((Vowel) curr).stress;
//							if (sb.length() == 0)
//								vowel = vowel.toUpperCase();
//							sb.append(vowel);
//						}
//						
//						// 2. Initial uppercase letter
//						else if (sb.length() == 0)
//						{
//							sb.append(curr.expression.substring(0,1).toUpperCase() + curr.expression.substring(1));
//						}
//						
//						// 3. Replace vowel lengtheners with a second of the corresponding vowel
//						else if (curr.expression.equals(":") && j > 0)
//						{
//							String prevChar = "" + sb.charAt(sb.length() - 1); 
//							sb.append(prevChar.toLowerCase());
//						}
//						
//						// 4. Add diaeresis for applicable hiatus
//						else if (j == 1 && k == 0 && prev != null && !prev.isConsonant())
//						{
//							VowelProperty currClose = (VowelProperty) curr.properties[0];
//							VowelProperty prevClose = (VowelProperty) prev.properties[0];
//							
//							curr = (Vowel) curr;
//							if (prev.expression.equals("y"))
//								sb.append(((Vowel) curr).diaeresis);
//							else if (currClose.ordinal() > prevClose.ordinal() || curr == prev)
//								sb.append(((Vowel) curr).diaeresis);
//							else
//								sb.append(curr.expression);
//						}
//						
//						// 5. Add hyphens before post-initial 'ng' onsets
//						else if (j == 0 && curr.expression.equals("ng"))
//						{
//							sb.append("-");
//							sb.append(curr.expression);
//						}
//						else
//							sb.append(curr.expression);
//					}
//				}
//			}	
//		}
//		defaultRep = sb.toString();
	}

	/**
	 * Renders the Name without any diacritics or symbols (besides glottal stops) and saves the result in a variable.
	 * @since 1.1
	 */
	public void renderPlain()
	{
//		Segment curr = null;
//		
//		StringBuilder sb = new StringBuilder();
//		
//		if (type == WordType.SUFFIX)
//			sb.append("-");
//		
//		for (int i = 0; i < syllables.size(); i++)
//		{
//			Syllable syl = syllables.get(i);
//			
//			for (int j = 0; j < 3; j++)
//			{
//				Constituent c = syl.constituents[j];
//				
//				if (j == 1 && (((type == WordType.SUFFIX && strength == RootStrength.WEAK && i == 0) ||
//						type == WordType.STEM && strength == RootStrength.WEAK && i == syllables.size() - 1)))
//					sb.append("(");
//				
//				if (c != null)
//					for (int k = 0; k < c.content.length; k++)
//					{
//						curr = c.content[k].segment;
//				
//						// 1. Initial uppercase letter
//						if (sb.length() == 0)
//						{
//							sb.append(curr.expression.substring(0,1).toUpperCase() + curr.expression.substring(1));
//						}
//						
//						// 2. Replace vowel lengtheners with a second of the corresponding vowel
//						else if (curr.expression.equals(":") && j > 0)
//						{
//							String prevChar = "" + sb.charAt(sb.length() - 1); 
//							sb.append(prevChar.toLowerCase());
//						}
//						else
//							sb.append(curr.expression);
//					}
//				if (j == 1 && (((type == WordType.SUFFIX && strength == RootStrength.WEAK && i == 0) ||
//						type == WordType.STEM && strength == RootStrength.WEAK && i == syllables.size() - 1)))
//					sb.append(")");
//			}	
//		}
//		
//		if (type == WordType.STEM)
//			sb.append("-");
//		
//		plain = sb.toString();
	}

	/**
	 * Renders the Name in IPA symbols and stores the result in a variable.
	 * @since 1.1
	 */
	public void renderIPA()
	{
		Segment curr = null;
		
//		StringBuilder sb = new StringBuilder();
//		sb.append("[");
//		
//		for (int i = 0; i < syllables.size(); i++)
//		{
//			Syllable syl = syllables.get(i);
//			
//			// 1. Indicate stress and syllable breaks
//			if (syl.stress == Stress.PRIMARY)
//				sb.append('ˈ');
//			else if (syl.stress == Stress.STRONG)
//				sb.append('ˌ');
//			else if (i > 0)
//				sb.append('.');
//	
//			for (int j = 0; j < 3; j++)
//			{
//				Constituent c = syl.constituents[j];
//				if (c != null)
//				{
//					for (int k = 0; k < c.content.length; k++)
//					{
//						curr = c.content[k].segment;
//						sb.append(curr.ipa);
//					}
//				}
//			}	
//		}
//		
//		sb.append("]");
//		ipa = sb.toString();
	}

	/**
	 * @return	The recommended representation of this Name, with symbols and diacritics to clarify pronunciation
	 * @since	1.1
	 */
	public String getDefault()
	{
		if (defaultRep == null)
			renderDefault();
		return defaultRep;
	}

	/**
	 * @return	A concise representation of the Name with no diacritics or symbols, except the glottal stop (')
	 * @since	1.1
	 */
	public String getPlain()
	{
		if (plain == null)
			renderPlain();
		return plain;
	}
	
	/**
	 * @return	A representation of the Name in IPA symbols
	 * @since	1.1
	 */
	public String getIPA()
	{
		if (ipa == null)
			renderIPA();
		return ipa;
	}

	/** 
	 * @return	A measurement of the information content of this Name
	 */
	public double getInformationContent()
	{
		return informationContent;
	}
	
	/**
	 * @param ic	Value to which to set this Name's information content
	 */
	public void setInformationContent(double ic)
	{
		this.informationContent = ic;
	}

	/**
	 * @return	The default representation of the Name
	 * @since	1.1
	 */
	public String toString()
	{
//		return getDefault();
		String result = "";
		for (int i = 0; i < constituentMorphemes.size() - 1; i++)
			result += constituentMorphemes.get(i) + " + ";
		result += constituentMorphemes.get(constituentMorphemes.size() - 1);
		
		return result;
	}
	
	public String phonemesToString()
	{
		String result = "";
		for (PhonemeInstance p : phonemes)
			result += p.getPhoneme().segment.expression;
		return result;
	}
	
	/**
	 * Returns true if the given Name contains the same sequence of Phonemes as this one
	 * @param 	The Name against which to be compared
	 * @return	True if both names contain the same sequence of Phonemes, otherwise false
	 * @since	1.2
	 */
	public boolean equals(GengenName other)
	{
		return this.getIPA().equals(other.getIPA()); 
	}
	
	/**
	 * The basic unit of the Name, the Syllable comprises 3 syllable Constituents (some of which may be empty):
	 * an onset (optional), a nucleus (required), and a coda (optional). 
	 * @since	1.1
	 */
	class Syllable
	{
		Constituent[] constituents; // References to each of this Syllable's Constituents.
									// 0 = onset, 1 = nucleus, 2 = coda
//		Stress stress;				// Denotes the strength of this syllable's Stress
		int index;					// This Syllable's location within the Name
		
		/**
		 * Produces a new Syllable with an empty array of Constituents and "weak" stress. The index is
		 * set on the assumption that new Syllables are always added to the end of the Name. 
		 */
		public Syllable()
		{
			
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
			return false;
		}
		
		/**
		 * @return	true if the current syllable is heavy, false if it's light
		 */
		public boolean isHeavy()
		{
			return isHeavy(false);
		}
	}
}