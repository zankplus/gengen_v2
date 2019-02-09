package gengenv2;

import java.util.Collections;
import java.util.List;

import gengenv2.Phonology.Constituent;

public class App 
{
    public static void main( String[] args )
    {
    	// TODO: Start with this phonology
    	for (int i = 0; i < 1; i++)
    	{
//	    	Phonology p = new Phonology(7810978930813334716L);	// For names that are just a single letter
//    		Phonology p = new Phonology(-3518982534379222749L);	// fascinating
//    		Phonology p = new Phonology(-6159931717817714899L);
//    		Phonology p = new Phonology(2983323053851964786L);
    		Phonology p = new Phonology();
	    	
    		List<Name> names = p.makeNames(50);
    		Collections.sort(names);
    		
    		for (Name name : names)
    		{
    			System.out.printf("%.3f\t", name.informationContent);
    			System.out.println(name.getClear() + "\t\t" + name.getIPA());
    		}
    		
//	    	List<Name> names = p.makeNames(40);
//	    	
//	    	int j = 1;
//	    	for (Name name : names)
//	    	{
//	    		String orthography = name.toString();
//	    		System.out.print(orthography + "\t");
//	    		
//	    		if (orthography.length() < 8)
//	    			System.out.print("\t");
//	    		if (orthography.length() < 16)
//	    			System.out.print("\t");
//	    		
//	    		
//	    		if (j % 5 == 0)
//	    			System.out.println();
//	    		j++;
//	    	}
//	    	System.out.println();
	
//	    	if (p.codas.length > 0)
//	    	{
//		    	double emptyCodaChance = 1 - p.baseMedialCodaChance;
//		    	for (j = 0; j < p.codas.length; j++)
//		    	{
//		    		double clusterLengthChance = p.baseMedialCodaChance;
//		    		if (j == 0)
//		    			clusterLengthChance *= (1 - p.baseCodaClusterChance) * Math.log(p.codas[0].size()) ;
//		    		else
//		    			clusterLengthChance *= p.baseCodaClusterChance * p.codaClusterLengthProbabilities[j - 1];
//		    		
//		    		double most = p.codas[j].get(0).probability * clusterLengthChance;
//		    		double least = p.codas[j].get(p.codas[j].size() - 1).probability * clusterLengthChance;
//		    		
//		    		if ((mostCommonCoda == null && most > emptyCodaChance) || (mostCommonCoda != null && most > mostCommonCoda.probability))
//		    			mostCommonCoda = p.codas[j].get(0);
//		    		
//		    		if (leastCommonCoda == null || (mostCommonCoda != null && least < leastCommonCoda.probability))
//		    			leastCommonCoda = p.codas[j].get(p.codas[j].size() - 1);
//		    	}
//		    	
//		    	mostCodaChance = (mostCommonCoda == null ? emptyCodaChance : mostCommonCoda.probability); 
//		    	leastCodaChance = (leastCommonCoda == null ? emptyCodaChance : leastCommonCoda.probability);
//		    	
//		    	System.out.println("Most common Coda:\t" + mostCommonCoda + "\t" + 
//						mostCodaChance);
//		    	System.out.println("Least common Coda:\t" + leastCommonCoda + "\t" + 
//						leastCodaChance);
//	    	}
	    	
//	    	System.out.println("Min. information content: " + 
//	    			(-Math.log(mostCommonOnset.probability * mostCommonNucleus.probability * mostCodaChance)));
//	    	System.out.println("Max. information content: " + 
//	    	    	(-Math.log(leastCommonOnset.probability * leastCommonNucleus.probability * leastCodaChance)));
	    	
	    	
	    	// Print basic syllable structure
//	    	String structure = "";
//	    	String coda = "";
//	    	
//	    	for (int k = 0; k < p.maxOnsetLength; k++)
//	    		structure += "C";
//	    	for (int k = 0; k < p.maxNucleusLength; k++)
//	    		structure += "V";
//	    	for (int k = 0; k < p.maxCodaLength; k++)
//	    		coda += "C";
//	    	System.out.println();
//	    	
//	    	System.out.print("Medial:\t" + structure);
//	    	if (p.baseMedialCodaChance > 0)
//	    		System.out.print(coda);
//	    	System.out.println(" (" + p.baseMedialCodaChance + ")");
//	    	
//	    	System.out.print("Coda:\t" + structure);
//	    	if (p.baseTerminalCodaChance > 0)
//	    		System.out.print(coda);
//	    	System.out.println(" (" + p.baseTerminalCodaChance + ")");
    	}
    	
//    	for (int i = 0; i < 10000000; i++)
//    	{
//    		Phonology p = new Phonology();
//    		if (p.maxNucleusLength == 1 && p.baseDiphthongChance != 0)
//    		{
//    			System.err.println("Found one");
//    			System.exit(1);
//    		}
//    	}
    }
}
