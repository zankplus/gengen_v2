package Gengen_v2.gengenv2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;

public class Phonology
{
	// General properties
	double[] prominence;	// prominence of each phonetic property in the language
	double[] codaProminence;
	
	// Phonetic properties
	Phoneme[] inv;			// language's phoneme inventory
	
	// Phonotactic properties
	int maxOnsetCluster, maxCodaCluster, clusterBonus;
	private boolean[] ptCatsRepresented;
	boolean[][] validTransitions;
	boolean[][] validCodaTransitions;
	double[] aggregance;	// tendency of each phonetic property to form clusters
	
	Phoneme toVowels;	// abstract phoneme representing average transition to available vowels
						// any phoneme without vowel transitions available int he onset can
						// use this instead
	
	// Generator properties
	static double phonemeProminenceStdev = 0.3;
	static double phonemeAggreganceStdev = 0.2;
	
	public Phonology()
	{
		determineProminence();
		selectSegments();
		makeBasicSyllableStructure();
		makePhonotactics();
//		makeCodaTactics();
	}
	
	// Determine prominence values for each phonetic property.
	// Properties with 0 prominence will not feature in this phonology
	public void determineProminence()
	{
		Random rng = new Random();
		prominence = new double[SegProp.values().length];
		aggregance = new double[SegProp.values().length];
		
		// Determine prominence value
		// Properties with 0 prominence will not feature in this phonology
		for (int i = 0; i < SegProp.values().length; i++)
			if (Math.random() < SegProp.values()[i].probability)	// properties failing this check receive 0 prominence
				prominence[i] = Math.max(rng.nextGaussian() * phonemeProminenceStdev + 1, 0.001);

		// ensure the language has vowels
		if (prominence[SegProp.SHORT.ordinal()] == 0)
				prominence[SegProp.LONG.ordinal()] = 1;
		
		// Determine aggregance values for consonant categories
		for (int i = 0; i < SegProp.values().length; i++)
			if (prominence[i] > 0)
				aggregance[i] = Math.max(rng.nextGaussian() * phonemeAggreganceStdev + 1, 0.001);
		
		// Ensure vowel clusters are less common
		prominence[SegProp.END.ordinal()] = Math.max(prominence[SegProp.VOWEL.ordinal()] + 1,
								 					 prominence[SegProp.END.ordinal()  ]);
		

	}
	
	// 
	public void selectSegments()
	{
		// Make easy reference to segment arrays
		Segment[] segs = Segment.segments;
				
		ArrayList<Phoneme> inv = new ArrayList<Phoneme>();
		
		// Populate consonant inventory
		for (int i = 0; i < segs.length; i++)
		{
			boolean add = true;
			
			for (int j = 0; j < segs[i].properties.length; j++)
				if (prominence[segs[i].properties[j].ordinal()] == 0)
				{
					add = false;
					j = segs[i].properties.length;
				}
			
			if (add)
				inv.add(new Phoneme(segs[i]));
		}
		
		this.inv = inv.toArray(new Phoneme[0]);
		
		// Mark categories represented by this language's inventory
		ptCatsRepresented = new boolean[Cluster.ptCats.size()];
		ptCatsRepresented[0] = true;
		ptCatsRepresented[ptCatsRepresented.length - 1] = true;
		
		for (Phoneme p : inv)
			ptCatsRepresented[p.segment.transitionCategory] = true;
	}
	
	public void makeBasicSyllableStructure()
	{
		Random rng = new Random();
		
		// Roll for onset
		int roll = rng.nextInt(12);
		if (roll < 3)			
			maxOnsetCluster = 1;	// 3/12
		else if (roll < 9)
			maxOnsetCluster = 2;	// 6/12
		else if (roll < 11)
			maxOnsetCluster = 3;	// 2/12
		else
			maxOnsetCluster = 4;	// 1/12
		
		// Roll for coda
		roll = rng.nextInt(8);
		if (roll < 1)
			maxCodaCluster = maxOnsetCluster + 1;	// 1/8
		else if (roll < 4)
			maxCodaCluster = maxOnsetCluster;		// 3/8
		else if (roll < 7)
			maxCodaCluster = maxOnsetCluster - 1;	// 3/8
		else
			maxCodaCluster = maxOnsetCluster - 2;	// 1/8
		
		maxCodaCluster = Math.max(maxCodaCluster, 0);
		
		// Cluster bonus = sum of values of cluster maxima in excess of 1
		clusterBonus = 0;
		if (maxOnsetCluster > 1)
			clusterBonus += maxOnsetCluster - 1;
		if (maxCodaCluster > 1)
			clusterBonus += maxCodaCluster - 1;
	}

