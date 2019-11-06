package gengenv2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import gengenv2.morphemes.Constituent;
import gengenv2.morphemes.Suffix;

public class SuffixLibrary
{
	private Random rng;
	private ArrayList<SuffixEntry> library;
	private int count;
	
	private static double baseProbabilityStdev = 0.25;
	
	public SuffixLibrary()
	{
		rng = PublicRandom.getRNG();
		library = new ArrayList<SuffixEntry>();
		count = 0;
	}
	
	public void addSuffix(Suffix name)
	{
		SuffixEntry entry = new SuffixEntry(name, rng.nextGaussian() * baseProbabilityStdev + 1);
		
		boolean duplicate = false;
		for (SuffixEntry se : library)
			if (se.getSuffix().equals(name))
			{
				duplicate = true;
				break;
			}
		
		if (!duplicate)
		{
			library.add(entry);
			count++;
		}
	}
	
	public void zipfScale()
	{
		for (int i = 0; i < library.size(); i++)
		{
			library.get(i).setProbability(library.get(i).getProbability() / (i + 1));
		}
	}
	
	public void normalize()
	{
		double sum = 0;
		for (SuffixEntry ne : library)
			sum += ne.getProbability();
		for (SuffixEntry ne : library)
			ne.setProbability(ne.getProbability() / sum);
	}
	
	public void sort()
	{
		Collections.sort(library);
		Collections.reverse(library);
	}
	
	public void exaggerate(double power)
	{
		for (SuffixEntry ne : library)
			ne.setProbability(Math.pow(ne.getProbability(), power));
	}
	
	
	
	public Suffix pick()
	{
		double rand = rng.nextDouble();
		SuffixEntry curr = null;
		int index = 0;
		
		while (rand > 0)
		{
			curr = library.get(index);
			index++;
		}
		
		return curr.getSuffix();
	}
	
	public ArrayList<SuffixEntry> getLibrary()
	{
		return library;
	}
	
	public int size()
	{
		return count;
	}
	
	public void printMembers()
	{
		for (SuffixEntry se : library)
			System.out.printf("%s\t%.3f\n", se.getSuffix(), se.getProbability());
	}
}

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