package Gengen_v2.gengenv2;

import java.util.ArrayList;
import java.util.Hashtable;

import Gengen_v2.gengenv2.Phonology.SyllableSegment;

class Flowchart
{
	Phonology p;
	Node startNode, syllableLocationNode, medialSyllableWeightNode, medialHeavyRimeNode, medialLightRimeNode,
	
		terminalSyllableWeightNode;
	ArrayList<SyllableSegment> name;
	
	StressSystem stressSystem;
	char[] pattern;
	int curr;
	
	// ~~ Decision Variables ~~
	// These are cumulative percentages, to reduce the amount of arithmetic at runtime.
	// The real chance of a simple onset is simpleOnsetChance - emptyOnsetChance;
	// of a complex onset, 1 - emptyOnsetChance - simpleOnsetChance.
	double emptyOnsetChance, simpleOnsetChance;
	
	static double EmptyCodaChanceDisturbanceStdev = 0.25;
	
	public Flowchart(Phonology p)
	{
		name = new ArrayList<SyllableSegment>();
		this.p = p;
		
		stressSystem = new StressSystem();
		startNode = new StartNode();
		
		makeWord();
	}
	
	public void makeWord()
	{
		pattern = stressSystem.makePattern().toCharArray();
		curr = -1;
		
		Node node = startNode;
		while (node != null)
		{
			node = node.nextNode();
		}
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
		public StartNode()
		{
			// Scaling each prominence value by the log of the number of possibilities it represents
			// helps ensure that rich inventories are well represented while poor ones are sampled
			// less frequently. This reduces the chances of something like a language with only 1
			// onset cluster having that cluster at the start of 20% of names.
			double total = 0;
			
			// Empty onset chance
			emptyOnsetChance = p.emptyInitialOnsetProminence * Math.log(Math.pow(p.data[Phonology.SIMPLE_NUCLEI] + p.data[Phonology.COMPLEX_NUCLEI], 2));
			total += emptyOnsetChance;
			
			// Simple onset chance
			simpleOnsetChance = 1 * Math.log(p.data[Phonology.SIMPLE_ONSETS] + 1);
			total += simpleOnsetChance;
			
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
				return syllableLocationNode;
			}

			// Option 2: Simple onset
			else if (rand < simpleOnsetChance)
			{
				name.add(p.pickSyllableSegment(p.onsets[0]));
				return syllableLocationNode;
			}
			
			// Option 3: Complex onset
			else
			{
				name.add(p.pickSyllableSegment(p.onsets[p.pickClusterLength(p.onsetClusterLengthProbabilities)]));
				return syllableLocationNode;
			}
		}
	}
	
	private class SyllableLocationNode implements Node
	{
		public Node nextNode()
		{
			// Option 1: Initial/medial syllable
			if (curr < pattern.length)
				return medialSyllableWeightNode;
			
			// Option 2: Terminal syllable
			else
				return terminalSyllableWeightNode;
		}
	}
	
//	private class MedialSyllableWeightNode implements Node
//	{
//		public Node nextNode()
//		{
//			if (pattern[curr] == 'S')
//				return medialHeavyRimeNode;
//			else
//				return medialLightRimeNode;
//		}
//	}
//	
//	private class MedialLightRimeNode implements Node
//	{
//		public MedialLightRimeNode()
//		{
//			
//		}
//		
//		public Node nextnode()
//		{
//			name.add(p.pickSyllableSegment(p.nuclei[0]));
//			
//			return null;
//		}
//	}
	
	private class TerminalSyllableWeightNode implements Node
	{
		public Node nextNode()
		{
			if (pattern[curr] == 'S')
				return null;
			else
				return null;
		}
	}
	
	private class TerminalLightRimeNode implements Node
	{
		
		public TerminalLightRimeNode()
		{
			
			
		}
		
		public Node nextNode()
		{
			return null;
		}
	}
}


