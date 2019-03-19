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
import gengenv2.Phonology.Phoneme;
import gengenv2.Phonology.VowelPhoneme;

/**
 * A sort of flowchart or state machine for generating names according to a Phonology's inventory, phonotactics, 
 * and stress system. Though the flowchart is structurally the same regardless of the anguage, its weights depend
 * on its Phonology's features, so every Phonology needs its own custom copy.
 * 
 * @author	Clayton Cooper
 * @version	1.2
 * @since	1.0
 */
class SimpleAssembly extends NameAssembly
{
	// Flowchart nodes
	private InitialOnsetNode ioNode;
	private SuffixStartNode ssNode;
	private NucleusLocationNode nlNode;
	private InitialNucleusNode inNode;
	private MedialNucleusNode mnNode;
	private InterludeNode ilNode;
	private ConsonantTerminationNode ctNode;
	private VowelTerminationNode vtNode;
	private RootNucleusNode rnNode;
	
	// Current name variables
	Name name;				// The name currently being generated
	double icTarget;		// Intended information content of the current name
	double pName;			// Probability of generating the current name
	Constituent prev;		// The most recent syllable constituent added to the name
	
	// Information content variables
	double infoConMean = 9;	// Average value of target information content
	double infoConStdev = 2;	// Standard deviation of target information content
	double suffixInfoConMean = 3;
	double suffixInfoConStdev = 1;
	EntropyStats entropyStats;	// Collection of entropy values for different flowchart nodes
	
	/**
	 * Constructor simply initializes all Nodes in the assembly flowchart, and saves the reference to the
	 * given Phonology as well as its RNG.
	 * 
	 * @param	p	The Phonology to which this NameAssembly belongs	
	 */
	public SimpleAssembly(Phonology p)
	{
		this.p = p;
		rng = p.rng;
	
		ioNode 		= new InitialOnsetNode();
		ssNode		= new SuffixStartNode();
		nlNode 		= new NucleusLocationNode();
		inNode		= new InitialNucleusNode();
		mnNode		= new MedialNucleusNode();
		ilNode		= new InterludeNode();
		ctNode		= new ConsonantTerminationNode();
		vtNode		= new VowelTerminationNode();
		rnNode		= new RootNucleusNode();
		entropyStats = new EntropyStats();
	}
	
	public Name makeName()
	{
		return makeName(rng.nextGaussian() * infoConStdev + infoConMean, RootStrength.WEAK);
	}
	
	public Name makeSuffix()
	{
		return makeName(rng.nextGaussian() * suffixInfoConStdev + suffixInfoConMean, RootStrength.CLOSED);
	}
	
