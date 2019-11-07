package gengenv2.morphemes;

import java.util.Random;

import gengenv2.PublicRandom;
import gengenv2.SuffixLibrary;

public class NounClass implements Comparable<NounClass>
{
	private Random rng;
	private double probability;
	private SuffixLibrary suffixes;
	
	public NounClass(double probability)
	{
		rng = PublicRandom.getRNG();
		initialize(probability);
	}
	
	public NounClass()
	{
		rng = PublicRandom.getRNG();
		initialize(rng.nextDouble());
	}
	
	private void initialize(double probability)
	{
		this.probability = probability;
		suffixes = new SuffixLibrary(false);
	}
	
	public void setSuffixLibrary(SuffixLibrary suffixLibrary)
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

}