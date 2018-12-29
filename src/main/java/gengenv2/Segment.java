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
	SegmentProperty[] properties;
	int transitionCategory;
	int id;
	
	/**
	 * A basic constructor that sets the most fundamental variables of the Segment. The id field is not set here
	 * but in the constructors of Segment's child classes, as the id of each Consonant is unique among Consonants,
	 * and just so for Vowels, and so this value depends on the static counts only within those classes.
	 *    
	 * @param expression			The character or characters used to denote this sound orthographically
	 * @param transitionCategory	The category to which this sound belongs for the purposes of cluster construction
	 * @param properties			The list of consonant or vowel properties that define this sound
	 */
	public Segment(String expression, int transitionCategory, SegmentProperty[] properties)
	{
		this.expression = expression;
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
	ConsonantProperty[] properties;
	
	/**
	 * Sets Segment fields by calling the superclass's constructor - but also sets this Consonant's id by
	 * checking the static count, and increments it.
	 * @param expression			Character(s) representing this sound's orthographic representation
	 * @param transitionCategory	Character's phonotactic category for the purposes of deciding consonant clusters
	 * @param properties			List of character's phonetic properties
	 */
	public Consonant(String expression, int transitionCategory, ConsonantProperty[] properties)
	{
		super(expression, transitionCategory, properties);
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
/* 0*/	new Consonant("k" ,  0, new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.VELAR, 		ConsonantProperty.VOICELESS,	ConsonantProperty.UNASPIRATED}),
/* 1*/	new Consonant("p" ,  0, new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.BILABIAL, 	ConsonantProperty.VOICELESS,	ConsonantProperty.UNASPIRATED}),
/* 2*/	new Consonant("t" ,  0, new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.ALVEOLAR, 	ConsonantProperty.VOICELESS,	ConsonantProperty.UNASPIRATED}),
/* 3*/	new Consonant("q" ,  0, new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.UVULAR, 		ConsonantProperty.VOICELESS,	ConsonantProperty.UNASPIRATED}),

/* 4*/	new Consonant("kh",  1, new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.VELAR, 		ConsonantProperty.VOICELESS,	ConsonantProperty.ASPIRATED}),
/* 5*/	new Consonant("ph",  1, new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.BILABIAL, 	ConsonantProperty.VOICELESS,	ConsonantProperty.ASPIRATED}),
/* 6*/	new Consonant("th",  1, new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.ALVEOLAR, 	ConsonantProperty.VOICELESS,	ConsonantProperty.ASPIRATED}),
/* 7*/	new Consonant("qh",  1, new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.UVULAR, 		ConsonantProperty.VOICELESS,	ConsonantProperty.ASPIRATED}),

/* 8*/	new Consonant("g" ,  2, new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.VELAR, 		ConsonantProperty.VOICED,		ConsonantProperty.BREATHLESS}),
/* 9*/	new Consonant("b" ,  2, new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.BILABIAL, 	ConsonantProperty.VOICED,		ConsonantProperty.BREATHLESS}),
/*10*/	new Consonant("d" ,  2, new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.ALVEOLAR, 	ConsonantProperty.VOICED,		ConsonantProperty.BREATHLESS}),

/*11*/	new Consonant("gh",  3, new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.VELAR, 		ConsonantProperty.VOICED,		ConsonantProperty.BREATHY}),
/*12*/	new Consonant("bh",  3, new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.BILABIAL, 	ConsonantProperty.VOICED,		ConsonantProperty.BREATHY}),
/*13*/	new Consonant("dh",  3, new ConsonantProperty[] {ConsonantProperty.PLOSIVE,		ConsonantProperty.ALVEOLAR, 	ConsonantProperty.VOICED,		ConsonantProperty.BREATHY}),

/*14*/	new Consonant("ch",  4, new ConsonantProperty[] {ConsonantProperty.AFFRICATE,	ConsonantProperty.POSTALVEOLAR,	ConsonantProperty.VOICELESS,	ConsonantProperty.SIBILANT}),
/*15*/	new Consonant("j" ,  4, new ConsonantProperty[] {ConsonantProperty.AFFRICATE,	ConsonantProperty.POSTALVEOLAR, ConsonantProperty.VOICED,		ConsonantProperty.SIBILANT}),

/*16*/	new Consonant("'" ,  5, new ConsonantProperty[] {ConsonantProperty.PLOSIVE, 	ConsonantProperty.GLOTTAL}),

/*17*/	new Consonant("f" ,  6, new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.LABIODENTAL,	ConsonantProperty.VOICELESS}),
/*18*/	new Consonant("s" ,  7, new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.ALVEOLAR,		ConsonantProperty.VOICELESS,	ConsonantProperty.SIBILANT}),
/*19*/	new Consonant("sh",  7, new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.POSTALVEOLAR,	ConsonantProperty.VOICELESS,	ConsonantProperty.SIBILANT}),
/*20*/	new Consonant("v" ,  8, new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.LABIODENTAL,	ConsonantProperty.VOICED}),
/*21*/	new Consonant("z" ,  9, new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.ALVEOLAR,		ConsonantProperty.VOICED,		ConsonantProperty.SIBILANT,	ConsonantProperty.VOICED_SIBILANT_FRICATIVE}),
/*22*/	new Consonant("zh",  9, new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.POSTALVEOLAR,	ConsonantProperty.VOICED,		ConsonantProperty.SIBILANT,	ConsonantProperty.VOICED_SIBILANT_FRICATIVE}),
/*23*/	new Consonant("h" , 10, new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.GLOTTAL_FRICATIVE}),
		
