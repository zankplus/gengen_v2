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
	
	ArrayList<SyllableSegment>[] onsets, nuclei, codas, interludes; 
	
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
	
	double onsetClusterRatio;	// ratio of clusters of length N to those of length N-1 (N >= 2) in onsets
	double diphthongRatio;		// as above, but for nuclei
	double codaClusterRatio;	// for codas
	
	double onsetClusterInhibitor;	// reduces the diversity of clusters appearing w/in a language
	double diphthongInhibitor;		// as above, but for diphthongs
	double codaClusterInhibitor;	// for codas
	
	double codaInhibitor;		// flat value subtracted from every coda prominence value to reflect (usually) decreased richness
								// in coda inventory
	double codaDisturbance;		// stdev for disturbance of codas, AS A PERCENTAGE of the prominenceStdev. at 0, the coda values
								// are undisturbed compared to the onset ones. at 1, they have essentially been rerolled.
	
	// Phonotactic inhibitors
	double onsetNgInhibitor;			// reduces the chance of a onset 'ng'
	double onsetTlDlInhibitor; 			// reduces the chances of an onset 'tl' or 'dl'
	double nasalDissonanceInhibitor;	// reduces the prevalence of coda nasal-plosive clusters that disagree in articulation
	
	
	// Generator properties
	static double minimumOnsetClusterRatio   = 0.04; // minimum value for onsetClusterRatio 
	static double minimumNucleusClusterRatio = 0.04; // " " " diphthongRatio
	static double minimumCodaClusterRatio 	 = 0.04; // " " " codaClusterRatio
	
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
	
	static int simpleOnsets = 0, simpleNuclei = 0, simpleCodas = 0, complexOnsets = 0, complexNuclei = 0, complexCodas = 0;
	
	public Phonology()
	{
		rng = new Random();
		
		makeBasicSyllableStructure();
		determineProminence();
		selectSegments();
		makeOnsets();
//		makeNuclei();
		if (maxCodaLength > 0)
			makeCodas();
		makeInterludes();
		
//		System.out.println();
//		for (int i = 0; i < maxOnsetLength; i++ )
//			printInventory(onsets[i]);
//		for (int i = 0; i < maxCodaLength; i++ )
//			printInventory(codas[i]);
//		for (int i = 0; i < maxNucleusLength; i++ )
//			printInventory(nuclei[i]);
		
		countSyllableLengths();
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
			diphthongRatio = Math.max(rng.nextGaussian() * 0.05 + 0.15, minimumNucleusClusterRatio);
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
		maxCodaLength = 2;
		
		// Initialize coda arrays
		codas = new ArrayList[maxCodaLength];
		for (int i = 0; i < codas.length; i++)
			codas[i] = new ArrayList<SyllableSegment>();
		
		if (maxCodaLength > 0)
		{
			codaClusterRatio = Math.max(rng.nextGaussian() * 0.1 + 0.25, minimumCodaClusterRatio);
			codaClusterInhibitor = rng.nextGaussian() * 0.25 + 0.5;
		}
		
		//Roll for interlude
		if (maxCodaLength > 0)
		{
			maxInterludeLength = 2 + rng.nextInt(maxOnsetLength + maxCodaLength - 2);
			interludes = new ArrayList[maxInterludeLength];
			for (int i = 0; i < interludes.length; i++)
				interludes[i] = new ArrayList<SyllableSegment>();
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
		findAllOnsets();

		// Shrink maxOnsetCluster to hide empty categories
		for ( ; onsets[maxOnsetLength - 1].size() == 0; maxOnsetLength--);
		
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
		findAllNuclei();

		// Shrink maxNucleusCluster to hide empty categories
		for ( ; nuclei[maxNucleusLength - 1].size() == 0; maxNucleusLength--);
		
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
		findAllCodas();

		// Shrink maxOnsetCluster to hide empty categories
//		for ( ; codas[maxCodaLength - 1].size() == 0; maxCodaLength--);
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
	
	// Create the lists of all interludes
	public void makeInterludes()
	{
		for (int i = 0 ; i < codas.length; i++)
		{
			for (SyllableSegment coda : codas[i])
			{
				Phoneme p1 = coda.content[coda.content.length - 1];
				for (int j = 0 ; j < onsets.length; j++)
				{
//					if (i + j + 2 <= maxInterludeLength)
					for (SyllableSegment onset : onsets[j])
					{
						Phoneme p2 = onset.content[onset.content.length - 1];
						
						if (p1 != p2)
						{
							// Prominence for an interlude should be equal to 1 plus the sum of variances of:
							// 1. the prominence of the coda
							// 2. the interludeLeadProminence of the coda's last segment
							// 3. the interludeFollowProminence of the onset's first segment
							// 4. the prominence of the onset
							
							double prominence = coda.prominence + onset.prominence;
							prominence += p1.interludeLeadProminence;
							prominence += p2.interludeFollowProminence;
							prominence -= 3;
							
							// Penalize nasal dissonance
							if (isDissonantNasalCluster(p1, p2))
							{
								prominence -= nasalDissonanceInhibitor;
							}
							
							if (prominence > 0)
							{
								Phoneme[] content = new Phoneme[coda.content.length + onset.content.length];
								for (int m = 0; m < coda.content.length; m++)
									content[m] = coda.content[m];
								for (int m = 0; m < onset.content.length; m++)
									content[m + coda.content.length] = onset.content[m];
								
								SyllableSegment interlude = new SyllableSegment(SegmentType.INTERLUDE, content, prominence);
								interludes[i + j].add(interlude);
							}
						}
					}
				}
			}
		}
		
		
//		for ( ; interludes[maxInterludeLength - 1].size() == 0; maxInterludeLength--) System.out.println(maxInterludeLength);
		
		// DEBUG: Print all possible interludes
		for (int i = 0; i < maxInterludeLength; i++)
		{
			System.out.println("LENGTH " + (i + 2));
			for (SyllableSegment ss : interludes[i])
				System.out.printf("%s\t%.3f\n", ss, ss.prominence);
			System.out.println();
		}

	}
	
	public void countSyllableLengths()
	{
//		int terminalSimpleNuclei, terminalSimpleCodas, terminalComplexNuclei, terminalComplexCodas;
		
		// Count all simple and complex onsets, nuclei, and codas with positive prominence
		for (int i = 0; i < onsets.length; i++)
			for (SyllableSegment ss : onsets[i])
				if (ss.prominence > 0)
					if (i == 0)
						simpleOnsets++;
					else
						complexOnsets++;
		
		for (int i = 0; i < nuclei.length; i++)
			for (SyllableSegment ss : nuclei[i])
				if (ss.prominence > 0)
					if (i == 0)
						simpleNuclei++;
					else
						complexNuclei++;
		
		for (int i = 0; i < codas.length; i++)
			for (SyllableSegment ss : codas[i])
				if (ss.prominence > 0)
					if (i == 0)
						simpleCodas++;
					else
						complexCodas++;
		
//		System.out.printf("Onsets:\t%d (%d simple, %d complex)\n", (simpleOnsets + complexOnsets), simpleOnsets, complexOnsets);
//		System.out.printf("Nuclei:\t%d (%d simple, %d complex)\n", (simpleNuclei + complexNuclei), simpleNuclei, complexNuclei);
//		System.out.printf("Codas:\t%d (%d simple, %d complex)\n", (simpleCodas + complexCodas), simpleCodas, complexCodas);
	}
	
	public ArrayList<SyllableSegment>[] disturbOnsets (double mean, double stdev)
	{
		return null;
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
	
	private void findAllInterludes()
	{
		for (Phoneme p : cInv)
		{
			
		}
	}
	
	// start: the phonotactic category to start combing from
	private void findAllInterludes(ArrayList<Phoneme> coda, int start)
	{
		
	}

	public void printInventory(ArrayList<SyllableSegment> baseList)
	{
		double total = 0;	// total prominence
		
		ArrayList<SyllableSegment> list = new ArrayList<SyllableSegment>();
		
		// Calculate combined prominence while making a copy of the list
		for (SyllableSegment ss : baseList)
		{
			// Calculating combined prominence
			if (ss.prominence > 0)
			{
				total += ss.prominence;
				list.add(new SyllableSegment(ss));
			}
		}
		
		if (list.size() == 0)
			return;
		
		// Normalize values
		for (SyllableSegment ss : list)
			ss.prominence /= total;
		
		// Sort lists
		Collections.sort(list);
		Collections.reverse(list);
		
		// Print sorted lists
		System.out.println(list.get(0).type + " " + list.get(0).content.length);
		for (SyllableSegment ss : list)
			System.out.printf("%s\t%.3f\n", ss, (ss.prominence / 1));
		
		System.out.println();
	}
	
	static public void gatherStats(int total)
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
		double interludeLeadProminence;
		double interludeFollowProminence;
		
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
				onsetClusterLeadProminence   = 1;
				onsetClusterFollowProminence = 1;
				codaClusterLeadProminence    = 1;
				codaClusterFollowProminence  = 1;
				interludeLeadProminence  	 = 1;
				interludeFollowProminence 	 = 1;
				
				// Add inhibitors
				if (segment.expression.equals("ng"))
					onsetInitialProminence -= onsetNgInhibitor;
				
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
					
					// interlude properties
					if (maxInterludeLength >= 2)
					{
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

class CountingHashtable<K, V> extends Hashtable<K, V>
{
	int total;
	
	public CountingHashtable()
	{
		super();
		total = 0;
	}
}

enum SegmentType { ONSET, NUCLEUS, CODA, INTERLUDE; }