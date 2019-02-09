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

import java.util.Random;

import gengenv2.Name.Syllable;

public class StressRules
{
	enum StressHead		{ LEFT, RIGHT};
	enum StressRhythm	{ IAMBIC, TROCHAIC };
	enum PrimaryStress	{ FIRST, LAST };
	
	public StressHead stressHead;				// The side of the word from which versification begins
	public StressRhythm stressRhythm;			// Whether feet follow a weak-strong or strong-weak pattern
	public PrimaryStress primaryStress;			// Which of the stresses in a word is most pronounced
	public boolean quantitySensitive;			// Whether heavy syllables attract stress
	public boolean allowClashes;				// Whether two adjacent syllables can receive stress
	public boolean externalExtrametricality;	// If enabled, the last syllable of the word is ignored when assigning stress
	public boolean internalExtrametricality;	// If enabled, allows non to contain an extra unstressed syllable
	public boolean consonantExtrametricality;	// If enabled, the last consonant of the word is ignored when determining syllable weight
	public boolean avoidFinalPrimaryStress;		// If enabled, primary stress will not fall on the final syllable unless it is the only stress
	
	public StressRules ()
	{
		this(new Random().nextLong());
	}
	
	public StressRules (long seed)
	{
		System.out.println("Stress Rules Seed: " + seed);
		Random rng = new Random(seed);
		
		// Set rules at random
		stressHead = (rng.nextInt(2) == 0) ? StressHead.LEFT : StressHead.RIGHT;
		stressRhythm = (rng.nextInt(2) == 0) ? StressRhythm.IAMBIC : StressRhythm.TROCHAIC;
//		primaryStress = (rng.nextInt(2) == 0) ? PrimaryStress.FIRST : PrimaryStress.LAST;
		primaryStress = stressHead == StressHead.LEFT ? PrimaryStress.FIRST : PrimaryStress.LAST;
		
		// Iambic languages are usually quantity-sensitive
		if (stressRhythm == StressRhythm.IAMBIC)
			quantitySensitive = (rng.nextInt(4) < 3);
		else
			quantitySensitive = (rng.nextInt(2) == 0);
		
		allowClashes = (rng.nextInt(2) == 0);
		externalExtrametricality = (rng.nextInt(3) == 0);
		internalExtrametricality = (rng.nextInt(3) == 0);
		
		if ((quantitySensitive || internalExtrametricality) && !externalExtrametricality)
			consonantExtrametricality = (rng.nextInt(4) == 0);
		
		if (!externalExtrametricality && primaryStress == PrimaryStress.LAST)
			avoidFinalPrimaryStress = (rng.nextInt(3) == 0);
	}
	
