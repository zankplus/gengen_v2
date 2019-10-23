/** Copyright 2018, 2019 Clayton Cooper
 *	
 *	This file is part of gengen2.
 *
 *	gengen2 is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	gengen2 is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with gengen2.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package gengenv2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import gengenv2.morphemes.Constituent;
import gengenv2.enums.ConstituentType;
import gengenv2.enums.ConstituentLocation;

/**
 * A compilation of all the syllable Constituents that might occur in a certain context, grouped according to
 * length and intended be drawn from at random. 
 * 
 * @author 	Clayton Cooper
 * @version	1.2
 * @since	1.2
 */
public class ConstituentLibrary
{
	// General variables
	private Phonology parent;
	private static Random rng = null;
	
	// Library information
	private ArrayList<Constituent>[] library;
	private double[] lengthProbabilities;
	private int maxLength;
	private int memberCount = 0;
	private double entropy;
	
	// Context variables
	private ConstituentType type;
	private ConstituentLocation location;
	
	// Static variables
	private static int count = 0;
	
	/**
	 * Constructor sets parent/RNG, default max length, and context variables, and initializes the library
	 * array list and information variables.
	 * @param parent		Reference to the Phonology to which this library belongs 
	 * @param maxLength		Length of longest cluster in library
	 * @param type			Type of Constituent housed in this library (onset/nucleus/coda)
	 * @param location		Location in which this library is invoked (initial/medial/final/root)
	 */
	public ConstituentLibrary (int maxLength, ConstituentType type, ConstituentLocation location)
	{
		this.parent = parent;
		this.maxLength = maxLength;
		this.location = location;
		this.type = type;
		
		if (rng == null)
			rng = parent.rng;
		
		library = new ArrayList[maxLength];
		for (int i = 0; i < maxLength; i++)
			library[i] = new ArrayList<Constituent>();
		memberCount = 0;
		entropy = -1;
	}
	
