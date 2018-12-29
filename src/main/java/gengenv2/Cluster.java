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

/**
 * An auxiliary class containing static data for use by Phonologies. Two kinds of data are present:
 * 1.	transition tables, which govern the likelihood that any sound may follow any other in consonant cluster
 * 		or diphthong.
 * 2.	category lists, which enumerate the ids of all consonants or vowels belonging to a transition category
 *  
 * @author	Clayton Cooper
 * 
 *
 */
final class Cluster
{
	// 3 = rare | 2 = common | 1 = rare | 0 = scandalous
	
	/**
	 * Prevalence of transitions between consonants in onset clusters
	 */
	static int[][] onsetTransitions = new int[][]
	{	//	0	1	2	3	4	5	6	7	8	9	10	11	13	14	15
		{	0,	1,	1,	1,	1,	1,	1,	2,	1,	1,	0,	1,	1,	3,	3	},	// 0. unvoiced unaspirated stops
		{	0,	0,	1,	1,	1,	1,	1,	1,	1,	1,	0,	1,	1,	3,	3	},	// 1. unvoiced aspirated stops
		{	0,	0,	0,	1,	1,	1,	1,	1,	2,	2,	0,	1,	1,	3,	3	},	// 2. voiced breathless stops
		{	0,	0,	0,	0,	1,	1,	1,	1,	1,	1,	0,	1,	1,	3,	3	},	// 3. voiced breathy stops
		{	0,	0,	0,	0,	0,	1,	1,	1,	1,	1,	1,	1,	1,	2,	2	},	// 4. affricates
		{	0,	0,	0,	0,	0,	0,	1,	1,	1,	1,	1,	1,	1,	2,	2	},	// 5. glottal stop
		{	0,	0,	0,	0,	0,	0,	0,	1,	1,	1,	1,	1,	1,	3,	3	},	// 6. unvoiced nonsibilant fricatives
		{	3,	2,	0,	0,	0,	0,	2,	0,	2,	1,	1,	2,	1,	3,	3	},	// 7. unvoiced sibilant fricatives
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	1,	1,	1,	2,	2	},	// 8. voiced nonsibilant fricatives
		{	0,	0,	2,	1,	0,	0,	0,	0,	1,	0,	1,	1,	1,	2,	3	},	// 9. voiced sibilant fricatives
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	1,	2,	2	},	// 10. glottal fricative
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	2,	2	},	// 11. voiced nasals
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	2,	2	},	// 12. unvoiced nasals
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	2	},	// 13. liquids
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0	}	// 14. glides
	};
	
	/**
	 * Prevalence of transitions between vowels in diphthongs (nucleus clusters)
	 */
	static int[][] nucleusTransitions = new int[][]
	{
		//	0	1	2	3	4
		{	0,	0,	0,	0,	0	},	// 0. schwa
		{	0,	0,	2,	3,	3	},	// 1. open vowels
		{	0,	0,	0,	3,	3	},	// 2. mid vowels
		{	0,	0,	0,	0,	3	},	// 3. close vowels
		{	0,	0,	0,	0,	0	},	// 4. vowel lengthener
	};
	
	/**
	 * Prevalence of transitions between consonants in coda clusters
	 */
	static int[][] codaTransitions = new int[][]
	{	//	0	1	2	3	4	5	6	7	8	9	10	11	12	13	14	
		{	0,	0,	0,	0,	0,	0,	0,	3,	0,	0,	0,	0,	0,	0,	0	},	// 0. unvoiced unaspirated stops
		{	1,	0,	0,	0,	0,	0,	0,	3,	0,	0,	0,	0,	0,	0,	0	},	// 1. unvoiced aspirated stops
		{	1,	1,	0,	0,	0,	0,	0,	0,	0,	3,	0,	0,	0,	0,	0	},	// 2. voiced breathless stops
		{	1,	1,	1,	0,	0,	0,	0,	0,	0,	3,	0,	0,	0,	0,	0	},	// 3. voiced breathy stops
		{	1,	1,	1,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0	},	// 4. affricates
		{	1,	1,	1,	1,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0	},	// 5. glottal stop
		{	2,	2,	1,	1,	1,	1,	0,	3,	0,	0,	0,	0,	0,	0,	0	},	// 6. unvoiced nonsibilant fricatives
		{	3,	2,	1,	1,	1,	1,	1,	0,	0,	0,	0,	0,	0,	0,	0	},	// 7. unvoiced sibilant fricatives
		{	1,	1,	1,	1,	1,	1,	1,	1,	0,	2,	0,	0,	0,	0,	0	},	// 8. voiced nonsibilant fricatives
		{	1,	1,	2,	2,	1,	1,	1,	1,	1,	0,	0,	0,	0,	0,	0	},	// 9. voiced sibilant fricatives
		{	2,	2,	2,	2,	2,	1,	2,	2,	2,	2,	0,	2,	1,	2,	0	},	// 10. h
		{	3,	3,	3,	3,	3,	1,	1,	3,	1,	3,	0,	0,	0,	0,	0	},	// 11. voiced nasals
		{	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	0,	0,	0	},	// 12. unvoiced nasals
		{	3,	3,	3,	3,	3,	1,	3,	3,	3,	3,	2,	3,	3,	0,	0	},	// 13. liquids
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0	}	// 14. glides
	};

	/**
	 * Prevalence of transitions between consonants across syllables, from codas to onsets
	 */
	static int[][] interludeTransitions = new int[][]
	{
		//	0	1	2	3	4	5	6	7	8	9	10	11	12	13	14	
		{	3,	2,	2,	2,	3,	2,	3,	3,	2,	2,	2,	3,	1,	3,	3	},	// 0. unvoiced unaspirated stops
		{	2,	2,	2,	1,	2,	2,	3,	3,	2,	2,	1,	3,	1,	3,	3	},	// 1. unvoiced aspirated stops
		{	2,	2,	3,	3,	2,	2,	2,	2,	3,	3,	3,	3,	1,	3,	3	},	// 2. voiced breathless stops
		{	2,	1,	2,	2,	2,	2,	2,	2,	3,	3,	1,	3,	1,	3,	3	},	// 3. voiced breathy stops
		{	2,	2,	2,	2,	1,	2,	2,	2,	2,	2,	2,	3,	1,	3,	3	},	// 4. affricates
		{	3,	3,	3,	3,	3,	0,	3,	3,	3,	3,	2,	3,	1,	3,	3	},	// 5. glottal stop
		{	3,	3,	2,	2,	2,	2,	0,	3,	2,	2,	2,	3,	1,	3,	3	},	// 6. unvoiced nonsibilant fricatives
		{	3,	3,	2,	2,	2,	2,	3,	1,	2,	1,	2,	3,	1,	3,	3	},	// 7. unvoiced sibilant fricatives
		{	2,	2,	3,	3,	2,	2,	2,	2,	0,	3,	2,	3,	1,	3,	3	},	// 8. voiced nonsibilant fricatives
		{	2,	2,	3,	3,	2,	2,	2,	2,	0,	3,	2,	3,	1,	3,	3	},	// 9. voiced sibilant fricatives
		{	3,	3,	3,	3,	3,	2,	3,	3,	3,	3,	0,	3,	1,	3,	3	},	// 10. h
		{	3,	3,	3,	3,	3,	2,	3,	3,	3,	3,	3,	3,	1,	3,	3	},	// 11. voiced nasals
		{	2,	2,	2,	2,	2,	2,	2,	2,	2,	2,	2,	2,	1,	2,	2	},	// 12. unvoiced nasals
		{	3,	3,	3,	3,	3,	2,	3,	3,	3,	3,	3,	3,	1,	3,	3	},	// 13. liquids
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0	}	// 14. glides
	};
	
	/**
	 * Prevalence of transitions between vowels across syllables, from one nucleus to another
	 */
	static int[][] hiatusTransitions = new int[][]
	{
		//	ə	a	e	o	i	u	y
		{	0,	0,	0,	0,	0,	0,	0},	// 0. ə
		{	0,	1,	1,	1,	1,	1,	1},	// 1. a
		{	0,	2,	1,	2,	1,	1,	1},	// 2. e
		{	0,	2,	2,	1,	1,	1,	1},	// 3. o
		{	0,	3,	2,	3,	1,	2,	1},	// 4. i
		{	0,	3,	3,	2,	2,	1,	1},	// 5. u
		{	0,	3,	2,	3,	1,	1,	0},	// 6. y 
	};
	
	/**
	 * Phonotactic categories for consonant sounds.
	 * Every consonant is assigned a 'phonotactic category'; consonants sharing a category are assumed to
	 * exhibit similar phonotactic behavior across all languages. These categories roughly correspond to
	 * manners of articulation, with a number of exceptions, such as obstruents being sorted by voicing
	 * to accommodate the fact that they prefer to agree in voicing with whatever they are clustered with.
	 * Generally, a Phonology will admit any member of the same category in the same position. 
	 */
	static ArrayList<int[]> consonantCategories = new ArrayList<int[]>()
	{
		{
			add(new int[] {0, 1, 2, 3});		// 0. unvoiced unaspirated stops
			add(new int[] {4, 5, 6, 7});		// 1. unvoiced aspirated stops
			add(new int[] {8, 9, 10});			// 2. voiced breathless stops
			add(new int[] {11, 12, 13});		// 3. voiced breathy stops
			add(new int[] {14, 15});			// 4. affricates
			add(new int[] {16});				// 5. glottal stop
			add(new int[] {17});				// 6. unvoiced nonsibilant fricatives
			add(new int[] {18, 19});			// 7. unvoiced sibilant fricatives
			add(new int[] {20});				// 8. voiced nonsibilant fricatives
			add(new int[] {21, 22});			// 9. voiced sibilant fricatives
			add(new int[] {23});				// 10. glottal fricative
			add(new int[] {24, 25, 26});		// 11. voiced nasals
			add(new int[] {27, 28});			// 12. unvoiced nasals
			add(new int[] {29, 30});			// 13. liquids
			add(new int[] {31, 32});			// 14. glides
		}
	};
	
	/**
	 * Phonotactic categories for vowel sounds
	 */
	static ArrayList<int[]> vowelCategories = new ArrayList<int[]>()
	{
		{
			add(new int[] {0});				// 0. schwa
			add(new int[] {1});				// 1. open vowels
			add(new int[] {2, 3});			// 2. mid vowels
			add(new int[] {4, 5, 6});		// 3. close vowels
			add(new int[] {7});				// 4. vowel length
		}
	};
}