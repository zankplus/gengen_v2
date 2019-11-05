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

import java.awt.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import gengenv2.morphemes.ConsonantPhoneme;
import gengenv2.morphemes.Constituent;
import gengenv2.morphemes.VowelPhoneme;
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
	private Random rng = null;
	
	// Library information
	private ArrayList<Constituent> library;
	private int memberCount = 0;
	
	// Context variables
	private ConstituentType type;
	private ConstituentLocation location;
	
	// Static variables
	private static int count = 0;
	
	// Cluster variables
	private String name;
	private double clusterChance;	// having reached this library, the chance of picking an item from it
	private int maxClusterLength;
	private double[] entropy;
	private boolean[] isEntropySet;
	
	/**
	 * Constructor sets parent/RNG, default max length, and context variables, and initializes the library
	 * array list and information variables.
	 * @param parent		Reference to the Phonology to which this library belongs 
	 * @param maxLength		Length of longest cluster in library
	 * @param type			Type of Constituent housed in this library (onset/nucleus/coda)
	 * @param location		Location in which this library is invoked (initial/medial/final/root)
	 */
	public ConstituentLibrary (String name, int maxClusterLength, ConstituentType type, ConstituentLocation location)
	{
		this.name = name;
		this.maxClusterLength = maxClusterLength;
		this.location = location;
		this.type = type;
		
		if (rng == null)
			rng = PublicRandom.getRNG();
		
		library = new ArrayList<Constituent>();
		memberCount = 0;
		entropy = new double[maxClusterLength];
		isEntropySet = new boolean[maxClusterLength];
	}
	
	/**
	 * Removes any members with negative probability values. If all members have negative probability, the
	 * member with the highest probability is retained.
	 * @since	1.2 
	 */
	public void removeUnusedMembers(boolean allowEmptyList)
	{
		if (memberCount == 0)
			return;
		
		
		if (library.size() > 0)
		{
			Constituent max = library.get(0);
			for (int i = 0; i < library.size(); i++)
				if (library.get(i).getProbability() < 0)
				{
					if (library.get(i).getProbability() > max.getProbability())
						max = library.get(i);
					
					library.remove(i);
					memberCount--;
					i--;
				}
			
			// Never leave a library totally empty - always keep 1 in the first list
			if (!allowEmptyList && library.size() == 0)
			{
				max.setProbability(1);
				library.add(max);
				memberCount++;
			}
		}
	}
	
	public void removeUnusedMembers()
	{
		removeUnusedMembers(false);
	}

	/**
	 * Normalizes the probabilities of all library members of each length up to maxLength.
	 * @since	1.2
	 */
	public void normalizeAll()
	{
		double sum = 0;
		for (Constituent c : library) 
			sum += c.getProbability();
		for (Constituent c : library)
			c.setProbability(c.getProbability() / sum);
	}
	
	/**
	 * Sorts library members of each length from highest probability to lowest.
	 * @since	1.2 
	 */
	public void sortAll()
	{
		Collections.sort(library);
		Collections.reverse(library);
	}
	
	public Constituent get(int index)
	{
		return library.get(index);
	}
	
	/**
	 * @param length	Length of Constituents desired
	 * @return			An ArrayList of all Constituents of the given length
	 * @since			1.2
	 */
	public ArrayList<Constituent> getMembers()
	{
		return library;
	}
	
	/**
	 * Exaggerates the differences between probabilities by raising the probability of every Constituent to the
	 * specified power. Automatically normalizes probabilities afterward.
	 * @param 	power		The degree of exaggeration, i.e., the power to which to raise all probabilities
	 * @since	1.2
	 */
	public void exaggerate(double power)
	{
		for (int j = 0; j < size(); j++)
			library.get(j).setProbability(Math.pow(library.get(j).getProbability(), power));
		
		normalizeAll();
	}
	
