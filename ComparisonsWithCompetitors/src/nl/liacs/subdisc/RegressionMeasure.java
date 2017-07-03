package nl.liacs.subdisc;

import java.awt.geom.*;
import java.util.*;
import Jama.*;

public class RegressionMeasure
{
	private int itsSampleSize;
	private double itsXSum; //The sum of all X-values
	private double itsYSum; // The sum of all Y-values
	private double itsXYSum;// SUM(x*y)
	private double itsXSquaredSum;// SUM(x*x)
	private double itsYSquaredSum;// SUM(y*y)

	private double itsErrorTermSquaredSum;//Sum of all the squared error terms, changes whenever the regression function is updated
	private double itsComplementErrorTermSquaredSum;//Sum of all the squared error terms of this complement, changes whenever the regression function is updated

	private double itsSlope; //The slope-value of the regression function
	private double itsIntercept;//The intercept-value of the regression function

	private double itsCorrelation;

	private List<Point2D.Float> itsData;//Stores all the datapoints for this measure
	private List<Point2D.Float> itsComplementData = new ArrayList<Point2D.Float>();//Stores all the datapoints for the complement

	public static QM itsQualityMeasure; // FIXME MM this should not be static
	private RegressionMeasure itsBase = null;

	private Matrix itsDataMatrix;
	private Matrix itsBetaHat;
	private Matrix itsHatMatrix;
	private Matrix itsResidualMatrix;

	private double itsP;
	private double itsSSquared;
	private double[] itsRSquared;
	private double[] itsT;
	private double[] itsSVP;

	private int itsBoundSevenCount;
	private int itsBoundSixCount;
	private int itsBoundFiveCount;
	private int itsBoundFourCount;
	private int itsRankDefCount;

	//make a base model from two columns
	public RegressionMeasure(QM theType, Column thePrimaryColumn, Column theSecondaryColumn)
	{
		itsQualityMeasure = theType;
		itsSampleSize = thePrimaryColumn.size();
		itsData = new ArrayList<Point2D.Float>(itsSampleSize);
		for(int i=0; i<itsSampleSize; i++)
		{
			itsXSum += thePrimaryColumn.getFloat(i);
			itsYSum += theSecondaryColumn.getFloat(i);
			itsXYSum += thePrimaryColumn.getFloat(i)*theSecondaryColumn.getFloat(i);
			itsXSquaredSum += thePrimaryColumn.getFloat(i)*thePrimaryColumn.getFloat(i);
			itsYSquaredSum += theSecondaryColumn.getFloat(i)*theSecondaryColumn.getFloat(i);

			itsData.add(new Point2D.Float(thePrimaryColumn.getFloat(i), theSecondaryColumn.getFloat(i)) );
		}

		switch (itsQualityMeasure)
		{
			case LINEAR_REGRESSION:
			{
				itsBase = null; //this *is* the base
				itsComplementData = null; //will remain empty for the base RM
				updateRegressionFunction();
				updateErrorTerms();
				break;
			}
			case COOKS_DISTANCE:
			{
				updateRegressionFunction(); //updating error terms unnecessary since Cook's distance does not care

				double[][] aData = new double[itsSampleSize][2];
				double[][] anXValues = new double[itsSampleSize][2];
				for (int i=0; i<itsSampleSize; i++){
					anXValues[i][0]=1;
					anXValues[i][1]=itsData.get(i).getX();
					aData[i][0] = anXValues[i][1];
				}
				double[][] aYValues = new double[itsSampleSize][1];
				for (int i=0; i<itsSampleSize; i++)
				{
					aYValues[i][0]=itsData.get(i).getY();
					aData[i][1] = aYValues[i][0];
				}
				Matrix anXMatrix = new Matrix(anXValues);
				Matrix aYMatrix = new Matrix(aYValues);

				itsDataMatrix = new Matrix(aData);
				itsBetaHat = (anXMatrix.transpose().times(anXMatrix)).inverse().times(anXMatrix.transpose()).times(aYMatrix);
				itsHatMatrix = anXMatrix.times((anXMatrix.transpose().times(anXMatrix)).inverse()).times(anXMatrix.transpose());
				itsResidualMatrix = (Matrix.identity(itsSampleSize,itsSampleSize).minus(itsHatMatrix)).times(aYMatrix);

				itsP = itsBetaHat.getRowDimension();
				itsSSquared = (itsResidualMatrix.transpose().times(itsResidualMatrix)).get(0,0)/((double) itsSampleSize-itsP);

				//fill R^2 array
				double[] aResiduals = new double[itsSampleSize];
				for (int i=0; i<itsSampleSize; i++)
					aResiduals[i] = itsResidualMatrix.get(i,0)*itsResidualMatrix.get(i,0);
				Arrays.sort(aResiduals);
				//now we have squared residuals in ascending order, but we want array[i] = sum(array[i]...array[n])
				for (int i=itsSampleSize-2; i>=0; i--)
					aResiduals[i] += aResiduals[i+1];
				itsRSquared = aResiduals;

				//fill T array
				double[] aT = new double[itsSampleSize];
				for (int i=0; i<itsSampleSize; i++)
					aT[i] = itsHatMatrix.get(i,i);
				Arrays.sort(aT);
				for (int i=itsSampleSize-2; i>=0; i--)
					aT[i] += aT[i+1];
				itsT = aT;

				//fill SVP array; this would be Cook's distance when removing only single points. Cf. Cook&Weisberg p.117, Cook1977a
				double[] aSVP = new double[itsSampleSize];
				for (int i=0; i<itsSampleSize; i++)
					aSVP[i] = itsResidualMatrix.get(i,0)*itsResidualMatrix.get(i,0)/itsP * itsHatMatrix.get(i,i)/(1-itsHatMatrix.get(i,i));
				itsSVP = aSVP;

				//initialize bounds
				itsBoundSevenCount=0;
				itsBoundSixCount=0;
				itsBoundFiveCount=0;
				itsBoundFourCount=0;
				itsRankDefCount=0;
				break;
			}
			default :
			{
				/*
				 * if the QM is valid for this TargetType
				 * 	it is not implemented here
				 * else
				 * 	this method should not have been called
				 */
				if (QM.getQualityMeasures(TargetType.DOUBLE_REGRESSION).contains(itsQualityMeasure))
					throw new AssertionError(itsQualityMeasure);
				else
					throw new IllegalArgumentException("Invalid argument: " + itsQualityMeasure);
			}
		}
	}

