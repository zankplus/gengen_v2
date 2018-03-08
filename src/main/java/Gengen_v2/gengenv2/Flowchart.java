package Gengen_v2.gengenv2;

import java.util.ArrayList;
import java.util.Hashtable;

import Gengen_v2.gengenv2.Phonology.SyllableSegment;

class Flowchart
{
	Phonology p;
	Node startNode, syllableLocationNode;
	
	public Flowchart(Phonology p)
	{
		ArrayList<SyllableSegment> name = new ArrayList<SyllableSegment>();
		this.p = p;
		
		startNode = new StartNode();
	}
	
	interface Node
	{
		public void nextNode();
	}

	private class StartNode implements Node
	{
		double emptyOnsetChance, simpleOnsetChance, complexOnsetChance, total;
		
		public StartNode()
		{
			// Scaling each prominence value by the log of the number of possibilities it represents
			// helps ensure that rich inventories are well represented while poor ones are sampled
			// less frequently. This reduces the chances of something like a language with only 1
			// onset cluster having that cluster at the start of 20% of names.
			total = 0;
			
			// Empty onset chance
			emptyOnsetChance = p.emptyInitialOnsetProminence * Math.log(Math.pow(p.data[p.SIMPLE_NUCLEI] + p.data[p.COMPLEX_NUCLEI], 2));
			total += this.emptyOnsetChance;
			
			// Simple onset chance
			simpleOnsetChance = 1 * Math.log(p.data[p.SIMPLE_ONSETS] + 1);
			total += this.simpleOnsetChance;
			
			// Complex onset chance
			if (p.maxOnsetLength > 1)
			{
				double r = p.onsetClusterProminence;
				complexOnsetChance = r * (Math.pow(r, p.maxOnsetLength) - 1) / (r - 1);
				complexOnsetChance *= Math.log(p.data[p.COMPLEX_ONSETS] + 1);
			}
			else
				complexOnsetChance = 0;
			total += complexOnsetChance;
			
			emptyOnsetChance   = emptyOnsetChance   / total;
			simpleOnsetChance  = simpleOnsetChance  / total;
			complexOnsetChance = complexOnsetChance / total;
		}
		
		public void nextNode()
		{
			double rand = p.rng.nextDouble();
			if (rand < emptyOnsetChance)
			{
				
			}
		}
	}
}


