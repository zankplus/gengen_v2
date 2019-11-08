package gengenv2.structures;

public class MorphemeEntry implements Comparable<MorphemeEntry>
{
	private Morpheme morpheme;
	private double probability;
	
	public MorphemeEntry(Morpheme morpheme, double probability)
	{
		this.morpheme = morpheme;;
		this.probability = probability;
	}
	
	public Morpheme getMorpheme()
	{
		return morpheme;
	}
	
	public double getProbability()
	{
		return probability;
	}
	
	public void setProbability(double probability)
	{
		this.probability = probability;
	}
	
	public int compareTo(MorphemeEntry me)
	{
		if (probability > me.probability)
			return 1;
		else if (probability < me.probability)
			return -1;
		return 0;
	}
}