	//constructor for non-base RM. It derives from a base-RM
	public RegressionMeasure(RegressionMeasure theBase, BitSet theMembers)
	{
		itsQualityMeasure = theBase.itsQualityMeasure;
		itsBase = theBase;

		//Create an empty measure
		itsSampleSize = 0;
		itsXSum = 0;
		itsYSum = 0;
		itsXYSum = 0;
		itsXSquaredSum = 0;
		itsYSquaredSum = 0;

		itsData = new ArrayList<Point2D.Float>(theMembers.cardinality());
		itsComplementData =
			new ArrayList<Point2D.Float>(itsBase.getSampleSize() - theMembers.cardinality()); //create empty one. will be filled after update()

		for (int i=0; i<itsBase.getSampleSize(); i++)
		{
			Point2D.Float anObservation = itsBase.getObservation(i);
			if (theMembers.get(i))
				addObservation(anObservation);
			else //complement
				itsComplementData.add(anObservation);
		}
	}

	//TODO test and verify method
	public double getEvaluationMeasureValue()
	{
		updateRegressionFunction();
		updateErrorTerms();
		return getSSD();
	}

	//TODO turn this t-value into a p-value.
	public double getSSD()
	{
		//determine the sums for the complement
		double aComplementXSum = itsBase.getXSum()-itsXSum;
		double aComplementYSum = itsBase.getYSum()-itsYSum;
		double aComplementXSquaredSum = itsBase.getXSquaredSum()-itsXSquaredSum;
		double aComplementXYSum = itsBase.getXYSum()-itsXYSum;
		double aComplementSampleSize = itsBase.getSampleSize()-itsSampleSize;

		//determine variance for the distribution
		double aNumerator = getErrorTermVariance(itsErrorTermSquaredSum, itsSampleSize);
		double aDenominator = itsXSquaredSum - 2*itsXSum*itsXSum/itsSampleSize + itsXSum*itsXSum/itsSampleSize;
		double aVariance = aNumerator / aDenominator;

		//if we divided by zero along the way, we are considering a degenerate candidate subgroup, hence quality=0
		if (itsSampleSize==0 || itsSampleSize==2 || aDenominator==0)
			return 0;

		//determine variance for the complement distribution
		aNumerator = getErrorTermVariance(itsComplementErrorTermSquaredSum, aComplementSampleSize);
		aDenominator = aComplementXSquaredSum - 2*aComplementXSum*aComplementXSum/aComplementSampleSize + aComplementXSum*aComplementXSum/aComplementSampleSize;
		double aComplementVariance = aNumerator/aDenominator;

		//if we divided by zero along the way, we are considering a degenerate candidate subgroup complement, hence quality=0
		if (aComplementSampleSize==0 || aComplementSampleSize==2 || aDenominator==0)
			return 0;

		//calculate the difference between slopes of this measure and its complement
		double aSlope = getSlope(itsXSum, itsYSum, itsXSquaredSum, itsXYSum, itsSampleSize);
		double aComplementSlope = getSlope(aComplementXSum, aComplementYSum, aComplementXSquaredSum, aComplementXYSum, aComplementSampleSize);
		double aSlopeDifference = Math.abs(aComplementSlope - aSlope);

		Log.logCommandLine("\n           slope: " + aSlope);
		Log.logCommandLine("complement slope: " + aComplementSlope);
		Log.logCommandLine("           variance: " + aVariance);
		Log.logCommandLine("complement variance: " + aComplementVariance);

		if (aVariance+aComplementVariance==0)
			return 0;
		else {return aSlopeDifference / Math.sqrt(aVariance+aComplementVariance);}
	}