//	/**
//	 * Searches for a Constituent with the same sequence as a given Constituent and returns it if found.
//	 * @param	other	The Constituent to be matched
//	 * @return	The matching Constituent if found, otherwise null
//	 * @since	1.2 
//	 */
//	public Constituent getMatchingConstituent(Constituent other)
//	{
//		for (Constituent c : getMembersOfLength(other.size()))
//			if (c.sameSequence(other))
//				return c;
//		return null;
//	}
	
	
	/**
	 * Adds a Constituent to the library and places it in the arraylist corresponding to its length.
	 * @param 	c	The Constituent to be added
	 * @since	1.2
	 */
	public void add(Constituent c)
	{
		System.out.println("Adding Constituent " + c + " to library " + toString() + " / " + c.getProbability());
		library.add(c);
		memberCount++;
	}
	
	/**
	 * Returns a random Constituent from the library.
	 * @return	A random Constituent from the library
	 * @since	1.2
	 */
	public ArrayList<Constituent> pick()
	{
		ArrayList<Constituent> result = new ArrayList<Constituent>();
		ConstituentLibrary lib = this;
		boolean done = false;
		
		while (result.size() < maxClusterLength && !done)
		{
			Constituent curr = lib.pickSingle(); 
			result.add(curr);
			ConstituentLibrary nextLib = curr.followers(type); 
			
			if (nextLib != null && rng.nextDouble() < nextLib.getClusterChance())
				lib = nextLib;
			else
				done = true;
			
		}
		
		return result;
	}
	
	/**
	 * Returns a random syllable Constituent from a list of Constituents
	 * 
	 * @param 	ArrayList		A list of Constituents from which to randomly select 
	 * @return	Constituent	A syllable Constituent from the given list 
	 * @since	1.0
	 */
	public Constituent pickSingle()
	{
		/*Generate a random number between 0 and 1 and subtract probability values in order (the  lists are sorted
		largest to smallest) until we reach a number lower than 0. The syllable segment whose probability value
		took us over the edge is returned.*/		
		try
		{
			double rand = rng.nextDouble();
			for (Constituent c : library)
			{
				if (rand < c.getProbability())
					return c;
				else
					rand -= c.getProbability();
			}
			throw new Exception();
		} 
		catch (Exception e) 
		{
			System.out.println("Failed to select syllable segment; were the inventory's prominence values not normalized?");
			System.out.println(toString());
			for (Constituent c : library)
				System.out.println(c + " " + c.getProbability());
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
	public void removeInvalidMembers()
	{
		for (int i = 0; i < library.size(); i++)
			if (!library.get(i).getContent().isValidInPosition(location, type))
			{
				library.remove(i);
				memberCount--;
				i--;
			}
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
		for (Constituent curr : library)
				result += curr.followers().size();
		return result;
	}

	public double getEntropy()
	{
		return getClusterEntropy(maxClusterLength);
	}
	
	/**
	 * @return	The first-order entropy measurement for this library
	 * @since	1.2
	 */
	public double getSelectionEntropy()
	{
		double h = 0;
		System.out.println("Calculating selection entropy for " + toString());
		for (Constituent c : library)
		{
			h += Entropy.partialEntropy(c.getProbability());
			System.out.println("\t" + c.toString() + ": " + Entropy.partialEntropy(c.getProbability()));
		}
		
		System.out.println("\t\tTotal entropy: " + h);
		
		return h;
	}
	
	// Entropy of a cluster beginning with a random selection from this library
	public void setClusterEntropy(int length)
	{
		double h = 0;
		
		if (length > 1)
		{
			double[] pFollowers = new double[size()];
			double[] hFollowers = new double[size()];
			
			for (int i = 0; i < library.size(); i++)
			{
				Constituent c = library.get(i);
				ConstituentLibrary next = null;
				
				if (type == ConstituentType.NUCLEUS)
					next = ((VowelPhoneme) c.getContent()).getNucleusFollowers();
				else if (type == ConstituentType.ONSET)
					next = ((ConsonantPhoneme) c.getContent()).getOnsetFollowers();
				else if (type == ConstituentType.CODA)
					next = ((ConsonantPhoneme) c.getContent()).getCodaPreceders();
				
				double pAddMore, pStopHere, hAddMore, hStopHere;
				
				pAddMore = next.clusterChance;
				hAddMore = next.getClusterEntropy(length - 1);
				pStopHere = 1 - pAddMore;
				hStopHere = pStopHere == 0 ? 0 : -Math.log(pStopHere);
//				System.out.println(this + " to " + c.getContent().segment.expression + ", length " + length + " / " + pAddMore + " " + hAddMore + " " + pStopHere + " " + hStopHere);
				pFollowers[i] = c.getProbability();
				hFollowers[i] = Entropy.decision(new double[] { pAddMore, pStopHere }, new double[] { hAddMore, hStopHere }); 
			}
			
			entropy[length - 1] = Entropy.decision(pFollowers, hFollowers);
			isEntropySet[length - 1] = true;
		}
		else if (length == 1)
		{
			entropy[length - 1] = getSelectionEntropy();
			isEntropySet[length - 1] = true;
		}
	}
	
	public double getClusterEntropy(int length)
	{
		if (!isEntropySet[length - 1])
			setClusterEntropy(length);
		return entropy[length - 1];
	}
	
	/**
	 * @return	The context in which this Constituent library is meant to be invoked (initial, medial, etc.)
	 * @since	1.2
	 */
	public ConstituentLocation getLocation()
	{
		return location;
	}
	
	public ConstituentType getType()
	{
		return type;
	}
	
	public String getName ()
	{
		return location  + " " + type; 
	}
	
	public void setClusterChance(double clusterChance)
	{
		this.clusterChance = clusterChance * Math.log(size() + 1);
	}
	
	public double getClusterChance()
	{
		return clusterChance;
	}
	
	public void printMembers()
	{
		System.out.println("Members of " + toString());
		for (int j = 0; j < library.size(); j++)
		{
			System.out.println(library.get(j) + "\t" + library.get(j).getProbability());
		}
	}
	
	public String toString()
	{
		return name + "/" + type + "/" + location + " [" + size() + "]";
	}
	
	public int getMaxClusterLength()
	{
		return maxClusterLength;
	}
}