	/**
	 * Generates a name by first resetting the naming variables and then invoking the StartNode. This initiates
	 * a decision process that propagates through all the Nodes in the flowchart, each of which may add a
	 * SyllableSegment (or two, for interludes) to the name list
	 * @return	The completed name
	 * @since	1.0
	 */
	protected Name makeName(double icTarget, RootStrength rootStrength)
	{
		// Initialize naming variables
		this.icTarget = icTarget;
		name = new Name(p);
		name.setRootStrength(rootStrength);
		prev = null;
		pName = 1;
		
		// Propagate through the flowchart until one of the nodes returns null
		Node node;
		if (rootStrength == RootStrength.CLOSED)
			node = ssNode; 
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
	 * The first node called by makeName(). Its job is to reset the naming variables and pick an initial onset before
	 * advancing to the SyllableLocationNode.
	 * 
	 * @since 1.0
	 */
	private class InitialOnsetNode extends Node
	{
		public double emptyOnsetChance;
		
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
		
		public Node nextNode()
		{	
			double rand = p.rng.nextDouble();
			
			// Option 1: Empty onset
			if (rand < emptyOnsetChance)
				pName *= emptyOnsetChance;

			// Option 2: Nonempty onset
			else
			{
				Constituent next = p.initialOnsets.pickSimple();
				pName *= 1 - emptyOnsetChance;
				pName *= next.probability * p.initialOnsets.getLengthProbability(next.size());
				addConstituent(next);
			}
			
			return nlNode;
		}
	}
	
	private class SuffixStartNode extends Node
	{
		double emptyOnsetChance;
		
		public SuffixStartNode()
		{
			emptyOnsetChance = Math.log(p.medialNuclei.size() + 1);
			emptyOnsetChance *= 1 - p.baseSuffixOnsetChance;
			double nonemptyOnsetChance = Math.log(p.medialOnsets.size() + 1);
			nonemptyOnsetChance *= p.baseSuffixOnsetChance;
			
			// Normalize
			emptyOnsetChance /= emptyOnsetChance + nonemptyOnsetChance;
			
			System.out.printf("Suffix empty onset chance: %.3f \n", emptyOnsetChance);
		}
		
		public Node nextNode()
		{
			if (rng.nextDouble() < emptyOnsetChance)
			{
				pName *= emptyOnsetChance;
			}
			else
			{
				Constituent next = p.medialOnsets.pick();
				pName *= 1 - emptyOnsetChance;
				pName *= next.probability;
				pName *= p.medialOnsets.getLengthProbability(next.size());
				addConstituent(next);
			}
			
			return nlNode;
		}
	}
	
	/**
	 * Decides what sequence of steps should follow, depending on the location of the next nucleus.
	 * @since	1.0
	 */
	private class NucleusLocationNode extends Node
	{
		private double initialSyllableEntropy;			// entropy for an initial, non-final syllable
		private double medialSyllableEntropy;			// entropy for a medial syllable starting with a consonant
		private double consonantTerminationEntropy;
		private double vowelTerminationEntropy;
		private double rootSyllableEntropy;
		
		private double hOnset;
		private double hCoda = 0;
		private int compoundInterludeCount = 0;
		
		public NucleusLocationNode()
		{
			hOnset = p.medialOnsets.getEntropy();
			if (p.medialCodas != null)
			{
				hCoda = p.medialCodas.getCompoundEntropy();
				compoundInterludeCount = p.medialCodas.getCompoundSize();
			}
			
			setSyllableEntropies();
		}
		
		public Node nextNode()
		{			
//			if (name.getSyllables().size() == 0)
//				return inNode;
//			else
			{
				if (name.getRootStrength() == RootStrength.CLOSED)
				{
					double hMedial, hTerminalCoda, hTerminalNoCoda;
					int vowelTerminationSize;
					
					if (prev != null && prev.type == ConstituentType.NUCLEUS)
					{
						VowelPhoneme v = ((VowelPhoneme) prev.lastPhoneme());
						hMedial = v.hiatusMedialSyllableEntropy;
						hTerminalCoda = v.hiatusConsonantTerminationEntropy;
						hTerminalNoCoda = v.hiatusVowelTerminationEntropy;
						vowelTerminationSize = v.terminalFollowers.size();
					}
					else
					{
						hMedial = medialSyllableEntropy;
						hTerminalCoda = consonantTerminationEntropy;
						hTerminalNoCoda = vowelTerminationEntropy;
						vowelTerminationSize = p.terminalNuclei.size();
					}
					
					double hMedialDiff = Math.abs(hMedial + -Math.log(pName) - icTarget);
					double hTerminalCodaDiff = Math.abs(hTerminalCoda + -Math.log(pName) - icTarget);
					double hTerminalNoCodaDiff = Math.abs(hTerminalNoCoda + -Math.log(pName) - icTarget);
					
//					System.out.printf("%.3f vs %.3f | %.3f | %.3f\n", icTarget, hMedialDiff, 
//										hTerminalCodaDiff, hTerminalNoCodaDiff);
					
					if (p.terminalCodas != null && p.terminalCodas.size() > 0 && 
							hTerminalCodaDiff < hTerminalNoCodaDiff && hTerminalCodaDiff < hMedialDiff)
						return ctNode;
					else if (vowelTerminationSize > 0 && hTerminalCodaDiff < hMedialDiff)
						return vtNode;
					else
						return mnNode;
				}
				
				// For monosyllables & roots
				else
				{
					double hMedial, hRoot;
					
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
		}

		/**
		 * Measures the entropy for different types of initial and medial syllables.
		 * @since	1.2 
		 */
		private void setSyllableEntropies()
		{
			// This syllable's interlude's entropy for each vowel the current syllable's nucleus might end in 
			double[] interludeEntropies = new double[p.vowelInventory.length];
			double[] interludeProbabilities = new double[p.vowelInventory.length];
			
			// Set interlude entropies for every vowel. These represent the entropy of adding (or not adding)
			// a medial coda and/or onset after a known vowel.
			for (int i = 0; i < p.vowelInventory.length; i++)
			{
				if (!p.vowelInventory[i].segment.expression.equals(":"))
				{
					ConstituentLibrary nuclei = p.vowelInventory[i].followers;
					System.out.print(p.vowelInventory[i].segment.expression + "\t" );
					
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
					
					System.out.print(hCoda + " " + hNoCoda);
					System.out.print(" " + pCoda + " " + pNoCoda);
					// Get overall interlude entropy/probability
					double hInterlude = decisionEntropy(
											new double[] { pCoda, pNoCoda }, 
											new double[] { hCoda, hNoCoda });
					System.out.println(" " + hInterlude);
					double pInterlude = Math.log(p.medialOnsets.size() + nuclei.size() + compoundInterludeCount + 1);
					
					interludeEntropies[i] = hInterlude;
					interludeProbabilities[i] = pInterlude;
				}
			}
			
			// Set each syllable entropy. This represents the entropy for generating an entire syllable from
			// nucleus to onset, with each value representing a different set of possible nuclei (and therefore
			// a different set of hiatus. Because this value depends on the interlude entropy for each vowel,
			// which is the same regardless of what Constituent that vowel is in, we pass the interlude
			// entropy along for each vowel to avoid recalculating it for every diphthong that might include it.
			initialSyllableEntropy = getMedialEntropy(p.initialNuclei, interludeEntropies, interludeProbabilities);
			medialSyllableEntropy = getMedialEntropy(p.medialNuclei, interludeEntropies, interludeProbabilities);
			if (p.terminalCodas == null)
				consonantTerminationEntropy = 0;
			else
				consonantTerminationEntropy = p.medialNuclei.getEntropy() + p.terminalCodas.getEntropy();
			vowelTerminationEntropy = p.terminalNuclei.getEntropy();
			rootSyllableEntropy = p.rootNuclei.getEntropy();
			
			System.out.println("General syllable entropies");
			System.out.printf("\t%.3f Initial syllable\n", initialSyllableEntropy);
			System.out.printf("\t%.3f Medial syllable\n", medialSyllableEntropy);
			System.out.printf("\t%.3f Terminal syllable with coda\n", consonantTerminationEntropy);
			System.out.printf("\t%.3f Terminal syllable, no coda\n", vowelTerminationEntropy);
			System.out.printf("\t%.3f Root syllable\n", rootSyllableEntropy);
			
			for (VowelPhoneme v : p.vowelInventory)
			{
				if (!v.segment.expression.equals(":"))
				{
					v.hiatusMedialSyllableEntropy = getMedialEntropy(v.followers, interludeEntropies, 
																		interludeProbabilities);
					
					if (p.terminalCodas != null)
						v.hiatusConsonantTerminationEntropy = v.followers.getEntropy() + p.terminalCodas.getEntropy();
					v.hiatusVowelTerminationEntropy = v.terminalFollowers.getEntropy();
					v.hiatusRootSyllableEntropy = v.rootFollowers.getEntropy();
					
					System.out.println("Entropies for hiatus on " + v + ":");
					System.out.printf("\t%.3f Medial syllable\n", v.hiatusMedialSyllableEntropy);
					System.out.printf("\t%.3f Terminal syllable with coda\n", v.hiatusConsonantTerminationEntropy);
					System.out.printf("\t%.3f Terminal syllable, no coda\n", v.hiatusVowelTerminationEntropy);
					System.out.printf("\t%.3f Root syllable\n", v.hiatusRootSyllableEntropy);
				}
			}
		}

		private double getMedialEntropy(ConstituentLibrary nuclei, double[] interludeEntropies,
										double[] interludeProbabilities)
		{
			if (nuclei.size() == 0)
				return 0;
			
			double[] nucleusEntropies = new double[nuclei.size()];
			double[] nucleusProbabilities = new double[nuclei.size()];
			double sum = 0;
			
			int index = 0;
			for (int i = 1; i <= nuclei.maxLength(); i++)
				for (Constituent curr : nuclei.getMembersOfLength(i))
				{
					Phoneme ph = curr.lastPhoneme();
					for (int j = 0; j < p.vowelInventory.length; j++)
						if (ph == p.vowelInventory[j])
						{
							nucleusEntropies[index] = interludeEntropies[j];
							nucleusProbabilities[index] = interludeProbabilities[j]
									* curr.probability * nuclei.getLengthProbability(curr.size());
							sum += nucleusProbabilities[index];
							break;
						}
					index++;
				}
		
			// Normalize probabilities
			for (int i = 0; i < nucleusProbabilities.length; i++)
				nucleusProbabilities[i] /= sum;
			
			double result = decisionEntropy(nucleusProbabilities, nucleusEntropies);
			
			return result;
		}

	}
	
	private class InitialNucleusNode extends Node
	{
		Node nextNode()
		{
			Constituent next = p.initialNuclei.pick();
			pName *= next.probability;
			pName *= p.initialNuclei.getLengthProbability(next.size());
			addConstituent(next);
			
			return ilNode;
		}
	}
	
	private class MedialNucleusNode extends Node
	{
		Node nextNode()
		{
			Constituent next;
			
			// Add nucleus
			if (prev != null && prev.type == ConstituentType.NUCLEUS)
			{
				next = prev.followers().pick();
				pName *= prev.followers().getLengthProbability(next.size());
			}
			else
			{
				next = p.medialNuclei.pick();
				pName *= p.medialNuclei.getLengthProbability(next.size());
			}
			
			pName *= next.probability;
			addConstituent(next);
			return ilNode;
		}
	}
	
	private class InterludeNode extends Node 
	{
		double nonemptyCodaWeight;
		private double nonemptyOnsetWeight;
		
		public InterludeNode()
		{
			nonemptyCodaWeight = (p.medialCodas == null) ? 0 : Math.log(p.medialCodas.getCompoundSize() + 1);
			nonemptyCodaWeight *= p.baseMedialCodaChance;
			
			nonemptyOnsetWeight = Math.log(p.medialOnsets.size() + 1);
			nonemptyOnsetWeight *= p.baseMedialOnsetChance;
			System.out.println("Average medial coda chance: " + getAverageCodaChance());
		}
		
		Node nextNode()
		{
			addCoda();
			addOnset();
			return nlNode;
		}
		
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
				next = p.medialCodas.pick();
				pName *= 1 - emptyCodaChance;
				pName *= next.probability;
				pName *= p.medialCodas.getLengthProbability(next.size());
				addConstituent(next);
			}
		}
		
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
					Constituent next = p.medialOnsets.pick();
					pName *= 1 - emptyOnsetChance;
					pName *= next.probability;
					pName *= p.medialOnsets.getLengthProbability(next.size());
					addConstituent(next);
				}
			}
			
