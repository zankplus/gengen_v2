package Gengen_v2.gengenv2;

abstract class Segment extends Object
{
	String expression;
	SegProp[] properties;
	int transitionCategory;
	int id;
	
	public Segment(String expression, int transitionCategory, SegProp[] properties)
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
	
	public Consonant(String expression, int transitionCategory, SegProp[] properties)
	{
		super(expression, transitionCategory, properties);
		id = count;
		count++;
	}
	
	public boolean isConsonant()
	{
		return true;
	}
	
	static Consonant[] segments = new Consonant[]
	{
/* 0*/	new Consonant("k" ,  0, new SegProp[] {SegProp.PLOSIVE, 	SegProp.VOICELESS,	SegProp.UNASPIRATED,	SegProp.VELAR}),
/* 1*/	new Consonant("p" ,  0, new SegProp[] {SegProp.PLOSIVE, 	SegProp.VOICELESS,	SegProp.UNASPIRATED,	SegProp.BILABIAL}),
/* 2*/	new Consonant("t" ,  0, new SegProp[] {SegProp.PLOSIVE, 	SegProp.VOICELESS,	SegProp.UNASPIRATED,	SegProp.ALVEOLAR}),
/* 3*/	new Consonant("q" ,  0, new SegProp[] {SegProp.PLOSIVE, 	SegProp.VOICELESS,	SegProp.UNASPIRATED,	SegProp.UVULAR}),
/* 4*/	new Consonant("kh",  1, new SegProp[] {SegProp.PLOSIVE, 	SegProp.VOICELESS,	SegProp.ASPIRATED,		SegProp.VELAR}),
/* 5*/	new Consonant("ph",  1, new SegProp[] {SegProp.PLOSIVE, 	SegProp.VOICELESS,	SegProp.ASPIRATED,		SegProp.BILABIAL}),
/* 6*/	new Consonant("th",  1, new SegProp[] {SegProp.PLOSIVE, 	SegProp.VOICELESS,	SegProp.ASPIRATED,		SegProp.ALVEOLAR}),
/* 7*/	new Consonant("qh",  1, new SegProp[] {SegProp.PLOSIVE, 	SegProp.VOICELESS,	SegProp.ASPIRATED,		SegProp.UVULAR}),
/* 8*/	new Consonant("g" ,  2, new SegProp[] {SegProp.PLOSIVE, 	SegProp.VOICED,		SegProp.BREATHLESS,		SegProp.VELAR}),
/* 9*/	new Consonant("b" ,  2, new SegProp[] {SegProp.PLOSIVE, 	SegProp.VOICED,		SegProp.BREATHLESS,		SegProp.BILABIAL}),
/*10*/	new Consonant("d" ,  2, new SegProp[] {SegProp.PLOSIVE, 	SegProp.VOICED,		SegProp.BREATHLESS,		SegProp.ALVEOLAR}),
/*11*/	new Consonant("gh",  3, new SegProp[] {SegProp.PLOSIVE, 	SegProp.VOICED,		SegProp.BREATHY,		SegProp.VELAR}),
/*12*/	new Consonant("bh",  3, new SegProp[] {SegProp.PLOSIVE, 	SegProp.VOICED,		SegProp.BREATHY,		SegProp.BILABIAL}),
/*13*/	new Consonant("dh",  3, new SegProp[] {SegProp.PLOSIVE, 	SegProp.VOICED,		SegProp.BREATHY,		SegProp.ALVEOLAR}),

/*14*/	new Consonant("ch",  4, new SegProp[] {SegProp.AFFRICATE,	SegProp.VOICELESS,	SegProp.POSTALVEOLAR,	SegProp.SIBILANT}),
/*15*/	new Consonant("j" ,  4, new SegProp[] {SegProp.AFFRICATE, 	SegProp.VOICED,		SegProp.POSTALVEOLAR,	SegProp.SIBILANT}),

/*16*/	new Consonant("'" ,  5, new SegProp[] {SegProp.PLOSIVE, 	SegProp.GLOTTAL}),

/*17*/	new Consonant("f" ,  6, new SegProp[] {SegProp.FRICATIVE,	SegProp.VOICELESS,	SegProp.LABIODENTAL}),
/*18*/	new Consonant("s" ,  7, new SegProp[] {SegProp.FRICATIVE,	SegProp.VOICELESS,	SegProp.ALVEOLAR,		SegProp.SIBILANT}),
/*19*/	new Consonant("sh",  7, new SegProp[] {SegProp.FRICATIVE,	SegProp.VOICELESS,	SegProp.POSTALVEOLAR,	SegProp.SIBILANT}),
/*20*/	new Consonant("v" ,  8, new SegProp[] {SegProp.FRICATIVE,	SegProp.VOICED,		SegProp.LABIODENTAL}),
/*21*/	new Consonant("z" ,  9, new SegProp[] {SegProp.FRICATIVE,	SegProp.VOICED,		SegProp.ALVEOLAR,		SegProp.SIBILANT,	SegProp.VOICED_SIBILANT_FRICATIVE}),
/*22*/	new Consonant("zh",  9, new SegProp[] {SegProp.FRICATIVE,	SegProp.VOICED,		SegProp.POSTALVEOLAR,	SegProp.SIBILANT,	SegProp.VOICED_SIBILANT_FRICATIVE}),
/*23*/	new Consonant("h" , 10, new SegProp[] {SegProp.FRICATIVE,	SegProp.GLOTTAL}),
		
/*24*/	new Consonant("m" , 11, new SegProp[] {SegProp.NASAL,		SegProp.BILABIAL}),
/*25*/	new Consonant("n" , 11, new SegProp[] {SegProp.NASAL,		SegProp.ALVEOLAR}),
/*26*/	new Consonant("ng", 12, new SegProp[] {SegProp.NASAL,		SegProp.VELAR,		SegProp.VELAR_NASAL}),
/*27*/	new Consonant("mh", 13, new SegProp[] {SegProp.VOICELESS_NASAL,	SegProp.BILABIAL}),
/*28*/	new Consonant("nh", 13, new SegProp[] {SegProp.VOICELESS_NASAL,	SegProp.ALVEOLAR}),

/*29*/	new Consonant("r" , 14, new SegProp[] {SegProp.LIQUID,	 SegProp.APPROXIMANT,		SegProp.ALVEOLAR_TRILL}),
/*30*/	new Consonant("l" , 14, new SegProp[] {SegProp.LIQUID,	 SegProp.APPROXIMANT,		SegProp.LATERAL_APPROX}),
/*31*/	new Consonant("y" , 15, new SegProp[] {SegProp.GLIDE,	 SegProp.APPROXIMANT,		SegProp.PALATAL_APPROX}),
/*32*/	new Consonant("w" , 15, new SegProp[] {SegProp.GLIDE,	 SegProp.APPROXIMANT,		SegProp.LABIOVELAR_APPROX}),
	};
}

