package nl.liacs.subdisc;

// TODO MM put Contingency table here without screwing up package classes layout.
/**
 * The QualityMeasure class includes all quality measures used
 * ({@link #calculate(float, float) contingency table}).
 */
public class QualityMeasure
{
	private QM itsQualityMeasure;
	private final int itsNrRecords;

	//SINGLE_NOMINAL
	private int itsTotalTargetCoverage;

	//SINGLE_NUMERIC and SINGLE_ORDINAL
	private float itsTotalAverage = 0.0f;
	private double itsTotalSampleStandardDeviation = 0.0;
	private ProbabilityDensityFunction itsPDF; // pdf for entire dataset

	//Bayesian
	private DAG itsDAG;
	private static int itsNrNodes;
	private static float itsAlpha;
	private static float itsBeta;
	private static boolean[][] itsVStructures;

	//SINGLE_NOMINAL
	public QualityMeasure(QM theMeasure, int theTotalCoverage, int theTotalTargetCoverage)
	{
		itsQualityMeasure = theMeasure;
		itsNrRecords = theTotalCoverage;
		itsTotalTargetCoverage = theTotalTargetCoverage;
	}

	//SINGLE_NUMERIC
	public QualityMeasure(QM theMeasure, int theTotalCoverage, float theTotalSum, float theTotalSSD, ProbabilityDensityFunction theDataPDF)
	{
		itsQualityMeasure = theMeasure;
		itsNrRecords = theTotalCoverage;
		if (itsNrRecords > 0)
			itsTotalAverage = theTotalSum/itsNrRecords;
		if (itsNrRecords > 1)
			itsTotalSampleStandardDeviation = Math.sqrt(theTotalSSD/(itsNrRecords-1));
		itsPDF = theDataPDF;
	}

	public ProbabilityDensityFunction getProbabilityDensityFunction()
	{
		return itsPDF;
	}

	/**
	 * Contingency table:</br>
	 * <table border="1" cellpadding="2" cellspacing="0">
	 * 	<tr align="center">
	 * 		<td></td>
	 * 		<td>B</td>
	 * 		<td><span style="text-decoration: overline">B</span></td>
	 * 		<td></td>
	 * 	</tr>
	 * 	<tr align="center">
	 * 		<td>H</td>
	 * 		<td><i>n</i>(HB)</td>
	 * 		<td><i>n</i>(H<span style="text-decoration: overline">B</span>)</td>
	 * 		<td><i>n</i>(H)</td>
	 * 	</tr>
	 * 	<tr align="center">
	 * 		<td><span style="text-decoration: overline">H</span></td>
	 * 		<td><i>n</i>(<span style="text-decoration: overline">H</span>B)</td>
	 * 		<td><i>n</i>(<span style="text-decoration: overline">HB</span>)</td>
	 * 		<td><i>n</i>(<span style="text-decoration: overline">H</span>)</td>
	 * 	</tr>
	 * 	<tr align="center">
	 * 		<td></td>
	 * 		<td><i>n</i>(B)</td>
	 * 		<td><i>n</i>(<span style="text-decoration: overline">B</span>)</td>
	 * 		<td>N</td>
	 * 	</tr>
	 * </table>
	 * <p>
	 * Please note the following:
	 * <ul>
	 * <li>n(H) = Coverage (subgroup coverage),</li>
	 * <li>n(B) = TotalTargetCoverage (of the data),</li>
	 * <li>N = TotalCoverage (number of rows in the data).</li>
	 * </ul>
	 */
	public float calculate(float theCountHeadBody, float theCoverage)
	{
		float aResult = calculate(itsQualityMeasure, itsNrRecords, itsTotalTargetCoverage, theCountHeadBody, theCoverage);
		if (Float.isNaN(aResult)) // FIXME MM this does not seem wise, see comment below
			return 0.0f;
		else
			return aResult;
	}

	//SINGLE_NOMINAL =======================================================

