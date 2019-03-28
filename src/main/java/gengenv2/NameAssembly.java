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

import gengenv2.Name.Syllable;
import gengenv2.Phonology.Phoneme;
import gengenv2.Phonology.VowelPhoneme;

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
public class NameAssembly
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
	Name name;				// The name currently being generated
	double icTarget;		// Intended information content of the current name
	double pName;			// Probability of generating the current name
	Constituent prev;		// The most recent syllable constituent added to the name
	
	// Information content variables
	double infoConMean = 9;				// Average value of target information content for names
	double infoConStdev = 2;			// Standard deviation of target information content for names
	double suffixInfoConMean = 0;		// Avg. value of target information content for suffixes
	double suffixInfoConStdev = 5;		// Standard deviation of target information content for suffixes

	
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
		nlNode 		= new NucleusLocationNode();
		inNode		= new InitialNucleusNode();
		mnNode		= new MedialNucleusNode();
		ilNode		= new InterludeNode();
		tsNode		= new TerminalSyllableNode();
		ctNode		= new ConsonantTerminationNode();
		vtNode		= new VowelTerminationNode();
		rnNode		= new RootNucleusNode();
	}
	
	/**
	 * @return	A complete name generated according to this NameAssembly's flowchart
	 * @since	1.2
	 */
	public Name makeName()
	{
		return makeName(rng.nextGaussian() * infoConStdev + infoConMean, WordType.STEM);
	}
	
	/**
	 * A suffix generated according to this NameAssembly's flowchart, meant to be attached to
	 * to a Name ending in a root nucleus to form a complete Name.
	 * 
	 * @return	A suffix generated according to this NameAssembly's flowchart
	 * @since	1.2	
	 */
	public Name makeSuffix()
	{
		return makeName(rng.nextGaussian() * suffixInfoConStdev + suffixInfoConMean, WordType.SUFFIX);
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
	protected Name makeName(double icTarget, WordType type)
	{
		// Initialize naming variables
		this.icTarget = icTarget;
		name = new Name(type);
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
			else if (rng.nextDouble() < ((VowelPhoneme)prev.lastPhoneme()).strongRootEndChance)
				name.strength = RootStrength.STRONG;
		}
		else if (name.getWordType() == WordType.SUFFIX)
		{
			Constituent first = name.getSyllables().get(0).constituents[1];
			
			// Suffixes comprising only a single nucleus are always strong
			if (name.getSyllables().size() == 1 && name.getSyllables().get(0).constituents[2] == null)
				name.strength = RootStrength.STRONG;
			// Diphthongs are always strong
			else if (first.size() > 1)
				name.strength = RootStrength.STRONG;
			else if (rng.nextDouble() < ((VowelPhoneme)first.lastPhoneme()).strongSuffixStartChance)
				name.strength = RootStrength.STRONG;
		}
		
		return name;
	}
	
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
			emptyOnsetChance = Math.log(p.initialNuclei.size());
			emptyOnsetChance *= p.baseEmptyInitialOnsetChance;
			
			// Scaling each prominence value by the log of the number of possibilities it represents
			// helps ensure that rich inventories are well represented while poor ones are sampled
			// less frequently. This reduces the chances of something like a language with only 1
			// onset cluster having that cluster at the start of 20% of names.
			// This technique is repeated abundantly throughout the Node classes.

			// Onset onset chance
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
		private double initialSyllableEntropy;			// entropy for an initial, non-final syllable
		private double medialSyllableEntropy;			// entropy for a medial syllable starting with a consonant
		private double terminalSyllableEntropy;			// entropy for a final syllable starting with a consonant
		private double rootSyllableEntropy;				// entropy for a root-ending syllable starting w/ a consonant
		
		/**
		 * Constructor measures and stores entropy for different types of initial, medial, and terminal syllables.
		 */
		public NucleusLocationNode()
		{
			// Obtain some general entropy measurements
			double hOnset = p.medialOnsets.getEntropy();
			double hCoda = 0;
			int compoundInterludeCount = 0;
			if (p.medialCodas != null)
			{
				hCoda = p.medialCodas.getCompoundEntropy();
				compoundInterludeCount = p.medialCodas.getCompoundSize();
			}
			
			// This syllable's interlude's entropy for each vowel the current syllable's nucleus might end in 
			double[] interludeEntropies = new double[p.vowelInventory.length];
			
			// Log of number of interludes available for each vowel
			double[] nucleusWeights = new double[p.vowelInventory.length];
			
			// Set interlude entropies for every vowel. These represent the entropy of adding (or not adding)
			// a medial coda and/or onset after a known vowel.
			for (int i = 0; i < p.vowelInventory.length; i++)
			{
				if (!p.vowelInventory[i].segment.expression.equals(":"))
				{
					ConstituentLibrary nuclei = p.vowelInventory[i].followers;
					
					// Get decision entropy of onset vs hiatus
					double pOnset = Math.log(p.medialOnsets.size() + 1) * p.baseMedialOnsetChance;
					double hHiatus = nuclei.getEntropy(); 
					double pHiatus = Math.log(nuclei.size() + 1) * (1 - p.baseMedialOnsetChance);
					
					// Normalize probabilities
					double sum = pHiatus + pOnset;
					pOnset /= sum;
					pHiatus /= sum;
					
					// Get decision entropy of coda vs no coda
					double hNoCoda = decisionEntropy(
										new double[] { pOnset, pHiatus }, 
										new double[] { hOnset, hHiatus });
					double pNoCoda = Math.log(p.medialOnsets.size() + nuclei.size() + 1) * 
										(1 - p.baseMedialCodaChance);
					double pCoda = Math.log(compoundInterludeCount + 1) * p.baseMedialCodaChance;
					
					// Normalize
					sum = pNoCoda + pCoda;
					pNoCoda /= sum;
					pCoda /= sum;
					
					// Get overall interlude entropy/probability
					double hInterlude = decisionEntropy(
											new double[] { pCoda, pNoCoda }, 
											new double[] { hCoda, hNoCoda });
					double pInterlude = Math.log(p.medialOnsets.size() + nuclei.size() + compoundInterludeCount + 1);
					
					interludeEntropies[i] = hInterlude;
					nucleusWeights[i] = pInterlude;
				}
			}
			
			// Set each syllable entropy. This represents the entropy for generating an entire syllable from
			// nucleus to onset, with each value representing a different set of possible nuclei (and therefore
			// a different set of hiatus. Because this value depends on the interlude entropy for each vowel,
			// which is the same regardless of what Constituent that vowel is in, we pass the interlude
			// entropy along for each vowel to avoid recalculating it for every diphthong that might include it.
			initialSyllableEntropy = getMedialEntropy(p.initialNuclei, interludeEntropies, nucleusWeights);
			medialSyllableEntropy = getMedialEntropy(p.medialNuclei, interludeEntropies, nucleusWeights);
			rootSyllableEntropy = p.rootNuclei.getEntropy();
			
			double consonantTerminationEntropy, vowelTerminationEntropy;
			double pConsonantTermination;
			
			if (p.terminalCodas == null)
			{
				consonantTerminationEntropy = 0;
				pConsonantTermination = 0;
			}
			else
			{
				consonantTerminationEntropy = p.medialNuclei.getEntropy() + p.terminalCodas.getEntropy();
				pConsonantTermination = getConsonantTerminationChance(p.medialNuclei, p.terminalNuclei);
			}
			
			vowelTerminationEntropy = p.terminalNuclei.getEntropy();
			terminalSyllableEntropy = decisionEntropy(
										new double[] { pConsonantTermination, 1 - pConsonantTermination },
										new double[] { consonantTerminationEntropy, vowelTerminationEntropy });
			
			System.out.println("General syllable entropies");
			System.out.printf("\t%.3f Initial syllable\n", initialSyllableEntropy);
			System.out.printf("\t%.3f Medial syllable\n", medialSyllableEntropy);
			System.out.printf("\t%.3f Terminal syllable with coda\n", consonantTerminationEntropy);
			System.out.printf("\t%.3f Terminal syllable, no coda\n", vowelTerminationEntropy);
			System.out.printf("\t%.3f Root syllable\n", rootSyllableEntropy);
			
			// As above, but for hiatus-based entropy values
			for (VowelPhoneme v : p.vowelInventory)
			{
				if (!v.segment.expression.equals(":"))
				{
					v.hiatusMedialSyllableEntropy = getMedialEntropy(v.followers, interludeEntropies, 
																		nucleusWeights);
					v.hiatusRootSyllableEntropy = v.rootFollowers.getEntropy();
					
					consonantTerminationEntropy = vowelTerminationEntropy = 0;
					if (p.terminalCodas != null)
					{
						consonantTerminationEntropy = v.followers.getEntropy() + p.terminalCodas.getEntropy();
					}
					
					vowelTerminationEntropy = v.terminalFollowers.getEntropy();
					v.hiatusTerminalCodaChance = getConsonantTerminationChance(v.followers, v.terminalFollowers);
					v.hiatusTerminalSyllableEntropy = decisionEntropy(
										new double[] { v.hiatusTerminalCodaChance, 1 - v.hiatusTerminalCodaChance },
										new double[] { consonantTerminationEntropy, vowelTerminationEntropy });
					
					System.out.println("Entropies for hiatus on " + v + ":");
					System.out.printf("\t%.3f Medial syllable\n", v.hiatusMedialSyllableEntropy);
					System.out.printf("\t%.3f Terminal syllable\n", v.hiatusTerminalSyllableEntropy);
					System.out.printf("\t%.3f Root syllable\n", v.hiatusRootSyllableEntropy);
				}
			}
		}
		
		/**
		 * Decides whether to add a root nucleus (for regular names), a consonant or vowel termination (for
		 * suffixes), or to add a medial syllable and revisit the decision again after.
		 */
		public Node nextNode()
		{			
			// For suffixes
			if (name.getWordType() == WordType.SUFFIX)
			{
				double hMedial, hTerminal;
				
				// Get entropy measurements
				if (prev != null && prev.type == ConstituentType.NUCLEUS)
				{
					VowelPhoneme v = ((VowelPhoneme) prev.lastPhoneme());
					hMedial = v.hiatusMedialSyllableEntropy;
					hTerminal = v.hiatusTerminalSyllableEntropy;
				}
				else
				{
					hMedial = medialSyllableEntropy;
					hTerminal = terminalSyllableEntropy;
				}
				
				System.out.printf("Entropy: %.3f | %.3f\n", hMedial, hTerminal);
				
				double currentIC = -Math.log(pName);
				double hMedialDiff = Math.abs(hMedial + currentIC - icTarget);
				double hTerminalDiff = Math.abs(hTerminal + currentIC - icTarget);
				
				// Use entropy measurements to decide what kind of syllable will bring us closest to the target
				return (hTerminal < hMedialDiff || currentIC > icTarget) ? tsNode : mnNode;
			}
			
			// For in-progress names ending in roots
			else
			{
				if (name.getSyllables().size() == 0)
					return inNode;
				
				double hMedial, hRoot;

				// Get entropy measurements
				if (prev != null && prev.type == ConstituentType.NUCLEUS)
				{
					VowelPhoneme v = ((VowelPhoneme) prev.lastPhoneme());
					hMedial = v.hiatusMedialSyllableEntropy;
					hRoot = v.hiatusRootSyllableEntropy;
				}
				else
				{
					hMedial = medialSyllableEntropy;
					hRoot = rootSyllableEntropy;
				}
				
				double hMedialDiff = Math.abs(hMedial + -Math.log(pName) - icTarget);
				double hRootDiff = Math.abs(hRoot + -Math.log(pName) - icTarget);
				
				// Use entropy measurements to decide what kind of syllable will bring us closest to the target
				if (hRootDiff < hMedialDiff)
				{
					// If we're adding a root but the previous nucleus doesn't can't undergo hiatus with
					// any of the root nuclei, add a simple onset
					if (hRoot == 0)
					{
						Constituent next = p.medialOnsets.pickSimple();
						pName *= next.probability;
						addConstituent(next);
					}
					return rnNode;
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
			if (nuclei.size() == 0)
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
									* curr.probability * nuclei.getLengthProbability(curr.size());
							sum += nucleusProbabilities[index];
							break;
						}
					index++;
				}
		
			// Normalize probabilities
			for (int i = 0; i < nucleusProbabilities.length; i++)
				nucleusProbabilities[i] /= sum;
			
			return decisionEntropy(nucleusProbabilities, nucleusEntropies);
		}

		private double getConsonantTerminationChance(ConstituentLibrary medialLib, ConstituentLibrary terminalLib)
		{
			if (p.terminalCodas == null)
				return 0;
			double pConsonantTermination, pVowelTermination;
			pConsonantTermination = Math.log(medialLib.size() * p.terminalCodas.size() + 1);
			pVowelTermination = Math.log(terminalLib.size() + 1);
			pVowelTermination *= 1 - p.baseTerminalCodaChance;
			return pConsonantTermination / (pConsonantTermination + pVowelTermination);
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
			{
				addConstituentFrom(prev.followers());
			}
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
					chance += c.probability * p.medialNuclei.getLengthProbability(c.size()) * nonemptyCodaChance;
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
				vFinal = ((VowelPhoneme)prev.lastPhoneme()).terminalFollowers.size();
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
			if (name.getWordType() == WordType.STEM)
				name.setWordType(WordType.COMPLETE);
						
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
				addConstituentFrom(((VowelPhoneme) prev.lastPhoneme()).terminalFollowers);
			else
				addConstituentFrom(p.terminalNuclei);
			
			// Names has completed instead of ending in a root
			if (name.getWordType() == WordType.STEM)
				name.setWordType(WordType.COMPLETE);
			
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
				addConstituentFrom(((VowelPhoneme)prev.lastPhoneme()).rootFollowers);
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
		Constituent next = lib.pick();
		pName *= next.probability;
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
		name.add(c);
	}
	
	/**
	 * Determines the entropy of an event where one subsequent event is chosen at random from a list of events
	 * with known probabilities and entropies. 
	 * 
	 * @param probabilities	The probabilities for an array of events
	 * @param entropies		The corresponding entropy measurements for an array of events
	 * @return				The entropy measurement for the decision between subsequent events
	 * @since				1.1
	 */
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