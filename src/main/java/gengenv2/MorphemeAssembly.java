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
import java.util.Random;

import gengenv2.enums.ConstituentType;
import gengenv2.enums.SuffixType;
import gengenv2.morphemes.ConsonantPhoneme;
import gengenv2.morphemes.Constituent;
import gengenv2.morphemes.Feature;
import gengenv2.morphemes.Morpheme;
import gengenv2.morphemes.Root;
import gengenv2.morphemes.Suffix;
import gengenv2.morphemes.VowelPhoneme;

/**
 * A sort of flowchart or state machine for generating names according to a Phonology's inventory, phonotactics, 
 * and stress system. The flowchart is structurally the same regardless of the language, the weights for
 * state transitions depend on the particulars of the Phonology - the sizes of inventories, the values of
 * coefficients, etc. - and so every language needs its own instantiation.
 * 
 * Structure vastly simplified in v1.2.
 * 
 * @author	Clayton Cooper
 * @version	1.2
 * @since	1.0
 */
public class MorphemeAssembly
{
	// General variables
	Phonology p;			// Reference to parent phonology
	Random rng;				// Reference to parent Phonology's random number generator
	
	// Flowchart nodes
	private InitialOnsetNode ioNode;
	private NucleusLocationNode nlNode;
	private InitialNucleusNode inNode;
	private MedialNucleusNode mnNode;
	private InterludeNode ilNode;
	private TerminalSyllableNode tsNode;
	private ConsonantTerminationNode ctNode;
	private FinalCodaNode fcNode;
	private VowelTerminationNode vtNode;
	private RootNucleusNode rnNode;
	private SuffixLengthNode slNode;
	private SyllabicSuffixNode ssNode;
	
	// Current name variables
	Morpheme morpheme;				// The name currently being generated
	double icTarget;		// Intended information content of the current name
	double pName;			// Probability of generating the current name
	Constituent prev;		// The most recent syllable constituent added to the name
	
	// Information content variables
	double boundRootInfoConMean = 8;				// Average value of target information content for bound roots
	double boundRootInfoConStdev = 1.5;				// Standard deviation of target information content for round roots
	double freeRootInfoConMean = 8;					// Average value of target information content for free roots
	double freeRootInfoConStdev = 1.5;				// Standard deviation of target information content for free roots

	
	/**
	 * Constructor simply initializes all Nodes in the assembly flowchart, and saves the reference to the
	 * given Phonology as well as its RNG.
	 * 
	 * @param	p	The Phonology to which this NameAssembly belongs	
	 */
	public MorphemeAssembly(Phonology p)
	{
		this.p = p;
		rng = PublicRandom.getRNG();
	
		ioNode 		= new InitialOnsetNode();
		nlNode 		= new NucleusLocationNode();
		inNode		= new InitialNucleusNode();
		mnNode		= new MedialNucleusNode();
		ilNode		= new InterludeNode();
		tsNode		= new TerminalSyllableNode();
		ctNode		= new ConsonantTerminationNode();
		fcNode		= new FinalCodaNode();
		vtNode		= new VowelTerminationNode();
		rnNode		= new RootNucleusNode();
		slNode		= new SuffixLengthNode();
		ssNode		= new SyllabicSuffixNode();
	}
	
//	/**
//	 * @return	A complete name generated according to this NameAssembly's flowchart
//	 * @since	1.2
//	 */
//	public Name makeName()
//	{
//		return makeName(rng.nextGaussian() * infoConStdev + infoConMean, WordType.STEM);
//	}
//	
//	/**
//	 * A suffix generated according to this NameAssembly's flowchart, meant to be attached to
//	 * to a Name ending in a root nucleus to form a complete Name.
//	 * 
//	 * @return	A suffix generated according to this NameAssembly's flowchart
//	 * @since	1.2	
//	 */
//	public Name makeSuffix()
//	{
//		return makeName(rng.nextGaussian() * suffixInfoConStdev + suffixInfoConMean, WordType.SUFFIX);
//	}
//	
//	public Name makeSuffix(double mean, double stdev)
//	{
//		return makeName(rng.nextGaussian() * stdev + mean, WordType.SUFFIX);
//	}
	
