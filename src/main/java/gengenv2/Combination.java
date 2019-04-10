package gengenv2;

import gengenv2.Name.Syllable;
import gengenv2.Phonology.ConsonantPhoneme;
import gengenv2.Phonology.Phoneme;
import gengenv2.Phonology.VowelPhoneme;

public class Combination
{
	public static Name hiation(Name stem, Name suffix)
	{
		if (stem.getWordType() != WordType.STEM || suffix.getWordType() != WordType.SUFFIX)
			return null;
		
		// Add all syllables from suffix to stem
		for (Syllable syl : suffix.getSyllables())
			for (Constituent c : syl.constituents)
				if (c != null)
					stem.add(c);
		
		stem.setWordType(WordType.COMPLETE);
		
		return stem;
	}
	
	public static Name unification(Name stem, Name suffix, Constituent diphthong)
	{
		if (stem.getWordType() != WordType.STEM || suffix.getWordType() != WordType.SUFFIX)
			return null;
		
		// Delete root from stem
		if (stem.deletePivotVowel() == null)
			return null;
		
		// Add diphthong
		stem.add(diphthong);
		
		// Add syllables from suffix to stem, omitting leftmost nucleus
		if (suffix.getSyllables().get(0).constituents[0] == null)
		{
			// Add coda of first syllable
			stem.add(suffix.getSyllables().get(0).constituents[2]);
			
			// Add syllables past the first
			for (int i = 1; i < suffix.getSyllables().size(); i++)
				for (Constituent c : suffix.getSyllables().get(i).constituents)
					if (c != null)
						stem.add(c);
			
			stem.setWordType(WordType.COMPLETE);
		}
		
		return stem;
	}
	
	public static Name insertion(Name stem, Name suffix, Constituent inserendum)
	{
		if (stem.getWordType() != WordType.STEM || suffix.getWordType() != WordType.SUFFIX)
			return null;
		
		stem.add(inserendum);
		
		// Add all syllables from suffix to stem
		for (Syllable syl : suffix.getSyllables())
			for (Constituent c : syl.constituents)
				if (c != null)
					stem.add(c);
		
		stem.setWordType(WordType.COMPLETE);
		
		return stem;
	}
	
	public static Name leftDeletion(Name stem, Name suffix)
	{
		if (stem.getWordType() != WordType.STEM || suffix.getWordType() != WordType.SUFFIX)
			return null;
		
		// Delete root from stem
		if (stem.deletePivotVowel() == null)
			return null;
		
		// Add syllables from suffix to stem
		for (Syllable syl : suffix.getSyllables())
			for (Constituent c : syl.constituents)
				if (c != null)
					stem.add(c);
		
		stem.setWordType(WordType.COMPLETE);
		
		return stem;
	}
	
	public static Name rightDeletion(Name stem, Name suffix)
	{
		if (stem.getWordType() != WordType.STEM || suffix.getWordType() != WordType.SUFFIX)
			return null;
		
		// Add syllables from suffix to stem, omitting leftmost nucleus
		if (suffix.getSyllables().get(0).constituents[0] == null)
		{
			// Add coda of first syllable
			stem.add(suffix.getSyllables().get(0).constituents[2]);
			
			// Add syllables past the first
			for (int i = 1; i < suffix.getSyllables().size(); i++)
				for (Constituent c : suffix.getSyllables().get(i).constituents)
					if (c != null)
						stem.add(c);
			
			stem.setWordType(WordType.COMPLETE);
		}
		else
			return null;
		
		return stem;
	}
	
	public static Name doubleDeletion(Name stem, Name suffix)
	{
		if (stem.getWordType() != WordType.STEM || suffix.getWordType() != WordType.SUFFIX)
			return null;
		
		// Delete root from stem
		if (stem.deletePivotVowel() == null)
			return null;
		
		// Add syllables from suffix to stem, omitting leftmost nucleus
		if (suffix.getSyllables().get(0).constituents[0] == null)
		{
			// Add coda of first syllable
			stem.add(suffix.getSyllables().get(0).constituents[2]);
			
			// Add syllables past the first
			for (int i = 1; i < suffix.getSyllables().size(); i++)
				for (Constituent c : suffix.getSyllables().get(i).constituents)
					if (c != null)
						stem.add(c);
			
			stem.setWordType(WordType.COMPLETE);
		}
		
		return stem;
	}
}

