package nl.liacs.subdisc;

public class CorrelationMeasure
{
	private final CorrelationMeasure itsBase;
	private final QM itsType;
	private int itsSampleSize = 0; //gives the size of this sample
	private double itsXSum = 0.0;
	private double itsYSum = 0.0;
	private double itsXYSum = 0.0;
	private double itsXSquaredSum = 0.0;
	private double itsYSquaredSum = 0.0;
	private double itsCorrelation = 0.0;
	private boolean itsCorrelationIsOutdated = true; //flag indicating whether the latest computed correlation is outdated and whether it should be computed again
	private boolean itsComplementIsOutdated = true; //flag indicating whether the latest computed correlation for its complement is outdated and whether it should be computed again
	private double itsComplementCorrelation = Double.NaN;

	//make a base model from two columns
	public CorrelationMeasure(QM theType, Column thePrimaryColumn, Column theSecondaryColumn) throws IllegalArgumentException
	{
		if (!isValidCorrelationMeasureType(theType))
			throw createQMException(theType);

		itsBase = null; //no base model to refer to yet
		itsType = theType;

		for (int i = 0, j = thePrimaryColumn.size(); i < j; i++)
			addObservation(thePrimaryColumn.getFloat(i), theSecondaryColumn.getFloat(i));
	}

	public CorrelationMeasure(CorrelationMeasure theBase) throws NullPointerException, IllegalArgumentException
	{
		if (theBase == null)
			throw (new NullPointerException("Implementation: theBase should not be null"));
		if (!isValidCorrelationMeasureType(theBase.itsType))
			throw createQMException(theBase.itsType);

		itsBase = theBase;
		itsType = theBase.itsType;
	}

	private static boolean isValidCorrelationMeasureType(QM theType)
	{
		return QM.getQualityMeasures(TargetType.DOUBLE_CORRELATION).contains(theType);
	}

	private static IllegalArgumentException createQMException(QM theType)
	{
		return new IllegalArgumentException(
				String.format("%s: invalid %s-type '%s'",
						CorrelationMeasure.class.getSimpleName(),
						QM.class.getSimpleName(),
						theType));
	}

	// XXX never used but LEAVE IT IN
/*
	public CorrelationMeasure(double[] theXValues, double[] theYValues, int theType, CorrelationMeasure theBase)
	{
		itsBase = theBase;
		itsType = theType;
		if(theXValues.length == theYValues.length)
		{
			itsSampleSize = theXValues.length;
			for(int n = 0; n < itsSampleSize; n++)
			{
				itsXSum += theXValues[n];
				itsYSum += theYValues[n];
				itsXYSum += theXValues[n]*theYValues[n];
				itsXSquaredSum += theXValues[n]*theXValues[n];
				itsYSquaredSum += theYValues[n]*theYValues[n];
			}
		}
		else
		{
			Log.error("Length of X-values different from length of the Y-values. Disregarding the values and construct empty CorrelationMeasure");
			itsSampleSize = 0;
			itsXSum = 0;
			itsYSum = 0;
			itsXYSum = 0;
			itsXSquaredSum = 0;
			itsYSquaredSum = 0;
		}
	}
*/
	/**
	 * Adds a new observation after which the current correlation will be
	 * outdated.
	 * 
	 * {@link #getCorrelation()} automatically takes care that the current
	 * correlation is returned by checking whether CorrelationMeasure is
	 * outdated.
	 * 
	 * @param theXValue the x-value of an observation
	 * @param theYValue the y-value of an observation
	 */
	public void addObservation(double theYValue, double theXValue)
	{
		itsSampleSize++;
		itsXSum += theXValue;
		itsYSum += theYValue;
		itsXYSum += theXValue*theYValue;
		itsXSquaredSum += theXValue*theXValue;
		itsYSquaredSum += theYValue*theYValue;
		itsCorrelationIsOutdated = true; //invalidate the computed correlation
		itsComplementIsOutdated = true; //invalidate the computed correlation for the complement
	}

	/**
	 * Returns the correlation given the observations contained by
	 * this CorrelationMeasure.
	 * 
	 * @return the correlation
	 */
	public double getCorrelation()
	{
		if (itsCorrelationIsOutdated)
			return computeCorrelation();
		else
			return itsCorrelation;
	}