	// Here's the basic strategy for phonotactics:
	// 1. determine which transitions between phonotactic categories are valid; the results
	// are stored in  validTransitions.
	// 2. 
	public void makePhonotactics()
	{
		Random rng = new Random();
		
		// Determine which transitions between categories are valid in onsets
		int[][] transProb = Cluster.transitionProbability;
		validTransitions = new boolean[transProb.length][transProb[0].length];
		for (int i = 0; i < transProb.length; i++)
			if (ptCatsRepresented[i])
				for (int j = 0; j < transProb[0].length; j++)
					if (ptCatsRepresented[j] && transProb[i][j] > 0)
					{
						// Chance of representation = 9 x 10^(commonness - 4)
						double p = 9 * Math.pow(10, transProb[i][j] - 4);
						
						// Cluster bonus applies only to interconsonant transitions.
						// Mainly, this prevents consonant clusters from forming in
						// language whose prescribed structures do not allow them.
						if (j > 0 && j < 17 && i != 0)
							p *= clusterBonus;
						
						// No transition has more than a 90% chance of inclusion
						
						p = Math.min(p, 0.9);
						
						if (rng.nextDouble() < p)
							validTransitions[i][j] = true; 
					}
		
		// 2. Determine frequency of transitions between different phonetic categories
		double[][] phCats = new double[SegProp.values().length][SegProp.values().length];
		
		for (int i = 0; i < phCats.length; i++)
			for (int j = 0; j < phCats.length; j++)
				phCats[i][j] = prominence[i] * aggregance[j];
		
		// 3. For each phoneme in this language's inventory, we want to create a list of
		// possible phonemes that might follow it in the onset. We start by using the
		// validTransitions array to determine which categories of segments are
		// phonotactically valid. then we use the phonetic transitions table we just made
		// to determine how frequently each transition occurs
		
		// if this is a nonvowel with no possible followers, give it a generic
		// list of vowels to follow instead
		
		ArrayList<Phoneme> possibleFollowers = new ArrayList<Phoneme>();
		
		toVowels = new Phoneme(null);
		
		for (Phoneme ph : inv)
			if (ph.segment.isVowel())
				possibleFollowers.add(ph);
		
		double[] prob = new double[possibleFollowers.size()];
		
		for (int i = 0; i < prob.length; i++)
		{
			prob[i] = 1;
			for (SegProp sp : possibleFollowers.get(i).segment.properties)
				prob[i] *= prominence[sp.ordinal()];
		}
		
		toVowels.makeOnsetRules(possibleFollowers.toArray(new Phoneme[0]), prob);
		
		
		for (Phoneme p : inv)
		{
			int ptCat = p.segment.transitionCategory;
			possibleFollowers = new ArrayList<Phoneme>();
			
			// figure out which sounds may follow phonotactically.
			// for each phonotactic category to which the current phoneme might transition
			for (int i = 0; i < validTransitions[ptCat].length; i++)
				if (validTransitions[ptCat][i])
				{
					// consider every member segment
					for (int nextSound : Cluster.ptCats.get(i))
						
						// if this language has that segment, then add it to the list
						for (int j = 0; j < inv.length; j++)
							if (inv[j].segment.id == nextSound)
							{
								possibleFollowers.add(inv[j]);
								j = inv.length;
							}
				}
			
			prob = new double[possibleFollowers.size()];
			
			// now that we have a list of all the sounds that CAN follow, given our 
			// determine the probability of each transition
			for (int j = 0; j < possibleFollowers.size(); j++)
			{
				Phoneme next = possibleFollowers.get(j);
				prob[j] = 1;
				
//				for (SegProp sp1 : p.segment.properties)
//					for (SegProp sp2 : next.segment.properties)
//						prob[j] *= phCats[sp1.ordinal()][sp2.ordinal()];
				
				for (SegProp sp1 : p.segment.properties)
					prob[j] *= aggregance[sp1.ordinal()];
					
				for (SegProp sp2 : next.segment.properties)
					prob[j] *= prominence[sp2.ordinal()];
					
				
			}

			// Vowels can always transition to end
			if (possibleFollowers.size() == 0)
			{
				if ((p.segment.properties[0] == SegProp.VOWEL))
					p.makeOnsetRules(new Phoneme[] {inv[inv.length - 1]}, new double[]{1});
				else
					p.onsetTransitions = toVowels.onsetTransitions;
			}		
			else
				p.makeOnsetRules(possibleFollowers.toArray(new Phoneme[0]), prob);
		}
	}
	
