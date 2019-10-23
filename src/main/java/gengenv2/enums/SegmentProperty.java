package gengenv2.enums;

/**
 * Interface used by ConsonantProperty and VowelProperty enums to grant them some semblance of polymorphism in
 * relevant contexts.
 * 
 * In Gengen, a Segment is defined chiefly by its SegmentProperties. A property's probability represents the odds
 * that sounds with that property will occur in the Phonology; any Segment all the properties of which occur in the
 * Phonology will itself appear in the Phonology's phonemic inventory. Accordingly, the raw chance of a Segment
 * occurring is equal to the product of the probability values of each of its properties. 
 * @since	1.0
 */
public interface SegmentProperty
{
	abstract double getProbability();
}