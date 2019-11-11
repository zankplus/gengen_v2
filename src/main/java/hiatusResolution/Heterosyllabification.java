package hiatusResolution;

import java.util.ArrayList;

import gengenv2.structures.Morpheme;
import gengenv2.structures.Name;
import gengenv2.structures.Phoneme;
import gengenv2.structures.VowelPhoneme;

public class Heterosyllabification extends HiatusResolutionMethod
{
	boolean hiatusAllowed;
	boolean diphthongsAllowed;
	
	public Heterosyllabification(boolean hiatusAllowed, boolean diphthongsAllowed)
	{
		this.hiatusAllowed = hiatusAllowed;
		this.diphthongsAllowed = diphthongsAllowed;
	}
	
	public boolean applies(VowelPhoneme v1, VowelPhoneme v2)
	{
		if (!hiatusAllowed)
			return false;
		else if (diphthongsAllowed && v1.getNucleusFollowers() != null && v1.getNucleusFollowers().contains(v2))
			return false;
		
		if ((v1.getMedialFollowers() != null && v1.getMedialFollowers().contains(v2)))
			return true;
		else if (v1.getTerminalFollowers() != null && v1.getTerminalFollowers().contains(v2))
			return true;
		
		return false;
	}
	
	public void resolve(ArrayList<Phoneme> phonemes, int v2Index)
	{
		// No action necessary to resolve
	}
}
