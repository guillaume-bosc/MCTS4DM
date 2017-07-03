package nl.liacs.subdisc;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import javax.swing.*;

public class SubgroupDiscovery extends MiningAlgorithm
{
	private final Table itsTable;
	private final int itsNrRows;		// itsTable.getNrRows()
	private final int itsMinimumCoverage;	// itsSearchParameters.getMinimumCoverage();
	private final int itsMaximumCoverage;	// itsNrRows * itsSearchParameters.getMaximumCoverageFraction();

	private final QualityMeasure itsQualityMeasure;
	private final float itsQualityMeasureMinimum;	// itsSearchParameters.getQualityMeasureMinimum();
	private boolean ignoreQualityMinimum = false; //used for swap-randomization purposes, and to get random qualities

	private SubgroupSet itsResult;
	private CandidateQueue itsCandidateQueue;
	private AtomicInteger itsCandidateCount = new AtomicInteger(0);

	//target concept type-specific information, including base models
	private BitSet itsBinaryTarget;		//SINGLE_NOMINAL
	private Column itsNumericTarget;	//SINGLE_NUMERIC
	private Column itsPrimaryColumn;	//DOUBLE_CORRELATION / DOUBLE_REGRESSION
	private Column itsSecondaryColumn;	//DOUBLE_CORRELATION / DOUBLE_REGRESSION
	private CorrelationMeasure itsBaseCM;	//DOUBLE_CORRELATION
	private RegressionMeasure itsBaseRM;	//DOUBLE_REGRESSION
	private BinaryTable itsBinaryTable;	//MULTI_LABEL
	private List<Column> itsTargets;	//MULTI_LABEL

	private LocalKnowledge itsLocalKnowledge; //PROPENSITY SCORE BASED
	private GlobalKnowledge itsGlobalKnowledge;//PROPENSITY SCORE BASED

	private int itsBoundSevenCount;
	private int itsBoundSixCount;
	private int itsBoundFiveCount;
	private int itsBoundFourCount;
	private int itsBoundSevenFired;
	private int itsBoundSixFired;
	private int itsBoundFiveFired;
	private int itsBoundFourFired;
	private int itsRankDefCount;

	private TreeSet<Candidate> itsBuffer;
	private JFrame itsMainWindow; //for feeding back progress info

	//SINGLE_NOMINAL
	public SubgroupDiscovery(SearchParameters theSearchParameters, Table theTable, int theNrPositive, JFrame theMainWindow)
	{
		super(theSearchParameters);
		itsTable = theTable;
		itsNrRows = itsTable.getNrRows();
		itsMainWindow = theMainWindow;
		itsMinimumCoverage = itsSearchParameters.getMinimumCoverage();
		itsMaximumCoverage = (int) (itsNrRows * itsSearchParameters.getMaximumCoverageFraction());
		itsQualityMeasure = new QualityMeasure(itsSearchParameters.getQualityMeasure(), itsNrRows, theNrPositive);
		itsQualityMeasureMinimum = itsSearchParameters.getQualityMeasureMinimum();

		TargetConcept aTC = itsSearchParameters.getTargetConcept();
		Condition aCondition = new Condition(aTC.getPrimaryTarget(), Operator.EQUALS);
		aCondition.setValue(aTC.getTargetValue());
		itsBinaryTarget = aTC.getPrimaryTarget().evaluate(aCondition);

		itsResult = new SubgroupSet(itsSearchParameters.getMaximumSubgroups(), itsNrRows, itsBinaryTarget);
	}

	//SINGLE_NUMERIC, float > signature differs from multi-label constructor
	public SubgroupDiscovery(SearchParameters theSearchParameters, Table theTable, float theAverage, JFrame theMainWindow)
	{
		super(theSearchParameters);
		itsTable = theTable;
		itsNrRows = itsTable.getNrRows();
		itsMainWindow = theMainWindow;
		itsMinimumCoverage = itsSearchParameters.getMinimumCoverage();
		itsMaximumCoverage = (int) (itsNrRows * itsSearchParameters.getMaximumCoverageFraction());
		TargetConcept aTC = itsSearchParameters.getTargetConcept();
		itsNumericTarget = aTC.getPrimaryTarget();

		BitSet aBitSet = new BitSet();
		aBitSet.set(0, itsNrRows);
		float[] aCounts = itsNumericTarget.getStatistics(aBitSet, false);
		ProbabilityDensityFunction aPDF = new ProbabilityDensityFunction(itsNumericTarget);
		aPDF.smooth();

		itsQualityMeasure = new QualityMeasure(itsSearchParameters.getQualityMeasure(), itsNrRows, aCounts[0], aCounts[1], aPDF);
		itsQualityMeasureMinimum = itsSearchParameters.getQualityMeasureMinimum();

		itsResult = new SubgroupSet(itsSearchParameters.getMaximumSubgroups(), itsNrRows, null); //TODO
	}

	//DOUBLE_CORRELATION and DOUBLE_REGRESSION
	public SubgroupDiscovery(SearchParameters theSearchParameters, Table theTable, boolean isRegression, JFrame theMainWindow)
	{
		super(theSearchParameters);
		itsTable = theTable;
		itsNrRows = itsTable.getNrRows();
		itsMainWindow = theMainWindow;
		itsMinimumCoverage = itsSearchParameters.getMinimumCoverage();
		itsMaximumCoverage = (int) (itsNrRows * itsSearchParameters.getMaximumCoverageFraction());
		itsQualityMeasure = new QualityMeasure(itsSearchParameters.getQualityMeasure(), itsNrRows, 100); //TODO
		itsQualityMeasureMinimum = itsSearchParameters.getQualityMeasureMinimum();

		//N.B.: Temporary lines for fetching Cook's experimental statistics
		if (theSearchParameters.getQualityMeasure() == QM.COOKS_DISTANCE)
		{
			Log.REFINEMENTLOG = true;
			Log.openFileOutputStreams();
//			Log.logRefinement("Bound graph for "+itsTable.getName());
//			Log.logRefinement("SubgroupSize,AvgRegressionTime,AvgCook,AvgBoundSeven,AvgBoundSix,AvgBoundFive,AvgBoundFour,CookComputable,BoundSevenComputable,BoundSixComputable,BoundFiveComputable,BoundFourComputable");
		}

		TargetConcept aTC = itsSearchParameters.getTargetConcept();
// TODO for stable jar, initiated here, SubgroupDiscovery revision 893 moved this to else below
		itsPrimaryColumn = aTC.getPrimaryTarget();
		itsSecondaryColumn = aTC.getSecondaryTarget();
		if (isRegression)
		{
// TODO RegressionMeasure revision 851 introduces the new RegressionMeasure constructor below (not mentioned in log)
			itsBaseRM = new RegressionMeasure(itsSearchParameters.getQualityMeasure(), itsPrimaryColumn, itsSecondaryColumn);
// TODO for stable jar, disabled, causes compile errors, reinstate later
//			itsBaseRM = new RegressionMeasure(itsSearchParameters.getQualityMeasure(), aTC);

			//initialize bounds
			itsBoundSevenCount=0;
			itsBoundSixCount=0;
			itsBoundFiveCount=0;
			itsBoundFourCount=0;
			itsBoundSevenFired=0;
			itsBoundSixFired=0;
			itsBoundFiveFired=0;
			itsBoundFourFired=0;
			itsRankDefCount=0;

			itsBuffer = new TreeSet<Candidate>();

// temp for testing
			//generateBoundGraph();
		}
		else
		{
// TODO for stable jar, disabled, initiated above, reinstate later as per SubgroupDiscovery revision 893
//			itsPrimaryColumn = aTC.getPrimaryTarget();
//			itsSecondaryColumn = aTC.getSecondaryTarget();
			itsBaseCM = new CorrelationMeasure(itsSearchParameters.getQualityMeasure(), itsPrimaryColumn, itsSecondaryColumn);
		}

		itsResult = new SubgroupSet(itsSearchParameters.getMaximumSubgroups());
	}

