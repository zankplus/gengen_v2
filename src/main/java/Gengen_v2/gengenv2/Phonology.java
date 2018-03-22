package Gengen_v2.gengenv2;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Timer;

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
	
	ArrayList<SyllableSegment>[] onsets, nuclei, codas;		// inventories of different syllable segment types
	ArrayList<SyllableSegment>[][] interludes;
	
	double[] onsetClusterLengthProbabilities;
	double[] codaClusterLengthProbabilities;
	
	// Phonotactic properties
	int maxOnsetLength, maxNucleusLength, maxCodaLength, maxInterludeLength, clusterBonus;
	private boolean[] consonantCategoriesRepresented;
	private boolean[] nucleusCategoriesRepresented;
	
	boolean[][] validOnsetTransitions;			// tables of valid transitions btwn different categories
	boolean[][] validNucleusTransitions;
	boolean[][] validCodaTransitions;
	
	double[] onsetClusterLeadProminences;		// how much each category lends itself to being followed by another sound in a cluster
	double[] onsetClusterFollowProminences;		// like above,  but for the sound that follows
	double[] diphthongLeadProminences;			// like the two above, but for diphthongs
	double[] diphthongFollowProminences;
	double[] codaClusterLeadProminences;		// and so on, for codas
	double[] codaClusterFollowProminences;
	double[] interludeLeadProminences;			// and so on, for interludes
	double[] interludeFollowProminences;
	
	double emptyInitialOnsetProminence;
	double onsetClusterProminence;	// ratio of clusters of length N to those of length N-1 (N >= 2) in onsets
	double diphthongProminence;		// as above, but for nuclei
	double codaClusterProminence;	// for codas
	
	double onsetClusterInhibitor;	// reduces the diversity of clusters appearing w/in a language
	double diphthongInhibitor;		// as above, but for diphthongs
	double codaClusterInhibitor;	// for codas
	
	double codaInhibitor;		// flat value subtracted from every coda prominence value to reflect (usually) decreased richness
								// in coda inventory
	double codaDisturbance;		// stdev for disturbance of codas, AS A PERCENTAGE of the prominenceStdev. at 0, the coda values
								// are undisturbed compared to the onset ones. at 1, they have essentially been rerolled.
	
	double hiatusBonus;		// increases (or decreases) the probability of hiatus occurring between 2 nuclei
	double interludeBonus;	// increases (or decreases) the porbability of interludes forming between a coda and an onset
	
	// Phonotactic inhibitors
	double onsetNgInhibitor;			// reduces the chance of a onset 'ng'
	double onsetTlDlInhibitor; 			// reduces the chances of an onset 'tl' or 'dl'
	double nasalDissonanceInhibitor;	// reduces the prevalence of coda nasal-plosive clusters that disagree in articulation
	double unequalVoicingInhibitor;		// reduces the prevalence of interludes that disagree in voicing
	
	// Generator properties
	static double emptyInitialOnsetProminenceMean    = 0.3;
	static double emptyInitialOnsetProminenceStdev   = 0.15;
	static double initialOnsetClusterProminenceMean	 = 0.2;
	static double initialOnsetClusterProminenceStdev = 0.1;
	
	static double emptyTerminalCodaProminenceMean = 0.5;
	static double emptyTerminalCodaProminenceStdev = 0.5;
	
	static double minimumOnsetClusterProminence   = 0.04; // minimum value for onsetClusterProminence 
	static double minimumNucleusClusterProminence = 0.04; // " " " diphthongProminence
	static double minimumCodaClusterProminence 	 = 0.04; // " " " codaClusterProminence
	
	static double prominenceStdev      = 0.60;
	static double vowelProminenceStdev = 0.50;
	static double codaInhibitorMean	   = 0.4;
	static double codaInhibitorStdev   = 0.4;
	
	static double clusterLeadStdev     = 0.50;
	static double clusterFollowStdev   = 0.50;
	static double nucleusLeadStdev     = 0.50;
	static double nucleusFollowStdev   = 0.50;
	
	static double onsetNgInhibitorMean    = 2;
	static double onsetNgInhibitorStdev   = 1;
	static double onsetTlDlInhibitorMean  = 1;
	static double onsetTlDlInhibitorStdev = 0.5;
	static double nasalDissonanceInhibitorMean = 2;
	static double nasalDissonanceInhibitorStdev = 1;
	static double unequalVoicingInhibitorMean = 1.25;
	static double unequalVoicingInhibitorStdev = 0.5;
	
	static double hiatusBonusStdev = 0.15;
	static double interludeBonusStdev = 0.15;
	
	// statistics data
	int[] data;

	static final int SIMPLE_ONSETS	= 0;
	static final int COMPLEX_ONSETS	= 1;
	static final int SIMPLE_NUCLEI	= 2;
	static final int COMPLEX_NUCLEI	= 3;
	static final int SIMPLE_CODAS	= 4;
	static final int COMPLEX_CODAS	= 5;
	static final int INTERLUDES		= 6;
	
	
	// Constructor
	public Phonology()
	{
		rng = new Random();
		long seed = rng.nextLong();
		
		// TODO: debug value for 4-length onset clusters
//		seed = 3460348928036823746L;
		
		System.out.println("Seed: " + seed);
		rng.setSeed(seed);
		
		makeBasicSyllableStructure();
		determineProminence();
		selectSegments();
		makeOnsets();
		if (maxCodaLength > 0)
		{
			makeCodas();
			makeInterludes();
		}
		
		makeNuclei();
		makeHiatus();
		
		printInterludes(nuclei[0]);
		printInterludes(codas[0]);

		
		
		
//		setWeightPreferences();
		
//		Print inventories
//		System.out.println();
//		for (int i = 0; i < maxOnsetLength; i++ )
//			printInventory(onsets[i]);
//		for (int i = 0; i < maxCodaLength; i++ )
//			printInventory(codas[i]);
//		for (int i = 0; i < maxNucleusLength; i++ )
//			printInventory(nuclei[i]);
		
//		data = countSyllableLengths();
//		setClusterChances();
		
//		for (int i = 0; i < 100; i++)
//			new Flowchart(this);
	}
	
	@SuppressWarnings("unchecked")
	public void makeBasicSyllableStructure()
	{
		// Chance of a name starting with a vowel
		emptyInitialOnsetProminence = Math.max(rng.nextGaussian() * emptyInitialOnsetProminenceStdev + emptyInitialOnsetProminenceMean, 0);
			
		
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
		
		// TODO: debug value
		maxOnsetLength = 4;
		
		// Initialize onset array(s)
		onsets = new ArrayList[maxOnsetLength];
		for (int i = 0; i < onsets.length; i++)
			onsets[i] = new ArrayList<SyllableSegment>();
		
		if (maxOnsetLength > 0)
		{
			onsetClusterProminence = Math.max(rng.nextGaussian() * initialOnsetClusterProminenceStdev + initialOnsetClusterProminenceMean,
											  minimumOnsetClusterProminence);
			onsetClusterInhibitor = rng.nextGaussian() * 0.25 + 0.5;
		}
		
		// Roll for nucleus
		roll = rng.nextInt(4);
		if (roll < 1)
			maxNucleusLength = 1;
		else
			maxNucleusLength = 2;

		// Initialize nucleus array(s)
		nuclei = new ArrayList[maxNucleusLength];
		for (int i = 0; i < nuclei.length; i++)
			nuclei[i] = new ArrayList<SyllableSegment>();
		
		if (maxNucleusLength > 1)
		{
			diphthongProminence = Math.max(rng.nextGaussian() * 0.05 + 0.15, minimumNucleusClusterProminence);
			diphthongInhibitor = rng.nextGaussian() * 0.25 + 0.5;
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
	
		
		// TODO: debug value
		maxCodaLength = 3;
		
		// Initialize coda arrays
		codas = new ArrayList[maxCodaLength];
		for (int i = 0; i < codas.length; i++)
			codas[i] = new ArrayList<SyllableSegment>();
		
		if (maxCodaLength > 0)
		{
			codaClusterProminence = Math.max(rng.nextGaussian() * 0.1 + 0.25, minimumCodaClusterProminence);
			codaClusterInhibitor = rng.nextGaussian() * 0.25 + 0.5;
		}
		
		//Roll for interlude
		if (maxCodaLength > 0)
		{
			maxInterludeLength = 2 + rng.nextInt(maxOnsetLength + maxCodaLength - 2);
			interludes = new ArrayList[maxInterludeLength][maxInterludeLength];
			for (int i = 0; i < interludes.length; i++)
				for (int j = 0; j < interludes[0].length; j++)
					interludes[i][j] = new ArrayList<SyllableSegment>();
		}
		
		// Cluster bonus = sum of values of cluster maxima in excess of 1
		clusterBonus = 0;
		if (maxOnsetLength > 1)
			clusterBonus += maxOnsetLength - 1;
		if (maxCodaLength > 1)
			clusterBonus += maxCodaLength - 1;
		
//		for (int i = 0; i < maxOnsetLength; i++)
//			System.out.print("C");
//		for (int i = 0; i < maxNucleusLength; i++)
//			System.out.print("V");
//		for (int i = 0; i < maxCodaLength; i++)
//			System.out.print("C");
//		System.out.println();
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
			if (rng.nextDouble() < ConsonantProperty.values()[i].probability)	// properties failing this check receive 0 prominence
				prominences[i] = Math.max(rng.nextGaussian() * prominenceStdev + 1, 0.001);
		
		// Coda prominence values are generated by disturbing onset prominence values, that is, by adding a gaussian
		// term with mean 0 and randomly parametrized stdev, so they are influenced by onset values but not beholden
		// to them.
		if (maxCodaLength > 0)
		{
			// Coda inhibitor is the square of a uniformly distributed random number between 0 and 1
			codaDisturbance = Math.pow(rng.nextDouble(), 2);
//			System.out.println("codaDisturbance:\t" + codaDisturbance);
			
			
			// Coda inhibitor is a gaussian random number; the mean and stdev are engine parameters.
			codaInhibitor = rng.nextGaussian() * codaInhibitorStdev + codaInhibitorMean;
			codaInhibitor = Math.max(0, codaInhibitor);
			
//			System.out.println("codaInhibitor  :\t" + codaInhibitor);
			
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
			
			// Randomly determine prominence values
			for (int i = 0; i < ConsonantProperty.values().length; i++)
				if (prominences[i] > 0)
				{
					codaClusterLeadProminences[i]   = rng.nextGaussian() * clusterLeadStdev   + 1;
					codaClusterFollowProminences[i] = rng.nextGaussian() * clusterFollowStdev + 1;
				}
		}
		
		// interlude properties
		if (maxInterludeLength > 1)
		{
			interludeLeadProminences =   new double[ConsonantProperty.values().length];
			interludeFollowProminences = new double[ConsonantProperty.values().length];
			
			// Determine prominence for consonant categories as cluster leads
			for (int i = 0; i < ConsonantProperty.values().length; i++)
				if (prominences[i] > 0)
				{
					interludeLeadProminences[i]   = rng.nextGaussian() * clusterLeadStdev   + 1;
					interludeFollowProminences[i] = rng.nextGaussian() * clusterFollowStdev + 1;
				}
		}
		
		// Determine prominence values for vowel properties
		for (int i = 0; i < VowelProperty.values().length; i++)
			if (rng.nextDouble() < VowelProperty.values()[i].probability)	// properties failing this check receive 0 prominence
				vowelProminences[i] = Math.max(rng.nextGaussian() * vowelProminenceStdev + 1, 0.001);
		
		if (maxNucleusLength > 1)
		{
			diphthongLeadProminences   = new double[VowelProperty.values().length];
			diphthongFollowProminences = new double[VowelProperty.values().length];
			
			// Determine prominence for consonant categories as cluster leads
			for (int i = 0; i < VowelProperty.values().length; i++)
				if (vowelProminences[i] > 0)
				{
					diphthongLeadProminences[i]   = rng.nextGaussian() * nucleusLeadStdev   + 1;
					diphthongFollowProminences[i] = rng.nextGaussian() * nucleusFollowStdev + 1;
				}
		}
		
		// Set inhibitors
		// initial 'ng'
		onsetNgInhibitor   		 = Math.max(rng.nextGaussian() *   onsetNgInhibitorStdev +   onsetNgInhibitorMean, 0);
		onsetTlDlInhibitor 		 = Math.max(rng.nextGaussian() * onsetTlDlInhibitorStdev + onsetTlDlInhibitorMean, 0);
		nasalDissonanceInhibitor = Math.max(rng.nextGaussian() * nasalDissonanceInhibitorStdev + nasalDissonanceInhibitorMean, 0);
		unequalVoicingInhibitor  = Math.max(rng.nextGaussian() * unequalVoicingInhibitorStdev  + unequalVoicingInhibitorMean,  0);
		
		System.out.println(nasalDissonanceInhibitor);
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
		consonantCategoriesRepresented = new boolean[Cluster.consonantCategories.size()];
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
		
//		System.out.println();
		vInv = inv.toArray(new Phoneme[inv.size()]);
		
		// Mark categories represented by this language's inventory
		nucleusCategoriesRepresented = new boolean[Cluster.vowelCategories.size()];
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
						// Chance of representation ~= f(.4 x 15^(commonness - 3))
						// 3 -> .559, 2 -> .069, 1 -> .007
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
		findAllOnsets();

		// Remove any unused onsets
		for (ArrayList<SyllableSegment> list : onsets)
			for (int i = 0; i < list.size(); i++)
				if (list.get(i).prominence < 0)
				{
					list.remove(i);
					i--;
				}
		
		// Shrink maxOnsetCluster to hide empty categories
		for ( ; onsets[maxOnsetLength - 1].size() == 0; maxOnsetLength--);
		
		// Normalize onset values
		for (int i = 0; i < maxOnsetLength; i++)
		{
			double total = 0;
			for (SyllableSegment onset : onsets[i])
				if (onset.prominence > 0)
					total += onset.prominence;
			
			for (SyllableSegment onset : onsets[i])
				onset.prominence = onset.prominence / total;
			
			Collections.sort(onsets[i]);
			Collections.reverse(onsets[i]);
		}

		// DEBUG: Print all possible onsets
//		for (int i = 0; i < maxOnsetLength; i++)
//		{
//			System.out.println("LENGTH " + (i + 1));
//			for (SyllableSegment ss : onsets[i])
//				System.out.printf("%s\t%.3f\n", ss, ss.prominence);
//			System.out.println();
//		}			
//		
//		System.out.printf("Impediment: %.3f\n", onsetClusterInhibitor);
//		System.out.println("maxOnsetCluster: " + maxOnsetLength);

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
						// Chance of representation ~= f(.4 x 15^(commonness - 3))
						// 3 -> .559, 2 -> .069, 1 -> .007
						double p = 0.4 * Math.pow(15, transProb[i][j] - 3);
						
						// Cluster bonus applies only to interconsonant transitions.
						// Mainly, this prevents consonant clusters from forming in
						// language whose prescribed structures do not allow them.
//						if (j > 0 && j < 17 && i != 0)
//							p *= clusterBonus;
						
						// No transition has more than a 90% chance of inclusion
//						p = Math.min(p, 0.9);
						
						if (leadProbability[i] * followProbability[j] < p)
							validNucleusTransitions[i][j] = true; 
					}
		}
		
		// Determine all nuclei
		findAllNuclei();

		// Remove vowel lengthener from simple nuclei
		for (int i = 0; i < nuclei[0].size(); i++)
			if (nuclei[0].get(i).content[0].segment.expression.equals(":"))	
			{
				nuclei[0].remove(i);
				System.out.println("Removed lengthener from simple nuclei");
			}
		
		// Removed any unused nuclei
		for (ArrayList<SyllableSegment> list : nuclei)
			for (int i = 0; i < list.size(); i++)
				if (list.get(i).prominence < 0)
				{
					list.remove(i);
					i--;
				}
		
		// Shrink maxNucleusCluster to hide empty categories
		for ( ; nuclei[maxNucleusLength - 1].size() == 0; maxNucleusLength--);
		
		// Normalize nucleus values
		for (int i = 0; i < maxNucleusLength; i++)
		{
			double total = 0;
			for (SyllableSegment nucleus : nuclei[i])
				if (nucleus.prominence > 0)
					total += nucleus.prominence;
			
			for (SyllableSegment nucleus : nuclei[i])
				nucleus.prominence = nucleus.prominence / total;
			
			Collections.sort(nuclei[i]);
			Collections.reverse(nuclei[i]);
		}
		
		// DEBUG: Print all possible nuclei
