package hiatusResolution;

import java.util.ArrayList;

import gengenv2.structures.Name;
import gengenv2.structures.Phoneme;
import gengenv2.structures.VowelPhoneme;

public abstract class HiatusResolutionMethod
{
	public abstract boolean applies(VowelPhoneme v1, VowelPhoneme v2);
	public abstract void resolve(ArrayList<Phoneme> phonemes, int v2Index);
}
