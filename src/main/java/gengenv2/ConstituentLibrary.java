package gengenv2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import gengenv2.Phonology.Constituent;
import gengenv2.Phonology.Phoneme;

public class ConstituentLibrary
{
	private Phonology parent;
	private static Random rng = null;
	
	private int maxLength;
	private int memberCount = 0;
	private ArrayList<Constituent>[] library;
	private double[] lengthProbabilities;
	
	private ConstituentType type;
	private ConstituentLocation location;
	private double entropy;
	
	private int id;
	private static int count = 0;
	
	public ConstituentLibrary (Phonology parent, int maxLength, ConstituentType type, ConstituentLocation location)
	{
		this.parent = parent;
		this.maxLength = maxLength;
		this.location = location;
		this.type = type;
		id = count++;
		
		if (rng == null)
			rng = parent.rng;
		
		createEmptyLibrary();
	}
	
	public void createEmptyLibrary()
	{
		library = new ArrayList[maxLength];
		for (int i = 0; i < maxLength; i++)
			library[i] = new ArrayList<Constituent>();
		memberCount = 0;
		entropy = -1;
	}
	
	public void setLengthProbabilities(double prob)
	{
		// Set length probabilities.
		lengthProbabilities = new double[maxLength];
		double sum = 0;
		for (int i = 0; i < maxLength - 1; i++)
		{
			lengthProbabilities[i] = (1 - prob) * Math.pow(prob, i) * 
										Math.log(countMembersOfLength(i + 1) + 1);
			sum += lengthProbabilities[i];
		}
		if (maxLength > 0)
		{
			lengthProbabilities[maxLength - 1] = Math.pow(prob, maxLength - 1) * 
													Math.log(countMembersOfLength(maxLength) + 1);
			sum += lengthProbabilities[maxLength - 1];
		}
		
		// Normalize length probabilities
		for (int i = 0; i < maxLength; i++)
			lengthProbabilities[i] /= sum;
		
//		System.out.printf("%d: (%.3f)\t ", id, prob);
//		for (int i = 1; i <= maxLength; i++)
//			System.out.printf("[%d: %.3f - %d] ", i, lengthProbabilities[i - 1], countMembersOfLength(i));
//		System.out.println();
	}
	
	public void removeUnusedMembers()
	{
		if (memberCount == 0)
			return;
		
		for (int j = 0; j < maxLength; j++)
		{
			ArrayList<Constituent> list = library[j];
			if (list.size() > 0)
			{
				Constituent max = list.get(0);
				for (int i = 0; i < list.size(); i++)
					if (list.get(i).probability < 0)
					{
						if (list.get(i).probability > max.probability)
							max = list.get(i);
						
						list.remove(i);
						memberCount--;
						i--;
					}
				
				// Never leave a library totally empty - always keep 1 in the first list
				if (j == 0 && list.size() == 0)
				{
					max.probability = 1;
					list.add(max);
				}
			}
		}
		entropy = -1;
		shrinkMaxToFit();
	}
	
	private void shrinkMaxToFit()
	{
		if (maxLength > 0)
			for ( ; maxLength > 0 && library[maxLength - 1].size() == 0; maxLength--)
				library[maxLength - 1] = null;
	}
	
	public void normalizeAll()
	{
		for (int i = 0; i < maxLength; i++)
		{
			double sum = 0;
			for (Constituent c : library[i]) 
				sum += c.probability;
			for (Constituent c : library[i])
				c.probability /= sum;
		}
	}
	
	public void sortAll()
	{
		for (int i = 0; i < maxLength; i++)
		{
			Collections.sort(library[i]);
			Collections.reverse(library[i]);
		}
	}
	
	public ArrayList<Constituent> getMembersOfLength(int length)
	{
		try
		{
			return library[length - 1];
		} 
		catch (ArrayIndexOutOfBoundsException e)
		{
			System.err.println("Tried to access syllable constituents of length " + length + " in library " +
								id + " (max length " + maxLength+ ")");
			e.printStackTrace();
			System.exit(0);
			return null;
		}
	}
	
	public int countMembersOfLength(int length)
	{
		return getMembersOfLength(length).size();
	}
	
	public double getLengthProbability(int length)
	{
		try
		{
			return lengthProbabilities[length - 1];
		} 
		catch (ArrayIndexOutOfBoundsException e)
		{
			System.err.println("Tried to access cluster probability of length " + length + " in library " +
								id + " (max length " + maxLength + ")");
			e.printStackTrace();
			System.exit(0);
			return -1;
		}
	}
	
