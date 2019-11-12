package hiatusResolution;

import java.util.ArrayList;

import gengenv2.structures.ConsonantPhoneme;
import gengenv2.structures.PhonemeInstance;
import gengenv2.structures.VowelPhoneme;

public class VowelElision extends LengthenableResolution
{
	@Override
	public boolean applies(ArrayList<PhonemeInstance> phonemes, int v2Index)
	{
		return true;
	}

	@Override
	// Always elides V1 for now
	public void resolve(ArrayList<PhonemeInstance> phonemes, int v2Index)
	{
		// Delete empty onset(s) between vowels
		VowelPhoneme v2 = (VowelPhoneme) phonemes.get(v2Index).getPhoneme();
		
		int i = v2Index - 1;
		while (phonemes.get(i).getPhoneme() == ConsonantPhoneme.emptyOnset)
		{
			phonemes.remove(i);
			v2Index--;
			i--;
		}
		
		// Delete V1
		phonemes.remove(getV1Index(phonemes, v2Index));
		
		if (canLengthen && v2.getNucleusFollowers().contains(longVowel))
			phonemes.add(v2Index, new PhonemeInstance(longVowel));
	}
	
	
}
