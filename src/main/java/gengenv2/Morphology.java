package gengenv2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import gengenv2.enums.Constraint;
import gengenv2.enums.SuffixType;
import gengenv2.structures.ConsonantPhoneme;
import gengenv2.structures.MorphemeEntry;
import gengenv2.structures.MorphemeLibrary;
import gengenv2.structures.NounClass;
import gengenv2.structures.PhonemeInstance;
import gengenv2.structures.Suffix;
import gengenv2.structures.VowelPhoneme;
import hiatusResolution.*;

public class Morphology
{
	double boundRootChance;
	double freeRootChance;
	
	Random rng;
	Phonology parent;
	
	MorphemeLibrary allSuffixes;
	NounClass[] nounClasses;
	
	HiatusResolutionMethod[] hiatusResolutionMethods;
	
	public Morphology(Phonology p)
	{
		parent = p;
		rng = PublicRandom.getRNG();
		allSuffixes = new MorphemeLibrary(true);
		
//		makeSuffixes();
//		generateNounClasses();
		
		generateHiatusResolutionMethods();
		rankHiatusResolutionConstraints();
	}
	
	private void generateHiatusResolutionMethods()
	{
		// Get references to mid-front and mid-back vowels (E and O)
		VowelPhoneme frontMidVowel = null, backMidVowel = null;
		for (VowelPhoneme vp : parent.vowelInventory)
			if (vp.segment.expression.equals("e"))
			{
				frontMidVowel = vp;
				break;
			}
		
		for (VowelPhoneme vp : parent.vowelInventory)
			if (vp.segment.expression.equals("o"))
			{
				backMidVowel = vp;
				break;
			}
		
		// Get references to Y- and W-glides
		ConsonantPhoneme yGlide = null, wGlide = null;
		for (ConsonantPhoneme cp : parent.consonantInventory)
			if (cp.segment.expression.equals("y"))
			{
				yGlide = cp;
				break;
			}
		for (ConsonantPhoneme cp : parent.consonantInventory)
			if (cp.segment.expression.equals("w"))
			{
				wGlide = cp;
				break;
			}
		
		
		
		// Get "least marked" consonant
		String[] leastMarkedConsonants = new String[] { "'", "h", "t", "y", "r", "w" };
		ConsonantPhoneme leastMarked = null;
		for (int i = 0; i < parent.medialOnsets.size() && leastMarked == null; i++)
		{
			for (int j = 0; j < leastMarkedConsonants.length && leastMarked == null; j++)
				if (parent.medialOnsets.getMembers().get(i).getContent().segment.expression.equals(leastMarkedConsonants[j]))
					leastMarked = (ConsonantPhoneme) parent.medialOnsets.getMembers().get(i).getContent();
		}
		
		// Initialize each method
		// Diaeresis
		Diaeresis diaeresis = new Diaeresis();
		
		// Diphthong Formation
		DiphthongFormation diphthongFormation = new DiphthongFormation();
		
		// Vowel Elision
		VowelElision vowelElision = new VowelElision();
		
		// Coalescence
		Coalescence coalescence = new Coalescence (frontMidVowel, backMidVowel, parent.medialNuclei.contains(frontMidVowel), parent.terminalNuclei.contains(frontMidVowel),
										parent.medialNuclei.contains(backMidVowel), parent.terminalNuclei.contains(backMidVowel));
		
		// Homorganic Glide Epenthesis
		boolean homorganicWithV1 = true;
		boolean homorganicWithV2 = false;
		boolean midVowelsTrigger = (rng.nextBoolean()); 
		boolean v1MustNotMatchV2 = (rng.nextBoolean());
		HomorganicGlideEpenthesis homorganicGlideEpenthesis = new HomorganicGlideEpenthesis(yGlide, wGlide, homorganicWithV1, homorganicWithV2, midVowelsTrigger, v1MustNotMatchV2);
		
		// Consistent Consonant Epenthesis
		ConsistentConsonantEpenthesis consistentConsonantEpenthesis = new ConsistentConsonantEpenthesis(leastMarked);
		
		// Glide formation
		boolean initialYAllowed = parent.initialNuclei != null ? yGlide != null && parent.initialNuclei.contains(yGlide) : false;
		boolean initialWAllowed = parent.initialNuclei != null ? wGlide != null && parent.initialNuclei.contains(yGlide) : false;
		boolean medialYAllowed = yGlide != null && parent.medialNuclei.contains(yGlide);
		boolean medialWAllowed = wGlide != null && parent.medialNuclei.contains(wGlide);
		boolean blockedBySameFrontness = rng.nextBoolean();
		GlideFormation glideFormation = new GlideFormation(yGlide, wGlide, initialYAllowed, initialWAllowed, medialYAllowed, medialWAllowed, midVowelsTrigger,
												blockedBySameFrontness, v1MustNotMatchV2);
		
		// Configure compensatory lengthening, if relevant
		vowelElision.configureCompensatoryLenghtening(true, parent.longVowel);
		glideFormation.configureCompensatoryLenghtening(true, parent.longVowel);
		coalescence.configureCompensatoryLenghtening(true, parent.longVowel);
		
		// Add to array
		hiatusResolutionMethods = new HiatusResolutionMethod[] { diaeresis, diphthongFormation, vowelElision, coalescence,
																	homorganicGlideEpenthesis, consistentConsonantEpenthesis, glideFormation };
		
	}
	
