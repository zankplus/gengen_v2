package hiatusResolution;

import java.util.ArrayList;

import gengenv2.structures.ConstituentLibrary;
import gengenv2.structures.Phoneme;
import gengenv2.structures.VowelPhoneme;

public class DiphthongFormation extends HiatusResolutionMethod 
{

	@Override
	public boolean applies(VowelPhoneme v1, VowelPhoneme v2)
	{
		ConstituentLibrary lib = v1.getNucleusFollowers();
		if (lib != null)
			return lib.contains(v2);
		
		return false;
	}

	@Override
	public void resolve(ArrayList<Phoneme> phonemes, int v2Index)
	{
		// No action necessary
	}
}