//			for (int i = 0; i < maxNucleusLength; i++)
//			{
//				System.out.println("LENGTH " + (i + 1));
//				for (SyllableSegment ss : nuclei[i])
//					System.out.printf("%s\t%.3f\n", ss, ss.prominence);
//				System.out.println();
//			}
	}

	// Create the lists of all codas
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
						// Chance of representation ~= f(.4 x 15^(commonness - 3))
						// 3 -> .559, 2 -> .069, 1 -> .007
						double p = 0.4 * Math.pow(15, transProb[i][j] - 3);
						
						// No transition has more than a 90% chance of inclusion
						p = Math.min(p, 0.9);
						
						if (leadProbability[i] * followProbability[j] < p)
							validCodaTransitions[i][j] = true; 
					}
		}
	
		// Determine all codas
		findAllCodas();

		// Remove unused codas
		for (ArrayList<SyllableSegment> list : codas)
			for (int i = 0; i < list.size(); i++)
				if (list.get(i).prominence < 0)
				{
					list.remove(i);
					i--;
				}
		
		// Shrink maxOnsetCluster to hide empty categories
		for ( ; codas[maxCodaLength - 1].size() == 0; maxCodaLength--);
		
		// Normalize coda values
		for (int i = 0; i < maxCodaLength; i++)
		{
			double total = 0;
			for (SyllableSegment coda : codas[i])
				if (coda.prominence > 0)
					total += coda.prominence;
			
			for (SyllableSegment coda : codas[i])
				coda.prominence = coda.prominence / total;
			
			Collections.sort(codas[i]);
			Collections.reverse(codas[i]);
		}
		