class CombinationRules
{
	CombinationRule[] rules;
	Phonology p;
	Constituent medialY, medialW;
	
	public CombinationRules(Phonology p)
	{
		this.p = p;
		rules = new CombinationRule[] { CombinationRule.HIATUS,
										CombinationRule.LEFT_DELETION,
										CombinationRule.RIGHT_DELETION,
										CombinationRule.LEFT_APPROXIMANT_INSERTION,
										CombinationRule.GENERAL_INSERTION };
		
		for (ConsonantPhoneme cp : p.consonantInventory)
		{
			if (cp.segment.expression.equals("y"))
				medialY = new Constituent(ConstituentType.ONSET, new ConsonantPhoneme[] { cp }, 1);
			if (cp.segment.expression.equals("w"))
				medialW = new Constituent(ConstituentType.ONSET, new ConsonantPhoneme[] { cp }, 1);
			if (medialY != null && medialW != null)
				break;
		}	
	}
	
	public Name combine(Name stem, Name suffix, boolean copyStem)
	{
		if (suffix == Name.NEW_SUFFIX)
			suffix = p.assembly.makeSuffix();
		
		Name left = copyStem ? new Name(stem) : stem;
		if (left.getWordType() != WordType.STEM || suffix.getWordType() != WordType.SUFFIX)
			return null;
		
		if (isDoubleDeletionLegal(stem, suffix))
			return Combination.doubleDeletion(stem, suffix);
		
		if (isHiationLegal(stem, suffix))
			return Combination.hiation(left, suffix);
		
		if (isLeftDeletionLegal(left, suffix))
			return Combination.leftDeletion(left, suffix);
		
		if (isRightDeletionLegal(left, suffix))
			return Combination.rightDeletion(left, suffix);
		
		if (isInsertionLegal(left, suffix))
		{
			Constituent leftInsertion = approximantInsertionCheck(left, suffix, false);
			if (leftInsertion != null)
				return Combination.insertion(left, suffix, leftInsertion);
			
			Constituent rightInsertion = approximantInsertionCheck(left, suffix, true);
			if (rightInsertion != null)
			{
//				System.out.println("right insertion on " + left);
				return Combination.insertion(left, suffix, rightInsertion);
			}
			else
				return Combination.insertion(left, suffix, p.suffixes.inserendum);
		}
		
		return null;
	}
	
	public boolean isLeftDeletionLegal(Name stem, Name suffix)
	{
		if (stem.strength == RootStrength.STRONG)
			return false;
		return true;
	}
	
	public boolean isRightDeletionLegal(Name stem, Name suffix)
	{
		if (suffix.strength == RootStrength.STRONG)
			return false;
		return true;
	}
	
	public boolean isDoubleDeletionLegal(Name stem, Name suffix)
	{
		if (stem.strength == RootStrength.STRONG || suffix.strength == RootStrength.STRONG)
			return false;
		
		// Stem must end in a consonant OR be polysyllabic and end in a non-hiatual vowel
		if (stem.lastConstituent().type == ConstituentType.NUCLEUS)
		{
			if (stem.sylCount() == 1)
				return false;
			if (!stem.lastSyllable().hasOnset())
				return false;
		}
		
		// First nucleus of suffix must be followed by a consonant
		if (suffix.sylCount() == 1)
		{
			if (!suffix.firstSyllable().hasCoda())
				return false;
		}
		else if (!suffix.getSyllables().get(1).hasOnset())
		{
			return false;
		}
		
		if (stem.lastSyllable().constituents[0].size() == 1)
		{
			// Check last consonant of stem against first of root
			Phoneme left, right;
			left = stem.lastSyllable().constituents[0].content[0];
			if (suffix.firstSyllable().hasCoda())
				right = suffix.firstSyllable().constituents[2].content[0];
			else
				right = suffix.getSyllables().get(1).constituents[0].content[0];
			
			
			Constituent c;
			ConstituentLibrary lib;
			if (suffix.sylCount() == 1)
			{
				lib = p.terminalCodas;
				c = new Constituent(ConstituentType.CODA, new Phoneme[] { left, right }, 0);
			}
			else
			{
				lib = left.followers;
				c = new Constituent(ConstituentType.CODA, new Phoneme[] { right }, 0);
			}
			
			Constituent match = lib.getMatchingConstituent(c);
			
			if (match == null)
				return false;
			
			// Check second-last consonant of stem against first of root, if necessary
			if (stem.sylCount() > 1 && stem.getSyllables().get(stem.sylCount() - 2).hasCoda())
			{
				Phoneme[] sequence = new Phoneme[2];  
				sequence[1] = left;
				sequence[0] = stem.getSyllables().get(stem.sylCount() - 2).constituents[2].lastPhoneme();
				c.content = sequence;
				
				if(suffix.sylCount() == 1)
					lib = p.medialCodas;
				match = lib.getMatchingConstituent(c);
				if (match == null)
					return false;
			}
			
			return true;
		}
		return false;
	}
	
