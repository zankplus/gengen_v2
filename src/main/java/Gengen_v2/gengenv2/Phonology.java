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
	double[] prominences;	// prominence of each phonetic property in the language
	double[] codaProminence;
	
	// Phonetic properties
	Phoneme[] inv;			// language's phoneme inventory
	
	ArrayList<SyllableSegment>[] onsets, nuclei, codas; 
	
	// Phonotactic properties
	int maxOnsetCluster, maxNucleusCluster, maxCodaCluster, clusterBonus;
	private boolean[] ptCatsRepresented;	
	boolean[][] validCodaTransitions;
	double[] clusterLeadProminences;		// how much each category lends itself to being followed by another sound in a cluster
	double[] clusterFollowProminences;		// like above,  but for the sound that follows
	boolean[][] validTransitions;
	double clusterImpediment;	// reduces the diversity of clusters appearing w/in a language
	
	// Generator properties
	static double prominenceStdev    = 0.6;
	static double clusterLeadStdev   = 0.5;
	static double clusterFollowStdev = 0.5;
	
	public Phonology()
	{
		makeBasicSyllableStructure();
		determineProminence();
		selectSegments();
		makeOnsets();
//		makeCodaTactics();
	}
	
	@SuppressWarnings("unchecked")
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
		
		maxOnsetCluster = 3;
		
		if (maxOnsetCluster > 0)
		onsets = new ArrayList[maxOnsetCluster];
		{
			for (int i = 0; i < onsets.length; i++)
				onsets[i] = new ArrayList<SyllableSegment>();
			
			clusterImpediment = rng.nextGaussian() * 0.25 + 0.5;
		}
		
		
		// Roll for nucleus
		roll = rng.nextInt(4);
		if (roll < 1)
			maxNucleusCluster = 1;
		else
			maxNucleusCluster = 2;
		
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

	/* Determine which phonetic properties will be present in this language. Present languages receive 
	 * a random "prominence" value; absent values receive a value of 0. Any segment all of whose properties
	 * are present may feature in the language. This segment can be said to possess a combined prominence value 
	 * proportionate to the sum of its properties' prominence values; this represents, for now, the relative
	 * chance of it appearing at the start of an onset (consonants only).
	 * 
	 * Related clusterLeadProminence and clusterFollowProminence help determine how frequently a consonant will
	 * appear in a cluster preceeding or following (respectively) other phonemes.
	 */ 
	public void determineProminence()
	{
		Random rng = new Random();
		prominences = new double[SegProp.values().length];	// likelihood to appear at start of word 

		// Determine prominence value
		for (int i = 0; i < SegProp.values().length; i++)
			if (Math.random() < SegProp.values()[i].probability)	// properties failing this check receive 0 prominence
				prominences[i] = Math.max(rng.nextGaussian() * prominenceStdev + 1, 0.001);
		
		if (maxOnsetCluster > 1)
		{
			clusterLeadProminences   = new double[SegProp.values().length];
			clusterFollowProminences = new double[SegProp.values().length];
			
			// Determine prominence for consonant categories as cluster leads
			for (int i = 0; i < SegProp.values().length; i++)
				if (prominences[i] > 0)
				{
					clusterLeadProminences[i]   = rng.nextGaussian() * clusterLeadStdev   + 1;
					clusterFollowProminences[i] = rng.nextGaussian() * clusterFollowStdev + 1;
				}
		}
	}
	
	// 
	public void selectSegments()
	{
		// Make easy reference to segment arrays
		Consonant[] segs = Consonant.segments;
				
		ArrayList<Phoneme> inv = new ArrayList<Phoneme>();
		
		// Populate consonant inventory
		for (int i = 0; i < segs.length; i++)
		{
			boolean add = true;
			
			for (int j = 0; j < segs[i].properties.length; j++)
				if (prominences[segs[i].properties[j].ordinal()] == 0)
				{
					add = false;
					j = segs[i].properties.length;
				}
			
			if (add)
			{
				inv.add(new Phoneme(segs[i]));
			}
		}
		
		this.inv = inv.toArray(new Phoneme[0]);
		
		// Mark categories represented by this language's inventory
		ptCatsRepresented = new boolean[Cluster.ptCats.size()];
		ptCatsRepresented[0] = true;
		ptCatsRepresented[ptCatsRepresented.length - 1] = true;
		
		for (Phoneme p : inv)
			ptCatsRepresented[p.segment.transitionCategory] = true;
	}
	
	// Create the lists of all phonotactics
	public void makeOnsets()
	{
		Random rng = new Random();
		
		
		// 1. Determine VALIDITY of transitions between different phonetic categories
		int[][] transProb = Cluster.transitionProbability;
		validTransitions = new boolean[transProb.length][transProb[0].length];
		
		// Every possible transition between categories has a chance of occurring, but those chances
		// are not wholly independent from each other. Instead of rolling an independent random variable
		// for each combination, we instead roll two for each category - one to indicate the chance of it
		// leading in a cluster, the other the chance of following. These are both uniformly distributed
		// random variables between 0 and 1. The roll for the inclusion of a given cluster is the product
		// of the the first phoneme's leadProbability and the second's followProbability. The cdf for the
		// the product of these two random variables is f(z) = z - z * log z.
		
		double[] leadProbability = new double[transProb.length], followProbability = new double[transProb[0].length];
		for (int i = 0; i < leadProbability.length; i++)
			leadProbability[i] = rng.nextDouble();
		for (int i = 0; i < followProbability.length; i++)
			followProbability[i] = rng.nextDouble();
		
		for (int i = 0; i < transProb.length; i++)
		{
			if (ptCatsRepresented[i])
				for (int j = 0; j < transProb[0].length; j++)
					if (ptCatsRepresented[j] && transProb[i][j] > 0)
					{
						// Chance of representation ~= f(.7 x 15^(commonness - 3))
						// 3 -> .808, 2 -> .109, 1 -> .011
						double p = 0.4 * Math.pow(15, transProb[i][j] - 3);
						
						// Cluster bonus applies only to interconsonant transitions.
						// Mainly, this prevents consonant clusters from forming in
						// language whose prescribed structures do not allow them.
//						if (j > 0 && j < 17 && i != 0)
//							p *= clusterBonus;
						
						// No transition has more than a 90% chance of inclusion
						p = Math.min(p, 0.9);
						
						if (leadProbability[i] * followProbability[j] < p)
							validTransitions[i][j] = true; 
					}
		}
	
		// Determine all onsets
		for (Phoneme p : inv)
		{
			ArrayList<Phoneme> onset = new ArrayList<Phoneme>();
			onset.add(p);
			findAllOnsets(onset);
		}
		
		// DEBUG: Print all possible onsets
		for (int i = 0; i < onsets.length; i++)
		{
			System.out.println("LENGTH " + (i + 1));
			for (SyllableSegment ss : onsets[i])
				System.out.printf("%s\t%.3f\n", ss, ss.prominence);
			System.out.println();
		}
				
		System.out.printf("Impediment: %.3f\n", clusterImpediment);
	}
	
	public void findAllOnsets(ArrayList<Phoneme> onset)
	{
		double prominence = 1;
		
		if (onset.size() == 1)
			prominence = onset.get(0).initialProminence;
		else
			for (int i = 0; i < onset.size() - 1; i++)
			{
				// this combined prominence value doesn't need its component deviations to be scaled;
				// it's fine (probably!) if more complex clusters have larger variances
				prominence += onset.get(i).clusterLeadProminence + onset.get(i+1).clusterFollowProminence - 2  - clusterImpediment;
			}
		
		
		SyllableSegment seg = new SyllableSegment(SegmentType.ONSET, onset.toArray(new Phoneme[onset.size()]), prominence);
		onsets[onset.size() - 1].add(seg);
		
		// If you've reached the largest cluster size, return; it is a waste of time to examine larger clusters.
		if (onset.size() >= maxOnsetCluster)
			return;
		
		// Otherwise, consider how this cluster might continue.
		int ptCat = onset.get(onset.size() - 1).segment.transitionCategory; // get phonotactic transition category of the latest phoneme
		ArrayList<Phoneme> possibleFollowers = new ArrayList<Phoneme>();
		
		// figure out which sounds may follow phonotactically.
		// for each phonotactic category to which the current phoneme might transition
		for (int i = 0; i < validTransitions[ptCat].length; i++)
			if (validTransitions[ptCat][i])
			{
				// consider every member segment
				for (int nextSound : Cluster.ptCats.get(i))
				{
					// if this language has that segment, then add it to the list
					for (int j = 0; j < inv.length; j++)
						if (inv[j].segment.id == nextSound)
						{
							// Copy current onset and add this phoneme to it
							ArrayList<Phoneme> copy = new ArrayList<Phoneme>();
							for (Phoneme p : onset)
								copy.add(p);
							copy.add(inv[j]);
							
							// Recurse on the copy
							findAllOnsets(copy);
							j = inv.length;
						}
				}
			}

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
			double phonemeProminence = p.initialProminence;
			
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
		Consonant segment;
		CountingHashtable<Phoneme, Integer> onsetTransitions;
		CountingHashtable<Phoneme, Integer> codaTransitions;
		
		double initialProminence;
		double clusterLeadProminence;
		double clusterFollowProminence;
		
		public Phoneme(Consonant segment)
		{
			this.segment = segment;
			onsetTransitions = new CountingHashtable<Phoneme, Integer>();
			codaTransitions  = new CountingHashtable<Phoneme, Integer>();
			
			initialProminence       = 1;
			clusterLeadProminence   = 1;
			clusterFollowProminence = 1;
			
			// initialProminence is the result of combining all the prominence values of the segment's 
			// properties, with mean 1. The deviance of each prominence is scaled by the root of the 
			// number of the segment's properties; this ensures that all intialProminence values are
			// distributed, in effect, with the same standard deviation, regardless of how many
			// values are added to make it (normally, adding random variables increases the stdev of the sum).
			// The same is true of the cluster prominence values as well.
			for (SegProp s : segment.properties)
			{
				double deviance = prominences[s.ordinal()] - 1;
				deviance /= Math.sqrt(segment.properties.length);
				initialProminence += deviance;
				
				if (maxOnsetCluster > 1)
				{
					deviance = clusterLeadProminences[s.ordinal()] - 1;
					deviance /= Math.sqrt(segment.properties.length);
					clusterLeadProminence += deviance;
					
					deviance = clusterFollowProminences[s.ordinal()] - 1;
					deviance /= Math.sqrt(segment.properties.length);
					clusterFollowProminence += deviance;
				}
				else
				{
					clusterLeadProminence = 0;
					clusterFollowProminence = 0;
				}
			}
			
			System.out.printf("%s\t%.3f\t%.3f\t%.3f\n", segment.expression, initialProminence, clusterLeadProminence, clusterFollowProminence);
		}
	}

	class SyllableSegment
	{
		SegmentType type;
		Phoneme[] content;
		double prominence;
		
		public SyllableSegment (SegmentType type, Phoneme[] content, double prominence)
		{
			this.type = type;
			this.content = content;
			this.prominence = prominence;
		}
		
		public String toString()
		{
			String result = "";
			for (Phoneme p : content)
				result += p.segment.expression;
			return result;
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

enum SegmentType { ONSET, NUCLEUS, CODA; }