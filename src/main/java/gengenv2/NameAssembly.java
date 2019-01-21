/** Copyright 2018 Clayton Cooper
 *	
 *	This file is part of gengen2.
 *
 *	gengen2 is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	gengen2 is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with gengen2.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package gengenv2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Random;

import gengenv2.Phonology.Constituent;

/**
 * A sort of flowchart or state machine for generating names according to a Phonology's inventory, phonotactics, 
 * and stress system. Though the flowchart is structurally the same regardless of the anguage, its weights depend
 * on its Phonology's features, so every Phonology needs its own custom copy.
 * 
 * @author	Clayton Cooper
 * @version	1.0
 * @since	1.0
 */
class NameAssembly
{
	Random rng;
	Phonology p;
	
	// Flowchart nodes
	public Node startNode, syllableLocationNode, medialSyllableWeightNode, medialLightRimeNode, medialHeavyRimeNode,
		 medialComplexNucleusNode, complexInterludeNode,
		 terminalSyllableWeightNode, terminalLightRimeNode, terminalHeavyRimeNode, terminalHeavyRimeComplexNucleusNode;
	
	// Current name variables
	Name name;				// The name currently being generated
	int length;				// The intended length of the currently generated word, in syllables
	int curr;				// The syllable of the current name currently being generated
	Constituent prev;		// The most recent syllable constituent added to the name
	
	/**
	 * Constructor simply initializes all Nodes in the assembly flowchart, and saves the reference to the
	 * given Phonology as well as its RNG.
	 * 
	 * @param	p	The Phonology to which this NameAssembly belongs	
	 */
	public NameAssembly(Phonology p)
	{
		this.p = p;
		rng = p.rng;
	
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
		terminalHeavyRimeComplexNucleusNode = new TerminalHeavyRimeComplexNucleusNode(); 
		
//		makeAdjustedCounts();
	}
	
	/*public void makeAdjustedCounts()
	{
		double medialLightRime;
		double medialHeavyRimeSimpleNucleus;
		double medialHeavyRimeComplexNucleus;
		
		// number of light rimes with simple interludes
		medialLightRime = p.counts[Phonology.SIMPLE_NUCLEI] * p.counts[Phonology.SIMPLE_NUCLEI] * p.baseMedialOnsetChance;
		
		// add number of light rimes with empty interludes
		medialLightRime += p.counts[Phonology.SIMPLE_NUCLEI_WITH_HIATUS] * (1 - p.baseMedialOnsetChance); 
		
		// number of heavy rimes with simple nuclei and complex onsets
		medialHeavyRimeSimpleNucleus = p.counts[Phonology.SIMPLE_NUCLEI] * p.counts[Phonology.COMPLEX_ONSETS];
		
		// scale by chance of onset, complex onset | onset, !coda | onset
		medialHeavyRimeSimpleNucleus *= p.baseMedialOnsetChance;
		medialHeavyRimeSimpleNucleus *= p.baseOnsetClusterChance;
		medialHeavyRimeSimpleNucleus *= (1 - p.baseMedialCodaChance);
		
		// number of heavy rimes with simple nuclei an compound interludes
		// scale by chance of onset, coda | onset
		medialHeavyRimeSimpleNucleus += p.counts[Phonology.SIMPLE_NUCLEI] * p.counts[Phonology.COMPOUND_INTERLUDES] *
										p.baseMedialOnsetChance * p.baseMedialCodaChance;
		
		double terminalLightRime;
		double terminalHeavyRimeSimpleNucleus;	
		double terminalHeavyRimeComplexNucleus;
		
		// number of light rimes with simple codas
		terminalLightRime = p.counts[Phonology.SIMPLE_CODAS] * p.baseTerminalCodaChance;
		
		// add number of light rimes with no codas
		terminalLightRime += (1 - p.baseTerminalCodaChance);
		
		// number of heavy rimes with simple nuclei
		terminalHeavyRimeSimpleNucleus = (p.counts[Phonology.SIMPLE_CODAS] + p.counts[Phonology.COMPLEX_CODAS]);
		
		// Scale the number of codas by the terminalCodaChance; we can't choose a simple nucleus if terminal codas aren't allowed!
		terminalHeavyRimeSimpleNucleus *= p.baseTerminalCodaChance;
		
		// Scale the complement of the diphthong chance
		terminalHeavyRimeSimpleNucleus *= (1 - p.baseDiphthongChance);
		
		// Start with the number of heavy rimes with complex nuclei
		terminalHeavyRimeComplexNucleus = (p.counts[Phonology.SIMPLE_CODAS] + p.counts[Phonology.COMPLEX_CODAS]);
		
		// Scale the number of coda-bearing rimes by the terminalCodaChance
		terminalHeavyRimeComplexNucleus *= p.baseTerminalCodaChance;
		
		// Add the number of coda-free rimes, scaled by their own chance
		terminalHeavyRimeComplexNucleus += (1 - p.baseTerminalCodaChance);
		
		// Scale by the diphthong chance
		terminalHeavyRimeComplexNucleus *= p.baseDiphthongChance;
	}*/
	