/*24*/	new Consonant("m" , 11, new ConsonantProperty[] {ConsonantProperty.NASAL,		ConsonantProperty.BILABIAL}),
/*25*/	new Consonant("n" , 11, new ConsonantProperty[] {ConsonantProperty.NASAL,		ConsonantProperty.ALVEOLAR}),
/*26*/	new Consonant("ng", 11, new ConsonantProperty[] {ConsonantProperty.NASAL,		ConsonantProperty.VELAR,		ConsonantProperty.VELAR_NASAL}),
/*27*/	new Consonant("mh", 12, new ConsonantProperty[] {ConsonantProperty.VOICELESS_NASAL,	ConsonantProperty.BILABIAL}),
/*28*/	new Consonant("nh", 12, new ConsonantProperty[] {ConsonantProperty.VOICELESS_NASAL,	ConsonantProperty.ALVEOLAR}),

/*29*/	new Consonant("r" , 13, new ConsonantProperty[] {ConsonantProperty.LIQUID,	 ConsonantProperty.APPROXIMANT,		ConsonantProperty.ALVEOLAR_TRILL}),
/*30*/	new Consonant("l" , 13, new ConsonantProperty[] {ConsonantProperty.LIQUID,	 ConsonantProperty.APPROXIMANT,		ConsonantProperty.LATERAL_APPROX}),
/*31*/	new Consonant("y" , 14, new ConsonantProperty[] {ConsonantProperty.GLIDE,	 ConsonantProperty.APPROXIMANT,		ConsonantProperty.PALATAL_APPROX}),
/*32*/	new Consonant("w" , 14, new ConsonantProperty[] {ConsonantProperty.GLIDE,	 ConsonantProperty.APPROXIMANT,		ConsonantProperty.LABIOVELAR_APPROX}),
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
	
	/**
	 * Sets Segment fields by calling the superclass's constructor - but also sets this Vowel's id by
	 * checking the static count, and increments it.
	 * @param expression			Character(s) representing this sound's orthographic representation
	 * @param transitionCategory	Character's phonotactic category for the purposes of deciding consonant clusters
	 * @param properties			List of character's phonetic properties
	 */
	public Vowel(String expression, String diaeresis, int transitionCategory, VowelProperty[] properties)
	{
		super(expression, transitionCategory, properties);
		this.properties = properties;
		this.diaeresis = diaeresis;
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
		/*0*/	new Vowel("ə", "",  0, new VowelProperty[] {VowelProperty.SCHWA}),
		/*1*/	new Vowel("a", "ä", 1, new VowelProperty[] {VowelProperty.OPEN}),
		/*2*/	new Vowel("e", "ë", 2, new VowelProperty[] {VowelProperty.MID,		VowelProperty.FRONT}),
		/*3*/	new Vowel("o", "ö", 2, new VowelProperty[] {VowelProperty.MID,		VowelProperty.BACK}),
		/*4*/	new Vowel("i", "ï", 3, new VowelProperty[] {VowelProperty.CLOSE,	VowelProperty.FRONT}),
		/*5*/	new Vowel("u", "ü", 3, new VowelProperty[] {VowelProperty.CLOSE,	VowelProperty.BACK}),
		/*6*/	new Vowel("y", "ÿ", 3, new VowelProperty[] {VowelProperty.CLOSE,	VowelProperty.CENTER}),
		/*7*/	new Vowel(":", "",  4, new VowelProperty[] {VowelProperty.LONG}),
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
	PLOSIVE(1), AFFRICATE(.624), FRICATIVE(.922), NASAL(.962), VOICELESS_NASAL(.038), APPROXIMANT(.969),
	GLOTTAL(.166), UVULAR(.140), VELAR(.953), POSTALVEOLAR(.590), ALVEOLAR(.965), LABIODENTAL(.510), BILABIAL(.942),
	VOICELESS(.989), VOICED(.767),
	UNASPIRATED(.949), ASPIRATED(.253),
	BREATHLESS(.789), BREATHY(.027),
	GLOTTAL_FRICATIVE(.647),
	SIBILANT(.914),
	LIQUID(.924),	// liquid | approximant
	GLIDE(.912),	// glide | approximant
	PALATAL_APPROX(.865), LABIOVELAR_APPROX(.761),	// x | approximant & glide 
	LATERAL_APPROX(.778), ALVEOLAR_TRILL(.698),		// x | approximant & liquid
	VELAR_NASAL(.552), VOICED_SIBILANT_FRICATIVE(.412);
	
	double probability;
	
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
	SCHWA(/*.169*/ 0), OPEN(.984), MID(.920), CLOSE(.993),
	FRONT(.991), CENTER(.159), BACK(.991),
	LONG(.250);	// this one is extremely fudged
	
	double probability;
	
	VowelProperty (double probability)
	{
		this.probability = probability;
	}
	
	public double getProbability() { return probability; }
}