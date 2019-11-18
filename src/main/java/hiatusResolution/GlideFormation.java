package hiatusResolution;

import java.util.ArrayList;

import gengenv2.enums.Constraint;
import gengenv2.enums.VowelProperty;
import gengenv2.structures.ConsonantPhoneme;
import gengenv2.structures.Phoneme;
import gengenv2.structures.PhonemeInstance;
import gengenv2.structures.VowelPhoneme;

public class GlideFormation extends LengthenableResolution
{
	private ConsonantPhoneme yGlide;
	private ConsonantPhoneme wGlide;
	private boolean initialYAllowed;
	private boolean initialWAllowed;
	private boolean medialYAllowed;
	private boolean medialWAllowed;
	private boolean midVowelsTrigger;
	private boolean blockedBySameFrontness;
	private boolean blockedByIdentity;
	
	public GlideFormation(ConsonantPhoneme yGlide, ConsonantPhoneme wGlide, boolean initialYAllowed, boolean initialWAllowed, boolean medialYAllowed, 
						boolean medialWAllowed, boolean midVowelsTrigger, boolean blockedBySameFrontness, boolean blockedByIdentity)
	{
		super();
		
		this.yGlide = yGlide;
		this.wGlide = wGlide;
		this.initialYAllowed = initialYAllowed;
		this.initialWAllowed = initialWAllowed;
		this.medialYAllowed = medialYAllowed;
		this.medialWAllowed = medialWAllowed;
		this.midVowelsTrigger = midVowelsTrigger;
		this.blockedBySameFrontness = blockedBySameFrontness;
		this.blockedByIdentity = blockedByIdentity;
		
		// Constraints	
		constraintSatisfaction[Constraint.MAX.ordinal()] 			= true;
		constraintSatisfaction[Constraint.DEP.ordinal()]			= true;
		constraintSatisfaction[Constraint.ISOLATION.ordinal()]	 	= true;
		constraintSatisfaction[Constraint.TIDY.ordinal()] 			= false;
		constraintSatisfaction[Constraint.ENCROACHING.ordinal()]	= true;
		constraintSatisfaction[Constraint.SYL_MAX.ordinal()]		= false;
		constraintSatisfaction[Constraint.SYL_MIN.ordinal()] 		= true;
		constraintSatisfaction[Constraint.NUCLEI_MAX.ordinal()]		= false;
		constraintSatisfaction[Constraint.NUCLEI_MIN.ordinal()]		= false;
	}

	private ConsonantPhoneme getGlide(ArrayList<PhonemeInstance> phonemes, int v2Index)
	{
		int v1Index = getV1Index(phonemes, v2Index);
		VowelPhoneme v1 = (VowelPhoneme) phonemes.get(v1Index).getPhoneme();
		
		if (v1.segment.properties[0] == VowelProperty.MID && !midVowelsTrigger)
			return null;
		if (v1.segment.properties[0] != VowelProperty.CLOSE)
			return null;
		
		VowelPhoneme v2 = (VowelPhoneme) phonemes.get(v2Index).getPhoneme();
		
		if (v1 == v2 && blockedByIdentity)
			return null;
		
		if (blockedBySameFrontness && v2.segment.properties.length > 1 && v2.segment.properties[1] == v1.segment.properties[1])
			return null;
		
		ConsonantPhoneme glide = null;
		if (v1.segment.properties[1] == VowelProperty.FRONT)
			glide = yGlide;
		else if (v1.segment.properties[1] == VowelProperty.BACK)
			glide = wGlide;
		if (glide == null)
			return null;
		
		Phoneme v0 = phonemes.get(v1Index - 1).getPhoneme();
		if (v0 != null && v0.segment.isConsonant() && glide.getBridgePreceders() != null && !glide.getBridgePreceders().contains(v0) && 
					((ConsonantPhoneme) v0).getOnsetFollowers() != null && !((ConsonantPhoneme) v0).getOnsetFollowers().contains(glide))
			return null;
		
		return glide;
	}
	
	@Override
	public boolean applies(ArrayList<PhonemeInstance> phonemes, int v2Index)
	{
		return getGlide(phonemes, v2Index) != null;
	}

	@Override
	public void resolve(ArrayList<PhonemeInstance> phonemes, int v2Index) 
	{
		ConsonantPhoneme glide = getGlide(phonemes, v2Index);
		
		// Delete empty onset if there is one (or all of them, if there's more than one)
		int i = v2Index - 1;
		while (phonemes.get(i).getPhoneme() == ConsonantPhoneme.emptyOnset)
		{
			phonemes.remove(i);
			i--;
			v2Index--;
		}
		
		// Delete first vowel
		phonemes.remove(v2Index - 1);
		
		// Add glide
		phonemes.add(v2Index - 1, new PhonemeInstance(glide));
		
		VowelPhoneme v2 = (VowelPhoneme) phonemes.get(v2Index).getPhoneme();
		if (canLengthen && v2.getNucleusFollowers().contains(longVowel))
			phonemes.add(v2Index + 1, new PhonemeInstance(longVowel));
	}

}
