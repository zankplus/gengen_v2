package Gengen_v2.gengenv2;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;

public class Phonology
{
	Random rng;
	
	// General properties
	double[] prominences;		// prominence of each consonantal phonetic property in the language
	double[] vowelProminences;	// prominence of each vocalic phonetic property
	double[] codaProminences;	// prominence of each consonantal property as applied to codas
	
	// Phonetic properties
	Phoneme[] cInv;			// language's CONSONANT inventory
	Phoneme[] vInv;			// language's VOWEL inventory
	
	ArrayList<SyllableSegment>[] onsets, nuclei, codas; 
	
	// Phonotactic properties
	int maxOnsetLength, maxNucleusLength, maxCodaLength, clusterBonus;
	private boolean[] consonantCategoriesRepresented;
	private boolean[] nucleusCategoriesRepresented;
	
	boolean[][] validOnsetTransitions;			// tables of valid transitions btwn different categories
	boolean[][] validNucleusTransitions;
	boolean[][] validCodaTransitions;
	
	double[] onsetClusterLeadProminences;		// how much each category lends itself to being followed by another sound in a cluster
	double[] onsetClusterFollowProminences;		// like above,  but for the sound that follows
	double[] nucleusLeadProminences;			// like the two above, but for diphthongs
	double[] nucleusFollowProminences;
	double[] codaClusterLeadProminences;		// and so on, for codas
	double[] codaClusterFollowProminences;
	
	double onsetClusterRatio;	// ratio of clusters of length N to those of length N-1 (N >= 2) in onsets
	double diphthongRatio;	// as above, but for nuclei
	
	double clusterImpediment;	// reduces the diversity of clusters appearing w/in a language
	double diphthongImpediment;	// as above, but for diphthongs
	
	double codaInhibitor;		// flat value subtracted from every coda prominence value to reflect (usually) decreased richness
								// in coda inventory
	double codaDisturbance;		// stdev for disturbance of codas, AS A PERCENTAGE of the prominenceStdev. at 0, the coda values
								// are undisturbed compared to the onset ones. at 1, they have essentially been rerolled.
	
	
	
	
	// Generator properties
	static double minimumOnsetClusterRatio   = 0.04; // minimum value for onsetClusterRatio 
	static double minimumNucleusClusterRatio = 0.04; // minimum value for diphthongRatio
	
	static double prominenceStdev      = 0.60;
	static double vowelProminenceStdev = 0.50;
	
	
	static double clusterLeadStdev     = 0.50;
	static double clusterFollowStdev   = 0.50;
	static double nucleusLeadStdev     = 0.50;
	static double nucleusFollowStdev   = 0.50;
	
	static double codaInhibitorMean	   = 0.5;
	static double codaInhibitorStdev   = 0.5;
	
	public Phonology()
	{
		rng = new Random();
		
		makeBasicSyllableStructure();
		determineProminence();
		selectSegments();
//		makeOnsets();
//		makeNuclei();
//		makeCodas();
//		makeCodaTactics();
	}
	
