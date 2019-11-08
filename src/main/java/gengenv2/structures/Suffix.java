package gengenv2.structures;

import gengenv2.enums.SuffixType;

public class Suffix extends Morpheme
{
	private SuffixType type;
	
	public Suffix()
	{
		// 
	}

	public SuffixType getType()
	{
		return type;
	}
	
	public String toString()
	{
		return "-" + super.toString();
	}
}