	/**
	 * @return	A complete name generated according to this NameAssembly's flowchart
	 * @since	1.2
	 */
	private Morpheme makeWord(double icStdev, double icMean)
	{
		return makeWord(rng.nextGaussian() * icStdev + icMean, ioNode);
	}
	
	public Root makeBoundRoot()
	{
		morpheme = new Root(true);
		return (Root) makeWord(boundRootInfoConStdev, boundRootInfoConMean);
	}
	
	public Root makeFreeRoot()
	{
		morpheme = new Root(false);
		return (Root) makeWord(freeRootInfoConStdev, freeRootInfoConMean);
	}
	
	public Suffix makeSuffix(double icTarget)
	{
		morpheme = new Suffix();
		return (Suffix) makeWord(icTarget, slNode);
	}
	
	/**
	 * Generates a name by first resetting the naming variables and then invoking a particular Node's nextNode()
	 * method (InitialOnsetNode for complete names, NucleusLocationNode for suffixes). This initiates a decision 
	 * process that propagates through all the Nodes in the flowchart, each of which may add a syllable Constituent 
	 * (or two, for interludes) to the in-progress Name.
	 * 
	 * @return	The completed name
	 * @since	1.0
	 */
	protected Morpheme makeWord(double icTarget, Node firstNode)
	{
		// Initialize naming variables
		this.icTarget = icTarget;
		prev = null;
		pName = 1;
		
		// Propagate through the flowchart until one of the nodes returns null
		Node node = firstNode;

		int nodesTraversed = 0;
		try
		{
			while (node != null)
			{
				node = node.nextNode();
				if (++nodesTraversed > 100)
					throw new Exception("Could not reach exit from assembly flowchart.");
				
			}
		} catch (Exception e)
		{
			System.err.println(morpheme);
			e.printStackTrace();
			System.exit(0);
		}

		morpheme.setInformationContent(-Math.log(pName));
		
		return morpheme;
	}
	
	/**
	 * Generates a name by first resetting the naming variables and then invoking a particular Node's nextNode()
	 * method (InitialOnsetNode for complete names, NucleusLocationNode for suffixes). This initiates a decision 
	 * process that propagates through all the Nodes in the flowchart, each of which may add a syllable Constituent 
	 * (or two, for interludes) to the in-progress Name.
	 * 
	 * @return	The completed name
	 * @since	1.0
	 *//*
	protected Name makeName(double icTarget, WordType type)
	{
		// Initialize naming variables
		this.icTarget = icTarget;
		root = new Name();
		prev = null;
		pName = 1;
		
		// Propagate through the flowchart until one of the nodes returns null
		Node node;
		if (name.getWordType() == WordType.SUFFIX)
			node = nlNode; 
		else
			node = ioNode;

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

		name.setInformationContent(-Math.log(pName));
		
		// Set root strength
		if (name.getWordType() == WordType.STEM)
		{
			// Diphthongs are always strong
			if (prev.size() > 1)
				name.strength = RootStrength.STRONG;
			// Monosyllabic roots are always strong
			if (name.sylCount() == 1)
				name.strength = RootStrength.STRONG;
			else if (rng.nextDouble() < ((VowelPhoneme)prev.lastPhoneme()).strongRootEndChance)
				name.strength = RootStrength.STRONG;
		}
		else if (name.getWordType() == WordType.SUFFIX)
		{
			Constituent first = name.getSyllables().get(0).constituents[1];
			
			// Suffixes comprising only a single nucleus are always strong
			if (name.sylCount() == 1 && name.getSyllables().get(0).constituents[2] == null)
				name.strength = RootStrength.STRONG;

			// Pivot vowels in polysyllabic suffixes are always weak
			else if (name.sylCount() > 1)
				name.strength = RootStrength.WEAK;
			
			// Diphthongs are always strong
			else if (first.size() > 1)
				name.strength = RootStrength.STRONG;
			
			else if (rng.nextDouble() < ((VowelPhoneme)first.lastPhoneme()).strongSuffixStartChance)
				name.strength = RootStrength.STRONG;
		}
		
		return name;
	}*/
	