	/**
	 * Computes and returns the correlation given the observations contained
	 * by this CorrelationMeasure.
	 * 
	 * @return the computed correlation
	 */
	private double computeCorrelation()
	{
		itsCorrelation = (itsSampleSize*itsXYSum - itsXSum*itsYSum)/
			Math.sqrt((itsSampleSize*itsXSquaredSum - itsXSum*itsXSum) * (itsSampleSize*itsYSquaredSum - itsYSum*itsYSum));
		itsCorrelationIsOutdated = false; //set flag to false, so subsequent calls to getCorrelation don't need anymore computation.
		return itsCorrelation;
	}

	/**
	 * Computes the difference between the correlations of this subset and
	 * its complement.
	 * 
	 * @return correlation distance
	 */
	public double computeCorrelationDistance()
	{
		int aSize = getSampleSize();
		int aComplementSize = itsBase.getSampleSize() - getSampleSize();
		if (aSize <= 2 || aComplementSize <=2) // either sample is too small
			return 0;
		else
			return Math.abs(getComplementCorrelation() - getCorrelation());
	}

	public int getSampleSize() { return itsSampleSize; }
	public double getXSum()	{ return itsXSum; }
	public double getYSum()	{ return itsYSum; }
	public double getXYSum() { return itsXYSum; }
	public double getXSquaredSum() { return itsXSquaredSum;	}
	public double getYSquaredSum() { return itsYSquaredSum;	}

	// > and < tests on 2 NaNs return false, so method returns 0 as expected
	public int compareTo(CorrelationMeasure theOtherCorrelationMeasure)
	{
		final double thiz = this.getEvaluationMeasureValue();
		final double that = theOtherCorrelationMeasure.getEvaluationMeasureValue();

		return (thiz < that) ? -1 : (thiz > that) ? 1 : 0;
	}

	/**
	 * There are different types of {@link QualityMeasure}s possible, all
	 * closely related to the correlation value.
	 * 
	 * Corresponding with the correct type as defined in the constructor,
	 * the correct {@link QualityMeasure} value is returned.
	 *
	 * @return the quality measure value
	 */
	public double getEvaluationMeasureValue()
	{
		final double aCorrelation = getCorrelation();
		switch (itsType)
		{
			case CORRELATION_R:		{ return aCorrelation; }
			case CORRELATION_R_NEG:		{ return -aCorrelation; }
			case CORRELATION_R_SQ:		{ return aCorrelation*aCorrelation;}
			case CORRELATION_R_NEG_SQ:	{ return -(aCorrelation*aCorrelation); }
			case CORRELATION_DISTANCE:	{ return computeCorrelationDistance(); }
			case CORRELATION_P:		{ return getPValue(); }
			case CORRELATION_ENTROPY:	{ return computeEntropy(); }
			case ADAPTED_WRACC:		{ return computeAdaptedWRAcc(); } //Done: Rob Konijn
			case COSTS_WRACC:		{ return computeCostsWRAcc(); } //Done: Rob Konijn
			default :
			{
				// validity of QM is checked by constructor
				throw new AssertionError(itsType);
			}
		}
	}

	/**
	 * @param theFirstValue the first parameter to be compared
	 * @param theSecondValue the second parameter to be compared
	 * 
	 * @return <code>true</code> when the first parameter is better or equal
	 * than the second, <code>false</code> if the second value is better
	 */
	// TODO: Make use of CorrelationMeasure being comparable
	// MM: not sure what is meant by this TODO, but explicit handling of NaN
	// values makes this method different from (current) compareTo()
	@Deprecated // never used
	public static boolean compareEMValues(double theFirstValue, double theSecondValue)
	{
		if(Double.isNaN(theSecondValue))
			return true;
		else if(Double.isNaN(theFirstValue))
			return false;
		else
			return theFirstValue>=theSecondValue;
	}

	/**
	 * Returns the correlation value for the complement of this subset.
	 * Computes this value if it is not calculated yet and simply returns
	 * the value if it is.
	 *
	 * @return complement correlation value
	 */
	public double getComplementCorrelation()
	{
		if(itsComplementIsOutdated)
			return computeComplementCorrelation();
		else
			return itsComplementCorrelation;
	}