	public void exaggerate(double power)
	{
		for (int i = 1; i <= maxLength; i++)
		{
			for (int j = 0; j < countMembersOfLength(i); j++)
				getMembersOfLength(i).get(j).probability = Math.pow(getMembersOfLength(i).get(j).probability, power);
		}
		
		normalizeAll();
	}
	
	public void add(Constituent c)
	{
		library[c.content.length - 1].add(c);
		memberCount++;
	}
	
	public Constituent pick()
	{
		double rand = rng.nextDouble();
		int length = -1;
		
		for (int i = 0; i < maxLength; i++)
		{
			rand -= lengthProbabilities[i];
			if (rand <= 0)
			{
				length = i;
				break;
			}
		}
		
		return pickConstituent(length);
	}
	
	public Constituent pickSimple()
	{
		return pickConstituent(0);
	}
	
	public Constituent pickComplex()
	{
		double rand = rng.nextDouble() * (1 - lengthProbabilities[0]);
		int length = -1;
		
		for (int i = 1; i < maxLength; i++)
		{
			rand -= lengthProbabilities[i];
			if (rand <= 0)
			{
				length = i;
				break;
			}
		}
		
		return pickConstituent(length);
	}
	
	/**
	 * Returns a random syllable Constituent from a list of Constituents
	 * 
	 * @param 	ArrayList		A list of Constituents from which to randomly select 
	 * @return	Constituent	A syllable Constituent from the given list 
	 * @since	1.0
	 */
	private Constituent pickConstituent(int index)
	{
		ArrayList<Constituent> lib = library[index];
		
		 /*Generate a random number between 0 and 1 and subtract probability values in order (the  lists are sorted
		 largest to smallest) until we reach a number lower than 0. The syllable segment whose probability value
		 took us over the edge is returned.*/		
		try
		{
			double rand = rng.nextDouble();
			for (Constituent c : lib)
			{
				if (rand < c.probability)
					return c;
				else
					rand -= c.probability;
			}
			throw new Exception();
		} 
		catch (Exception e) 
		{
			System.err.println("Failed to select syllable segment; " +
								"were the inventory's prominence values not normalized?");
			for (Constituent c : lib)
				System.out.println(c + " " + c.probability);
			e.printStackTrace();
			System.exit(0);
		}
		
		return null;
	}
	
	public void pruneMembersWithoutFollowers()
	{
		for (int j = 0; j < maxLength; j++)
		{
			ArrayList<Constituent> list = library[j];
			for (int i = 0; i < list.size(); i++)
				if (list.get(i).followers().getMembersOfLength(1).size() == 0)
				{
					list.remove(i);
					memberCount--;
					i--;
				}
		}
		entropy = -1;
	}
	
	public void scaleProbabilityByFollowerCount()
	{
		for (int j = 0; j < maxLength; j++)
		{
			ArrayList<Constituent> list = library[j];
			for (int i = 0; i < list.size(); i++)
				list.get(i).probability *= Math.log(list.get(i).followers().countMembersOfLength(1) + 1);
		}
	}
	
	public int size()
	{
		return memberCount;
	}
	
	public int getCompoundSize()
	{
		int result = 0;
		for (int i = 1; i <= maxLength; i++)
			for (Constituent curr : getMembersOfLength(i))
				result += curr.followers().size();
		return result;
	}
	
	public int maxLength()
	{
		return maxLength;
	}
	
	private double getEntropy(boolean compoundEntropy)
	{
		if (entropy != -1)
			return entropy;
		
		entropy = 0;
		for (int i = 1; i <= maxLength; i++)
			for (Constituent c : getMembersOfLength(i))
			{
				double pCurr = c.probability * getLengthProbability(i);
				double p = pCurr;
				if (compoundEntropy)
				{
					for (int j = 1; j < c.followers().maxLength; j++)
						for (Constituent d : c.followers().getMembersOfLength(j))
						{
							double pNext = d.probability * c.followers().getLengthProbability(j);
							p = pCurr * pNext;
							if (p != 0)
								entropy += -p * Math.log(p);
						}
				}
				
				if (p != 0)
					entropy += -p * Math.log(p);
			}
		return entropy;
	}
	
	public double getEntropy()
	{
		return getEntropy(false);
	}
	
	public double getCompoundEntropy()
	{
		return getEntropy(true);
	}
	
	public ConstituentLocation getLocation()
	{
		return location;
	}
}