class Vowel extends Segment
{
	static int count = 0;
	
	public Vowel(String expression, int transitionCategory, SegProp[] properties)
	{
		super(expression, transitionCategory, properties);
		id = count;
		count++;
	}
	
	public boolean isConsonant()
	{
		return false;
	}
	
	static Vowel[] segments = new Vowel[]
	{
		/*0*/	new Vowel("a" , 17, new SegProp[] {SegProp.OPEN}),
		/*1*/	new Vowel("e" , 18, new SegProp[] {SegProp.MID,	SegProp.FRONT}),
		/*2*/	new Vowel("o" , 18, new SegProp[] {SegProp.MID,	SegProp.BACK}),
		/*3*/	new Vowel("i" , 19, new SegProp[] {SegProp.CLOSE,	SegProp.FRONT}),
		/*4*/	new Vowel("y" , 19, new SegProp[] {SegProp.CLOSE,	SegProp.CENTER}),
		/*5*/	new Vowel("u" , 19, new SegProp[] {SegProp.CLOSE,	SegProp.BACK}),
		/*6*/	new Vowel(":" , 20, new SegProp[] {SegProp.LONG}),
	};
}



enum SegProp
{
	START(1), END(1),
	VOWEL(1), CONSONANT(1),
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
	VELAR_NASAL(.552), VOICED_SIBILANT_FRICATIVE(.412),
	/*SHORT(1), LONG(.113), OVERSHORT(0),*/
	OPEN(.984), MID(.920), CLOSE(.993),
	FRONT(.991), CENTER(.159), BACK(.991),
	LONG(.100);	// this one is extremely fudged
	
	double probability;	
	
	SegProp (double probability)
	{
		this.probability = probability;
	}
}

	