	/**
	 * Calculates the selection probability for each length of Constituent (i.e., the chance of choosing a length-1
	 * Constituent, of choosing a length-2 one, etc.) in this library. Probability is based on the number of
	 * members of a given length, and a factor reducing probability geometrically as length increases.
	 * @param coefficient	Length scaling factor
	 */
	public void setLengthProbabilities(double coefficient)
	{
		// Set length probabilities.
		lengthProbabilities = new double[maxLength];
		double sum = 0;
		for (int i = 0; i < maxLength - 1; i++)
		{
			lengthProbabilities[i] = (1 - coefficient) * Math.pow(coefficient, i) * 
										Math.log(countMembersOfLength(i + 1) + 1);
			sum += lengthProbabilities[i];
		}
		if (maxLength > 0)
		{
			lengthProbabilities[maxLength - 1] = Math.pow(coefficient, maxLength - 1) * 
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
	
	/**
	 * Removes any members with negative probability values. If all members have negative probability, the
	 * member with the highest probability is retained.
	 * @since	1.2 
	 */
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
	
	/**
	 * Reduces the maxLength to the length of the longest Constituent(s) available in the library.
	 * This prevents other classes from accidentally accessing empty arrays in library and lets us keep
	 * more accurate information about the content of a library.
	 * @since	1.2
	 */
	private void shrinkMaxToFit()
	{
		if (maxLength > 0)
			for ( ; maxLength > 0 && library[maxLength - 1].size() == 0; maxLength--)
				library[maxLength - 1] = null;
	}
	
	/**
	 * Normalizes the probabilities of all library members of each length up to maxLength.
	 * @since	1.2
	 */
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
	
	/**
	 * Sorts library members of each length from highest probability to lowest.
	 * @since	1.2 
	 */
	public void sortAll()
	{
		for (int i = 0; i < maxLength; i++)
		{
			Collections.sort(library[i]);
			Collections.reverse(library[i]);
		}
	}
	
	/**
	 * @param length	Length of Constituents desired
	 * @return			An ArrayList of all Constituents of the given length
	 * @since			1.2
	 */
	public ArrayList<Constituent> getMembersOfLength(int length)
	{
		return library[length - 1];
	}
	
	/**
	 * @param length	Length of Constituents queried
	 * @return			The count of all Constituents of the given length
	 * @since			1.2
	 */
	public int countMembersOfLength(int length)
	{
		return getMembersOfLength(length).size();
	}
	
	/**
	 * Returns the probability of picking a Constituent of the given length from a random call to pick().
	 * @param 	length	Length of Constituent queried
	 * @return	The chance of picking a Constituent of the given length 
	 * @since	1.2
	 */
	public double getLengthProbability(int length)
	{
		return lengthProbabilities[length - 1];
	}
	
	/**
	 * Exaggerates the differences between probabilities by raising the probability of every Constituent to the
	 * specified power. Automatically normalizes probabilities afterward.
	 * @param 	power		The degree of exaggeration, i.e., the power to which to raise all probabilities
	 * @since	1.2
	 */
	public void exaggerate(double power)
	{
		for (int i = 1; i <= maxLength; i++)
		{
			for (int j = 0; j < countMembersOfLength(i); j++)
				getMembersOfLength(i).get(j).probability = Math.pow(getMembersOfLength(i).get(j).probability, power);
		}
		
		normalizeAll();
	}
	
	/**
	 * Searches for a Constituent with the same sequence as a given Constituent and returns it if found.
	 * @param	other	The Constituent to be matched
	 * @return	The matching Constituent if found, otherwise null
	 * @since	1.2 
	 */
	public Constituent getMatchingConstituent(Constituent other)
	{
		if (other.size() > maxLength)
			return null;
		
		for (Constituent c : getMembersOfLength(other.size()))
			if (c.sameSequence(other))
				return c;
		return null;
	}
	
	/**
	 * Returns true if the given Constituent is present in this library
	 * @param	other	The Constituent to be found
	 * @return	True if the Constituent is somewhere in this library, otherwise false
	 * @since	1.2 
	 */
	
	/**
	 * Adds a Constituent to the library and places it in the arraylist corresponding to its length.
	 * @param 	c	The Constituent to be added
	 * @since	1.2
	 */
	public void add(Constituent c)
	{
		library[c.content.length - 1].add(c);
		memberCount++;
	}
	
	/**
	 * Returns a random Constituent from the library.
	 * @return	A random Constituent from the library
	 * @since	1.2
	 */
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
	
	/**
	 * Returns a random Constituent of length 1 from the library.
	 * @return	A Constituent of length 1 from the library
	 * @since	1.2
	 */
	public Constituent pickSimple()
	{
		return pickConstituent(0);
	}
	
	/**
	 * Returns	a random Constituent of length 2 or more from the library.
	 * @return	A Constituent of length 2 or more from the library
	 * @since	1.2
	 */
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
	
	/**
	 * Removes any Constituents from the library whose own follower libraries are empty.
	 * TODO: Should this remove members with null follower libraries too?
	 * @since	1.2
	 */
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
	
	/**
	 * Multiplies the probability of each Constituent in the library by the log of its follower account, making
	 * members with more followers more prevalent. Automatically normalizes probabilities after.
	 * @since	1.2
	 */
	public void scaleProbabilityByFollowerCount()
	{
		for (int j = 0; j < maxLength; j++)
		{
			ArrayList<Constituent> list = library[j];
			for (int i = 0; i < list.size(); i++)
				list.get(i).probability *= Math.log(list.get(i).followers().countMembersOfLength(1) + 1);
		}
		
		normalizeAll();
	}
	
	/**
	 * @return	The number of Constituents in this library
	 * @since	1.2
	 */
	public int size()
	{
		return memberCount;
	}
	
	/**
	 * Returns the sum of the number of followers for each member of the library 
	 * @return	The number of compounds that can be made between libraries and their followers
	 * @since	1.2
	 */
	public int getCompoundSize()
	{
		int result = 0;
		for (int i = 1; i <= maxLength; i++)
			for (Constituent curr : getMembersOfLength(i))
				result += curr.followers().size();
		return result;
	}
	
	/**
	 * @return	The maximum length of any Constituent in this library
	 * @since	1.2
	 */
	public int maxLength()
	{
		return maxLength;
	}
	
	/**
	 * Calculates the first-order entropy for this library's pick() function. Optionally, it can instead calculate
	 * the second-order entropy, i.e., the entropy of picking a random Constituent from this library and then 
	 * picking one of its followers. The value is saved after first calculation, so future calls simply return the 
	 * stored entropy measurement.
	 * 
	 * @param 	compoundEntropy	Determines whether to return first- or second-order entropy
	 * @return	The specified entropy measurement
	 * @since	1.2
	 */
	private double getEntropy(boolean compoundEntropy)
	{
		if (entropy != -1 && !compoundEntropy)
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
	
	/**
	 * @return	The first-order entropy measurement for this library
	 * @since	1.2
	 */
	public double getEntropy()
	{
		return getEntropy(false);
	}
	
	/**
	 * @return	The second-order entropy measurement for this library
	 * @since	1.2
	 */
	public double getCompoundEntropy()
	{
		return getEntropy(true);
	}
	
	/**
	 * @return	The context in which this Constituent library is meant to be invoked (initial, medial, etc.)
	 * @since	1.2
	 */
	public ConstituentLocation getLocation()
	{
		return location;
	}
}