	//MULTI_LABEL
	public SubgroupDiscovery(SearchParameters theSearchParameters, Table theTable, JFrame theMainWindow)
	{
		super(theSearchParameters);
		itsTable = theTable;
		itsNrRows = itsTable.getNrRows();
		itsMainWindow = theMainWindow;
		itsMinimumCoverage = itsSearchParameters.getMinimumCoverage();
		itsMaximumCoverage = (int) (itsNrRows * itsSearchParameters.getMaximumCoverageFraction());

		//compute base model
		itsTargets = itsSearchParameters.getTargetConcept().getMultiTargets();
		itsBinaryTable = new BinaryTable(itsTable, itsTargets);

		Bayesian aBayesian = new Bayesian(itsBinaryTable, itsTargets);
		aBayesian.climb();

		itsQualityMeasure = new QualityMeasure(itsSearchParameters,
							aBayesian.getDAG(),
							itsNrRows);
		itsQualityMeasureMinimum = itsSearchParameters.getQualityMeasureMinimum();

		itsResult = new SubgroupSet(itsSearchParameters.getMaximumSubgroups());
	}

	/*
	 * Only the top result is used in this setting. Maximum coverage and
	 * binary target constructor parameters are not needed.
	 * NOTE this is a failed attempt to speedup calculation in the
	 * swap-randomise setting. Storing just the top-1 result is only
	 * sufficient for the last depth.
	 * It may be enabled again in the future, LEAVE IT IN.
	 */
	//protected void useSwapRandomisationSetting() {
	//	itsResult.useSwapRandomisationSetting();
	//}

	public void ignoreQualityMinimum()
	{
		ignoreQualityMinimum = true;
	}

	public void mine(long theBeginTime)
	{
		//make subgroup to start with, containing all elements
		BitSet aBitSet = new BitSet(itsNrRows);
		aBitSet.set(0, itsNrRows);
		Subgroup aStart = new Subgroup(null, aBitSet, itsResult);

		itsCandidateQueue = new CandidateQueue(itsSearchParameters, new Candidate(aStart));

		int aSearchDepth = itsSearchParameters.getSearchDepth();

		long theEndTime = theBeginTime + (((long) itsSearchParameters.getMaximumTime()) * 60 * 1000);
		if (theEndTime <= theBeginTime)
			theEndTime = Long.MAX_VALUE;

		while ((itsCandidateQueue.size() > 0 ) && (System.currentTimeMillis() <= theEndTime))
		{
			Candidate aCandidate = itsCandidateQueue.removeFirst(); // take off first Candidate from Queue
			Subgroup aSubgroup = aCandidate.getSubgroup();

			if (aSubgroup.getDepth() < aSearchDepth)
			{
				RefinementList aRefinementList = new RefinementList(aSubgroup, itsTable, itsSearchParameters);

				for (int i = 0, j = aRefinementList.size(); i < j; i++)
				{
					if (System.currentTimeMillis() > theEndTime)
						break;

					Refinement aRefinement = aRefinementList.get(i);
					Condition aCondition = aRefinement.getCondition();
					// if refinement is (num_attr = value) then treat it as nominal
					if (aCondition.getColumn().getType() == AttributeType.NUMERIC && aCondition.getOperator() != Operator.EQUALS)
						evaluateNumericRefinements(aSubgroup, aRefinement);
					else
						evaluateNominalBinaryRefinements(aSubgroup, aRefinement);
				}
			}

			if (itsCandidateQueue.size() == 0)
				flushBuffer();
		}
		Log.logCommandLine("number of candidates: " + itsCandidateCount.get());
		if (itsSearchParameters.getQualityMeasure() == QM.COOKS_DISTANCE)
		{
			Log.logCommandLine("Bound seven computed " + getNrBoundSeven() + " times");
			Log.logCommandLine("Bound six   computed " + getNrBoundSix() + " times");
			Log.logCommandLine("Bound five  computed " + getNrBoundFive() + " times");
			Log.logCommandLine("Bound four  computed " + getNrBoundFour() + " times");
			Log.logCommandLine("Bound seven fired " + getNrBoundSevenFired() + " times");
			Log.logCommandLine("Bound six   fired " + getNrBoundSixFired() + " times");
			Log.logCommandLine("Bound five  fired " + getNrBoundFiveFired() + " times");
			Log.logCommandLine("Bound four  fired " + getNrBoundFourFired() + " times");
			Log.logCommandLine("Rank deficient models: " + getNrRankDef());
		}
		Log.logCommandLine("number of subgroups: " + getNumberOfSubgroups());

		itsResult.setIDs(); //assign 1 to n to subgroups, for future reference in subsets
		if ((itsSearchParameters.getTargetType() == TargetType.MULTI_LABEL) && itsSearchParameters.getPostProcessingDoAutoRun())
			postprocess();

		//now just for cover-based beam search post selection
		// TODO MM see note at SubgroupSet.postProcess(), all itsResults will remain in memory
		itsResult = itsResult.postProcess(itsSearchParameters.getSearchStrategy());

		// in MULTI_LABEL, order may have changed
		// in COVER_BASED_BEAM_SELECTION, subgroups may have been removed
		itsResult.setIDs(); //assign 1 to n to subgroups, for future reference in subsets
	}

