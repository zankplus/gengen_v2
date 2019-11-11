package gengenv2.structures;

import gengenv2.enums.ConstituentType;

public class PhonemeInstance 
{
	private Phoneme phoneme;
	private ConstituentType type;
	private boolean syllableHead;
	
	public PhonemeInstance(Phoneme phoneme)
	{
		this.phoneme = phoneme;
	}
	
	public Phoneme getPhoneme()
	{
		return phoneme;
	}
	
	
	
	public ConstituentType getType()
	{
		return type;
	}
}
