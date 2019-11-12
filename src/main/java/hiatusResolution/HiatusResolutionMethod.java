package hiatusResolution;

import java.util.ArrayList;

import gengenv2.structures.ConsonantPhoneme;
import gengenv2.structures.GengenName;
import gengenv2.structures.Phoneme;
import gengenv2.structures.PhonemeInstance;
import gengenv2.structures.VowelPhoneme;

public abstract class HiatusResolutionMethod
{
	public int getV1Index(ArrayList<PhonemeInstance> phonemes, int v2Index)
	{
		int i = v2Index - 1;
		while (phonemes.get(i).getPhoneme() == ConsonantPhoneme.emptyOnset)
			i--;
		
		return i;
	}
	
	public abstract boolean applies(ArrayList<PhonemeInstance> phonemes, int v2Index);
	public abstract void resolve(ArrayList<PhonemeInstance> phonemes, int v2Index);
}
