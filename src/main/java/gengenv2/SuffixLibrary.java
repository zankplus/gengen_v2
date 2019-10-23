package gengenv2;

import java.util.ArrayList;
import java.util.Collections;

import gengenv2.morphemes.Constituent;

public class SuffixLibrary
{
	private Phonology p;
	private ArrayList<NameEntry> library;
	Constituent inserendum;
	private int count;
	private boolean normalized;
	private double maxInformationContent = 9;
	
	public SuffixLibrary(Phonology p)
	{
		this.p = p;
		inserendum = p.medialOnsets.pickSimple();
		library = new ArrayList<NameEntry>();
		normalized = false;
		count = 0;
	}
	
	public void addName(Name name, double probability)
	{
		NameEntry entry = new NameEntry(name, probability);
		if (name.getInformationContent() > maxInformationContent)
			return;
		
		boolean duplicate = false;
		for (NameEntry s : library)
			if (s.getName().equals(name))
			{
				duplicate = true;
				s.setProbability(s.getProbability() + probability);
				break;
			}
		
		if (!duplicate)
		{
			library.add(entry);
			count++;
		}
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
	
	public void zipfScale()
	{
		for (int i = 0; i < library.size(); i++)
		{
			library.get(i).setProbability(library.get(i).getProbability() / (i + 1));
		}
	}
	
	public void pick()
	{
		if (!normalized)
		{
			normalize();
			normalized = true;
		}
	}
	
	public ArrayList<NameEntry> getLibrary()
	{
		return library;
	}
	
	public int size()
	{
		return count;
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