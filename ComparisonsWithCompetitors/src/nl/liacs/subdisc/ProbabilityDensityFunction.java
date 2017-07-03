package nl.liacs.subdisc;

import java.util.*;

import nl.liacs.subdisc.gui.*;

public class ProbabilityDensityFunction
{
	private final int DEFAULT_NR_BINS = 1000;

	private final Column itsData;
	private float[] itsDensity;
	// future update may allow custom number of bins
	private int itsNrBins = DEFAULT_NR_BINS;
	private float itsMin, itsMax, itsBinWidth;

	//create from entire dataset
	public ProbabilityDensityFunction(Column theData)
	{
		itsData = theData;
		itsDensity = new float[itsNrBins];
		//TODO include outlier treatment
		itsMin = itsData.getMin();
		itsMax = itsData.getMax();
		itsBinWidth = (itsMax-itsMin)/itsNrBins;

		//Log.logCommandLine("Min = " + itsMin);
		//Log.logCommandLine("Max = " + itsMax);
		//Log.logCommandLine("BinWidth = " + itsBinWidth);
		int aSize = itsData.size();
		float anIncrement = 1.0f / aSize;
		for (int i=0; i<aSize; i++)
		{
			float aValue = itsData.getFloat(i);
			add(aValue, anIncrement);
		}
	}

	//create for subgroup, relative to existing PDF (use same Column data)
	public ProbabilityDensityFunction(ProbabilityDensityFunction thePDF, BitSet theMembers)
	{
		itsData = thePDF.itsData;
		itsDensity = new float[thePDF.itsNrBins];
		itsMin = thePDF.itsMin;
		itsMax = thePDF.itsMax;
		itsBinWidth = thePDF.itsBinWidth;

		float anIncrement = 1.0f / theMembers.cardinality();
		for (int i = theMembers.nextSetBit(0); i >= 0; i = theMembers.nextSetBit(i + 1))
			add(thePDF.itsData.getFloat(i), anIncrement);
	}

	public float getDensity(float theValue)
	{
		return getDensity(getIndex(theValue));
	}

	public float getDensity(int theIndex)
	{
		return itsDensity[theIndex];
	}

	private int getIndex(float aValue)
	{
		if (aValue == itsMax)
			return itsNrBins-1;
		else
			return (int) ((aValue-itsMin)/itsBinWidth);
	}

	public float getMiddle(int theIndex)
	{
		return itsMin + (theIndex + 0.5f)*itsBinWidth;
	}

	/*
	 * FIXME
	 * accumulates rounding errors, alternative would be to just count the
	 * absolute number of items in a bin and report the density for a
	 * particular bin as: (bin_nr_items / total_nr_items)
	 */
	private void add(float theValue, float theIncrement)
	{
		int aBin = getIndex(theValue);
		itsDensity[aBin] += theIncrement;
	}

	public void print()
	{
		Log.logCommandLine("ProbabilityDensityFunction:\n");
		for (int i = 0; i < itsDensity.length; i++)
			Log.logCommandLine("  " + i + "	" + itsDensity[i]);
		Log.logCommandLine("");
	}

	public int size()
	{
		return itsNrBins;
	}

	private static final double CUTOFF = 4.0;	// for now
	public static double[] getGaussianDistribution(double theSigma)
	{
		if (theSigma <= 0.0 || Double.isInfinite(theSigma) || Double.isNaN(theSigma))
			throw new IllegalArgumentException("Invalid sigma: " + theSigma);

		// mu = 0.0
		int aWidth = (int)(2.0 * CUTOFF * theSigma);
		double[] aKernel = new double[aWidth];
		double aCorrection = 0.0;	// to set AUC to 1.0

		int halfWidth = aWidth / 2;
		double variance = theSigma * theSigma;
		double doubleVariance = 2.0 * variance;
		double factor = 1.0 / Math.sqrt(Math.PI * doubleVariance);

		// NOTE this is Arno's simplified code for TimeSeries.Gaussian
		// it does no guarantee symmetry like the old code did
		// as is computes each x value twice for [-x , midpoint, +x]
		for (int i = 0, j = aWidth; i < j; ++i)
		{
			double anX = i-halfWidth;
			double anXSquared = -anX*anX;
			double aValue = factor * Math.exp(anXSquared / doubleVariance);
			aKernel[i] = aValue;
			aCorrection += aValue;
		}

		// correct all values such that they sum to 1.0
		// NOTE rounding errors may still prevent this
		for (int i = 0, j = aWidth; i < j; ++i)
			aKernel[i] /= aCorrection;

		return aKernel;
	}

