package gengenv2.enums;

/**
 * The set of phonetic properties used to define vocalic segments. These include chiefly two features: vowel height 
 * (or closeness) and vowel backness. As a possibly temporary provision, the 'schwa' property is included as the sole
 * feature of its corresponding Segment, as is the 'long' property, which follows another vowel (as if in a diphthong)
 * to make it long by nature. 
 * 
 * @author	Clayton Cooper
 * @version	1.0
 * @since	1.0
 */
public enum VowelProperty implements SegmentProperty
{
	NULL(/*.169*/ 0), OPEN(.984), MID(.920), CLOSE(.993),
	FRONT(.991), CENTER(.159), BACK(.991),
	LONG(0);	
	
	private double probability;
	
	VowelProperty (double probability)
	{
		this.probability = probability;
	}
	
	public double getProbability() { return probability; }
}