	/**
	 * Assigns stress to every syllable of the given name according to rules set.
	 * @param	name	The name to be versified
	 */
	public void addStresses(Name name)
	{
		// Only assign stress in polysyllabic names
		if (name.syllables.size() == 1)
			return;
		
		Stress leftStress, rightStress;
		int footPosition = 0;
		
		if (stressRhythm == StressRhythm.TROCHAIC)
		{
			leftStress = Stress.STRONG;
			rightStress = Stress.WEAK;
		}
		else
		{
			leftStress = Stress.WEAK;
			rightStress = Stress.STRONG;
		}
		
		// Word-extrametricality causes us to ignore the last syllable in versification
		int lastMetricalSyllable = name.syllables.size() - 1;
		if (externalExtrametricality)
			lastMetricalSyllable--;
		
		// Set strong syllables
		// Start with the rules for left-headed (left-to-right) scanning
		if (stressHead == StressHead.LEFT)
			for (int i = 0; i <= lastMetricalSyllable; i++)
			{	
				Syllable syl = name.syllables.get(i);
				
				// 1. Quantity sensitivity attracts stress to heavy syllables
				if (quantitySensitive && syl.isHeavy(consonantExtrametricality))
				{
					// 1a. Quantity sensitivity doesn't apply to a syllable when it would cause a forbidden clash
					if (i > 0 && name.syllables.get(i - 1).stress == Stress.STRONG && !allowClashes)
					{
						syl.stress = Stress.WEAK;
						footPosition = leftStress == Stress.WEAK ? 1 : 0;
					}
					else
					{
						syl.stress = Stress.STRONG;
						footPosition = leftStress == Stress.STRONG ? 1 : 0;
					}
				}

				// 2. Foot extrametricality
				// If extrametrical feet are allowed, a light syllable at the right of a foot will be appended to
				// that foot if the next syllable is heavy (in a trochaic system) or light (in an iambic one).
				// This is a sort of quantity-sensitivity that helps attract the next foot's stress to heavy syllables,
				// although this can come at the expense of still more distant feet. Still, in that short range,
				// it helps improve rhythm.
				else if (internalExtrametricality && i > 0 && !syl.isHeavy(consonantExtrametricality) && name.syllables.get(i - 1).stress == rightStress &&
						 (i < lastMetricalSyllable && name.syllables.get(i + 1).isHeavy(consonantExtrametricality) == (leftStress == Stress.STRONG)))
				{
					// Make this syllable strong if it would result in 3 consecutive weak syllables
					if (externalExtrametricality && i == lastMetricalSyllable && stressRhythm == StressRhythm.TROCHAIC)
						syl.stress = Stress.STRONG;
					else
						syl.stress = Stress.WEAK;
				}
				
				// 3. Initial lapse prevention
				// If the current syllable is the initial syllable, and the following is weak, the current syllable 
				// must be strong to prevent a pair of initial weak syllables (lapse). In a left-headed language, 
				// this only occurs in a two-syllable iambic word with an extrametrical final syllable.
				else if (i == 0 && externalExtrametricality && name.syllables.size() == 2)
				{
					syl.stress = Stress.STRONG;
				}
				
				// 3a. Application of default stress according to position in foot (beginning of foot)
				else if (footPosition == 0)
				{
					syl.stress = leftStress;
					footPosition = 1;
				}
				// 3b. Application of default stress according to position in foot (end of foot)
				else
				{
					syl.stress = rightStress;
					footPosition = 0;
				}
			}
		
		// Proceed with the rules for right-headed (right-headed) scanning
		else
			for (int i = lastMetricalSyllable; i >= 0; i--)
			{	
				Syllable syl = name.syllables.get(i);
				
				// 1. Quantity sensitivity attracts stress to heavy syllables
				if (quantitySensitive && syl.isHeavy(consonantExtrametricality))
				{
					// 1a. Quantity sensitivity doesn't apply to a syllable when it would cause a forbidden clash
					if (i < lastMetricalSyllable && name.syllables.get(i + 1).stress == Stress.STRONG && !allowClashes)
					{
						syl.stress = Stress.WEAK;
						footPosition = rightStress == Stress.WEAK ? 1 : 0;
					}
					else
					{
						syl.stress = Stress.STRONG;
						footPosition = rightStress == Stress.STRONG ? 1 : 0;
					}
				}

				// 2. Foot extrametricality
				// The rules for a extrametricality are different on a right-edged word. For starters, in order to ensure
				// a variety of language that includes both 
				else if (internalExtrametricality && i < lastMetricalSyllable && !syl.isHeavy(consonantExtrametricality) &&
						 name.syllables.get(i + 1).stress == leftStress &&
						 (i > 0 && name.syllables.get(i - 1).isHeavy(consonantExtrametricality) == (rightStress == Stress.STRONG)))
				{
					// Make this syllable strong if it would result in 3 consecutive weak syllables
					if (externalExtrametricality && i == lastMetricalSyllable && stressRhythm == StressRhythm.TROCHAIC)
						syl.stress = Stress.STRONG;
					else
						syl.stress = Stress.WEAK;
				}
				
				// 3. Initial lapse prevention
				// If the current syllable is the initial syllable, and the following is weak, the current syllable must be strong
				// to prevent a pair of initial weak syllables (lapse)
				else if (i == 0 && name.syllables.get(i + 1).stress == Stress.WEAK)
				{
					syl.stress = Stress.STRONG;
				}
				
				// 4a. Application of default stress according to position in foot (beginning of foot)
				else if (footPosition == 0)
				{
					syl.stress = rightStress;
					footPosition = 1;
				}
				
				// 4b. Application of default stress according to position in foot (end of foot)
				else
				{
					syl.stress = leftStress;
					footPosition = 0;
				}
			}
	
		// Assign primary stress
		// Primary stress on first strong syllable
		if (primaryStress == PrimaryStress.FIRST)
			for (int i = 0; i < name.syllables.size(); i++)
			{
				if (name.syllables.get(i).stress == Stress.STRONG)
				{
					name.syllables.get(i).stress = Stress.PRIMARY;
					return;
				}
			}
		// Primary stress on last strong syllable
		else
		{
			for (int i = name.syllables.size() - 1; i >= 0; i--)
			{
				if (name.syllables.get(i).stress == Stress.STRONG &&
						(!avoidFinalPrimaryStress || i != name.syllables.size() - 1))
				{
					name.syllables.get(i).stress = Stress.PRIMARY;
					return;
				}
			}
			name.syllables.get(name.syllables.size() - 1).stress = Stress.PRIMARY;
		}

	}
	
	public String toString()
	{
		String result = "quantity-";
		result += quantitySensitive ? "sensitive " : "insensitive ";
		result += stressHead.name().toLowerCase() + "-headed ";
		result += stressRhythm.name().toLowerCase() + ", ";
		result += primaryStress.name().toLowerCase();
		if (avoidFinalPrimaryStress)
			result += " non-final";
		result += " stress primary, clashes ";
		result += allowClashes ? "allowed. " : "forbidden. ";
		
		int ex = (externalExtrametricality ? 1 : 0) + (internalExtrametricality ? 1 : 0) + (consonantExtrametricality ? 1 : 0);
		if (ex > 0)
		{
			result += "extrametrical";
			if (externalExtrametricality)
			{
				result += " words";
				if (ex > 1)
					result += ",";
				ex--;
			}
			if (internalExtrametricality)
			{
				result += " feet";
				if (ex > 1)
					result += ",";
				ex--;
			}
			if (consonantExtrametricality)
				result += " consonants";
		}
		
		return result;
	}
}