	private void evaluateNumericRefinements(Subgroup theSubgroup, Refinement theRefinement)
	{
		final int anOldCoverage = theSubgroup.getCoverage();

		switch (itsSearchParameters.getNumericStrategy())
		{
			case NUMERIC_ALL :
			{
				float[] aSplitPoints = theRefinement.getCondition().getColumn().getUniqueNumericDomain(theSubgroup.getMembers());
				for (float aSplit : aSplitPoints)
				{
					Subgroup aNewSubgroup = theRefinement.getRefinedSubgroup(Float.toString(aSplit));
					//addToBuffer(aNewSubgroup);
					checkAndLog(aNewSubgroup, anOldCoverage);
				}
				break;
			}
			case NUMERIC_BINS :
			{
				//this is the crucial translation from nr bins to nr splitpoint
				// code does nothing if aNrSplitPoints == 0
				int aNrSplitPoints = itsSearchParameters.getNrBins() - 1;

				float[] aSplitPoints = theRefinement.getCondition().getColumn().getSplitPoints(theSubgroup.getMembers(), aNrSplitPoints);
				boolean first = true;
				for (int j=0; j<aNrSplitPoints; j++)
				{
					if (first || aSplitPoints[j] != aSplitPoints[j-1])
					{
						Subgroup aNewSubgroup = theRefinement.getRefinedSubgroup(Float.toString(aSplitPoints[j]));
						//addToBuffer(aNewSubgroup);
						checkAndLog(aNewSubgroup, anOldCoverage);
					}
					first = false;
				}
				break;
			}
			case NUMERIC_BEST :
			{
				float[] aSplitPoints = theRefinement.getCondition().getColumn().getUniqueNumericDomain(theSubgroup.getMembers());
				float aMax = Float.NEGATIVE_INFINITY;
				Subgroup aBestSubgroup = null;
				Subgroup aNewSubgroup;
				for (float aSplit : aSplitPoints)
				{
					aNewSubgroup = theRefinement.getRefinedSubgroup(Float.toString(aSplit));

					final int aNewCoverage = aNewSubgroup.getCoverage();
					if (aNewCoverage >= itsMinimumCoverage && aNewCoverage <= itsMaximumCoverage && aNewCoverage < anOldCoverage)
					{
						float aQuality = evaluateCandidate(aNewSubgroup);
						if (aQuality > aMax)
						{
							aMax = aQuality;
							aNewSubgroup.setMeasureValue(aQuality);
							aBestSubgroup = aNewSubgroup;
						}
					}
				}

				//add best
				if (aBestSubgroup!=null) //at least one threshold found that has enough quality and coverage
					//addToBuffer(aBestSubgroup);
					// unnecessarily re-evaluates result
					checkAndLog(aBestSubgroup, anOldCoverage);

				break;
			}
			case NUMERIC_INTERVALS :
			{
				float[] aSplitPoints = theRefinement.getCondition().getColumn().getUniqueNumericDomain(theSubgroup.getMembers());
				RealBaseIntervalCrossTable aRBICT = new RealBaseIntervalCrossTable(aSplitPoints, theRefinement.getCondition().getColumn(), theSubgroup, itsBinaryTarget);

				// prune splitpoints for which adjacent base intervals have equal class distribution
				// TODO: check whether this preprocessing reduces *total* computation time
				aRBICT.aggregateIntervals();
				if (aRBICT.getNrSplitPoints() == 0)
				{
					break; // no specialization improves quality
				}

				double aBestQuality = Double.NEGATIVE_INFINITY;
				Interval aBestInterval = new Interval(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);

				if (false && itsSearchParameters.getQualityMeasure() == QM.WRACC)
				{
					long aPg = (long)itsQualityMeasure.getNrPositives();
					long aNg = (long)itsQualityMeasure.getNrRecords() - aPg;
					int aPr = aRBICT.getPositiveCount();
					int aNr = aRBICT.getNegativeCount();
					int aPl = aPr;
					int aNl = aNr;

					long aMaxH = aPr * aNg - aNr * aPg;
					float aBestLHS = Float.NEGATIVE_INFINITY;

					for (int i = 0; i < aRBICT.getNrBaseIntervals(); i++)
					{
						long anH = aPr * aNg - aNr * aPg;
						if (anH > aMaxH)
						{
							aMaxH = anH;
							aBestLHS = aRBICT.getSplitPoint(i - 1);
							aPl = aPr;
							aNl = aNr;
						}
						aPr -= aRBICT.getPositiveCount(i);
						aNr -= aRBICT.getNegativeCount(i);
						double aQuality = itsQualityMeasure.calculate(aPl - aPr, aPl + aNl - aPr - aNr);
						if (aQuality > aBestQuality) {
							aBestQuality = aQuality;
							if (i == aRBICT.getNrSplitPoints())
								aBestInterval = new Interval(aBestLHS, Float.POSITIVE_INFINITY);
							else
								aBestInterval = new Interval(aBestLHS, aRBICT.getSplitPoint(i));
						}
					}
				}
				else // not WRAcc
				{
					// brute force method, keep for now for testing purposes
					/*
					aSplitPoints = aRBICT.getSplitPoints();
					for (int i=0; i<aSplitPoints.length; i++)
					{
						Interval aNewInterval = new Interval(aSplitPoints[i], Float.POSITIVE_INFINITY);
						Subgroup aNewSubgroup = theRefinement.getRefinedSubgroup(aNewInterval);
						double aQuality = evaluateCandidate(aNewSubgroup);
						if (aQuality > aBestQuality) {
							aBestQuality = aQuality;
							aBestInterval = aNewInterval;
						}
						aNewInterval = new Interval(Float.NEGATIVE_INFINITY, aSplitPoints[i]);
						aNewSubgroup = theRefinement.getRefinedSubgroup(aNewInterval);
						aQuality = evaluateCandidate(aNewSubgroup);
						if (aQuality > aBestQuality) {
							aBestQuality = aQuality;
							aBestInterval = aNewInterval;
						}
						for (int j=i+1; j<aSplitPoints.length; j++)
						{
							aNewInterval = new Interval(aSplitPoints[i], aSplitPoints[j]);
							aNewSubgroup = theRefinement.getRefinedSubgroup(aNewInterval);
							aQuality = evaluateCandidate(aNewSubgroup);
							if (aQuality > aBestQuality) {
								aBestQuality = aQuality;
								aBestInterval = aNewInterval;
							}
						}
					}*/

					// the linear algo
					@SuppressWarnings("unused")
					int anEvalCounter = 0; // debug counter
					ConvexHull [] aHulls = new ConvexHull[aRBICT.getNrBaseIntervals()];
					int aPi = 0;
					int aNi = 0;
					for (int l = 0; l < aRBICT.getNrSplitPoints(); l++) {
						aPi += aRBICT.getPositiveCount(l);
						aNi += aRBICT.getNegativeCount(l);
						aHulls[l] = new ConvexHull(aNi, aPi, aRBICT.getSplitPoint(l), Float.NEGATIVE_INFINITY);
					}
					aHulls[aRBICT.getNrBaseIntervals()-1] = new ConvexHull(aRBICT.getNegativeCount(), aRBICT.getPositiveCount(), Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY);

					for (int k = aRBICT.getNrBaseIntervals(); k > 1; k = (k+1)/2)
					{
						for (int l = 0; l+1 < k; l += 2)
						{
							ConvexHull aMinkDiff = aHulls[l].minkowskiDifference(aHulls[l+1], true);
							for (int aSide = 0; aSide < 2; aSide++)
							{
								for (int i = 0; i < aMinkDiff.getSize(aSide); i++)
								{
									if (aSide == 1 && (i == 0 || i == aMinkDiff.getSize(aSide)-1) )
										continue; // no need to check duplicate hull points
									HullPoint aCandidate = aMinkDiff.getPoint(aSide, i);
									double aQuality = itsQualityMeasure.calculate(aCandidate.itsY, aCandidate.itsX + aCandidate.itsY);
									anEvalCounter++;
									if (aQuality > aBestQuality) {
										aBestQuality = aQuality;
										aBestInterval = new Interval(aCandidate.itsLabel2, aCandidate.itsLabel1);
									}
								}
							}
						}

						for (int l = 0; l+1 < k; l += 2)
							aHulls[l/2] = aHulls[l].concatenate(aHulls[l+1]);
						if (k % 2 == 1)
							aHulls[k/2] = aHulls[k-1];
					}

					//Log.logCommandLine("Evalutations: " + anEvalCounter);
				}

				Subgroup aNewSubgroup = theRefinement.getRefinedSubgroup(aBestInterval);
				checkAndLog(aNewSubgroup, anOldCoverage);

				break;
			}
			default :
			{
				Log.logCommandLine("SubgroupDiscovery.evaluateNumericRefinements(): unknown Numeric Strategy: " +
							itsSearchParameters.getNumericStrategy());
				break;
			}
		}
	}