	public void rankHiatusResolutionConstraints()
	{
		
		ArrayList<Constraint> constraints = new ArrayList<Constraint>();
		for (Constraint c : Constraint.values())
			constraints.add(c);
		
		ArrayList<Constraint> ranked = new ArrayList<Constraint>();
		ArrayList<Constraint> rankedLowest = new ArrayList<Constraint>();
		
		
		// Create a randomly ranked list of constraints
		while (constraints.size() > 0)
		{
			int rand = rng.nextInt(constraints.size());
			Constraint target = constraints.remove(rand);
			
			if (target == Constraint.NUCLEI_MAX)
				rankedLowest.add(constraints.remove(constraints.indexOf(Constraint.NUCLEI_MIN)));
			else if (target == Constraint.NUCLEI_MIN)
				rankedLowest.add(constraints.remove(constraints.indexOf(Constraint.NUCLEI_MAX)));
			else if (target == Constraint.SYL_MAX)
				rankedLowest.add(constraints.remove(constraints.indexOf(Constraint.SYL_MIN)));
			else if (target == Constraint.SYL_MIN)
				rankedLowest.add(constraints.remove(constraints.indexOf(Constraint.SYL_MAX)));
			else if (target == Constraint.TIDY)
				rankedLowest.add(constraints.remove(constraints.indexOf(Constraint.ENCROACHING)));
			else if (target == Constraint.ENCROACHING)
				rankedLowest.add(constraints.remove(constraints.indexOf(Constraint.TIDY)));
			
			ranked.add(target);
		}
		
		for (int i = rankedLowest.size() - 1; i >= 0; i--)
			ranked.add(rankedLowest.remove(i));
		
		for (int i = 0; i < ranked.size(); i++)
			System.out.println((i + 1) + ": " + ranked.get(i));
		
		// Rank hiatus resolution methods according to how well they satisfy the constraints
		ArrayList<HiatusResolutionMethod> rankedMethods = new ArrayList<HiatusResolutionMethod>();
		
		for (HiatusResolutionMethod hrm : hiatusResolutionMethods)
		{
			int index = rankedMethods.size();
			
			for (int i = 0; i < rankedMethods.size() && index == rankedMethods.size(); i++)
			{
				for (Constraint c : ranked)
				{
					if (hrm.satisfiesConstraint(c) && !rankedMethods.get(i).satisfiesConstraint(c))
					{
						index = i;
						System.out.println(hrm + " > " + rankedMethods.get(i) + " on " + c);
						break;
					}
					else if (!hrm.satisfiesConstraint(c) && rankedMethods.get(i).satisfiesConstraint(c))
					{
						System.out.println(hrm + " < " + rankedMethods.get(i) + " on " + c);
						break;
					}
				}
			}
			
			rankedMethods.add(index, hrm);
		}
		
		System.out.println("Ranked methods:");
		for (int i = 0; i < rankedMethods.size(); i++)
			hiatusResolutionMethods[i] = rankedMethods.get(i);
		
		for (int i = 0; i < hiatusResolutionMethods.length; i++)
			System.out.println((i + 1) + ": " + hiatusResolutionMethods[i]);
	}
	
	public void generateNounClasses()
	{
		int qty = Math.min(Math.max(rng.nextInt(6) + rng.nextInt(6) - 2, 1), allSuffixes.size());
		generateNounClasses(qty);
	}
	
