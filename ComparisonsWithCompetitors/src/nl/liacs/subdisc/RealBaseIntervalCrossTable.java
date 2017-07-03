package nl.liacs.subdisc;

import java.util.*;

//Michael says: this is basically a copy of NominalCrossTable

public class RealBaseIntervalCrossTable
{
	private float[] itsSplitPoints;
	private int itsSplitPointCount;
	private int[] itsPositiveCounts;
	private int[] itsNegativeCounts;
	private int itsPositiveCount; //sum
	private int itsNegativeCount; //sum
	private boolean itsUseNegInfty;

	public RealBaseIntervalCrossTable(float[] theSplitPoints, Column theColumn, Subgroup theSubgroup, BitSet theTarget)
	{
		this(theSplitPoints, theColumn, theSubgroup, theTarget, true);
	}

	public RealBaseIntervalCrossTable(float[] theSplitPoints, Column theColumn, Subgroup theSubgroup, BitSet theTarget, boolean theUseNegInfty)
	{
		itsUseNegInfty = theUseNegInfty;
		itsSplitPointCount = theSplitPoints.length + (itsUseNegInfty ? 1 : 0);
		itsSplitPoints = new float[itsSplitPointCount];
		itsPositiveCounts = new int[getNrBaseIntervals()];
		itsNegativeCounts = new int[getNrBaseIntervals()];

		int aCount = 0;
		if (itsUseNegInfty)
		{
			itsSplitPoints[0] = Float.NEGATIVE_INFINITY;
			aCount = 1;
		}
		for (float aSplitPoint : theSplitPoints)
		{
			itsSplitPoints[aCount] = aSplitPoint;
			aCount++;
		}
		//sort(itsSplitPoints);

		for (int i=0; i<theColumn.size(); i++) //loop over all records (AK could be faster? ok for now)
		{
			if (theSubgroup.covers(i))
			{
				float aValue = theColumn.getFloat(i);
				int anIndex = Arrays.binarySearch(itsSplitPoints, aValue);
				if (anIndex < 0)
					anIndex = -anIndex - 1;
				if (theTarget.get(i))
					itsPositiveCounts[anIndex]++;
				else
					itsNegativeCounts[anIndex]++;
			}
		}
		for (int i=0; i<getNrBaseIntervals(); i++)
		{
			itsPositiveCount += itsPositiveCounts[i];
			itsNegativeCount += itsNegativeCounts[i];
		}
	}

	public float getSplitPoint(int theIndex)
	{
		return itsSplitPoints[theIndex];
	}

	public Interval getBaseInterval(int theIndex)
	{
		if (itsSplitPointCount == 0)
			return new Interval(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
		if (theIndex == 0)
			return new Interval(Float.NEGATIVE_INFINITY, itsSplitPoints[0]);
		else if (theIndex == itsSplitPointCount)
			return new Interval(itsSplitPoints[theIndex-1], Float.POSITIVE_INFINITY);
		else
			return new Interval(itsSplitPoints[theIndex-1], itsSplitPoints[theIndex]);
	}

	public int getPositiveCount(int theIndex)
	{
		return itsPositiveCounts[theIndex];
	}

	public int getNegativeCount(int theIndex)
	{
		return itsNegativeCounts[theIndex];
	}

	public int getPositiveCount()
	{ 
		return itsPositiveCount;
	}

	public int getNegativeCount()
	{
		return itsNegativeCount;
	}

	public int getNrSplitPoints()
	{
		return itsSplitPointCount;
	}

	public int getNrBaseIntervals() {
		return itsSplitPointCount + 1; 
	}

	public float[] getSplitPoints()
	{
		return Arrays.copyOfRange(itsSplitPoints, 0, itsSplitPointCount);
	}

	// eliminate split points that separate base intervals with equal distributions
	// only to be used for convex quality measures
	public void aggregateIntervals()
	{
		int aPruneCnt = 0;
		for (int i = (itsUseNegInfty ? 1: 0); i < itsSplitPointCount; i++)
		{
			if ( itsPositiveCounts[i] * itsNegativeCounts[i+1] == itsPositiveCounts[i+1] * itsNegativeCounts[i] )
			{
				itsPositiveCounts[i-aPruneCnt] += itsPositiveCounts[i+1];
				itsNegativeCounts[i-aPruneCnt] += itsNegativeCounts[i+1];
				aPruneCnt++;
			}
			else if (aPruneCnt > 0)
			{
				itsPositiveCounts[i-aPruneCnt+1] = itsPositiveCounts[i+1];
				itsNegativeCounts[i-aPruneCnt+1] = itsNegativeCounts[i+1];
				itsSplitPoints[i-aPruneCnt] = itsSplitPoints[i];
			}
		}

		itsSplitPointCount -= aPruneCnt;

		return;
	}

	public void print()
	{
		for (int i = 0; i < getNrBaseIntervals(); i++)
			Log.logCommandLine(getBaseInterval(i) + ": (" + itsPositiveCounts[i] + ", " + itsNegativeCounts[i] + ")");
		return;
	}
}