	private void evaluateNominalBinaryRefinements(Subgroup theSubgroup, Refinement theRefinement)
	{
		final QM aQualityMeasure = itsSearchParameters.getQualityMeasure();
		Condition aCondition = theRefinement.getCondition();
		int anOldCoverage = theSubgroup.getCoverage();

		if (aCondition.getOperator() == Operator.ELEMENT_OF) //set-valued. Note that this implies that the target type is SINGLE_NOMINAL
		{
			NominalCrossTable aNCT = new NominalCrossTable(aCondition.getColumn(), theSubgroup, itsBinaryTarget);
			final SortedSet<String> aDomainBestSubSet = new TreeSet<String>();

			if (aQualityMeasure == QM.WRACC)
			{
				float aRatio = itsQualityMeasure.getNrPositives() / (float)(itsQualityMeasure.getNrRecords());
				for (int i = 0; i < aNCT.size(); i++)
				{
					int aPi = aNCT.getPositiveCount(i);
					int aNi = aNCT.getNegativeCount(i);
					// include values with WRAcc=0 too, result has same WRAcc but higher support
					if (aPi >= aRatio * (aPi + aNi))
						aDomainBestSubSet.add(aNCT.getValue(i));
				}
			}
			else // not WRACC
			{
				// construct and check all subsets on the convex hull
				final List<Integer> aSortedDomainIndices = aNCT.getSortedDomainIndices();
				final int aSortedDomainIndicesSize = aSortedDomainIndices.size();
				double aBestQuality = Double.NEGATIVE_INFINITY;

				// upper part of the hull
				int aP = 0;
				int aN = 0;
				int aPrevBestI = -1;
				for (int i = 0; i < aSortedDomainIndicesSize - 1; i++)
				{
					int anIndex = aSortedDomainIndices.get(i);
					int aPi = aNCT.getPositiveCount(anIndex);
					int aNi = aNCT.getNegativeCount(anIndex);
					aP += aPi;
					aN += aNi;
					int aNextIndex = aSortedDomainIndices.get(i+1);
					if (i < aSortedDomainIndicesSize-2 && aPi * aNCT.getNegativeCount(aNextIndex) == aNCT.getPositiveCount(aNextIndex) * aNi) // skip checking degenerate hull points
						continue;
					double aQuality = itsQualityMeasure.calculate(aP, aP + aN);
					if (aQuality > aBestQuality)
					{
						aBestQuality = aQuality;
						for (int j = aPrevBestI+1; j <= i; j++)
						{
							String aValue = aNCT.getValue(aSortedDomainIndices.get(j));
							aDomainBestSubSet.add(aValue);
						}
						aPrevBestI = i;
					}
				}

				// lower part of the hull
				// TODO: complete list of QMs
				boolean aLowIsDominatedQM = false;
				boolean anAsymmetricQM = true;
				if (aQualityMeasure == QM.BINOMIAL)
					aLowIsDominatedQM = true;
				if (aQualityMeasure == QM.CHI_SQUARED || aQualityMeasure == QM.INFORMATION_GAIN)
					anAsymmetricQM = false;

				if (false && !anAsymmetricQM) // TODO: fix this for depth > 1, check only upper OR lower hull
				{
					if (aDomainBestSubSet.size() > aNCT.size()/2) // prefer complement if smaller
					{
						aDomainBestSubSet.clear();
						for (int j = aPrevBestI + 1; j < aSortedDomainIndicesSize; j++)
						{
							String aValue = aNCT.getValue(aSortedDomainIndices.get(j));
							aDomainBestSubSet.add(aValue);
						}
					}
				}
				else if (true || !aLowIsDominatedQM)
				{
					aP = 0;
					aN = 0;
					aPrevBestI = -1;
					for (int i = aSortedDomainIndicesSize - 1; i > 0; i--)
					{
						int anIndex = aSortedDomainIndices.get(i).intValue();
						int aPi = aNCT.getPositiveCount(anIndex);
						int aNi = aNCT.getNegativeCount(anIndex);
						aP += aPi;
						aN += aNi;
						int aPrevIndex = aSortedDomainIndices.get(i-1);
						if (i > 1 && aPi * aNCT.getNegativeCount(aPrevIndex) == aNCT.getPositiveCount(aPrevIndex) * aNi)
							continue; // skip degenerate hull points
						double aQuality = itsQualityMeasure.calculate(aP, aP + aN);
						if (aQuality > aBestQuality)
						{
							aBestQuality = aQuality;
							if (aPrevBestI == -1)
							{
								aDomainBestSubSet.clear();
								aPrevBestI = aSortedDomainIndicesSize;
							}
							for (int j = aPrevBestI-1; j >= i; j--)
							{
								String aValue = aNCT.getValue(aSortedDomainIndices.get(j));
								aDomainBestSubSet.add(aValue);
							}
							aPrevBestI = i;
						}
					}
				}
			}

			if (aDomainBestSubSet.size() != 0)
			{
				final ValueSet aBestSubset = new ValueSet(aDomainBestSubSet);
				Subgroup aNewSubgroup = theRefinement.getRefinedSubgroup(aBestSubset);
				Log.logCommandLine("values: "  + aBestSubset);
				checkAndLog(aNewSubgroup, anOldCoverage);
			}
		}
		else //regular single-value conditions
		{
			for (String aValue : aCondition.getColumn().getDomain())
			{
				Subgroup aNewSubgroup = theRefinement.getRefinedSubgroup(aValue);
				checkAndLog(aNewSubgroup, anOldCoverage);
			}
		}
	}

