package Gengen_v2.gengenv2;


import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Timer;

import org.apache.commons.math3.distribution.LogNormalDistribution;

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
	
	double[] onsetClusterLengthProbabilities;
	double[] codaClusterLengthProbabilities;
	
	// Phonotactic properties
	int maxOnsetLength, maxNucleusLength, maxCodaLength, clusterBonus;
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
	double baseOnsetClusterChance;	// ratio of clusters of length N to those of length N-1 (N >= 2) in onsets, before scaling
	double baseDiphthongChance;		// as above, but for nuclei
	double baseCodaClusterChance;	// for codas
	double simpleOnsetProbability;	// overall probability of having an onset cluster vs. a simple onset
	double simpleCodaProbability;	// overall probability of having a coda cluster vs. a simple coda
	
	double onsetClusterInhibitor;	// reduces the diversity of clusters appearing w/in a language
	double diphthongInhibitor;		// as above, but for diphthongs
	double codaClusterInhibitor;	// for codas
	
	double codaInhibitor;		// flat value subtracted from every coda prominence value to reflect (usually) decreased richness
								// in coda inventory
	double codaDisturbance;		// stdev for disturbance of codas, AS A PERCENTAGE of the prominenceStdev. at 0, the coda values
								// are undisturbed compared to the onset ones. at 1, they have essentially been rerolled.
	
	double hiatusBonus;		// increases (or decreases) the probability of hiatus occurring between 2 nuclei
	double interludeBonus;	// increases (or decreases) the porbability of interludes forming between a coda and an onset

	
	// Flowchart control variables
	protected double strongHeavyRimeChance;
	protected double strongLightRimeChance;
	protected double weakHeavyRimeChance;
	protected double weakLightRimeChance;
	protected double baseOnsetChance;
	protected double codaChance;
	protected double codaLocationBalance;
	protected double medialCodaChance;
	protected double terminalCodaChance;
	
	
	// Phonotactic inhibitors
	double onsetNgInhibitor;			// reduces the chance of a onset 'ng'
	double onsetTlDlInhibitor; 			// reduces the chances of an onset 'tl' or 'dl'
	double nasalDissonanceInhibitor;	// reduces the prevalence of coda nasal-plosive clusters that disagree in articulation
	double unequalVoicingInhibitor;		// reduces the prevalence of interludes that disagree in voicing
	
	// Generator properties
	static double emptyInitialOnsetProminenceMean    = 0.3;
	static double emptyInitialOnsetProminenceStdev   = 0.15;
	static double onsetClusterProminenceMean	 	 = 0.1;
	static double onsetClusterProminenceStdev 		 = 0.05;
	
	static double emptyTerminalCodaProminenceMean = 0.5;
	static double emptyTerminalCodaProminenceStdev = 0.5;
	
	static double minimumOnsetClusterProminence   = 0.01; // minimum value for onsetClusterProminence 
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
	
	static double strongHeavyRimeChanceMean = 0.8;
	static double strongHeavyRimeChanceStdev = 0.2;
	static double weakHeavyRimeChanceMean = 0.3;
	static double weakHeavyRimeChanceStdev = 0.15;
	static double codaChanceMean  = 0.15;
	static double codaChanceStdev = 0.25;
	static double codaLocationBalanceMean = 0.4;
	static double codaLocationBalanceStdev = 0.33;
	static double onsetChanceMean = 0;
	static double onsetChanceStdev = 0.1;
	static double onsetChanceOffset = 0.8;
	
	// statistics data
	int[] data = new int[11];
	
	static int[] persistentData = new int[11];

	static final int SIMPLE_ONSETS				=  0;
	static final int COMPLEX_ONSETS				=  1;
	static final int SIMPLE_NUCLEI				=  2;
	static final int COMPLEX_NUCLEI				=  3;
	static final int SIMPLE_CODAS				=  4;
	static final int COMPLEX_CODAS				=  5;
	static final int SIMPLE_NUCLEI_WITH_HIATUS	=  6;
	static final int COMPLEX_NUCLEI_WITH_HIATUS	=  7;
	static final int COMPOUND_INTERLUDES		=  8;
	static final int LIGHT_RIMES				=  9;
	static final int HEAVY_RIMES				= 10;
	
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