	/**
	 * Smooths the density histogram using a default &sigma; that is
	 * determined as follows:</br>
	 * &sigma; = (max - min) / 64, where min and max are the minimum and
	 * maximum value in the data that was used to build this
	 * ProbabilityDensityFunction.</br>
	 * To use a different &sigma;, use {@link #smooth(float)}.
	 * <p>
	 * NOTE that any smooth method is destructive in the sense that after
	 * smoothing the density histogram is altered, and can not be returned
	 * to the state it was in before the operation.
	 *
	 * @return The smoothed histogram for this ProbabilityDensityFunction.
	 *
	 * @see #smooth(float)
	 */
	public float[] smooth()
	{
		return smooth((itsMax-itsMin) / 64);
	}

	// can not be applied if width > theInput
	public float[] smooth(float theSigma)
	{
		final int length = itsNrBins;

		double aSigma = theSigma/itsBinWidth;
		final double[] aKernel = getGaussianDistribution(aSigma);

		// initialised to 0.0
		final float[] anOutput = new float[length];

		// values where no full window/kernel can be applied
		final int aWidth = aKernel.length;
		final int halfWidth = aWidth / 2;

		double aCorrection;
		for (int i = 0, j = halfWidth; i < j; ++i)
		{
			aCorrection = 0.0;
			for (int k = halfWidth+i, n = 0; k >= 0; --k, ++n)
			{
				aCorrection += aKernel[k];
				anOutput[i] += (aKernel[k] * itsDensity[n]);
			}
			anOutput[i] /= aCorrection;
		}
		for (int i = length-1, j = 0; j < halfWidth; --i, ++j)
		{
			aCorrection = 0.0;
			for (int k = halfWidth+j, n = length-1; k >= 0; --k, --n)
			{
				aCorrection += aKernel[k];
				anOutput[i] += aKernel[k] * itsDensity[n];
			}
			anOutput[i] /= aCorrection;
		}

		// apply kernel on theInput
		for (int i = halfWidth, j = length - halfWidth; i < j; ++i)
			for (int k = 0, m = aWidth, n = i-halfWidth; k < m; ++k, ++n)
				anOutput[i] += (aKernel[k] * itsDensity[n]);

		itsDensity = anOutput;
		return anOutput;
	}

	public static void main(String[] args)
	{
		int nrRows = 100;
		Column c = new Column("TEST", "TEST", AttributeType.NUMERIC, 0, nrRows);
		c.add(0.0f);
		for (int i = 0; i < 49; ++i)
			c.add(30.0f);
		for (int i = 0; i < 49; ++i)
			c.add(70.0f);
		c.add(100.0f);
		c.print();
		System.out.println();

		ProbabilityDensityFunction pdf;
		for (int i = 1; i <= 5; i+=2)
		{

			pdf = new ProbabilityDensityFunction(c);
			System.out.println(Arrays.toString((pdf.itsDensity)));
			plot("pre smooth, sigma = " + i, pdf.itsDensity);
			pdf.smooth(i);
			System.out.println(Arrays.toString((pdf.itsDensity)));
			plot("post smooth, sigma = " + i, pdf.itsDensity);
			System.out.println();
		}

		// uniform probability, all bins of equal height (30 = itsNrBins)
		int nrBins = 30;
		c = new Column("UNIFORM", "UNIFORM", AttributeType.NUMERIC, 0, nrBins);
		for (int i = 0, j = nrBins; i < j; ++i)
			c.add(i);
		c.print();
		System.out.println();

		pdf = new ProbabilityDensityFunction(c);
		System.out.println(Arrays.toString((pdf.itsDensity)));
		plot("UNIFORM pre smooth", pdf.itsDensity);
		pdf.smooth(3);
		System.out.println(Arrays.toString((pdf.itsDensity)));
		plot("UNIFORM post smooth", pdf.itsDensity);
		System.out.println();
	}

	private static void plot(String theName, float[] theDensity)
	{
		final Column c = new Column(theName, theName, AttributeType.NUMERIC, 0, theDensity.length);
		for (float f : theDensity)
			c.add(f);
		new PlotWindow(c);
	}
}
