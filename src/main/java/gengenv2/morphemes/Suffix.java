package gengenv2.morphemes;

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
	
	@Override
	public double minimumInformationContent()
	{
		return 0;
	}
	
	public String toString()
	{
		return "-" + super.toString();
	}
}