	/**
	 * A simple interface implemented by all Nodes in the name assembly flowchart, to ensure that they can all
	 * be called upon to hand over the next Node in the sequence.
	 * @since	1.0
	 */
	private abstract class Node
	{
		abstract Node nextNode();
	}

	/**
	 * Decides whether to start the Name with an onset before advancing to the NucleusLocationNode.
	 * 
	 * @since 1.0
	 */
	private class InitialOnsetNode extends Node
	{
		public double emptyOnsetChance;
		
		/**
		 * Constructor calculates and stores emptyOnsetChance for future use in nextNode().
		 */
		public InitialOnsetNode()
		{
			
			// Empty onset chance
			if (p.features.initialOnsets != Feature.REQUIRED)
			{
				emptyOnsetChance = Math.log(p.initialNuclei.size());
				emptyOnsetChance *= p.baseEmptyInitialOnsetChance;
			}
			else
				emptyOnsetChance = 0;
			
			// Scaling each prominence value by the log of the number of possibilities it represents
			// helps ensure that rich inventories are well represented while poor ones are sampled
			// less frequently. This reduces the chances of something like a language with only 1
			// onset cluster having that cluster at the start of 20% of names.
			// This technique is repeated abundantly throughout the Node classes.

			// Nonempty onset chance
			double nonemptyOnsetChance = Math.log(p.initialOnsets.size() + 1);
			nonemptyOnsetChance *= (1 - p.baseEmptyInitialOnsetChance);
			
			// Normalize chances
			double sum = emptyOnsetChance + nonemptyOnsetChance;
			emptyOnsetChance /= sum;
		}
		
		/**
		 * Adds an onset from the initial onset library (or no onset) and advances to the NucleusLocationNode
		 */
		public Node nextNode()
		{	
			double rand = p.rng.nextDouble();
			
			// Option 1: Empty onset
			if (rand < emptyOnsetChance)
				pName *= emptyOnsetChance;

			// Option 2: Nonempty onset
			else
			{
				addConstituentFrom(p.initialOnsets);
				pName *= 1 - emptyOnsetChance;
			}
			
			return nlNode;
		}
	}
	
	/**
	 * Decides whether the next nucleus should represent the first or last or a medial syllable, and advances to
	 * the corresponding nucleus Node from there. The process considers the entropy (expected information content)
	 * of each kind of syllable and functions by adding whichever would bring the Name's running info content
	 * closest to the target: a terminal/root syllable, or one or more medial onsets. 
	 * @since	1.0
	 */
	private class NucleusLocationNode extends Node
	{
		// General entropy measurements (for syllables not beginning with a hiatus)
		private double initialSyllableEntropy;			// entropy for an initial, non-final syllable beginning with a nucleus
		private double medialSyllableEntropy;			// entropy for a medial syllable starting after a consonant
		private double terminalSyllableEntropy;			// entropy for a final syllable starting after a consonant
		private double boundRootSyllableEntropy;		// entropy for a root-ending syllable starting after a consonant
	
		private double[] hiatusMedialSyllableEntropies;		// Entropies for syllables starting with word-medial nucleus beginning after each hiatus vowel
		private double[] hiatusBoundRootSyllableEntropies;	// Entropies for terminal syllables beginning after each hiatus vowel, ending with a bound root
		private double[] hiatusTerminalSyllableEntropies;	// Entropies for terminal syllables beginning after each hiatus vowel, ending with a free root
		