	/**
	 * Generates a name by first resetting the naming variables and then invoking the StartNode. This initiates
	 * a decision process that propagates through all the Nodes in the flowchart, each of which may add a
	 * SyllableSegment (or two, for interludes) to the name list
	 * @return	The completed name
	 * @since	1.0
	 */
	protected Name makeName()
	{
		// Initialize naming variables
		curr = 0;
		length = rng.nextInt(5) + 1;
		name = new Name();
		prev = null;
		
		// Propagate through the flowchart until one of the nodes returns null
		Node node = startNode;

		try
		{
			while (node != null)
			node = node.nextNode();
		} catch (Exception e)
		{
			System.err.println(name);
			e.printStackTrace();
			System.exit(0);
		}
		
		name.scan();

		return name;
	}
	
	/**
	 * A simple interface implemented by all Nodes in the name assembly flowchart, to ensure that they can all
	 * be called upon to hand over the next Node in the sequence.
	 * @since	1.0
	 */
	private interface Node
	{
		double lightRimeProminence = 0;

		public Node nextNode();
	}

	/**
	 * The first node called by makeName(). Its job is to reset the naming variables and pick an initial onset before
	 * advancing to the SyllableLocationNode.
	 * @since 1.0
	 */
	private class StartNode implements Node
	{
		double emptyOnsetChance, nonemptyOnsetChance;
		
		public StartNode()
		{
			// Empty onset chance
			emptyOnsetChance = p.emptyInitialOnsetProminence * Math.log(Math.pow(p.counts[Phonology.SIMPLE_NUCLEI] + p.counts[Phonology.COMPLEX_NUCLEI] + 1, 2));
			
			// Simple onset chance
			double simpleOnsetChance = p.counts[Phonology.SIMPLE_ONSETS] + 1;
			
			// Complex onset chance
			double complexOnsetChance = 0;
			for (int i = 1; i < p.maxOnsetLength; i++)
			{
				// For each length of onset cluster allowed, add an amount equal to the product of the number of clusters of that
				// length in the inventory and the base onset cluster chance raised to the power of the length minus 1 (=i)
				complexOnsetChance += p.onsets[i].size() * Math.pow(p.baseOnsetClusterChance, i);
			}

			/* Scaling each prominence value by the log of the number of possibilities it represents
			 * helps ensure that rich inventories are well represented while poor ones are sampled
			 * less frequently. This reduces the chances of something like a language with only 1
			 * onset cluster having that cluster at the start of 20% of names.
			 * This technique is repeated abundantly throughout the Node classes.
			 */
			nonemptyOnsetChance  = Math.log(simpleOnsetChance + complexOnsetChance + 1);
			
			// Normalize chances
			emptyOnsetChance = emptyOnsetChance / (emptyOnsetChance + nonemptyOnsetChance);
			nonemptyOnsetChance = 1 - emptyOnsetChance;
		}
		
		public Node nextNode()
		{	
			double rand = p.rng.nextDouble();
			
			// Option 1: Empty onset
			if (rand < emptyOnsetChance)
			{
				return syllableLocationNode;
			}

			// Option 2: Nonempty onset
			else
			{
				addConstituent(p.pickOnset());
				return syllableLocationNode;
			}
		}
	}
	
