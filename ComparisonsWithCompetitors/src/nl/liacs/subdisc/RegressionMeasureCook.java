package nl.liacs.subdisc;

import java.awt.geom.*;
import java.text.*;
import java.util.*;
import Jama.*;

public class RegressionMeasureCook
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

	// Stores all the datapoints for this measure
	//private List<Point2D.Float> itsData;
	// Stores all the datapoints for the complement // TODO initialise in constructor
	//private List<Point2D.Float> itsComplementData = new ArrayList<Point2D.Float>();

	public static QM itsQualityMeasure;
	private RegressionMeasure itsBase = null;

	private Matrix itsXMatrix;
	private Matrix itsYMatrix;
	private Matrix itsXTXInverseMatrix;
	private Matrix itsZMatrix;
	private Matrix itsBetaHat;
	private Matrix itsHatMatrix;
	private Matrix itsResidualMatrix;

	private double itsP;
	private double itsQ;
	private double itsSSquared;
	private double[] itsRSquared;
	private double[] itsT;
	private double[] itsSVP;

	private int[] itsIndices;
	private int[] itsRemovedIndices;

	private double itsSquaredResidualSum;
	private double itsRemovedTrace;

	private int itsI; // = itsNrSecondaryTargets
	private int itsJ; // = itsNrTertiaryTargets
						// maar dat ga ik niet iedere keer uittypen.

	private boolean itsInterceptRelevance;

	private String itsPrimaryName;
	private List<String> itsSecondaryNames;
	private List<String> itsTertiaryNames;

	private String itsGlobalModel;
	private static final DecimalFormat aDf = new DecimalFormat("#.#####");

	//make a base model from multiple columns
	public RegressionMeasureCook(QM theType, TargetConcept theTargetConcept)
	{//TODO MM: Either remove legacy code, or make something decent out of it. For now, it is hacked.
		//get target data
		Column aPrimaryTarget = theTargetConcept.getPrimaryTarget();
		List<Column> aSecondaryTargets = theTargetConcept.getSecondaryTargets();
		List<Column> aTertiaryTargets = theTargetConcept.getTertiaryTargets();

		itsI = aSecondaryTargets.size();
		itsJ = aTertiaryTargets.size();
		itsP = 1+itsI+itsJ;
		itsQ = itsI;
		itsInterceptRelevance = theTargetConcept.getInterceptRelevance();
		if (itsInterceptRelevance)
			itsQ++;

		itsPrimaryName = aPrimaryTarget.getName();
		itsSecondaryNames = new ArrayList<String>(itsI);
		itsTertiaryNames = new ArrayList<String>(itsJ);
		for (Column c : aSecondaryTargets)
			itsSecondaryNames.add(c.getName());
		for (Column c : aTertiaryTargets)
			itsTertiaryNames.add(c.getName());

		itsQualityMeasure = theType;
		itsSampleSize = aPrimaryTarget.size();
/*		itsData = new ArrayList<Point2D.Float>(itsSampleSize);
		for(int i=0; i<itsSampleSize; i++)
		{
/*			itsXSum += thePrimaryColumn.getFloat(i);
			itsYSum += theSecondaryColumn.getFloat(i);
			itsXYSum += thePrimaryColumn.getFloat(i)*theSecondaryColumn.getFloat(i);
			itsXSquaredSum += thePrimaryColumn.getFloat(i)*thePrimaryColumn.getFloat(i);
			itsYSquaredSum += theSecondaryColumn.getFloat(i)*theSecondaryColumn.getFloat(i);

			itsData.add(new Point2D.Float(thePrimaryColumn.getFloat(i), theSecondaryColumn.getFloat(i)) );
		}*/

		switch (itsQualityMeasure)
		{
			case LINEAR_REGRESSION:
			{/* TODO: fix or remove
				itsBase = null; //this *is* the base
				itsComplementData = null; //will remain empty for the base RM
				updateRegressionFunction();
				updateErrorTerms();*/
				break;
			}
			case COOKS_DISTANCE:
			{
				//updateRegressionFunction(); //updating error terms unnecessary since Cook's distance does not care
				//"I see no reason for this to continue"
				//  -- Electric Six, `Lenny Kravitz'

				/*fill arrays which will contain the data. Schematically it looks like this (where x denotes a secondary and x' a tertiary target):
				 * aData =
				 *   x_1^1 ... x_1^i  x'_1^1 ... x'_1^j  y_1
				 *     .   .     .       .   .      .     .
				 *     .    .    .       .    .     .     .
				 *     .     .   .       .     .    .     .
				 *   x_n^1 ... x_n^i  x'_n^1 ... x'_n^j  y_n
				 *
				 * anXValues =
				 *   1  x_1^1 ... x_1^i  x'_1^1 ... x'_1^j
				 *   .    .   .     .       .   .      .
				 *   .    .    .    .       .    .     .
				 *   .    .     .   .       .     .    .
				 *   1  x_n^1 ... x_n^i  x'_n^1 ... x'_n^j
				 *
				 * aYValues =
				 *   y_1
				 *    .
				 *    .
				 *    .
				 *   y_n
				 *
				 * the indices in the for-loops will correspond to the indices used here.
				 */

				double[][] anXValues = new double[itsSampleSize][(int) itsP];
				double[][] aYValues = new double[itsSampleSize][1];
				double[][] aZValues = new double[(int) itsQ][(int) itsP];

				for (int n=0; n<itsSampleSize; n++)
				{
					anXValues[n][0]=1;
					for (int i=0; i<itsI; i++)
					{
						Column aSecondaryColumn = aSecondaryTargets.get(i);
						anXValues[n][1+i] = aSecondaryColumn.getFloat(n);
					}
					for (int j=0; j<itsJ; j++)
					{
						Column aTertiaryColumn = aTertiaryTargets.get(j);
						anXValues[n][1+itsI+j] = aTertiaryColumn.getFloat(n);
					}
					aYValues[n][0] = aPrimaryTarget.getFloat(n);
				}

				// build Z-matrix values; indicating which subset of beta we're interested in
				int anOffset = 1;
				if (itsInterceptRelevance)
					anOffset--;
				for (int q=0; q<itsQ; q++)
					for (int p=0; p<itsP; p++)
						if (p==q+anOffset)
							aZValues[q][p] = 1;
						else
							aZValues[q][p] = 0;

				itsXMatrix = new Matrix(anXValues);
				itsYMatrix = new Matrix(aYValues);
				itsZMatrix = new Matrix(aZValues);

				//do the regression math
				//TODO: refuse to do anything in the unlikely case that the data matrix is row deficient.
				//      If this unlikely event is not caught, the program will crash.
				computeRegression();

				theTargetConcept.setGlobalRegressionModel(spellFittedModel(itsBetaHat));

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
//				double[] aSVP = new double[itsSampleSize];
//				for (int i=0; i<itsSampleSize; i++)
//					aSVP[i] = itsResidualMatrix.get(i,0)*itsResidualMatrix.get(i,0)/itsP * itsHatMatrix.get(i,i)/(1-itsHatMatrix.get(i,i));
//				itsSVP = aSVP;
			}
		}
	}

	public String spellFittedModel(Matrix theBetaHat)
	{
		String aRegressionModel = "  fitted model: "+ itsPrimaryName + " = " + aDf.format(theBetaHat.get(0,0));
//		String aRegressionModel = "  fitted model: "+ itsPrimaryName + " = " + theBetaHat.get(0,0);
		for (int i=0; i<itsI; i++)
			aRegressionModel = aRegressionModel + " + " + aDf.format(theBetaHat.get(i+1,0)) + " * " + itsSecondaryNames.get(i);
//			aRegressionModel = aRegressionModel + " + " + theBetaHat.get(i+1,0) + " * " + itsSecondaryNames.get(i);
		for (int j=0; j<itsJ; j++)
			aRegressionModel = aRegressionModel + " + " + aDf.format(theBetaHat.get(j+itsI+1,0)) + " * " + itsTertiaryNames.get(j);
//			aRegressionModel = aRegressionModel + " + " + theBetaHat.get(j+itsI+1,0) + " * " + itsTertiaryNames.get(j);
		Log.logCommandLine(aRegressionModel);
		return aRegressionModel;
	}

	public void computeRegression()
	{
		itsXTXInverseMatrix = (itsXMatrix.transpose().times(itsXMatrix)).inverse();
		itsBetaHat = itsXTXInverseMatrix.times(itsXMatrix.transpose()).times(itsYMatrix);
		itsHatMatrix = itsXMatrix.times(itsXTXInverseMatrix).times(itsXMatrix.transpose());
		itsResidualMatrix = (Matrix.identity(itsSampleSize,itsSampleSize).minus(itsHatMatrix)).times(itsYMatrix);

		itsSSquared = (itsResidualMatrix.transpose().times(itsResidualMatrix)).get(0,0)/((double) itsSampleSize-itsP);
	}

	//constructor for non-base RM. It derives from a base-RM
