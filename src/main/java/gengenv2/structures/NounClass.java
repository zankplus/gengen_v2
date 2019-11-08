package gengenv2.structures;

import java.util.Random;

import gengenv2.PublicRandom;

public class NounClass implements Comparable<NounClass>
{
	private Random rng;
	private double probability;
	private MorphemeLibrary suffixes;
	
	public NounClass(MorphemeLibrary suffixes, double probability)
	{
		rng = PublicRandom.getRNG();
		this.suffixes = suffixes;
		this.probability = probability;
	}
	
	public void setSuffixLibrary(MorphemeLibrary suffixLibrary)
	{
		this.suffixes = suffixLibrary;
	}
	
	public int compareTo(NounClass other) 
	{
		if (this.probability > other.probability)
			return 1;
		else if (this.probability < other.probability)
			return -1;
		else
			return 0;
	}
	
	public Suffix pickSuffix()
	{
		return (Suffix) suffixes.pick();
	}

}