		private double nonemptyBridgeEntropy;			// Entropy for the selection of a medial onset, plus potentially a medial coda, between two nuclei
		
		
		/**
		 * Constructor measures and stores entropy for different types of initial, medial, and terminal syllables.
		 */
		public NucleusLocationNode()
		{
			// CALCULATE ENTROPIES
			// 1. non-hiatus entropies
			
			// initial
			if (p.features.initialOnsets != Feature.REQUIRED)
				initialSyllableEntropy = getNonfinalSyllableEntropy(p.initialNuclei);
			
			// medial
			medialSyllableEntropy = getNonfinalSyllableEntropy(p.medialNuclei);

			// terminal (bound root)
			boundRootSyllableEntropy = p.rootNuclei.getEntropy();
			
			// terminal (free root)
			terminalSyllableEntropy = getFinalSyllableEntropy(p.medialNuclei, p.terminalNuclei, p.closedFinalSyllableChance);
			
			System.out.println("General syllable entropies");
			System.out.printf("\t%.3f Initial syllable (vowel-initial only)\n", initialSyllableEntropy);
			System.out.printf("\t%.3f Medial syllable\n", medialSyllableEntropy);
//			System.out.printf("\t%.3f Terminal syllable with coda\n", consonantTerminationEntropy);
//			System.out.printf("\t%.3f Terminal syllable, no coda\n", vowelTerminationEntropy);
			System.out.printf("\t%.3f Free root terminal syllable (either)\n", terminalSyllableEntropy);
			System.out.printf("\t%.3f Bound root terminal syllable\n", boundRootSyllableEntropy);
			
			// 2. non-terminal hiatus entropies for each vowel
			if (p.features.hiatus != Feature.NO)
			{
				// medial
				hiatusMedialSyllableEntropies = new double[p.vowelInventory.length];
				hiatusBoundRootSyllableEntropies = new double[p.vowelInventory.length];
				hiatusTerminalSyllableEntropies = new double[p.vowelInventory.length];
				
				for (int i = 0; i < p.vowelInventory.length; i++)
				{
					VowelPhoneme vp = p.vowelInventory[i];
				
					// medial
					hiatusMedialSyllableEntropies[i] = getNonfinalSyllableEntropy(vp.getMedialFollowers());
					
					// terminal (bound root)
					hiatusBoundRootSyllableEntropies[i] = vp.getRootFollowers().getEntropy();
					
					// terminal (free root)
					hiatusTerminalSyllableEntropies[i] = getFinalSyllableEntropy(vp.getMedialFollowers(), vp.getTerminalFollowers(), 
															vp.getClosedFinalSyllableChance());
				} 
			}		
			else
			{
				hiatusMedialSyllableEntropies = new double[0];
				hiatusBoundRootSyllableEntropies = new double[0];
				hiatusTerminalSyllableEntropies = new double[0];
			}
		}
		
		/**
		 * Decides whether to add a root nucleus (for regular names), a consonant or vowel termination (for
		 * suffixes), or to add a medial syllable and revisit the decision again after.
		 */
		public Node nextNode()
		{			
			if (morpheme.phonemes.size() == 0)
				return inNode;
			
			double hMedial, hEnding;

			// Get entropy measurements
			if (prev != null && prev.type == ConstituentType.NUCLEUS)
			{
				VowelPhoneme v = ((VowelPhoneme) prev.getContent());
				hMedial = v.getHiatusMedialSyllableEntropy();
				
				if (morpheme instanceof Root && ((Root) morpheme).isBound())
					hEnding = v.getHiatusRootSyllableEntropy();
				else
					hEnding = v.getHiatusTerminalSyllableEntropy();
			}
			else
			{
				hMedial = medialSyllableEntropy;
				if (morpheme instanceof Root && ((Root) morpheme).isBound())
					hEnding = boundRootSyllableEntropy;
				else
					hEnding = terminalSyllableEntropy;
			}
			
			double hEndingDiff = Math.abs(hEnding + -Math.log(pName) - icTarget);
			double hMedialDiff = Math.abs(hMedial + hEnding + -Math.log(pName) - icTarget);
			
			// Use entropy measurements to decide what kind of syllable will bring us closest to the target
			if (hEndingDiff < hMedialDiff && -Math.log(pName) >= morpheme.minimumInformationContent())
			{
				// If we're adding a root but the previous nucleus doesn't can't undergo hiatus with
				// any of the root nuclei, add a simple onset
				if (hEnding == 0)
				{
					Constituent next = p.medialOnsets.pickSingle();
					pName *= next.getProbability();
					addConstituent(next);
				}
				
				if (morpheme instanceof Root && ((Root) morpheme).isBound())
					return rnNode;
				else
					return tsNode;
			}
			else
				return mnNode;
		
		}