	/*
	 * SubgroupsSet's add() method is thread save.
	 *
	 * CandidateQueue's add() method is thread save.
	 *
	 * itsCandidateCount is Atomic (synchronized by nature).
	 */
	private void checkAndLog(Subgroup theSubgroup, int theOldCoverage)
	{
		final int aNewCoverage = theSubgroup.getCoverage();

		if (aNewCoverage < theOldCoverage && aNewCoverage >= itsMinimumCoverage)
		{
			float aQuality = evaluateCandidate(theSubgroup);
			theSubgroup.setMeasureValue(aQuality);

			//if the quality is enough, or should be ignored, ...
			if (ignoreQualityMinimum || aQuality > itsQualityMeasureMinimum)
				//...and, the coverage is not too high
				if (aNewCoverage <= itsMaximumCoverage)
					itsResult.add(theSubgroup);

			itsCandidateQueue.add(new Candidate(theSubgroup));

			logCandidateAddition(theSubgroup);
		}
		itsCandidateCount.getAndIncrement();
	}

	private void logCandidateAddition(Subgroup theSubgroup)
	{
		StringBuffer sb = new StringBuffer(200);
		sb.append("candidate ");
		sb.append(theSubgroup.getConditions());
		sb.append(" size: ");
		sb.append(theSubgroup.getCoverage());
		Log.logCommandLine(sb.toString());

		Log.logCommandLine(String.format("  subgroup nr. %d; quality %s",
							itsCandidateCount.get(),
							Double.toString(theSubgroup.getMeasureValue())));
	}