	public void makeCodaTactics()
	{
		Random rng = new Random();
		
		codaProminence = new double[SegProp.values().length];
		
		// Determine prominence value
		for (int i = 0; i < SegProp.values().length; i++)
			if (prominence[i] > 0)
				codaProminence[i] = Math.max(rng.nextGaussian() * phonemeProminenceStdev * 2 + 1, 0.001);
		
		// Determine which transitions between categories are valid in onsets
		int[][] transProb = Cluster.codaTransitionProbability;
		validCodaTransitions = new boolean[transProb.length][transProb[0].length];
		for (int i = 0; i < transProb.length; i++)
			if (ptCatsRepresented[i])
				for (int j = 0; j < transProb[0].length; j++)
					if (ptCatsRepresented[j] && transProb[i][j] > 0)
					{
						double p = 6 * Math.pow(10, transProb[i][j] - 4);
						
						// cluster bonus only applies to consonants
						if (i < 17)
							p *= Math.min(clusterBonus, 1);

						p = Math.min(p, 0.6);
							
						double temp = rng.nextDouble();
						if (temp < p)
						{
							validCodaTransitions[i][j] = true;
							System.out.println(PTCat.values()[i] + "-->" + PTCat.values()[j]);
						}
					}
		
		
		double[][] phCats = new double[SegProp.values().length][SegProp.values().length];
		
		for (int i = 0; i < phCats.length; i++)
			for (int j = 0; j < phCats.length; j++)
				phCats[i][j] = codaProminence[i] * aggregance[j];
		
		// Print possible transitions
		for (Phoneme p : inv)
		{
			int ptCat = p.segment.transitionCategory;
			ArrayList<Phoneme> possibleFollowers = new ArrayList<Phoneme>();
			
			System.out.print(p.segment.expression + ": ");
			
			// figure out which sounds may follow phonotactically.
			// for each phonotactic category to which the current phoneme might transition
			for (int i = 0; i < validCodaTransitions[ptCat].length; i++)
				if (validCodaTransitions[ptCat][i])
				{
					// consider every member segment
					for (int nextSound : Cluster.ptCats.get(i))
						
						// if this language has that segment, then add it to the list
						for (int j = 0; j < inv.length; j++)
							if (inv[j].segment.id == nextSound)
							{
								possibleFollowers.add(inv[j]);
								j = inv.length;
							}
				}
			
			double[] prob = new double[possibleFollowers.size()];
			
			// determine the probability of each transition
			for (int j = 0; j < possibleFollowers.size(); j++)
			{
				Phoneme next = possibleFollowers.get(j);
				prob[j] = 1;
				
				for (SegProp sp1 : p.segment.properties)
					prob[j] *= aggregance[sp1.ordinal()];
					
				for (SegProp sp2 : next.segment.properties)
					prob[j] *= prominence[sp2.ordinal()];
				
				System.out.print(possibleFollowers.get(j).segment.expression + " " + prob[j] + "\t");
			}
			
			// If nothing else, the end can follow
			if (possibleFollowers.size() == 0)
				p.makeCodaRules(new Phoneme[] {inv[inv.length - 1]}, new double[]{1});
			else
				p.makeCodaRules(possibleFollowers.toArray(new Phoneme[0]), prob);
			
			System.out.println();
		}
	}

