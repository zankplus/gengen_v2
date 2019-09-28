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
 * Abstract class representing a phonetic segment, or a discrete and distinct unit of sound. These are extended 
 * into Consonant and Vowel classes with their own sets of properties, and form the basis of Phonemes in the
 * Phonology class.
 *  
 * @author	Clayton Cooper
 * @version	1.0
 * @since	1.0
 */
abstract class Segment extends Object
{
	String expression;
	String ipa;
	SegmentProperty[] properties;
	int transitionCategory;
	int id;
	
	/**
	 * A basic constructor that sets the most fundamental variables of the Segment. The id field is not set here
	 * but in the constructors of Segment's child classes, as the id of each Consonant is unique among Consonants,
	 * and just so for Vowels, and so this value depends on the static counts only within those classes.
	 *    
	 * @param expression			The character or characters used to denote this sound orthographically
	 * @param ipa					The IPA character representing this segment
	 * @param transitionCategory	The category to which this sound belongs for the purposes of cluster construction
	 * @param properties			The list of consonant or vowel properties that define this sound
	 */
	public Segment(String expression, String ipa, int transitionCategory, SegmentProperty[] properties)
	{
		this.expression = expression;
		this.ipa = ipa;
		this.transitionCategory = transitionCategory;
		this.properties = properties;
	}
	
	/**
	 * Returns true if this Segment is a consonant. Otherwise it is a vowel and returns falls. 
	 * @return	true if this segment is a consnant, false if it is a vowel
	 */
	abstract public boolean isConsonant();
}

/**
 * An extension of the Segment class for representing consonantal segments.
 * 
 * @author	Clayton Cooper
 * @version	1.0
 * @since	1.0
 */
class Consonant extends Segment
{
	static int count = 0;
	double defectiveChance;
	ConsonantProperty[] properties;
	
	/**
	 * Sets Segment fields by calling the superclass's constructor - but also sets this Consonant's id by
	 * checking the static count, and increments it.
	 * @param expression			Character(s) representing this sound's orthographic representation
	 * @param ipa					The IPA symbol representing this sound
	 * @param transitionCategory	Character's phonotactic category for the purposes of deciding consonant clusters
	 * @param defectiveChance		The chance of this segment being defective, i.e., absent form a language, despite the presence of all of its properties
	 * @param properties			List of character's phonetic properties
	 */
	public Consonant(String expression, String ipa, int transitionCategory, double defectiveChance, 
			ConsonantProperty[] properties)
	{
		super(expression, ipa, transitionCategory, properties);
		this.defectiveChance = defectiveChance;
		this.properties = properties; 
		id = count;
		count++;
	}
	
	/**
	 * Returns true for all instances of this class.
	 */
	public boolean isConsonant()
	{
		return true;
	}
	
	// Defines cross-linguistic consonantal inventory
	static Consonant[] segments = new Consonant[]
	{
/* 0*/	new Consonant("p" ,  "p",	0,	0.129,	new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.BILABIAL, 	ConsonantProperty.VOICELESS}),
/* 1*/	new Consonant("t" ,  "t",	0,	0.043,	new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.ALVEOLAR, 	ConsonantProperty.VOICELESS}),
/* 2*/	new Consonant("k" ,  "k",	0,	0.052,	new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.VELAR, 		ConsonantProperty.VOICELESS}),
/* 3*/	new Consonant("q" ,  "q",	0,	0, 		new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.UVULAR, 		ConsonantProperty.VOICELESS}),
/* 4*/	new Consonant("b" ,  "b",	2,	0.165,	new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.BILABIAL, 	ConsonantProperty.VOICED}),
/* 5*/	new Consonant("d" ,  "d",	2,	0.195,	new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.ALVEOLAR, 	ConsonantProperty.VOICED}),
/* 6*/	new Consonant("g" ,  "g",	2,	0.259,	new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.VELAR, 		ConsonantProperty.VOICED}),
/* 7*/	new Consonant("'" ,  "ʔ", 	5,	0.357,	new ConsonantProperty[] {ConsonantProperty.PLOSIVE, 	ConsonantProperty.GLOTTAL}),

/* 8*/	new Consonant("c" ,  "ts",	4, 0.452,	new ConsonantProperty[] {ConsonantProperty.AFFRICATE,	ConsonantProperty.ALVEOLAR, 	ConsonantProperty.VOICED,		ConsonantProperty.SIBILANT}),
/* 9*/	new Consonant("ch",  "tʃ",	4, 0,		new ConsonantProperty[] {ConsonantProperty.AFFRICATE,	ConsonantProperty.POSTALVEOLAR,	ConsonantProperty.VOICELESS,	ConsonantProperty.SIBILANT}),
/*10*/	new Consonant("j" ,  "dʒ",	4, 0.025,	new ConsonantProperty[] {ConsonantProperty.AFFRICATE,	ConsonantProperty.POSTALVEOLAR, ConsonantProperty.VOICED,		ConsonantProperty.SIBILANT}),