		// Calculates the entropy of the event where a medial onset is generated (plus, optionally, a medial coda)
		private double getNonemptyBridgeEntropy()
		{
			if (nonemptyBridgeEntropy > 0)
				return nonemptyBridgeEntropy;
			
			double[] pForEachOnset = new double[p.medialOnsets.size()];
			double[] hForEachOnset = new double[p.medialOnsets.size()];
			
			for (int i = 0; i < p.medialOnsets.size(); i++)
			{
				ConsonantPhoneme onsetHead = (ConsonantPhoneme) p.medialOnsets.getMembers().get(i).getContent();
				
				System.out.println("Entropy for onset head " + onsetHead.segment.expression);
				
				// event: add a coda from the onset head's bridge preceders list?
				double pCoda = onsetHead.getMedialCodaChance();
				double hCoda = pCoda > 0 ? onsetHead.getBridgePreceders().getEntropy() : 0;
				
				
				double hNoCoda = 0;
				double pNoCoda = 1 - pCoda;
				double hAddCoda = Entropy.decision(new double[] { pCoda, pNoCoda }, new double[] { hCoda, hNoCoda });
				
				System.out.println("\t pCoda: " + pCoda);
				System.out.println("\t hCoda: " + hCoda);
				System.out.println("\t pNoCoda: " + pNoCoda);
				System.out.println("\t hNoCoda: " + hNoCoda);
				System.out.println("\t hAddCoda: " + hAddCoda);
				
				
				
				// event: expand the onset head into a cluster?
				double pCluster = onsetHead.getOnsetFollowers() != null ? onsetHead.getOnsetFollowers().getClusterChance() : 0;
				double hCluster = pCluster > 0 ? onsetHead.getOnsetFollowers().getClusterEntropy(p.getMaxOnsetLength() - 1) : 0;
				
				double hNoCluster = 0;
				double pNoCluster = 1 - pCluster;
				double hAddCluster = Entropy.decision(new double[] { pCluster,  pNoCluster }, new double[] { hCluster, hNoCluster });
				
				System.out.println("\t pCluster: " + pCluster);
				System.out.println("\t hCluster: " + hCluster);
				System.out.println("\t pNoCluster: " + pNoCluster);
				System.out.println("\t hNoCluster: " + hNoCluster);
				System.out.println("\t hAddCluster: " + hAddCluster);
				
				double hForGivenOnsetHead = hAddCoda + hAddCluster;
				
				pForEachOnset[i] = p.medialOnsets.getMembers().get(i).getProbability();
				hForEachOnset[i] = hForGivenOnsetHead;
				
				System.out.println("\t\tTotal: " + hForGivenOnsetHead);
			}
			
			double hMedialOnset = Entropy.decision(pForEachOnset, hForEachOnset);
			System.out.println("\t\t\thAddOnset: " + hMedialOnset);
			
			nonemptyBridgeEntropy = hMedialOnset;
			return hMedialOnset;
		}
		
		// Calculates the entropy of an event where a medial nucleus is chosen, followed by (optionally) a medial onset
		private double getNonfinalSyllableEntropy(ConstituentLibrary nuclei)
		{
			double[] pForEachNucleus = new double[nuclei.size()];
			double[] hForEachNucleus = new double[nuclei.size()];
			
			for (int i = 0; i < nuclei.size(); i++)
			{
				VowelPhoneme nucleus = (VowelPhoneme) nuclei.get(i).getContent();	
				double hNucleus = getNucleusEntropy(nucleus);
				
				pForEachNucleus[i] = nuclei.get(i).getProbability();
				hForEachNucleus[i] = hNucleus;
			}
			
			return Entropy.decision(pForEachNucleus, hForEachNucleus);
		}
		