	public ArrayList<Phoneme> makeOnset()
	{
		ArrayList<Phoneme> result = new ArrayList<Phoneme>();
		Phoneme curr = inv[0];
		
		int consonants = 0;
		
		try
		{
			while (curr.segment.id != 52)	// end character
			{
				if (curr.segment.id != 0)
				{
					result.add(curr);
					if (curr.segment.isConsonant())
						consonants++;
				}
				
				if (curr.segment.isConsonant() && consonants >= maxOnsetCluster)
					curr = curr.forceVowel();
				else
					curr = curr.nextOnsetPhoneme();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println(toString(result));
			System.exit(0);
		}
			
		
		
		
		return result;
	}
	
	public ArrayList<Phoneme> makeSyllable()
	{
		ArrayList<Phoneme> result = makeOnset();
		int codaLength = 0;
		
		Phoneme curr = result.get(result.size() - 1).nextCodaPhoneme();
		while (curr.segment.id != 52)	// end character
		{
			if (curr.segment.id != 0)
			{
				result.add(curr);
				codaLength++;
			}
			
			if (codaLength < maxCodaCluster)
				curr = curr.nextCodaPhoneme();
			else
				return result;
		}
		
		return result;
	}
	
	public void printProminence()
	{
		System.out.println("PROPERTY PROMINENCE/AGGREGANCE");
		System.out.println("onset\tcoda\taggreg.\tname");
		for (int i = 0; i < SegProp.values().length; i++)	
			if (prominence[i] > 0)
			{
				System.out.printf("%.3f\t", prominence[i]);
//				if (codaProminence[i] > 0)
//					System.out.printf("%.3f\t", codaProminence[i]);	
//				System.out.printf("%.3f\t", aggregance[i]);
				
				System.out.println(SegProp.values()[i].toString().toLowerCase());
				
			}
		
		
		System.out.println();
	}
	
	public void printInventory()
	{
		System.out.println("CONSONANTS");
		
		Phoneme prev = null;
		for (Phoneme p : inv)
		{
			if (prev != null && prev.segment.properties[0] != p.segment.properties[0])
			{
				System.out.println();
				if (p.segment.properties[0] == SegProp.VOWEL)
					System.out.println("VOWELS");
				
			}
			
			System.out.print(p.segment.expression + " ");
			if (p.segment.expression.length() == 1)
				System.out.print(" ");
			
			prev = p; 
		}
	}
	
	public void printInventoryWithProminence()
	{
		// Sortable pairing of phonemes with the combined prominence of their properties
		class Pair implements Comparable<Pair> 
		{
			Phoneme p;
			double prom;
			public Pair(Phoneme p, double prom) { this.p = p; this.prom = prom; }
			
			public int compareTo(Pair p) {
				if (prom > p.prom)
					return 1;
				else if (prom < p.prom)
					return -1;
				return 0;
			}
		}
		
		ArrayList<Pair> consonants = new ArrayList<Pair>();
		ArrayList<Pair> vowels = new ArrayList<Pair>();
		
		double cTotal = 0, vTotal = 0;	// total prominence for consonant and vowel phonemes
		
		// Calculate combined prominence and sort each phoneme into consonant and vowel lists
		// Also find the total prominence for each list so we can normalize the values
		for (Phoneme p : inv)
		{
			// Calculating combined prominence
			double phonemeProminence = 1;
			for (SegProp s : p.segment.properties)
				phonemeProminence *= prominence[s.ordinal()];
			
			Pair pair = new Pair(p, phonemeProminence);
			
			if (p.segment.properties[0] == SegProp.VOWEL)
			{
				vowels.add(pair);
				vTotal += phonemeProminence;
			}
			else
			{
				consonants.add(pair);
				cTotal += phonemeProminence;
			}
		}
		
		// Normalize values
		for (Pair p : vowels)
			p.prom /= vTotal;
		for (Pair p : consonants)
			p.prom /= cTotal;
		
		// Sort lists
		Collections.sort(consonants);
		Collections.reverse(consonants);
		Collections.sort(vowels);
		Collections.reverse(vowels);
		
		// Print sorted lists
		System.out.println("CONSONANTS");
		for (Pair p : consonants)
		{
			System.out.printf("%s\t%.3f\t", p.p.segment.expression, p.prom);
			
			Iterator<Entry<Phoneme, Integer>> itr = p.p.onsetTransitions.entrySet().iterator();
			Entry<Phoneme, Integer> curr;
			int totalConsonants = 0;
			
	//		System.out.print(segment.expression + "->" + rand + "\t");
			while (itr.hasNext())
			{
				curr = itr.next();
				
				if (curr.getKey().segment.isConsonant())
				{
					System.out.printf("%s %.3f\t", p.p.segment.expression + curr.getKey().segment.expression, 
													1.0 * curr.getValue() / p.p.onsetTransitions.total);
					totalConsonants += curr.getValue();
				}
			}
			
			if (totalConsonants != 0)
				System.out.printf("%s %.3f", "no cluster", 1 - (1.0 * totalConsonants / p.p.onsetTransitions.total));
			System.out.println();
		}
		
		System.out.println("\nVOWELS");
		for (Pair p : vowels)
			System.out.printf("%s\t%.3f\n", p.p.segment.expression, p.prom);
	}
	
	public void printClusteringRules()
	{
		// print syllable structure
		for (int i = 0; i < maxOnsetCluster; i++)
			System.out.print("C");
		System.out.print("V");
		for (int i = 0; i < maxCodaCluster; i++)
			System.out.print("C");
		System.out.println(" (" + clusterBonus + ")\n");
		
		// print transitions
		int[][] transProb = Cluster.transitionProbability;
		
		for (int i = 0; i < transProb.length; i++)
			if (ptCatsRepresented[i])
			{
				System.out.print(PTCat.values()[i] + ":\t");
				for (int j = i; j < transProb[0].length; j++)
					if (validTransitions[i][j])
					{
						System.out.print(PTCat.values()[j].name().toLowerCase());
						
						if (transProb[i][j] < 3)
							System.out.print("!");
						if (transProb[i][j] < 2)
							System.out.print("!!");
						if (transProb[i][j] < 1)
							System.out.print("!!!");
						
						System.out.print("\t");
					}
				System.out.println();
			}
	
		
		// print valid phonotactic category transitions
		for (int i = 0; i < validTransitions.length; i++)
		{
			for (int j = 0; j < validTransitions[0].length; j++)
				if (validTransitions[i][j])
					System.out.print("1" + " ");
				else
					System.out.print("0" + " ");
			
			System.out.println();
		}
		System.out.println();
		
		// print valid coda phonotactic transitions
		for (int i = 0; i < validCodaTransitions.length; i++)
		{
			for (int j = 0; j < validCodaTransitions[0].length; j++)
				if (validCodaTransitions[i][j])
					System.out.print("1" + " ");
				else
					System.out.print("0" + " ");
			
			System.out.println();
		}
		System.out.println();
		
		// print individual transitions
		for (int i = 0; i < inv.length; i += 6)
		{		
			int longest = 0;
			Iterator<Entry<Phoneme, Integer>>[] itrs = new Iterator[6];
			for (int j = 0; j < 6 && i + j < inv.length; j++)
			{
				System.out.print(inv[i+j].segment.expression + ":\t\t\t");
				if (inv[i+j].onsetTransitions.size() > longest)
					longest = inv[i+j].onsetTransitions.size();
				
				itrs[j] = inv[i+j].onsetTransitions.entrySet().iterator(); 
			}
			System.out.println();
			
			for (int k = 0; k < longest; k++)
			{
				for (int j = 0; j < 6 && i + j < inv.length; j++)
				{
					if (itrs[j].hasNext())
					{
						Entry<Phoneme, Integer> next = itrs[j].next();
						System.out.print("  " + next.getKey().segment.expression + "\t" + 
											next.getValue() + "\t\t");
					}
					else
						System.out.print("\t\t\t");
				}
				System.out.println();
				
			}
			System.out.println();
		}
		
		System.out.println("\n\t~~~~~~~~~~~~~~~~~~~~~\n");
		
		// coda side now
		for (int i = 0; i < inv.length; i += 6)
		{		
			int longest = 0;
			Iterator<Entry<Phoneme, Integer>>[] itrs = new Iterator[6];
			for (int j = 0; j < 6 && i + j < inv.length; j++)
			{
				System.out.print(inv[i+j].segment.expression + ":\t\t\t");
				if (inv[i+j].codaTransitions.size() > longest)
					longest = inv[i+j].codaTransitions.size();
				
				itrs[j] = inv[i+j].codaTransitions.entrySet().iterator(); 
			}
			System.out.println();
			
			for (int k = 0; k < longest; k++)
			{
				for (int j = 0; j < 6 && i + j < inv.length; j++)
				{
					if (itrs[j].hasNext())
					{
						Entry<Phoneme, Integer> next = itrs[j].next();
						System.out.print("  " + next.getKey().segment.expression + "\t" + 
											next.getValue() + "\t\t");
					}
					else
						System.out.print("\t\t\t");
				}
				System.out.println();
				
			}
			System.out.println();
		}
	}
	
	public String toString(ArrayList<Phoneme> phrase)
	{
		StringBuilder result = new StringBuilder();
		
		for (Phoneme p : phrase)
			if (p.segment.id != 0)
				result.append(p.segment.expression);
		
		return result.toString();
	}

	class Phoneme
	{
		Segment segment;
		CountingHashtable<Phoneme, Integer> onsetTransitions;
		CountingHashtable<Phoneme, Integer> codaTransitions;
		
		public Phoneme(Segment segment)
		{
			this.segment = segment;
			onsetTransitions = new CountingHashtable<Phoneme, Integer>();
			codaTransitions  = new CountingHashtable<Phoneme, Integer>();
		}
	
		public void makeOnsetRules(Phoneme[] next, double[] prob)
		{
			double total = 0;
			for (int i = 0; i < prob.length; i++)
				total += prob[i];
			
			for (int i = 0; i < next.length; i++)
			{
				// Represent probabilities as integers / 100
				int flatProb = (int) (1000 * prob[i] / total);
				if (flatProb > 0)
				{
					onsetTransitions.put(next[i], flatProb);
					onsetTransitions.total += flatProb;
				}
				
			}
		}
		
		public void makeCodaRules(Phoneme[] next, double[] prob)
		{
			double total = 0;
			for (int i = 0; i < prob.length; i++)
				total += prob[i];
			
			for (int i = 0; i < next.length; i++)
			{
				// Represent probabilities as integers / 100
				int flatProb = (int) (1000 * prob[i] / total);
				if (flatProb > 0)
				{
					codaTransitions.put(next[i], flatProb);
					codaTransitions.total += flatProb;
				}
				
			}
		}
		
		public Phoneme nextOnsetPhoneme()
		{
			int rand = (int) (Math.random() * onsetTransitions.total) + 1;
			
			Iterator<Entry<Phoneme, Integer>> itr = onsetTransitions.entrySet().iterator();
			Entry<Phoneme, Integer> curr = itr.next();
	//		System.out.print(segment.expression + "->" + rand + "\t");
			while (true)
			{
				rand -= curr.getValue();
				if (rand <= 0)
					return curr.getKey();
				else
					curr = itr.next();
			}
		}
		
		public Phoneme nextCodaPhoneme()
		{
			int rand = (int) (Math.random() * codaTransitions.total) + 1;
			
			Iterator<Entry<Phoneme, Integer>> itr = codaTransitions.entrySet().iterator();
			Entry<Phoneme, Integer> curr = null;
	//		System.out.print(segment.expression + "->" + rand + " ");
			try
			{
				curr = itr.next();
			}
			catch (Exception e)
			{
				System.out.println("Dead end at phoneme " + segment.expression);
				System.exit(0);
			}
			
			
			while (true)
			{
				rand -= curr.getValue();
				if (rand <= 0)
					return curr.getKey();
				else
					curr = itr.next();
			}
		}
		
		public Phoneme forceVowel()
		{
			int total = 0;
			
			Iterator<Entry<Phoneme, Integer>> itr = onsetTransitions.entrySet().iterator();
			Entry<Phoneme, Integer> curr = null;
			
			// Count total vowel 
			while (itr.hasNext())
			{
				curr = itr.next();
				if (curr.getKey().segment.isVowel())
					total += curr.getValue();
			}
			
			int rand = (int) (Math.random() * total) + 1;
			
			if (total == 0)
				itr = toVowels.onsetTransitions.entrySet().iterator();
			else
				itr = onsetTransitions.entrySet().iterator();
			curr = itr.next();
			
			int count = 0;
			while (true)
			{
				count++;
				if (curr.getKey().segment.isVowel())
				{
					rand -= curr.getValue();
					if (rand <= 0)
						return curr.getKey();
					else
						curr = itr.next();
				}
				else
					curr = itr.next();
			}
		}
	}
}

class CountingHashtable<K, V> extends Hashtable<K, V>
{
	int total;
	
	public CountingHashtable()
	{
		super();
		total = 0;
	}
}