	private float evaluateCandidate(Subgroup theNewSubgroup)
	{
		float aQuality = 0.0f;

		switch (itsSearchParameters.getTargetType())
		{
			case SINGLE_NOMINAL :
			{
				final int aCoverage = theNewSubgroup.getCoverage();
				final BitSet aMembers = theNewSubgroup.getMembers();
				// NOTE aMembers is a clone so this is safe
				aMembers.and(itsBinaryTarget);
				final int aCountHeadBody = aMembers.cardinality();
				final QM aMeasure = itsSearchParameters.getQualityMeasure();

				//Rob
				if ((aMeasure == QM.PROP_SCORE_WRACC) || (aMeasure == QM.PROP_SCORE_RATIO))
				{
					PropensityScore aPropensityScore = new PropensityScore(theNewSubgroup, itsBinaryTarget, itsLocalKnowledge, itsGlobalKnowledge, PropensityScore.LOGISTIC_REGRESSION);
//					double[] aScores = aPropensityScore.getPropensityScore();
					double aCountHeadPropensityScore = aPropensityScore.getPropensityScoreSum();
					System.out.println("Evaluating subgroup");
					//System.out.println(itsBinaryTarget.cardinality());
//					double aSumTest = 0.0;
//					for (int i = aMembers.nextSetBit(0); i >= 0; i = aMembers.nextSetBit(i+1))
//					{
//						aCountHeadPropensityScore += aScores[i];
//						//if (aPropensityScore.getPropensityScore()[i]>0.24){
//						//	aSumTest++;
//						//}
//						// count propensity score for all points in subgroup (aMembers)
//						if (itsBinaryTarget.get(i))
//							++aCountHeadBody;
//					}
					System.out.print("Count head:");
					System.out.println(aCountHeadBody);
					System.out.print("Count expected head:");
					System.out.println(aCountHeadPropensityScore);
					//System.out.print("Propensity score bigger than PT:");
					//System.out.println(aSumTest);
					//double aSum =0; // small check for propensity score (should sum to #target)
					//for (int i=0;i<aPropensityScore.getPropensityScore().length;i++){
					//	aSum = aSum+ aPropensityScore.getPropensityScore()[i];
					//}
					//System.out.println("Sum propensity score");
					//System.out.println(aSum);
					aQuality = QualityMeasure.calculatePropensityBased(aMeasure, aCountHeadBody, aCoverage, itsNrRows , aCountHeadPropensityScore);
				}
				else
				{
//					for (int i = aMembers.nextSetBit(0); i >= 0; i = aMembers.nextSetBit(i+1))
//						if (itsBinaryTarget.get(i))
//							++aCountHeadBody;
					aQuality = itsQualityMeasure.calculate(aCountHeadBody, aCoverage);
				}

				theNewSubgroup.setSecondaryStatistic(aCountHeadBody / (double) aCoverage); //relative occurence of positives in subgroup
				theNewSubgroup.setTertiaryStatistic(aCountHeadBody); //count of positives in the subgroup
				break;
			}
			case SINGLE_NUMERIC :
			{
				final BitSet aMembers = theNewSubgroup.getMembers();

				// what needs to be calculated for this QM
				// NOTE requiredStats could be a final SubgroupDiscovery.class member
				Set<Stat> aRequiredStats = QM.requiredStats(itsSearchParameters.getQualityMeasure());
				//float[] aCounts = itsNumericTarget.getStatistics(aMembers, aRequiredStats);
				float[] aCounts = itsNumericTarget.getStatistics(aMembers, itsSearchParameters.getQualityMeasure() == QM.MMAD);
				ProbabilityDensityFunction aPDF = null;
				if (aRequiredStats.contains(Stat.PDF))
				{
					aPDF = new ProbabilityDensityFunction(itsQualityMeasure.getProbabilityDensityFunction(), aMembers);
					aPDF.smooth();
				}

				aQuality = itsQualityMeasure.calculate(theNewSubgroup.getCoverage(), aCounts[0], aCounts[1], aCounts[2], aCounts[3], aPDF);
				theNewSubgroup.setSecondaryStatistic(aCounts[0]/(double)theNewSubgroup.getCoverage()); //average
				theNewSubgroup.setTertiaryStatistic(Math.sqrt(aCounts[1]/(double)theNewSubgroup.getCoverage())); // standard deviation
				break;
			}
			case DOUBLE_REGRESSION :
			{
				switch (itsBaseRM.itsQualityMeasure)
				{
					case LINEAR_REGRESSION:
					{
						RegressionMeasure aRM = new RegressionMeasure(itsBaseRM, theNewSubgroup.getMembers());
						aQuality = (float) aRM.getEvaluationMeasureValue();
						theNewSubgroup.setSecondaryStatistic(aRM.getSlope()); //slope
						theNewSubgroup.setTertiaryStatistic(aRM.getIntercept()); //intercept

						break;
					}
/*
 * LEAVE THIS CODE IN, it will be used one day
					case QualityMeasure.COOKS_DISTANCE:
					{
						// initialize variables
						double aThreshold = -Double.MAX_VALUE;
						boolean aNeedToComputeRegression = true;
						boolean aNeedToComputeBounds = true;

						// check what the pruning quality will be, if this exists at all
						int aBorderlineSubgroupNumber;
						if (theNewSubgroup.itsDepth < itsSearchParameters.getSearchDepth())
							aBorderlineSubgroupNumber = itsSearchParameters.getSearchStrategyWidth();
						else aBorderlineSubgroupNumber = itsSearchParameters.getMaximumSubgroups();
						// TODO these methods on itsResult are not (yet) thread save and will will cause
						// problems during concurrent access, easy to fix
						if ( itsResult.size() >= aBorderlineSubgroupNumber )
							aThreshold = itsResult.last().getMeasureValue();
						else { aNeedToComputeBounds = false; }

						// start actual computation
						Log.logCommandLine("");
						BitSet aMembers = theNewSubgroup.getMembers();
						int aSampleSize = aMembers.cardinality();

						// filter out rank deficient model that crash matrix multiplication library // TODO: should read <itsP instead of <2!!!
						if (aSampleSize<2)
						{
							itsRankDefCount++;
							return -Float.MAX_VALUE;
						}

						itsBaseRM.computeRemovedIndices(aMembers, aSampleSize);

						// calculate the upper bound values. Before each bound, only the necessary computations are done.
						if (aNeedToComputeBounds)
						{
							double aT = itsBaseRM.getT(aSampleSize);
							double aRSquared = itsBaseRM.getRSquared(aSampleSize);

							// bound seven
							double aBoundSeven = itsBaseRM.computeBoundSeven(aSampleSize, aT, aRSquared);
							if (aBoundSeven<Double.MAX_VALUE)
							{
								Log.logCommandLine("                   Bound 7: " + aBoundSeven);
								itsBoundSevenCount++;
							}

							if (aBoundSeven < aThreshold)
							{
								aNeedToComputeRegression = false;
								itsBoundSevenFired++;
							}
							else
							{	// bound six
								double aBoundSix = itsBaseRM.computeBoundSix(aSampleSize, aT);
								if (aBoundSix<Double.MAX_VALUE)
								{
									Log.logCommandLine("                   Bound 6: " + aBoundSix);
									itsBoundSixCount++;
								}
								if (aBoundSix < aThreshold)
								{
									aNeedToComputeRegression = false;
									itsBoundSixFired++;
								}
								else
								{	// bound five
									double aBoundFive = itsBaseRM.computeBoundFive(aSampleSize, aRSquared);
									if (aBoundFive<Double.MAX_VALUE)
									{
										Log.logCommandLine("                   Bound 5: " + aBoundFive);
										itsBoundFiveCount++;
									}
									if (aBoundFive < aThreshold)
									{
										aNeedToComputeRegression = false;
										itsBoundFiveFired++;
									}
									else
									{	// bound four
										double aBoundFour = itsBaseRM.computeBoundFour(aSampleSize);
										if (aBoundFour<Double.MAX_VALUE)
										{
											Log.logCommandLine("                   Bound 4: " + aBoundFour);
											itsBoundFourCount++;
										}
										if (aBoundFour < aThreshold)
										{
											aNeedToComputeRegression = false;
											itsBoundFourFired++;
										}
									}
								}
							}
						}

						// finally, compute regression
						if (aNeedToComputeRegression)
						{
							double aDoubleQuality = itsBaseRM.calculate(theNewSubgroup);
							if (aDoubleQuality == -Double.MAX_VALUE)
								itsRankDefCount++;
							aQuality = (float) aDoubleQuality;
						}
						else aQuality = -Float.MAX_VALUE;
					}
*/
				}
				break;
			}
			case DOUBLE_CORRELATION :
			{
				CorrelationMeasure aCM = new CorrelationMeasure(itsBaseCM);
				for (int i = 0; i < itsNrRows; i++)
					if (theNewSubgroup.getMembers().get(i))
						aCM.addObservation(itsPrimaryColumn.getFloat(i), itsSecondaryColumn.getFloat(i));
				theNewSubgroup.setSecondaryStatistic(aCM.getCorrelation()); //correlation
				theNewSubgroup.setTertiaryStatistic(aCM.computeCorrelationDistance()); //intercept
				aQuality = (float) aCM.getEvaluationMeasureValue();
				break;
			}
			case MULTI_LABEL :
			{
				aQuality = multiLabelCalculate(theNewSubgroup); //also stores DAG in Subgroup
				theNewSubgroup.setSecondaryStatistic(itsQualityMeasure.calculateEditDistance(theNewSubgroup.getDAG())); //edit distance
				theNewSubgroup.setTertiaryStatistic(QualityMeasure.calculateEntropy(itsNrRows, theNewSubgroup.getCoverage())); //entropy
				break;
			}
			default : break;
		}
		return aQuality;
	}
/*
TODO for stable jar, disabled, causes compile errors, reinstate later
	private void generateBoundGraph()
	{
		for (int aSampleSize = itsMaximumCoverage-1; aSampleSize >= 2; aSampleSize--)
		{
			Log.logCommandLine("aSampleSize = " + aSampleSize);
			int aBoundSevenComputable = 0;
			int aBoundSixComputable = 0;
			int aBoundFiveComputable = 0;
			int aBoundFourComputable = 0;
			int aCookComputable = 0;
			double avgBoundSeven = 0.0;
			double avgBoundSix = 0.0;
			double avgBoundFive = 0.0;
			double avgBoundFour = 0.0;
			double avgCook = 0.0;
			long avgRegressionTime = 0;
			for (int i=0; i<100; i++)
			{
				Subgroup aSubgroup = itsTable.getRandomSubgroup(aSampleSize);
				BitSet aMembers = aSubgroup.getMembers();
				itsBaseRM.computeRemovedIndices(aMembers, aSampleSize);
				double aT = itsBaseRM.getT(aSampleSize);
				double aRSquared = itsBaseRM.getRSquared(aSampleSize);
				double aBoundSeven = itsBaseRM.computeBoundSeven(aSampleSize, aT, aRSquared);
				double aBoundSix = itsBaseRM.computeBoundSix(aSampleSize, aT);
				double aBoundFive = itsBaseRM.computeBoundFive(aSampleSize, aRSquared);
				double aBoundFour = itsBaseRM.computeBoundFour(aSampleSize);
				long aStartTime = System.currentTimeMillis();
				double aDoubleQuality = itsBaseRM.calculate(aSubgroup);
				long anEndTime = System.currentTimeMillis();
				if (aBoundSeven<Double.MAX_VALUE)
				{
					aBoundSevenComputable++;
					avgBoundSeven += aBoundSeven;
				}
				if (aBoundSix<Double.MAX_VALUE)
				{
					aBoundSixComputable++;
					avgBoundSix += aBoundSix;
				}
				if (aBoundFive<Double.MAX_VALUE)
				{
					aBoundFiveComputable++;
					avgBoundFive += aBoundFive;
				}
				if (aBoundFour<Double.MAX_VALUE)
				{
					aBoundFourComputable++;
					avgBoundFour += aBoundFour;
				}
				if (aDoubleQuality > -Double.MAX_VALUE)
				{
					aCookComputable++;
					avgCook += aDoubleQuality;
					avgRegressionTime += (anEndTime - aStartTime);
				}
			}
			if (aBoundSevenComputable>0)
				avgBoundSeven /= aBoundSevenComputable;
			if (aBoundSixComputable>0)
				avgBoundSix /= aBoundSixComputable;
			if (aBoundFiveComputable>0)
				avgBoundFive /= aBoundFiveComputable;
			if (aBoundFourComputable>0)
				avgBoundFour /= aBoundFourComputable;
			if (aCookComputable>0)
			{
				avgCook /= aCookComputable;
				avgRegressionTime /= aCookComputable;
			}
			Log.logRefinement(""+aSampleSize+","+avgRegressionTime+","+avgCook+","+avgBoundSeven+","+avgBoundSix+","+avgBoundFive+","+avgBoundFour+","+aCookComputable+","+aBoundSevenComputable+","+aBoundSixComputable+","+aBoundFiveComputable+","+aBoundFourComputable);
		}
	}
*/
	private float multiLabelCalculate(Subgroup theSubgroup)
	{
		BinaryTable aBinaryTable = itsBinaryTable.selectRows(theSubgroup.getMembers());
		Bayesian aBayesian = new Bayesian(aBinaryTable, itsTargets);
		aBayesian.climb(); //induce DAG
		DAG aDAG = aBayesian.getDAG();
		theSubgroup.setDAG(aDAG); //store DAG with subgroup for later use
		return itsQualityMeasure.calculate(theSubgroup);
	}

