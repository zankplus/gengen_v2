package gengenv2;

import gengenv2.enums.Feature;

public class FeatureSet
{
	public Feature initialOnsets;
	public Feature onsetClusters;
	public Feature medialCodas;
	public Feature terminalCodas;
	public Feature codaClusters;
	public Feature geminateConsonants;
	public Feature geminateVowels;
	public Feature diphthongs;
	public Feature hiatus;
	public Feature fCombination;
	
	// Morphological features
	public Feature compensatoryLengthening;
	
	public FeatureSet()
	{
		initialOnsets		= Feature.YES;
		onsetClusters 		= Feature.NO;
		medialCodas 		= Feature.NO;
		terminalCodas		= Feature.NO;
		codaClusters 		= Feature.NO;
		geminateConsonants	= Feature.NO;
		geminateVowels		= Feature.NO;
		diphthongs			= Feature.NO;
		hiatus				= Feature.NO;
		compensatoryLengthening	= Feature.NO;
	}
}