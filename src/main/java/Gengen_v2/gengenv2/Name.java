package Gengen_v2.gengenv2;

import java.util.ArrayList;

import Gengen_v2.gengenv2.Phonology.SyllableSegment;

public class Name
{
	Phonology language;
	SyllableSegment[] content;
	String orthography;
	
	public Name(ArrayList<SyllableSegment> content)
	{
		this.content = content.toArray(new SyllableSegment[content.size()]);
		
		
	}
}