	/*
	 * FIXME MM each case should check the result value instead of
	 * returning junk and let calculate(float, float) handle that
	 */
	public  static float calculate(QM theMeasure, int theTotalCoverage, float theTotalTargetCoverage, float theCountHeadBody, float theCoverage)
	{
		float aCountNotHeadBody			= theCoverage - theCountHeadBody;
		float aTotalTargetCoverageNotBody	= theTotalTargetCoverage - theCountHeadBody;
		float aCountNotHeadNotBody		= theTotalCoverage - (theTotalTargetCoverage + aCountNotHeadBody);
		float aCountBody			= aCountNotHeadBody + theCountHeadBody;

		float returnValue = -10f; // FIXME MM Bad measure value for default
		switch (theMeasure)
		{
			case WRACC:
			{
				returnValue = (theCountHeadBody/theTotalCoverage)-(theTotalTargetCoverage/theTotalCoverage)*(aCountBody/theTotalCoverage);
				break;
			}
			case CHI_SQUARED:
			{
				returnValue = calculateChiSquared(theTotalCoverage, theTotalTargetCoverage, aCountBody, theCountHeadBody);
				break;
			}
			case INFORMATION_GAIN:
			{
				returnValue = calculateInformationGain(theTotalCoverage, theTotalTargetCoverage, aCountBody, theCountHeadBody);
				break;
			}
			case BINOMIAL:
			{
				returnValue = ((float) Math.sqrt(aCountBody/theTotalCoverage)) * (theCountHeadBody/aCountBody - theTotalTargetCoverage/theTotalCoverage);
				break;
			}
			case JACCARD:
			{
				returnValue = theCountHeadBody / (aCountBody + aTotalTargetCoverageNotBody);
				break;
			}
			case COVERAGE:
			{
				returnValue = aCountBody;
				break;
			}
			case ACCURACY:
			{
				returnValue = theCountHeadBody /aCountBody;
				break;
			}
			case SPECIFICITY:
			{
				returnValue = aCountNotHeadNotBody / (theTotalCoverage - theTotalTargetCoverage);
				break;
			}
			case SENSITIVITY:
			{
				returnValue = theCountHeadBody / theTotalTargetCoverage;
				break;
			}
			case LAPLACE:
			{
				returnValue = (theCountHeadBody+1) / (aCountBody+2);
				break;
			}
			case F_MEASURE:
			{
				returnValue = theCountHeadBody / (aCountBody + theTotalTargetCoverage);
				break;
			}
			case G_MEASURE:
			{
				returnValue = theCountHeadBody / (aCountNotHeadBody + theTotalTargetCoverage);
				break;
			}
			case CORRELATION:
			{
				float aCountNotHead = theTotalCoverage-theTotalTargetCoverage;
				returnValue = (float) ((theCountHeadBody*aCountNotHead - theTotalTargetCoverage*aCountNotHeadBody) / Math.sqrt(theTotalTargetCoverage*aCountNotHead*aCountBody*(theTotalCoverage-aCountBody)));
				break;
			}
			case PURITY:
			{
				returnValue = theCountHeadBody/aCountBody;
				if (returnValue < 0.5f)
					returnValue = 1.0f - returnValue;
				break;
			}
			case ABSWRACC:
			{
				returnValue = theCountHeadBody/theTotalCoverage - ((theTotalTargetCoverage/theTotalCoverage) * aCountBody/theTotalCoverage);
				returnValue = Math.abs(returnValue);
				break;
			}
			case BAYESIAN_SCORE:
			{
				returnValue = (float) calculateBayesianFactor(theTotalCoverage, theTotalTargetCoverage, aCountBody, theCountHeadBody);
				break;
			}
			case LIFT:
			{
				returnValue = (theCountHeadBody * theTotalCoverage) / (theCoverage * theTotalTargetCoverage);
				// alternative has 3 divisions, but TTC/N is constant and could be cached
				// returnValue = (theCountHeadBody / theCoverage) / (theTotalTargetCoverage / theTotalCoverage);
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
				if (QM.getQualityMeasures(TargetType.SINGLE_NOMINAL).contains(theMeasure))
					throw new AssertionError(theMeasure);
				else
					throw new IllegalArgumentException("Invalid argument: " + theMeasure);
			}
		}
		return returnValue;
	}