/*11*/	new Consonant("ph",  "ɸ",	6, 0.863,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.BILABIAL,		ConsonantProperty.VOICELESS,	ConsonantProperty.NONSIBILANT}),
/*12*/	new Consonant("th",  "θ",	6, 0.937,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.ALVEOLAR,		ConsonantProperty.VOICELESS,	ConsonantProperty.NONSIBILANT}),
/*13*/	new Consonant("kh",  "x",	6, 0.568,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.VELAR,		ConsonantProperty.VOICELESS,	ConsonantProperty.NONSIBILANT}),
/*14*/	new Consonant("qh",  "ʁ",	6, 0,		new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.UVULAR,		ConsonantProperty.VOICELESS,	ConsonantProperty.NONSIBILANT}),
/*15*/	new Consonant("f" ,  "f",	6, 0.093,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.LABIODENTAL,	ConsonantProperty.VOICELESS,	ConsonantProperty.NONSIBILANT}),
/*16*/	new Consonant("s" ,  "s",	7, 0.016,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.ALVEOLAR,		ConsonantProperty.VOICELESS,	ConsonantProperty.SIBILANT}),
/*17*/	new Consonant("sh",  "ʃ",	7, 0.156,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.POSTALVEOLAR,	ConsonantProperty.VOICELESS,	ConsonantProperty.SIBILANT}),

/*18*/	new Consonant("bh",  "β",	8, 0.754,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.BILABIAL, 	ConsonantProperty.VOICED,		ConsonantProperty.NONSIBILANT}),
/*19*/	new Consonant("dh",  "ð",	8, 0.900,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.ALVEOLAR, 	ConsonantProperty.VOICED,		ConsonantProperty.NONSIBILANT}),
/*20*/	new Consonant("gh",  "ɣ",	8, 0.748,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.VELAR, 		ConsonantProperty.VOICED,		ConsonantProperty.NONSIBILANT}),
/*21*/	new Consonant("v" ,  "v",	8, 0.211,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.LABIODENTAL,	ConsonantProperty.VOICED,		ConsonantProperty.NONSIBILANT}),
/*22*/	new Consonant("z" ,  "z",	9, 0.580,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.ALVEOLAR,		ConsonantProperty.VOICED,		ConsonantProperty.SIBILANT}),
/*23*/	new Consonant("zh",  "ʒ",	9, 0.646,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.POSTALVEOLAR,	ConsonantProperty.VOICED,		ConsonantProperty.SIBILANT}),

/*24*/	new Consonant("h" ,  "h",	10, 0.058,	new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.GLOTTAL}),
		
/*25*/	new Consonant("m" ,  "m",	11, 0.015,	new ConsonantProperty[] {ConsonantProperty.NASAL,		ConsonantProperty.BILABIAL}),
/*26*/	new Consonant("n" ,  "n",	11, 0.004,	new ConsonantProperty[] {ConsonantProperty.NASAL,		ConsonantProperty.ALVEOLAR}),
/*27*/	new Consonant("ng",  "ŋ",	11, 0.447,	new ConsonantProperty[] {ConsonantProperty.NASAL,		ConsonantProperty.VELAR}),
/*28*/	new Consonant("mh",  "m̥",	12, 0,		new ConsonantProperty[] {ConsonantProperty.VOICELESS_NASAL,	ConsonantProperty.BILABIAL}),
/*29*/	new Consonant("nh",  "n̥",	12, 0.112,	new ConsonantProperty[] {ConsonantProperty.VOICELESS_NASAL,	ConsonantProperty.ALVEOLAR}),

/*30*/	new Consonant("r" ,  "r",	13, 0,		new ConsonantProperty[] {ConsonantProperty.LIQUID,	 ConsonantProperty.APPROXIMANT,		ConsonantProperty.ALVEOLAR_TRILL}),
/*31*/	new Consonant("l" ,  "l",	13, 0,		new ConsonantProperty[] {ConsonantProperty.LIQUID,	 ConsonantProperty.APPROXIMANT,		ConsonantProperty.LATERAL_APPROX}),
/*32*/	new Consonant("y" ,  "j",	14, 0,		new ConsonantProperty[] {ConsonantProperty.GLIDE,	 ConsonantProperty.APPROXIMANT,		ConsonantProperty.PALATAL_APPROX}),
/*33*/	new Consonant("w" ,  "w",	14, 0,		new ConsonantProperty[] {ConsonantProperty.GLIDE,	 ConsonantProperty.APPROXIMANT,		ConsonantProperty.LABIOVELAR_APPROX}),
	};
}

/**
 * An extension of the Segment class for representing vocalic segments.
 * 
 * @author	Clayton Cooper
 * @version	1.0
 * @since	1.0
 */
class Vowel extends Segment
{
	static int count = 0;
	VowelProperty[] properties;
	public final String diaeresis;
	public final String stress;
	
