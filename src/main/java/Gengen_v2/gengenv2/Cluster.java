package Gengen_v2.gengenv2;

import java.util.ArrayList;

public class Cluster
{
	static int[][] transitionProbability = new int[][]
	{	//	1	2	3	4	5	6	7	8	9	10	11	12	13	14	15	16
		{	0,	1,	1,	1,	1,	1,	1,	2,	2,	1,	0,	1,	1,	1,	3,	3},	// 1. unvoiced unaspirated stops
		{	0,	0,	1,	1,	1,	1,	1,	1,	1,	1,	0,	1,	1,	1,	3,	3},	// 2. unvoiced aspirated stops
		{	0,	0,	0,	1,	1,	1,	1,	1,	2,	2,	0,	1,	1,	1,	3,	3},	// 3. voiced breathless stops
		{	0,	0,	0,	0,	1,	1,	1,	1,	1,	1,	0,	1,	1,	1,	3,	3},	// 4. voiced breathy stops
		{	0,	0,	0,	0,	0,	1,	1,	1,	1,	1,	1,	1,	1,	1,	2,	2},	// 5. affricates
		{	0,	0,	0,	0,	0,	0,	1,	1,	1,	1,	1,	1,	1,	1,	2,	2},	// 6. glottal stop
		{	0,	0,	0,	0,	0,	0,	0,	1,	1,	1,	1,	1,	1,	1,	1,	1},	// 7. unvoiced nonsibilant fricatives
		{	0,	0,	0,	0,	0,	0,	0,	0,	2,	1,	1,	2,	1,	1,	3,	3},	// 8. unvoiced sibilant fricatives
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	1,	1,	1,	1,	3,	3},	// 9. voiced nonsibilant fricatives
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	1,	1,	1,	3,	3},	// 10. voiced sibilant fricatives
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	1,	1,	2,	3},	// 11. glottal fricative
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	1,	2,	2},	// 12. voiced bilabial/alveolar nasals
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	2,	2},	// 13. velar nasal
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	2,	2},	// 14. unvoiced nasals
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	2},	// 15. liquids
		{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0}	// 16. glides
	};
	
	static int[][] codaTransitionProbability = new int[][]
		{	//	0	1	2	3	4	5	6	7	8	9	10	11	12	13	14	15	16	17	18	19	20	21	22	23	24	25	26
			{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 0. start	
			{	0,	0,	0,	0,	0,	0,	0,	0,	3,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 1. unvoiced unaspirated stops
			{	0,	0,	0,	0,	0,	0,	0,	0,	3,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 2. unvoiced aspirated stops
			{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 3. voiced breathless stops
			{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	2,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 4. voiced breathy stops
			{	0,	2,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 5. affricates
			{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 6. glottal stop
			{	0,	2,	2,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 7. unvoiced nonsibilant fricatives
			{	0,	3,	3,	1,	1,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 8. unvoiced sibilant fricatives
			{	0,	0,	0,	2,	2,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 9. voiced nonsibilant fricatives
			{	0,	0,	0,	2,	2,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 10. voiced sibilant fricatives
			{	0,	3,	3,	3,	3,	3,	0,	3,	3,	3,	3,	0,	3,	3,	3,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 11. glottal fricative
			{	0,	3,	3,	3,	3,	3,	0,	2,	3,	2,	3,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 12. voiced bilabial/alveolar nasals
			{	0,	3,	3,	3,	3,	1,	0,	2,	3,	2,	3,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 13. velar nasal
			{	0,	0,	0,	0,	0,	0,	0,	0,	2,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 14. unvoiced nasals
			{	0,	3,	3,	3,	3,	3,	0,	3,	3,	3,	3,	0,	3,	3,	2,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 15. liquids
			{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 16. glides
			{	0,	3,	3,	3,	3,	3,	0,	3,	3,	3,	3,	3,	3,	3,	3,	3,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3}, // 17. short open vowels
			{	0,	3,	3,	3,	3,	3,	0,	3,	3,	3,	3,	3,	3,	3,	3,	3,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3}, // 18. long open vowels
			{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3}, // 19. overshort open vowels
			{	0,	3,	3,	3,	3,	3,	0,	3,	3,	3,	3,	3,	3,	3,	3,	3,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 20. short mid vowels
			{	0,	3,	3,	3,	3,	3,	0,	3,	3,	3,	3,	3,	3,	3,	3,	3,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 21. long mid vowels
			{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 22. overshort mid vowels
			{	0,	3,	3,	3,	3,	3,	0,	3,	3,	3,	3,	3,	3,	3,	3,	3,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 23. short close vowels
			{	0,	3,	3,	3,	3,	3,	0,	3,	3,	3,	3,	3,	3,	3,	3,	3,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 24. long close vowels
			{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3},	// 25. overshort close vowels
			{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	3}	// 26. end
		};
	
	static ArrayList<int[]> ptCats = new ArrayList<int[]>()
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
			add(new int[] {24, 25});			// 12. voiced bilabial/alveolar nasals
			add(new int[] {26});				// 13. velar nasal
			add(new int[] {27, 28});			// 14. unvoiced nasals
			add(new int[] {29, 30});			// 15. liquids
			add(new int[] {31, 32});			// 16. glides
		}
	};
	
	static ArrayList<int[]> vowelCats = new ArrayList<int[]>()
	{
		{
			add(new int[] {0});				// 17. open vowels
			add(new int[] {1, 2});			// 18. mid vowels
			add(new int[] {3, 4, 5});		// 19. close vowels
			add(new int[] {6});		// 19. vowel length
		}
	};
}

enum PTCat { START,	UNVOICED_UNASPIRATED_STOP, UNVOICED_ASPIRATED_STOP, VOICED_BREATHLESS_STOP,
	 VOICED_BREATHY_STOP, AFFRICATE, GLOTTAL_STOP, UNVOICED_NONSIBILANT_FRICATIVE,
	 UNVOICED_SIBILANT_FRICATIVE, VOICED_NONSIBILANT_FRICATIVE, VOICED_SIBILANT_FRICATIVE,
	 GLOTTAL_FRICATIVE, VOICED_NASAL, VELAR_NASAL, UNVOICED_NASAL, LIQUID, GLIDE, OPEN_VOWEL,
	 MID_VOWEL, SHORT_CLOSE_VOWEL, END };