	public static float calculatePropensityBased(QM theMeasure, int theCountHeadBody, int theCoverage, int theTotalCount, double theCountHeadPropensityScore)
	{
		float aCountHeadBody = (float) theCountHeadBody;
		float aCoverage = (float) theCoverage;
		float aTotalCount = (float) theTotalCount;
		float aCountHeadPropensityScore = (float) theCountHeadPropensityScore;
		float returnValue = -10.0f; // FIXME MM Bad measure value for default

		switch (theMeasure)
		{
			case PROP_SCORE_WRACC:
			{
				returnValue = ((aCountHeadBody/aCoverage - (aCountHeadPropensityScore/aCoverage) ) * aCoverage/aTotalCount);
				System.out.println("Calculate Propensity based WRAcc");
				System.out.println(returnValue);
				break;
			}
			case PROP_SCORE_RATIO:
			{
				returnValue = (aCountHeadBody/aTotalCount) / (aCountHeadPropensityScore/aTotalCount);
				break;
			}
			default :
			{
				throw new IllegalArgumentException(QM.class.getSimpleName() + " invalid: " + theMeasure);
			}
		}

		return returnValue;
	}

	private static float calculateChiSquared(float totalSupport, float headSupport, float bodySupport, float bodyHeadSupport)
	{
		//HEADBODY
		float Eij = calculateExpectency(totalSupport, bodySupport, headSupport);
		float quality = (calculatePowerTwo(bodyHeadSupport - Eij))/ Eij;

		//HEADNOTBODY
		Eij = calculateExpectency(totalSupport, (totalSupport - bodySupport), headSupport);
		quality += (calculatePowerTwo(headSupport - bodyHeadSupport - Eij)) / Eij;

		//NOTHEADBODY
		Eij = calculateExpectency(totalSupport, (totalSupport - headSupport), bodySupport);
		quality += (calculatePowerTwo(bodySupport - bodyHeadSupport - Eij)) / Eij;

		//NOTHEADNOTBODY
		Eij = calculateExpectency(totalSupport, (totalSupport - bodySupport), (totalSupport - headSupport));
		quality += (calculatePowerTwo((totalSupport - headSupport - bodySupport + bodyHeadSupport) - Eij)) / Eij;

		return quality;
	}

	private static float calculatePowerTwo(float value)
	{
		return (value * value);
	}

	private static float calculateExpectency(float totalSupport, float bodySupport, float headSupport)
	{
		return totalSupport * (bodySupport / totalSupport) * (headSupport / totalSupport);
	}

	/**
	 * Computes the 2-log of p.
	 */
	private static float lg(float p)
	{
		return (float) (Math.log(p) / Math.log(2));
	}

	public static float calculateEntropy(float bodySupport, float headBodySupport)
	{
		if (bodySupport == 0)
			return 0.0f; //special case that should never occur

		if (headBodySupport==0 || bodySupport==headBodySupport)
			return 0.0f; // by definition

		float pj = headBodySupport/bodySupport;
		return -1.0f*pj*lg(pj) - (1-pj)*lg(1-pj);
	}

	/**
	 * Calculates the ConditionalEntropy.
	 * By definition, 0*lg(0) is 0, such that any boundary cases return 0.
	 *
	 * @param bodySupport
	 * @param bodyHeadSupport
	 * @return the conditional entropy for given the two parameters.
	 */
	public static float calculateConditionalEntropy(float bodySupport, float bodyHeadSupport)
	{
		if (bodySupport == 0)
			return 0.0f; //special case that should never occur

		float Phb = bodyHeadSupport/bodySupport; //P(H|B)
		float Pnhb = (bodySupport - bodyHeadSupport)/bodySupport; //P(H|B)
		if (Phb == 0 || Pnhb == 0)
			return 0.0f; //by definition

		float quality = -1.0f*Phb*lg(Phb) - Pnhb*lg(Pnhb);
		return quality;
	}

	public static float calculateInformationGain(float totalSupport, float headSupport, float bodySupport, float headBodySupport)
	{
		float aFraction = bodySupport/totalSupport;
		float aNotBodySupport = totalSupport-bodySupport;
		float aHeadNotBodySupport = headSupport-headBodySupport;

		return calculateEntropy(totalSupport, headSupport)
			- aFraction*calculateConditionalEntropy(bodySupport, headBodySupport) //inside the subgroup
			- (1-aFraction)*calculateConditionalEntropy(aNotBodySupport, aHeadNotBodySupport); //the complement
	}

