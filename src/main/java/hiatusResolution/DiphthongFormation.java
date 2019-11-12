package hiatusResolution;

import java.util.ArrayList;

import gengenv2.structures.ConsonantPhoneme;
import gengenv2.structures.ConstituentLibrary;
import gengenv2.structures.Phoneme;
import gengenv2.structures.PhonemeInstance;
import gengenv2.structures.VowelPhoneme;

public class DiphthongFormation extends HiatusResolutionMethod 
{
	public boolean canFormDiphthong(VowelPhoneme v1, VowelPhoneme v2)
	{
		ConstituentLibrary lib = v1.getNucleusFollowers();
		if (lib != null)
			return lib.contains(v2);
		
		return false;
	}
	
	@Override
	public boolean applies(ArrayList<PhonemeInstance> phonemes, int v2Index)
	{
		VowelPhoneme v1 = (VowelPhoneme) phonemes.get(getV1Index(phonemes, v2Index)).getPhoneme();
		VowelPhoneme v2 = (VowelPhoneme) phonemes.get(v2Index).getPhoneme();
		
		return canFormDiphthong(v1, v2);
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
		}
	}
}