		// Calculates the entropy of an event where a choice is made between generating an open or closed final syllable
		private double getFinalSyllableEntropy(ConstituentLibrary medialNuclei, ConstituentLibrary terminalNuclei, double pConsonantTermination)
		{
			if (medialNuclei.size() == 0 && terminalNuclei.size() == 0)	// For hiatus libraries with neither medial nor terminal followers
				return -1;
			
			double hConsonantTermination, hVowelTermination;
			double pVowelTermination;
			
			// Calculate chance of ending the syllable with a consonant
			pVowelTermination = 1 - pConsonantTermination;
			
			// Calculate entropy
			if (p.features.terminalCodas == Feature.NO)
				hConsonantTermination = 0;
			else
				hConsonantTermination = medialNuclei.getEntropy() + p.terminalCodas.getEntropy();
			
			pVowelTermination = 1 - pConsonantTermination;
			hVowelTermination = terminalNuclei.getEntropy();
			
			return Entropy.decision(new double[] { pConsonantTermination, pVowelTermination }, new double[] { hConsonantTermination, hVowelTermination });
		}
		
		// Calculates the entropy of an event where a decision is made between adding an empty or nonempty medial onset to nucleus in a nonfinal syllable
		private double getNucleusEntropy(VowelPhoneme nucleus)
		{
			double pAddNoOnset = nucleus.getHiatusChance();
			double hAddNoOnset = 0;
			double pAddOnset = 1 - pAddNoOnset;
			double hAddOnset = getNonemptyBridgeEntropy();
			
			return Entropy.decision(new double[] { pAddOnset,  pAddNoOnset }, new double[] { hAddOnset, hAddNoOnset });
		}
		
		public double getTerminalSyllableEntropy()
		{
			return terminalSyllableEntropy;
		}
	}
	
	/**
	 * Attaches an initial nucleus to the start of a word with no initial nucleus, then advances to the
	 * InterludeNode.
	 * TODO: Add an information content check and the ability to transition to a TerminalCodaNode
	 * @since	1.2
	 */
	private class InitialNucleusNode extends Node
	{
		/**
		 * Adds a nucleus from the library of initial nuclei and advances to InterludeNode
		 */
		Node nextNode()
		{
			// Add initial nucleus
			addConstituentFrom(p.initialNuclei);
			return ilNode;
		}
	}
	
	/**
	 * Adds a medial nucleus and advances to the interludeNode.
	 * @since	1.2
	 */
	private class MedialNucleusNode extends Node
	{
		/**
		 * Adds a nucleus from the Phonology's medial nucleus list, or from the previous nucleus' medial followers
		 * list in the case of hiatus, then advances to the InterludeNode.
		 */
		Node nextNode()
		{
			if (prev != null && prev.type == ConstituentType.NUCLEUS)
				addConstituentFrom(prev.followers());
			else
				addConstituentFrom(p.medialNuclei);
			
			return ilNode;
		}
	}
	
	/**
	 * Determines the boundary between the current syllable and the next, adding a compound interlude (coda +
	 * onset), an onset alone, or neither (hiatus), before returnign to the NucleusLocationNode. 
	 * @since	1.2
	 */
	private class InterludeNode extends Node 
	{
		/**
		 * Optionally adds an onset and coda (before the onset) before returning to the NucleusLocationNode.
		 */
		Node nextNode()
		{
			double hiatusChance = ((VowelPhoneme) prev.getContent()).getHiatusChance();
			
			if (rng.nextDouble() < hiatusChance)
			{
				pName *= hiatusChance;
			}
			else
			{
				pName *= 1 - hiatusChance;
				
				// Select a medial onset but do not add it yet
				ArrayList<Constituent> onset = p.medialOnsets.pick();
				pName *= getSelectionProbability(onset, ConstituentType.ONSET, p.medialOnsets.getMaxClusterLength());
				
				// Pick and add coda from onset's preceders list, if applicable
				ConsonantPhoneme onsetHead = (ConsonantPhoneme) onset.get(0).getContent();
				if (rng.nextDouble() < onsetHead.getMedialCodaChance())
				{
					addConstituentFrom(onsetHead.getBridgePreceders());
					pName *= onsetHead.getMedialCodaChance();
				}
				else
					pName *= 1 - onsetHead.getMedialCodaChance();
				
				// Add the chosen medial onset
				addConstituent(onset);
			}
			
			return nlNode;
		}
	}
	
