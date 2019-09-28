package gengenv2;

import java.util.ArrayList;
import java.util.Random;

public class VowelTest
{
	
	
	public static void main (String[] args)
	{
		VowelSystem vs = new VowelSystem();
		System.out.println();
		System.out.println(vs);
		vs.printStructure();
	}
	
	
	
}

enum VowelStructure
{
	MONOVOCALIC	("Monovocalic",	false,
				 new Position(VowelHeight.MID,		VowelBackness.CENTRAL)),
	
	VERTICAL	("Vertical", true,
			 	 new Position(VowelHeight.CLOSE,	VowelBackness.CENTRAL),
			 	 new Position(VowelHeight.OPEN, 	VowelBackness.CENTRAL)),
	
	TRIANGULAR	("Triangular", true,
				 new Position(VowelHeight.CLOSE,	VowelBackness.FRONT),
				 new Position(VowelHeight.CLOSE,	VowelBackness.BACK),
				 new Position(VowelHeight.OPEN,		VowelBackness.CENTRAL)),
	
	RECTANGULAR ("Rectangular", true,
			 	 new Position(VowelHeight.CLOSE,	VowelBackness.FRONT),
			 	 new Position(VowelHeight.CLOSE,	VowelBackness.BACK),
			 	 new Position(VowelHeight.OPEN,		VowelBackness.FRONT),
			 	 new Position(VowelHeight.OPEN,		VowelBackness.BACK)),
	
	V_SHAPED	("Conical", false,
				 new Position(VowelHeight.CLOSE,	VowelBackness.FRONT),
				 new Position(VowelHeight.CLOSE,	VowelBackness.BACK),
				 new Position(VowelHeight.MID,		VowelBackness.FRONT),
				 new Position(VowelHeight.MID,		VowelBackness.BACK),
				 new Position(VowelHeight.OPEN,		VowelBackness.CENTRAL)),
	
	TERNARY		("Ternary", false,
			 	 new Position(VowelHeight.CLOSE,	VowelBackness.FRONT),
			 	 new Position(VowelHeight.CLOSE,	VowelBackness.CENTRAL),
			 	 new Position(VowelHeight.CLOSE,	VowelBackness.BACK),
			 	 new Position(VowelHeight.MID,		VowelBackness.FRONT),
			 	 new Position(VowelHeight.MID,		VowelBackness.BACK),
			 	 new Position(VowelHeight.OPEN,		VowelBackness.CENTRAL));
	
	String name;
	boolean neutralAllowed;
	Position[] positions;
	
	VowelStructure (String name, boolean neutralAllowed,	Position... positions)
	{
		this.name = name;
		this.neutralAllowed = neutralAllowed;
		this.positions = positions;
	}
}

class Position
{
	VowelHeight height;
	VowelBackness backness;
	static Random rng;
	
	public Position (VowelHeight height, VowelBackness backness)
	{
		this.height = height;
		this.backness = backness;
	}
	
	// Evaluates the suitability of a vowel for a given position in the vowel structure.
	// 2 represents a perfect match. Imperfect matches are represented by substracting a small random quantity.
	// 1 represents an unsatisfactory match, and anything less than 1 is especially nonsensical.
	public double evaluateVowel(TestVowel v)
	{
		double result = 4 - Math.abs(height.ordinal() - v.height.ordinal());
		result -= Math.abs(backness.ordinal() - v.backness.ordinal());

		if (result == 4)
			return 3 + rng.nextDouble() * 0.1;
		else
			return result;
	}
	
	public String toString()
	{
		String result = height + " " + backness;
		return result;
	}
	
	public String shortForm()
	{
		return height.toString().substring(0, 2) + "-" + backness.toString().substring(0, 2);
	}
}

class VowelSystem
{
	static TestVowel[] vowels = new TestVowel[] {
			new TestVowel('A', 1.000, VowelHeight.OPEN,		VowelBackness.CENTRAL),
			new TestVowel('E', 0.865, VowelHeight.MID,		VowelBackness.FRONT),
			new TestVowel('I', 0.969, VowelHeight.CLOSE,	VowelBackness.FRONT),
			new TestVowel('O', 0.906, VowelHeight.MID,		VowelBackness.BACK),
			new TestVowel('U', 0.969, VowelHeight.CLOSE,	VowelBackness.BACK),
			new TestVowel('Y', 0.156, VowelHeight.CLOSE,	VowelBackness.CENTRAL)
	};
	
	Random rng;
	
	TestVowel[] inventory;
	VowelStructure structure;
	int[] mapping;
	double mappingDistance;
	