	/**
	 * Decides whether to add a medial syllable rime (which ends in an onset, a coda and onset, or just a nucleus), 
	 * or a terminal syllable rime (ending in coda or just a nucleus), depending on the current position in the word's
	 * stress pattern.
	 * 
	 * Note that the use of 'rime' herein includes the onset of the next syllable, which is contrary to technical usage in
	 * linguistics. In gengen, medial onsets are grouped with the previous syllable as they contribute to that syllable's
	 * weight rather than its own, as per the Latin scansion rules on which gengen's syllable weighting is based.
	 * @since	1.0
	 */
	private class SyllableLocationNode implements Node
	{
		public Node nextNode()
		{
			// Option 1: Initial/medial syllable
			if (curr < length - 1)
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
	
	/**
	 * Decides whether to add a light or heavy rime to a medial syllable in the current name, then transitions to
	 * the MedialLightRimeNode or MedialHeavyRimeNode accordingly.
	 * 
	 * Unlike the syllable location node, this node (and all subsequent) is decided probabilistically, based on the number of
	 * possible features each following node may add. This is a mathematically complex process that involves counting the
	 * number of every feature referenced throughout the remainder of the medial branch of the tree; furthermore, it must
	 * be performed every time, as the number of rimes possible depends on the number of nuclei available, which can vary
	 * if this syllable is onset-free following a hiatus.  
	 * @since 1.0
	 */
	private class MedialSyllableWeightNode implements Node
	{
		public double lightRimeProminence;
		double heavyRimeProminence;
		
		public MedialSyllableWeightNode()
		{
			// light rimes
			double simpleNucleusSimpleInterlude = p.counts[Phonology.SIMPLE_NUCLEI] * p.counts[Phonology.SIMPLE_ONSETS];
			double simpleNucleusEmptyInterlude = p.counts[Phonology.SIMPLE_NUCLEI_WITH_HIATUS];
			
			// heavy rimes
			double heavySimple = p.counts[Phonology.SIMPLE_NUCLEI] * (p.counts[Phonology.COMPLEX_ONSETS] + p.counts[Phonology.COMPOUND_INTERLUDES]);
			
			double heavyComplex = 0; 
			if (p.maxNucleusLength > 1)
			{
				heavyComplex += p.counts[Phonology.SIMPLE_ONSETS] + p.counts[Phonology.COMPLEX_ONSETS] + p.counts[Phonology.COMPOUND_INTERLUDES];
				heavyComplex *= p.counts[Phonology.COMPLEX_NUCLEI];
				heavyComplex += p.counts[Phonology.COMPLEX_NUCLEI_WITH_HIATUS];
			}
			
			lightRimeProminence = Math.log(simpleNucleusSimpleInterlude + simpleNucleusEmptyInterlude + 1);
			heavyRimeProminence = Math.log(heavySimple + heavyComplex + 1);
			
			double sum = lightRimeProminence + heavyRimeProminence;
			lightRimeProminence /= sum;
			heavyRimeProminence /= sum;
			
			System.out.println("Medial light rime:\t" + lightRimeProminence);
			System.out.println("Medial heavy rime:\t" + heavyRimeProminence);
		}
		
		public Node nextNode()
		{
			// If the previous syllable segment was a nucleus, hiatus occurs, and the following nucleus must be added
			// from the previous one's interlude inventory. 
			
			if (prev != null && prev.type == SegmentType.NUCLEUS)
			{
				// light rimes
				double simpleNucleusSimpleInterlude = prev.lastPhoneme().interludes[0].size() * p.counts[Phonology.SIMPLE_ONSETS];

				double simpleNucleusEmptyInterlude = 0;
				for (int i = 0; i < prev.lastPhoneme().interludes[0].size(); i++)
					if (prev.lastPhoneme().interludes[0].get(i).c.lastPhoneme().interludes[0].size() > 0)
						simpleNucleusEmptyInterlude++;
				
				// heavy rimes
				double heavySimple = prev.lastPhoneme().interludes[0].size() * (p.counts[Phonology.COMPLEX_ONSETS] + p.counts[Phonology.COMPOUND_INTERLUDES]);
				
				double heavyComplex = 0; 
				if (p.maxNucleusLength > 1)
				{
					heavyComplex = p.counts[Phonology.SIMPLE_ONSETS] + p.counts[Phonology.COMPLEX_ONSETS] + p.counts[Phonology.COMPOUND_INTERLUDES];
					heavyComplex *= prev.lastPhoneme().interludes[1].size();
				}
				
				lightRimeProminence = Math.log(simpleNucleusSimpleInterlude + simpleNucleusEmptyInterlude + 1);
				heavyRimeProminence = Math.log(heavySimple + heavyComplex + 1);
			}

			// If hiatus does not occur, select a nucleus from the master inventory instead.
						
			double rand = rng.nextDouble() * (lightRimeProminence + heavyRimeProminence / length);
			if (rand < lightRimeProminence)
				return medialLightRimeNode;
			else
				return medialHeavyRimeNode;
		}
	}
	
	/**
	 * Adds a simple nucleus to a light rime in a medial syllable. Decides then whether to add a simple interlude or an empty one.
	 * Selects and adds the appropriate onset, if it is indeed appropriate, then transitions to the SyllableLocationNode.
	 * @since	1.0
	 */
	private class MedialLightRimeNode implements Node
	{
		double simpleInterludeProminence;
		
		public MedialLightRimeNode()
		{
			simpleInterludeProminence = p.baseMedialOnsetChance * Math.log(p.counts[Phonology.SIMPLE_ONSETS] + 1);
		}
		
		public Node nextNode()
		{
			Constituent next;

			// If this is hiatus, add an nucleus from the previous vowel's interlude list.
			// Otherwise, add any available simple nucleus
			if (prev != null && prev.type == SegmentType.NUCLEUS)
				next = prev.lastPhoneme().pickInterlude(0);
			else
				next = p.pickSimpleNucleus();
			
			// Add nucleus
			addConstituent(next);
			
			// Decide whether to add next an empty interlude or a simple interlude.
			// hiatus prominence = (complement of onset chance) scaled by log of the available hiatus count for this phoneme
			double emptyInterludeProminence = 0;
			if (next.lastPhoneme().interludes[0].size() > 0)
				emptyInterludeProminence = Math.log(1 + 1) * (1 - p.baseMedialOnsetChance); 
			
			//S Select and add a simple onset, if fortune sees fit
			if (rng.nextDouble() * (simpleInterludeProminence + emptyInterludeProminence) < simpleInterludeProminence)
				addConstituent(p.pickSimpleOnset());
			
			// Rime complete; transition back to the syllable location node either way
			return syllableLocationNode;
		}
	}
	
	/** 
	 * Decides whether a simple or complex nucleus should feature in a heavy rime in a medial syllable.
	 * Selects and adds either a simple or complex nucleus. If a simple nucleus was selected, this method transitions to
	 * ComplexInterludeNode; if a complex nucleus was selected, it transitions to MedialComplexNucleusNode instead to decide
	 * what type of interlude to add.
	 * @since	1.0
	 */
	private class MedialHeavyRimeNode implements Node
	{		
		public Node nextNode()
		{
			Constituent next;
			double simple = 0, complex = 0;
			
			// Hiatus: If the previous syllable ended with a vowel, this nucleus must come from that vowel's interlude table
			if (prev != null && prev.type == SegmentType.NUCLEUS)
			{
				// Count all heavy rhymes with simple nuclei
				simple = p.counts[Phonology.COMPLEX_ONSETS] + p.counts[Phonology.COMPOUND_INTERLUDES];
				simple *= prev.lastPhoneme().interludes[0].size();	
				
				// Log scale the count and multiply it by the inverse of the base diphthong chance
				simple = Math.log(simple + 1);	
				simple *= 1 - p.baseDiphthongChance;
				
				if (p.maxNucleusLength > 1)
				{
					// Add the remaining types of interlude and multiply by the number of diphthongs in the preceding vowel's hiatus list
					complex = p.counts[Phonology.SIMPLE_ONSETS] + p.counts[Phonology.COMPLEX_ONSETS] + p.counts[Phonology.COMPOUND_INTERLUDES];
					complex *= prev.lastPhoneme().interludes[1].size();
					
					// Log scale the count and multiply it by the base diphthong chance
					complex = Math.log(complex + 1);
					complex *= p.baseDiphthongChance;
				}
				
				// Select and add the next nucleus
				if (rng.nextDouble() * (simple + complex) < simple)
					next = prev.lastPhoneme().pickInterlude(0);
				else
					next = prev.lastPhoneme().pickInterlude(1);
			}
			
			// No Hiatus: If the previous syllable ended with a consonant, we may choose a nucleus freely
			else
			{
				// Count the number of heavy rhymes with simple nuclei
				simple = p.counts[Phonology.COMPLEX_ONSETS] + p.counts[Phonology.COMPOUND_INTERLUDES]; 
				simple *= p.counts[Phonology.SIMPLE_NUCLEI];
				
				// Log scale the count and multiply it by the inverse of the base diphthong chance
				simple = Math.log(simple + 1);
				simple *= 1 - p.baseDiphthongChance;
				
				if (p.maxNucleusLength > 1)
				{
					// Count the number of heavy rhymes with complex nuclei
					complex = p.counts[Phonology.SIMPLE_ONSETS] + p.counts[Phonology.COMPLEX_ONSETS] + p.counts[Phonology.COMPOUND_INTERLUDES];
					complex *= p.counts[Phonology.COMPLEX_NUCLEI];
					complex += p.counts[Phonology.COMPLEX_NUCLEI_WITH_HIATUS];
					
					// Multiply the log of the count by the base diphthong chance
					complex = Math.log(complex + 1);
					complex *= p.baseDiphthongChance;
				}

				// Select and add the next nucleus
				if (rng.nextDouble() * (simple + complex) < simple)
					next = p.pickSimpleNucleus();
				else
					next = p.pickComplexNucleus();
			}
			
			// Add the chosen nucleus to name
			addConstituent(next);
			
			// Advance to the next node
			if (next.content.length == 1)
				return complexInterludeNode;
			else
				return medialComplexNucleusNode;
		}
	}
	
	/**
	 * Determines what type of interlude (simple, complex, or none) should follow a complex nucleus in a heavy rime in a medial syllable.
	 * (A complex interlude can be a compound interlude (simple/complex coda + simple/complex onset), or merely a complex onset.)
	 * Selects and adds the appropriate interlude, if it is indeed appropriate. Transitions to ComplexInterludeNode if a complex interlude
	 * was selected; transitions to to SyllableLocationNode otherwise.  
	 * @since 1.0
	 */
	private class MedialComplexNucleusNode implements Node
	{
		double simpleInterludeProminence;	// prominence of a simple interlude given a heavy rime with a complex nucleus
		double complexInterludeProminence;	// prominence of a complex interlude given a heavy rime with a complex nucleus
		
		public MedialComplexNucleusNode()
		{
			simpleInterludeProminence = Math.log(p.counts[Phonology.SIMPLE_ONSETS] + 1);
			simpleInterludeProminence *= p.baseMedialOnsetChance; // multiply by P(onset)
			simpleInterludeProminence *= (1 - p.baseOnsetClusterChance); // multiply by P(!onset cluster | onset)
			simpleInterludeProminence *= (1 - p.baseMedialCodaChance); // multiply by P(!coda | onset)
			
			complexInterludeProminence = Math.log(p.counts[Phonology.COMPLEX_ONSETS] + p.counts[Phonology.COMPOUND_INTERLUDES] + 1);

			// multiply by P(onset), as all complex interludes include an onset of length 1+
			complexInterludeProminence *= p.baseMedialOnsetChance; 
			
			// multiply by P((onset cluster AND !coda | onset) OR (coda | onset)) 
			complexInterludeProminence *= (p.baseOnsetClusterChance * (1 - p.baseMedialCodaChance)) + p.baseMedialCodaChance;
		}
		
		public Node nextNode()
		{
			// Calculate emptyInterludeProminence, which depends on the number of interludes available to the last vowel
			double emptyInterludeProminence = 0;
			if (prev.lastPhoneme().interludes[0].size() > 0)
				emptyInterludeProminence = Math.log(1 + 1) * (1 - p.baseMedialOnsetChance);
			
			double rand = rng.nextDouble() * (emptyInterludeProminence + simpleInterludeProminence + complexInterludeProminence);
			
			// Empty interlude
			if (rand < emptyInterludeProminence)
				return syllableLocationNode;
			
			// Simple interlude
			else if (rand < emptyInterludeProminence + simpleInterludeProminence)
			{
				addConstituent(p.pickSimpleOnset());
				return syllableLocationNode;
			}
			
			// Complex interlude
			else
				return complexInterludeNode;
		}
	}
	
	/**
	 * Decides what type of complex interlude to add: a complex onset, or a compound interlude.
	 * Selects and adds an appropriate interlude and transitions to the SyllableLocationNode.
	 * @since 1.0
	 */
	private class ComplexInterludeNode implements Node
	{
		double complexOnsetProminence;
		double compoundInterludeProminence;
		
		public ComplexInterludeNode()
		{
			complexOnsetProminence = Math.log(p.counts[Phonology.COMPLEX_ONSETS] + 1);
			complexOnsetProminence *= p.baseOnsetClusterChance;	// multiply by P(onset cluster | onset)
			complexOnsetProminence *= (1 - p.baseMedialCodaChance); // multiply by P(!coda | onset)
			
			compoundInterludeProminence = Math.log(p.counts[Phonology.COMPOUND_INTERLUDES] + 1);
			compoundInterludeProminence *= p.baseMedialCodaChance;	// multiply by P(coda | onset)
		}
		
		public Node nextNode()
		{
			if (rng.nextDouble() * (complexOnsetProminence + compoundInterludeProminence) < complexOnsetProminence)
				addConstituent(p.pickComplexOnset());
			else
			{
				// Add any coda and then an interlude from that coda's final phoneme's interlude list
				Constituent next = p.pickCoda();
				addConstituent(next);
				addConstituent(p.pickInterlude(next.lastPhoneme()));
			}
			
			return syllableLocationNode;
		}
	}
	
	/**
	 * Decides whether to add a light or heavy rime, then transitions to TerminalLightRimeNode or TerminalHeavyRimeNode accordingly.
	 * Does not add a SyllableSegment to the name.
	 * @since	1.0
	 */
	private class TerminalSyllableWeightNode implements Node
	{
		public Node nextNode()
		{
			double lightRimeProminence;
			double heavyRimeProminence;
			
			// If the previous syllable segment was a nucleus, hiatus occurs, and the following nucleus must be added
			// from the previous one's interlude inventory. 
			if (prev != null && prev.type == SegmentType.NUCLEUS)
			{
				// light rimes
				double simpleNucleusSimpleCoda = 0;
				if (p.baseTerminalCodaChance > 0)
					simpleNucleusSimpleCoda = prev.lastPhoneme().interludes[0].size() * p.counts[Phonology.SIMPLE_CODAS];
				
				double simpleNucleusEmptyCoda = 0;
				if (p.baseTerminalCodaChance < 1)
					simpleNucleusEmptyCoda = prev.lastPhoneme().interludes[0].size();

				// heavy rimes
				double heavySimple = 0;
				if (p.baseTerminalCodaChance > 0)
					heavySimple = prev.lastPhoneme().interludes[0].size() * p.counts[Phonology.COMPLEX_CODAS];
				
				double heavyComplex = 0;
				if (p.maxNucleusLength > 1)
				{
					if (p.baseTerminalCodaChance > 0)
						heavyComplex += p.counts[Phonology.SIMPLE_CODAS] + p.counts[Phonology.COMPLEX_CODAS];
					if (p.baseTerminalCodaChance < 1)
						heavyComplex += 1;
					
					heavyComplex *= prev.lastPhoneme().interludes[1].size();
					heavyComplex = Math.log(heavyComplex + 1) * p.baseDiphthongChance;
				}
				
				lightRimeProminence = Math.log(simpleNucleusEmptyCoda + simpleNucleusSimpleCoda + 1);
				heavyRimeProminence = Math.log(heavySimple + heavyComplex + 1);
			}
			
			// If hiatus does not occur, select a nucleus from the master inventory instead.
			else
			{
				// light rimes
				double simpleNucleusSimpleCoda = 0;
				if (p.baseTerminalCodaChance > 0)
					simpleNucleusSimpleCoda = p.counts[Phonology.SIMPLE_NUCLEI] * p.counts[Phonology.SIMPLE_CODAS];
				
				double simpleNucleusEmptyCoda = 0;
				if (p.baseTerminalCodaChance < 1)
					simpleNucleusEmptyCoda = p.counts[Phonology.SIMPLE_NUCLEI];

				// heavy rimes
				double heavySimple = 0;
				if (p.baseTerminalCodaChance > 0)
					heavySimple = p.counts[Phonology.SIMPLE_NUCLEI] * p.counts[Phonology.COMPLEX_CODAS];
				
				double heavyComplex = 0;
				if (p.maxNucleusLength > 1)
				{
					heavyComplex = 1;
					if (p.baseTerminalCodaChance > 0)
						heavyComplex += p.counts[Phonology.SIMPLE_CODAS] + p.counts[Phonology.COMPLEX_CODAS];
					
					heavyComplex *= p.counts[Phonology.COMPLEX_NUCLEI];
					heavyComplex = Math.log(heavyComplex + 1) * p.baseDiphthongChance;
				}
				
				lightRimeProminence = Math.log(simpleNucleusEmptyCoda + simpleNucleusSimpleCoda + 1);
				heavyRimeProminence = Math.log(heavySimple + heavyComplex + 1);
			}
			
			double rand = rng.nextDouble();
			if (length == 1 && name.syllables.size() == 0 && heavyRimeProminence > 0)
			{
				return terminalHeavyRimeNode;
			}
			else
			{
				
				rand *= lightRimeProminence + heavyRimeProminence;
				if (rand < lightRimeProminence)
					return terminalLightRimeNode;
				else
					return terminalHeavyRimeNode;
			}
		}
	}
	
	/**
	 * Adds a simple nucleus to a light rime in the terminal syllable. Decides whether to add a simple coda as well.
	 * Selects and adds the appropriate coda, if it is indeed appropriate, then exits the flowchart.
	 * @since	1.1
	 *
	 */
	private class TerminalLightRimeNode implements Node
	{
		double emptyCodaProminence;
		double simpleCodaProminence;
		
		public TerminalLightRimeNode()
		{
			emptyCodaProminence = Math.log(1 + 1) * (1 - p.baseTerminalCodaChance);
			simpleCodaProminence = Math.log(p.counts[Phonology.SIMPLE_CODAS] + 1) * p.baseTerminalCodaChance;
		}
		
		public Node nextNode()
		{
			Constituent next;
		
			// If this is hiatus, add an nucleus from the previous vowel's interlude list.
			// Otherwise, add any available simple nucleus
			if (prev != null && prev.type == SegmentType.NUCLEUS)
				next = prev.lastPhoneme().pickInterlude(0);
			else
				next = p.pickSimpleNucleus();
			
			// Add nucleus
			addConstituent(next);
						
			// Decide whether to add next a simple coda or none at all
			if (rng.nextDouble() * (simpleCodaProminence + emptyCodaProminence) < simpleCodaProminence)
				addConstituent(p.pickSimpleCoda());
			
			// Rime complete; return null to exit loop
			return null;
		}
	}
	
	/** 
	 * Decides whether a simple or complex nucleus should feature in a heavy rime in the terminal syllable.
	 * Selects and adds either a simple or complex nucleus and either transitions to the TerminalSyllableHeavyRimeComplexNucleus
	 * node, in the case of a complex nucleus, or in the case of a simple one, adds a complex coda and exits the flowchart.
	 * or exits 
	 * @since	1.0
	 */
	private class TerminalHeavyRimeNode implements Node
	{
		public Node nextNode()
		{
			Constituent next;
			
			// Hiatus case: If the previous phoneme was a vowel, this nucleus must come from that vowel's interlude table
			if (prev != null && prev.type == SegmentType.NUCLEUS)
			{
				// Number of heavy rimes with simple nuclei is proportionate to the number of simple nuclei times complex codas,
				// as long as terminal codas are allowed.
				double simple = 0;
				if (p.baseTerminalCodaChance > 0)
				{
					// Count all heavy rhymes with simple nuclei
					simple = p.counts[Phonology.COMPLEX_CODAS];
					simple *= prev.lastPhoneme().interludes[0].size();

					// Log scale the count and multiply it by the inverse of the base diphthong chance
					simple = Math.log(simple + 1);
					simple *= 1 - p.baseDiphthongChance;
				}
				
				// Number of heavy rimes with complex nuclei is proportionate to the number of complex nuclei times the number
				// of codas availables, including the null coda.
				double complex = 0;
				if (p.maxNucleusLength > 1)
				{
					// Rimes with codas
					if (p.baseTerminalCodaChance > 0)
						complex += p.counts[Phonology.SIMPLE_CODAS] + p.counts[Phonology.COMPLEX_CODAS];
					
					// Rimes without
					if (p.baseTerminalCodaChance < 1)
						complex += 1;
					
					complex *= prev.lastPhoneme().interludes[1].size();
					complex = Math.log(complex + 1) * p.baseDiphthongChance;
				}
				
				// Select and add the next nucleus
				if (rng.nextDouble() * (simple + complex) < simple)
					next = prev.lastPhoneme().pickInterlude(0);
				else
					next = prev.lastPhoneme().pickInterlude(1);
			}
			
			// Otherwise, we may choose a nucleus freely
			else
			{
				// Number of heavy rimes with simple nuclei is proportionate to the number of simple nuclei times codas,
				// as long as terminal codas are allowed.
				double simple = 0;
				if (p.baseTerminalCodaChance > 0)
				{
					simple = p.counts[Phonology.SIMPLE_NUCLEI] * p.counts[Phonology.COMPLEX_CODAS];
					simple = Math.log(simple + 1) * (1 - p.baseDiphthongChance);
				}
				
				// Number of heavy rimes with complex nuclei is proportionate to the number of complex nuclei times the number
				// of codas availables, including the null coda.
				double complex = 0;
				if (p.maxNucleusLength > 1)
				{
					complex = 1;
					if (p.baseTerminalCodaChance > 0)
						complex += p.counts[Phonology.SIMPLE_CODAS] + p.counts[Phonology.COMPLEX_CODAS];
					
					complex *= p.counts[Phonology.COMPLEX_NUCLEI];
					complex = Math.log(complex + 1) * p.baseDiphthongChance;
				}
				
				// Select and add the next nucleus
				if (rng.nextDouble() * (simple + complex) < simple)
					next = p.pickSimpleNucleus();
				else
					next = p.pickComplexNucleus();
			}
			
			// Add the chosen nucleus to name
			addConstituent(next);

			// Select next node
			if (next.content.length == 1)
			{
				addConstituent(p.pickComplexCoda());
				return null;
			}
			else
				return terminalHeavyRimeComplexNucleusNode;
		}
	}
	
	/**
	 * Decides what type of coda, if any, should follow a complex nucleus in the terminal syllable.
	 * Adds the appropriate coda and exits the flowchart.
	 * @since	1.0
	 */
	public class TerminalHeavyRimeComplexNucleusNode implements Node
	{
		double emptyCodaProminence;
		double simpleCodaProminence;
		double complexCodaProminence;
		
		public TerminalHeavyRimeComplexNucleusNode()
		{
			emptyCodaProminence = Math.log(1 + 1) * (1 - p.baseTerminalCodaChance);
			simpleCodaProminence = Math.log(p.counts[Phonology.SIMPLE_CODAS] + 1) * p.baseTerminalCodaChance * (1 - p.baseCodaClusterChance);
			complexCodaProminence = Math.log(p.counts[Phonology.COMPLEX_CODAS] + 1) * p.baseTerminalCodaChance * p.baseCodaClusterChance;
		}
		
		public Node nextNode()
		{
			double rand = rng.nextDouble() * (emptyCodaProminence + simpleCodaProminence + complexCodaProminence);
			
			if (rand < simpleCodaProminence)
				addConstituent(p.pickSimpleCoda());
			else if (rand < simpleCodaProminence + complexCodaProminence)
				addConstituent(p.pickComplexCoda());

			// Rime complete; return null to exit loop
			return null;
		}
	}
	
	private void addConstituent(Constituent c)
	{
		prev = c;
		name.add(c);
	}
}