	private void postprocess()
	{
		if (itsResult.isEmpty())
			return;

		// Create quality measures on whole dataset
		Log.logCommandLine("Creating quality measures.");
		int aPostProcessingCount = itsSearchParameters.getPostProcessingCount();
		double aPostProcessingCountSquare = Math.pow(aPostProcessingCount, 2);
		int itsNrRecords = itsTable.getNrRows();

		QualityMeasure[] aQMs = new QualityMeasure[aPostProcessingCount];
		for (int i = 0; i < aPostProcessingCount; i++)
		{
			Bayesian aGlobalBayesian = new Bayesian(itsBinaryTable);
			aGlobalBayesian.climb();
			aQMs[i] = new QualityMeasure(itsSearchParameters, aGlobalBayesian.getDAG(), itsNrRecords);
		}

		// Iterate over subgroups
		SubgroupSet aNewSubgroupSet = new SubgroupSet(itsSearchParameters.getMaximumSubgroups());
		// most methods of SubgroupSet are not thread save, but this is
		// no problem for this method as it is run by a single thread
		// however all itsResult sets, of all refinement depths,  will
		// be kept in memory
		// see comment in SubgroupSet.postProcess()
		for (Subgroup s : itsResult)
		{
			Log.logCommandLine("Postprocessing subgroup " + s.getID());
			double aTotalQuality = 0.0;
			BinaryTable aSubgroupTable = itsBinaryTable.selectRows(s.getMembers());
			for (int i = 0; i < aPostProcessingCount; i++)
			{
				Bayesian aLocalBayesian = new Bayesian(aSubgroupTable);
				aLocalBayesian.climb();
				s.setDAG(aLocalBayesian.getDAG());
				for (int j = 0; j < aPostProcessingCount; j++)
					aTotalQuality += aQMs[j].calculate(s);
			}
			s.setMeasureValue(aTotalQuality / aPostProcessingCountSquare);
			s.renouncePValue();
			aNewSubgroupSet.add(s);
		}
		aNewSubgroupSet.setIDs();
		itsResult = aNewSubgroupSet;
	}

	public int getNumberOfSubgroups() { return itsResult.size(); }
	public SubgroupSet getResult() { return itsResult; }
	public void clearResult() { itsResult.clear(); }
	public BitSet getBinaryTarget() { return (BitSet)itsBinaryTarget.clone(); }
	public QualityMeasure getQualityMeasure() { return itsQualityMeasure; }
	public SearchParameters getSearchParameters() { return itsSearchParameters; }


	/*
	 * TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST
	 *
	 * same as public void mine(long theBeginTime)
	 * but allows nrThreads to be set
	 * use theNrThreads = 0 to run old mine(theBeginTime)
	 */
	public void mine(long theBeginTime, int theNrThreads)
	{
		final QM aQualityMeasure = itsSearchParameters.getQualityMeasure();

		//fill the conditionList of local and global knowledge, Rob
		if (aQualityMeasure == QM.PROP_SCORE_WRACC || aQualityMeasure == QM.PROP_SCORE_RATIO)
		{
			ExternalKnowledgeFileLoader extKnowledge;
			extKnowledge = new ExternalKnowledgeFileLoader(new File("").getAbsolutePath());
			extKnowledge.createConditionListLocal(itsTable);
			extKnowledge.createConditionListGlobal(itsTable);
			itsLocalKnowledge = new LocalKnowledge(extKnowledge.getLocal(), itsBinaryTarget);
			itsGlobalKnowledge = new GlobalKnowledge(extKnowledge.getGlobal(), itsBinaryTarget);
		}

		if (theNrThreads < 0)
		{
			mine(theBeginTime);
			return;
		}
		else if (theNrThreads == 0)
			theNrThreads = Runtime.getRuntime().availableProcessors();

		// make subgroup to start with, containing all elements
		BitSet aBitSet = new BitSet(itsNrRows);
		aBitSet.set(0, itsNrRows);
		Subgroup aStart = new Subgroup(null, aBitSet, itsResult);

		itsCandidateQueue = new CandidateQueue(itsSearchParameters, new Candidate(aStart));

		final int aSearchDepth = itsSearchParameters.getSearchDepth();

		long theEndTime = theBeginTime + (((long) itsSearchParameters.getMaximumTime()) * 60 * 1000);
		if (theEndTime <= theBeginTime)
			theEndTime = Long.MAX_VALUE;

		/*
		 * essential multi-thread setup
		 * uses semaphores so only nrThreads can run at the same time
		 * AND ExecutorService can only start new Test after old one
		 * completes
		 */
		ExecutorService es = Executors.newFixedThreadPool(theNrThreads);
		Semaphore s = new Semaphore(theNrThreads);

		while (System.currentTimeMillis() <= theEndTime)
		{
			// wait until a Thread becomes available
			try { s.acquire(); }
			catch (InterruptedException e) { e.printStackTrace(); }

			Candidate aCandidate = null;
			/*
			 * if other threads still have Candidates to add they
			 * are blocked from doing so through this lock on
			 * itsCandidateQueue, and therefore can not release
			 * their permit
			 * although they could have added their Candidates
			 * immediately prior this lock, without releasing their
			 * permit yet
			 *
			 * NOTE for beam search strategies (COVER-BASED/ BEAM)
			 * CandidateQueue will moveToNext level upon depletion
			 * of the current one, overriding the current one with
			 * the next, and creation a new next level
			 * therefore only after all but the last Candidates are
			 * processed (added to next level) can we take the last
			 * one and let the next level become the current
			 *
			 * NOTE 2 although individual methods of CandidateQueue
			 * are thread save, we need a compound action here
			 * so synchronized is still needed
			 */
			synchronized (itsCandidateQueue)
			{
				final int aTotalSize = itsCandidateQueue.size();
				final boolean alone = (s.availablePermits() == theNrThreads-1);
				// take off first Candidate from Queue
				if (itsCandidateQueue.currentLevelQueueSize() > 0)
					aCandidate = itsCandidateQueue.removeFirst();
				// obviously (currentLevelQueueSize <= 0)
				// take solely when this is only active thread
				else if ((aTotalSize > 0) && alone)
					aCandidate = itsCandidateQueue.removeFirst();
				// no other thread can add new candidates
				else if ((aTotalSize == 0) && alone)
					break;

				int aDepth = 1;
				String aCurrent = new String("(empty)");
				if (aCandidate != null)
				{
					aDepth = aCandidate.getSubgroup().getDepth()+1;
					aCurrent = aCandidate.getSubgroup().toString();
				}
				if (itsMainWindow != null)
					itsMainWindow.setTitle(new StringBuilder(aCurrent.length() + 32).append("d=")
													.append(aDepth)
													.append(", cands=")
													.append(itsCandidateCount.get())
													.append(", refining ")
													.append(aCurrent).toString());
			}

			if (aCandidate != null)
				es.execute(new Test(aCandidate, aSearchDepth, theEndTime, s));
			// queue was empty, but other threads were running, they
			// may be in the process of adding new Candidates
			// wait until at least one finishes, or this one becomes
			// only thread
			else
			{
				try
				{
					final int aNrFree = s.drainPermits();
					if (aNrFree < theNrThreads-1)
					{
						s.acquire();
						s.release(aNrFree+2);
					}
					else
						s.release(aNrFree+1);
					//continue;
				}
				catch (InterruptedException e) { e.printStackTrace(); }
			}
		}
		es.shutdown();
		// wait for last active threads to complete
		while(!es.isTerminated()) {};

		Log.logCommandLine("number of candidates: " + itsCandidateCount.get());
		if (aQualityMeasure == QM.COOKS_DISTANCE)
		{
			Log.logCommandLine("Bound seven computed " + getNrBoundSeven() + " times");
			Log.logCommandLine("Bound six   computed " + getNrBoundSix() + " times");
			Log.logCommandLine("Bound five  computed " + getNrBoundFive() + " times");
			Log.logCommandLine("Bound four  computed " + getNrBoundFour() + " times");
			Log.logCommandLine("Bound seven fired " + getNrBoundSevenFired() + " times");
			Log.logCommandLine("Bound six   fired " + getNrBoundSixFired() + " times");
			Log.logCommandLine("Bound five  fired " + getNrBoundFiveFired() + " times");
			Log.logCommandLine("Bound four  fired " + getNrBoundFourFired() + " times");
			Log.logCommandLine("Rank deficient models: " + getNrRankDef());
		}
		Log.logCommandLine("number of subgroups: " + getNumberOfSubgroups());

		itsResult.setIDs(); //assign 1 to n to subgroups, for future reference in subsets
		if ((itsSearchParameters.getTargetType() == TargetType.MULTI_LABEL) && itsSearchParameters.getPostProcessingDoAutoRun())
			postprocess();

		//now just for cover-based beam search post selection
		// TODO MM see note at SubgroupSet.postProcess(), all itsResults will remain in memory
		itsResult = itsResult.postProcess(itsSearchParameters.getSearchStrategy());

		// in MULTI_LABEL, order may have changed
		// in COVER_BASED_BEAM_SELECTION, subgroups may have been removed
		itsResult.setIDs(); //assign 1 to n to subgroups, for future reference in subsets
	}

