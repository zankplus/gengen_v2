package Gengen_v2.gengenv2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Random;

import Gengen_v2.gengenv2.Phonology.SyllableSegment;

class Flowchart
{
	Phonology p;
	Node startNode, syllableLocationNode, medialSyllableWeightNode, medialLightRimeNode, medialHeavyRimeNode,
		 medialComplexNucleusNode, complexInterludeNode,
		 terminalSyllableWeightNode, terminalLightRimeNode, terminalHeavyRimeNode,
		 terminalHeavyRimeSimpleNucleusNode, terminalHeavyRimeComplexNucleusNode;
	
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
		
		stressSystem = new StressSystem(p.rng.nextLong());
		startNode = new StartNode();
		syllableLocationNode = new SyllableLocationNode();
		medialSyllableWeightNode = new MedialSyllableWeightNode();
		medialLightRimeNode = new MedialLightRimeNode();
		medialHeavyRimeNode = new MedialHeavyRimeNode();
		medialComplexNucleusNode = new MedialComplexNucleusNode();
		complexInterludeNode = new ComplexInterludeNode();
		terminalSyllableWeightNode = new TerminalSyllableWeightNode();
		terminalLightRimeNode = new TerminalLightRimeNode();
		terminalHeavyRimeNode = new TerminalHeavyRimeNode();
		terminalHeavyRimeSimpleNucleusNode = new TerminalHeavyRimeSimpleNucleusNode();
		terminalHeavyRimeComplexNucleusNode = new TerminalHeavyRimeComplexNucleusNode(); 
		
		makeAdjustedCounts();
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
	
	public void makeWords(int names)
	{
		ArrayList<String> list = new ArrayList<String>(names);
		
		for (int i = 0; i < names; i++)
		{
			name = new ArrayList<SyllableSegment>();
			pattern = stressSystem.makePattern().toCharArray();
			
			Node node = startNode;
			while (node != null)
			{
				node = node.nextNode();
			}
		
			StringBuilder nameBuilder;
			
			nameBuilder = new StringBuilder();
			for (SyllableSegment ss : name)
			{
				nameBuilder.append(ss);
			}
			list.add(nameBuilder.toString());
		}
		
		Collections.sort(list);
		
		for (String name : list)
			System.out.println(name.substring(0, 1).toUpperCase() + name.substring(1));
	}
	
	interface Node
	{
		public Node nextNode();
	}

	private class StartNode implements Node
	{
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
			curr = -1;
			
			// Option 1: Empty onset
			if (rand < emptyOnsetChance)
			{
				return syllableLocationNode;
			}

			// Option 2: Simple onset
			else if (rand < emptyOnsetChance + simpleOnsetChance)
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
			if (curr < pattern.length - 1)
			{
				curr++;
				return medialSyllableWeightNode;
			}
			