//		Print inventories
//		System.out.println();
//		for (int i = 0; i < maxOnsetLength; i++ )
//			printInventory(onsets[i]);
//		for (int i = 0; i < maxCodaLength; i++ )
//			printInventory(codas[i]);
//		for (int i = 0; i < maxNucleusLength; i++ )
//			printInventory(nuclei[i]);
		
		data = gatherStatistics();
		setClusterChances();
		setFlowControlVariables();		
		
		
//		printInterludes(nuclei[0]);
//		if (codas.length > 0)
//			printInterludes(codas[0]);
		
		Flowchart f = new Flowchart(this);
		for (int i = 0; i < 50; i++)
			f.makeWord();
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
//		maxOnsetLength = 4;
		
		// Initialize onset array(s)
		onsets = new ArrayList[maxOnsetLength];
		for (int i = 0; i < onsets.length; i++)
			onsets[i] = new ArrayList<SyllableSegment>();
		
		if (maxOnsetLength > 0)
		{
			baseOnsetClusterChance = Math.max(rng.nextGaussian() * onsetClusterProminenceStdev + onsetClusterProminenceMean,
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
			baseDiphthongChance = Math.max(rng.nextGaussian() * 0.05 + 0.15, minimumNucleusClusterProminence);
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
//		maxCodaLength = 3;
		
		// Initialize coda arrays
		codas = new ArrayList[maxCodaLength];
		for (int i = 0; i < codas.length; i++)
			codas[i] = new ArrayList<SyllableSegment>();
		
		if (maxCodaLength > 0)
		{
			baseCodaClusterChance = Math.max(rng.nextGaussian() * 0.1 + 0.25, minimumCodaClusterProminence);
			codaClusterInhibitor = rng.nextGaussian() * 0.25 + 0.5;
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
		interludeLeadProminences =   new double[ConsonantProperty.values().length];
		interludeFollowProminences = new double[ConsonantProperty.values().length];
		
		// Determine prominence for consonant categories as cluster leads
		for (int i = 0; i < ConsonantProperty.values().length; i++)
			if (prominences[i] > 0)
			{
				interludeLeadProminences[i]   = rng.nextGaussian() * clusterLeadStdev   + 1;
				interludeFollowProminences[i] = rng.nextGaussian() * clusterFollowStdev + 1;
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
		if (maxOnsetLength > 0)
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
		if  (maxNucleusLength > 0)
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
		if (maxCodaLength > 0)
			for ( ; maxCodaLength > 0 && codas[maxCodaLength - 1].size() == 0; maxCodaLength--);
		
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
					p1.addInterlude(nuclei[0].get(j));
				
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
						p1.addInterlude(onsets[0].get(j));
					
//					System.out.println();
				}
			}
			
			// Normalize and sort values
			p1.normalizeAndSortInterludes();
		}
		
		// TODO: Temporary feature: remove codas with no interludes.
		// In the future, we may want to include these phonemes in the terminal coda, but those will be
		// handled by a separate inventory.
		for (int i = 0; i < codas[0].size(); i++)
			if (codas[0].get(i).content[0].interludes[0].isEmpty())
			{
				codas[0].remove(i);
				i--;
			}
				
		
		System.out.println("Interlude bonus: " +  interludeBonus);
	}
		
	
	public int[] gatherStatistics()
	{
		int[] results = new int[11];
		
		// Count all simple and complex onsets
		results[SIMPLE_ONSETS] += onsets[0].size();
		for (int i = 1; i < onsets.length; i++)
			results[COMPLEX_ONSETS] += onsets[i].size();
		
		// Count all simple and complex nuclei, and separately count those with hiatus
		for (int i = 0; i < nuclei.length; i++)
		{
			if (i == 0)
				results[SIMPLE_NUCLEI] += nuclei[0].size();
			else
				results[COMPLEX_NUCLEI] += nuclei[i].size();
		
			for (SyllableSegment ss : nuclei[i])
				if (!ss.content[ss.content.length - 1].interludes[0].isEmpty())
					if (i == 0)
						results[SIMPLE_NUCLEI_WITH_HIATUS]++;
					else
						results[COMPLEX_NUCLEI_WITH_HIATUS]++;
		}
		
		// Count all simple and complex codas, and compound interludes
		for (int i = 0; i < codas.length; i++)
		{
			if (i == 0)
				results[SIMPLE_CODAS] += codas[i].size();
			else
				results[COMPLEX_CODAS] += codas[i].size();
			for (SyllableSegment ss : codas[i])
			{
				Phoneme last = ss.content[ss.content.length - 1];
				
				for (ArrayList interludeList : last.interludes)
					results[COMPOUND_INTERLUDES] += interludeList.size();
			}
		}
		
		// Add up light and heavy rimes
		results[LIGHT_RIMES]  = results[SIMPLE_NUCLEI] * results[SIMPLE_ONSETS]
							  +	results[SIMPLE_NUCLEI_WITH_HIATUS];
		
		results[HEAVY_RIMES]  = results[SIMPLE_NUCLEI] * results[COMPLEX_ONSETS]
//							  + results[SIMPLE_NUCLEI] * results[COMPOUND_INTERLUDES] 
							  + results[SIMPLE_NUCLEI] * (results[SIMPLE_CODAS] + results[COMPLEX_CODAS])
							  + results[COMPLEX_NUCLEI_WITH_HIATUS]
							  + results[COMPLEX_NUCLEI] * results[SIMPLE_ONSETS]
							  + results[COMPLEX_NUCLEI] * results[COMPLEX_ONSETS]
//						      + results[COMPLEX_NUCLEI] * results[COMPOUND_INTERLUDES];
							  + results[COMPLEX_NUCLEI] * (results[SIMPLE_CODAS] + results[COMPLEX_CODAS]);
		
		// Print the results, if desired
		System.out.printf("Onsets:\t%d (%d simple, %d complex)\n", (results[SIMPLE_ONSETS] + results[COMPLEX_ONSETS]), 
																	results[SIMPLE_ONSETS] , results[COMPLEX_ONSETS]);
		System.out.printf("Nuclei:\t%d (%d simple, %d complex)\n", (results[SIMPLE_NUCLEI] + results[COMPLEX_NUCLEI]),
																	results[SIMPLE_NUCLEI] , results[COMPLEX_NUCLEI]);
		System.out.printf("Codas:\t%d (%d simple, %d complex)\n",  (results[SIMPLE_CODAS ] + results[COMPLEX_CODAS ]),
																	results[SIMPLE_CODAS ] , results[COMPLEX_CODAS ]);
		System.out.printf(" Simple nuclei with hiatus:\t%d\n", results[ SIMPLE_NUCLEI_WITH_HIATUS]);
		System.out.printf("Complex nuclei with hiatus:\t%d\n", results[COMPLEX_NUCLEI_WITH_HIATUS]);
		System.out.printf("Complex interludes:\t%d\n", results[COMPOUND_INTERLUDES]);
		System.out.printf("Light rimes:\t%d\n", results[LIGHT_RIMES]);
		System.out.printf("Heavy rimes:\t%d\n", results[HEAVY_RIMES]);
		
		// Copy results to tally
		for (int i = 0; i < results.length; i++)
			persistentData[i] += results[i];
		
		return results;
	}

	// TODO: i am way too tired to explain the math here right now but the idea is that the prominence value for each list of length
	// x is scaled by the log of the number of items in it and then its proportion of the total is deducted
	public void setClusterChances()
	{
		if (maxOnsetLength >= 2)
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
								  Math.log((onsets[i+1].size() + 1) * (Math.pow(remainingTotal + 1, baseOnsetClusterChance)));
					
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
				onsetClusterLengthProbabilities[onsetClusterLengthProbabilities.length - 1] =
						(1 - baseOdds[onsetClusterLengthProbabilities.length - 2]) * remainingProportion;
			}
			
			// Set overall probability of having a cluster vs a simple onset
			simpleOnsetProbability = Math.log(data[SIMPLE_ONSETS] + 1) /
						  		     Math.log((data[SIMPLE_ONSETS] + 1) * (Math.pow(data[COMPLEX_ONSETS] + 1, baseOnsetClusterChance)));
			
			for(int i = 0; i < onsetClusterLengthProbabilities.length; i ++)
				System.out.println(onsetClusterLengthProbabilities[i]);
			
			double total = 0;
			for (double o : onsetClusterLengthProbabilities)
				total += o;
			System.out.println("total\t" + total);
		}
		
		// Repeat the process for codas
		if (maxCodaLength >= 2)
		{
			codaClusterLengthProbabilities = new double[maxCodaLength - 1];
			double[] baseOdds = new double[maxCodaLength - 1];
			
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
								  Math.log((codas[i+1].size() + 1) * (Math.pow(remainingTotal + 1, baseCodaClusterChance)));
					
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
			
			int total = 0;
			for (double o : codaClusterLengthProbabilities)
				total += o;
			System.out.println("total\t" + total);
			
			// Set overall probability of having a coda cluster vs. a simple coda 
			simpleCodaProbability = Math.log(data[SIMPLE_CODAS] + 1) /
									Math.log((data[SIMPLE_CODAS] + 1) * (Math.pow(data[COMPLEX_CODAS] + 1, baseCodaClusterChance)));
		}
		
		// Repeat the process for each nucleus and coda
		for (SyllableSegment ss : nuclei[0])
			ss.content[0].setInterludeClusterChance();
		if (maxCodaLength > 0)
			for (SyllableSegment ss : codas[0])
				ss.content[0].setInterludeClusterChance();
	}
	
	public void setFlowControlVariables()
	{
		// strong 
		strongHeavyRimeChance = rng.nextGaussian() * strongHeavyRimeChanceStdev + strongHeavyRimeChanceMean;
		strongHeavyRimeChance = Math.max(Math.min(strongHeavyRimeChance, 1), 0);
		
		strongLightRimeChance = (1 - strongHeavyRimeChance);
		
		
		double total = strongHeavyRimeChance + strongLightRimeChance;
		strongHeavyRimeChance /= total;
		strongLightRimeChance /= total;
		
		// weak
		weakHeavyRimeChance = rng.nextGaussian() * weakHeavyRimeChanceStdev + weakHeavyRimeChanceMean;
		weakHeavyRimeChance = Math.max(Math.min(weakHeavyRimeChance, 1), 0);
		
		weakLightRimeChance = (1 - weakHeavyRimeChance);
		
		total = weakHeavyRimeChance + weakLightRimeChance;
		weakHeavyRimeChance /= total;
		weakLightRimeChance /= total;
		
		// Print
		System.out.printf("strong:\tHeavy %.3f\n", strongHeavyRimeChance);
		System.out.printf("\t\tLight %.3f\n",  strongLightRimeChance);
		System.out.printf("weak:\tHeavy %.3f\n", weakHeavyRimeChance);
		System.out.printf("\t\tLight %.3f\n",  weakLightRimeChance);
		
		
		
		
		if (maxCodaLength > 0)
			codaChance = new LogNormalDistribution(codaChanceMean, codaChanceStdev).sample();
		else
			codaChance = 0;
		codaLocationBalance = rng.nextGaussian() * codaLocationBalanceStdev + codaLocationBalanceMean;
		
		codaLocationBalance = Math.max(Math.min(codaLocationBalance, 1), 0);
		
		medialCodaChance =   Math.max(Math.min(codaChance * codaLocationBalance, 1), 0);
		terminalCodaChance = Math.max(Math.min(codaChance * (1 - codaLocationBalance), 1), 0);
		
		System.out.printf("Base coda chance\t%.3f\n", codaChance / 2);
		System.out.printf("Coda location balance\t%.3f\n", codaLocationBalance);
		System.out.printf("Medial coda chance\t%.3f\n", medialCodaChance);
		System.out.printf("Terminal coda chance\t%.3f\n", terminalCodaChance);
		
		if (data[SIMPLE_NUCLEI_WITH_HIATUS] > 0)
			baseOnsetChance = 1 - (new LogNormalDistribution(onsetChanceMean, onsetChanceStdev).sample() - onsetChanceOffset);
		else
			baseOnsetChance = 1;
		
		baseOnsetChance = Math.max(Math.min(baseOnsetChance, 1), 0);
		System.out.printf("Onset chance\t\t%.3f\n", baseOnsetChance);
	}
	
	// Returns an onset of any length
	public SyllableSegment pickOnset()
	{
		if (maxOnsetLength == 1 || rng.nextDouble() < simpleOnsetProbability)
			return pickSimpleOnset();
		else
			return pickComplexOnset();
	}
	
	// Returns an onset of length 1
	public SyllableSegment pickSimpleOnset()
	{
		return pickSyllableSegment(onsets[0]);
	}
	
	// Returns an onset of length 2 or more
	public SyllableSegment pickComplexOnset()
	{
		pickSyllableSegment(onsets[1 + pickClusterLength(onsetClusterLengthProbabilities)]);
		System.err.println("Failed to select onset cluster!");
		System.exit(0);
		return null;
	}
	
	// Returns a nucleus of length 1
	public SyllableSegment pickSimpleNucleus()
	{
		return pickSyllableSegment(nuclei[0]);
	}
	
	// Returns a nucleus of length 2
	public SyllableSegment pickComplexNucleus()
	{
		return pickSyllableSegment(nuclei[1]);
	}
	
	// Returns a coda of any length
	public SyllableSegment pickCoda()
	{
		if (maxCodaLength == 1 || rng.nextDouble() < simpleCodaProbability)
			return pickSimpleCoda();
		else
			return pickComplexCoda();
	}
	
	// Returns a coda of length 1
	public SyllableSegment pickSimpleCoda()
	{
		return pickSyllableSegment(codas[0]);
	}
	
	// Returns a coda of length 2 or more
	public SyllableSegment pickComplexCoda()
	{
		pickSyllableSegment(codas[1 + pickClusterLength(codaClusterLengthProbabilities)]);
		System.err.println("Failed to select coda cluster!");
		System.exit(0);
		return null;
	}
	
	// Returns a random follower from a given consonant Phoneme's interlude list
	public SyllableSegment pickInterlude(Phoneme p)
	{
		if (maxOnsetLength == 1)
			return p.pickInterlude(0);
		else
			return p.pickInterlude(pickClusterLength(p.interludeLengthProbabilities));
	}
	
	// The math here is simple. Having normalized them already, the prominence values of every inventory list sum to 1.
	// We generate a random number between 1 and 0 and subtract prominence values in order (the  lists are sorted largest
	// to smallest) until we reach a number lower than 0. The syllable segment whose prominence value took us over the edge
	// is returned.
	private SyllableSegment pickSyllableSegment(ArrayList<SyllableSegment> inventory)
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
	
	// Picks an index from an array of doubles (corresponding to a list of probabilities for clusters of different lengths).
	// The sum of all entries in the array is assumed to equal one. The probability of an index being returned should therefore
	// be equal to its content.
	private int pickClusterLength(double[] probabilities)
	{
		// Select length of onset
		double rand = rng.nextDouble();
		for (int i = 0 ; i < probabilities.length; i++)
		{
			if (rand < probabilities[i])
				return i + 1;
			else
				rand -= probabilities[i];
		}
		
		System.err.println("pickClusterLength() returned -1");
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
	
	static public void massGatherStats(int total)
	{
		long startTime = System.nanoTime();
		
		PrintStream original = System.out;
		PrintStream dummy = new PrintStream(new OutputStream() { public void write(int b) {} });
		System.setOut(dummy);
		
		for (int i = 0; i < total; i++)
			new Phonology();
		
		System.setOut(original);
		
		long endTime = System.nanoTime();
		double time = (endTime - startTime) / (total * 1000000);
		
		System.out.println("AVERAGE\tSIMPLE\tCOMPLEX");
		System.out.println("ONSETS\t" + (persistentData[SIMPLE_ONSETS] / total) + "\t" + (persistentData[COMPLEX_ONSETS] / total));
		System.out.println("NUCLEI\t" + (persistentData[SIMPLE_NUCLEI] / total) + "\t" + (persistentData[COMPLEX_NUCLEI] / total));
		System.out.println("CODAS \t" + (persistentData[SIMPLE_CODAS]  / total) + "\t" + (persistentData[COMPLEX_CODAS]  / total));
		System.out.println("HIATUS\t" + (persistentData[SIMPLE_NUCLEI_WITH_HIATUS]  / total) + "\t" 
									  + (persistentData[COMPLEX_NUCLEI_WITH_HIATUS]  / total));
		System.out.println("COMPOUND INTERLUDES\t" + persistentData[COMPOUND_INTERLUDES] / total);
		System.out.println("LIGHT RIMES\t" + persistentData[LIGHT_RIMES] / total);
		System.out.println("HEAVY RIMES\t" + persistentData[HEAVY_RIMES] / total);
		
		System.out.println("Average time per language: " + time + "ms");
	}
	
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
		
		ArrayList<Follower>[] interludes;	// for vowels, the interlude field serve to describe hiatus
		double[] interludeLengthProbabilities;
		
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
			
			// Consonant case
			if (segment.isConsonant())
			{
				interludes = new ArrayList[maxOnsetLength];
				for (int i = 0; i < maxOnsetLength; i++)
					interludes[i] = new ArrayList<Follower>();
				
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
				interludes = new ArrayList[maxNucleusLength];
				for (int i = 0; i < maxNucleusLength; i++)
					interludes[i] = new ArrayList<Follower>();

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
		public void addInterlude(SyllableSegment ss)
		{
			// if interludeLeadProminence <= 0, do nothing
			if (interludeLeadProminence <= 0)
				return;
			
			// Calculate base probability from following segment's interludeFollow and onsetInitial prominences
			double probability = ss.content[0].interludeFollowProminence + ss.content[0].onsetInitialProminence - 1;
			
			// Apply nasal dissonance inhibitor, if relevant
			if (segment.isConsonant() && isDissonantNasalCluster(this, ss.content[0]))
				probability -= nasalDissonanceInhibitor;
			
			// If probability is positive, add this interlude
			if (probability > 0)
				interludes[0].add(new Follower(ss, probability));
			
			// Add any possible clusters, too. Examine onset clusters if this is a consonantal interlude ...
			if (segment.isConsonant())
				for (int i = 1; i < maxOnsetLength; i++)
				{
					for (SyllableSegment onset : onsets[i])
						if (onset.content[0] == ss.content[0])
						{
							// Set base probability equal to the next segment's prominence
							probability = onset.prominence;
							
							// Apply nasal dissonance inhibitor, if relevant
							if (isDissonantNasalCluster(this, onset.content[0]))
								probability -= nasalDissonanceInhibitor;
							
							// If probability is positive, add this interlude
							if (probability > 0)
								interludes[i].add(new Follower(onset, probability));
						}
				}
			// ... or if this is a hiatus, look at diphthongs
			else if (maxNucleusLength == 2)
				for (SyllableSegment diphthong : nuclei[1])
					if (diphthong.content[0] == ss.content[0])
					{
						// Set base probability equal to the next segment's prominence
						probability = diphthong.prominence;
						
						// If probability is positive, add this interlude
						if (probability > 0)
							interludes[1].add(new Follower(diphthong, probability));
					}
			
//			Print interlude statistics
//			System.out.printf("%.3f (%.3f + %,3f)", probability, p.interludeFollowProminence, p.onsetInitialProminence);
		}
		
		public void setInterludeClusterChance()
		{
			// Obtain true length of longest followers allowed
			int maxFollowerLength = interludes.length;
			for ( ; maxFollowerLength > 0 && interludes[maxFollowerLength - 1].size() == 0; maxFollowerLength--);

			if (maxFollowerLength == 0)
				return;
			
			double clusterProminence;
			if (segment.isConsonant())
				clusterProminence = baseOnsetClusterChance;
			else
				clusterProminence = baseDiphthongChance;
			
			// Unlike the general onset/nucleus/coda inventories, these probabilities include the chance of
			// a result of length 1.
			interludeLengthProbabilities = new double[maxFollowerLength];
			double[] baseOdds = new double[maxFollowerLength];
			
			if (maxFollowerLength == 1)
				interludeLengthProbabilities[0] = 1;
			else
			{
				for (int i = 0; i < maxFollowerLength - 1; i++)
				{
					// Number of followers of greater length than this one
					int remainingTotal = 0;
					for (int j = i + 1; j < maxFollowerLength; j++)
						remainingTotal += interludes[j].size();
					
					// Proportion of followers of this length relative to those of greater (out of 1)
					double base = Math.log(interludes[i].size() + 1) /
								  Math.log((interludes[i].size() + 1) * (Math.pow(remainingTotal + 1, clusterProminence)));
					
					// Proportion of all followers of this length or longer
					double remainingProportion = 1;
					for (int j = 0; j < i; j++)
						remainingProportion -= interludeLengthProbabilities[j];
					
					interludeLengthProbabilities[i] = base * remainingProportion;
				}
				
				double remainingProportion = 1;
				for (int j = 0; j < interludeLengthProbabilities.length - 1; j++)
					remainingProportion -= interludeLengthProbabilities[j];
				
				// Basically the complement to all previous odds
				interludeLengthProbabilities[interludeLengthProbabilities.length - 1] =
						(1 - baseOdds[interludeLengthProbabilities.length - 2]) * remainingProportion;
			}
		}
		
		public SyllableSegment pickInterlude(int length)
		{
			double rand = rng.nextDouble();
			for (Follower f : interludes[length])
			{
				if (rand < f.probability)
					return f.ss;
				else
					rand -= f.probability;
			}
			
			System.err.println("Failed to select follower!");
			System.exit(0);
			
			return null;
		}
		
		public void normalizeAndSortInterludes()
		{
			for (ArrayList<Follower> interludeSet : interludes)
			{
				double total = 0;
				
				for (Follower f : interludeSet)
					total += f.probability;
				
				for (Follower f : interludeSet)
					f.probability = f.probability /= total;
				
				Collections.sort(interludeSet);
				Collections.reverse(interludeSet);
			}
		}
		
		public void printInterludes()
		{
			System.out.println("~~~" + segment.expression + "~~~");
			
			if (interludes[0].size() == 0)
				System.out.println("none");
			else
			{
				for (int i = 0; i < interludes.length; i++)
				{
					if (!interludes[i].isEmpty())
					{
						System.out.printf("[%.3f]\t", interludeLengthProbabilities[i]);
						for (Follower first : interludes[i])
							System.out.printf("%s%s (%.3f)\t", segment.expression, first.ss, first.probability);
						System.out.println();						
					}
				}
			}
		}
		
		class Follower implements Comparable<Follower>
		{
			SyllableSegment ss;
			double probability;
			
			public Follower(SyllableSegment ss, double probability)
			{
				this.ss = ss;
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
		
		public Phoneme lastPhoneme()
		{
			return content[content.length - 1];
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


enum SegmentType { ONSET, NUCLEUS, CODA; }