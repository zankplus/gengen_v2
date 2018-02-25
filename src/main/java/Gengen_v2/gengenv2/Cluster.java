package Gengen_v2.gengenv2;

import java.util.ArrayList;

public class Cluster
{
	// 3 = rare | 2 = common | 1 = rare | 0 = scandalous
	
	static int[][] onsetTransitions = new int[][]
	{	//	0	1	2	3	4	5	6	7	8	9	10	11	13	14	15
		{	0,	1,	1,	1,	1,	1,	1,	2,	2,	1,	0,	1,	1,	3,	3},	// 0. unvoiced unaspirated stops
		{	0,	0,	1,	1,	1,	1,	1,	1,	1,	1,	0,	1,	1,	3,	3},	// 1. unvoiced aspirated stops
		{	0,	0,	0,	1,	1,	1,	1,	1,	2,	2,	0,	1,	1,	3,	3},	// 2. voiced breathless stops
		{	0,	0,	0,	0,	1,	1,	1,	1,	1,	1,	0,	1,	1,	3,	3},	// 3. voiced breathy stops
		{	0,	0,	0,	0,	0,	1,	1,	1,	1,	1,	1,	1,	1,	2,	2},	// 4. affricates
		{	0,	0,	0,	0,	0,	0,	1,	1,	1,	1,	1,	1,	1,	2,	2},	// 5. glottal stop
		{	0,	0,	0,	0,	0,	0,	0,	1,	1,	1,	1,	1,	1,	1,	1},	// 6. unvoiced nonsibilant fricatives
		{	0,	0,	0,	0,	0,	0,	0,	0,	2,	1,	1,	2,	1,	3,	3},	// 7. unvoiced sibilant fricatives
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	1,	1,	1,	3,	3},	// 8. voiced nonsibilant fricatives
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	1,	1,	3,	3},	// 9. voiced sibilant fricatives
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	1,	2,	3},	// 10. glottal fricative
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	2,	2},	// 11. voiced nasals
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	2,	2},	// 12. unvoiced nasals
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	2},	// 13. liquids
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0}	// 14. glides
	};
	
	static int[][] nucleusTransitions = new int[][]
	{
		//	0	1	2	3	4
		{	0,	0,	0,	0,	0},	// 0. schwa
		{	0,	0,	2,	3,	3},	// 1. open vowels
		{	0,	0,	0,	3,	3},	// 2. mid vowels
		{	0,	0,	0,	0,	3},	// 3. close vowels
		{	0,	0,	0,	0,	0},	// 4. vowel lengthener
	};
	
	static int[][] codaTransitions = new int[][]
	{	//	0	1	2	3	4	5	6	7	8	9	10	11	12	13	14	
		{	0,	0,	0,	0,	0,	0,	0,	3,	0,	0,	0,	0,	0,	0,	0},	// 0. unvoiced unaspirated stops
		{	1,	0,	0,	0,	0,	0,	0,	3,	0,	0,	0,	0,	0,	0,	0},	// 1. unvoiced aspirated stops
		{	1,	1,	0,	0,	0,	0,	0,	0,	0,	3,	0,	0,	0,	0,	0},	// 2. voiced breathless stops
		{	1,	1,	1,	0,	0,	0,	0,	0,	0,	3,	0,	0,	0,	0,	0},	// 3. voiced breathy stops
		{	1,	1,	1,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},	// 4. affricates
		{	1,	1,	1,	1,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},	// 5. glottal stop
		{	2,	2,	1,	1,	1,	1,	0,	3,	0,	0,	0,	0,	0,	0,	0},	// 6. unvoiced nonsibilant fricatives
		{	3,	2,	1,	1,	1,	1,	1,	0,	0,	3,	0,	0,	0,	0,	0},	// 7. unvoiced sibilant fricatives
		{	1,	1,	1,	1,	1,	1,	1,	1,	0,	0,	0,	0,	0,	0,	0},	// 8. voiced nonsibilant fricatives
		{	1,	1,	2,	2,	1,	1,	1,	1,	1,	0,	0,	0,	0,	0,	0},	// 9. voiced sibilant fricatives
		{	2,	2,	2,	2,	2,	1,	2,	2,	2,	2,	0,	2,	1,	2,	0},	// 10. h
		{	3,	3,	3,	3,	3,	1,	1,	3,	1,	3,	0,	0,	0,	0,	0},	// 11. voiced nasals
		{	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	0,	0,	0},	// 12. unvoiced nasals
		{	3,	3,	3,	3,	3,	1,	3,	3,	3,	3,	2,	3,	3,	0,	0},	// 13. liquids
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0}	// 14. glides
	};
	
	static ArrayList<int[]> onsetCategories = new ArrayList<int[]>()
	{
		{
			add(new int[] {0, 1, 2, 3});		// 1. unvoiced unaspirated stops
			add(new int[] {4, 5, 6, 7});		// 2. unvoiced aspirated stops
			add(new int[] {8, 9, 10});			// 3. voiced breathless stops
			add(new int[] {11, 12, 13});		// 4. voiced breathy stops
			add(new int[] {14, 15});			// 5. affricates
			add(new int[] {16});				// 6. glottal stop
			add(new int[] {17});				// 7. unvoiced nonsibilant fricatives
			add(new int[] {18, 19});			// 8. unvoiced sibilant fricatives
			add(new int[] {20});				// 9. voiced nonsibilant fricatives
			add(new int[] {21, 22});			// 10. voiced sibilant fricatives
			add(new int[] {23});				// 11. glottal fricative
			add(new int[] {24, 25, 26});		// 12. voiced nasals
			add(new int[] {27, 28});			// 13. unvoiced nasals
			add(new int[] {29, 30});			// 14. liquids
			add(new int[] {31, 32});			// 15. glides
		}
	};
	
	static ArrayList<int[]> nucleusCategories = new ArrayList<int[]>()
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