	private class TerminalSyllableNode extends Node
	{
		public Node nextNode()
		{
			double pConsonantTermination;
			
			if (prev != null && prev.type == ConstituentType.NUCLEUS)
				pConsonantTermination = ((VowelPhoneme) prev.getContent()).getClosedFinalSyllableChance();
			else
				pConsonantTermination = p.closedFinalSyllableChance;

			if (rng.nextDouble() < pConsonantTermination)
			{
				pName *= pConsonantTermination;
				return ctNode;
			}
			else
			{
				pName *= 1 - pConsonantTermination;
				return vtNode;
			} 
		}
	}
	
	/**
	 * Ends a Name by adding a nucleus and (non-optional!) coda to the final syllable of a word.
	 * @since	1.2
	 */
	private class ConsonantTerminationNode extends Node
	{	
		/**
		 * Adds a nucleus from the Phonology's medial nucleus list, or from the previous nucleus' medial
		 * followers library in the case of hiatus, then adds a terminal coda and advances to the end state.
		 */
		Node nextNode()
		{
			// Add nucleus
			if (prev != null && prev.type == ConstituentType.NUCLEUS)
				addConstituentFrom(prev.followers());
			else
				addConstituentFrom(p.medialNuclei);
			
			return fcNode;
		}
	}
	
	private class FinalCodaNode extends Node
	{
		Node nextNode()
		{
			// Add coda
			addConstituentFrom(p.terminalCodas);
			return null;
		}
	}
	
	/**
	 * Ends a Name by adding a terminal nucleus.
	 * @since	1.2
	 */
	private class VowelTerminationNode extends Node
	{
		/**
		 * Adds a nucleus from the Phonology's terminal nucleus list, or from the previous nucleus' terminal
		 * followers library in the case of hiatus, then advances to the end state.
		 */
		Node nextNode()
		{
			// Add terminal nucleus
			if (prev != null && prev.type == ConstituentType.NUCLEUS)
				addConstituentFrom(((VowelPhoneme) prev.getContent()).getTerminalFollowers());
			else
				addConstituentFrom(p.terminalNuclei);
			
			// Names has completed instead of ending in a root
//			if (name.getWordType() == WordType.STEM)
//				name.setWordType(WordType.COMPLETE);
			
			return null;
		}
	}
	
	/**
	 * Ends the root of a Name by adding a root nucleus.
	 * @since	1.2
	 */
	private class RootNucleusNode extends Node
	{
		/**
		 * Adds a nucleus from the Phonology's root nucleus list, or from the previous nucleus' root
		 * followers library in the case of hiatus, then advances to the end state.
		 */
		Node nextNode()
		{
			// Add root nucleus
			if (prev != null && prev.type == ConstituentType.NUCLEUS)
				addConstituentFrom(((VowelPhoneme)prev.getContent()).getRootFollowers());
			else
				addConstituentFrom(p.rootNuclei);
			
			return null;
		}
	}
	
	private class SuffixLengthNode extends Node
	{
		double hSyllabicSuffix;
		double hNucleicSuffix;
		double hCaudalSuffix;
		
		public SuffixLengthNode()
		{
			hCaudalSuffix = p.features.terminalCodas != Feature.NO ? p.terminalCodas.getEntropy() : 0;
			hNucleicSuffix = nlNode.getTerminalSyllableEntropy();
			hSyllabicSuffix = hNucleicSuffix + p.medialOnsets.getEntropy();
			
			System.out.println("hCaudalSuffix: " + hCaudalSuffix);
			System.out.println("hNucleicSuffix: " + hNucleicSuffix);
			System.out.println("hSyllabicSuffix: " + hSyllabicSuffix);
			
			if (hCaudalSuffix > hNucleicSuffix)
				System.err.println("Warning: hCaudalSuffix > hNucleicSuffix");
			if (hCaudalSuffix > hSyllabicSuffix)
				System.err.println("Warning: hCaudalSuffix > hSyllabicSuffix");
			if (hNucleicSuffix > hSyllabicSuffix)
				System.err.println("Warning: hNucleicSuffix > hSyllabicSuffix");	
		}
		
