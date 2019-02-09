/** Copyright 2018, 2019 Clayton Cooper
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
import gengenv2.Phonology.Phoneme.Follower;

/**
 * A sort of flowchart or state machine for generating names according to a Phonology's inventory, phonotactics, 
 * and stress system. Though the flowchart is structurally the same regardless of the anguage, its weights depend
 * on its Phonology's features, so every Phonology needs its own custom copy.
 * 
 * @author	Clayton Cooper
 * @version	1.1
 * @since	1.0
 */
class NameAssembly
{
	Random rng;
	Phonology p;
	
	// Flowchart nodes
	private InitialOnsetNode ioNode;
	private SyllableLocationNode slNode;
	private MedialSyllableWeightNode mswNode;
	private MedialLightRimeNode mlrNode;
	private MedialHeavyRimeNode mhrNode;
	private MedialComplexNucleusNode mcnNode;
	private LightInterludeNode liNode;
	private HeavyInterludeNode hiNode;
	private TerminalSyllableWeightNode tswNode;
	private TerminalLightRimeNode tlrNode;
	private TerminalHeavyRimeNode thrNode;
	private TerminalHeavyRimeComplexNucleusNode thrcnNode;
	private LightCodaNode lcNode;
	
	// Current name variables
	Name name;				// The name currently being generated
	double icTarget;		// Intended information content of the current name
	double pName;			// Probability of generating the current name
	Constituent prev;		// The most recent syllable constituent added to the name
	
	// Information content variables
	double infoConMean = 12;	// Average value of target information content
	double infoConStdev = 2;	// Standard deviation of target information content
	EntropyStats entropyStats;	// Collection of entropy values for different flowchart nodes
	
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
	
