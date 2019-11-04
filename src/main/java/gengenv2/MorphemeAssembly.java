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

import java.util.Random;

import gengenv2.enums.ConstituentType;
import gengenv2.morphemes.ConsonantPhoneme;
import gengenv2.morphemes.Constituent;
import gengenv2.morphemes.Feature;
import gengenv2.morphemes.Morpheme;
import gengenv2.morphemes.Phoneme;
import gengenv2.morphemes.Root;
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
	private VowelTerminationNode vtNode;
	private RootNucleusNode rnNode;
	
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
		vtNode		= new VowelTerminationNode();
		rnNode		= new RootNucleusNode();
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
		return makeWord(rng.nextGaussian() * icStdev + icMean);
	}
	
	public Morpheme makeBoundRoot()
	{
		morpheme = new Root(true);
		return makeWord(boundRootInfoConStdev, boundRootInfoConMean);
	}
	
	public Morpheme makeFreeRoot()
	{
		morpheme = new Root(false);
		return makeWord(freeRootInfoConStdev, freeRootInfoConMean);
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
	protected Morpheme makeWord(double icTarget)
	{
		// Initialize naming variables
		this.icTarget = icTarget;
		prev = null;
		pName = 1;
		
		// Propagate through the flowchart until one of the nodes returns null
		Node node = ioNode;

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
		private double freeRootSyllableEntropy;			// entropy for a final syllable starting after a consonant
		private double boundRootSyllableEntropy;		// entropy for a root-ending syllable starting after a consonant
	
		private double[] hiatusMedialSyllableEntropies;		// Entropies for syllables starting with word-medial nucleus beginning after each hiatus vowel
		private double[] hiatusBoundRootSyllableEntropies;	// Entropies for terminal syllables beginning after each hiatus vowel, ending with a bound root
		private double[] hiatusFreeRootSyllableEntropies;	// Entropies for terminal syllables beginning after each hiatus vowel, ending with a free root
		
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
			freeRootSyllableEntropy = getFinalSyllableEntropy(p.medialNuclei, p.terminalNuclei);
			
			System.out.println("General syllable entropies");
			System.out.printf("\t%.3f Initial syllable (vowel-initial only)\n", initialSyllableEntropy);
			System.out.printf("\t%.3f Medial syllable\n", medialSyllableEntropy);
//			System.out.printf("\t%.3f Terminal syllable with coda\n", consonantTerminationEntropy);
//			System.out.printf("\t%.3f Terminal syllable, no coda\n", vowelTerminationEntropy);
			System.out.printf("\t%.3f Free root terminal syllable (either)\n", freeRootSyllableEntropy);
			System.out.printf("\t%.3f Bound root terminal syllable\n", boundRootSyllableEntropy);
			
			// 2. non-terminal hiatus entropies for each vowel
			if (p.features.hiatus != Feature.NO)
			{
				// medial
				hiatusMedialSyllableEntropies = new double[p.vowelInventory.length];
				
				for (int i = 0; i < p.vowelInventory.length; i++)
				{
					VowelPhoneme vp = p.vowelInventory[i];
				
					// medial
					hiatusMedialSyllableEntropies[i] = getNonfinalSyllableEntropy(vp.getFollowers());
					
					// terminal (bound root)
					hiatusBoundRootSyllableEntropies[i] = vp.getRootFollowers().getEntropy();
					
					// terminal (free root)
					hiatusFreeRootSyllableEntropies[i] = getFinalSyllableEntropy(vp.getFollowers(), vp.getTerminalFollowers());
				} 
			}		
			else
			{
				hiatusMedialSyllableEntropies = new double[0];
				hiatusBoundRootSyllableEntropies = new double[0];
				hiatusFreeRootSyllableEntropies = new double[0];
			}
		}
		
		/**
		 * Decides whether to add a root nucleus (for regular names), a consonant or vowel termination (for
		 * suffixes), or to add a medial syllable and revisit the decision again after.
		 */
		public Node nextNode()
		{			
			
			// For suffixes
//			if (name.getWordType() == WordType.SUFFIX)
//			{
//				double hMedial, hTerminal;
//				
//				// Get entropy measurements
//				if (prev != null && prev.type == ConstituentType.NUCLEUS)
//				{
//					VowelPhoneme v = ((VowelPhoneme) prev.lastPhoneme());
//					hMedial = v.hiatusMedialSyllableEntropy;
//					hTerminal = v.hiatusTerminalSyllableEntropy;
//				}
//				else
//				{
//					hMedial = medialSyllableEntropy;
//					hTerminal = terminalSyllableEntropy;
//				}
//				
//				double currentIC = -Math.log(pName);
//				double hMedialDiff = Math.abs(hMedial + currentIC - icTarget);
//				double hTerminalDiff = Math.abs(hTerminal + currentIC - icTarget);
//				
//				// Use entropy measurements to decide what kind of syllable will bring us closest to the target
//				return (hTerminal < hMedialDiff || currentIC > icTarget || name.sylCount() > 0) ? tsNode : mnNode;
//			}
			
			// For in-progress names ending in roots
//			else
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
						Constituent next = p.medialOnsets.pick();
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
		}

		/**
		 * Calculates the medial syllable entropy for a library of medial nuclei
		 * @param 	nuclei					A library of medial nuclei
		 * @param 	interludeEntropies		The interlude entropy for each vowel in the NameAssembly's Phonology
		 * @param 	nucleusWeightings		Weightings for the probability of each vowel starting a syllable
		 * @return	The medial syllable entropy for this nucleus library
		 */
		private double getMedialEntropy(ConstituentLibrary nuclei, double[] interludeEntropies,
										double[] nucleusWeightings)
		{
			if (nuclei == null || nuclei.size() == 0)
				return 0;
			
			double[] nucleusEntropies = new double[nuclei.size()];
			double[] nucleusProbabilities = new double[nuclei.size()];
			double sum = 0;
			
			// Obtain the entropy and probability for each nucleus in the library
			int index = 0;
			for (int i = 1; i <= nuclei.maxLength(); i++)
				for (Constituent curr : nuclei.getMembersOfLength(i))
				{
					Phoneme ph = curr.lastPhoneme();
					
					// Entropy and probability weight depend solely on the final vowel phoneme, so for each 
					// Constituent we can obtain these values from interludeEntropies and nucleusWeightings
					// entries corresponding to the Constituent's final phoneme
					for (int j = 0; j < p.vowelInventory.length; j++)
						if (ph == p.vowelInventory[j])
						{
							nucleusEntropies[index] = interludeEntropies[j];
							nucleusProbabilities[index] = nucleusWeightings[j]
									* curr.getProbability() * nuclei.getLengthProbability(curr.size());
							sum += nucleusProbabilities[index];
							break;
						}
					index++;
				}
		
			// Normalize probabilities
			for (int i = 0; i < nucleusProbabilities.length; i++)
				nucleusProbabilities[i] /= sum;
			
			return decision(nucleusProbabilities, nucleusEntropies);
		}

		private double getConsonantTerminationChance(ConstituentLibrary medialLib, ConstituentLibrary terminalLib)
		{
			
		}
		
		private double getMedialOnsetEntropy()
		{
			if (nonemptyBridgeEntropy > 0)
				return nonemptyBridgeEntropy;
			
			double[] pForEachOnset = new double[p.medialOnsets.size()];
			double[] hForEachOnset = new double[p.medialOnsets.size()];
			
			System.out.println("BRIDGE ENTROPY");
			for (Constituent c : p.medialOnsets.getMembers())
				System.out.print(c.getContent().segment.expression + " ");
			System.out.println();
			
			for (int i = 0; i < p.medialOnsets.size(); i++)
			{
				ConsonantPhoneme onsetHead = (ConsonantPhoneme) p.medialOnsets.getMembers().get(i).getContent();
				
				System.out.println("Entropy for onset head " + onsetHead.segment.expression);
				
				// event: add a coda from the onset head's bridge preceders list?
				double hCoda = onsetHead.getBridgePreceders().getEntropy();
				double pCoda = onsetHead.getMedialCodaChance();
				double hNoCoda = 0;
				double pNoCoda = 1 - pCoda;
				double hAddCoda = Entropy.decision(new double[] { pCoda, pNoCoda }, new double[] { hCoda, hNoCoda });
				
				System.out.println("\t pCoda: " + pCoda);
				System.out.println("\t hCoda: " + hCoda);
				System.out.println("\t pNoCoda: " + pNoCoda);
				System.out.println("\t hNoCoda: " + hNoCoda);
				System.out.println("\t hAddCoda: " + hAddCoda);
				
				
				
				// event: expand the onset head into a cluster?
				double hCluster = onsetHead.getOnsetFollowers().getClusterEntropy(p.getMaxOnsetLength() - 1);
				double pCluster = onsetHead.getOnsetFollowers().getClusterChance();
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
		
		private double getFinalSyllableEntropy(ConstituentLibrary medialNuclei, ConstituentLibrary terminalNuclei)
		{
			if (medialNuclei.size() == 0 && terminalNuclei.size() == 0)	// For hiatus libraries with neither medial nor terminal followers
				return -1;
			
			double hConsonantTermination, hVowelTermination;
			double pConsonantTermination, pVowelTermination;
			
			// Calculate chance of ending the syllable with a consonant
			if (p.features.terminalCodas == Feature.NO)
				pConsonantTermination = 0;
			else if (p.features.terminalCodas == Feature.REQUIRED || terminalNuclei.size() == 0)
				pConsonantTermination = 1;
			else
			{
				pConsonantTermination = p.baseTerminalCodaChance;
				pVowelTermination = 1 - pConsonantTermination;
				
				int qtyCodas= (p.features.terminalCodas != Feature.NO) ? p.terminalCodas.size() : 0;
				int qtyNuclei = (p.features.terminalCodas != Feature.REQUIRED) ? terminalNuclei.size() : 0;
				
				// Scale pConsonantTermination toward 0 or 1 according to the proportions of terminal consonants to terminal nuclei
				if (qtyCodas < qtyNuclei)
					pConsonantTermination *= qtyCodas / qtyNuclei;
				else if (qtyCodas > qtyNuclei)
					pConsonantTermination = 1 - pVowelTermination * qtyNuclei / qtyCodas;
			}
			
			pVowelTermination = 1 - pConsonantTermination;
			
			
			// Calculate entropy
			if (p.features.terminalCodas == Feature.NO)
				hConsonantTermination = 0;
			else
				hConsonantTermination = medialNuclei.getEntropy() + p.terminalCodas.getEntropy();
			
			pVowelTermination = 1 - pConsonantTermination;
			hVowelTermination = terminalNuclei.getEntropy();
			
			terminalSyllableEntropy = Entropy.decision(
										new double[] { pConsonantTermination, pVowelTermination },
										new double[] { hConsonantTermination, hVowelTermination });
			
			return terminalSyllableEntropy;
		}
		
		private double getNucleusEntropy(VowelPhoneme nucleus)
		{
			int followerCount = nucleus.getFollowers().size();
			
			System.out.println("\tbaseHiatusChance: " + p.baseHiatusChance);
			double pAddNoOnset = p.baseHiatusChance * followerCount / (p.medialOnsets.size() + followerCount);
			double hAddNoOnset = 0;
			double pAddOnset = 1 - pAddNoOnset;
			double hAddOnset = getMedialOnsetEntropy();
			
			return Entropy.decision(new double[] { pAddOnset,  pAddNoOnset }, new double[] { hAddOnset, hAddNoOnset });
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
			Constituent next;
			
			// Add nucleus
			ConstituentLibrary lib;
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
		private double nonemptyCodaWeight;
		private double nonemptyOnsetWeight;
		
		/**
		 * Constructor calculates and stores reusable weightings for nonempty coda and onsets
		 */
		public InterludeNode()
		{
			nonemptyCodaWeight = (p.medialCodas == null) ? 0 : Math.log(p.medialCodas.getCompoundSize() + 1);
			nonemptyCodaWeight *= p.baseMedialCodaChance;
			nonemptyOnsetWeight = Math.log(p.medialOnsets.size() + 1);
			nonemptyOnsetWeight *= p.baseMedialOnsetChance;
			System.out.println("Average medial coda chance: " + getAverageCodaChance());
		}
		
		/**
		 * Optionally adds a coda and optionally adds an onset before returning to the NucleusLocationNode.
		 */
		Node nextNode()
		{
			addCoda();
			addOnset();
			return nlNode;
		}
		
		/**
		 * Optionally adds a coda to the name
		 */
		private void addCoda()
		{
			Constituent next;
			
			double emptyCodaChance = Math.log(p.medialOnsets.size() + prev.followers().size() + 1);
			emptyCodaChance *= 1 - p.baseMedialCodaChance;
			emptyCodaChance /= emptyCodaChance + nonemptyCodaWeight;
			
			if (rng.nextDouble() < emptyCodaChance)
			{
				pName *= emptyCodaChance;
			}
			else
			{
				addConstituentFrom(p.medialCodas);
				pName *= 1 - emptyCodaChance;
			}
		}
		
		/**
		 * Optionally adds an onset to the name. If a coda was previously added, an onset must be added now.
		 * Otherwise, the possibility of hiatus remains open.
		 */
		private void addOnset()
		{
			// Onset following a nucleus: add a medial onset, or add nothing (hiatus)
			if (prev.type == ConstituentType.NUCLEUS)
			{
				double emptyOnsetChance = 0;
				if (prev.followers() != null && prev.followers().size() > 0)
				{
					emptyOnsetChance = Math.log(prev.followers().size() + 1);
					emptyOnsetChance *= 1 - p.baseMedialOnsetChance;
					
					// Normalize
					emptyOnsetChance /= emptyOnsetChance + nonemptyOnsetWeight;
				}
				
				if (rng.nextDouble() < emptyOnsetChance)
				{
					pName *= emptyOnsetChance;
				}
				else
				{
					pName *= 1 - emptyOnsetChance;
					addConstituentFrom(p.medialOnsets);
				}
			}
			
			// Onset following a coda: add an onset from the coda's follower list
			else
				addConstituentFrom(prev.followers());
		}
		
		/**
		 * Calculates the (average) chance of including a medial coda in the interlude. Does not includes
		 * syllables beginning in hiatus in its considerations. 
		 * @return	The (rough) (average) chance of a medial coda occurring in a syllable
		 */
		public double getAverageCodaChance()
		{
			double chance = 0;
			for (int i = 1; i < p.medialNuclei.maxLength(); i++)
			{
				for (Constituent c : p.medialNuclei.getMembersOfLength(i))
				{
					double emptyCodaChance = Math.log(p.medialOnsets.size() + c.followers().size() + 1);
					emptyCodaChance *= 1 - p.baseMedialCodaChance;
					double nonemptyCodaChance = nonemptyCodaWeight / (emptyCodaChance + nonemptyCodaWeight);
					chance += c.getProbability() * p.medialNuclei.getLengthProbability(c.size()) * nonemptyCodaChance;
				}
			}
			return chance;
		}
	}
	
	private class TerminalSyllableNode extends Node
	{
		public Node nextNode()
		{
			int cFinal, vFinal;
			if (prev != null && prev.type == ConstituentType.NUCLEUS)
			{
				cFinal = prev.followers().size();
				vFinal = ((VowelPhoneme)prev.lastPhoneme()).getTerminalFollowers().size();
			}
			else
			{
				cFinal = p.medialNuclei.size();
				vFinal = p.terminalNuclei.size();
			}
			cFinal *= (p.terminalCodas != null) ? p.terminalCodas.size() : 0;
			
			double pConsonantTermination = Math.log(cFinal + 1) * p.baseTerminalCodaChance;
			double pVowelTermination = Math.log(vFinal + 1) * (1 - p.baseTerminalCodaChance);
			pConsonantTermination /= pConsonantTermination + pVowelTermination;
			
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
			
			// Add coda
			addConstituentFrom(p.terminalCodas);
			
			// Names has completed instead of ending in a root
//			if (name.getWordType() == WordType.STEM)
//				name.setWordType(WordType.COMPLETE);
						
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
				addConstituentFrom(((VowelPhoneme) prev.lastPhoneme()).getTerminalFollowers());
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
				addConstituentFrom(((VowelPhoneme)prev.lastPhoneme()).getRootFollowers());
			else
				addConstituentFrom(p.rootNuclei);
			
			return null;
		}
	}
	
	/**
	 * Picks a Constituent from the given library and adds it to the name, updating its cumulative probability
	 * measurement in pName accordingly.
	 * @param	lib	The library from which to pick & add a Constituent
	 * @since	1.2
	 */
	private void addConstituentFrom(ConstituentLibrary lib)
	{
		ArrayList<Constituent> next = lib.pick();
		pName *= next.getProbability();
		pName *= lib.getLengthProbability(next.size());
		addConstituent(next);
	}
	
	/**
	 * Adds a Constituent to the end of the current Name, while updating the preference to the
	 * previous Constituent.
	 * @param 	c	The Constituent to append
	 * @since	1.1
	 */
	private void addConstituent(Constituent c)
	{
//		if (pName == 0)
//			try {
//				throw new Exception();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				System.exit(0);
//			}
		prev = c;
		for (int i = 0; i < c.getLength(); i++)
			morpheme.add(c.getContent(i));
	}
	
	
}