package gengenv2.morphemes;

import gengenv2.enums.ConsonantProperty;

/**
 * An extension of the Segment class for representing consonantal segments.
 * 
 * @author	Clayton Cooper
 * @version	1.0
 * @since	1.0
 */
public class ConsonantSegment extends Segment
{
	static int count = 0;
	public final double defectiveChance;
	public final ConsonantProperty[] properties;
	private final int id;
	
	/**
	 * Sets Segment fields by calling the superclass's constructor - but also sets this Consonant's id by
	 * checking the static count, and increments it.
	 * @param expression			Character(s) representing this sound's orthographic representation
	 * @param ipa					The IPA symbol representing this sound
	 * @param transitionCategory	Character's phonotactic category for the purposes of deciding consonant clusters
	 * @param defectiveChance		The chance of this segment being defective, i.e., absent form a language, despite the presence of all of its properties
	 * @param properties			List of character's phonetic properties
	 */
	public ConsonantSegment(String expression, String ipa, int transitionCategory, double defectiveChance, 
			ConsonantProperty[] properties)
	{
		super(expression, ipa, transitionCategory, properties);
		this.defectiveChance = defectiveChance;
		this.properties = properties; 
		id = count;
		count++;
	}
	
	public int getID()
	{
		return id;
	}
	
	/**
	 * Returns true for all instances of this class.
	 */
	public boolean isConsonant()
	{
		return true;
	}
	
	// Defines cross-linguistic consonantal inventory
	public static final ConsonantSegment[] segments = new ConsonantSegment[]
	{
/* 0*/	new ConsonantSegment("p" ,  "p",	0,	0.129,	new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.BILABIAL, 	ConsonantProperty.VOICELESS}),
/* 1*/	new ConsonantSegment("t" ,  "t",	0,	0.043,	new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.ALVEOLAR, 	ConsonantProperty.VOICELESS}),
/* 2*/	new ConsonantSegment("k" ,  "k",	0,	0.052,	new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.VELAR, 		ConsonantProperty.VOICELESS}),
/* 3*/	new ConsonantSegment("q" ,  "q",	0,	0, 		new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.UVULAR, 		ConsonantProperty.VOICELESS}),
/* 4*/	new ConsonantSegment("b" ,  "b",	2,	0.165,	new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.BILABIAL, 	ConsonantProperty.VOICED}),
/* 5*/	new ConsonantSegment("d" ,  "d",	2,	0.195,	new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.ALVEOLAR, 	ConsonantProperty.VOICED}),
/* 6*/	new ConsonantSegment("g" ,  "g",	2,	0.259,	new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.VELAR, 		ConsonantProperty.VOICED}),
/* 7*/	new ConsonantSegment("'" ,  "ʔ", 	5,	0.357,	new ConsonantProperty[] {ConsonantProperty.PLOSIVE, 	ConsonantProperty.GLOTTAL}),

/* 8*/	new ConsonantSegment("ts" ,  "ts",	4, 0.452,	new ConsonantProperty[] {ConsonantProperty.AFFRICATE,	ConsonantProperty.ALVEOLAR, 	ConsonantProperty.VOICED,		ConsonantProperty.SIBILANT}),
/* 9*/	new ConsonantSegment("ch",  "tʃ",	4, 0,		new ConsonantProperty[] {ConsonantProperty.AFFRICATE,	ConsonantProperty.POSTALVEOLAR,	ConsonantProperty.VOICELESS,	ConsonantProperty.SIBILANT}),
/*10*/	new ConsonantSegment("j" ,  "dʒ",	4, 0.025,	new ConsonantProperty[] {ConsonantProperty.AFFRICATE,	ConsonantProperty.POSTALVEOLAR, ConsonantProperty.VOICED,		ConsonantProperty.SIBILANT}),