	public VowelSystem()
	{
		long seed = new Random().nextLong();
		System.out.println("Seed: " + seed);
//		rng = new Random(3110880734803850282L);
		rng = new Random(seed);
		Position.rng = rng;
		
		generateInventory();
		chooseStructure();
	}
	
	public void generateInventory()
	{
		TestVowel[] tempInv = new TestVowel[vowels.length];
		
		// Select vowels
		int count = 0;
		for (int i = 0; i < vowels.length; i++)
			if (rng.nextDouble() < (vowels[i].prominence + 0) / 1)
			{
				tempInv[i] = vowels[i];
				count++;
			}
		
		// If there are no members, prevent an empty inventory by adding 'A'
		if (count == 0)
		{
			tempInv[0] = vowels[0];
			count = 1;
		}
		
		// Copy to correctly sized array
		inventory = new TestVowel[count];
		int index = 0;
		for (TestVowel vowel : tempInv)
			if (vowel != null)
			{
				inventory[index] = vowel;
				index++;
			}
		
		// Debug
//		inventory = new TestVowel[] { vowels[1], vowels[2], vowels[3], vowels[4], vowels[5] };
	}

	public void chooseStructure()
	{
		ArrayList<VowelStructure> options = new ArrayList<VowelStructure>();
		
		// Make a list of all potential structures
		for (VowelStructure vs : VowelStructure.values())
		{
			if (inventory.length == vs.positions.length ||
				(vs.neutralAllowed && inventory.length == vs.positions.length + 1))
				options.add(vs);
		}
		
		mapping = null;
		mappingDistance = 0;
		structure = null;
		
		// Select the most suitable structure
		for (VowelStructure vs : options)
		{
			EvaluationStation es = new EvaluationStation(vs);
			
			if (mapping == null || es.bestMappingDistance > mappingDistance)
			{
				mapping = es.bestMapping;
				mappingDistance = es.bestMappingDistance;
				structure = vs;
			}
		}
		
//		System.out.print("Best structure: " + structure + " (");
//		for (int i = 0; i < mapping.length; i++)
//			System.out.print(mapping[i]);
//		System.out.printf(") - %.3f\n", mappingDistance);
	}
	
	private class EvaluationStation
	{
		double[][] grid;
		int[] bestMapping;
		double bestMappingDistance;
		VowelStructure vs;
		
		public EvaluationStation(VowelStructure vs)
		{
			this.vs = vs;
			
			grid = new double[inventory.length][inventory.length];
			Position p;
			
			for (int i = 0; i < inventory.length; i++)
			{
				if (i < vs.positions.length)
					p = vs.positions[i];
				else
					p = new Position(VowelHeight.MID, VowelBackness.CENTRAL);
				
				for (int j = 0 ; j < inventory.length; j++)	
					grid[i][j] = p.evaluateVowel(inventory[j]);
			}
			
			// Find the best configuration for this structure
			bestMapping = new int[inventory.length];
			bestMappingDistance = 0;
			int[] order = new int[inventory.length];
			
			for (int i = 0; i < order.length; i++)
				order[i] = i;
			
			findBestMapping(order, 0, inventory.length - 1);
		}
		
		public void printGrid()
		{
			// Print evaluation grid
			for (TestVowel v : inventory) System.out.print("\t" + v.id);
			System.out.println();
			
			for (int i = 0; i < grid.length; i++)
			{
				if (i < vs.positions.length)
					System.out.print(vs.positions[i].shortForm());
				else
					System.out.print("MI-CE");
				
				for (int j = 0; j < grid[i].length; j++)
					System.out.printf("\t%.3f", grid[i][j]);
				
				System.out.println(" --> " + inventory[bestMapping[i]].id);
			}
			System.out.println();
		}
		
		private void findBestMapping(int[] order, int i, int n)
		{
			int j;
			if (i == n)
			{
				// Evaluate this mapping
				if (bestMapping == null)
				{
					for (int m = 0; m < order.length; m++)
						bestMapping[m] = order[m];
					bestMappingDistance = getMappingDistance(order);
				}
				else
				{
					double distance = getMappingDistance(order);
					
					if (distance > bestMappingDistance)
					{
						bestMappingDistance = distance;
						for (int m = 0; m < order.length; m++)
							bestMapping[m] = order[m];
					}
				}
			}
			
			else
			{
				// Recurse across other mappings
				for (j = i; j <= n; j++)
				{
					swap(order, i, j);
					findBestMapping(order, i + 1, n);
					swap(order, i, j);
				}
			}
		}
		
		private void swap(int[] arr, int i, int j)
		{
			int temp = arr[i];
			arr[i] = arr[j];
			arr[j] = temp;
		}
		