	@SuppressWarnings("unchecked")
	public void makeBasicSyllableStructure()
	{
		// Roll for onset
		int roll = rng.nextInt(12);
		if (roll < 3)			
			maxOnsetLength = 1;	// 3/12
		else if (roll < 9)
			maxOnsetLength = 2;	// 6/12
		else if (roll < 11)
			maxOnsetLength = 3;	// 2/12
		else
			maxOnsetLength = 4;	// 1/12
		
		// Initialize onset array(s)
		onsets = new ArrayList[maxOnsetLength];
		for (int i = 0; i < onsets.length; i++)
			onsets[i] = new ArrayList<SyllableSegment>();
		
		if (maxOnsetLength > 0)
		{
			onsetClusterRatio = Math.max(rng.nextGaussian() * 0.1 + 0.25, minimumOnsetClusterRatio);
			clusterImpediment = rng.nextGaussian() * 0.25 + 0.5;
		}
		
		// Roll for nucleus
		roll = rng.nextInt(4);
		if (roll < 1)
			maxNucleusLength = 1;
		else
			maxNucleusLength = 2;

		maxNucleusLength = 2;
		
		// Initialize nucleus array(s)
		nuclei = new ArrayList[maxNucleusLength];
		for (int i = 0; i < nuclei.length; i++)
			nuclei[i] = new ArrayList<SyllableSegment>();
		
		if (maxNucleusLength > 1)
		{
			diphthongRatio = Math.max(rng.nextGaussian() * 0.05 + 0.15, minimumNucleusClusterRatio);
			clusterImpediment = rng.nextGaussian() * 0.25 + 0.5;
		}
		
		// Roll for coda
		roll = rng.nextInt(8);
		if (roll < 1)
			maxCodaLength = maxOnsetLength + 1;	// 1/8
		else if (roll < 4)
			maxCodaLength = maxOnsetLength;		// 3/8
		else if (roll < 7)
			maxCodaLength = maxOnsetLength - 1;	// 3/8
		else
			maxCodaLength = maxOnsetLength - 2;	// 1/8
		
		maxCodaLength = Math.max(maxCodaLength, 0);
		
		// TODO: Debug value
		maxCodaLength = 3;
		
		// Cluster bonus = sum of values of cluster maxima in excess of 1
		clusterBonus = 0;
		if (maxOnsetLength > 1)
			clusterBonus += maxOnsetLength - 1;
		if (maxCodaLength > 1)
			clusterBonus += maxCodaLength - 1;
		
		for (int i = 0; i < maxOnsetLength; i++)
			System.out.print("C");
		for (int i = 0; i < maxNucleusLength; i++)
			System.out.print("V");
		for (int i = 0; i < maxCodaLength; i++)
			System.out.print("C");
		System.out.println();
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
		prominences = new double[ConsonantProperty.values().length];	// likelihood to appear at start of word
		vowelProminences = new double[VowelProperty.values().length];
		codaProminences = new double[ConsonantProperty.values().length];

		// Determine prominence values for consonant properties
		for (int i = 0; i < ConsonantProperty.values().length; i++)
			if (Math.random() < ConsonantProperty.values()[i].probability)	// properties failing this check receive 0 prominence
				prominences[i] = Math.max(rng.nextGaussian() * prominenceStdev + 1, 0.001);
		
		// Coda prominence values are generated by disturbing onset prominence values, that is, by adding a gaussian
		// term with mean 0 and randomly parametrized stdev, so they are influenced by onset values but not beholden
		// to them.
		if (maxCodaLength > 0)
		{
			// Coda inhibitor is the square of a uniformly distributed random number between 0 and 1
			codaDisturbance = Math.pow(rng.nextDouble(), 2);
			System.out.println("codaDisturbance:\t" + codaDisturbance);
			
			// Coda inhibitor is a gaussian random number; the mean and stdev are engine parameters.
			codaInhibitor = rng.nextGaussian() * codaInhibitorStdev + codaInhibitorMean;
			codaInhibitor = Math.max(0, codaInhibitor);
			
			System.out.println("codaInhibitor  :\t" + codaInhibitor);
			
			for (int i = 0; i < prominences.length; i++)
				if (prominences[i] > 0)
				{
					// Take the onset (general) prominence value as a base
					codaProminences[i] = prominences[i];
					
					// Disturb prominence
					codaProminences[i] += rng.nextGaussian() * prominenceStdev * codaDisturbance;
					
					// Inhibit prominence
					codaProminences[i] -= codaInhibitor;
				}
		}
		
		
		
		// onset cluster properties
		if (maxOnsetLength > 1)
		{
			onsetClusterLeadProminences   = new double[ConsonantProperty.values().length];
			onsetClusterFollowProminences = new double[ConsonantProperty.values().length];
			
			// Determine prominence for consonant categories as cluster leads
			for (int i = 0; i < ConsonantProperty.values().length; i++)
				if (prominences[i] > 0)
				{
					onsetClusterLeadProminences[i]   = rng.nextGaussian() * clusterLeadStdev   + 1;
					onsetClusterFollowProminences[i] = rng.nextGaussian() * clusterFollowStdev + 1;
				}
		}
		
		// coda cluster properties
		if (maxCodaLength > 1)
		{
			codaClusterLeadProminences   = new double[ConsonantProperty.values().length];
			codaClusterFollowProminences = new double[ConsonantProperty.values().length];
			
			// Determine prominence for consonant categories as cluster leads
			for (int i = 0; i < ConsonantProperty.values().length; i++)
				if (prominences[i] > 0)
				{
					codaClusterLeadProminences[i]   = rng.nextGaussian() * clusterLeadStdev   + 1;
					codaClusterFollowProminences[i] = rng.nextGaussian() * clusterFollowStdev + 1;
				}
		}
		
		// Determine prominence values for vowel properties
		for (int i = 0; i < VowelProperty.values().length; i++)
			if (Math.random() < VowelProperty.values()[i].probability)	// properties failing this check receive 0 prominence
				vowelProminences[i] = Math.max(rng.nextGaussian() * vowelProminenceStdev + 1, 0.001);
		
		if (maxNucleusLength > 1)
		{
			nucleusLeadProminences   = new double[VowelProperty.values().length];
			nucleusFollowProminences = new double[VowelProperty.values().length];
			
			// Determine prominence for consonant categories as cluster leads
			for (int i = 0; i < VowelProperty.values().length; i++)
				if (vowelProminences[i] > 0)
				{
					nucleusLeadProminences[i]   = rng.nextGaussian() * nucleusLeadStdev   + 1;
					nucleusFollowProminences[i] = rng.nextGaussian() * nucleusFollowStdev + 1;
				}
		}
	}
	