	public double calculate(Subgroup theNewSubgroup)
	{
		BitSet aMembers = theNewSubgroup.getMembers();
		int aSampleSize = aMembers.cardinality();

		//filter out rank deficient model that crash matrix multiplication library
		if (aSampleSize<2)
		{
			itsRankDefCount++;
			return Double.MIN_VALUE;
		}

		//calculate the upper bound values. Before each bound, only the necessary computations are done.
		double aT = itsT[aSampleSize];
		double aRSquared = itsRSquared[aSampleSize];

		double aBoundSeven = computeBoundSeven(aSampleSize, aT, aRSquared);
		if (aBoundSeven>Double.MIN_VALUE)
		{
			Log.logCommandLine("                   Bound 7: " + aBoundSeven);
			itsBoundSevenCount++;
		}

		int[] anIndices = new int[aSampleSize];
		int[] aRemovedIndices = new int[itsSampleSize-aSampleSize];
		int anIndex=0;
		int aRemovedIndex=0;
		for (int i=0; i<itsSampleSize; i++)
		{
			if (aMembers.get(i))
			{
				anIndices[anIndex] = i;
				anIndex++;
			}
			else
			{
				aRemovedIndices[aRemovedIndex] = i;
				aRemovedIndex++;
			}
		}

		Matrix aRemovedResiduals = itsResidualMatrix.getMatrix(aRemovedIndices,0,0);
		double aSquaredResidualSum = squareSum(aRemovedResiduals);
		double aBoundSix = computeBoundSix(aSampleSize, aT, aSquaredResidualSum);
		if (aBoundSix>Double.MIN_VALUE)
		{
			Log.logCommandLine("                   Bound 6: " + aBoundSix);
			itsBoundSixCount++;
		}

		Matrix aRemovedHatMatrix = itsHatMatrix.getMatrix(aRemovedIndices,aRemovedIndices);
		double aRemovedTrace = aRemovedHatMatrix.trace();
		double aBoundFive = computeBoundFive(aSampleSize, aRemovedTrace, aRSquared);
		if (aBoundFive>Double.MIN_VALUE)
		{
			Log.logCommandLine("                   Bound 5: " + aBoundFive);
			itsBoundFiveCount++;
		}

		double aBoundFour = computeBoundFour(aSampleSize, aRemovedTrace, aSquaredResidualSum);
		if (aBoundFour>Double.MIN_VALUE)
		{
			Log.logCommandLine("                   Bound 4: " + aBoundFour);
			itsBoundFourCount++;
		}

		//compute estimate based on projection of single influence values
		double anSVPDistance = computeSVPDistance(itsSampleSize-aSampleSize, aRemovedIndices);
		Log.logCommandLine("                   SVP est: " + anSVPDistance);

		//start computing Cook's Distance
		Matrix aNewDataMatrix = itsDataMatrix.getMatrix(anIndices,0,1);

		//filter out rank-deficient cases; these regressions cannot be computed, hence low quality
		LUDecomposition itsDecomp = new LUDecomposition(aNewDataMatrix);
		if (!itsDecomp.isNonsingular())
		{
			itsRankDefCount++;
			return Double.MIN_VALUE;
		}

		//make submatrices
		double[][] anXValues = new double[aSampleSize][2];
		for (int i=0; i<aSampleSize; i++)
		{
			anXValues[i][0]=1;
			anXValues[i][1]=aNewDataMatrix.get(i,0);
		}
		double[][] aYValues = new double[aSampleSize][1];
		for (int i=0; i<aSampleSize; i++)
			aYValues[i][0]=aNewDataMatrix.get(i,1);
		Matrix anXMatrix = new Matrix(anXValues);
		Matrix aYMatrix = new Matrix(aYValues);

		//compute regression
		Matrix aBetaHat = (anXMatrix.transpose().times(anXMatrix)).inverse().times(anXMatrix.transpose()).times(aYMatrix);
		Matrix aHatMatrix = anXMatrix.times((anXMatrix.transpose().times(anXMatrix)).inverse()).times(anXMatrix.transpose());
		Matrix aResidualMatrix = (Matrix.identity(aSampleSize,aSampleSize).minus(aHatMatrix)).times(aYMatrix);

		//compute Cook's distance
		double aP = aBetaHat.getRowDimension();
		double anSSquared = (aResidualMatrix.transpose().times(aResidualMatrix)).get(0,0)/((double) itsSampleSize-aP);
		double[][] aParentValues = {{itsIntercept},{itsSlope}};
		Matrix aParentBetaHat = new Matrix(aParentValues);

		double aQuality = aBetaHat.minus(aParentBetaHat).transpose().times(anXMatrix.transpose()).times(anXMatrix).times(aBetaHat.minus(aParentBetaHat)).get(0,0)/(aP*anSSquared);
		//N.B.: Temporary line for fetching Cook's experimental statistics
		Log.logRefinement(""+aQuality+","+anSVPDistance+","+aSampleSize);
		return aQuality;
	}