	public boolean isHiationLegal(Name stem, Name suffix)
	{
		if (!p.hasHiatus())
			return false;
		
		if (stem.strength == RootStrength.WEAK || suffix.strength == RootStrength.WEAK)
			return false;
		
		// If the suffix is monosyllabic, check the stem's vowel's terminal followers list
		if (suffix.sylCount() == 1)
		{
			if (((VowelPhoneme)stem.lastConstituent().lastPhoneme()).terminalFollowers.getMatchingConstituent(suffix.firstConstituent())
					!= null)
			{
				return true;
			}
		}
		
		// If the suffix is polysyllabic, check the stem's vowel's medial followers list
		else if (stem.lastConstituent().lastPhoneme().followers.getMatchingConstituent(suffix.firstConstituent())
				!= null)
			return true;
		
		return false;
	}
	
	public Constituent unificationCheck(Name stem, Name suffix)
	{
		// unification only works on pairs of simple onsets
		if (stem.lastConstituent().size() > 1 || suffix.firstConstituent().size() > 1)
			return null;
		
		VowelPhoneme v1 = ((VowelPhoneme)stem.lastConstituent().lastPhoneme());
		VowelPhoneme v2 = ((VowelPhoneme)suffix.firstConstituent().content[0]);
		
		ConstituentLibrary lib;
		
		// Medial nucleus case
		if (suffix.sylCount() > 1 || suffix.getSyllables().get(0).constituents[2] != null)
			lib = v1.followers;
		
		// Terminal nucleus case
		else
			lib = v1.terminalFollowers;
		
		Constituent diphthong = null;
		
		// If v1 and v2 match, search for long version of v1 
		if (p.longVowel != null && v1 == v2)
			diphthong = new Constituent(null, new Phoneme[] { v1, p.longVowel }, 0);
		else
			diphthong = new Constituent(null, new Phoneme[] { v1, v2 }, 0);
		
		return lib.getMatchingConstituent(diphthong);
	}
	
	public boolean isInsertionLegal(Name stem, Name suffix)
	{
		return (stem.strength == RootStrength.STRONG && suffix.strength == RootStrength.STRONG);
	}

	public Constituent approximantInsertionCheck(Name stem, Name suffix, boolean rightSide)
	{
		Phoneme ph;
		if (rightSide)
			ph = suffix.getSyllables().get(0).constituents[1].content[0];
		else
			ph = stem.lastConstituent().lastPhoneme();
		
		if (medialY != null &&	(ph.segment.expression.equals("i") || 
								 ph.segment.expression.equals("e") ||
								 ph.segment.expression.equals("y")))
			return medialY;
		
		else if (medialW != null && (ph.segment.expression.equals("u") || 
									 ph.segment.expression.equals("o")))
		{
			return medialW;
		}
		
		return null;
	}
}

enum CombinationRule { LEFT_DELETION, RIGHT_DELETION, HIATUS, UNIFICATION, GENERAL_INSERTION,
					   LEFT_APPROXIMANT_INSERTION }