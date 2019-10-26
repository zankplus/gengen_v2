package gengenv2;

import java.util.ArrayList;
import java.util.Random;

public class PublicRandom
{
	private static ArrayList<Random> rngs = new ArrayList<Random>();
	
	
	
	// Adds a new RNG to the list and returns its index
	public static int newRNG()
	{
		return newRNG(new Random().nextLong());
	}
	
	// Adds a new RNG with the given seen to the list and returns its index
	public static int newRNG(long seed)
	{
		rngs.add(new Random(seed));
		return rngs.size() - 1;
	}
	
	// Returns the RNG at the given index
	public static Random getRNG(int index)
	{
		if (rngs.size() == 0)
			index = newRNG();
		return rngs.get(index);
	}
	
	// Returns the default RNG (index 0)
	public static Random getRNG()
	{
		return getRNG(0);
	}
}
