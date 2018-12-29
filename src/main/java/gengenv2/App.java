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
	    	
	    	List<Name> names = p.makeNames(100);
	    	
	    	for (Name name : names)
	    		System.out.println(name);
    	}
    }
}
