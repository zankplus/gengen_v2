package hiatusResolution;

import java.util.ArrayList;

import gengenv2.structures.ConsonantPhoneme;
import gengenv2.structures.PhonemeInstance;

public class ConsistentConsonantEpenthesis extends HiatusResolutionMethod
{
	private ConsonantPhoneme leastMarkedConsonant;
	
	public ConsistentConsonantEpenthesis(ConsonantPhoneme leastMarkedConsonant)
	{
		this.leastMarkedConsonant = leastMarkedConsonant;
	}
	
	@Override
	public boolean applies(ArrayList<PhonemeInstance> phonemes, int v2Index)
	{
		return leastMarkedConsonant != null;
	}

	@Override
	public void resolve(ArrayList<PhonemeInstance> phonemes, int v2Index)
	{
		// Delete empty onset if there is one (or all of them, if there's more than one)
		int i = v2Index - 1;
		while (phonemes.get(i).getPhoneme() == ConsonantPhoneme.emptyOnset)
		{
			phonemes.remove(i);
			i--;
			v2Index--;
		}
		
		// Insert glide
		phonemes.add(v2Index, new PhonemeInstance(leastMarkedConsonant));
	}

}