	/**
	 * Calculates the correlation value for the complement set.
	 * 
	 * @return the complement correlation value
	 */
	private double computeComplementCorrelation()
	{
		if(itsBase!=null)
		{
			double aSampleSize = itsBase.getSampleSize() - getSampleSize();
			double anXSum = itsBase.getXSum() - getXSum();
			double aYSum = itsBase.getYSum() - getYSum();
			double anXYSum = itsBase.getXYSum() - getXYSum();
			double anXSquaredSum = itsBase.getXSquaredSum() - getXSquaredSum();
			double aYSquaredSum = itsBase.getYSquaredSum() - getYSquaredSum();
			itsComplementCorrelation = (aSampleSize*anXYSum - anXSum*aYSum)/Math.sqrt((aSampleSize*anXSquaredSum - anXSum*anXSum) * (aSampleSize*aYSquaredSum - aYSum*aYSum));
			itsComplementIsOutdated = false; //Correlation for the complement is up to date, till the next observation is added
			return itsComplementCorrelation;
		}
		return Double.NaN;
	}

	/**
	 * Return the {@code p-value} for this CorrelationMeasure and its base.
	 * 
	 * @return the {@code p-value}, or {@code 0.0} if either the base or
	 * the sample is {@code <= 2}.
	 */
	// TODO Verify whether solution is the same when z1 - z2 and z2 - z1
	public double getPValue()
	{
		int aSize = getSampleSize();
		int aComplementSize = itsBase.getSampleSize() - getSampleSize();
		if (aSize <= 2 || aComplementSize <= 2) // either sample is too small
			return 0.0;

		NormalDistribution aNormalDistro = new NormalDistribution();
		double aComplementSampleSize = itsBase.getSampleSize() - getSampleSize();;
		double aSubgroupDeviation = 1.0 / Math.sqrt(getSampleSize() - 3);
		double aComplementDeviation = 1.0 / Math.sqrt(aComplementSampleSize - 3);
		double aZScore = (transform2FisherScore(getCorrelation()) - transform2FisherScore(getComplementCorrelation()))
						 / (aSubgroupDeviation+aComplementDeviation);

		//[example:] z = (obs - mean)/std = (0.9730 - 0.693)/0.333 = 0.841
		double anErfValue = aNormalDistro.calcErf(aZScore/Math.sqrt(2.0));
		double aPValue = 0.5*(1.0 + anErfValue);

		return (aPValue>0.5) ? aPValue : (1.0-aPValue);
	}

	private double transform2FisherScore(double theCorrelationValue)
	{
		//z' = 0.5 ln[(1+r)/(1-r)]
		return 0.5 * Math.log((1+theCorrelationValue)/(1-theCorrelationValue));
	}

	/**
	 * The correlation distance between the subset and its complement is
	 * weighted with the entropy.
	 * 
	 * The entropy is defined by the function H(p) = -p*log(p).
	 * 
	 * @return weighted correlation distance
	 */
	public double computeEntropy()
	{
		double aCorrelation = computeCorrelationDistance();
		if (aCorrelation == 0.0)
			return 0.0;
		double aFraction;
		aFraction = itsBase!=null ? itsSampleSize / (double) itsBase.getSampleSize() : 1.0;
		double aWeight = -1.0 * aFraction * Math.log(aFraction) / Math.log(2);
		return aWeight * aCorrelation;
	}

	public double computeAdaptedWRAcc()
	{
		return (itsXSum/itsSampleSize - itsYSum/itsSampleSize) * itsSampleSize/itsBase.itsSampleSize;
	}

	/**
	 * Computes the WRAcc multiplied by difference in costs between target
	 * in subgroup and its complement (rest of the data), useful for fraud
	 * detection.
	 * 
	 * @return some number
	 */
	public double computeCostsWRAcc()
	{
		// TODO should use more braces and line out, hard to read
		return (itsXSum - itsSampleSize*itsBase.itsXSum/itsBase.itsSampleSize) * ((itsXYSum/itsXSum) - (itsBase.itsYSum-itsYSum)/(itsBase.itsSampleSize-itsSampleSize));
	}
}