/*	public RegressionMeasure(RegressionMeasure theBase, BitSet theMembers)
	{
		itsType = theBase.itsType;
		itsBase = theBase;

		//Create an empty measure
		itsSampleSize = theMembers.cardinality();

		itsXMatrix;
		private Matrix itsYMatrix;

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
	}*/

	//TODO test and verify method
/*	public double getEvaluationMeasureValue()
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
*/
	public double calculate(Subgroup theNewSubgroup)
	{
//		int aSampleSize = theNewSubgroup.getCoverage();

		//make submatrices
		Matrix anXMatrix = itsXMatrix.getMatrix(itsIndices,0,itsI+itsJ);
		Matrix aYMatrix = itsYMatrix.getMatrix(itsIndices,0,0);

		//filter out rank-deficient cases; these regressions cannot be computed, hence low quality
		LUDecomposition itsDecomp = new LUDecomposition(anXMatrix);
		if (!itsDecomp.isNonsingular())
			return -Double.MAX_VALUE;

		//compute regression
		Matrix anXTXMatrix = anXMatrix.transpose().times(anXMatrix);
		LUDecomposition itsXTXDecomp = new LUDecomposition(anXTXMatrix);
		if (!itsXTXDecomp.isNonsingular())
			return -Double.MAX_VALUE;
		Matrix anXTXInverseMatrix = anXTXMatrix.inverse();

		Matrix aBetaHat = anXTXInverseMatrix.times(anXMatrix.transpose()).times(aYMatrix);
//		Matrix aHatMatrix = anXMatrix.times(anXTXInverseMatrix).times(anXMatrix.transpose());
//		Matrix aResidualMatrix = (Matrix.identity(aSampleSize,aSampleSize).minus(aHatMatrix)).times(aYMatrix);

		theNewSubgroup.setRegressionModel(spellFittedModel(aBetaHat));

		//compute Cook's distance
//		double aP = aBetaHat.getRowDimension();
//		double anSSquared = (aResidualMatrix.transpose().times(aResidualMatrix)).get(0,0)/((double) aSampleSize-itsP);

//		double anOldQuality = aBetaHat.minus(itsBetaHat).transpose().times(anXMatrix.transpose()).times(anXMatrix).times(aBetaHat.minus(itsBetaHat)).get(0,0)/(itsP*anSSquared);
		Matrix aZXTXInverseZTMatrix = itsZMatrix.times(itsXTXInverseMatrix).times(itsZMatrix.transpose());
		LUDecomposition itsOtherDecomp = new LUDecomposition(aZXTXInverseZTMatrix);
		if (!itsOtherDecomp.isNonsingular())
			return -Double.MAX_VALUE;

		Matrix anM = itsZMatrix.transpose().times(aZXTXInverseZTMatrix.inverse()).times(itsZMatrix);
//		logMatrix(anM);
//		logMatrixDimensions(anM, "Zt(Z(XtX)-1Zt)-1Z");

		double aQuality = (aBetaHat.minus(itsBetaHat).transpose().times(anM).times(aBetaHat.minus(itsBetaHat))).get(0,0)/(itsQ*itsSSquared);
		return aQuality;
	}

	public double computeBoundSeven(int theSampleSize, double theT, double theRSquared)
	{
		if (theT>=1)
			return Double.MAX_VALUE;
		return itsP/itsQ*theT/((1-theT)*(1-theT))*theRSquared/(itsP*itsSSquared);
	}

	public double computeBoundSix(int theSampleSize, double theT)
	{
		updateSquaredResidualSum();
		if (theT>=1)
			return Double.MAX_VALUE;
		return itsP/itsQ*theT/((1-theT)*(1-theT))*itsSquaredResidualSum/(itsP*itsSSquared);
	}

	public double computeBoundFive(int theSampleSize, double theRSquared)
	{
		updateRemovedTrace();
		if (itsRemovedTrace>=1)
			return Double.MAX_VALUE;
		return itsP/itsQ*itsRemovedTrace/((1-itsRemovedTrace)*(1-itsRemovedTrace))*theRSquared/(itsP*itsSSquared);
	}

	public double computeBoundFour(int theSampleSize)
	{
		if (itsRemovedTrace>=1)
			return Double.MAX_VALUE;
		return itsP/itsQ*itsRemovedTrace/((1-itsRemovedTrace)*(1-itsRemovedTrace))*itsSquaredResidualSum/(itsP*itsSSquared);
	}