	/**
	 * Sets Segment fields by calling the superclass's constructor - but also sets this Vowel's id by
	 * checking the static count, and increments it.
	 * @param expression			Character(s) representing this sound's orthographic representation
	 * @param ipa					IPA symbol representing this segment
	 * @param transitionCategory	Character's phonotactic category for the purposes of deciding consonant clusters
	 * @param properties			List of character's phonetic properties
	 */
	public Vowel(String expression, String ipa, String diaeresis, String stress, int transitionCategory, 
					VowelProperty[] properties)
	{
		super(expression, ipa, transitionCategory, properties);
		this.properties = properties;
		this.diaeresis = diaeresis;
		this.stress = stress;
		id = count;
		count++;
	}
	
	/**
	 * Returns true for all instances of this class.
	 */
	public boolean isConsonant()
	{
		return false;
	}
	
	// Defines cross-linguistic consonantal inventory
	static Vowel[] segments = new Vowel[]
	{
		/*0*/	new Vowel("*", "" , "" , "" , 0, new VowelProperty[] {VowelProperty.NULL}),
		/*1*/	new Vowel("a", "a", "ä", "á", 1, new VowelProperty[] {VowelProperty.OPEN}),
		/*2*/	new Vowel("e", "e", "ë", "é", 2, new VowelProperty[] {VowelProperty.MID,		VowelProperty.FRONT}),
		/*3*/	new Vowel("o", "o", "ö", "ó", 2, new VowelProperty[] {VowelProperty.MID,		VowelProperty.BACK}),
		/*4*/	new Vowel("i", "i", "ï", "í", 3, new VowelProperty[] {VowelProperty.CLOSE,	VowelProperty.FRONT}),
		/*5*/	new Vowel("u", "u", "ü", "ú", 3, new VowelProperty[] {VowelProperty.CLOSE,	VowelProperty.BACK}),
		/*6*/	new Vowel("y", "ɨ", "ÿ", "ý", 3, new VowelProperty[] {VowelProperty.CLOSE,	VowelProperty.CENTER}),
		/*7*/	new Vowel(":", "ː", "",  "" , 4, new VowelProperty[] {VowelProperty.LONG}),
	};
}

/**
 * Interface used by ConsonantProperty and VowelProperty enums to grant them some semblance of polymorphism in
 * relevant contexts.
 * 
 * In Gengen, a Segment is defined chiefly by its SegmentProperties. A property's probability represents the odds
 * that sounds with that property will occur in the Phonology; any Segment all the properties of which occur in the
 * Phonology will itself appear in the Phonology's phonemic inventory. Accordingly, the raw chance of a Segment
 * occurring is equal to the product of the probability values of each of its properties. 
 * @since	1.0
 */
interface SegmentProperty
{
	abstract double getProbability();
};

/**
 * The set of phonetic properties used to define consonantal segments. These cover a range of standard properties,
 * including manner and location of articulation, voicing, aspiration, etc., but also further categorizations
 * like 'liquid' and 'glide'. Even more specific labels like 'glottal fricative' or 'velar nasal' are used to
 * control probabilities and ensure they approximate real-world values.
 * 
 * @author	Clayton Cooper
 * @version	1.0
 * @since	1.0
 */
enum ConsonantProperty implements SegmentProperty
{
	PLOSIVE(1), AFFRICATE(.623), FRICATIVE(.922), NASAL(.962), VOICELESS_NASAL(.038), APPROXIMANT(.969),
	GLOTTAL(.745), UVULAR(.140), VELAR(.987), POSTALVEOLAR(.590), ALVEOLAR(.997), LABIODENTAL(.574), BILABIAL(.993),
	VOICELESS(.989), VOICED(.767),
	SIBILANT(.914), NONSIBILANT(.694),
	LIQUID(.896),	// liquid | approximant
	GLIDE(.889),	// glide | approximant
	PALATAL_APPROX(.838), LABIOVELAR_APPROX(.741),	// x | approximant & glide 
	LATERAL_APPROX(.754), ALVEOLAR_TRILL(.661);		// x | approximant & liquid;
	
	private double probability;
	
	ConsonantProperty (double probability)
	{
		this.probability = probability;
	}
	
	public double getProbability() { return probability; }
}

/**
 * The set of phonetic properties used to define vocalic segments. These include chiefly two features: vowel height 
 * (or closeness) and vowel backness. As a possibly temporary provision, the 'schwa' property is included as the sole
 * feature of its corresponding Segment, as is the 'long' property, which follows another vowel (as if in a diphthong)
 * to make it long by nature. 
 * 
 * @author	Clayton Cooper
 * @version	1.0
 * @since	1.0
 */
enum VowelProperty implements SegmentProperty
{
	NULL(/*.169*/ 0), OPEN(.984), MID(.920), CLOSE(.993),
	FRONT(.991), CENTER(.159), BACK(.991),
	LONG(0);	
	
	private double probability;
	
	VowelProperty (double probability)
	{
		this.probability = probability;
	}
	
	public double getProbability() { return probability; }
}