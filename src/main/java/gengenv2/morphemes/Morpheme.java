package gengenv2.morphemes;

import java.util.ArrayList;

import gengenv2.Phonology;

public abstract class Morpheme
{
	public ArrayList<Phoneme> phonemes;	
	private double informationContent;
	public int[] syllables;
	
	public Morpheme()
	{
		phonemes = new ArrayList<Phoneme>();
	}
	
	public void add(Phoneme ph)
	{
		phonemes.add(ph);
	}
	
	/** 
	 * @return	A measurement of the information content of this Name
	 */
	public double getInformationContent()
	{
		return informationContent;
	}
	
	/**
	 * @param ic	Value to which to set this Name's information content
	 */
	public void setInformationContent(double ic)
	{
		this.informationContent = ic;
	}
	
	public String toString()
	{
		String result = "";
		for (Phoneme ph : phonemes)
			result += ph.segment.expression;
		return result;
	}
	
	public abstract double minimumInformationContent();
}
