package gengenv2.structures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import gengenv2.PublicRandom;

public class MorphemeLibrary
{
	private Random rng;
	private ArrayList<MorphemeEntry> library;
	private int count;
	private boolean allowDuplicates;
	
	private static double baseProbabilityStdev = 0.25;
	
	public MorphemeLibrary(boolean allowDuplicates)
	{
		rng = PublicRandom.getRNG();
		library = new ArrayList<MorphemeEntry>();
		count = 0;
		this.allowDuplicates = allowDuplicates;
	}
	
	public void addMorpheme(Morpheme morpheme)
	{
		addMorpheme(morpheme, rng.nextGaussian() * baseProbabilityStdev + 1);
	}
	
	public void addMorpheme(Morpheme morpheme, double probability)
	{
		MorphemeEntry entry = new MorphemeEntry(morpheme, probability);	
		
		boolean duplicate = false;
		if (!allowDuplicates)
			for (MorphemeEntry se : library)
				if (se.getMorpheme().equals(morpheme))
				{
					duplicate = true;
					se.setProbability(se.getProbability() + probability);
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
		for (MorphemeEntry ne : library)
			sum += ne.getProbability();
		for (MorphemeEntry ne : library)
			ne.setProbability(ne.getProbability() / sum);
	}
	
	public void sort()
	{
		Collections.sort(library);
		Collections.reverse(library);
	}
	
	public void exaggerate(double power)
	{
		for (MorphemeEntry ne : library)
			ne.setProbability(Math.pow(ne.getProbability(), power));
	}
	
	
	
	public Morpheme pick()
	{
		double rand = rng.nextDouble();
		MorphemeEntry curr = null;
		int index = 0;
		
		while (rand > 0)
		{
			curr = library.get(index);
			rand -= curr.getProbability();
			index++;
		}
		
		return curr.getMorpheme();
	}
	
	public ArrayList<MorphemeEntry> getLibrary()
	{
		return library;
	}
	
	public int size()
	{
		return count;
	}
	
	public void printMembers()
	{
		int columns = 4;
		for (int j = 0; j < Math.ceil(library.size() * 1.0 / columns); j++)
		{
			for (int k = 0; k < columns; k++)
			{
				MorphemeEntry rootEntry = library.get(k * library.size() / columns + j);    				
				System.out.printf("%.4f\t%.3f\t", rootEntry.getProbability(), rootEntry.getMorpheme().getInformationContent());
				System.out.print(padString(rootEntry.getMorpheme().toString(), 24));
//				System.out.print(padString(root.getDefault(), 16) + padString(name.getIPA(), 24));
				
			}
			System.out.println();
		}
	}
	
	 public static String padString(String s, int len)
    {
    	StringBuilder result = new StringBuilder();
    	result.append(s);
    	
    	len -= s.length();
		while (len % 8 > 0)
		{
			result.append(" ");
			len--;
		}
		while (len > 0)
		{
			result.append("\t");
			len -= 8;
		}
		
		return result.toString();
    }
}