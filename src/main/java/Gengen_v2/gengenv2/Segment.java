package Gengen_v2.gengenv2;

abstract class Segment extends Object
{
	String expression;
	SegmentProperty[] properties;
	int transitionCategory;
	int id;
	
	public Segment(String expression, int transitionCategory, SegmentProperty[] properties)
	{
		this.expression = expression;
		this.transitionCategory = transitionCategory;
		this.properties = properties;
	}
	
	abstract public boolean isConsonant();
}

class Consonant extends Segment
{
	static int count = 0;
	ConsonantProperty[] properties;
	
	public Consonant(String expression, int transitionCategory, ConsonantProperty[] properties)
	{
		super(expression, transitionCategory, properties);
		this.properties = properties; 
		id = count;
		count++;
	}
	
	public boolean isConsonant()
	{
		return true;
	}
	
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
/*23*/	new Consonant("h" , 10, new ConsonantProperty[] {ConsonantProperty.FRICATIVE,	ConsonantProperty.GLOTTAL}),
		
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

class Vowel extends Segment
{
	static int count = 0;
	VowelProperty[] properties;
	
	public Vowel(String expression, int transitionCategory, VowelProperty[] properties)
	{
		super(expression, transitionCategory, properties);
		this.properties = properties;
		id = count;
		count++;
	}
	
	public boolean isConsonant()
	{
		return false;
	}
	
	static Vowel[] segments = new Vowel[]
	{
		/*0*/	new Vowel("ə" , 0, new VowelProperty[] {VowelProperty.SCHWA}),
		/*1*/	new Vowel("a" , 1, new VowelProperty[] {VowelProperty.OPEN}),
		/*2*/	new Vowel("e" , 2, new VowelProperty[] {VowelProperty.MID,	VowelProperty.FRONT}),
		/*3*/	new Vowel("o" , 2, new VowelProperty[] {VowelProperty.MID,	VowelProperty.BACK}),
		/*4*/	new Vowel("i" , 3, new VowelProperty[] {VowelProperty.CLOSE,	VowelProperty.FRONT}),
		/*5*/	new Vowel("y" , 3, new VowelProperty[] {VowelProperty.CLOSE,	VowelProperty.CENTER}),
		/*6*/	new Vowel("u" , 3, new VowelProperty[] {VowelProperty.CLOSE,	VowelProperty.BACK}),
		/*7*/	new Vowel(":" , 4, new VowelProperty[] {VowelProperty.LONG}),
	};
}

interface SegmentProperty
{
	abstract double getProbability();
};

enum ConsonantProperty implements SegmentProperty
{
	PLOSIVE(1), AFFRICATE(.624), FRICATIVE(.922), NASAL(.962), VOICELESS_NASAL(.038), APPROXIMANT(.969),
	GLOTTAL(.721), UVULAR(.140), VELAR(.953), POSTALVEOLAR(.590), ALVEOLAR(.965), LABIODENTAL(.510), BILABIAL(.942),
	VOICELESS(.989), VOICED(.767),
	UNASPIRATED(.949), ASPIRATED(.253),
	BREATHLESS(.789), BREATHY(.027),
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

enum VowelProperty implements SegmentProperty
{
	SCHWA(.169), OPEN(.984), MID(.920), CLOSE(.993),
	FRONT(.991), CENTER(.159), BACK(.991),
	LONG(.250);	// this one is extremely fudged
	
	double probability;
	
	VowelProperty (double probability)
	{
		this.probability = probability;
	}
	
	public double getProbability() { return probability; }
}