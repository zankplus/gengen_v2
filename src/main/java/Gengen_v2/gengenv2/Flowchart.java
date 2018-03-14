package Gengen_v2.gengenv2;

import java.util.ArrayList;
import java.util.Hashtable;

import Gengen_v2.gengenv2.Phonology.SyllableSegment;

class Flowchart
{
	Phonology p;
	Node startNode, syllableLocationNode;
	ArrayList<SyllableSegment> name;
	
	
	
	public Flowchart(Phonology p)
	{
		name = new ArrayList<SyllableSegment>();
		this.p = p;
		
		startNode = new StartNode();
		
		Node node = startNode;
		do
		{
			
		} while (node.nextNode() != null);
		System.out.print("Initial: ");
		for (SyllableSegment ss : name)
			System.out.print(ss);
		System.out.println();
	}
	
	interface Node
	{
		public Node nextNode();
	}

	private class StartNode implements Node
	{
		// These are cumulative percentages, to reduce the amount of arithmetic at runtime.
		// The real chance of a simple onset is simpleOnsetChance - emptyOnsetChance;
		// of a complex onset, 1 - emptyOnsetChance - simpleOnsetChance.
		double emptyOnsetChance, simpleOnsetChance;
		
		public StartNode()
		{
			// Scaling each prominence value by the log of the number of possibilities it represents
			// helps ensure that rich inventories are well represented while poor ones are sampled
			// less frequently. This reduces the chances of something like a language with only 1
			// onset cluster having that cluster at the start of 20% of names.
			double total = 0;
			
			// Empty onset chance
			emptyOnsetChance = p.emptyInitialOnsetProminence * Math.log(Math.pow(p.data[Phonology.SIMPLE_NUCLEI] + p.data[Phonology.COMPLEX_NUCLEI], 2));
			total += this.emptyOnsetChance;
			
			// Simple onset chance
			simpleOnsetChance = 1 * Math.log(p.data[Phonology.SIMPLE_ONSETS] + 1);
			total += this.simpleOnsetChance;
			
			// Complex onset chance
			double complexOnsetChance = 0;
			if (p.maxOnsetLength > 1)
			{
				double r = p.onsetClusterProminence;
				complexOnsetChance = r * (Math.pow(r, p.maxOnsetLength) - 1) / (r - 1);
				complexOnsetChance *= Math.log(p.data[Phonology.COMPLEX_ONSETS] + 1);
			}
			total += complexOnsetChance;
			
			emptyOnsetChance   = emptyOnsetChance   / total;
			simpleOnsetChance  = simpleOnsetChance  / total;
		}
		
		public Node nextNode()
		{
			double rand = p.rng.nextDouble();

			// Option 1: Empty onset
			if (rand < emptyOnsetChance)
			{
				return null;
			}

			// Option 2: Simple onset
			else if (rand < simpleOnsetChance)
			{
				name.add(p.pickSyllableSegment(p.onsets[0]));
				return null;
			}
			
			// Option 3: Complex onset
			else
			{
				name.add(p.pickSyllableSegment(p.onsets[p.pickClusterLength(p.onsetClusterLengthProbabilities)]));
				return null;
			}
		}
	}
}


