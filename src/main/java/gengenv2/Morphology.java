package gengenv2;

import java.util.Random;

public class Morphology
{
	double[] suffixPreferences;	// Chance of generating each type of suffix
	Random rng;
	Phonology parent;
	
	
	public Morphology(Phonology p)
	{
		parent = p;
		
		if (p.terminalCodas == null)
			suffixPreferences = new double[2];
		else
			suffixPreferences = new double[3];
		
		rng = PublicRandom.getRNG();
		
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
	
	public static void main(String[] args)
	{
		// Test
		
		Phonology p = new Phonology(-4889008631637249723L);
		System.out.println(p);
		
		Morphology m = new Morphology(p);
		for (int i = 0; i < m.suffixPreferences.length; i++)
			System.out.println(SuffixType.values()[i] + ":\t" + m.suffixPreferences[i]);
	}
}

enum SuffixType { SYLLABIC, NUCLEIC, CAUDAL }