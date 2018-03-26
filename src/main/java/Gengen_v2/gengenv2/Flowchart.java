package Gengen_v2.gengenv2;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import Gengen_v2.gengenv2.Phonology.SyllableSegment;

class Flowchart
{
	Phonology p;
	Node startNode, syllableLocationNode, medialSyllableWeightNode, medialLightRimeNode, medialHeavyRimeNode,
		 medialComplexNucleusNode, complexInterludeNode,
		 terminalSyllableWeightNode, terminalLightRimeNode, terminalHeavyRimeNode, terminalSimpleNucleusNode,
		 terminalComplexNucleusNode;
	
	ArrayList<SyllableSegment> name;
	
	Random rng;
	
	StressSystem stressSystem;
	char[] pattern;
	int curr;
	
	static double EmptyCodaChanceDisturbanceStdev = 0.25;
	
	public Flowchart(Phonology p)
	{
		this.p = p;
		rng = p.rng;
		
		stressSystem = new StressSystem();
		startNode = new StartNode();
		syllableLocationNode = new SyllableLocationNode();
		medialSyllableWeightNode = new MedialSyllableWeightNode();
		medialLightRimeNode = new MedialLightRimeNode();
		medialHeavyRimeNode = new MedialHeavyRimeNode();
		medialComplexNucleusNode = new MedialComplexNucleusNode();
		complexInterludeNode = new ComplexInterludeNode();
		
		makeAdjustedCounts();
		
		makeWord();
	}
	
	public void makeAdjustedCounts()
	{
		double medialLightRime;
		double medialHeavyRimeSimpleNucleus;
		double medialHeavyRimeComplexNucleus;
		
		// number of light rimes with simple interludes
		medialLightRime = p.data[Phonology.SIMPLE_NUCLEI] * p.data[Phonology.SIMPLE_NUCLEI] * p.baseOnsetChance;
		
		// add number of light rimes with empty interludes
		medialLightRime += p.data[Phonology.SIMPLE_NUCLEI_WITH_HIATUS] * (1 - p.baseOnsetChance); 
		
		// number of heavy rimes with simple nuclei and complex onsets
		medialHeavyRimeSimpleNucleus = p.data[Phonology.SIMPLE_NUCLEI] * p.data[Phonology.COMPLEX_ONSETS];
		
		// scale by chance of onset, complex onset | onset, !coda | onset
		medialHeavyRimeSimpleNucleus *= p.baseOnsetChance;
		medialHeavyRimeSimpleNucleus *= p.baseOnsetClusterChance;
		medialHeavyRimeSimpleNucleus *= (1 - p.medialCodaChance);
		
		// number of heavy rimes with simple nuclei an compound interludes
		// scale by chance of onset, coda | onset
		medialHeavyRimeSimpleNucleus += p.data[Phonology.SIMPLE_NUCLEI] * p.data[Phonology.COMPOUND_INTERLUDES] *
										p.baseOnsetChance * p.medialCodaChance;
		
		
		
		double terminalLightRime;
		double terminalHeavyRimeSimpleNucleus;	
		double terminalHeavyRimeComplexNucleus;
		
		// number of light rimes with simple codas
		terminalLightRime = p.data[Phonology.SIMPLE_CODAS] * p.terminalCodaChance;
		
		// add number of light rimes with no codas
		terminalLightRime += (1 - p.terminalCodaChance);
		
		// number of heavy rimes with simple nuclei
		terminalHeavyRimeSimpleNucleus = (p.data[Phonology.SIMPLE_CODAS] + p.data[Phonology.COMPLEX_CODAS]);
		
		// Scale the number of codas by the terminalCodaChance; we can't choose a simple nucleus if terminal codas aren't allowed!
		terminalHeavyRimeSimpleNucleus *= p.terminalCodaChance;
		
		// Scale the complement of the diphthong chance
		terminalHeavyRimeSimpleNucleus *= (1 - p.baseDiphthongChance);
		
		// Start with the number of heavy rimes with complex nuclei
		terminalHeavyRimeComplexNucleus = (p.data[Phonology.SIMPLE_CODAS] + p.data[Phonology.COMPLEX_CODAS]);
		
		// Scale the number of coda-bearing rimes by the terminalCodaChance
		terminalHeavyRimeComplexNucleus *= p.terminalCodaChance;
		
		// Add the number of coda-free rimes, scaled by their own chance
		terminalHeavyRimeComplexNucleus += (1 - p.terminalCodaChance);
		
		// Scale by the diphthong chance
		terminalHeavyRimeComplexNucleus *= p.baseDiphthongChance;
	}
	
