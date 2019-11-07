package gengenv2;

import gengenv2.morphemes.Suffix;

class SuffixEntry implements Comparable<SuffixEntry>
{
	private Suffix suffix;
	private double probability;
	
	public SuffixEntry(Suffix suffix, double probability)
	{
		this.suffix = suffix;;
		this.probability = probability;
	}
	
	public Suffix getSuffix()
	{
		return suffix;
	}
	
	public double getProbability()
	{
		return probability;
	}
	
	public void setProbability(double probability)
	{
		this.probability = probability;
	}
	
	public int compareTo(SuffixEntry se)
	{
		if (probability > se.probability)
			return 1;
		else if (probability < se.probability)
			return -1;
		return 0;
	}
}