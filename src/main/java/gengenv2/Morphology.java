package gengenv2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

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
	
	HiatusResolutionMethod dieresis;
	HiatusResolutionMethod diphthongFormation;
	VowelElision vowelElision;
	Coalescence coalescence;
	HomorganicGlideEpenthesis homorganicGlideEpenthesis;
	ConsistentConsonantEpenthesis consistentConsonantEpenthesis;
	Syneresis syneresis;
	
	public Morphology(Phonology p)
	{
		parent = p;
		rng = PublicRandom.getRNG();
		allSuffixes = new MorphemeLibrary(true);
		
//		makeSuffixes();
//		generateNounClasses();
		
		dieresis = new Dieresis();
		diphthongFormation = new DiphthongFormation();
		vowelElision = new VowelElision();
		vowelElision.configureCompensatoryLenghtening(true, parent.longVowel);
		
		
		// Coalescence
		
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
		
		coalescence = new Coalescence (frontMidVowel, backMidVowel, p.medialNuclei.contains(frontMidVowel), p.terminalNuclei.contains(frontMidVowel),
										p.medialNuclei.contains(backMidVowel), p.terminalNuclei.contains(backMidVowel));
		coalescence.configureCompensatoryLenghtening(true, parent.longVowel);
		
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
		
		homorganicGlideEpenthesis = new HomorganicGlideEpenthesis(yGlide, wGlide, true, true, true, true);
		
		String[] leastMarkedConsonants = new String[] { "'", "h", "t", "y", "r", "w" };
		ConsonantPhoneme leastMarked = null;
		for (int i = 0; i < p.medialOnsets.size() && leastMarked == null; i++)
		{
			for (int j = 0; j < leastMarkedConsonants.length && leastMarked == null; j++)
				if (p.medialOnsets.getMembers().get(i).getContent().segment.expression.equals(leastMarkedConsonants[j]))
					leastMarked = (ConsonantPhoneme) p.medialOnsets.getMembers().get(i).getContent();
		}
		
		consistentConsonantEpenthesis = new ConsistentConsonantEpenthesis(leastMarked);
		
		boolean initialYAllowed = p.initialNuclei != null ? yGlide != null && p.initialNuclei.contains(yGlide) : false;
		boolean initialWAllowed = p.initialNuclei != null ? wGlide != null && p.initialNuclei.contains(yGlide) : false;
		boolean medialYAllowed = yGlide != null && p.medialNuclei.contains(yGlide);
		boolean medialWAllowed = wGlide != null && p.medialNuclei.contains(wGlide);
		
		syneresis = new Syneresis(yGlide, wGlide, initialYAllowed, initialWAllowed, medialYAllowed, medialWAllowed,
									true, true, true);
		syneresis.configureCompensatoryLenghtening(true, parent.longVowel);
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
		HiatusResolutionMethod[] methods = new HiatusResolutionMethod[] { syneresis,  diphthongFormation, homorganicGlideEpenthesis, coalescence,
																			dieresis, vowelElision, consistentConsonantEpenthesis };
		
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