	public void makeWord()
	{
		name = new ArrayList<SyllableSegment>();
		pattern = stressSystem.makePattern().toCharArray();
		curr = 0;
		
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
		// ~~ Decision Variables ~~
		// These are cumulative percentages, to reduce the amount of arithmetic at runtime.
		// The real chance of a simple onset is simpleOnsetChance - emptyOnsetChance;
		// of a complex onset, 1 - emptyOnsetChance - simpleOnsetChance.
		// Note: I wound up not doing this with other nodes lol
		double emptyOnsetChance, simpleOnsetChance;
		
		public StartNode()
		{
			// Scaling each prominence value by the log of the number of possibilities it represents
			// helps ensure that rich inventories are well represented while poor ones are sampled
			// less frequently. This reduces the chances of something like a language with only 1
			// onset cluster having that cluster at the start of 20% of names.
			double total = 0;
			
			// Empty onset chance
			emptyOnsetChance = p.emptyInitialOnsetProminence *
					Math.log(Math.pow(p.data[Phonology.SIMPLE_NUCLEI] + p.data[Phonology.COMPLEX_NUCLEI] + 1, 2));
			total += emptyOnsetChance;
			
			// Simple onset chance
			simpleOnsetChance = 1 * Math.log(p.data[Phonology.SIMPLE_ONSETS] + 1);
			total += simpleOnsetChance;
			
			// Complex onset chance
			double complexOnsetChance = 0;
			if (p.maxOnsetLength > 1)
			{
				double r = p.baseOnsetClusterChance;
				complexOnsetChance = r * (Math.pow(r, p.maxOnsetLength) - 1) / (r - 1);	// why?
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
				name.add(p.pickSimpleOnset());
				return syllableLocationNode;
			}
			
			// Option 3: Complex onset
			else
			{
				name.add(p.pickComplexOnset());
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
			{
				curr++;
				return medialSyllableWeightNode;
			}
			
			// Option 2: Terminal syllable
			else
				return terminalSyllableWeightNode;
		}
	}
	
	private class MedialSyllableWeightNode implements Node
	{
		public Node nextNode()
		{
			double rand = rng.nextDouble();
			if ((pattern[curr] == 'S' && rand < p.strongHeavyRimeChance) || (pattern[curr] == 'w' && rand < p.weakHeavyRimeChance))
					return medialHeavyRimeNode;
				else
					return medialLightRimeNode;
		}
	}
	
	private class MedialLightRimeNode implements Node
	{
		double simpleInterludeProminence;
		
		public MedialLightRimeNode()
		{
			simpleInterludeProminence = p.baseOnsetChance * Math.log(p.data[Phonology.SIMPLE_ONSETS] + 1);
		}
		
		public Node nextNode()
		{
			// If this is hiatus, add an nucleus from the previous vowel's interlude list
			SyllableSegment prev = name.get(name.size() - 1);
			SyllableSegment next;
			
			if (prev.type == SegmentType.NUCLEUS)
				next = prev.lastPhoneme().pickInterlude(0);
			
			// Otherwise, add any available simple nucleus
			else
				next = p.pickSimpleNucleus();
			
			// Add nucleus
			name.add(next);
			
			// Decide whether to add next an empty interlude or a simple interlude
			// hiatus prominence = (complement of onset chance) scaled by log of the available hiatus count for this phoneme
			double emptyInterludeProminence = (1 - p.baseOnsetChance) * Math.log(next.lastPhoneme().interludes[0].size() + 1); 
			
			if (rng.nextDouble() * (simpleInterludeProminence + emptyInterludeProminence) < simpleInterludeProminence)
				name.add(p.pickSimpleOnset());
			
			// Rime complete; transition back to the syllable location node either way
			return syllableLocationNode;
		}
	}
	
	private class MedialHeavyRimeNode implements Node
	{
		double simpleNucleusProminence, complexNucleusProminence;
		
		public MedialHeavyRimeNode()
		{
			simpleNucleusProminence = (1 - p.baseDiphthongChance) * Math.log(p.nuclei[0].size() + 1);
			complexNucleusProminence = p.baseDiphthongChance * Math.log(p.nuclei[1].size() + 1);
		}
		
		public Node nextNode()
		{
			SyllableSegment prev = name.get(name.size() - 1);
			SyllableSegment next;
			
			// Hiatus: If the previous phoneme was a vowel, this nucleus must come from that vowel's interlude table
			if (prev.type == SegmentType.NUCLEUS)
			{
				if (rng.nextDouble() < prev.lastPhoneme().interludeLengthProbabilities[0])
					next = prev.lastPhoneme().pickInterlude(0);
				else
					next = prev.lastPhoneme().pickInterlude(1);
			}
			
			// Otherwise, we may choose a nucleus freely
			else
			{
				if (rng.nextDouble() * (simpleNucleusProminence + complexNucleusProminence) < simpleNucleusProminence)
					next = p.pickSimpleNucleus();
				else
					next = p.pickComplexNucleus();
			}
			
			// Add next syllable segment to name
			name.add(next);
			
			if (name.size() == 0)
				return complexInterludeNode;
			else
				return medialComplexNucleusNode;
		}
	}
	
	private class MedialComplexNucleusNode implements Node
	{
		double simpleInterludeProminence;
		double complexInterludeProminence;
		
		public MedialComplexNucleusNode()
		{
			simpleInterludeProminence = Math.log(p.data[Phonology.SIMPLE_ONSETS] + 1);
			simpleInterludeProminence *= p.baseOnsetChance; // multiply by P(onset)
			simpleInterludeProminence *= (1 - p.baseOnsetClusterChance); // multiply by P(!onset cluster | onset)
			simpleInterludeProminence *= (1 - p.medialCodaChance); // multiply by P(!coda | onset)
			
			complexInterludeProminence = Math.log(p.data[Phonology.COMPLEX_ONSETS] + p.data[Phonology.COMPOUND_INTERLUDES] + 1);
			complexInterludeProminence *= p.baseOnsetChance; // multiple by P(onset), as all complex interludes have an onset of length 1+
			
			// multiply by P((onset cluster | onset) OR (coda | onset)), which equals the complement of P(neither) 
			complexInterludeProminence *= (1 - (1 - p.baseOnsetClusterChance) * (1 - p.medialCodaChance));
		}
		
		public Node nextNode()
		{
			SyllableSegment nucleus = name.get(name.size() - 1);
			
			// Calculate emptyInterludeProminence, which depends on the number of interludes available to the last vowel
			double emptyInterludeProminence = Math.log(nucleus.lastPhoneme().interludes[0].size() + 1);
			emptyInterludeProminence *= (1 - p.baseOnsetChance);
			
			double rand = rng.nextDouble() * (emptyInterludeProminence + simpleInterludeProminence + complexInterludeProminence);
			
			// Empty interlude
			if (rand < emptyInterludeProminence)
				return syllableLocationNode;
			
			// Simple interlude
			else if (rand < emptyInterludeProminence + simpleInterludeProminence)
			{
				name.add(p.pickSimpleOnset());
				return syllableLocationNode;
			}
			
			// Complex interlude
			else
				return complexInterludeNode;
		}
	}
	
	private class ComplexInterludeNode implements Node
	{
		double complexOnsetChance;
		double compoundInterludeChance;
		
		public ComplexInterludeNode()
		{
			complexOnsetChance = Math.log(p.data[Phonology.COMPLEX_ONSETS] + 1);
			complexOnsetChance *= p.baseOnsetClusterChance;	// multiply by P(onset cluster | onset)
			complexOnsetChance *= (1 - p.medialCodaChance); // multiply by P(!coda | onset)
			
			compoundInterludeChance = Math.log(p.data[Phonology.COMPOUND_INTERLUDES] + 1);
			complexOnsetChance *= p.medialCodaChance;	// multiply by P(coda | onset)
			
			complexOnsetChance /= (complexOnsetChance + compoundInterludeChance);
			compoundInterludeChance = 1 - complexOnsetChance;
		}
		
		public Node nextNode()
		{
			if (rng.nextDouble() < complexOnsetChance)
				name.add(p.pickComplexOnset());
			else
			{
				SyllableSegment next = p.pickCoda();
				name.add(next);
				name.add(p.pickInterlude(next.lastPhoneme()));
			}
			
			return syllableLocationNode;
		}
	}
	
	private class TerminalSyllableWeightNode implements Node
	{
		double lightRimeProminence;
		double heavyRimeProminence;
		
		public TerminalSyllableWeightNode()
		{
			// As per TerminalLightRimeNode
			double lightRimeEmptyCoda = p.data[Phonology.SIMPLE_NUCLEI] * p.data[Phonology.SIMPLE_CODAS] * (1 - p.terminalCodaChance);
			double lightRimeSimpleCoda = p.data[Phonology.SIMPLE_NUCLEI] * p.terminalCodaChance;
			
			// As per TerminalHeavyRimeNode()
			double heavyRimeSimpleNucleus = p.data[Phonology.SIMPLE_NUCLEI];
			heavyRimeSimpleNucleus *= (p.data[Phonology.SIMPLE_CODAS] + p.data[Phonology.COMPLEX_CODAS]);
			heavyRimeSimpleNucleus *= p.terminalCodaChance;
			heavyRimeSimpleNucleus *= (1 - p.baseDiphthongChance);
			
			double heavyRimeComplexNucleus = p.data[Phonology.COMPLEX_NUCLEI];
			heavyRimeComplexNucleus *= (p.data[Phonology.SIMPLE_CODAS + p.data[Phonology.COMPLEX_CODAS]]);
			heavyRimeComplexNucleus *= p.terminalCodaChance;
			heavyRimeComplexNucleus += p.data[Phonology.COMPLEX_NUCLEI] * (1 - p.terminalCodaChance);
			heavyRimeComplexNucleus *= p.baseDiphthongChance;
			
			double lightRimeProminence = Math.log(lightRimeEmptyCoda + lightRimeSimpleCoda + 1);
			double heavyRimeProminence = Math.log(heavyRimeSimpleNucleus + heavyRimeComplexNucleus + 1);
		}
		
		public Node nextNode()
		{
			if (pattern[curr] == 'S')
			{
				double rand = Math.random() * (lightRimeProminence * p.strongLightRimeChance + heavyRimeProminence * p.strongHeavyRimeChance);
				if (rand < lightRimeProminence * p.strongLightRimeChance)
					return terminalLightRimeNode;
				else
					return terminalHeavyRimeNode;
			}
			else
			{
				double rand = Math.random() * (lightRimeProminence * p.weakLightRimeChance + heavyRimeProminence * p.weakHeavyRimeChance);
				if (rand < lightRimeProminence * p.weakLightRimeChance)
					return terminalLightRimeNode;
				else
					return terminalHeavyRimeNode;
			}
		}
	}
	
	private class TerminalLightRimeNode implements Node
	{
		double emptyCodaProminence;
		double simpleCodaProminence;
		
		public TerminalLightRimeNode()
		{
			emptyCodaProminence = Math.log(p.data[Phonology.SIMPLE_NUCLEI] * p.data[Phonology.SIMPLE_CODAS]) * (1 - p.terminalCodaChance);
			simpleCodaProminence = Math.log(p.data[Phonology.SIMPLE_NUCLEI]) * p.terminalCodaChance;
		}
		
		public Node nextNode()
		{
			// If this is hiatus, add an nucleus from the previous vowel's interlude list
			SyllableSegment prev = name.get(name.size() - 1);
			SyllableSegment next;
			
			if (prev.type == SegmentType.NUCLEUS)
			{
				next = prev.lastPhoneme().pickInterlude(0);
			}
			
			// Otherwise, add any available simple nucleus
			else
				next = p.pickSimpleNucleus();
			
			// Add nucleus
			name.add(next);
			
			// Decide whether to add next a simple coda or none at all.
			if (rng.nextDouble() * (simpleCodaProminence + emptyCodaProminence) < simpleCodaProminence)
				name.add(p.pickSimpleCoda());
			
			// Rime complete; return null to exit loop
			return null;
		}
	}
	
	private class TerminalHeavyRimeNode implements Node
	{
		double simpleNucleusProminence, complexNucleusProminence;
		
		public TerminalHeavyRimeNode()
		{
			// number of heavy rimes with simple nuclei
			simpleNucleusProminence = p.data[Phonology.SIMPLE_NUCLEI] * (p.data[Phonology.SIMPLE_CODAS] + p.data[Phonology.COMPLEX_CODAS]);
			
			// Scale the number of codas by the terminalCodaChance; we can't choose a simple nucleus if terminal codas aren't allowed!
			simpleNucleusProminence *= p.terminalCodaChance;
			
			// Take the log and scale by the complement of the base diphthong chance
			simpleNucleusProminence =  Math.log(simpleNucleusProminence + 1) * (1 - p.baseDiphthongChance);
			
			// Start with the number of heavy rimes with complex nuclei
			complexNucleusProminence = p.data[Phonology.COMPLEX_NUCLEI] * (p.data[Phonology.SIMPLE_CODAS + p.data[Phonology.COMPLEX_CODAS]]);
			
			// Scale the number of coda-bearing rimes by the terminalCodaChance
			complexNucleusProminence *= p.terminalCodaChance;
			
			// Add the number of coda-free rimes, scaled by their own chance
			complexNucleusProminence += p.data[Phonology.COMPLEX_NUCLEI] * (1 - p.terminalCodaChance);
			
			// Take the log and scale by the base diphthong chance
			complexNucleusProminence = Math.log(complexNucleusProminence + 1) * p.baseDiphthongChance;
		}
		
		public Node nextNode()
		{
			SyllableSegment prev = name.get(name.size() - 1);
			SyllableSegment next;
			
			// Hiatus: If the previous phoneme was a vowel, this nucleus must come from that vowel's interlude table
			if (prev.type == SegmentType.NUCLEUS)
			{
				// calculate nucleus prominences based on the above formula but using the hiatus lists instead of the general coda lists
				double simple = prev.lastPhoneme().interludes[0].size() * (p.data[Phonology.SIMPLE_CODAS] + p.data[Phonology.COMPLEX_CODAS]);
				simple *= p.terminalCodaChance;
				simple = Math.log(simple + 1) * (1 - p.baseDiphthongChance);
				
				double complex = prev.lastPhoneme().interludes[1].size() * (p.data[Phonology.SIMPLE_CODAS + p.data[Phonology.COMPLEX_CODAS]]);
				complex *= p.terminalCodaChance;
				complex += prev.lastPhoneme().interludes[1].size() * (1 - p.terminalCodaChance);
				complex = Math.log(complex + 1) * p.baseDiphthongChance;				
				
				if (rng.nextDouble() * (simple + complex) < simple)
					next = prev.lastPhoneme().pickInterlude(0);
				else
					next = prev.lastPhoneme().pickInterlude(1);
			}
			
			// Otherwise, we may choose a nucleus freely
			else
			{
				if (rng.nextDouble() * (simpleNucleusProminence + complexNucleusProminence) < simpleNucleusProminence)
					next = p.pickSimpleNucleus();
				else
					next = p.pickComplexNucleus();
			}
			
			// Add next syllable segment to name
			name.add(next);
			
			// Select next node
			if (name.size() == 0)
				return terminalSimpleNucleusNode;
			else
				return terminalComplexNucleusNode;
		}
	}
	
	public class TerminalHeavyRimeSimpleNucleusNode implements Node
	{
		double simpleCodaProminence;
		double complexCodaProminence;
		
		public TerminalHeavyRimeSimpleNucleusNode()
		{
			simpleCodaProminence = Math.log(p.data[Phonology.SIMPLE_CODAS] + 1) * (1 - p.baseCodaClusterChance);
			simpleCodaProminence *= Math.log(p.data[Phonology.COMPLEX_CODAS] + 1) * p.baseCodaClusterChance;
		}
		
		public Node nextNode()
		{
			if (rng.nextDouble() * (simpleCodaProminence + complexCodaProminence) < simpleCodaProminence)
				name.add(p.pickSimpleCoda());
			else
				name.add(p.pickComplexCoda());
			
			return null;
		}
	}
	
	public class TerminalHeavyRimeComplexNucleusNode implements Node
	{
		double emptyCodaProminence;
		double simpleCodaProminence;
		double complexCodaProminence;
		
		public TerminalHeavyRimeComplexNucleusNode()
		{
			emptyCodaProminence = 1 - p.terminalCodaChance;
			simpleCodaProminence = Math.log(p.data[Phonology.SIMPLE_CODAS] + 1) * p.terminalCodaChance * (1 - p.baseCodaClusterChance);
			complexCodaProminence = Math.log(p.data[Phonology.COMPLEX_CODAS] + 1) * p.terminalCodaChance * p.baseCodaClusterChance;
		}
		
		public Node nextNode()
		{
			double rand = rng.nextDouble() * (emptyCodaProminence + simpleCodaProminence + complexCodaProminence);
			
			if (rand < simpleCodaProminence)
				name.add(p.pickSimpleCoda());
			else if (rand < complexCodaProminence)
				name.add(p.pickComplexCoda());
			
			return null;
		}
	}
}


