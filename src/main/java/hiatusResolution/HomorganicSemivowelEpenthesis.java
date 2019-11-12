package hiatusResolution;

import java.util.ArrayList;

import gengenv2.enums.VowelProperty;
import gengenv2.structures.ConsonantPhoneme;
import gengenv2.structures.Phoneme;
import gengenv2.structures.PhonemeInstance;
import gengenv2.structures.VowelPhoneme;

public class HomorganicSemivowelEpenthesis extends LengthenableResolution
{
	private ConsonantPhoneme yGlide;
	private ConsonantPhoneme wGlide;
	private boolean initialYAllowed;
	private boolean initialWAllowed;
	private boolean medialYAllowed;
	private boolean medialWAllowed;
	
	public HomorganicSemivowelEpenthesis(ConsonantPhoneme yGlide, ConsonantPhoneme wGlide, boolean initialYAllowed, boolean initialWAllowed,
											boolean medialYAllowed, boolean medialWAllowed)
	{
		this.yGlide = yGlide;
		this.wGlide = wGlide;
		this.initialYAllowed = initialYAllowed;
		this.initialWAllowed = initialWAllowed;
		this.medialYAllowed = medialYAllowed;
		this.medialWAllowed = medialWAllowed;
	}

	@Override
	public boolean applies(ArrayList<PhonemeInstance> phonemes, int v2Index)
	{
		int v1Index = getV1Index(phonemes, v2Index);
		VowelPhoneme v1 = (VowelPhoneme) phonemes.get(v1Index).getPhoneme();
		
		Phoneme prev = null;
		if (v1Index > 0) 
				prev = phonemes.get(v1Index - 1).getPhoneme();
		
		
		if (v1.segment.properties[0] == VowelProperty.CLOSE)
		{
			if (v1.segment.properties[1] == VowelProperty.FRONT)
			{
				
			}
		}
		
		return false;
	}

	@Override
	public void resolve(ArrayList<PhonemeInstance> phonemes, int v2Index) {
		// TODO Auto-generated method stub
		
	}
}
