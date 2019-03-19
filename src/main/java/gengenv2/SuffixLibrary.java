package gengenv2;

import java.util.ArrayList;
import java.util.Collections;

import gengenv2.Phonology.Constituent;

public class SuffixLibrary
{
	private ArrayList<NameEntry> library;
	private boolean normalized;
	
	public SuffixLibrary()
	{
		library = new ArrayList<NameEntry>();
		normalized = false;
	}
	
	public void addName(Name name, double probability)
	{
		NameEntry entry = new NameEntry(name, probability);
		
		boolean duplicate = false;
		for (NameEntry s : library)
			if (s.getName().equals(name))
			{
				duplicate = true;
				s.setProbability(s.getProbability() + probability);
				break;
			}
		
		if (!duplicate)
			library.add(entry);
	}
	
	public void normalize()
	{
		double sum = 0;
		for (NameEntry ne : library)
			sum += ne.getProbability();
		for (NameEntry ne : library)
			ne.setProbability(ne.getProbability() / sum);
		normalized = true;
	}
	
	public void sort()
	{
		Collections.sort(library);
		Collections.reverse(library);
	}
	
	public void exaggerate(double power)
	{
		for (NameEntry ne : library)
			ne.setProbability(Math.pow(ne.getProbability(), power));
		normalized = false;
	}
	
	public ArrayList<NameEntry> getLibrary()
	{
		return library;
	}
}

class NameEntry implements Comparable<NameEntry>
{
	private Name name;
	private double probability;
	
	public NameEntry(Name name, double probability)
	{
		this.name = name;
		this.probability = probability;
	}
	
	public Name getName()
	{
		return name;
	}
	
	public double getProbability()
	{
		return probability;
	}
	
	public void setProbability(double probability)
	{
		this.probability = probability;
	}
	
	public int compareTo(NameEntry ne)
	{
		if (probability > ne.probability)
			return 1;
		else if (probability < ne.probability)
			return -1;
		return 0;
	}
}