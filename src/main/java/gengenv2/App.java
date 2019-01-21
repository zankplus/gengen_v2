package gengenv2;

import java.util.List;

public class App 
{
    public static void main( String[] args )
    {
    	// TODO: Start with this phonology
    	for (int i = 0; i < 1; i++)
    	{
//	    	Phonology p = new Phonology(7810978930813334716L);	// For names that are just a single letter
//    		Phonology p = new Phonology(-3518982534379222749L);	// fascinating
    		Phonology p = new Phonology();
//    		Phonology p = new Phonology(-706302095727261L);
	    	
	    	List<Name> names = p.makeNames(100);
	    	
	    	int j = 1;
	    	for (Name name : names)
	    	{
	    		String orthography = name.toString();
	    		System.out.print(orthography + "\t");
	    		
	    		if (orthography.length() < 8)
	    			System.out.print("\t");
	    		if (orthography.length() < 16)
	    			System.out.print("\t");
	    		
	    		
	    		if (j % 5 == 0)
	    			System.out.println();
	    		j++;
	    	}
	    	System.out.println();
	    	
	    	// Print basic syllable structure
	    	String structure = "";
	    	String coda = "";
	    	
	    	for (int k = 0; k < p.maxOnsetLength; k++)
	    		structure += "C";
	    	for (int k = 0; k < p.maxNucleusLength; k++)
	    		structure += "V";
	    	for (int k = 0; k < p.maxCodaLength; k++)
	    		coda += "C";
	    	System.out.println();
	    	
	    	System.out.print("Medial:\t" + structure);
	    	if (p.baseMedialCodaChance > 0)
	    		System.out.print(coda);
	    	System.out.println(" (" + p.baseMedialCodaChance + ")");
	    	
	    	System.out.print("Coda:\t" + structure);
	    	if (p.baseTerminalCodaChance > 0)
	    		System.out.print(coda);
	    	System.out.println(" (" + p.baseTerminalCodaChance + ")");
	    	
    	}
    }
}
