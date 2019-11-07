package gengenv2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import gengenv2.enums.SuffixType;
import gengenv2.morphemes.NounClass;
import gengenv2.morphemes.Suffix;

public class Morphology
{
	double boundRootChance;
	double freeRootChance;
	
	Random rng;
	Phonology parent;
	
	SuffixLibrary allSuffixes;
	Suffix[] nounClasses;
	
	public Morphology(Phonology p)
	{
		parent = p;
		rng = PublicRandom.getRNG();
		allSuffixes = new SuffixLibrary(true);
		
		setRootPreferences();
//		makeSuffixes();
//		generateNounClasses();
	}
	
	private void setRootPreferences()
	{
		// 20% chance purely bound, 40% purely free, 40% somewhere in between
		boundRootChance = rng.nextDouble() * 2.5 - 0.5;
		boundRootChance = Math.min(boundRootChance, 1);
		boundRootChance = Math.max(boundRootChance, 0);
		
		freeRootChance = 1 - boundRootChance;
	}
	
	public void makeSuffixes()
	{
		System.out.println("all suffixes");
		
		int nounTypes = 15 + rng.nextInt(11);
		ArrayList<Double> weights = generateWeights(nounTypes, 0.5);
	
		for (int i = 0; i < weights.size(); i++)
			allSuffixes.addSuffix(parent.makeSuffix(-Math.log(weights.get(i))), weights.get(i));
		
		allSuffixes.printMembers();
	}
	
	public void generateNounClasses()
	{
		int qty = Math.min(Math.max(rng.nextInt(6) + rng.nextInt(6) - 2, 1), allSuffixes.size());
		generateNounClasses(qty);
	}
	
	public void generateNounClasses(int nounClasses)
	{
		// Generate noun classes
		int qtyNounClasses = Math.min(Math.max(rng.nextInt(6) + rng.nextInt(6) - 4, 1), allSuffixes.size());
		ArrayList<SuffixLibrary> genders = new ArrayList<SuffixLibrary>();
		ArrayList<Double> weights = generateWeights(qtyNounClasses, 0.25);
		
		System.out.println("Noun classes");
		
		for (int i = 0; i < weights.size(); i++)
		{
			genders.add(new SuffixLibrary(true));
			System.out.println(weights.get(i));
		}
		
		// Make copy of suffixes list
		ArrayList<SuffixEntry> suffixes = new ArrayList<SuffixEntry>();
		for (SuffixEntry se : allSuffixes.getLibrary())
			suffixes.add(se);
		
		double[] diff = new double[qtyNounClasses];
		for (int i = 0; i < diff.length; i++)
			diff[i] = weights.get(i);
		
		// Populate each noun class. for in suffix in descending order of probability, add it to the class with the highest 'diff'
		// (and then update the diff)
		while (suffixes.size() < 0)
		{
			double max = diff[0];
			int maxIndex = 0;
			
			for (int i = 1; i < diff.length; i++)
				if (diff[i] > diff[maxIndex])
				{
					max = diff[i];
					maxIndex = i;
				}
			
			SuffixEntry se = suffixes.get(0);
			genders.get(maxIndex).addSuffix(se.getSuffix(), se.getProbability());
			diff[maxIndex] - se.getProbability();
		}
		
		for (int i = 0; i < genders.size(); i++)
		{
			System.out.println("Class " + (i + 1) + " (" + weights.get(index))
		}
		
		
		// Any remaining syllables should be added to the noun class with the highest remaining diff
		
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