			// Option 2: Terminal syllable
			else
			{
				return terminalSyllableWeightNode;
			}
		}
	}
	
	private class MedialSyllableWeightNode implements Node
	{
		double complexNucleusSimpleInterlude;
		double complexNucleusComplexInterlude;
		double complexInterlude;
		
		public MedialSyllableWeightNode()
		{
			// As in MedialComplexNucleusNode
			complexInterlude = p.data[Phonology.COMPLEX_ONSETS] * p.baseOnsetClusterChance * (1 - p.medialCodaChance);
			complexInterlude += p.data[Phonology.COMPOUND_INTERLUDES] * p.medialCodaChance;
			
			// As in MedialComplexNucleusNode
			complexNucleusSimpleInterlude = p.data[Phonology.SIMPLE_ONSETS];
			complexNucleusSimpleInterlude *= p.baseOnsetChance; // multiply by P(onset)
			complexNucleusSimpleInterlude *= (1 - p.baseOnsetClusterChance); // multiply by P(!onset cluster | onset)
			complexNucleusSimpleInterlude *= (1 - p.medialCodaChance); // multiply by P(!coda | onset)
			
			complexNucleusComplexInterlude = complexInterlude *= p.baseOnsetChance;
		}
		
		public Node nextNode()
		{
			SyllableSegment prev = null;
			if (name.size() > 0)
				prev = name.get(name.size() - 1);
		
			double lightRimeProminence;
			double heavyRimeProminence;
			
			if (prev != null && prev.type == SegmentType.NUCLEUS)
			{
				// hiatus case
				double simpleNucleusSimpleInterlude = prev.lastPhoneme().interludes[0].size() * p.data[Phonology.SIMPLE_ONSETS];
				simpleNucleusSimpleInterlude *= p.baseOnsetChance;
				double simpleNucleusEmptyInterlude = 0;
				for (int i = 0; i < prev.lastPhoneme().interludes[0].size(); i++)
					if (prev.lastPhoneme().interludes[0].get(i).ss.lastPhoneme().interludes[0].size() > 0)
						simpleNucleusEmptyInterlude++;
				simpleNucleusEmptyInterlude *= (1 - p.baseOnsetChance);
				
				double heavySimple = prev.lastPhoneme().interludes[0].size() * complexInterlude * (1 - p.baseDiphthongChance); 
				
				double heavyComplex = 0;
				if (p.maxNucleusLength > 1)
				{
					for (int i = 0; i < prev.lastPhoneme().interludes[1].size(); i++)
						if (prev.lastPhoneme().interludes[1].get(i).ss.lastPhoneme().interludes[0].size() > 0)
							heavyComplex++;
					heavyComplex *= (1 - p.baseOnsetChance); // now represents empty interlude prominence
					heavyComplex +=(complexNucleusSimpleInterlude + complexNucleusComplexInterlude) * prev.lastPhoneme().interludes[1].size();
					heavyComplex *= p.baseDiphthongChance;
				}
				
				lightRimeProminence = Math.log(simpleNucleusSimpleInterlude + simpleNucleusEmptyInterlude + 1);
				heavyRimeProminence = Math.log(heavySimple + heavyComplex + 1);
			}
			else
			{
				// no hiatus case
				double simpleNucleusSimpleInterlude = p.data[Phonology.SIMPLE_NUCLEI] * p.data[Phonology.SIMPLE_ONSETS];
				simpleNucleusSimpleInterlude *= p.baseOnsetChance;
				double simpleNucleusEmptyInterlude = p.data[Phonology.SIMPLE_NUCLEI_WITH_HIATUS] * (1 - p.baseOnsetChance);
				
				
				double heavySimple = p.data[Phonology.SIMPLE_NUCLEI] * complexInterlude * (1 - p.baseDiphthongChance);
				
				double heavyComplex = 0; 
				if (p.maxNucleusLength > 1)
					for (int i = 0; i < p.nuclei[1].size(); i++)
						if (p.nuclei[1].get(i).lastPhoneme().interludes[0].size() > 0)
							heavyComplex++;
				heavyComplex = heavyComplex * (1 - p.baseOnsetChance); // now represents empty interlude prominence
				heavyComplex += (complexNucleusSimpleInterlude + complexNucleusComplexInterlude) // add simple and complex interlude prominence,
							  * p.data[Phonology.COMPLEX_NUCLEI]; 		 			// multiplied by number of complex nuclei available
				heavyComplex *= p.baseDiphthongChance; // take the log and multiply by base diphthong chance
				
				lightRimeProminence = Math.log(simpleNucleusSimpleInterlude + simpleNucleusEmptyInterlude + 1);
				heavyRimeProminence = Math.log(heavySimple + heavyComplex + 1);
			}
			
			
			double rand = rng.nextDouble();
			if (pattern[curr] == 'S')
			{
				rand *= (lightRimeProminence * p.strongLightRimeChance) + (heavyRimeProminence * p.strongHeavyRimeChance);
				if (rand < lightRimeProminence * p.strongLightRimeChance)
					return medialLightRimeNode;
				else
					return medialHeavyRimeNode;
			}
			else
			{
				rand *= (lightRimeProminence * p.weakLightRimeChance) + (heavyRimeProminence * p.weakHeavyRimeChance);
				if (rand < lightRimeProminence * p.weakLightRimeChance)
					return medialLightRimeNode;
				else
					return medialHeavyRimeNode;
			}
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
			SyllableSegment prev = null;
			if (name.size() > 0)
				prev = name.get(name.size() - 1);
			SyllableSegment next;
			
			if (prev != null && prev.type == SegmentType.NUCLEUS)
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
		double simpleInterludeProminence;
		double complexInterludeProminence;
		double baseComplexInterludeProminence;
		
		public MedialHeavyRimeNode()
		{
			baseComplexInterludeProminence = p.data[Phonology.COMPLEX_ONSETS] * p.baseOnsetClusterChance * (1 - p.medialCodaChance);
			baseComplexInterludeProminence += p.data[Phonology.COMPOUND_INTERLUDES] * p.medialCodaChance;
			
			// As in MedialComplexNucleusNode
			simpleInterludeProminence = p.data[Phonology.SIMPLE_ONSETS];
			simpleInterludeProminence *= p.baseOnsetChance; // multiply by P(onset)
			simpleInterludeProminence *= (1 - p.baseOnsetClusterChance); // multiply by P(!onset cluster | onset)
			simpleInterludeProminence *= (1 - p.medialCodaChance); // multiply by P(!coda | onset)
			
			complexInterludeProminence = baseComplexInterludeProminence *= p.baseOnsetChance; 
		}
		
		public Node nextNode()
		{
			SyllableSegment prev = null;
			if (name.size() > 0)
				prev = name.get(name.size() - 1);
			SyllableSegment next;
			
			// Hiatus: If the previous phoneme was a vowel, this nucleus must come from that vowel's interlude table
			if (prev != null && prev.type == SegmentType.NUCLEUS)
			{
				double simple = prev.lastPhoneme().interludes[0].size() * baseComplexInterludeProminence;
				simple = Math.log(simple + 1) * (1 - p.baseDiphthongChance);
				
				double complex = 0;
				
				// Count the number of hiatus available for every hiatus available to the previous vowel
				if (p.maxNucleusLength > 1)
				{
					for (int i = 0; i < prev.lastPhoneme().interludes[1].size(); i++)
						if (prev.lastPhoneme().interludes[1].get(i).ss.lastPhoneme().interludes[0].size() > 0)
							complex ++;
					
					complex = complex * (1 - p.baseOnsetChance); // now represents empty interlude prominence
					complex += (simpleInterludeProminence + complexInterludeProminence) // add simple and complex interlude prominence,
								* prev.lastPhoneme().interludes[1].size();  			// multiplied by number of simple nuclei available
					complex = Math.log(complex + 1) * p.baseDiphthongChance; // take the log and multiply by base diphthong chance
				}
				
				if (rng.nextDouble() * (simple + complex) < simple)
					next = prev.lastPhoneme().pickInterlude(0);
				else
				{
					prev.lastPhoneme().printInterludes();
					next = prev.lastPhoneme().pickInterlude(1);
				}
				
			}
			
			// Otherwise, we may choose a nucleus freely
			else
			{
				double simple = p.data[Phonology.SIMPLE_NUCLEI] * baseComplexInterludeProminence;
				simple = Math.log(simple + 1) * (1 - p.baseDiphthongChance);
				
				double complex = 0; 
				if (p.maxNucleusLength > 1)
					for (int i = 0; i < p.nuclei[1].size(); i++)
						if (p.nuclei[1].get(i).lastPhoneme().interludes[0].size() > 0)
							complex++;
				complex = complex * (1 - p.baseOnsetChance); // now represents empty interlude prominence
				complex += (simpleInterludeProminence + complexInterludeProminence) // add simple and complex interlude prominence,
							* p.data[Phonology.COMPLEX_NUCLEI]; 		 			// multiplied by number of complex nuclei available
				complex = Math.log(complex + 1) * p.baseDiphthongChance; // take the log and multiply by base diphthong chance
				
				if (rng.nextDouble() * (simple + complex) < simple)
					next = p.pickSimpleNucleus();
				else
					next = p.pickComplexNucleus();
			}
			
			// Add next syllable segment to name
			name.add(next);
			
			if (next.content.length == 1)
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

			// multiple by P(onset), as all complex interludes have an onset of length 1+
			complexInterludeProminence *= p.baseOnsetChance; 
			
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
		double complexOnsetProminence;
		double compoundInterludeProminence;
		
		public ComplexInterludeNode()
		{
			complexOnsetProminence = Math.log(p.data[Phonology.COMPLEX_ONSETS] + 1);
			complexOnsetProminence *= p.baseOnsetClusterChance;	// multiply by P(onset cluster | onset)
			complexOnsetProminence *= (1 - p.medialCodaChance); // multiply by P(!coda | onset)
			
			compoundInterludeProminence = Math.log(p.data[Phonology.COMPOUND_INTERLUDES] + 1);
			complexOnsetProminence *= p.medialCodaChance;	// multiply by P(coda | onset)
		}
		
		public Node nextNode()
		{
			if (rng.nextDouble() * (complexOnsetProminence + compoundInterludeProminence) < complexOnsetProminence)
				name.add(p.pickComplexOnset());
			else
			{
				// Add any coda and then an interlude that that coda's final phoneme's interlude list
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
			heavyRimeComplexNucleus *= (p.data[Phonology.SIMPLE_CODAS] + p.data[Phonology.COMPLEX_CODAS]);
			heavyRimeComplexNucleus *= p.terminalCodaChance;
			heavyRimeComplexNucleus += p.data[Phonology.COMPLEX_NUCLEI] * (1 - p.terminalCodaChance);
			heavyRimeComplexNucleus *= p.baseDiphthongChance;
			
			double lightRimeProminence = Math.log(lightRimeEmptyCoda + lightRimeSimpleCoda + 1);
			double heavyRimeProminence = Math.log(heavyRimeSimpleNucleus + heavyRimeComplexNucleus + 1);
		}
		
		public Node nextNode()
		{
			SyllableSegment prev = null;
			if (name.size() > 0)
				prev = name.get(name.size() - 1);
		
			double lightRimeProminence;
			double heavyRimeProminence;
			
			if (prev != null && prev.type == SegmentType.NUCLEUS)
			{
				// hiatus case
				double simpleNucleusSimpleCoda = prev.lastPhoneme().interludes[0].size() * p.data[Phonology.SIMPLE_CODAS];
				simpleNucleusSimpleCoda *= p.baseOnsetChance;
				double simpleNucleusEmptyCoda = prev.lastPhoneme().interludes[0].size();
				simpleNucleusEmptyCoda *= (1 - p.baseOnsetChance);
				
				double heavySimple = prev.lastPhoneme().interludes[0].size() *
						(p.data[Phonology.SIMPLE_CODAS] * (1 - p.baseCodaClusterChance) +
						 p.data[Phonology.COMPLEX_CODAS] * p.baseCodaClusterChance);
				heavySimple *= (1 - p.baseDiphthongChance);
				
				double heavyComplex = 0;
				if (p.maxNucleusLength > 1)
				{
					heavyComplex += 1;
					heavyComplex *= (1 - p.terminalCodaChance); // now represents empty interlude prominence
					heavyComplex += p.data[Phonology.SIMPLE_CODAS] * p.terminalCodaChance * (1 - p.baseCodaClusterChance);
					heavyComplex += p.data[Phonology.COMPLEX_CODAS] * p.terminalCodaChance * p.baseCodaClusterChance;
					heavyComplex *= prev.lastPhoneme().interludes[1].size() * p.baseDiphthongChance;
				}
				
				lightRimeProminence = Math.log(simpleNucleusEmptyCoda + simpleNucleusSimpleCoda + 1);
				heavyRimeProminence = Math.log(heavySimple + heavyComplex + 1);
			}
			else
			{
				// non-hiatus case
				double simpleNucleusSimpleCoda = p.data[Phonology.SIMPLE_NUCLEI] * p.data[Phonology.SIMPLE_CODAS];
				simpleNucleusSimpleCoda *= p.baseOnsetChance;
				double simpleNucleusEmptyCoda = p.data[Phonology.SIMPLE_NUCLEI];
				simpleNucleusEmptyCoda *= (1 - p.baseOnsetChance);
				
				double heavySimple = p.data[Phonology.SIMPLE_NUCLEI] *
						(p.data[Phonology.SIMPLE_CODAS] * (1 - p.baseCodaClusterChance) +
						 p.data[Phonology.COMPLEX_CODAS] * p.baseCodaClusterChance);
				heavySimple *= (1 - p.baseDiphthongChance);
				
				double heavyComplex = 0;
				if (p.maxNucleusLength > 1)
				{
					heavyComplex += 1;
					heavyComplex *= (1 - p.terminalCodaChance); // now represents empty interlude prominence
					heavyComplex += p.data[Phonology.SIMPLE_CODAS] * p.terminalCodaChance * (1 - p.baseCodaClusterChance);
					heavyComplex += p.data[Phonology.COMPLEX_CODAS] * p.terminalCodaChance * p.baseCodaClusterChance;
					heavyComplex *= p.data[Phonology.COMPLEX_NUCLEI] * p.baseDiphthongChance;
				}
				
				lightRimeProminence = Math.log(simpleNucleusEmptyCoda + simpleNucleusSimpleCoda + 1);
				heavyRimeProminence = Math.log(heavySimple + heavyComplex + 1);
			}
			
			
			double rand = rng.nextDouble();
			if (pattern[curr] == 'S')
			{
				rand *= (lightRimeProminence * p.strongLightRimeChance) + (heavyRimeProminence * p.strongHeavyRimeChance);
				if (rand < lightRimeProminence * p.strongLightRimeChance)
					return terminalLightRimeNode;
				else
					return terminalHeavyRimeNode;
			}
			else
			{
				rand *= (lightRimeProminence * p.weakLightRimeChance) + (heavyRimeProminence * p.weakHeavyRimeChance);
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
			emptyCodaProminence = Math.log(p.data[Phonology.SIMPLE_NUCLEI]) * (1 - p.terminalCodaChance);
			simpleCodaProminence = Math.log(p.data[Phonology.SIMPLE_NUCLEI] * p.data[Phonology.SIMPLE_CODAS]) * p.terminalCodaChance;
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
			System.out.println(simpleCodaProminence + " " + emptyCodaProminence);
			if (rng.nextDouble() * (simpleCodaProminence + emptyCodaProminence) < simpleCodaProminence)
				name.add(p.pickSimpleCoda());
			
			// Rime complete; return null to exit loop
			return null;
		}
	}
	
	private class TerminalHeavyRimeNode implements Node
	{
		double codaProminence;
		double complexNucleusProminence;
		
		public TerminalHeavyRimeNode()
		{
			// adjusted number of codas possible
			codaProminence = p.data[Phonology.SIMPLE_CODAS] * (1 - p.baseCodaClusterChance);
			codaProminence += p.data[Phonology.COMPLEX_CODAS] * p.baseCodaClusterChance;
		}
		
		public Node nextNode()
		{
			SyllableSegment prev = null;
			if (name.size() > 0)
				prev = name.get(name.size() - 1);
			SyllableSegment next;
			
			// Hiatus: If the previous phoneme was a vowel, this nucleus must come from that vowel's interlude table
			if (prev != null && prev.type == SegmentType.NUCLEUS)
			{
				// calculate nucleus prominences based on the above formula but using the hiatus lists instead of the general coda lists
				double simple = prev.lastPhoneme().interludes[0].size() * codaProminence;
				simple = Math.log(simple + 1) * (1 - p.baseDiphthongChance);
				
				double complex = 0;
				
				if (p.maxNucleusLength > 1)
				{
					complex += prev.lastPhoneme().interludes[1].size() * codaProminence;
					complex *= p.terminalCodaChance;
					complex += prev.lastPhoneme().interludes[1].size() * (1 - p.terminalCodaChance);
					complex = Math.log(complex + 1) * p.baseDiphthongChance;
				}
				
				if (rng.nextDouble() * (simple + complex) < simple)
					next = prev.lastPhoneme().pickInterlude(0);
				else
					next = prev.lastPhoneme().pickInterlude(1);
			}
			
			// Otherwise, we may choose a nucleus freely
			else
			{
				double simple = p.data[Phonology.SIMPLE_NUCLEI] * codaProminence;
				simple = Math.log(simple + 1) * (1 - p.baseDiphthongChance);
				
				double complex = p.data[Phonology.COMPLEX_NUCLEI] * codaProminence;
				complex *= p.terminalCodaChance;
				complex += p.data[Phonology.COMPLEX_NUCLEI] * (1 - p.terminalCodaChance);
				complex = Math.log(complex + 1) * p.baseDiphthongChance;	
				
				if (rng.nextDouble() * (simple + complex) < simple)
					next = p.pickSimpleNucleus();
				else
					next = p.pickComplexNucleus();
			}
			
			// Add next syllable segment to name
			name.add(next);
						
			// Select next node
			if (name.size() == 0)
				return terminalHeavyRimeSimpleNucleusNode;
			else
				return terminalHeavyRimeComplexNucleusNode;
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


