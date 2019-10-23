package gengenv2.morphemes;

import gengenv2.enums.VowelProperty;

/**
 * An extension of the Segment class for representing vocalic segments.
 * 
 * @author	Clayton Cooper
 * @version	1.0
 * @since	1.0
 */
public class Vowel extends Segment
{
	private static int count = 0;
	public final VowelProperty[] properties;
	public final String diaeresis;
	public final String stress;
	private final int id;
	
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
	
	public int getID()
	{
		return id;
	}
	
	public static int count()
	{
		return count;
	}
	
	/**
	 * Returns true for all instances of this class.
	 */
	public boolean isConsonant()
	{
		return false;
	}
	
	// Defines cross-linguistic consonantal inventory
	public final static Vowel[] segments = new Vowel[]
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