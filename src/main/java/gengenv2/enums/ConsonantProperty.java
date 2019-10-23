package gengenv2.enums;

/**
 * The set of phonetic properties used to define consonantal segments. These cover a range of standard properties,
 * including manner and location of articulation, voicing, aspiration, etc., but also further categorizations
 * like 'liquid' and 'glide'. Even more specific labels like 'glottal fricative' or 'velar nasal' are used to
 * control probabilities and ensure they approximate real-world values.
 * 
 * @author	Clayton Cooper
 * @version	1.0
 * @since	1.0
 */
public enum ConsonantProperty implements SegmentProperty
{
	PLOSIVE(1), AFFRICATE(.623), FRICATIVE(.922), NASAL(.962), VOICELESS_NASAL(.038), APPROXIMANT(.969),
	GLOTTAL(.745), UVULAR(.140), VELAR(.987), POSTALVEOLAR(.590), ALVEOLAR(.997), LABIODENTAL(.574), BILABIAL(.993),
	VOICELESS(.989), VOICED(.767),
	SIBILANT(.914), NONSIBILANT(.694),
	LIQUID(.896),	// liquid | approximant
	GLIDE(.889),	// glide | approximant
	PALATAL_APPROX(.838), LABIOVELAR_APPROX(.741),	// x | approximant & glide 
	LATERAL_APPROX(.754), ALVEOLAR_TRILL(.661);		// x | approximant & liquid;
	
	private double probability;
	
	ConsonantProperty (double probability)
	{
		this.probability = probability;
	}
	
	public double getProbability() { return probability; }
}