		Node nextNode()
		{
			double diff = icTarget;
			SuffixType bestFit = SuffixType.NULL;
			
			// Find the suffix type that will result in the smallest difference
			if (Math.abs(hCaudalSuffix - icTarget) < diff)
			{
				diff = Math.abs(hCaudalSuffix - icTarget);
				bestFit = SuffixType.CAUDAL;
			}
			
			if (Math.abs(hNucleicSuffix - icTarget) < diff)
			{
				diff = Math.abs(hNucleicSuffix - icTarget);
				bestFit = SuffixType.NUCLEIC;
			}
			
			if (Math.abs(hSyllabicSuffix - icTarget) < diff)
			{
				diff = Math.abs(hSyllabicSuffix - icTarget);
				bestFit = SuffixType.SYLLABIC;
			}
			
			// Advance to the whichever node kicks off the suffix type with the smallest gap
			switch (bestFit)
			{
				case CAUDAL:
					return fcNode;
				case NUCLEIC:
					return tsNode;
				case SYLLABIC:
					return ssNode;
				default:
					return null;
			}
		}
	}
	
	private class SyllabicSuffixNode extends Node
	{
		Node nextNode()
		{
			addConstituentFrom(p.medialOnsets);
			return tsNode;
		}
	}
	
	public void addConstituent(Constituent c)
	{
		prev = c;
		morpheme.add(c.getContent());
	}
	
	/**
	 * Picks a Constituent from the given library and adds it to the name, updating its cumulative probability
	 * measurement in pName accordingly.
	 * @param	lib	The library from which to pick & add a Constituent
	 * @since	1.2
	 */
	private void addConstituent(ArrayList<Constituent> seq)
	{
		if (seq.size() == 1)
		{
			addConstituent(seq.get(0));
		}
		else
		{
			// Reverse order of codas
			ArrayList<Constituent> constituents;
			if (seq.get(0).type == ConstituentType.CODA)
			{
				constituents = new ArrayList<Constituent>();
				for (int i = seq.size() - 1; i >= 0; i--)
					constituents.add(seq.get(i));
			}
			else
				constituents = seq;
			
			// Add sequence to morpheme in progress
			for (int i = 0; i < constituents.size(); i++)
				addConstituent(constituents.get(i));
		}
	}
	
	// Picks a constituent (sequence) from a library, adds it to the morpheme in progress, and updates pName with the chance of selecting that sequence
	private void addConstituentFrom(ConstituentLibrary lib)
	{
		ArrayList<Constituent> constituents = lib.pick();
		addConstituent(constituents);
		double p = getSelectionProbability(constituents, lib.getType(), lib.getMaxClusterLength()); 
		
		pName *= p;
		
	}
	
	// The chance of generating a given constituent sequence (single constituent or constituent cluster)
	private double getSelectionProbability(ArrayList<Constituent> constituents, ConstituentType type, int maxClusterLength)
	{
		// Calculate probability of picking this constituent sequence
		double prob = 1;
		
		for (int i = 0; i < constituents.size(); i++)
		{
			Constituent curr = constituents.get(i);
			prob *= curr.getProbability();
			
			if (i == constituents.size() - 1)
			{
				if (i < maxClusterLength - 1)
					prob *= 1 - curr.followers(type).getClusterChance();  
			}
			else
			{
				prob *= curr.followers(type).getClusterChance(); 
			}
		}
		
		// Print result
//		System.out.print("Selection probability for " + type + " ");
//		for (Constituent c : constituents)
//			System.out.print(c.getContent().segment.expression);
//		System.out.println(": " + prob);
		
		return prob;
	}
}