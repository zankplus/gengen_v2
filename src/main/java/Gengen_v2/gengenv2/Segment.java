package Gengen_v2.gengenv2;

class Segment extends Object
{
	String expression;
	SegProp[] properties;
	int transitionCategory;
	
	int id;
	static int count = 0;
	
	public Segment(String expression, int transitionCategory, SegProp[] properties)
	{
		this.expression = expression;
		this.transitionCategory = transitionCategory;
		this.properties = properties;
		id = count;
		count++;
	}
	
	public boolean isConsonant()
	{
		return (properties[properties.length - 1] == SegProp.CONSONANT);
	}
	
	public boolean isVowel()
	{
		return (properties[0] == SegProp.VOWEL);
	}
	
	static Segment[] segments = new Segment[]
	{
///* 0*/	new Segment("^" ,  0, new SegProp[]{SegProp.START}),

/* 1*/	new Segment("k" ,  1, new SegProp[]{SegProp.PLOSIVE, 	SegProp.VOICELESS,	SegProp.UNASPIRATED,	SegProp.VELAR,		SegProp.CONSONANT}),
/* 2*/	new Segment("p" ,  1, new SegProp[]{SegProp.PLOSIVE, 	SegProp.VOICELESS,	SegProp.UNASPIRATED,	SegProp.BILABIAL,	SegProp.CONSONANT}),
/* 3*/	new Segment("t" ,  1, new SegProp[]{SegProp.PLOSIVE, 	SegProp.VOICELESS,	SegProp.UNASPIRATED,	SegProp.ALVEOLAR,	SegProp.CONSONANT}),
/* 4*/	new Segment("q" ,  1, new SegProp[]{SegProp.PLOSIVE, 	SegProp.VOICELESS,	SegProp.UNASPIRATED,	SegProp.UVULAR,		SegProp.CONSONANT}),
/* 5*/	new Segment("kh",  2, new SegProp[]{SegProp.PLOSIVE, 	SegProp.VOICELESS,	SegProp.ASPIRATED,		SegProp.VELAR,		SegProp.CONSONANT}),
/* 6*/	new Segment("ph",  2, new SegProp[]{SegProp.PLOSIVE, 	SegProp.VOICELESS,	SegProp.ASPIRATED,		SegProp.BILABIAL,	SegProp.CONSONANT}),
/* 7*/	new Segment("th",  2, new SegProp[]{SegProp.PLOSIVE, 	SegProp.VOICELESS,	SegProp.ASPIRATED,		SegProp.ALVEOLAR,	SegProp.CONSONANT}),
/* 8*/	new Segment("qh",  2, new SegProp[]{SegProp.PLOSIVE, 	SegProp.VOICELESS,	SegProp.ASPIRATED,		SegProp.UVULAR,		SegProp.CONSONANT}),
/* 9*/	new Segment("g" ,  3, new SegProp[]{SegProp.PLOSIVE, 	SegProp.VOICED,		SegProp.BREATHLESS,		SegProp.VELAR,		SegProp.CONSONANT}),
/*10*/	new Segment("b" ,  3, new SegProp[]{SegProp.PLOSIVE, 	SegProp.VOICED,		SegProp.BREATHLESS,		SegProp.BILABIAL,	SegProp.CONSONANT}),
/*11*/	new Segment("d" ,  3, new SegProp[]{SegProp.PLOSIVE, 	SegProp.VOICED,		SegProp.BREATHLESS,		SegProp.ALVEOLAR,	SegProp.CONSONANT}),
/*12*/	new Segment("gh",  4, new SegProp[]{SegProp.PLOSIVE, 	SegProp.VOICED,		SegProp.BREATHY,		SegProp.VELAR,		SegProp.CONSONANT}),
/*13*/	new Segment("bh",  4, new SegProp[]{SegProp.PLOSIVE, 	SegProp.VOICED,		SegProp.BREATHY,		SegProp.BILABIAL,	SegProp.CONSONANT}),
/*14*/	new Segment("dh",  4, new SegProp[]{SegProp.PLOSIVE, 	SegProp.VOICED,		SegProp.BREATHY,		SegProp.ALVEOLAR,	SegProp.CONSONANT}),
	
/*15*/	new Segment("ch",  5, new SegProp[]{SegProp.AFFRICATE,	SegProp.VOICELESS,	SegProp.POSTALVEOLAR,	SegProp.SIBILANT,	SegProp.CONSONANT}),
/*16*/	new Segment("j" ,  5, new SegProp[]{SegProp.AFFRICATE, 	SegProp.VOICED,		SegProp.POSTALVEOLAR,	SegProp.SIBILANT,	SegProp.CONSONANT}),

/*17*/	new Segment("'" ,  6, new SegProp[]{SegProp.PLOSIVE, 	SegProp.GLOTTAL,	SegProp.CONSONANT}),

/*18*/	new Segment("f" ,  7, new SegProp[]{SegProp.FRICATIVE,	SegProp.VOICELESS,	SegProp.LABIODENTAL,	SegProp.CONSONANT}),
/*19*/	new Segment("s" ,  8, new SegProp[]{SegProp.FRICATIVE,	SegProp.VOICELESS,	SegProp.ALVEOLAR,		SegProp.SIBILANT,	SegProp.CONSONANT}),
/*20*/	new Segment("sh",  8, new SegProp[]{SegProp.FRICATIVE,	SegProp.VOICELESS,	SegProp.POSTALVEOLAR,	SegProp.SIBILANT,	SegProp.CONSONANT}),
/*21*/	new Segment("v" ,  9, new SegProp[]{SegProp.FRICATIVE,	SegProp.VOICED,		SegProp.LABIODENTAL,	SegProp.CONSONANT}),
/*22*/	new Segment("z" , 10, new SegProp[]{SegProp.FRICATIVE,	SegProp.VOICED,		SegProp.ALVEOLAR,		SegProp.SIBILANT,	SegProp.VOICED_SIBILANT_FRICATIVE,	SegProp.CONSONANT}),
/*23*/	new Segment("zh", 10, new SegProp[]{SegProp.FRICATIVE,	SegProp.VOICED,		SegProp.POSTALVEOLAR,	SegProp.SIBILANT,	SegProp.VOICED_SIBILANT_FRICATIVE,	SegProp.CONSONANT}),
/*24*/	new Segment("h" , 11, new SegProp[]{SegProp.FRICATIVE,	SegProp.GLOTTAL,	SegProp.CONSONANT}),
		
/*25*/	new Segment("m" , 12, new SegProp[]{SegProp.NASAL,		SegProp.BILABIAL,	SegProp.CONSONANT}),
/*26*/	new Segment("n" , 12, new SegProp[]{SegProp.NASAL,		SegProp.ALVEOLAR,	SegProp.CONSONANT}),
/*27*/	new Segment("ng", 13, new SegProp[]{SegProp.NASAL,		SegProp.VELAR,		SegProp.VELAR_NASAL,	SegProp.CONSONANT}),
/*28*/	new Segment("mh", 14, new SegProp[]{SegProp.VOICELESS_NASAL,	SegProp.BILABIAL,	SegProp.CONSONANT}),
/*29*/	new Segment("nh", 14, new SegProp[]{SegProp.VOICELESS_NASAL,	SegProp.ALVEOLAR,	SegProp.CONSONANT}),

/*30*/	new Segment("r" , 15, new SegProp[]{SegProp.LIQUID,	 SegProp.APPROXIMANT,		SegProp.ALVEOLAR_TRILL,		SegProp.CONSONANT}),
/*31*/	new Segment("l" , 15, new SegProp[]{SegProp.LIQUID,	 SegProp.APPROXIMANT,		SegProp.LATERAL_APPROX,		SegProp.CONSONANT}),
/*32*/	new Segment("y" , 16, new SegProp[]{SegProp.GLIDE,	 SegProp.APPROXIMANT,		SegProp.PALATAL_APPROX,		SegProp.CONSONANT}),
/*33*/	new Segment("w" , 16, new SegProp[]{SegProp.GLIDE,	 SegProp.APPROXIMANT,		SegProp.LABIOVELAR_APPROX,	SegProp.CONSONANT}),

/*34*/	new Segment("a" , 17, new SegProp[]{SegProp.VOWEL,	SegProp.OPEN,	SegProp.SHORT}),
/*35*/	new Segment("ā" , 18, new SegProp[]{SegProp.VOWEL,	SegProp.OPEN,	SegProp.LONG}),
/*36*/	new Segment("ă" , 19, new SegProp[]{SegProp.VOWEL,	SegProp.OPEN,	SegProp.OVERSHORT}),

/*37*/	new Segment("e" , 20, new SegProp[]{SegProp.VOWEL,	SegProp.MID,	SegProp.FRONT,	SegProp.SHORT}),
/*38*/	new Segment("ē" , 21, new SegProp[]{SegProp.VOWEL,	SegProp.MID,	SegProp.FRONT,	SegProp.LONG}),
/*39*/	new Segment("ĕ" , 22, new SegProp[]{SegProp.VOWEL,	SegProp.MID,	SegProp.FRONT,	SegProp.OVERSHORT}),
		
/*40*/	new Segment("o" , 20, new SegProp[]{SegProp.VOWEL,	SegProp.MID,	SegProp.BACK,	SegProp.SHORT}),
/*41*/	new Segment("ō" , 21, new SegProp[]{SegProp.VOWEL,	SegProp.MID,	SegProp.BACK,	SegProp.LONG}),
/*42*/	new Segment("ŏ" , 22, new SegProp[]{SegProp.VOWEL,	SegProp.MID,	SegProp.BACK,	SegProp.OVERSHORT}),

/*43*/	new Segment("i" , 23, new SegProp[]{SegProp.VOWEL,	SegProp.CLOSE,	SegProp.FRONT,	SegProp.SHORT}),
/*44*/	new Segment("ī" , 24, new SegProp[]{SegProp.VOWEL,	SegProp.CLOSE,	SegProp.FRONT,	SegProp.LONG}),
/*45*/	new Segment("ĭ" , 25, new SegProp[]{SegProp.VOWEL,	SegProp.CLOSE,	SegProp.FRONT,	SegProp.OVERSHORT}),

/*46*/	new Segment("y" , 23, new SegProp[]{SegProp.VOWEL,	SegProp.CLOSE,	SegProp.CENTER,	SegProp.SHORT}),
/*47*/	new Segment("ȳ" , 24, new SegProp[]{SegProp.VOWEL,	SegProp.CLOSE,	SegProp.CENTER,	SegProp.LONG}),
/*48*/	new Segment("y̆" , 25, new SegProp[]{SegProp.VOWEL,	SegProp.CLOSE,	SegProp.CENTER,	SegProp.OVERSHORT}),

/*49*/	new Segment("u" , 23, new SegProp[]{SegProp.VOWEL,	SegProp.CLOSE,	SegProp.BACK,	SegProp.SHORT}),
/*50*/	new Segment("ū" , 24, new SegProp[]{SegProp.VOWEL,	SegProp.CLOSE,	SegProp.BACK,	SegProp.LONG}),
/*51*/	new Segment("ŭ" , 25, new SegProp[]{SegProp.VOWEL,	SegProp.CLOSE,	SegProp.BACK,	SegProp.OVERSHORT}),
			
///*52*/	new Segment("$" , 26, new SegProp[]{SegProp.END})
		

	
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
	SHORT(1 /*.993*/), LONG(.113), OVERSHORT(/*.020*/ 0),
	OPEN(.984), MID(.920), CLOSE(.993),
	FRONT(.991), CENTER(.159), BACK(.991);
	
	double probability;	
	
	SegProp (double probability)
	{
		this.probability = probability;
	}
}

	