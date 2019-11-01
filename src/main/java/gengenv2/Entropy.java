package gengenv2;

public class Entropy
{
	/**
	 * Determines the entropy of an event where one subsequent event is chosen at random from a list of events
	 * with known probabilities and entropies. 
	 * 
	 * @param probabilities	The probabilities for an array of events
	 * @param entropies		The corresponding entropy measurements for an array of events
	 * @return				The entropy measurement for the decision between subsequent events
	 * @since				1.1
	 */
	public static double decisionEntropy(double[] probabilities, double[] entropies)
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
	
	// Returns entropy of a probability, weighted by that probability. For use in entropy summation
	public static double partialEntropy(double probability)
	{
		return probability * -Math.log(probability);
	}
}