	public void generateNounClasses(int nounClassCount)
	{
		// Generate noun classes
		int qtyNounClasses = Math.max(rng.nextInt(6) + rng.nextInt(6) - 4, 1);
		int qtySuffixes = Math.max(qtyNounClasses * rng.nextInt(3) + rng.nextInt(6) + 1, qtyNounClasses);
		
		makeSuffixes(qtySuffixes);
		
		ArrayList<MorphemeLibrary> genders = new ArrayList<MorphemeLibrary>();
		ArrayList<Double> weights = generateWeights(qtyNounClasses, 0.25);
		
		
		
		System.out.println("Noun classes");
		
		for (int i = 0; i < weights.size(); i++)
		{
			genders.add(new MorphemeLibrary(false));
			System.out.println(weights.get(i));
		}
		
		// Make copy of suffixes list
		ArrayList<MorphemeEntry> suffixes = new ArrayList<MorphemeEntry>();
		for (MorphemeEntry se : allSuffixes.getLibrary())
			suffixes.add(se);
		
		double[] diff = new double[qtyNounClasses];
		for (int i = 0; i < diff.length; i++)
			diff[i] = weights.get(i);
		
		// Populate each noun class. for in suffix in descending order of probability, add it to the class with the highest 'diff'
		// (and then update the diff)
		while (suffixes.size() > 0)
		{
			double max = diff[0];
			int maxIndex = 0;
			
			for (int i = 1; i < diff.length; i++)
				if (diff[i] > diff[maxIndex])
				{
					max = diff[i];
					maxIndex = i;
				}
			
			MorphemeEntry se = suffixes.remove(0);
			genders.get(maxIndex).addMorpheme(se.getMorpheme(), se.getProbability());
			diff[maxIndex] -= se.getProbability();
		}
		
		for (int i = 0; i < genders.size(); i++)
		{
			System.out.println("Class " + (i + 1) + " (" + weights.get(i) + ", " + genders.get(i).size() + " members)");
			genders.get(i).normalize();
			genders.get(i).sort();
			genders.get(i).printMembers();
		}
		
		nounClasses = new NounClass[genders.size()];
		for (int i = 0; i < genders.size(); i++)
			nounClasses[i] = new NounClass(genders.get(i), weights.get(i));
	}
	
	public void resolveHiatus(ArrayList<PhonemeInstance> phonemes, int v2Index)
	{
		HiatusResolutionMethod[] methods = hiatusResolutionMethods;
		
		for (HiatusResolutionMethod method : methods)
			if (method.applies(phonemes, v2Index))
			{
				method.resolve(phonemes, v2Index);
				System.out.print("(OK) ");
				return;
			}
		
	}
	
	private void makeSuffixes(int suffixCount)
	{
		System.out.println("all suffixes");
		ArrayList<Double> weights = generateWeights(suffixCount, 0.5);
	
		for (int i = 0; i < weights.size(); i++)
		{
			Suffix suffix = parent.makeSuffix(-Math.log(weights.get(i)));
			
			allSuffixes.addMorpheme(suffix, weights.get(i));
			System.out.println("for target " + -Math.log(weights.get(i)) + " generated " + suffix);
		}
		
		allSuffixes.printMembers();
	}
	
	private ArrayList<Double> generateWeights(int count, double stdev)
	{
		// generates a list of normalized, zipf-scaled weights
		ArrayList<Double> weights = new ArrayList<Double>();
		
		for (int i = 0; i < count; i++)
		{
			double value = 1 + rng.nextGaussian() * stdev; 
			if (value > 0)
				weights.add(value);
		}
		
		Collections.sort(weights);
		Collections.reverse(weights);
		
		// Zipf scale weights and calculate sum for normalization
		double sum = 0;
		for (int i = 0; i < weights.size(); i++)
		{
			double value = weights.get(i) / (i + 1);
			weights.set(i, value);
			sum += value;
		}
		
		// Normalize
		for (int i = 0; i < weights.size(); i++)
			weights.set(i, weights.get(i) / sum);
		
		return weights;
	}
	
	public String toString()
	{
		String result = "";
		result += "ROOTS\n";
		result += "Bound root chance:\t" + boundRootChance + "\n";
		result += "Free root chance:\t" + freeRootChance + "\n";
		result += "\n";
		return result;
	}
}