/*11*/	new ConsonantSegment("ph",  "ɸ",	6, 0.863,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.BILABIAL,		ConsonantProperty.VOICELESS,	ConsonantProperty.NONSIBILANT}),
/*12*/	new ConsonantSegment("th",  "θ",	6, 0.937,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.ALVEOLAR,		ConsonantProperty.VOICELESS,	ConsonantProperty.NONSIBILANT}),
/*13*/	new ConsonantSegment("kh",  "x",	6, 0.568,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.VELAR,		ConsonantProperty.VOICELESS,	ConsonantProperty.NONSIBILANT}),
/*14*/	new ConsonantSegment("qh",  "ʁ",	6, 0,		new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.UVULAR,		ConsonantProperty.VOICELESS,	ConsonantProperty.NONSIBILANT}),
/*15*/	new ConsonantSegment("f" ,  "f",	6, 0.093,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.LABIODENTAL,	ConsonantProperty.VOICELESS,	ConsonantProperty.NONSIBILANT}),
/*16*/	new ConsonantSegment("s" ,  "s",	7, 0.016,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.ALVEOLAR,		ConsonantProperty.VOICELESS,	ConsonantProperty.SIBILANT}),
/*17*/	new ConsonantSegment("sh",  "ʃ",	7, 0.156,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.POSTALVEOLAR,	ConsonantProperty.VOICELESS,	ConsonantProperty.SIBILANT}),

/*18*/	new ConsonantSegment("bh",  "β",	8, 0.754,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.BILABIAL, 	ConsonantProperty.VOICED,		ConsonantProperty.NONSIBILANT}),
/*19*/	new ConsonantSegment("dh",  "ð",	8, 0.900,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.ALVEOLAR, 	ConsonantProperty.VOICED,		ConsonantProperty.NONSIBILANT}),
/*20*/	new ConsonantSegment("gh",  "ɣ",	8, 0.748,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.VELAR, 		ConsonantProperty.VOICED,		ConsonantProperty.NONSIBILANT}),
/*21*/	new ConsonantSegment("v" ,  "v",	8, 0.211,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.LABIODENTAL,	ConsonantProperty.VOICED,		ConsonantProperty.NONSIBILANT}),
/*22*/	new ConsonantSegment("z" ,  "z",	9, 0.580,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.ALVEOLAR,		ConsonantProperty.VOICED,		ConsonantProperty.SIBILANT}),
/*23*/	new ConsonantSegment("zh",  "ʒ",	9, 0.646,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.POSTALVEOLAR,	ConsonantProperty.VOICED,		ConsonantProperty.SIBILANT}),

/*24*/	new ConsonantSegment("h" ,  "h",	10, 0.058,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.GLOTTAL}),
		
/*25*/	new ConsonantSegment("m" ,  "m",	11, 0.015,	new ConsonantProperty[] {ConsonantProperty.NASAL,		ConsonantProperty.BILABIAL}),
/*26*/	new ConsonantSegment("n" ,  "n",	11, 0.004,	new ConsonantProperty[] {ConsonantProperty.NASAL,		ConsonantProperty.ALVEOLAR}),
/*27*/	new ConsonantSegment("ng",  "ŋ",	11, 0.447,	new ConsonantProperty[] {ConsonantProperty.NASAL,		ConsonantProperty.VELAR}),
/*28*/	new ConsonantSegment("mh",  "m̥",	12, 0,		new ConsonantProperty[] {ConsonantProperty.VOICELESS_NASAL,	ConsonantProperty.BILABIAL}),
/*29*/	new ConsonantSegment("nh",  "n̥",	12, 0.112,	new ConsonantProperty[] {ConsonantProperty.VOICELESS_NASAL,	ConsonantProperty.ALVEOLAR}),

/*30*/	new ConsonantSegment("r" ,  "r",	13, 0,		new ConsonantProperty[] {ConsonantProperty.LIQUID,	 ConsonantProperty.APPROXIMANT,		ConsonantProperty.ALVEOLAR_TRILL}),
/*31*/	new ConsonantSegment("l" ,  "l",	13, 0,		new ConsonantProperty[] {ConsonantProperty.LIQUID,	 ConsonantProperty.APPROXIMANT,		ConsonantProperty.LATERAL_APPROX}),
/*32*/	new ConsonantSegment("y" ,  "j",	14, 0,		new ConsonantProperty[] {ConsonantProperty.GLIDE,	 ConsonantProperty.APPROXIMANT,		ConsonantProperty.PALATAL_APPROX}),
/*33*/	new ConsonantSegment("w" ,  "w",	14, 0,		new ConsonantProperty[] {ConsonantProperty.GLIDE,	 ConsonantProperty.APPROXIMANT,		ConsonantProperty.LABIOVELAR_APPROX}),
	};
}