	//Iyad Batal: Calculate the Bayesian score assuming uniform beta priors on all parameters
	public static double calculateBayesianFactor(float totalSupport, float headSupport, float bodySupport, float headBodySupport)
	{
		//type=Bayes_factor: the score is the Bayes factor of model M_h (a number between [-Inf, + Inf])
		//score = P(M_h)*P(D|M_h) / (P(M_l)*P(D|M_l)+P(M_e)*P(D|M_e))

		//True Positive
		int N11 = (int)headBodySupport;
		//False Positive
		int N12 = (int)(bodySupport-headBodySupport);
		//False Negative
		int N21 = (int)(headSupport-headBodySupport);
		//True Negative
		int N22 = (int)(totalSupport-N11-N12-N21);

		int N1 = N11+N21;
		int N2 = N12+N22;

		//the parameter priors: uniform priors
		int alpha = 1, beta = 1;
		int alpha1 = 1, beta1 = 1;
		int alpha2 = 1, beta2 = 1;

		double logM_e = score_M_e(N1, N2, alpha, beta);
		double[] res = score_M_h(N11, N12, N21, N22, alpha1, beta1, alpha2, beta2);
		double logM_h = res[0];
		res = score_M_h(N21, N22, N11, N12, alpha2, beta2, alpha1, beta1);
		double logM_l = res[0];

		//assume uniform prior on all models
		double prior_M_e = 0.33333333, prior_M_h = 0.33333333, prior_M_l = 0.33333334;
		double log_numerator = Math.log(prior_M_h) + logM_h;
		double log_denom1 = logAdd(Math.log(prior_M_e) + logM_e, Math.log(prior_M_l) + logM_l);

		double bayesian_score = log_numerator - log_denom1;
		return bayesian_score;
	}

	//Iyad Batal: Calculate the Bayesian score assuming uniform beta priors on all parameters
	public static double calculateBayesianScore(float totalSupport, float headSupport, float bodySupport, float headBodySupport)
	{
		String type="Bayes_factor";
		//type=Bayes_factor: the score is the Bayes factor of model M_h (a number between [-Inf, + Inf])
		//score = P(M_h)*P(D|M_h) / (P(M_l)*P(D|M_l)+P(M_e)*P(D|M_e))

		//String type="posterior";
		//type=posterior: the score is the posterior of model M_h (a number between [0, 1])
		//score = P(M_h)*P(D|M_h) / (P(M_l)*P(D|M_l)+P(M_e)*P(D|M_e)+P(M_h)*P(D|M_h))

		//Both Bayes_factor and posterior provide the same ranking of the patterns!

		//True Positive
		int N11 = (int)headBodySupport;
		//False Positive
		int N12 = (int)(bodySupport-headBodySupport);
		//False Negative
		int N21 = (int)(headSupport-headBodySupport);
		//True Negative
		int N22 = (int)(totalSupport-N11-N12-N21);

		int N1 = N11+N21;
		int N2 = N12+N22;

		//the parameter priors: uniform priors
		int alpha = 1, beta = 1;
		int alpha1 = 1, beta1 = 1;
		int alpha2 = 1, beta2 = 1;

		double logM_e = score_M_e(N1, N2, alpha, beta);
		double[] res = score_M_h(N11, N12, N21, N22, alpha1, beta1, alpha2, beta2);
		double logM_h = res[0];
		res = score_M_h(N21, N22, N11, N12, alpha2, beta2, alpha1, beta1);
		double logM_l = res[0];

		//assume uniform prior on all models
		double prior_M_e = 0.33333333, prior_M_h = 0.33333333, prior_M_l = 0.33333334;
		double log_numerator = Math.log(prior_M_h) + logM_h;
		double log_denom1 = logAdd(Math.log(prior_M_e) + logM_e, Math.log(prior_M_l) + logM_l);
		double log_denominator = logAdd(log_denom1, Math.log(prior_M_h) + logM_h);

		//this is the posterior probability of model M_h
		double M_h_posterior = Math.exp(log_numerator - log_denominator);

		double bayesian_score = log_numerator - log_denom1;

		if(type.equals("Bayes_factor"))
			return bayesian_score;
		else
			return M_h_posterior;
	}

