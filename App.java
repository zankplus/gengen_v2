package Gengen_v2.gengenv2;

import java.util.List;

public class App 
{
    public static void main( String[] args )
    {
    	// TODO: Start with this phonology
    	for (int i = 0; i < 10000; i++)
    	{
//	    	Phonology p = new Phonology(7810978930813334716L);	// For names that are just a single letter
    		Phonology p = new Phonology();
	    	
	    	List<String> names = p.makeNames(10000);
	    	
//	    	for (String name : names)
//	    		System.out.println(name);
    	}
    	
    	
//    	StressSystem ss = new StressSystem();
//    	System.out.println(ss);
    	
//    	Phonology.massGatherStats(10000);
    	
//    	for (int i = 0; i < Cluster.consonantCategories.size(); i++)
//    		for (int j = 0; j < Cluster.consonantCategories.size(); j++)
//    		{
//    			System.out.print("[" + i + "-" + j + "]\t");
//    			for (int m = 0; m < Cluster.consonantCategories.get(i).length; m++)
//    				for (int n = 0; n < Cluster.consonantCategories.get(j).length; n++)
//    					if (Cluster.consonantCategories.get(i)[m] != Cluster.consonantCategories.get(j)[n])
//    						System.out.print("a" + 
//    										 Consonant.segments[Cluster.consonantCategories.get(i)[m]].expression + 
//    										 Consonant.segments[Cluster.consonantCategories.get(j)[n]].expression +
//    										 "a ");
//    			System.out.println();
//    		}
    	
        
//		p.printInventoryWithProminence();
		
		
		
//		p.printClusteringRules();
//		
//		for (int i = 0; i < 8; i++)
//		{
//			for (int j = 0; j < 12; j++)
//				System.out.print(p.toString(p.makeSyllable()) + "\t");
//			System.out.println();
//		}
    }
}
