package hiatusResolution;

import gengenv2.structures.VowelPhoneme;

public abstract class LengthenableResolution extends HiatusResolutionMethod 
{
	protected boolean canLengthen;
	protected VowelPhoneme longVowel;
	
	public void configureCompensatoryLenghtening(boolean canLengthen, VowelPhoneme longVowel)
	{
		this.canLengthen = canLengthen;
		this.longVowel = longVowel;
	}
}