	//Iyad Batal: auxiliary function to compute the sum of logarithms (input: log(a), log(b), output log(a+b))
	private static double logAdd(double x, double y)
	{
		double res;
		if (Math.abs(x-y) >= 36.043)
			res = Math.max(x, y);
		else
			res= Math.log(1 + Math.exp(y - x)) + x;
		return res;
	}

	//Iyad Batal: auxiliary function to compute the difference of logarithms (input: log(a), log(b), output log(a-b))
	private static double logDiff(double x, double y)
	{
		double res;
		if((x-y) >= 36.043)
			res = x;
		else
			res = Math.log(1-Math.exp(y - x)) + x;
		return res;
	}

	//Iyad Batal: auxiliary function to compute the marginal likelihood of model M_e (used in computing the Bayesian score)
	private static double score_M_e(int N1, int N2, int alpha, int beta)
	{
		return Function.logGammaBig(alpha+beta) - Function.logGammaBig(alpha+N1+beta+N2) + Function.logGammaBig(alpha+N1) - Function.logGammaBig(alpha) + Function.logGammaBig(beta+N2) - Function.logGammaBig(beta);
	}

	//Iyad Batal: auxiliary function to compute the marginal likelihood of model M_h (used in computing the Bayesian score)
	private static double[] score_M_h(int N11, int N12, int N21, int N22, int alpha1, int beta1, int alpha2, int beta2)
	{

		int a = N21+alpha2;
		int b = N22+beta2;
		int c = N11+alpha1;
		int d = N12+beta1;

		double k = 0.5;
		double C = Function.logGammaBig(alpha1+beta1) - Function.logGammaBig(alpha1) - Function.logGammaBig(beta1) + Function.logGammaBig(alpha2+beta2) - Function.logGammaBig(alpha2) - Function.logGammaBig(beta2);

		double part2=0;
		for (int i=1; i<=b; i++)
		{
			 int j=a+i-1;
			 double temp = Function.logGammaBig(a) + Function.logGammaBig(b) - Function.logGammaBig(j+1) - Function.logGammaBig(a+b-j) + Function.logGammaBig(c+j) + Function.logGammaBig(a+b+d-1-j) - Function.logGammaBig(a+b+c+d-1);
			 if (i==1)
				 part2 = temp;
			 else
				 part2 = logAdd(part2,temp);
		}

		double part1 = Function.logGammaBig(a) + Function.logGammaBig(b) - Function.logGammaBig(a+b) + Function.logGammaBig(c) + Function.logGammaBig(d) - Function.logGammaBig(c+d);

		double[] res = new double[2];

		res[0] = -Math.log(k) + C + part2;
		res[1] = logDiff(-Math.log(k)+C+part1, res[0]);

		return res;
	}

	public int getNrRecords() { return itsNrRecords; }
	public int getNrPositives() { return itsTotalTargetCoverage; }

	//get quality of upper left corner
	public float getROCHeaven()
	{
		return calculate(itsTotalTargetCoverage, itsTotalTargetCoverage);
	}

	//lower right corner
	public float getROCHell()
	{
		return calculate(0, itsNrRecords - itsTotalTargetCoverage);
	}







	//SINGLE_NUMERIC =======================================================

