package gengenv2;

import java.util.ArrayList;
import java.util.Random;

public class PublicRandom
{
	private static ArrayList<Random> rngs;
	
	// Initializes the RNG list with one Random in it
	public static void initialize()
	{
		rngs = new ArrayList<Random>();
		rngs.add(new Random());
	}
	
	// Adds a new RNG to the list and returns its index
	public static int newRNG()
	{
		return newRNG(new Random().nextLong());
	}
	
	// Adds a new RNG with the given seen to the list and returns its index
	public static int newRNG(long seed)
	{
		PublicRandom.initialize();
		rngs.add(new Random(seed));
		return rngs.size() - 1;
	}
	
	// Returns the RNG at the given index
	public static Random getRNG(int index)
	{
		return rngs.get(index);
	}
	
	// Returns the default RNG (index 0)
	public static Random getRNG()
	{
		return rngs.get(0);
	}
}