	// select which segments will be present in the language
	public void selectSegments()
	{
		// Make easy reference to segment arrays
		Consonant[] consonants = Consonant.segments;
				
		ArrayList<Phoneme> inv = new ArrayList<Phoneme>();
		
		// Populate consonant inventory
		for (int i = 0; i < consonants.length; i++)
		{
			boolean add = true;
			
			for (int j = 0; j < consonants[i].properties.length; j++)
				if (prominences[((ConsonantProperty) consonants[i].properties[j]).ordinal()] == 0)
				{
					add = false;
					j = consonants[i].properties.length;
				}
			
			if (add)
			{
				inv.add(new Phoneme(consonants[i]));
			}
		}
		
		cInv = inv.toArray(new Phoneme[0]);
		
		// Mark categories represented by this language's inventory
		consonantCategoriesRepresented = new boolean[Cluster.onsetCategories.size()];
		consonantCategoriesRepresented[0] = true;
		consonantCategoriesRepresented[consonantCategoriesRepresented.length - 1] = true;
		
		for (Phoneme p : inv)
			consonantCategoriesRepresented[p.segment.transitionCategory] = true;
		
		
		
		// Make easy reference to segment arrays
		Vowel[] vowels = Vowel.segments;
		inv = new ArrayList<Phoneme>();
		
		// Populate vowel inventory
		for (int i = 0; i < vowels.length; i++)
		{
			boolean add = true;
			
			for (int j = 0; j < vowels[i].properties.length; j++)
				if (vowelProminences[((VowelProperty) vowels[i].properties[j]).ordinal()] == 0)
				{
					add = false;
					j = vowels[i].properties.length;
				}
			
			if (add)
			{
				inv.add(new Phoneme(vowels[i]));
			}
		}
		
		System.out.println();
		vInv = inv.toArray(new Phoneme[inv.size()]);
		
		// Mark categories represented by this language's inventory
		nucleusCategoriesRepresented = new boolean[Cluster.nucleusCategories.size()];
		nucleusCategoriesRepresented[0] = true;
		nucleusCategoriesRepresented[nucleusCategoriesRepresented.length - 1] = true;
		
		for (Phoneme p : inv)
			nucleusCategoriesRepresented[p.segment.transitionCategory] = true;
	}
	