	/*
	 * MEAN = sqrt(sampleSize)*(sampleAvg-dataAvg);
	 * Z = MEAN / dataStdDev;
	 * T = MEAN / sampleStdDev-1
	 */
	/**
	 * Calculates the quality for a sample, or {@link Subgroup}.
	 * 
	 * @param theCoverage the number of members in the sample
	 * @param theSum the sum for the sample
	 * @param theSSD the sum of squared deviations for the sample
	 * @param theMedian the median for the sample
	 * @param theMedianAD the median average deviation for the sample
	 * @param thePDF the ProbabilityDensityFunction for the sample
	 * 
	 * @return the quality
	 * 
	 * @see Stat
	 * @see Column#getStatistics(java.util.BitSet, java.util.Set)
	 * @see ProbabilityDensityFunction
	 */
	public float calculate(int theCoverage, float theSum, float theSSD, float theMedian, float theMedianAD, ProbabilityDensityFunction thePDF)
	{
		float aReturn = Float.NEGATIVE_INFINITY;
		switch (itsQualityMeasure)
		{
			//NUMERIC
			case Z_SCORE :
			{
				if (itsNrRecords <= 1)
					aReturn = 0.0f;
				else
					aReturn = (float) ((Math.sqrt(theCoverage) * ((theSum/theCoverage) - itsTotalAverage)) / itsTotalSampleStandardDeviation);
				break;
			}
			case INVERSE_Z_SCORE :
			{
				if (itsNrRecords <= 1)
					aReturn = 0.0f;
				else
					aReturn = (float) -((Math.sqrt(theCoverage) * ((theSum/theCoverage) - itsTotalAverage)) / itsTotalSampleStandardDeviation);
				break;
			}
			case ABS_Z_SCORE :
			{
				if (itsNrRecords <= 1)
					aReturn = 0.0f;
				else
					aReturn = (float) (Math.abs((Math.sqrt(theCoverage) * (theSum/theCoverage - itsTotalAverage)) / itsTotalSampleStandardDeviation));
				break;
			}
			case AVERAGE :
			{
				aReturn = theSum/theCoverage;
				break;
			}
			case INVERSE_AVERAGE :
			{
				aReturn = -theSum/theCoverage;
				break;
			}
			case MEAN_TEST :
			{
				aReturn = (float) (Math.sqrt(theCoverage) * ((theSum/theCoverage) - itsTotalAverage));
				break;
			}
			case INVERSE_MEAN_TEST :
			{
				aReturn = (float) -(Math.sqrt(theCoverage) * ((theSum/theCoverage) - itsTotalAverage));
				break;
			}
			case ABS_MEAN_TEST :
			{
				aReturn = (float) (Math.abs(Math.sqrt(theCoverage) * ((theSum/theCoverage) - itsTotalAverage)));
				break;
			}
			case T_TEST :
			{
				if(theCoverage <= 2)
					aReturn = 0.0f;
				else
					aReturn = (float) ((Math.sqrt(theCoverage) * ((theSum/theCoverage) - itsTotalAverage)) / Math.sqrt(theSSD/(theCoverage-1)));
				break;
			}
			case INVERSE_T_TEST :
			{
				if(theCoverage <= 2)
					aReturn = 0.0f;
				else
					aReturn = (float) -((Math.sqrt(theCoverage) * ((theSum/theCoverage) - itsTotalAverage)) / Math.sqrt(theSSD/(theCoverage-1)));
				break;
			}
			case ABS_T_TEST :
			{
				if(theCoverage <= 2)
					aReturn = 0;
				else
					aReturn = (float) (Math.abs((Math.sqrt(theCoverage) * (theSum/theCoverage - itsTotalAverage)) / Math.sqrt(theSSD/(theCoverage-1))));
				break;
			}
			//ORDINAL
			case AUC :
			{
				float aComplementCoverage = itsNrRecords - theCoverage;
				float aSequenceSum = theCoverage*(theCoverage+1)/2.0f; //sum of all positive ranks, assuming ideal case
				aReturn = 1.0f + (aSequenceSum-theSum)/(theCoverage*aComplementCoverage);
				break;
			}
			case WMW_RANKS :
			{
				float aComplementCoverage = itsNrRecords - theCoverage;
				float aMean = (theCoverage*(theCoverage+aComplementCoverage+1))/2.0f;
				float aStDev = (float) Math.sqrt((theCoverage*aComplementCoverage*(theCoverage+aComplementCoverage+1))/12.0f);
				aReturn = (theSum-aMean)/aStDev;
				break;
			}
			case INVERSE_WMW_RANKS :
			{
				float aComplementCoverage = itsNrRecords - theCoverage;
				float aMean = (theCoverage*(theCoverage+aComplementCoverage+1))/2.0f;
				float aStDev = (float) Math.sqrt((theCoverage*aComplementCoverage*(theCoverage+aComplementCoverage+1))/12.0f);
				aReturn = -((theSum-aMean)/aStDev);
				break;
			}
			case ABS_WMW_RANKS :
			{
				float aComplementCoverage = itsNrRecords - theCoverage;
				float aMean = (theCoverage*(theCoverage+aComplementCoverage+1))/2.0f;
				float aStDev = (float) Math.sqrt((theCoverage*aComplementCoverage*(theCoverage+aComplementCoverage+1))/12.0f);
				aReturn = Math.abs((theSum-aMean)/aStDev);
				break;
			}
			case MMAD :
			{
				aReturn = (theCoverage/(2.0f*theMedian+theMedianAD));
				break;
			}
			// normal H^2 for continuous PDFs
			case SQUARED_HELLINGER :
			{
				double aTotalSquaredDifference = 0.0;
				for (int i = 0, j = itsPDF.size(); i < j; ++i)
				{
					float aDensity = itsPDF.getDensity(i);
					float aDensitySubgroup = thePDF.getDensity(i);
					double aDifference = Math.sqrt(aDensity) - Math.sqrt(aDensitySubgroup);
					aTotalSquaredDifference += (aDifference * aDifference);
					//Log.logCommandLine("difference in PDF: " + aTotalSquaredDifference);
				}
				Log.logCommandLine("difference in PDF: " + aTotalSquaredDifference);
				aReturn = (float) (0.5 * aTotalSquaredDifference);
				break;
			}
			case SQUARED_HELLINGER_WEIGHTED :
			{
				double aTotalSquaredDifference = 0.0;
				for (int i = 0, j = itsPDF.size(); i < j; ++i)
				{
					float aDensity = itsPDF.getDensity(i);
					float aDensitySubgroup = thePDF.getDensity(i);
					double aDifference = Math.sqrt(aDensity) - Math.sqrt(aDensitySubgroup);
					aTotalSquaredDifference += (aDifference * aDifference);
					//Log.logCommandLine("difference in PDF: " + aTotalSquaredDifference);
				}
				Log.logCommandLine("difference in PDF: " + aTotalSquaredDifference);
				aReturn = (float) ((0.5 * (aTotalSquaredDifference * theCoverage)) / itsNrRecords);
				break;
			}
			case SQUARED_HELLINGER_WEIGHTED_ADJUSTED :
			{
				// SQUARED_HELLINGER
				double aTotalSquaredDifference = 0.0;
				for (int i = 0, j = itsPDF.size(); i < j; ++i)
				{
					float aDensity = itsPDF.getDensity(i);
					float aDensitySubgroup = thePDF.getDensity(i);
					double aDifference = Math.sqrt(aDensity) - Math.sqrt(aDensitySubgroup);
					aTotalSquaredDifference += (aDifference * aDifference);
					//Log.logCommandLine("difference in PDF: " + aTotalSquaredDifference);
				}
				Log.logCommandLine("difference in PDF: " + aTotalSquaredDifference);
				aReturn = (float) ((0.5 * (aTotalSquaredDifference * theCoverage)) / itsNrRecords);

				// now weight SQUARED_HELLINGER
				// magic number = maximum possible score
//				aReturn = (float) (aTotalSquaredDifference * (theCoverage / (2.0 * itsNrRecords)));
				aReturn = (float) (aReturn / 0.1481481481481481);
				break;
			}
			case KULLBACK_LEIBLER :
			{
				double aTotalDivergence = 0.0;
				for (int i = 0, j = itsPDF.size(); i < j; ++i)
				{
					float aDensity = itsPDF.getDensity(i);
					float aDensitySubgroup = thePDF.getDensity(i);
					/*
					 * avoid errors in Math.log() because of
					 * (0 / x) or (x / 0)
					 * returns 0 by definition according to
					 * http://en.wikipedia.org/wiki/Kullback%E2%80%93Leibler_divergence
					 * NOTE this also catches DIVIVE_BY_0
					 * for (aDensity == 0) because
					 * (aSubgroupDensity == 0) for at least
					 * all situations where (aDenisity == 0)
					 */
					if (aDensitySubgroup == 0.0)
						continue;
					aTotalDivergence += (aDensitySubgroup * Math.log(aDensitySubgroup/aDensity));
				}
				aReturn = (float) aTotalDivergence;
				break;
			}
			case KULLBACK_LEIBLER_WEIGHTED :
			{
				double aTotalDivergence = 0.0;
				for (int i = 0, j = itsPDF.size(); i < j; ++i)
				{
					float aDensity = itsPDF.getDensity(i);
					float aDensitySubgroup = thePDF.getDensity(i);
					/*
					 * avoid errors in Math.log() because of
					 * (0 / x) or (x / 0)
					 * returns 0 by definition according to
					 * http://en.wikipedia.org/wiki/Kullback%E2%80%93Leibler_divergence
					 * NOTE this also catches DIVIVE_BY_0
					 * for (aDensity == 0) because
					 * (aSubgroupDensity == 0) for at least
					 * all situations where (aDenisity == 0)
					 */
					if (aDensitySubgroup == 0.0)
						continue;
					aTotalDivergence += (aDensitySubgroup * Math.log(aDensitySubgroup/aDensity));
				}
				aReturn = (float) ((aTotalDivergence * theCoverage) / itsNrRecords);
				break;
			}
			case CWRACC :
			{
				//some random code
				// http://en.wikipedia.org/wiki/Total_variation_distance ?
				double aTotalDifference = 0.0;
				for (int i = 0, j = itsPDF.size(); i < j; ++i)
				{
					float aDensity = itsPDF.getDensity(i);
					float aDensitySubgroup = thePDF.getDensity(i);
					aTotalDifference += Math.abs(aDensity - aDensitySubgroup);
				}
				Log.logCommandLine("difference in PDF: " + aTotalDifference);
				aReturn = (float) ((aTotalDifference * theCoverage) / itsNrRecords);
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
				if (QM.getQualityMeasures(TargetType.SINGLE_NUMERIC).contains(itsQualityMeasure) ||
						QM.getQualityMeasures(TargetType.SINGLE_ORDINAL).contains(itsQualityMeasure))
					throw new AssertionError(itsQualityMeasure);
				else
					throw new IllegalArgumentException("Invalid QM: " + itsQualityMeasure);
			}
		}

		return aReturn;
	}


