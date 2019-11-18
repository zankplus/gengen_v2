package hiatusResolution;

import java.util.ArrayList;

import gengenv2.enums.Constraint;
import gengenv2.structures.ConsonantPhoneme;
import gengenv2.structures.ConstituentLibrary;
import gengenv2.structures.Phoneme;
import gengenv2.structures.PhonemeInstance;
import gengenv2.structures.VowelPhoneme;

public class DiphthongFormation extends HiatusResolutionMethod 
{
	public DiphthongFormation()
	{
		super();
		
		// Constraints
		constraintSatisfaction[Constraint.MAX.ordinal()] 			= true;
		constraintSatisfaction[Constraint.DEP.ordinal()]			= true;
		constraintSatisfaction[Constraint.ISOLATION.ordinal()]	 	= false;
		constraintSatisfaction[Constraint.TIDY.ordinal()] 			= false;
		constraintSatisfaction[Constraint.ENCROACHING.ordinal()]	= true;
		constraintSatisfaction[Constraint.SYL_MAX.ordinal()]		= false;
		constraintSatisfaction[Constraint.SYL_MIN.ordinal()] 		= true;
		constraintSatisfaction[Constraint.NUCLEI_MAX.ordinal()]		= true;
		constraintSatisfaction[Constraint.NUCLEI_MIN.ordinal()]		= false;
	}
	
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
