package gengenv2;

import gengenv2.enums.ConsonantProperty;
import gengenv2.enums.SegmentProperty;
import gengenv2.enums.VowelProperty;

public class PhoneticRatings
{
	/**
	 * 
	 */
	private final Phonology phonology;
	private double[] ratings;
	boolean[] propertiesRepresented;
	SegmentProperty[] properties;
	
	public PhoneticRatings(Phonology phonology, boolean consonant, double stdev, double mean)
	{
		this.phonology = phonology;
		if (consonant)
		{
			properties = ConsonantProperty.values();
			propertiesRepresented = this.phonology.consonantPropertiesRepresented;
		}
		else
		{
			properties = VowelProperty.values();
			propertiesRepresented = this.phonology.vowelPropertiesRepresented;
		}
		
		ratings = new double[properties.length];
		
		for (int i = 0; i < ratings.length; i++)
			if (propertiesRepresented[i])
				ratings[i] = this.phonology.rng.nextGaussian() * stdev + mean;
	}
	
	/**
	 * Copy constructor
	 * @param other	The PhoneticRatings to be copied
	 * @param phonology TODO
	 */
	public PhoneticRatings(Phonology phonology, PhoneticRatings other)
	{
		this.phonology = phonology;
		ratings = new double[other.properties.length];
		for (int i = 0; i < ratings.length; i++)
			ratings[i] = other.getRating(i);
		
		this.properties = other.properties;
		this.propertiesRepresented = other.propertiesRepresented;
	}
	
	public void disturb(double stdev)
	{
		for (int i = 0; i < ratings.length; i++)
			ratings[i] = this.phonology.rng.nextGaussian() * stdev + ratings[i];
	}
	
	public void offset(double offset)
	{
		for (int i = 0; i < ratings.length; i++)
			ratings[i] = ratings[i] + offset;
	}
	
	public void offset(double[] offsets)
	{
		if (offsets.length != ratings.length)
			return;
		
		for (int i = 0; i < ratings.length; i++)
			ratings[i] = ratings[i] + offsets[i];
	}
	
	public void exaggerate(double power)
	{
		for (int i = 0; i < ratings.length; i++)
			ratings[i] = Math.pow(ratings[i], power);
	}
	
	public double getRating(int index)
	{
		return ratings[index];
	}
	
	public void setRating(int index, double value)
	{
		ratings[index] = value;
	}
	
	public int size()
	{
		return ratings.length;
	}
}