//		
//		// DEBUG: Print all possible onsets
//		for (int i = 0; i < maxCodaLength; i++)
//		{
//			System.out.println("LENGTH " + (i + 1));
//			for (SyllableSegment ss : codas[i])
//				System.out.printf("%s\t%.3f\n", ss, ss.prominence);
//			System.out.println();
//		}
//				
////		System.out.printf("Impediment: %.3f\n", clusterImpediment);
//		System.out.println("maxCodaLength: " + maxCodaLength);

	}
	
	// Decides what nucleus-nucleus transitions are allowed in the case of an empty interlude
	public void makeHiatus()
	{
		hiatusBonus = rng.nextGaussian() * hiatusBonusStdev;
		
		int[][] transProb = Cluster.hiatusTransitions;

		// Same principle as in makeOnsets(), etc.
		// -1 to count ignores the 'lengthener' segment
		double[] leadProbability = new double[Vowel.count - 1], followProbability = new double[Vowel.count - 1];
		for (int i = 0; i < leadProbability.length; i++)
			leadProbability[i] = rng.nextDouble();
		for (int i = 0; i < followProbability.length; i++)
			followProbability[i] = rng.nextDouble();
		
		for (int i = 0; i < nuclei[0].size(); i++)
		{
			Phoneme p1 = nuclei[0].get(i).content[0];
			for (int j = 0; j < nuclei[0].size(); j++)
			{
				Phoneme p2 = nuclei[0].get(j).content[0];
				
				// Chance of representation ~= f(.3 x 4^(commonness - 3))
				// 3 -> .446, 2 -> .155, 1 -> .050
				double probability = 0.3 * Math.pow(4, transProb[p1.segment.id][p2.segment.id] - 3) + hiatusBonus;
				
				System.out.printf("%s%s %.3f vs. %.3f [%.3f * %.3f]\t", p1.segment.expression, p2.segment.expression,
						probability, (leadProbability[p1.segment.id] * followProbability[p2.segment.id]),
						leadProbability[p1.segment.id], followProbability[p2.segment.id]);
				
				if (leadProbability[p1.segment.id] * followProbability[p2.segment.id] < probability)
					p1.addInterlude(p2);
				
				System.out.println();
			}
			
			// Normalize and sort values
			p1.normalizeAndSortInterludes();
		}
		System.out.println("Hiatus bonus: " +  hiatusBonus);
		

	}
	
	// Decides what coda-onset transitions are allowed in the case of a complex interlude
	public void makeInterludes()
	{
		interludeBonus = rng.nextGaussian() * interludeBonusStdev;
		
		int[][] transProb = Cluster.interludeTransitions;
		
		double[] leadProbability   = new double[transProb.length],
				 followProbability = new double[transProb[0].length];
		
		for (int i = 0; i < leadProbability.length; i++)
			leadProbability[i] = rng.nextDouble();
		for (int i = 0; i < followProbability.length; i++)
			followProbability[i] = rng.nextDouble();
		
		// Generate lists of interlude transitions for phonemes
		for (int i = 0; i < codas[0].size(); i++)
		{
			Phoneme p1 = codas[0].get(i).content[0];
			for (int j = 0; j < onsets[0].size(); j++)
			{
				Phoneme p2 = onsets[0].get(j).content[0];
				
				if (p1 != p2)	// don't match up 
				{
					// Chance of representation ~= f(.4 x 15^(commonness - 3))
					// 3 -> .559, 2 -> .069, 1 -> .007
					double probability = 0.4 * Math.pow(15, transProb[p1.segment.transitionCategory][p2.segment.transitionCategory] - 3)
											+ interludeBonus;
					
//					System.out.printf("a%s%sa\t%.3f vs. %.3f [%.3f * %.3f]\t", p1.segment.expression, p2.segment.expression,
//							probability, (leadProbability[p1.segment.transitionCategory] * followProbability[p2.segment.transitionCategory]),
//							leadProbability[p1.segment.transitionCategory], followProbability[p2.segment.transitionCategory]);
					
					if (leadProbability[p1.segment.transitionCategory] * followProbability[p2.segment.transitionCategory] < probability)
						p1.addInterlude(p2);
					
//					System.out.println();
				}
			}
			
			// Normalize and sort values
			p1.normalizeAndSortInterludes();
		}
		System.out.println("Interlude bonus: " +  interludeBonus);
	}
		
	
	public int[] countSyllableLengths()
	{
		int[] results = new int[6];
		
		// Count all simple and complex onsets, nuclei, and codas with positive prominence
		for (int i = 0; i < onsets.length; i++)
			for (SyllableSegment ss : onsets[i])
				if (ss.prominence > 0)
					if (i == 0)
						results[SIMPLE_ONSETS]++;
					else
						results[COMPLEX_ONSETS]++;
		
		for (int i = 0; i < nuclei.length; i++)
			for (SyllableSegment ss : nuclei[i])
				if (ss.prominence > 0)
					if (i == 0)
						results[SIMPLE_NUCLEI]++;
					else
						results[COMPLEX_NUCLEI]++;
		
		for (int i = 0; i < codas.length; i++)
			for (SyllableSegment ss : codas[i])
				if (ss.prominence > 0)
					if (i == 0)
						results[SIMPLE_CODAS]++;
					else
						results[COMPLEX_CODAS]++;
		
//		System.out.printf("Onsets:\t%d (%d simple, %d complex)\n", (simpleOnsets + complexOnsets), simpleOnsets, complexOnsets);
//		System.out.printf("Nuclei:\t%d (%d simple, %d complex)\n", (simpleNuclei + complexNuclei), simpleNuclei, complexNuclei);
//		System.out.printf("Codas:\t%d (%d simple, %d complex)\n", (simpleCodas + complexCodas), simpleCodas, complexCodas);
		
		return results;
	}

	// TODO: i am way too tired to explain the math here right now but the idea is that the prominence value for each list of length
	// x is scaled by the log of the number of items in it and then its proportion of the total is deducted
	public void setClusterChances()
	{
		onsetClusterLengthProbabilities = new double[maxOnsetLength - 1];
		double[] baseOdds = new double[maxOnsetLength - 1];
		
		if (maxOnsetLength == 2)
			onsetClusterLengthProbabilities[0] = 1;
		else
		{
			for (int i = 0; i < maxOnsetLength - 2; i++)
			{
				// Number of onsets of greater length than this one
				int remainingTotal = 0;
				for (int j = i + 2; j < maxOnsetLength; j++)
					remainingTotal += onsets[j].size();
				
				// Proportion of onsets of this length relative to those of greater (out of 1)
				double base = Math.log(onsets[i+1].size() + 1) /
							  Math.log((onsets[i+1].size() + 1) * (Math.pow(remainingTotal + 1, onsetClusterProminence)));
				
				// Proportion of all complex onsets of this length or longer
				double remainingProportion = 1;
				for (int j = 0; j < i; j++)
					remainingProportion -= onsetClusterLengthProbabilities[j];
				
				onsetClusterLengthProbabilities[i] = base * remainingProportion;
			}
			
			double remainingProportion = 1;
			for (int j = 0; j < onsetClusterLengthProbabilities.length - 1; j++)
				remainingProportion -= onsetClusterLengthProbabilities[j];
			
			// Basically the complement to all previous odds
			onsetClusterLengthProbabilities[onsetClusterLengthProbabilities.length - 1] = (1 - baseOdds[onsetClusterLengthProbabilities.length - 2]) * remainingProportion;
		}
		
		
		for(int i = 0; i < onsetClusterLengthProbabilities.length; i ++)
			System.out.println(onsetClusterLengthProbabilities[i]);
		
		double total = 0;
		for (double o : onsetClusterLengthProbabilities)
			total += o;
		System.out.println("total\t" + total);
		
		
		// Repeat the process for codas
		codaClusterLengthProbabilities = new double[maxCodaLength - 1];
		baseOdds = new double[maxCodaLength - 1];
		
		if (maxCodaLength == 2)
			codaClusterLengthProbabilities[0] = 1;
		else
		{
			for (int i = 0; i < maxCodaLength - 2; i++)
			{
				// Number of codas of greater length than this one
				int remainingTotal = 0;
				for (int j = i + 2; j < maxCodaLength; j++)
					remainingTotal += codas[j].size();
				
				// Proportion of codas of this length relative to those of greater (out of 1)
				double base = Math.log(codas[i+1].size() + 1) /
							  Math.log((codas[i+1].size() + 1) * (Math.pow(remainingTotal + 1, codaClusterProminence)));
				
				// Proportion of all complex onsets of this length or longer
				double remainingProportion = 1;
				for (int j = 0; j < i; j++)
					remainingProportion -= codaClusterLengthProbabilities[j];
				
				codaClusterLengthProbabilities[i] = base * remainingProportion;
			}
			
			double remainingProportion = 1;
			for (int j = 0; j < codaClusterLengthProbabilities.length - 1; j++)
				remainingProportion -= codaClusterLengthProbabilities[j];
			
			// Basically the complement to all previous odds
			codaClusterLengthProbabilities[codaClusterLengthProbabilities.length - 1] = (1 - baseOdds[codaClusterLengthProbabilities.length - 2]) * remainingProportion;
		}
		
		
		for(int i = 0; i < codaClusterLengthProbabilities.length; i ++)
			System.out.println(codaClusterLengthProbabilities[i]);
		
		total = 0;
		for (double o : codaClusterLengthProbabilities)
			total += o;
		System.out.println("total\t" + total);
	}
	
	public void setWeightPreferences()
	{
		double heavyStrongSyllableChance;
		double heavyWeakSyllableChance;
		double heavyExtrametricalSyllableChance;
		
		double finalHeavyStrongSyllableChance;
		double finalHeavyWeakSyllableChance;
		double finalHeavyExtrametricalSyllableChance;
		
		double heavyStrongSyllableProminenceMean = 2.0/3;
		double heavyStrongsyllableProminenceStdev = 1.0/3;
		double lightStrongSyllableProminenceMean = 1.0/3;
		double lightStrongSyllableProminenceStdev = 1.0/6;
		
		// scale prominence by log of the number of possible long syllables
		double heavyStrongSyllableProminence = (SIMPLE_NUCLEI + COMPLEX_ONSETS);
		
		
	}
	
	// The math here is simple. Having normalized them already, the prominence values of every inventory list sum to 1.
	// We generate a random number between 1 and 0 and subtract prominence values in order (the  lists are sorted largest
	// to smallest) until we reach a number lower than 0. The syllable segment whose prominence value took us over the edge
	// is returned.
	public SyllableSegment pickSyllableSegment(ArrayList<SyllableSegment> inventory)
	{
		double rand = rng.nextDouble();
		for (SyllableSegment ss : inventory)
		{
			if (rand < ss.prominence)
				return ss;
			else
				rand -= ss.prominence;
		}
		
		System.err.println("Failed to select syllable segment; were the inventory's prominence values not normalized?");
		System.exit(0);
		
		return null;
	}
	
	// Note that this function doesn't return an actual length value but the index (>= 1) of the onsets[] or codas[] array
	// corresponding to the appropriate index of the onsetProbabilities[] array 
	public int pickClusterLength(double[] clusterLengthProbabilities)
	{
		// Select length of onset
		double rand = rng.nextDouble();
		for (int i = 0 ; i < clusterLengthProbabilities.length; i++)
		{
			if (rand < clusterLengthProbabilities[i])
				return i + 1;
			else
				rand -= clusterLengthProbabilities[i];
		}
		
		return -1;
	}
	
	private void findAllOnsets()
	{
		for (Phoneme p : cInv)
		{
			ArrayList<Phoneme> onset = new ArrayList<Phoneme>();
			onset.add(p);
			findAllOnsets(onset);
		}
	}
	
	private void findAllOnsets(ArrayList<Phoneme> onset)
	{
		double prominence = onset.get(0).onsetInitialProminence;
		
		if (onset.size() > 1)
			for (int i = 0; i < onset.size() - 1; i++)
			{
				// this combined prominence value doesn't need its component deviations to be scaled;
				// it's fine (probably!) if more complex clusters have larger variances
				prominence += onset.get(i).onsetClusterLeadProminence + onset.get(i+1).onsetClusterFollowProminence - 2  - onsetClusterInhibitor;
				
				// inhibitor for tl/dl
				if (onset.get(i).segment.properties[0] == ConsonantProperty.PLOSIVE &&
					onset.get(i).segment.properties[1] == ConsonantProperty.ALVEOLAR &&
					onset.get(i+1).segment.expression.equals("l"))
				{
					prominence -= onsetTlDlInhibitor;
				}
				
				// inhibitor for sh/zh
			}
//		else if (onset.get(0).segment.expression.equals("ng"))
//			prominence -= onsetNgInhibitor;
		
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
				for (int nextSound : Cluster.consonantCategories.get(i))
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
	
	private void findAllNuclei()
	{
		for (Phoneme p : vInv)
		{
			ArrayList<Phoneme> nucleus = new ArrayList<Phoneme>();
			nucleus.add(p);
			findAllNuclei(nucleus);
		}
	}
	
	private void findAllNuclei(ArrayList<Phoneme> nucleus)
	{
		// do not allow nuclei beginning with a vowel lengthener
		if (nucleus.get(0).segment.expression.equals(":"))
			return;
		
		double prominence = nucleus.get(0).onsetInitialProminence;
		
		if (nucleus.size() > 1)
			for (int i = 0; i < nucleus.size() - 1; i++)
			{
				// this combined prominence value doesn't need its component deviations to be scaled;
				// it's fine (probably!) if more complex clusters have larger variances
				prominence += nucleus.get(i).nucleusLeadProminence + nucleus.get(i+1).nucleusFollowProminence - 2 - diphthongInhibitor;
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
				for (int nextSound : Cluster.vowelCategories.get(i))
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
	
	private void findAllCodas()
	{
		for (Phoneme p : cInv)
		{
			ArrayList<Phoneme> coda = new ArrayList<Phoneme>();
			coda.add(p);
			findAllCodas(coda, Cluster.codaTransitions.length - 1);
		}
	}
	
	// start: the phonotactic category to start combing from
	private void findAllCodas(ArrayList<Phoneme> coda, int start)
	{
		// do not allow codas beginning with a glide
		if (coda.get(0).segment.properties[0].equals(ConsonantProperty.GLIDE))
			return;
		
		double prominence = coda.get(0).codaInitialProminence;
		
		if (coda.size() > 1)
			for (int i = 0; i < coda.size() - 1; i++)
			{
				// this combined prominence value doesn't need its component deviations to be scaled;
				// it's fine (probably!) if more complex clusters have larger variances
				prominence += coda.get(i).codaClusterLeadProminence + coda.get(i+1).codaClusterFollowProminence - 2 - codaClusterInhibitor;
				
				/* penalize for unassimilated nasal-plosive clusters
				 */
				if (isDissonantNasalCluster(coda.get(i), coda.get(i+1)))
				{
					prominence -= nasalDissonanceInhibitor;
				}
			}
		
		// Add the current coda to the coda inventory
		SyllableSegment seg = new SyllableSegment(SegmentType.CODA, coda.toArray(new Phoneme[coda.size()]), prominence);
		codas[coda.size() - 1].add(seg);
		
		// If you've reached the largest cluster size, return; it is a waste of time to examine larger clusters.
		if (coda.size() >= maxCodaLength)
			return;
		
		// Otherwise, consider how this cluster might continue.
		int ptCat = coda.get(coda.size() - 1).segment.transitionCategory; // get phonotactic transition category of the latest phoneme
		
		// figure out which sounds may follow phonotactically.
		// we sweep across the transitions chart in a modular manner, beginning 1 category after the initial letter
		// and ending 1 category before it, so that we never include it twice.
		// for each phonotactic category to which the current phoneme might transition,
		int curr = coda.get(coda.size() - 1).segment.transitionCategory;
		
		for (int i = Math.floorMod(curr-1, validCodaTransitions.length); i != start; i = Math.floorMod(i-1, validCodaTransitions.length))
		{
			if (validCodaTransitions[ptCat][i])
			{
				// consider every member segment
				for (int nextSound : Cluster.consonantCategories.get(i))
				{
					// if this language has that segment, then add it to the list
					for (int j = 0; j < cInv.length; j++)
					{
						if (cInv[j].segment.id == nextSound)
						{
							// Copy current onset and add this phoneme to it
							ArrayList<Phoneme> copy = new ArrayList<Phoneme>();
							for (Phoneme p : coda)
								copy.add(p);
							copy.add(cInv[j]);
							
							// Recurse on the copy
							findAllCodas(copy, start);
							j = cInv.length;					
		}	}	}	}	}
	}

	public void printInventory(ArrayList<SyllableSegment> list)
	{
		if (list.size() == 0)
			return;
	
		System.out.println(list.get(0).type + " " + list.get(0).content.length);
		for (SyllableSegment ss : list)
			System.out.printf("%s\t%.3f\n", ss, (ss.prominence / 1));
		
		System.out.println();
	}
	
	public void printInterludes(ArrayList<SyllableSegment> list)
	{
		if (list.size() == 0)
			return;
		
		for (SyllableSegment coda : list)
			coda.content[coda.content.length - 1].printInterludes();
	}
	
/*	static public void gatherStats(int total)
	{
		long startTime = System.nanoTime();
		
		for (int i = 0; i < total; i++)
			new Phonology();
		
		long endTime = System.nanoTime();
		double time = (endTime - startTime) / (total * 1000000);
		
		System.out.println("AVERAGE\tSIMPLE\tCOMPLEX");
		System.out.println("ONSETS\t" + (simpleOnsets / total) + "\t" + (complexOnsets / total));
		System.out.println("NUCLEI\t" + (simpleNuclei / total) + "\t" + (complexNuclei / total));
		System.out.println("CODAS \t" + (simpleCodas  / total) + "\t" + (complexCodas  / total));
		
		System.out.println("Average time per language: " + time + "ms");
	}*/
	
	/* Returns true if a nasal cluster has unharmonious voicing, i.e.,
	 * 1.  the first segment is a NASAL, and either
	 * 2a. the second segment is a plosive and its place of articulation differs from the first, OR
	 * 2b. the second segment is postalveolar and the first is not nasal
	 * */
	public boolean isDissonantNasalCluster(Phoneme p1, Phoneme p2)
	{
		if (p1.segment.properties[0] == ConsonantProperty.NASAL &&
				(	(p2.segment.properties[0] == ConsonantProperty.PLOSIVE &&
					 p1.segment.properties[1] != p2.segment.properties[1]) || 
					(p2.segment.properties[1] == ConsonantProperty.POSTALVEOLAR &&
					 p1.segment.properties[1] != ConsonantProperty.ALVEOLAR)
				)
			   )
			{
				return true;
			}
		return false;
	}
	
	/* Returns true if a cluster disagrees in voicing, i.e.,
	 * 1.  both segments are plosives, affricates, or fricatives, and either
	 * 2a. the first is voiced and the second is unvoiced, or
	 * 2b. vice versa
	 * */
	public boolean isUnequalVoicing(Phoneme p1, Phoneme p2)
	{
		SegmentProperty place1 = p1.segment.properties[0];
		SegmentProperty place2 = p2.segment.properties[0];
		SegmentProperty voice1 = p1.segment.properties[1];
		SegmentProperty voice2 = p2.segment.properties[1];
		Segment s2 = p2.segment;
		
		if ((place1 == ConsonantProperty.PLOSIVE || place1 == ConsonantProperty.AFFRICATE || place1 == ConsonantProperty.FRICATIVE) &&
			(place2 == ConsonantProperty.PLOSIVE || place2 == ConsonantProperty.AFFRICATE || place2 == ConsonantProperty.FRICATIVE) &&
			(voice1 == ConsonantProperty.VOICED  || voice1 == ConsonantProperty.VOICELESS) &&
			(voice2 == ConsonantProperty.VOICED  || voice2 == ConsonantProperty.VOICELESS) &&
			(voice1 != voice2))
			{
				return true;
			}
		
		return false;
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
		
		ArrayList<Follower> interludes;	// for vowels, the interlude field serve to describe hiatus
		
		double onsetInitialProminence;
		double onsetClusterLeadProminence;
		double onsetClusterFollowProminence;
		double codaInitialProminence;
		double codaClusterLeadProminence;
		double codaClusterFollowProminence;
		double nucleusLeadProminence;
		double nucleusFollowProminence;
		double interludeLeadProminence;		
		double interludeFollowProminence;
		
		public Phoneme(Segment segment)
		{
			this.segment = segment;
			interludes = new ArrayList<Follower>();
			
			// Consonant case
			if (segment.isConsonant())
			{				
				onsetInitialProminence       = 1;
				codaInitialProminence		 = 1;
				onsetClusterLeadProminence   = 1;
				onsetClusterFollowProminence = 1;
				codaClusterLeadProminence    = 1;
				codaClusterFollowProminence  = 1;
				interludeLeadProminence  	 = 1;
				interludeFollowProminence 	 = 1;
				
				// Add inhibitors
				if (segment.expression.equals("ng"))
				{
					onsetInitialProminence    -= onsetNgInhibitor;
					interludeFollowProminence -= onsetNgInhibitor;
				}
				
//				if (segment.expression.equals("sh") || segment.expression.equals("zh"))
//				{
//					
//				}
				
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
					
					// coda cluster properties
					if (maxCodaLength > 1)
					{
						deviance = codaClusterLeadProminences[s.ordinal()] - 1;
						deviance /= Math.sqrt(segment.properties.length);
						codaClusterLeadProminence += deviance;
						
						deviance = codaClusterFollowProminences[s.ordinal()] - 1;
						deviance /= Math.sqrt(segment.properties.length);
						codaClusterFollowProminence += deviance;
						
						deviance = interludeLeadProminences[s.ordinal()] - 1;
						deviance /= Math.sqrt(segment.properties.length);
						interludeLeadProminence += deviance;
						
						deviance = interludeFollowProminences[s.ordinal()] - 1;
						deviance /= Math.sqrt(segment.properties.length);
						interludeFollowProminence += deviance;
					}
				}
				
				System.out.printf("%s\t%.3f\t%.3f\t%.3f\t%.3f\t%.3f\t%.3f\t%.3f\t%.3f\n", segment.expression,
									onsetInitialProminence,  onsetClusterLeadProminence, onsetClusterFollowProminence,
									codaInitialProminence,   codaClusterLeadProminence,  codaClusterFollowProminence,
									interludeLeadProminence, interludeFollowProminence);
			}
			
			
			// Vowel case
			else
			{
				
				onsetInitialProminence		= 1;	// this functions as the nucleus initial prominence here
				nucleusLeadProminence 		= 1;
				nucleusFollowProminence 	= 1;
				interludeLeadProminence  	= 1;
				interludeFollowProminence 	= 1;
				
				for (VowelProperty s : ((Vowel) segment).properties)
				{
					double deviance = vowelProminences[s.ordinal()] - 1;
					deviance /= Math.sqrt(segment.properties.length);
					onsetInitialProminence += deviance;

					deviance = interludeLeadProminences[s.ordinal()] - 1;
					deviance /= Math.sqrt(segment.properties.length);
					interludeLeadProminence += deviance;
					
					deviance = interludeFollowProminences[s.ordinal()] - 1;
					deviance /= Math.sqrt(segment.properties.length);
					interludeFollowProminence += deviance;
					
					if (maxNucleusLength > 1)
					{
						deviance = diphthongLeadProminences[s.ordinal()] - 1;
						deviance /= Math.sqrt(segment.properties.length);
						nucleusLeadProminence += deviance;
						
						deviance = diphthongFollowProminences[s.ordinal()] - 1;
						deviance /= Math.sqrt(segment.properties.length);
						nucleusFollowProminence += deviance;	
					}
					else
					{
						nucleusLeadProminence = 0;
						nucleusFollowProminence = 0;
					}
				}
				
//				System.out.printf("%s\t%.3f\t%.3f\t%.3f\n", segment.expression, onsetInitialProminence, nucleusLeadProminence, nucleusFollowProminence);
			}
		}
		
		// Interlude is only added if it has a nonzero chance of appearing
		public void addInterlude(Phoneme p)
		{
			double probability = p.interludeFollowProminence + p.onsetInitialProminence - 1;
			
			if (segment.isConsonant() && isDissonantNasalCluster(this, p))
				probability -= nasalDissonanceInhibitor;
			
			if (probability > 0 && interludeLeadProminence > 0)
				interludes.add(new Follower(p, probability));
			
//			Print interlude statistics
//			System.out.printf("%.3f (%.3f + %,3f)", probability, p.interludeFollowProminence, p.onsetInitialProminence);
		}
		
		public void normalizeAndSortInterludes()
		{
			double total = 0;
			
			for (Follower f : interludes)
				total += f.probability;
			
			for (Follower f : interludes)
				f.probability = f.probability /= total;
			
			Collections.sort(interludes);
			Collections.reverse(interludes);
		}
		
		public void printInterludes()
		{
			System.out.print(segment.expression + ":\t");
			
			if (interludes.size() == 0)
				System.out.println("none");
			else
			{
				for (Follower first : interludes)
					System.out.printf("%s%s (%.3f)\t", segment.expression, first.p.segment.expression, first.probability);
				System.out.println();
			}
		}
		
		class Follower implements Comparable<Follower>
		{
			Phoneme p;
			double probability;
			
			public Follower(Phoneme p, double probability)
			{
				this.p = p;
				this.probability = probability;
			}
			
			public int compareTo(Follower f)
			{
				if (probability > f.probability)
					return 1;
				else if (probability < f.probability)
					return -1;
				return 0;
			}
		}
	}

	class SyllableSegment implements Comparable<SyllableSegment>
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
		
		// Copy constructor
		public SyllableSegment(SyllableSegment other)
		{
			this.type = other.type;
			this.content = other.content;
			this.prominence = other.prominence;
		}
		
		public String toString()
		{
			String result = "";
			for (Phoneme p : content)
				result += p.segment.expression;
			return result;
		}

		public int compareTo(SyllableSegment ss)
		{
			if (prominence > ss.prominence)
				return 1;
			else if (prominence < ss.prominence)
				return -1;
			return 0;
		}
	}
}


enum SegmentType { ONSET, NUCLEUS, CODA, INTERLUDE; }