	/*
	 * Essential Runnable, code copied from old mine().
	 * After Test is done, semaphore is release, so ExecutorService can
	 * start a new Test.
	 */
	private class Test implements Runnable
	{
		private final Candidate itsCandidate;
		private final int itsSearchDepth;
		private final long itsEndTime;
		private final Semaphore itsSemaphore;

		public Test(Candidate theCandidate, int theSearchDepth, long theEndTime, Semaphore theSemaphore)
		{
			itsCandidate = theCandidate;
			itsSearchDepth= theSearchDepth;
			itsEndTime = theEndTime;
			itsSemaphore = theSemaphore;
		}

		@Override
		public void run()
		{
			Subgroup aSubgroup = itsCandidate.getSubgroup();

			if (aSubgroup.getDepth() < itsSearchDepth)
			{
				RefinementList aRefinementList = new RefinementList(aSubgroup, itsTable, itsSearchParameters);

				for (int i = 0, j = aRefinementList.size(); i < j; i++)
				{
					if (System.currentTimeMillis() > itsEndTime)
						break;

					Refinement aRefinement = aRefinementList.get(i);
					Condition aCondition = aRefinement.getCondition();
					// if refinement is (num_attr = value) then treat it as nominal
					if (aCondition.getColumn().getType() == AttributeType.NUMERIC && aCondition.getOperator() != Operator.EQUALS)
						evaluateNumericRefinements(aSubgroup, aRefinement);
					else
						evaluateNominalBinaryRefinements(aSubgroup, aRefinement);
				}
			}
			itsSemaphore.release();
		}
	}
/*
TODO for stable jar, disabled, causes compile errors, reinstate later
	private void addToBuffer(Subgroup theSubgroup )
	{
		int aCoverage = theSubgroup.getCoverage();
		itsBaseRM.computeRemovedIndices(theSubgroup.getMembers(), aCoverage);
		itsBaseRM.updateSquaredResidualSum();
		itsBaseRM.updateRemovedTrace();
		double aPriority = itsBaseRM.computeBoundFour(aCoverage);
		Log.logCommandLine(theSubgroup.getConditions().toString() + " --- bound : " + aPriority);
		// @deprecated constructor
		itsBuffer.add(new Candidate(theSubgroup, aPriority));
	}
*/
	private void flushBuffer()
	{
		if (itsBuffer == null)
			return;
		Iterator<Candidate> anIterator = itsBuffer.iterator();
		while (anIterator.hasNext())
		{
			Candidate aCandidate = anIterator.next();
			Subgroup aSubgroup = aCandidate.getSubgroup();
			int anOldCoverage = itsTable.getNrRows(); // MM ?
			checkAndLog(aSubgroup, anOldCoverage);
		}
		itsBuffer = new TreeSet<Candidate>();
	}

	public int getNrBoundSeven() { return itsBoundSevenCount; }
	public int getNrBoundSix() { return itsBoundSixCount; }
	public int getNrBoundFive() { return itsBoundFiveCount; }
	public int getNrBoundFour() { return itsBoundFourCount; }
	public int getNrBoundSevenFired() { return itsBoundSevenFired; }
	public int getNrBoundSixFired() { return itsBoundSixFired; }
	public int getNrBoundFiveFired() { return itsBoundFiveFired; }
	public int getNrBoundFourFired() { return itsBoundFourFired; }
	public int getNrRankDef() { return itsRankDefCount; }

	/**
	 * Return the base {@link RegressionMeasure} for this SubgroupDiscovery.
	 *
	 * @return the base RegressionMeasure, if this SubgroupDiscovery is of
	 * {@link TargetType} {@link TargetType#DOUBLE_REGRESSION}, {@code null}
	 * otherwise.
	 */
	public RegressionMeasure getRegressionMeasureBase()
	{
		return itsBaseRM;
	}
}