	//Baysian ==============================================================

	public QualityMeasure(SearchParameters theSearchParameters, DAG theDAG, int theNrRecords)
	{
		itsQualityMeasure = theSearchParameters.getQualityMeasure();
		itsNrRecords = theNrRecords;
		itsDAG = theDAG;
		itsNrNodes = itsDAG.getSize();
		itsAlpha = theSearchParameters.getAlpha();
		itsBeta = theSearchParameters.getBeta();
		itsVStructures = itsDAG.determineVStructures();
	}

	public float calculate(Subgroup theSubgroup)
	{
		switch (itsQualityMeasure)
		{
			case WEED :
			{
				return (float) (Math.pow(calculateEntropy(itsNrRecords, theSubgroup.getCoverage()), itsAlpha) *
						Math.pow(calculateEditDistance(theSubgroup.getDAG()), itsBeta));
			}
			case EDIT_DISTANCE :
			{
				return calculateEditDistance(theSubgroup.getDAG());
			}
			default :
			{
				Log.logCommandLine(
						String.format("%s.calculate(): invalid measure %s (only '%s' and '%s' allowed).",
								QualityMeasure.class.getSimpleName(),
								itsQualityMeasure,
								QM.WEED,
								QM.EDIT_DISTANCE));

				/*
				 * if the QM is valid for this TargetType
				 * 	it is not implemented here
				 * else
				 * 	this method should not have been called
				 */
				if (QM.getQualityMeasures(TargetType.MULTI_LABEL).contains(itsQualityMeasure))
					throw new AssertionError(itsQualityMeasure);
				else
					throw new IllegalArgumentException("Invalid QM: " + itsQualityMeasure);
			}
		}
	}

	public float calculateEditDistance(DAG theDAG)
	{
		if (theDAG.getSize() != itsNrNodes)
		{
			Log.logCommandLine("Comparing incompatible DAG's. One has " + theDAG.getSize() + " nodes and the other has " + itsNrNodes + ". Throw pi.");
			return (float) Math.PI;
		}
		int nrEdits = 0;
		for (int i=0; i<itsNrNodes; i++)
			for (int j=0; j<i; j++)
				if ((theDAG.getNode(j).isConnected(i)==0 && itsDAG.getNode(j).isConnected(i)!=0) || (theDAG.getNode(j).isConnected(i)!=0 && itsDAG.getNode(j).isConnected(i)==0))
					nrEdits++;
				else if (theDAG.getNode(j).isConnected(i)==0)
						if (theDAG.testVStructure(j,i) != itsVStructures[j][i])
							nrEdits++;
		return (float) nrEdits / (float) (itsNrNodes*(itsNrNodes-1)/2); // Actually n choose 2, but this boils down to the same...
	}
}
