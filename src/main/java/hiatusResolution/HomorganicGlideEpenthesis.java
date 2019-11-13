package hiatusResolution;

import java.util.ArrayList;

import gengenv2.enums.VowelProperty;
import gengenv2.structures.ConsonantPhoneme;
import gengenv2.structures.Phoneme;
import gengenv2.structures.PhonemeInstance;
import gengenv2.structures.VowelPhoneme;

public class HomorganicGlideEpenthesis extends LengthenableResolution
{
	private ConsonantPhoneme yGlide;
	private ConsonantPhoneme wGlide;
	private boolean homorganicWithV1;
	private boolean homorganicWithV2;
	private boolean midVowelsTrigger;
	private boolean v1MustNotMatchV2;
	
	public HomorganicGlideEpenthesis(ConsonantPhoneme yGlide, ConsonantPhoneme wGlide, boolean homorganicWithV1, boolean homorganicWithV2,
										boolean midVowelsTrigger, boolean v1MustNotMatchV2)
	{
		this.yGlide = yGlide;
		this.wGlide = wGlide;
		this.homorganicWithV1 = homorganicWithV1;
		this.homorganicWithV2 = homorganicWithV2;
		this.midVowelsTrigger = midVowelsTrigger;
		this.v1MustNotMatchV2 = v1MustNotMatchV2;
	}

	public ConsonantPhoneme getEpentheticGlide(ArrayList<PhonemeInstance> phonemes, int v2Index)
	{
		int v1Index = getV1Index(phonemes, v2Index);
		VowelPhoneme v1 = (VowelPhoneme) phonemes.get(v1Index).getPhoneme();
		VowelPhoneme v2 = (VowelPhoneme) phonemes.get(v2Index).getPhoneme();
		
		Phoneme prev = null;
		if (v1Index > 0) 
				prev = phonemes.get(v1Index - 1).getPhoneme();
		
		if (v1 == v2 && v1MustNotMatchV2)
			return null;
		
		if (homorganicWithV1)
		{
			if (v1.segment.properties[0] == VowelProperty.CLOSE || (v1.segment.properties[0] == VowelProperty.MID && midVowelsTrigger))
			{
				if (v1.segment.properties[1] == VowelProperty.FRONT)
					return yGlide;
				else if (v1.segment.properties[1] == VowelProperty.BACK)
					return wGlide;
			}
		}
		
		if (homorganicWithV2)
		{
			if (v2.segment.properties[0] == VowelProperty.CLOSE || (v2.segment.properties[0] == VowelProperty.MID && midVowelsTrigger))
			{
				if (v2.segment.properties[1] == VowelProperty.FRONT)
					return yGlide;
				else if (v2.segment.properties[1] == VowelProperty.BACK)
					return wGlide;
			}
		}
		
		return null;
	}
	
	@Override
	public boolean applies(ArrayList<PhonemeInstance> phonemes, int v2Index)
	{
		return getEpentheticGlide(phonemes, v2Index) != null;
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
		ConsonantPhoneme glide = getEpentheticGlide(phonemes, v2Index);
		phonemes.add(v2Index, new PhonemeInstance(glide));
	}
}