		ioNode 		= new InitialOnsetNode();
		slNode 		= new SyllableLocationNode();
		mswNode		= new MedialSyllableWeightNode();
		mlrNode 	= new MedialLightRimeNode();
		mhrNode 	= new MedialHeavyRimeNode();
		mcnNode 	= new MedialComplexNucleusNode();
		liNode 		= new LightInterludeNode();
		hiNode 		= new HeavyInterludeNode();
		tswNode 	= new TerminalSyllableWeightNode();
		tlrNode 	= new TerminalLightRimeNode();
		thrNode 	= new TerminalHeavyRimeNode();
		thrcnNode 	= new TerminalHeavyRimeComplexNucleusNode();
		lcNode 		= new LightCodaNode();
		entropyStats = new EntropyStats();
	}
	
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
		icTarget = rng.nextGaussian() * infoConStdev + infoConMean;
		name = new Name();
		prev = null;
		pName = 1;
		
		// Propagate through the flowchart until one of the nodes returns null
		Node node = ioNode;

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

		name.informationContent = -Math.log(pName);
		p.stressRules.addStresses(name);
		
		return name;
	}
	
	/**
	 * A simple interface implemented by all Nodes in the name assembly flowchart, to ensure that they can all
	 * be called upon to hand over the next Node in the sequence.
	 * @since	1.0
	 */
	private interface Node
	{
		public Node nextNode();
	}

	/**
	 * The first node called by makeName(). Its job is to reset the naming variables and pick an initial onset before
	 * advancing to the SyllableLocationNode.
	 * @since 1.0
	 */
	private class InitialOnsetNode implements Node
	{
		public double emptyOnsetChance, simpleOnsetChance, complexOnsetChance;
		
		public InitialOnsetNode()
		{
			// Empty onset chance
			emptyOnsetChance = Math.log(Math.pow(p.counts[Phonology.SIMPLE_NUCLEI] + 
					p.counts[Phonology.COMPLEX_NUCLEI] + 1, 2));
			emptyOnsetChance *= p.baseEmptyInitialOnsetChance;
			
			// Scaling each prominence value by the log of the number of possibilities it represents
			// helps ensure that rich inventories are well represented while poor ones are sampled
			// less frequently. This reduces the chances of something like a language with only 1
			// onset cluster having that cluster at the start of 20% of names.
			// This technique is repeated abundantly throughout the Node classes.

			// Onset onset chance
			simpleOnsetChance = Math.log(p.counts[Phonology.SIMPLE_ONSETS] + 1);
			simpleOnsetChance *= (1 - p.baseEmptyInitialOnsetChance) * (1 - p.baseOnsetClusterChance);
			
			// Complex onset chance
			complexOnsetChance = Math.log(p.counts[Phonology.COMPLEX_ONSETS] + 1);
			complexOnsetChance *= (1 - p.baseEmptyInitialOnsetChance) * p.baseOnsetClusterChance;
			
			// Normalize chances
			double sum = emptyOnsetChance + simpleOnsetChance + complexOnsetChance;
			emptyOnsetChance /= sum;
			simpleOnsetChance /= sum;
			complexOnsetChance /= sum;
		}
		
		public Node nextNode()
		{	
			double rand = p.rng.nextDouble();
			
			// Option 1: Empty onset
			if (rand < emptyOnsetChance)
			{
				pName *= emptyOnsetChance;
				return slNode;
			}

			// Option 2: Simple onset
			else if (rand < emptyOnsetChance + simpleOnsetChance)
			{
				Constituent c = p.pickSimpleOnset();
				pName *= simpleOnsetChance;
				pName *= c.probability;
				addConstituent(c);
				return slNode;
			}
			
			// Option 3: Complex onset
			else
			{
				Constituent c = p.pickComplexOnset();
				pName *= complexOnsetChance;
				pName *= c.probability;
				pName *= p.onsetClusterLengthProbabilities[c.content.length - 2];
				addConstituent(c);
				return slNode;
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
			// If an average medial rhyme plus an average terminal one would bring the infocon closer to the 
			// target value than an average terminal rhyme alone, add a medial rhyme
			double a = Math.abs(entropyStats.terminalRimeH + -Math.log(pName) - icTarget);
			double b = Math.abs(entropyStats.terminalRimeH + entropyStats.medialRimeH + -Math.log(pName) - icTarget);
			
			if (a > b)
			{
				return mswNode;
			}
			
			// Otherwise, add a medial rhyme
			else
				return tswNode;
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
		public double lightRimeChance;
		public double heavyRimeChance;
		
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
			
			lightRimeChance = Math.log(simpleNucleusSimpleInterlude + simpleNucleusEmptyInterlude + 1);
			heavyRimeChance = Math.log(heavySimple + heavyComplex + 1);
			
			double sum = lightRimeChance + heavyRimeChance;
			lightRimeChance /= sum;
			heavyRimeChance /= sum;
		}
		
		public Node nextNode()
		{
			double light = lightRimeChance, heavy = heavyRimeChance;
			
			// Hiatus: If the previous constituent was a nucleus, the probabilities for light and heavy rimes must be
			// recalculated based on the number of nuclei in the previous nucleus' hiatus list (instead of deferring
			// to the master nucleus list)
			if (prev != null && prev.type == SegmentType.NUCLEUS)
			{
				// chance of light rimes
				double simpleNucleusSimpleInterlude = prev.lastPhoneme().interludes[0].size() * 
						p.counts[Phonology.SIMPLE_ONSETS];

				double simpleNucleusEmptyInterlude = 0;
				for (int i = 0; i < prev.lastPhoneme().interludes[0].size(); i++)
					if (prev.lastPhoneme().interludes[0].get(i).c.lastPhoneme().interludes[0].size() > 0)
						simpleNucleusEmptyInterlude++;
				
				// chance of heavy rimes
				double heavySimple = prev.lastPhoneme().interludes[0].size() * (p.counts[Phonology.COMPLEX_ONSETS] + 
						p.counts[Phonology.COMPOUND_INTERLUDES]);
				
				double heavyComplex = 0; 
				if (p.maxNucleusLength > 1)
				{
					heavyComplex = p.counts[Phonology.SIMPLE_ONSETS] + p.counts[Phonology.COMPLEX_ONSETS] + 
							p.counts[Phonology.COMPOUND_INTERLUDES];
					heavyComplex *= prev.lastPhoneme().interludes[1].size();
				}
				
				light = Math.log(simpleNucleusSimpleInterlude + simpleNucleusEmptyInterlude + 1);
				heavy = Math.log(heavySimple + heavyComplex + 1);
			}
			
//			heavy /= 2;
			
			// Normalize
			double sum = light + heavy;
			light = light / sum;
			heavy = heavy / sum;
			
			double rand = rng.nextDouble();
			if (rand < light)
			{
				pName *= light;
				return mlrNode;
			}
			else
			{
				pName *= heavy;
				return mhrNode;
			}
		}
	}
	
	/**
	 * Adds a simple nucleus to a light rime in a medial syllable. Decides then whether to add a simple interlude or an empty one.
	 * Selects and adds the appropriate onset, if it is indeed appropriate, then transitions to the SyllableLocationNode.
	 * @since	1.0
	 */
	private class MedialLightRimeNode implements Node
	{
		public Node nextNode()
		{
			Constituent next;

			// If this is hiatus, add an nucleus from the previous vowel's interlude list.
			// Otherwise, add any available simple nucleus
			if (prev != null && prev.type == SegmentType.NUCLEUS)
			{
				Follower f = prev.lastPhoneme().pickInterlude(0); 
				next = f.c;
				pName *= f.probability;
			}
			else
			{
				next = p.pickSimpleNucleus();
				pName *= next.probability;
			}
			
			// Add nucleus
			addConstituent(next);
			
			// Transition to light interlude node
			return liNode;
		}
	}
	
	/** 
	 * Decides whether a simple or complex nucleus should feature in a heavy rime in a medial syllable.
	 * Selects and adds either a simple or complex nucleus. If a simple nucleus was selected, this method transitions to
	 * HeavyInterludeNode; if a complex nucleus was selected, it transitions to MedialComplexNucleusNode instead to decide
	 * what type of interlude to add.
	 * @since	1.0
	 */
	private class MedialHeavyRimeNode implements Node
	{		
		double basicSimpleNucleusChance;
		double basicComplexNucleusChance;
		
		public MedialHeavyRimeNode()
		{
			// Count the number of heavy rhymes with simple nuclei
			basicSimpleNucleusChance = p.counts[Phonology.COMPLEX_ONSETS] + p.counts[Phonology.COMPOUND_INTERLUDES]; 
			basicSimpleNucleusChance *= p.counts[Phonology.SIMPLE_NUCLEI];
			
			// Log scale the count and multiply it by the inverse of the base diphthong chance
			basicSimpleNucleusChance = Math.log(basicSimpleNucleusChance + 1);
			basicSimpleNucleusChance *= 1 - p.baseDiphthongChance;
			
			basicComplexNucleusChance = 0;
			if (p.maxNucleusLength > 1)
			{
				// Count the number of heavy rhymes with complex nuclei
				basicComplexNucleusChance += p.counts[Phonology.SIMPLE_ONSETS] + p.counts[Phonology.COMPLEX_ONSETS] + p.counts[Phonology.COMPOUND_INTERLUDES];
				basicComplexNucleusChance *= p.counts[Phonology.COMPLEX_NUCLEI];
				basicComplexNucleusChance += p.counts[Phonology.COMPLEX_NUCLEI_WITH_HIATUS];
				
				// Multiply the log of the count by the base diphthong chance
				basicComplexNucleusChance = Math.log(basicComplexNucleusChance + 1);
				basicComplexNucleusChance *= p.baseDiphthongChance;
			}
			
			// Normalize
			double sum = basicSimpleNucleusChance + basicComplexNucleusChance;
			if (sum == 0)
				basicSimpleNucleusChance = basicComplexNucleusChance = 0;
			else
			{
				basicSimpleNucleusChance /= sum;
				basicComplexNucleusChance /= sum;
			}
			
		}
		
		public Node nextNode()
		{
			Constituent next;
			double simple = basicSimpleNucleusChance, complex = basicComplexNucleusChance;
			
			// Hiatus: If the previous syllable ended with a vowel, this nucleus must come from that vowel's 
			// interlude table, and the chance of choosing between simple and complex nuclei must be
			// accordingly recalculated
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
				double sum = simple + complex;
				
				if (rng.nextDouble() * sum < simple)
				{
					Follower f = prev.lastPhoneme().pickInterlude(0);
					next = f.c;
					pName *= simple / sum;
					pName *= f.probability;
				}
				else
				{
					Follower f = prev.lastPhoneme().pickInterlude(1);
					next = f.c;
					pName *= complex / sum;
					pName *= f.probability;
				}
			}
			
			// No Hiatus: If the previous syllable ended with a consonant, we may choose a nucleus freely,
			// and use the saved calculation
			else
			{
				double sum = simple + complex;
				
				// Select and add the next nucleus
				if (rng.nextDouble() * sum < simple)
				{
					next = p.pickSimpleNucleus();
					pName *= next.probability * simple / sum;
					pName *= next.probability;
				}
				else
				{
					next = p.pickComplexNucleus();
					pName *= next.probability * complex / sum;
					pName *= next.probability;
				}
			}
			
			// Add the chosen nucleus to name
			addConstituent(next);
			
			// Advance to the next node
			if (next.content.length == 1)
				return hiNode;
			else
				return mcnNode;
		}
	}
	
	/**
	 * Determines what type of interlude (light or heavy) should follow a complex nucleus in a heavy rime in a medial
	 * syllable. (A heavy interlude can be a compound interlude (simple/complex coda + simple/complex onset), or merely
	 * a complex onset.) Transitions to LightInterludeNode or HeavyInterludeNode without adding a new constituent.  
	 * @since 1.0
	 */
	private class MedialComplexNucleusNode implements Node
	{
		double lightInterludeMultiplier;	// multiplies the log of the light interlude count to produce the
											// prominence of a light interlude given a complex nucleus.
											// prominence cannot be stored itself as it depends on the previous nucleus
		double heavyInterludeProminence;	// prominence of a heavy interlude given a complex nucleus
		double basicLightInterludeChance;	// chance of a light interlude given a complex hiatus-capable nucleus
		double basicHeavyInterludeChance;	// "	"	"  heavy interlude ...
		double complexNucleusWithHiatusChance;
		
		public MedialComplexNucleusNode()
		{
			// Chance the preceding nucleus is eligible for hiatus, assuming the current syllable
			// did not begin with hiatus itself
			complexNucleusWithHiatusChance = 0;
			if (p.maxNucleusLength == 2)
				for (Constituent n : p.nuclei[1])
					if (n.content[0].interludes[0].size() > 0 || n.content[0].interludes[1].size() > 0)
						complexNucleusWithHiatusChance += n.probability; 
			
			double lightInterludeProminence = Math.log(1 + p.counts[Phonology.SIMPLE_ONSETS] +
													  complexNucleusWithHiatusChance + 1);
			
			// Multiply by P((onset AND no cluster AND no coda) OR (no onset))
			lightInterludeMultiplier = p.baseMedialOnsetChance * (1 - p.baseOnsetClusterChance) * 
										(1 - p.baseMedialCodaChance) + 
										(1 - p.baseMedialOnsetChance);
			
			lightInterludeProminence = lightInterludeProminence * lightInterludeMultiplier;
			
			heavyInterludeProminence = Math.log(p.counts[Phonology.COMPLEX_ONSETS] + 
												p.counts[Phonology.COMPOUND_INTERLUDES] + 1);

			// multiply by P(onset), as all heavy interludes include an onset of length 1+
			heavyInterludeProminence *= p.baseMedialOnsetChance; 
			
			// multiply by P((onset cluster AND !coda | onset) OR (coda | onset)) 
			heavyInterludeProminence *= (p.baseOnsetClusterChance * (1 - p.baseMedialCodaChance)) + p.baseMedialCodaChance;
			
			basicLightInterludeChance = lightInterludeMultiplier * Math.log(p.counts[Phonology.SIMPLE_ONSETS] + 2);
			
			// Normalize
			double sum = basicLightInterludeChance + heavyInterludeProminence;
			basicLightInterludeChance /= sum;
			basicHeavyInterludeChance = heavyInterludeProminence / sum;
		}
		
		public Node nextNode()
		{
			double light = 0, heavy = heavyInterludeProminence;
			Node next = null;
			
			// Recalculate light interlude prominence according to the immediately preceding nucleus
			int lightInterludeCount = p.counts[Phonology.SIMPLE_ONSETS];
			if (prev.content[1].interludes[0].size() > 0)
				lightInterludeCount++;
			light = Math.log(lightInterludeCount + 1) * lightInterludeMultiplier;
			
			double rand = rng.nextDouble() * (light + heavy);
			
			// Light interlude
			if (rand < light)
			{
				pName *= light / (light + heavy);
				return liNode;
			}
			
			// heavy interlude
			else
			{
				pName *= heavy / (light + heavy);
				return hiNode;
			}
		}
	}
	
	/**
	 * Decides what type of light interlude to add: an empty interlude (hiatus), or a simple onset.
	 * Selects and adds an appropriate interlude and transitions to the SyllableLocationNode.
	 * @since 1.0
	 */
	private class LightInterludeNode implements Node
	{
		double simpleOnsetProminence;
		double basicSimpleOnsetChance;
		double basicHiatusChance; 		// General chance of hiatus occurring at this node (assuming the current
										// syllable did not begin with hiatus)
		double overallHiatusChance;		// General chance of hiatus occurring, period (again, assuming the current 
										// (syllable did not begin with hiatus)
		
		public LightInterludeNode()
		{
			simpleOnsetProminence = Math.log(p.counts[Phonology.SIMPLE_ONSETS] + 1);
			simpleOnsetProminence *= p.baseMedialOnsetChance;
			
			double pSimpleNucleusWithHiatus = 0;	// chance of a simple nucleus being eligible for hiatus
			for (Constituent n : p.nuclei[0])
				if (n.content[0].interludes[0].size() > 0)
					pSimpleNucleusWithHiatus += n.probability;
			
			// Chance of this node being reached with a nonzero possibility of hiatus occurring
			double hiatusChance = mswNode.lightRimeChance * pSimpleNucleusWithHiatus;
			hiatusChance += mswNode.heavyRimeChance * mhrNode.basicComplexNucleusChance * 
									 mcnNode.complexNucleusWithHiatusChance * mcnNode.basicLightInterludeChance;
			
			double hiatusProminence = Math.log(hiatusChance + 1);
			hiatusProminence *= (1 - p.baseMedialOnsetChance);
		
			// Normalize
			double sum = simpleOnsetProminence + hiatusProminence;
			basicSimpleOnsetChance = simpleOnsetProminence / sum;
			basicHiatusChance = hiatusProminence / sum;
			
			overallHiatusChance = hiatusChance * basicHiatusChance;
		}
		
		public Node nextNode()
		{
			double hiatusProminence = Math.log((prev.lastPhoneme().interludes[0].size() > 0 ? 1 : 0) + 1);
			hiatusProminence *= (1 - p.baseMedialOnsetChance);
			
			// Add either an onset or nothing, according to probability, before ending the rhyme
			if (rng.nextDouble() * (hiatusProminence + simpleOnsetProminence) < simpleOnsetProminence)
			{
				Constituent next = p.pickSimpleOnset();
				pName *= simpleOnsetProminence / (simpleOnsetProminence + hiatusProminence);
				pName *= next.probability;
				addConstituent(next);	
			}
			else
				pName *= hiatusProminence / (simpleOnsetProminence + hiatusProminence);
			
			return slNode;
		}
	}
	
	/**
	 * Decides what type of heavy interlude to add: a complex onset, or a compound interlude.
	 * Selects and adds an appropriate interlude and transitions to the SyllableLocationNode.
	 * @since 1.0
	 */
	private class HeavyInterludeNode implements Node
	{
		double complexOnsetChance;
		double compoundInterludeChance;
		
		public HeavyInterludeNode()
		{
			complexOnsetChance = Math.log(p.counts[Phonology.COMPLEX_ONSETS] + 1);
			complexOnsetChance *= p.baseOnsetClusterChance;	// multiply by P(onset cluster | onset)
			complexOnsetChance *= (1 - p.baseMedialCodaChance); // multiply by P(!coda | onset)
			
			compoundInterludeChance = Math.log(p.counts[Phonology.COMPOUND_INTERLUDES] + 1);
			compoundInterludeChance *= p.baseMedialCodaChance;	// multiply by P(coda | onset)
			
			// Normalize
			double sum = complexOnsetChance + compoundInterludeChance;
			if (sum == 0)
				complexOnsetChance = compoundInterludeChance = 0;
			else
			{
				complexOnsetChance /= sum;
				compoundInterludeChance /= sum;
			}
		}
		
		public Node nextNode()
		{
			double sum = complexOnsetChance + compoundInterludeChance;
			if (rng.nextDouble() * sum < complexOnsetChance)
			{
				pName *= complexOnsetChance / sum;
				addConstituent(p.pickComplexOnset());
			}
			else
			{
				// Add compound interlude: Add any coda, then any onset from that coda's interlude list
				// Add coda
				Constituent next = p.pickCoda();
				Follower f = p.pickInterlude(next.lastPhoneme());
				
				pName *= compoundInterludeChance / sum;
				
				pName *= next.probability;
				if (next.content.length == 1)
					pName *= p.simpleCodaProbability;
				else
					pName *= (1 - p.simpleCodaProbability) * p.codaClusterLengthProbabilities[next.content.length - 2];
				
				pName *= f.probability;
				Constituent o = f.c;
				if (o.content.length == 1)
					pName *= p.simpleOnsetProbability;
				else
					pName *= (1 - p.simpleOnsetProbability) * p.onsetClusterLengthProbabilities[o.content.length - 2];
				
				addConstituent(next);
				addConstituent(f.c);
			}
			
			return slNode;
		}
	}
	
	/**
	 * Decides whether to add a light or heavy rime, then transitions to TerminalLightRimeNode or TerminalHeavyRimeNode accordingly.
	 * Does not add a SyllableSegment to the name.
	 * @since	1.0
	 */
	private class TerminalSyllableWeightNode implements Node
	{
		double basicLightRimeChance;
		double basicHeavyRimeChance;
		
		public TerminalSyllableWeightNode()
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
			
			basicLightRimeChance = Math.log(simpleNucleusEmptyCoda + simpleNucleusSimpleCoda + 1);
			basicHeavyRimeChance = Math.log(heavySimple + heavyComplex + 1);
			
			// Normalize
			double sum = basicLightRimeChance + basicHeavyRimeChance;
			basicLightRimeChance /= sum;
			basicHeavyRimeChance /= sum;
		}
		
		public Node nextNode()
		{
			double lightRimeProminence;
			double heavyRimeProminence;
			
			// If the previous constituent was a nucleus, hiatus occurs, and the following nucleus must be added
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
				lightRimeProminence = basicLightRimeChance;
				heavyRimeProminence = basicHeavyRimeChance;
			}
			
			// Add either a light or heavy rime
			double sum = lightRimeProminence + heavyRimeProminence;
			double rand = rng.nextDouble() * sum;
			if (rand < lightRimeProminence)
			{
				pName *= lightRimeProminence / sum;
				return tlrNode;
			}
			else
			{
				pName *= heavyRimeProminence / sum;
				return thrNode;
			}
		}
	}
	
	/**
	 * Adds a simple nucleus to a light rime in the terminal syllable. In order to add a coda and finish the rime,
	 * it then advances to the lightCodaNode.
	 * @since	1.1
	 *
	 */
	private class TerminalLightRimeNode implements Node
	{
		public Node nextNode()
		{
			Constituent next;
			
			// If this is hiatus, add an nucleus from the previous vowel's interlude list.
			// Otherwise, add any available simple nucleus
			if (prev != null && prev.type == SegmentType.NUCLEUS)
			{
				Follower f = prev.lastPhoneme().pickInterlude(0);
				next = f.c;
				pName *= f.probability;
			}
			else
			{
				next = p.pickSimpleNucleus();
				pName *= next.probability;
			}
			
			// Add nucleus
			addConstituent(next);
						
			// Advance to the light coda node
			return lcNode;
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
		double basicSimpleNucleusChance;
		double basicComplexNucleusChance;
		
		public TerminalHeavyRimeNode()
		{
			// Number of heavy rimes with simple nuclei is proportionate to the number of simple nuclei times codas,
			// as long as terminal codas are allowed.
			basicSimpleNucleusChance = 0;
			if (p.baseTerminalCodaChance > 0)
			{
				basicSimpleNucleusChance = p.counts[Phonology.SIMPLE_NUCLEI] * p.counts[Phonology.COMPLEX_CODAS];
				basicSimpleNucleusChance = Math.log(basicSimpleNucleusChance + 1) * (1 - p.baseDiphthongChance);
			}
			
			// Number of heavy rimes with complex nuclei is proportionate to the number of complex nuclei times the number
			// of codas availables, including the null coda.
			basicComplexNucleusChance = 0;
			if (p.maxNucleusLength > 1)
			{
				basicComplexNucleusChance = 1;
				if (p.baseTerminalCodaChance > 0)
					basicComplexNucleusChance += p.counts[Phonology.SIMPLE_CODAS] + p.counts[Phonology.COMPLEX_CODAS];
				
				basicComplexNucleusChance *= p.counts[Phonology.COMPLEX_NUCLEI];
				basicComplexNucleusChance = Math.log(basicComplexNucleusChance + 1) * p.baseDiphthongChance;
			}	
			
			// Normalize
			double sum = basicSimpleNucleusChance + basicComplexNucleusChance;
			if (sum == 0)
				basicSimpleNucleusChance = basicComplexNucleusChance = 0;
			else
			{
				basicSimpleNucleusChance /= sum;
				basicComplexNucleusChance /= sum;
			}
		}
		
	
		public Node nextNode()
		{
			Constituent next;
			
			// Hiatus case: If the previous phoneme was a vowel, this nucleus must come from that vowel's 
			// interlude table, and the probabilities for picking a simple or complex nucleus must be recalculated
			if (prev != null && prev.type == SegmentType.NUCLEUS)
			{
				// Number of heavy rimes with simple nuclei is proportionate to the number of simple nuclei times
				// complex codas, as long as terminal codas are allowed.
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
				
				// Number of heavy rimes with complex nuclei is proportionate to the number of complex nuclei times 
				// the number of codas availables, including the null coda.
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
				double sum = simple + complex;
				if (rng.nextDouble() * sum < simple)
				{
					Follower f = prev.lastPhoneme().pickInterlude(0);
					next = f.c;
					pName *= simple / sum;
					pName *= f.probability;
				}
				else
				{
					Follower f = prev.lastPhoneme().pickInterlude(1);
					next = f.c;
					pName *= complex / sum;
					pName *= f.probability;
				}
			}
			
			// Otherwise, we may choose a nucleus freely
			else
			{
				double simple = basicSimpleNucleusChance, complex = basicComplexNucleusChance;
				
				// Select and add the next nucleus
				double sum = simple + complex;
				if (rng.nextDouble() * sum < simple)
				{
					next = p.pickSimpleNucleus();
					pName *= simple / sum;
					pName *= next.probability;
				}
				else
				{
					next = p.pickComplexNucleus();
					pName *= complex / sum;
					pName *= next.probability;
				}
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
				return thrcnNode;
		}
	}
	
	/**
	 * Decides what type of coda, if any, should follow a complex nucleus in the terminal syllable.
	 * If light, transitions to TerminalLightRimeNode, otherwise adds a complex coda and exits the flowchart.
	 * @since	1.0
	 */
	public class TerminalHeavyRimeComplexNucleusNode implements Node
	{
		double lightCodaChance;
		double heavyCodaChance;
		
		public TerminalHeavyRimeComplexNucleusNode()
		{
			lightCodaChance = Math.log(p.counts[Phonology.SIMPLE_CODAS] + 1 + 1);
			lightCodaChance *= (1 - p.baseTerminalCodaChance) + (p.baseTerminalCodaChance * (1 - p.baseCodaClusterChance));
			
			heavyCodaChance = Math.log(p.counts[Phonology.COMPLEX_CODAS] + 1);
			heavyCodaChance *= p.baseTerminalCodaChance * p.baseCodaClusterChance;
			
			double sum = lightCodaChance + heavyCodaChance;
			lightCodaChance /= sum;
			heavyCodaChance /= sum;
		}
		
		public Node nextNode()
		{
			double rand = rng.nextDouble();
			
			if (rand < lightCodaChance)
			{
				pName *= lightCodaChance;
				return lcNode;
			}
			else
			{
				Constituent next = p.pickComplexCoda();
				pName *= heavyCodaChance;
				pName *= next.probability * p.codaClusterLengthProbabilities[next.content.length - 2];
				addConstituent(next);
				return null;
			}
		}
	}
	
	private class LightCodaNode implements Node
	{
		double emptyCodaChance;
		double simpleCodaChance;
		
		public LightCodaNode()
		{
			emptyCodaChance = Math.log(1 + 1) * (1 - p.baseTerminalCodaChance);
			simpleCodaChance = Math.log(p.counts[Phonology.SIMPLE_CODAS] + 1) * p.baseTerminalCodaChance;
			
			double sum = emptyCodaChance + simpleCodaChance;
			emptyCodaChance /= sum;
			simpleCodaChance /= sum;
		}
		
		public Node nextNode()
		{
			// Decide whether to add next a simple coda or none at all
			double sum = simpleCodaChance + emptyCodaChance;
			if (rng.nextDouble() * sum < simpleCodaChance)
			{
				Constituent next = p.pickSimpleCoda();
				pName *= simpleCodaChance;
				pName *= next.probability;
				addConstituent(next);
			}
			else
			{
				pName *= emptyCodaChance;
			}
			
			// Rime complete; return null to exit loop
			return null;
		}
	}
	
	private void addConstituent(Constituent c)
	{
		prev = c;
		name.add(c);
	}
	
	private class EntropyStats
	{
		public double medialRimeH;
		public double terminalRimeH;

		public EntropyStats()
		{
			// Fundamental counts
			double simpleOnsetH = 0;
			double complexOnsetH = 0;
			double simpleNucleusH = 0;
			double complexNucleusH = 0;
			double simpleCodaH = 0;
			double complexCodaH = 0;
			double compoundInterludeH = 0;
			
			for (int i = 0; i < p.onsets[0].size(); i++)										// Simple onset
				simpleOnsetH += scaledInfo(p.onsets[0].get(i).probability);
			
			for (int j = 0; j < p.onsetClusterLengthProbabilities.length; j++)					// Complex onset
				for (int i = 0; i < p.onsets[j + 1].size(); i++)
					complexOnsetH += scaledInfo(p.onsets[j + 1].get(i).probability * 
							p.onsetClusterLengthProbabilities[j]);
			
			for (int i = 0; i < p.nuclei[0].size(); i++)										// Simple nuclei
				simpleNucleusH += scaledInfo(p.nuclei[0].get(i).probability);

			if (p.maxNucleusLength == 2)															// Complex nuclei
				for (int i = 0; i < p.nuclei[1].size(); i++)
					complexNucleusH += scaledInfo(p.nuclei[1].get(i).probability);
			
			if (p.maxCodaLength > 0)
			{
				for (int i = 0; i < p.codas[0].size(); i++)											// Simple coda
					simpleCodaH += scaledInfo(p.codas[0].get(i).probability);
				
				for (int j = 0; j < p.codaClusterLengthProbabilities.length; j++)					// Complex coda
					for (int i = 0; i < p.codas[j + 1].size(); i++)
						complexCodaH += scaledInfo(p.codas[j + 1].get(i).probability * 
								p.codaClusterLengthProbabilities[j]);
				
				for (int i = 0; i < p.maxCodaLength; i++)
					for (Constituent coda : p.codas[i])
					{
						// Coda probability
						double codaProb = coda.probability;
						if (i == 0)
							codaProb *= p.simpleCodaProbability;
						else
							codaProb *= p.codaClusterLengthProbabilities[i - 1];
						
						for (int j = 0; j < coda.lastPhoneme().interludes.length; j++)
							for (Follower onset : coda.lastPhoneme().interludes[j])
							{
								// Multiplied by onset probability
								double interludeProb = onset.c.probability;
								if (j == 0)
									interludeProb *= p.simpleOnsetProbability;
								else
								{
									interludeProb *= (1 - p.simpleOnsetProbability);
									interludeProb *= p.onsetClusterLengthProbabilities[j - 1];
								}
								compoundInterludeH += scaledInfo(codaProb * interludeProb); 
							}
					}
			}
			
			// Complex entropies
			double initialOnsetH = decisionEntropy(
					new double[]{ ioNode.emptyOnsetChance, ioNode.simpleOnsetChance, ioNode.complexOnsetChance },
					new double[]{ 0, simpleOnsetH, complexOnsetH});
			
			double lightInterludeH = decisionEntropy(
					new double[] { liNode.basicHiatusChance, liNode.basicSimpleOnsetChance },
					new double[] { 0, simpleOnsetH });
			
			double medialLightRimeH = simpleNucleusH + lightInterludeH;
			
			double heavyInterludeH = decisionEntropy(
					new double[] { hiNode.complexOnsetChance, hiNode.compoundInterludeChance },
					new double[] { complexOnsetH, compoundInterludeH });
			
			double interludeWeightH = decisionEntropy(
					new double[] { mcnNode.basicLightInterludeChance, mcnNode.basicHeavyInterludeChance },
					new double[] { lightInterludeH, heavyInterludeH });
			
			double medialHeavyRimeH = decisionEntropy(
					new double[] { mhrNode.basicSimpleNucleusChance, mhrNode.basicComplexNucleusChance },
					new double[] { simpleNucleusH + heavyInterludeH, complexNucleusH + interludeWeightH });
			
			medialRimeH = decisionEntropy(
					new double[] { mswNode.lightRimeChance, mswNode.heavyRimeChance },
					new double[] { medialLightRimeH, medialHeavyRimeH });
			
			double lightCodaH = decisionEntropy(
					new double[] { lcNode.emptyCodaChance, lcNode.simpleCodaChance },
					new double[] { 0, simpleCodaH });
			
			double codaWeightH = decisionEntropy(
					new double[] { thrcnNode.lightCodaChance, thrcnNode.heavyCodaChance },
					new double[] { lightCodaH, complexOnsetH });
			
			double terminalLightRimeH = simpleNucleusH + lightCodaH;
			
			double terminalHeavyRimeH = decisionEntropy(
					new double[] { thrNode.basicSimpleNucleusChance, thrNode.basicComplexNucleusChance },
					new double[] { simpleNucleusH + complexCodaH, complexNucleusH + codaWeightH });
			
			terminalRimeH = decisionEntropy(
					new double[] { tswNode.basicLightRimeChance, tswNode.basicHeavyRimeChance },
					new double[] { terminalLightRimeH, terminalHeavyRimeH });
			
			// Print results
//			System.out.println("Simple onset entropy:\t\t\t\t\t" + simpleOnsetH);
//			System.out.println("Complex onset entropy:\t\t\t\t\t" + complexOnsetH);
//			System.out.println("Simple nucleus entropy:\t\t\t\t\t" + simpleNucleusH);
//			System.out.println("Complex nucleus entropy:\t\t\t\t" + complexNucleusH);
//			System.out.println("Simple coda entropy:\t\t\t\t\t" + simpleCodaH);
//			System.out.println("Complex coda entropy:\t\t\t\t\t" + complexCodaH);
//			System.out.println("Compound interlude entropy:\t\t\t\t" + compoundInterludeH);
//			System.out.println();
//			
//			System.out.println("Initial onset entropy:\t\t\t\t\t" + initialOnsetH);
//			System.out.println("Medial rime entropy:\t\t\t\t\t" + medialRimeH);
//			System.out.println("\tMedial light rime entropy:\t\t\t" + medialLightRimeH);
//			System.out.println("\tMedial heavy rime entropy:\t\t\t" + medialHeavyRimeH);
//			System.out.println("\t\tInterlude weight entropy:\t\t" + interludeWeightH);
//			System.out.println("\t\t\tLight interlude entropy:\t" + lightInterludeH);
//			System.out.println("\t\t\tHeavy interlude entropy:\t" + heavyInterludeH);
//			System.out.println("Terminal rime entropy:\t\t\t\t\t" + terminalRimeH);
//			System.out.println("\tTerminal light rime entropy:\t\t\t" + terminalLightRimeH);
//			System.out.println("\tTerminal heavy rime entropy:\t\t\t" + terminalHeavyRimeH);
//			System.out.println("\t\tCoda weight entropy:\t\t\t" + codaWeightH);
//			System.out.println("\t\t\tLight coda entropy:\t\t" + lightCodaH);
//			
//			System.out.println();
//			System.out.printf("Word entropy: %.3f + %.3f per syllable beyond the first\n", 
//								initialOnsetH + terminalRimeH, medialRimeH);
		}
		
	}
	
	private double scaledInfo(double p)
	{
		if (p == 0)
			return 0;
		return -p * Math.log(p);
	}
	
	private double decisionEntropy(double[] probabilities, double[] entropies)
	{
		if (probabilities.length != entropies.length)
		{
			System.err.println("Error in decisionEntropy(): length of probabilities[] and entropies[] did not match");
			System.exit(0);
		}
		
		double result = 0;
		for (int i = 0; i < probabilities.length; i++)
		{
			double p = (probabilities[i] == 0) ? 0 : -Math.log(probabilities[i]);
			
			result += probabilities[i] * (p + entropies[i]);
		}
		
		return result;
	}
}


