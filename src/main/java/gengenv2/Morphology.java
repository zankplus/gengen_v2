package gengenv2;

import java.util.Random;

public class Morphology
{
	double[] suffixPreferences;	// Chance of generating each type of suffix
	double boundRootChance;
	double freeRootChance;
	
	Random rng;
	Phonology parent;
	
	
	public Morphology(Phonology p)
	{
		parent = p;
		rng = PublicRandom.getRNG();
		
		setRootPreferences();
		setSuffixPreferences();
	}
	
	private void setRootPreferences()
	{
		// 20% chance purely bound, 40% purely free, 40% somewhere in between
		boundRootChance = rng.nextDouble() * 2.5 - 0.5;
		boundRootChance = Math.min(boundRootChance, 1);
		boundRootChance = Math.max(boundRootChance, 0);
		
		freeRootChance = 1 - boundRootChance;
	}
	
	private void setSuffixPreferences()
	{
		if (parent.terminalCodas == null)
			suffixPreferences = new double[2];
		else
			suffixPreferences = new double[3];
		
		
		
		// Set base chances for each type of suffix randomly between -0.5 and 1	
		int max = 0;
		for (int i = 0; i < suffixPreferences.length; i++)
		{
			suffixPreferences[i] = rng.nextDouble() * 1.5 - 1;
			if (suffixPreferences[i] > suffixPreferences[max])
				max = i;
		}
		
		// If all the base chances are negative, set the highest one to 1
		if (suffixPreferences[max] <= 0)
			suffixPreferences[max] = 1;
		
		
		// Set any negative values to 0 and normalize the base chances.
		double sum = 0;
		for (int i = 0; i < suffixPreferences.length; i++)
		{
			if (suffixPreferences[i] < 0)
				suffixPreferences[i] = 0;
			sum += suffixPreferences[i];
		}
		
		for (int i = 0; i < suffixPreferences.length; i++)
			suffixPreferences[i] /= sum;
	}
	
	public String toString()
	{
		String result = "";
		result += "ROOTS\n";
		result += "Bound root chance:\t" + boundRootChance + "\n";
		result += "Free root chance:\t" + freeRootChance + "\n";
		result += "\n";
		result += "SUFFIXES\n";
		for (int i = 0; i < suffixPreferences.length; i++)
			result += SuffixType.values()[i] + ": \t" + suffixPreferences[i] + "\n"; 
		result += "\n";
		return result;
	}
}

enum SuffixType { SYLLABIC, NUCLEIC, CAUDAL }