	private double computeBoundSeven(int theSampleSize, double theT, double theRSquared)
	{
		if (theT>=1)
			return Double.MIN_VALUE;
		return theT/((1-theT)*(1-theT))*theRSquared/(itsP*itsSSquared);
	}

	private double computeBoundSix(int theSampleSize, double theT, double theSquaredResidualSum)
	{
		if (theT>=1)
			return Double.MIN_VALUE;
		return theT/((1-theT)*(1-theT))*theSquaredResidualSum/(itsP*itsSSquared);
	}

	private double computeBoundFive(int theSampleSize, double theRemovedTrace, double theRSquared)
	{
		if (theRemovedTrace>=1)
			return Double.MIN_VALUE;
		return theRemovedTrace/((1-theRemovedTrace)*(1-theRemovedTrace))*theRSquared/(itsP*itsSSquared);
	}

	private double computeBoundFour(int theSampleSize, double theRemovedTrace, double theSquaredResidualSum)
	{
		if (theRemovedTrace>=1)
			return Double.MIN_VALUE;
		return theRemovedTrace/((1-theRemovedTrace)*(1-theRemovedTrace))*theSquaredResidualSum/(itsP*itsSSquared);
	}

	private double computeSVPDistance(int theNrRemoved, int[] theIndices)
	{
		double result = 0.0;
		for (int i=0; i<theNrRemoved; i++)
			result += itsSVP[theIndices[i]];
		return result;
	}

	private double squareSum(Matrix itsMatrix)
	{
		int aSampleSize = itsMatrix.getRowDimension();
		double[] itsValues = itsMatrix.getRowPackedCopy();
		double aSum = 0.0;
		for (int i=0; i<aSampleSize; i++)
			aSum += itsValues[i]*itsValues[i];
		return aSum;
	}

	/**
	 * Updates the slope and intercept of the regression function.
	 * Function used to determine slope:
	 * b = SUM( (x_n - x_mean)*(y_n - y_mean) ) / SUM( (x_n - x_mean)*(x_n - x_mean) )
	 * this can be rewritten to
	 * b = ( SUM(x_n*y_n) - x_mean*y_sum - y_mean*x_sum + n*x_mean*y_mean ) / ( SUM(x_n*x_n) - 2*x_mean*x_sum + n*x_mean*x_mean )
	 *
	 */
	private void updateRegressionFunction()
	{
		double aXMean = itsXSum / itsSampleSize;
		double aYMean = itsYSum / itsSampleSize;
		itsSlope = getSlope(itsXSum, itsYSum, itsXSquaredSum, itsXYSum, itsSampleSize);
		itsIntercept = aYMean - itsSlope*aXMean;
	}

	private double getSlope(double theXSum, double theYSum, double theXSquaredSum, double theXYSum, double theSampleSize)
	{
		double aXMean = theXSum / theSampleSize;
		double aYMean = theYSum / theSampleSize;
		double aNumerator = theXYSum - aXMean*theYSum - aYMean*theXSum + theSampleSize*aXMean*aYMean;
		double aDenominator = theXSquaredSum - 2*aXMean*theXSum + theXSum*aXMean;
		return aNumerator/aDenominator;
	}

