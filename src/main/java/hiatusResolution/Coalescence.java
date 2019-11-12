package hiatusResolution;

import java.util.ArrayList;

import gengenv2.enums.VowelProperty;
import gengenv2.structures.ConsonantPhoneme;
import gengenv2.structures.ConstituentLibrary;
import gengenv2.structures.PhonemeInstance;
import gengenv2.structures.VowelPhoneme;

public class Coalescence extends LengthenableResolution
{
	private VowelPhoneme frontMidVowel;
	private VowelPhoneme backMidVowel;
	private boolean frontMidMedial;
	private boolean frontMidTerminal;
	private boolean backMidMedial;
	private boolean backMidTerminal;
	
	public Coalescence(VowelPhoneme frontMidVowel, VowelPhoneme backMidVowel, boolean frontMidMedial, boolean frontMidTerminal, boolean backMidMedial,
						boolean backMidTerminal)
	{
		this.frontMidVowel = frontMidVowel;
		this.backMidVowel = backMidVowel;
		this.frontMidMedial = frontMidMedial;
		this.frontMidTerminal = frontMidTerminal;
		this.backMidMedial = backMidMedial;
		this.backMidTerminal = backMidTerminal; 
	}
	
	public boolean applies(ArrayList<PhonemeInstance> phonemes, int v2Index)
	{
		VowelPhoneme v2 = (VowelPhoneme) phonemes.get(v2Index).getPhoneme();
		if (v2.segment.properties[0] != VowelProperty.CLOSE)
			return false;
		
		VowelPhoneme v1 = (VowelPhoneme) phonemes.get(getV1Index(phonemes, v2Index)).getPhoneme();
		if (v1.segment.properties[0] == VowelProperty.CLOSE)
			return false;
		else if (v2.segment.properties.length == 0)
			return false;
		else if (v2.segment.properties[1] == VowelProperty.FRONT)
		{
			if (frontMidVowel == null)
				return false;
			else if (v2Index == phonemes.size() - 1)
				return (frontMidTerminal);
			else
				return (frontMidMedial);
		}
		
		else if (v2.segment.properties[1] == VowelProperty.BACK)
		{
			if (backMidVowel == null)
				return false;
			else if (v2Index == phonemes.size() - 1)
				return (backMidTerminal);
			else
				return (backMidMedial);
		}
		
		return false;
	}

	@Override
	public void resolve(ArrayList<PhonemeInstance> phonemes, int v2Index)
	{
		VowelPhoneme v2 = (VowelPhoneme) phonemes.get(v2Index).getPhoneme();
		VowelPhoneme target;
		if (v2.segment.properties[1] == VowelProperty.FRONT)
			target = frontMidVowel;
		else
			target = backMidVowel;
		
		// Delete empty onset(s) between vowels
		int i = v2Index - 1;
		while (phonemes.get(i).getPhoneme() == ConsonantPhoneme.emptyOnset)
		{
			phonemes.remove(i);
			v2Index--;
			i--;
		}
		
		phonemes.remove(v2Index);
		phonemes.remove(v2Index - 1);
		phonemes.add(v2Index - 1, new PhonemeInstance(target));
		
		if (canLengthen && target.getNucleusFollowers().contains(longVowel))
			phonemes.add(v2Index, new PhonemeInstance(longVowel));
	}

}