		private double getMappingDistance(int[] order)
		{
			double distance = 0;
			for (int k = 0; k < grid.length; k++)
				distance += grid[k][order[k]];
			
			return distance;
		}
	}
	
	public void printStructure()
	{
		System.out.println("Structure: " + structure.name);
		System.out.println();
		switch (structure)
		{
			case MONOVOCALIC:
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println("      " + inventory[mapping[0]].id);
				System.out.println();
				System.out.println();
				System.out.println();
				break;
		
			case VERTICAL:
				if (hasNeutralVowel())
				{
					System.out.println();
					System.out.println("      " + inventory[mapping[0]].id);
					System.out.println("      " + "|");
					System.out.println("      " + inventory[mapping[2]].id);
					System.out.println("      " + "|");
					System.out.println("      " + inventory[mapping[1]].id);
					System.out.println();
				}
				else
				{
					System.out.println();
					System.out.println("      " + inventory[mapping[0]].id);
					System.out.println("      " + "|");
					System.out.println("      " + "|");
					System.out.println("      " + "|");
					System.out.println("      " + inventory[mapping[1]].id);
					System.out.println();
				}
				break;
				
			case TRIANGULAR:
				if (hasNeutralVowel())
				{
					System.out.println("   " + inventory[mapping[0]].id + "     " + inventory[mapping[1]].id);
					System.out.println("    \\   /");
					System.out.println("     \\ /");
					System.out.println("      " + inventory[mapping[3]].id);
					System.out.println("      |");
					System.out.println("      |");
					System.out.println("      " + inventory[mapping[2]].id);
				}
				else
				{
					System.out.println();
					System.out.println("  " + inventory[mapping[0]].id + "-------" + inventory[mapping[1]].id);
					System.out.println("   \\     /");
					System.out.println("    \\   /");
					System.out.println("     \\ /");
					System.out.println("      " + inventory[mapping[2]].id);
					System.out.println();
				}
				break;
				
			case RECTANGULAR:
				if (hasNeutralVowel())
				{
					System.out.println("   " + inventory[mapping[0]].id + "-----" + inventory[mapping[1]].id);
					System.out.println("   |\\   /|");
					System.out.println("   | \\ / |");
					System.out.println("   |  " + inventory[mapping[4]].id + "  |");
					System.out.println("   | / \\ |");
					System.out.println("   |/   \\|");
					System.out.println("   " + inventory[mapping[2]].id + "-----" + inventory[mapping[3]].id);
				}
				else
				{
					System.out.println();
					System.out.println("  " + inventory[mapping[0]].id + "-------" + inventory[mapping[1]].id);
					System.out.println("  |       |");
					System.out.println("  |       |");
					System.out.println("  |       |");
					System.out.println("  " + inventory[mapping[2]].id + "-------" + inventory[mapping[3]].id);
					System.out.println();
				}
				break;
				
			case V_SHAPED:
				System.out.println(inventory[mapping[0]].id + "-----------" + inventory[mapping[1]].id);
				System.out.println(" \\         /");
				System.out.println("  \\       / ");
				System.out.println("   " + inventory[mapping[2]].id + "-----" + inventory[mapping[3]].id);
				System.out.println("    \\   /");
				System.out.println("     \\ /");
				System.out.println("      " + inventory[mapping[4]].id);
				break;
				
			case TERNARY:
				System.out.println(inventory[mapping[0]].id + "-----" + inventory[mapping[1]].id + "-----" 
									+ inventory[mapping[2]].id);
				System.out.println(" \\   / \\   /");
				System.out.println("  \\ /   \\ / ");
				System.out.println("   " + inventory[mapping[3]].id + "-----" + inventory[mapping[4]].id);
				System.out.println("    \\   /");
				System.out.println("     \\ /");
				System.out.println("      " + inventory[mapping[5]].id);
				break;
				
			default:
				return;
		}
	}
	
	public boolean hasNeutralVowel()
	{
		return inventory.length > structure.positions.length;
	}
	
	public String toString()
	{
		String result = "Inventory: ";
		for (TestVowel v : inventory)
			result += v.id + " ";
		return result;
	}

}

class TestVowel
{
	char id;
	double prominence;
	VowelHeight height;
	VowelBackness backness;
	
	public TestVowel (char id, double prominence, VowelHeight height, VowelBackness backness)
	{
		this.id = id;
		this.prominence = prominence;
		this.height = height;
		this.backness = backness;
	}
	
	public String toString()
	{
		String result = id + " (" + height + " " + backness + ")";
		return result;
	}
}

enum VowelHeight
{
	OPEN, MID, CLOSE;
}

enum VowelBackness
{
	FRONT, CENTRAL, BACK;
}