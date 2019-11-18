package hiatusResolution;

import java.util.ArrayList;

import gengenv2.enums.Constraint;
import gengenv2.structures.ConsonantPhoneme;
import gengenv2.structures.PhonemeInstance;
import gengenv2.structures.VowelPhoneme;

public class Diaeresis extends HiatusResolutionMethod
{

	public Diaeresis()
	{
		super();
		
		// Constraints
		constraintSatisfaction[Constraint.MAX.ordinal()] 			= true;
		constraintSatisfaction[Constraint.DEP.ordinal()]			= true;
		constraintSatisfaction[Constraint.ISOLATION.ordinal()]	 	= false;
		constraintSatisfaction[Constraint.TIDY.ordinal()] 			= true;
		constraintSatisfaction[Constraint.ENCROACHING.ordinal()]	= false;
		constraintSatisfaction[Constraint.SYL_MAX.ordinal()]		= true;
		constraintSatisfaction[Constraint.SYL_MIN.ordinal()] 		= false;
		constraintSatisfaction[Constraint.NUCLEI_MAX.ordinal()]		= true;
		constraintSatisfaction[Constraint.NUCLEI_MIN.ordinal()]		= false;
	}
	
	@Override
	public boolean applies(ArrayList<PhonemeInstance> phonemes, int v2Index)
	{
		VowelPhoneme v1 = (VowelPhoneme) phonemes.get(getV1Index(phonemes, v2Index)).getPhoneme();
		VowelPhoneme v2 = (VowelPhoneme) phonemes.get(v2Index).getPhoneme();
		
		if ((v1.getMedialFollowers() != null && v1.getMedialFollowers().contains(v2)) ||
				(v1.getTerminalFollowers() != null && v1.getTerminalFollowers().contains(v2)))
			return true;
		
		return false;
	}

	@Override
	public void resolve(ArrayList<PhonemeInstance> phonemes, int v2Index)
	{
		if (v2Index - getV1Index(phonemes, v2Index) == 1)
			phonemes.add(v2Index, new PhonemeInstance(ConsonantPhoneme.emptyOnset));
	}
	
	
}
