package Gengen_v2.gengenv2;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class StressSystem
{
	Random rng;
	
	Rhythm rhythm;
	FootDirection footDirection;
	PrimaryStress primaryStress;
	QuantitySensitivity quantitySensitivity;
	double[] metricalSyllableChances;
	double[] extrametricalSyllableChances;
	
	// Generator properties
	static double degenerateFootChanceMean			= 0.50;
	static double degenerateFootChanceStdev			= 0.25;
	static double zeroFootProminenceMean			= 0.50;
	static double zeroFootProminenceStdev			= 0.25;
	static double twoFootProminenceMean				= 1.0 / 6;
	static double twoFootProminenceStdev			= 1.0 / 12;
	static double extrametricalSyllableChanceStdev	= 0.80;
	
	static boolean[][] extrametricalPlacementRules = new boolean[][] {
		/*1*/	{	true,	true,	false,	true,	false,	false	},
		/*2*/	{	false,	false,	true,	false,	true,	false	},
		/*3*/	{	false,	false,	false,	false,	false,	true	},
		/*4*/	{	true,	false,	true,	false,	false,	true	},
		/*5*/	{	false,	true,	false,	false,	true,	false	},
		/*6*/	{	false,	false,	false,	true,	false,	false }
		};
	
	public StressSystem()
	{
		rng = new Random(23423);
		
		// Set foot dominance
		rhythm = Rhythm.values()[rng.nextInt(Rhythm.values().length)];
		
		// Set foot direction
		footDirection = FootDirection.values()[rng.nextInt(FootDirection.values().length)];
		
		// Set primary stress
		primaryStress = PrimaryStress.values()[rng.nextInt(PrimaryStress.values().length)];
		
		// Set quantity senstivity
		quantitySensitivity = QuantitySensitivity.values()[rng.nextInt(QuantitySensitivity.values().length)];
		
		// Set metrical syllable chances
		// These 5 values correspond to the chances of a word having 1-5 metrical syllables.
		// Odd values (even indices) represent 0, 1 or 2 full feet plus one degenerate feet.
		// Even values (odd indices) represent 1 or 2 full feed with no degenerate feet.
		metricalSyllableChances = new double[5];
		
		// Note that mathematically, degenerateFootChance isn't actually the TRUE chance of a degenerate foot
		// appearing in a syllable; they are in reality relatively overrepresented, because 0 full + 0 degenerate
		// foot words are excluded from appearing. TODO: You might fix this.
		double degenerateFootChance	= rng.nextGaussian() * degenerateFootChanceStdev + degenerateFootChanceMean;
		double zeroFootProminence	= rng.nextGaussian() * zeroFootProminenceStdev + zeroFootProminenceMean;
		// Note also that the "one foot prominence" value is implicitly 1.
		double twoFootProminence	= rng.nextGaussian() * twoFootProminenceStdev + twoFootProminenceMean;
		
		// 1: 0 full + 1 degenerate
		metricalSyllableChances[0] = zeroFootProminence * degenerateFootChance;
		
		// 2: 1 full + 0 degenerate
		metricalSyllableChances[1] = 1 * (1 - degenerateFootChance);
		
		// 3: 1 full + 1 degenerate
		metricalSyllableChances[2] = 1 * degenerateFootChance;
		
		// 4: 2 full + 0 degenerate
		metricalSyllableChances[3] = twoFootProminence * (1 - degenerateFootChance);
		
		// 5: 2 full + 1 degenerate
		metricalSyllableChances[4] = twoFootProminence * degenerateFootChance;
		
		// Normalize metrical chances, so that they actually represent probabilities instead of prominences
		double total = 0;
		for (int i = 0; i < metricalSyllableChances.length; i++)
			if (metricalSyllableChances.length > 0)
				total += metricalSyllableChances[i];
		for (int i = 0; i < metricalSyllableChances.length; i++)
			metricalSyllableChances[i] = metricalSyllableChances[i] / total;
		
		// Set extrametrical syllable chances
		extrametricalSyllableChances = new double[6];
		
		// Randomly permute rule rankings
		int[] ruleRankings = new int[6];
		for (int i = 0; i < ruleRankings.length; i++)
			ruleRankings[i] = i;
		
		Collections.shuffle(Arrays.asList(extrametricalSyllableChances));
		
		// Randomly decide which rules to include and their chances of applying
		boolean[] rulesIncluded = new boolean[6];
		double[] ruleStrengths = new double[6];
		
		for (int i = 0; i < rulesIncluded.length; i++)
		{
			rulesIncluded[i] = rng.nextBoolean();
			ruleStrengths[i] = Math.max(rng.nextGaussian() * extrametricalSyllableChanceStdev, 0);
			ruleStrengths[i] = Math.min(ruleStrengths[i], 1);
			
			// Make values that aren't guaranteed either way tend toward 0.5
			if (ruleStrengths[i] >= 1)
				ruleStrengths[i] = (Math.pow((ruleStrengths[i] * 2 - 1), 2) + 1) / 2;
			else
				ruleStrengths[i] = (-Math.pow((ruleStrengths[i] * 2 - 1), 2) + 1) / 2;
		}
			
		// Use rule rankings to determine which feet may have extrametrical syllables
		for (int i = 0; i < extrametricalSyllableChances.length; i++)
			for (int j = 0; j < ruleRankings.length; j++)
			{
				if (extrametricalPlacementRules[ruleRankings[j]][i])
				{
					extrametricalSyllableChances[i] = ruleStrengths[j];
					j = ruleRankings.length;
				}
			}
	}
	
	public void makePattern()
	{
		// Determine metrical syllables
		double rand = rng.nextDouble();
		int syllables = 0;
		
		for (int i = 0; rand > 0; i++)
		{
			syllables = i + 1;
			rand -= metricalSyllableChances[i];
		}
		
		int degenerateFeet = syllables % 2;
		int fullFeet = (syllables - degenerateFeet) / 2;
		
		char[] fullFoot, degenerateFoot;
		
		if (rhythm == Rhythm.TROCHAIC)
			fullFoot = new char[] {'S', 'w'};
		else
			fullFoot = new char[] {'w', 'S'};
		
		boolean[] extra = new boolean[3];
		if (syllables == 5)
		{
			extra[0] = (rng.nextDouble() < extrametricalSyllableChances[3]);
			extra[1] = (rng.nextDouble() < extrametricalSyllableChances[4]);
			extra[2] = (rng.nextDouble() < extrametricalSyllableChances[5]);
		}
		else if (syllables >= 3)
		{
			extra[0] = (rng.nextDouble() < extrametricalSyllableChances[1]);
			extra[1] = (rng.nextDouble() < extrametricalSyllableChances[2]);
		}
		else
		{
			extra[0] = (rng.nextDouble() < extrametricalSyllableChances[0]);
		}
		
		
		
		String result = "";
		
		// Deal with full feet
		for (int i = 0; i < fullFeet; i++)
		{
			String nextFoot;
			nextFoot = "(" + fullFoot[0] + " " + fullFoot[1] + ") ";
			if (extra[i])
				nextFoot += "w ";
			if (footDirection == FootDirection.LEFT_TO_RIGHT)
				result = result + nextFoot;
			else
				result = nextFoot + result;
		}
		
		// Deal with degenerate feet
		if (degenerateFeet == 1)
		{
			String nextFoot = "";
			if (extra[fullFeet])
				nextFoot += "w ";
			if (footDirection == FootDirection.LEFT_TO_RIGHT)
			{
				nextFoot = "(" + fullFoot[0] + ") " + nextFoot;
				result = result + nextFoot;
			}
			else
			{
				nextFoot = "(" + fullFoot[1] + ") " + nextFoot;
				result = nextFoot + result;
			}
		}
		
		System.out.println(result);
	}
	
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		result.append("Rhythm\t\t" + rhythm + "\n");
		result.append("Direction\t" + footDirection + "\n");
		result.append("Primary Stress\t" + primaryStress + "\n");
		result.append("Sensitivity\t" + quantitySensitivity + "\n\n");
		
		double[] met = metricalSyllableChances;
		double[] ext = extrametricalSyllableChances;
		
		result.append("Metrical syllable chances\n");
		for (int i = 0; i < met.length; i++)
			if (met[i] > 0)
				result.append(String.format("  %d: %.3f\n", (i+1), met[i]));
		result.append("\n");
		
		result.append("Extrametrical syllable chances\n");
		for (int i = 0; i < 3; i++)
			for (int j = 0; j <= i; j++)
				result.append(String.format("  %d/%d: %.3f\n", i+1, j+1, ext[i*(i+1)/2 + j]));
		result.append("\n");
		
		
		result.append("Word lengths\n");
		result.append(String.format("  1: %.3f\n", met[0] * (1 - ext[0])));	// 1 syl * not 1/1
		result.append(String.format("  2: %.3f\n", met[0] * ext[0] +			// 1 syl * 1/1
												 met[1] * (1 - ext[0])));	// 2 syl * not 1/1
		result.append(String.format("  3: %.3f\n", met[1] * ext[0] +			// 2 syl * 1/1
												 met[2] * (1 - ext[1]) * (1 - ext[2]))); // 3 syl * not 2/1 * not 2/2
		result.append(String.format("  4: %.3f\n", met[2] * ext[1] * (1 - ext[2]) +		 // 3 syl * 2/1 * not 2/2
												 met[2] * (1 - ext[1]) * ext[2] +		 // 3 syl * not 2/1 * 2/2
												 met[3] * (1 - ext[1]) * (1 - ext[2])));	 // 4 syl * not 2/1 * not 2/2
		result.append(String.format("  5: %.3f\n", met[2] * ext[1] * ext[2] +			 // 3 syl * 2/1 * 2/2
												 met[3] * ext[1] * (1 - ext[2]) +		 // 4 syl * 2/1 * not 2/2
												 met[3] * (1 - ext[1]) * ext[2] +		 // 4 syl * not 2/1 * 2/2
												 met[4] * (1 - ext[3]) * (1 - ext[4]) * (1 - ext[5])));	// 5 syl * not 3/1 * not 3/2 * not 3/3
		result.append(String.format("  6: %.3f\n", met[3] * ext[1] * ext[2] +				 // 4 syl * 2/1 * 2/2
												 met[4] * ext[3] * (1 - ext[4]) * (1 - ext[5]) +		// 5 syl * 3/1 * not 3/2 * not 3/3
												 met[4] * (1 - ext[3]) * ext[4] * (1 - ext[5]) +		// 5 syl * not 3/1 * 3/2 * not 3/3
												 met[4] * (1 - ext[3]) * (1 - ext[4]) * ext[5]));		// 5 syl * not 3/1 * not 3/2 * 3/3
		result.append(String.format("  7: %.3f\n", met[4] * ext[3] * ext[4] * (1 - ext[5]) +		// 5 syl * 3/1 * 3/2 * not 3/3
												 met[4] * ext[3] * (1 - ext[4]) * ext[5] +		// 5 syl * 3/1 * not 3/2 * 3/3
												 met[4] * (1 - ext[3]) * ext[4] * ext[5]));		// 5 syl * not 3/1 * 3/2 * 3/3
		result.append(String.format("  8: %.3f\n", met[4] * ext[3] * ext[4] * ext[5]));		// 5 syl * 3/1 * 3/2 * 3/3
		
		return result.toString();
	}
	
	enum Rhythm { TROCHAIC, IAMBIC };
	enum FootDirection { LEFT_TO_RIGHT, RIGHT_TO_LEFT };
	enum PrimaryStress { FIRST, SECOND, LAST, SECOND_LAST };
	enum QuantitySensitivity { QUANTITY_SENSITIVE, QUANTITY_INSENSITIVE };
}
