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
	
	private int id;
	private static int count = 0;
	
	public ConstituentLibrary (Phonology parent, int maxLength, ArrayList<Constituent>[] library,
			double clusterProbability)
	{
		this.parent = parent;
		if (rng == null)
			rng = parent.rng;
		this.maxLength = maxLength;
		
		this.library = library;
		id = count++;
		
		if (library != null)
		{
			// Count members
			for (ArrayList<Constituent> list : library)
				memberCount += list.size();
			
			setLengthProbabilities(clusterProbability);
			
			normalizeAll();
			sortAll();
		}
	}
	
	public ConstituentLibrary (Phonology parent, int maxLength)
	{
		this.parent = parent;
		if (rng == null)
			rng = parent.rng;
		this.maxLength = maxLength;
		
		createEmptyLibrary();
		
		id = count++;
	}
	
	public void createEmptyLibrary()
	{
		library = new ArrayList[maxLength];
		for (int i = 0; i < maxLength; i++)
			library[i] = new ArrayList<Constituent>();
		memberCount = 0;
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
		for (ArrayList<Constituent> list : library)
			for (int i = 0; i < list.size(); i++)
				if (list.get(i).probability < 0)
				{
					list.remove(i);
					memberCount--;
					i--;
				}
		
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
				if (list.get(i).lastPhoneme().followers.getMembersOfLength(1).size() == 0)
				{
					list.remove(i);
					memberCount--;
					i--;
				}
		}
	}
	
	public void scaleProbabilityByFollowerCount()
	{
		for (int j = 0; j < maxLength; j++)
		{
			ArrayList<Constituent> list = library[j];
			for (int i = 0; i < list.size(); i++)
				list.get(i).probability *= Math.log(list.get(i).lastPhoneme().followers.countMembersOfLength(1) + 1);
		}
	}
	
	public int size()
	{
		return memberCount;
	}
	
	public int maxLength()
	{
		return maxLength;
	}
}