/*	private double computeSVPDistance(int theNrRemoved, int[] theIndices)
	{
		double result = 0.0;
		for (int i=0; i<theNrRemoved; i++)
			result += itsSVP[theIndices[i]];
		return result;
	}
*/
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
/*	public void addObservation(float theY, float theX)
	{
		//adjust the sums
		itsSampleSize++;
		itsXSum += theX;
		itsYSum += theY;
		itsXYSum += theX*theY;
		itsXSquaredSum += theX*theX;
		itsYSquaredSum += theY*theY;

		//Add to its own lists
		Point2D.Float aPoint = new Point2D.Float(theX,theY);
		itsData.add(aPoint);
	}

	public void addObservation(Point2d.Float theObservation)
	{
		float anX = theObservation.getX();
		float aY = theObservation.getY();

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

	public Point2D.Float getObservation(int theIndex)
	{
		return itsData.get(theIndex);
	}
*/
 	/**
	 * calculates the error terms for the distribution and recomputes the
	 * sum of the squared error term
	 *
	 */
/*	private void updateErrorTerms()
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

	}*/

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

	@Deprecated
	// call getErrorTerm(double theX, double theY) directly
	private double getErrorTerm(Point2D.Float theDataPoint)
	{
		return getErrorTerm(theDataPoint.getX(), theDataPoint.getY());
	}

	private double getErrorTermVariance(double theErrorTermSquaredSum, double theSampleSize)
	{
		return theErrorTermSquaredSum / (theSampleSize - 2 );
	}

	public int getSampleSize()
	{
		return itsSampleSize;
	}

	public double getXSum()
	{
		return itsXSum;
	}

	public double getYSum()
	{
		return itsYSum;
	}

	public double getXYSum()
	{
		return itsXYSum;
	}

	public double getXSquaredSum()
	{
		return itsXSquaredSum;
	}

	public double getYSquaredSum()
	{
		return itsYSquaredSum;
	}

	/**
	 * Computes and returns the correlation given the observations contained by CorrelationMeasure
	 * @return the computed correlation
	 */
	public double getCorrelation()
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

	public String getGlobalModel() { return itsGlobalModel; }

	public double getT(int theSampleSize) { return itsT[theSampleSize]; }
	public double getRSquared(int theSampleSize) { return itsRSquared[theSampleSize]; }

	public void computeRemovedIndices(BitSet theMembers, int theSampleSize)
	{
		int[] anIndices = new int[theSampleSize];
		int[] aRemovedIndices = new int[itsSampleSize-theSampleSize];
		int anIndex=0;
		int aRemovedIndex=0;
		for (int i=0; i<itsSampleSize; i++)
		{
			if (theMembers.get(i))
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
		itsIndices = anIndices;
		itsRemovedIndices = aRemovedIndices;
	}

	public void updateSquaredResidualSum()
	{
		Matrix aRemovedResiduals = itsResidualMatrix.getMatrix(itsRemovedIndices,0,0);
		itsSquaredResidualSum = squareSum(aRemovedResiduals);
	}

	public void updateRemovedTrace()
	{
		Matrix aRemovedHatMatrix = itsHatMatrix.getMatrix(itsRemovedIndices,itsRemovedIndices);
		itsRemovedTrace = aRemovedHatMatrix.trace();
	}

	public void logMatrix( Matrix theMatrix )
	{
		for (int i=0; i<theMatrix.getRowDimension(); i++)
		{
			String aRow = ""+theMatrix.get(i, 0);
			for (int j=1; j<theMatrix.getColumnDimension(); j++)
				aRow += ", " + theMatrix.get(i,j);
			Log.logCommandLine(aRow);
		}
	}

	public void logMatrixDimensions(Matrix theMatrix, String theName)
	{
		Log.logCommandLine("Dimensions of Matrix " + theName + " : " + theMatrix.getRowDimension() + ", " + theMatrix.getColumnDimension());
	}
}