			// Onset following a coda: add an onset from the coda's follower list
			else
			{
				Constituent next = prev.followers().pick();
				pName *= next.probability;
				pName *= prev.followers().getLengthProbability(next.size());
				addConstituent(next);
			}
		}
		
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
					System.out.println(c + " " + emptyCodaChance);
				}
			}
			System.out.println(nonemptyCodaWeight + ": " + p.medialCodas.getCompoundSize() + " " + p.baseMedialCodaChance);
			return chance;
		}
	}
	
	private class ConsonantTerminationNode extends Node
	{	
		Node nextNode()
		{
			addNucleus();
			addCoda(); 
			return null;
		}
		
		private void addNucleus()
		{
			Constituent next;
			
			// Add nucleus
			if (prev != null && prev.type == ConstituentType.NUCLEUS)
			{
				next = prev.followers().pick();
				pName *= prev.followers().getLengthProbability(next.size());
			}
			else
			{
				next = p.terminalNuclei.pick();
				pName *= p.terminalNuclei.getLengthProbability(next.size());
			}
			
			pName *= next.probability;
			addConstituent(next);
		}
		
		private void addCoda()
		{
			if (p.terminalCodas != null)
			{
				Constituent next = p.terminalCodas.pick();
				pName *= next.probability;
				pName *= p.terminalCodas.getLengthProbability(next.size());
				addConstituent(next);
			}
		}
	}
	
	private class VowelTerminationNode extends Node
	{
		Node nextNode()
		{
			Constituent next;
			if (prev != null && prev.type == ConstituentType.NUCLEUS)
			{
				ConstituentLibrary lib = ((VowelPhoneme) prev.lastPhoneme()).terminalFollowers; 
				next = lib.pick();
				pName *= next.probability;
				pName *= lib.getLengthProbability(next.size());
				
			}
			else
			{
				next = p.terminalNuclei.pick();
				pName *= next.probability;
				pName *= p.terminalNuclei.getLengthProbability(next.size());
			}
			
			addConstituent(next);
			
			return null;
		}
	}
	
	private class RootNucleusNode extends Node
	{
		Node nextNode()
		{
			if (prev.type == ConstituentType.NUCLEUS)
			{
				ConstituentLibrary lib = ((VowelPhoneme)prev.lastPhoneme()).rootFollowers;
				Constituent next = lib.pick();
				pName *= next.probability * lib.getLengthProbability(next.size());
				addConstituent(next);
			}
			else
			{
				Constituent next = p.rootNuclei.pick();
				pName *= next.probability * p.rootNuclei.getLengthProbability(next.size());
				addConstituent(next);
			}
			
			return null;
		}
	}
	
	/**
	 * Adds a Constituent to the end of the current Name, while updating the preference to the
	 * previous Constituent.
	 * @param c	The Constituent to append
	 */
	private void addConstituent(Constituent c)
	{
		prev = c;
		name.add(c);
	}
	
	/**
	 * Compiles and stores a collection of entropy measurements for each Node. Useful for predicting the
	 * information content of various Nodes and Constituents.
	 * @since	1.1
	 */
	private class EntropyStats
	{
		public double medialRimeH;		// Entropy of the MedialRimeNode
		public double terminalRimeH;	// Entropy of the TerminalRimeNode

		public EntropyStats()
		{
			// Fundamental counts
			double hMedialOnset = 0;
			double hInitialOnset = 0;
			double hMedialNucleus = 0;
			double hTerminalCoda = 0;
			double hCompoundInterlude = 0;
			
			// Onset entropy
			hMedialOnset = p.medialOnsets.getEntropy();
			hInitialOnset = p.initialOnsets.getEntropy();
			hMedialNucleus = p.medialNuclei.getEntropy();
			
			if (p.terminalCodas != null)
				hTerminalCoda = p.terminalCodas.getEntropy();
			
			if (p.medialCodas != null)
				hCompoundInterlude = p.medialCodas.getCompoundEntropy();
			
			// Complex entropies
			double initialOnsetH = decisionEntropy(
					new double[]{ ioNode.emptyOnsetChance, 1 - ioNode.emptyOnsetChance },
					new double[]{ hMedialNucleus, hInitialOnset });
			
			
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
	
	/**
	 * @param	The probability of a certain outcome
	 * @return	The information content of that outcome
	 */
	private double scaledInfo(double p)
	{
		if (p == 0)
			return 0;
		return -p * Math.log(p);
	}
	
	/**
	 * Determines the entropy of an event where one subsequent event is chosen at random from a list of events
	 * with known probabilities and entropies. 
	 * 
	 * @param probabilities	The probabilities for an array of events
	 * @param entropies		The corresponding entropy measurements for an array of events
	 * @return				The entropy measurement for the decision between subsequent events
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