	/**
	 * Add a new datapoint to this measure, where the Y-value is the target variable.
	 * Always call update() after all datapoints have been added.
	 * @param theY the Y-value, the target
	 * @param theX the X-value
	 */
	// never used
	@Deprecated
	private void addObservation(float theY, float theX)
	{
		//adjust the sums
		itsSampleSize++;
		itsXSum += theX;
		itsYSum += theY;
		itsXYSum += theX*theY;
		itsXSquaredSum += theX*theX;
		itsYSquaredSum += theY*theY;

		//Add to its own lists
		Point2D.Float aDataPoint = new Point2D.Float(theX,theY);
		itsData.add(aDataPoint);
	}

	private void addObservation(Point2D.Float theObservation)
	{
		float anX = (float) theObservation.getX();
		float aY = (float) theObservation.getY();

		//adjust the sums
		itsSampleSize++;
		itsXSum += anX;
		itsYSum += aY;
		itsXYSum += anX*aY;
		itsXSquaredSum += anX*anX;
		itsYSquaredSum += aY*aY;

		//Add to its own lists
		itsData.add(theObservation);
	}

	private Point2D.Float getObservation(int theIndex)
	{
		return itsData.get(theIndex);
	}

	/**
	 * calculates the error terms for the distribution and recomputes the
	 * sum of the squared error term
	 *
	 */
	private void updateErrorTerms()
	{
		itsErrorTermSquaredSum = 0;
		for(int i=0; i<itsSampleSize; i++)
		{
			double anErrorTerm = getErrorTerm(itsData.get(i));
			itsErrorTermSquaredSum += anErrorTerm*anErrorTerm;
		}

		//update the error terms of the complement of this measure, if present
		if(itsBase!=null)
		{
			itsComplementErrorTermSquaredSum=0;
			for(int i=0; i<(itsBase.getSampleSize()-itsSampleSize); i++)
			{
				if(itsComplementData.size()!=itsBase.getSampleSize()-itsSampleSize)
					System.err.println("incorrect computation of complement!");
				double anErrorTerm = getErrorTerm(itsComplementData.get(i));
				itsComplementErrorTermSquaredSum += anErrorTerm*anErrorTerm;
			}
		}

	}

	/**
	 * Determine the error term for a given point
	 *
	 * @param theX the x-value
	 * @param theY the y-value
	 * @return the error term
	 */
	private double getErrorTerm(double theX, double theY)
	{
		return theY - (itsSlope*theX+itsIntercept);
	}

	private double getErrorTerm(Point2D.Float theDataPoint)
	{
		return getErrorTerm(theDataPoint.getX(), theDataPoint.getY());
	}

	private double getErrorTermVariance(double theErrorTermSquaredSum, double theSampleSize)
	{
		return theErrorTermSquaredSum / (theSampleSize - 2 );
	}

	private int getSampleSize()
	{
		return itsSampleSize;
	}

	private double getXSum()
	{
		return itsXSum;
	}

	private double getYSum()
	{
		return itsYSum;
	}

	private double getXYSum()
	{
		return itsXYSum;
	}

	private double getXSquaredSum()
	{
		return itsXSquaredSum;
	}

	// never used
	@Deprecated
	private double getYSquaredSum()
	{
		return itsYSquaredSum;
	}

	/**
	 * Computes and returns the correlation given the observations contained
	 * by CorrelationMeasure.
	 *
	 * @return the computed correlation
	 */
	// never used
	@Deprecated
	private double getCorrelation()
	{
		itsCorrelation = (itsSampleSize*itsXYSum - itsXSum*itsYSum)/Math.sqrt((itsSampleSize*itsXSquaredSum - itsXSum*itsXSum) * (itsSampleSize*itsYSquaredSum - itsYSum*itsYSum));
		//itsCorrelationIsOutdated = false; //set flag to false, so subsequent calls to getCorrelation don't need anymore computation.
		return itsCorrelation;
	}

	public double getSlope()
	{
		return itsSlope;
	}

	public double getIntercept()
	{
		return itsIntercept;
	}

	public double getBaseFunctionValue(double theX)
	{
		return theX*itsSlope + itsIntercept;
	}

	public int getNrBoundSeven() { return itsBoundSevenCount; }
	public int getNrBoundSix() { return itsBoundSixCount; }
	public int getNrBoundFive() { return itsBoundFiveCount; }
	public int getNrBoundFour() { return itsBoundFourCount; }
	public int getNrRankDef() { return itsRankDefCount; }
}