	// Create the lists of all onsets
	public void makeOnsets()
	{
		// 1. Determine VALIDITY of transitions between different phonetic categories
		int[][] transProb = Cluster.onsetTransitions;
		validOnsetTransitions = new boolean[transProb.length][transProb[0].length];
		
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
			if (consonantCategoriesRepresented[i])
				for (int j = 0; j < transProb[0].length; j++)
					if (consonantCategoriesRepresented[j] && transProb[i][j] > 0)
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
							validOnsetTransitions[i][j] = true; 
					}
		}
	
		// Determine all onsets
		for (Phoneme p : cInv)
		{
			ArrayList<Phoneme> onset = new ArrayList<Phoneme>();
			onset.add(p);
			findAllOnsets(onset);
		}

		// Shrink maxOnsetCluster to hide empty categories
		for ( ; onsets[maxOnsetLength - 1].size() == 0; maxOnsetLength--);
		
		// DEBUG: Print all possible onsets
		for (int i = 0; i < maxOnsetLength; i++)
		{
			System.out.println("LENGTH " + (i + 1));
			for (SyllableSegment ss : onsets[i])
				System.out.printf("%s\t%.3f\n", ss, ss.prominence);
			System.out.println();
		}
				
		System.out.printf("Impediment: %.3f\n", clusterImpediment);
		System.out.println("maxOnsetCluster: " + maxOnsetLength);

	}
	
	private void findAllOnsets(ArrayList<Phoneme> onset)
	{
		double prominence = onset.get(0).onsetInitialProminence;
		
		if (onset.size() > 1)
			for (int i = 0; i < onset.size() - 1; i++)
			{
				// this combined prominence value doesn't need its component deviations to be scaled;
				// it's fine (probably!) if more complex clusters have larger variances
				prominence += onset.get(i).onsetClusterLeadProminence + onset.get(i+1).onsetClusterFollowProminence - 2  - clusterImpediment;
			}
		
		// Add the current onset to the onset inventory
		SyllableSegment seg = new SyllableSegment(SegmentType.ONSET, onset.toArray(new Phoneme[onset.size()]), prominence);
		onsets[onset.size() - 1].add(seg);
		
		// If you've reached the largest cluster size, return; it is a waste of time to examine larger clusters.
		if (onset.size() >= maxOnsetLength)
			return;
		
		// Otherwise, consider how this cluster might continue.
		int ptCat = onset.get(onset.size() - 1).segment.transitionCategory; // get phonotactic transition category of the latest phoneme
		
		// figure out which sounds may follow phonotactically.
		// for each phonotactic category to which the current phoneme might transition
		for (int i = 0; i < validOnsetTransitions[ptCat].length; i++)
		{
			if (validOnsetTransitions[ptCat][i])
			{
				// consider every member segment
				for (int nextSound : Cluster.onsetCategories.get(i))
				{
					// if this language has that segment, then add it to the list
					for (int j = 0; j < cInv.length; j++)
					{
						if (cInv[j].segment.id == nextSound)
						{
							// Copy current onset and add this phoneme to it
							ArrayList<Phoneme> copy = new ArrayList<Phoneme>();
							for (Phoneme p : onset)
								copy.add(p);
							copy.add(cInv[j]);
							
							// Recurse on the copy
							findAllOnsets(copy);
							j = cInv.length;					
		}	}	}	}	}
	}
	
	// Create the lists of all nuclei
	public void makeNuclei()
	{
		// 1. Determine VALIDITY of transitions between different phonetic categories
		int[][] transProb = Cluster.nucleusTransitions;
		validNucleusTransitions = new boolean[transProb.length][transProb[0].length];
		
		// For more details on the math here, see makeOnsets()
		double[] leadProbability = new double[transProb.length], followProbability = new double[transProb[0].length];
		for (int i = 0; i < leadProbability.length; i++)
			leadProbability[i] = rng.nextDouble();
		for (int i = 0; i < followProbability.length; i++)
			followProbability[i] = rng.nextDouble();
		
		for (int i = 0; i < transProb.length; i++)
		{
			if (nucleusCategoriesRepresented[i])
				for (int j = 0; j < transProb[0].length; j++)
					if (nucleusCategoriesRepresented[j] && transProb[i][j] > 0)
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
							validNucleusTransitions[i][j] = true; 
					}
		}
	
		// Determine all nuclei
		for (Phoneme p : vInv)
		{
			ArrayList<Phoneme> nucleus = new ArrayList<Phoneme>();
			nucleus.add(p);
			findAllNuclei(nucleus);
		}

		// Shrink maxNucleusCluster to hide empty categories
		for ( ; nuclei[maxNucleusLength - 1].size() == 0; maxNucleusLength--);
		
		// DEBUG: Print all possible nuclei
		for (int i = 0; i < maxNucleusLength; i++)
		{
			System.out.println("LENGTH " + (i + 1));
			for (SyllableSegment ss : nuclei[i])
				System.out.printf("%s\t%.3f\n", ss, ss.prominence);
			System.out.println();
		}
	}

	private void findAllNuclei(ArrayList<Phoneme> nucleus)
	{
		double prominence = nucleus.get(0).onsetInitialProminence;
		
		if (nucleus.size() > 1)
			for (int i = 0; i < nucleus.size() - 1; i++)
			{
				// this combined prominence value doesn't need its component deviations to be scaled;
				// it's fine (probably!) if more complex clusters have larger variances
				prominence += nucleus.get(i).nucleusLeadProminence + nucleus.get(i+1).nucleusFollowProminence - 2 - clusterImpediment;
			}
		
		// Add this nucleus to the nucleus inventory
		SyllableSegment seg = new SyllableSegment(SegmentType.NUCLEUS, nucleus.toArray(new Phoneme[nucleus.size()]), prominence);
		nuclei[nucleus.size() - 1].add(seg);
		
		// If you've reached the largest cluster size, return; it is a waste of time to examine larger clusters.
		if (nucleus.size() >= maxNucleusLength)
			return;
		
		// Otherwise, consider how this cluster might continue.
		int ptCat = nucleus.get(nucleus.size() - 1).segment.transitionCategory; // get phonotactic transition category of the latest phoneme
		
		// figure out which sounds may follow phonotactically.
		// for each phonotactic category to which the current phoneme might transition
		for (int i = 0; i < validNucleusTransitions[ptCat].length; i++)
			if (validNucleusTransitions[ptCat][i])
			{
				// consider every member segment
				for (int nextSound : Cluster.nucleusCategories.get(i))
				{
					// if this language has that segment, then add it to the list
					for (int j = 0; j < vInv.length; j++)
						if (vInv[j].segment.id == nextSound)
						{
							// Copy current nucleus and add this phoneme to it
							ArrayList<Phoneme> copy = new ArrayList<Phoneme>();
							for (Phoneme p : nucleus)
								copy.add(p);
							copy.add(vInv[j]);
							
							// Recurse on the copy
							findAllNuclei(copy);
							j = vInv.length;
						}
				}
			}
	}
	
	/*// Create the lists of all codas
	public void makeCodas()
	{
		// 1. Determine VALIDITY of transitions between different phonetic categories
		int[][] transProb = Cluster.codaTransitions;
		validCodaTransitions = new boolean[transProb.length][transProb[0].length];
		
		// For notes on the math here, consult the corresponding method for syllable onsets
		double[] leadProbability = new double[transProb.length], followProbability = new double[transProb[0].length];
		for (int i = 0; i < leadProbability.length; i++)
			leadProbability[i] = rng.nextDouble();
		for (int i = 0; i < followProbability.length; i++)
			followProbability[i] = rng.nextDouble();
		
		for (int i = 0; i < transProb.length; i++)
		{
			if (consonantCategoriesRepresented[i])
				for (int j = 0; j < transProb[0].length; j++)
					if (consonantCategoriesRepresented[j] && transProb[i][j] > 0)
					{
						// Chance of representation ~= f(.7 x 15^(commonness - 3))
						// 3 -> .808, 2 -> .109, 1 -> .011
						double p = 0.4 * Math.pow(15, transProb[i][j] - 3);
						
						// No transition has more than a 90% chance of inclusion
						p = Math.min(p, 0.9);
						
						if (leadProbability[i] * followProbability[j] < p)
							validCodaTransitions[i][j] = true; 
					}
		}
	
		// Determine all codas
		for (Phoneme p : cInv)
		{
			ArrayList<Phoneme> coda = new ArrayList<Phoneme>();
			coda.add(p);
			findAllCodas(coda);
		}

		// Shrink maxOnsetCluster to hide empty categories
		for ( ; codas[maxCodaLength - 1].size() == 0; maxCodaLength--);
		
		// DEBUG: Print all possible onsets
		for (int i = 0; i < maxCodaLength; i++)
		{
			System.out.println("LENGTH " + (i + 1));
			for (SyllableSegment ss : codas[i])
				System.out.printf("%s\t%.3f\n", ss, ss.prominence);
			System.out.println();
		}
				
//		System.out.printf("Impediment: %.3f\n", clusterImpediment);
		System.out.println("maxCodaLength: " + maxCodaLength);

	}
	
	private void findAllCodas(ArrayList<Phoneme> coda)
	{
		double prominence = coda.get(0).onsetInitialProminence;
		
		if (coda.size() > 1)
			for (int i = 0; i < coda.size() - 1; i++)
			{
				// this combined prominence value doesn't need its component deviations to be scaled;
				// it's fine (probably!) if more complex clusters have larger variances
				prominence += coda.get(i).onsetClusterLeadProminence + coda.get(i+1).onsetClusterFollowProminence - 2  - clusterImpediment;
			}
		
		// Add the current onset to the onset inventory
		SyllableSegment seg = new SyllableSegment(SegmentType.ONSET, onset.toArray(new Phoneme[onset.size()]), prominence);
		onsets[onset.size() - 1].add(seg);
		
		// If you've reached the largest cluster size, return; it is a waste of time to examine larger clusters.
		if (onset.size() >= maxOnsetLength)
			return;
		
		// Otherwise, consider how this cluster might continue.
		int ptCat = onset.get(onset.size() - 1).segment.transitionCategory; // get phonotactic transition category of the latest phoneme
		
		// figure out which sounds may follow phonotactically.
		// for each phonotactic category to which the current phoneme might transition
		for (int i = 0; i < validOnsetTransitions[ptCat].length; i++)
		{
			if (validOnsetTransitions[ptCat][i])
			{
				// consider every member segment
				for (int nextSound : Cluster.onsetCategories.get(i))
				{
					// if this language has that segment, then add it to the list
					for (int j = 0; j < cInv.length; j++)
					{
						if (cInv[j].segment.id == nextSound)
						{
							// Copy current onset and add this phoneme to it
							ArrayList<Phoneme> copy = new ArrayList<Phoneme>();
							for (Phoneme p : onset)
								copy.add(p);
							copy.add(cInv[j]);
							
							// Recurse on the copy
							findAllOnsets(copy);
							j = cInv.length;					
		}	}	}	}	}
	}*/

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
		for (Phoneme p : cInv)
		{
			// Calculating combined prominence
			double phonemeProminence = p.onsetInitialProminence;
			
			Pair pair = new Pair(p, phonemeProminence);
			
			consonants.add(pair);
			cTotal += phonemeProminence;
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
		Segment segment;
		CountingHashtable<Phoneme, Integer> onsetTransitions;
		CountingHashtable<Phoneme, Integer> codaTransitions;
		
		double onsetInitialProminence;
		double onsetClusterLeadProminence;
		double onsetClusterFollowProminence;
		
		double codaInitialProminence;
		double codaClusterLeadProminence;
		double codaClusterFollowProminence;
		
		double nucleusLeadProminence;
		double nucleusFollowProminence;
		
		public Phoneme(Segment segment)
		{
			// Consonant case
			if (segment.isConsonant())
			{
				this.segment = segment;
				onsetTransitions = new CountingHashtable<Phoneme, Integer>();
				codaTransitions  = new CountingHashtable<Phoneme, Integer>();
				
				onsetInitialProminence       = 1;
				codaInitialProminence		 = 1;
//				onsetClusterLeadProminence   = 1;
//				onsetClusterFollowProminence = 1;
//				codaClusterLeadProminence   = 1;
//				codaClusterFollowProminence = 1;
				
				// initialProminence is the result of combining all the prominence values of the segment's 
				// properties, with mean 1. The deviance of each prominence is scaled by the root of the 
				// number of the segment's properties; this ensures that all intialProminence values are
				// distributed, in effect, with the same standard deviation, regardless of how many
				// values are added to make it (normally, adding random variables increases the stdev of the sum).
				// The same is true of the cluster prominence values as well.
				for (ConsonantProperty s : ((Consonant) segment).properties)
				{
					double deviance = prominences[s.ordinal()] - 1;
					deviance /= Math.sqrt(segment.properties.length);
					onsetInitialProminence += deviance;
					
					if (maxCodaLength > 0)
					{
						deviance = codaProminences[s.ordinal()] - 1;
						deviance /= Math.sqrt(segment.properties.length);
						codaInitialProminence += deviance;
					}
					
					// onset cluster properties
					if (maxOnsetLength > 1)
					{
						deviance = onsetClusterLeadProminences[s.ordinal()] - 1;
						deviance /= Math.sqrt(segment.properties.length);
						onsetClusterLeadProminence += deviance;
						
						deviance = onsetClusterFollowProminences[s.ordinal()] - 1;
						deviance /= Math.sqrt(segment.properties.length);
						onsetClusterFollowProminence += deviance;
					}
					else
					{
						onsetClusterLeadProminence = 0;
						onsetClusterFollowProminence = 0;
					}
					
					// coda cluster properties
					if (maxCodaLength > 1)
					{
						deviance = codaClusterLeadProminences[s.ordinal()] - 1;
						deviance /= Math.sqrt(segment.properties.length);
						codaClusterLeadProminence += deviance;
						
						deviance = codaClusterFollowProminences[s.ordinal()] - 1;
						deviance /= Math.sqrt(segment.properties.length);
						codaClusterFollowProminence += deviance;
					}
					else
					{
						codaClusterLeadProminence = 0;
						codaClusterFollowProminence = 0;
					}
				}
				
				System.out.printf("%s\t%.3f\t%.3f\t%.3f\t%.3f\t%.3f\t%.3f\n", segment.expression,
									onsetInitialProminence, onsetClusterLeadProminence, onsetClusterFollowProminence,
									codaInitialProminence,  codaClusterLeadProminence,  codaClusterFollowProminence);
			}
			
			
			// Vowel case
			else
			{
				this.segment = segment;
				
				onsetInitialProminence = 1;
				nucleusLeadProminence = 1;
				nucleusFollowProminence = 1;
				
				for (VowelProperty s : ((Vowel) segment).properties)
				{
					double deviance = vowelProminences[s.ordinal()] - 1;
					deviance /= Math.sqrt(segment.properties.length);
					onsetInitialProminence += deviance;
					
					if (maxNucleusLength > 1)
					{
						deviance = nucleusLeadProminences[s.ordinal()] - 1;
						deviance /= Math.sqrt(segment.properties.length);
						nucleusLeadProminence += deviance;
						
						deviance = nucleusFollowProminences[s.ordinal()] - 1;
						deviance /= Math.sqrt(segment.properties.length);
						nucleusFollowProminence += deviance;
					}
					else
					{
						nucleusLeadProminence = 0;
						nucleusFollowProminence = 0;
					}
				}
				
				System.out.printf("%s\t%.3f\t%.3f\t%.3f\n", segment.expression, onsetInitialProminence, nucleusLeadProminence, nucleusFollowProminence);
			}
			
			
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