package nl.liacs.subdisc;

// XXX MM class may be useful for SubgroupROCPoint as well
/**
 Simple class for 2D points, having 2 labels.
 */
class HullPoint
{
	public final float itsX;
	public final float itsY;
	public final float itsLabel1;
	// not final, (re)set by ConvexHull, 'save' as class is package-private
	public float itsLabel2;

	public HullPoint(float theX, float theY, float theLabel1, float theLabel2)
	{
		itsX = theX;
		itsY = theY;
		itsLabel1 = theLabel1;
		itsLabel2 = theLabel2;
	}

	public HullPoint(HullPoint theOther)
	{
		this(theOther.itsX, theOther.itsY, theOther.itsLabel1, theOther.itsLabel2);
	}

	public void print()
	{
		Log.logCommandLine("HullPoint (" + itsX + "," + itsY + ") " + itsLabel1 + ", " + itsLabel2);
	}
}

/*
 * Class containing for maintaining and constructing convex hulls in 2D.
 * A hull is split into an upper and lower part for convenience.
 * Sorted by x coordinate.
 */
public class ConvexHull
{
	private HullPoint [][] itsHullPoints;

	private ConvexHull()
	{
		itsHullPoints = new HullPoint[2][];
	}

	/* construct single point hull
	 */
	public ConvexHull(float theX, float theY, float theLabel1, float theLabel2)
	{
		this();

		for (int aSide = 0; aSide < 2; aSide++)
		{
			itsHullPoints[aSide] = new HullPoint[1];
			itsHullPoints[aSide][0] = new HullPoint(theX, theY, theLabel1, theLabel2);
		}
	}

	public int getSize(int theSide)
	{
		return itsHullPoints[theSide].length;
	}

	public HullPoint getPoint(int theSide, int theIndex)
	{
		return itsHullPoints[theSide][theIndex];
	}

	/* assumes points on upper and lower hull are already 
	 * sorted by x coord hence linear time complexity
	 */
	public void grahamScanSorted()
	{
		for (int aSide = 0; aSide < 2; aSide++)
		{
			int aLen = itsHullPoints[aSide].length;

			if (aLen < 3)
				continue;

			int aSign = (aSide == 0) ? 1 : -1;

			int aPruneCnt = 0;
			int[] aNextList = new int[aLen];
			int[] aPrevList = new int[aLen];
			for (int i = 0; i < aLen; i++)
			{
				aNextList[i] = i + 1;
				aPrevList[i] = i - 1;
			}

			int aCurr = 0;
			while (aNextList[aCurr] < aLen && aNextList[aNextList[aCurr]] < aLen )
			{
				float aX1 = itsHullPoints[aSide][aCurr].itsX;
				float aY1 = itsHullPoints[aSide][aCurr].itsY;
				float aX2 = itsHullPoints[aSide][aNextList[aCurr]].itsX;
				float aY2 = itsHullPoints[aSide][aNextList[aCurr]].itsY;
				float aX3 = itsHullPoints[aSide][aNextList[aNextList[aCurr]]].itsX;
				float aY3 = itsHullPoints[aSide][aNextList[aNextList[aCurr]]].itsY;

				if ( aSign * (aY2-aY1) * (aX3-aX2) > aSign * (aY3-aY2) * (aX2-aX1) ) //convex, go to next point
				{
					aCurr = aNextList[aCurr];
				}
				else // not convex, remove middle point, go to previous point
				{
					aPrevList[aNextList[aNextList[aCurr]]] = aCurr;
					aNextList[aCurr] = aNextList[aNextList[aCurr]];
					aPruneCnt++;
					if (aCurr > 0)
						aCurr = aPrevList[aCurr];
				}
			}

			// put convexhullpoints in a new list
			HullPoint [] aNewHullPoints = new HullPoint[aLen - aPruneCnt];
			aCurr = 0;
			for (int i = 0; i < aNewHullPoints.length; i++)
			{
				aNewHullPoints[i] = itsHullPoints[aSide][aCurr];
				aCurr = aNextList[aCurr];
			}
			itsHullPoints[aSide] = aNewHullPoints;
		}
	}

	/*
	 * assumes this.x < theOther.x, i.e., no overlap between the hulls
	 * hence linear time complexity
	 */
	public ConvexHull concatenate(ConvexHull theOther)
	{
		ConvexHull aResult = new ConvexHull();

		for (int aSide = 0; aSide < 2; aSide++)
		{
			int aLen1 = itsHullPoints[aSide].length;
			int aLen2 = theOther.itsHullPoints[aSide].length;
			aResult.itsHullPoints[aSide] = new HullPoint[aLen1 + aLen2];
			for (int i = 0; i < aLen1; i++)
				aResult.itsHullPoints[aSide][i] = itsHullPoints[aSide][i];
			for (int i = 0; i < aLen2; i++)
				aResult.itsHullPoints[aSide][aLen1+i] = theOther.itsHullPoints[aSide][i];
		}

		aResult.grahamScanSorted();

		return aResult;
	}

	/* 
	 * Compute the Minkowski difference of two convex polygons.
	 * Again, linear time complexity.
	 */
	public ConvexHull minkowskiDifference(ConvexHull theOther)
	{
		return minkowskiDifference(theOther, true);
	}

	public ConvexHull minkowskiDifference(ConvexHull theOther, boolean thePruneDegenerate)
	{
		ConvexHull aResult = new ConvexHull();

		for (int aSide = 0 ; aSide < 2; aSide++)
		{
			int aSign = (aSide==0) ? 1 : -1 ;

			int aLen1 = itsHullPoints[aSide].length;
			int aLen2 = theOther.itsHullPoints[1-aSide].length;
			HullPoint[] aHull = new HullPoint[aLen1 + aLen2];
			int aHullSize = 0;

			int i = 0;
			int j = aLen2 - 1;
			float aSlope1, aSlope2;
			while (i < aLen1 - 1 || j > 0)
			{
				if (i == aLen1 - 1)
					aSlope1 = aSign * Float.NEGATIVE_INFINITY; // dummy for last
				else
					aSlope1 = (itsHullPoints[aSide][i+1].itsY - itsHullPoints[aSide][i].itsY) / (itsHullPoints[aSide][i+1].itsX - itsHullPoints[aSide][i].itsX + itsHullPoints[aSide][i+1].itsY - itsHullPoints[aSide][i].itsY);
				if (j == 0)
					aSlope2 = aSign * Float.NEGATIVE_INFINITY; // dummy for last
				else
					aSlope2 = (theOther.itsHullPoints[1-aSide][j-1].itsY - theOther.itsHullPoints[1-aSide][j].itsY) / (theOther.itsHullPoints[1-aSide][j-1].itsX - theOther.itsHullPoints[1-aSide][j].itsX + theOther.itsHullPoints[1-aSide][j-1].itsY - theOther.itsHullPoints[1-aSide][j].itsY);

				if (aSign * aSlope1 >= aSign * aSlope2)
				{
					aHull[aHullSize] = new HullPoint(itsHullPoints[aSide][i]);
					aHull[aHullSize].itsLabel2 = aSide; 
					aHullSize++;
					i++;
				}
				if (aSign * aSlope1 <= aSign * aSlope2)
				{
					aHull[aHullSize] = new HullPoint(theOther.itsHullPoints[1-aSide][j]);
					aHull[aHullSize].itsLabel2 = 1 - aSide;
					aHullSize++;
					j--;
				}
			}
			aHull[aHullSize] = new HullPoint(itsHullPoints[aSide][i]);
			aHull[aHullSize].itsLabel2 = aSide;
			aHullSize++;
			aHull[aHullSize] = new HullPoint(theOther.itsHullPoints[1-aSide][j]);
			aHull[aHullSize].itsLabel2 = 1 - aSide;
			aHullSize++;

			// build final hull
			HullPoint[] aNewHull = new HullPoint[aHullSize];

			for (int k = 0; k < aHullSize; k++)
			{
				int aVertex = k;
				int aNextVertex = (aVertex + 1) % aHullSize;
				while (aHull[aVertex].itsLabel2 == aHull[aNextVertex].itsLabel2)
					aNextVertex = (aNextVertex + 1) % aHullSize;
				if (aHull[aNextVertex].itsLabel1 >= aHull[aVertex].itsLabel1)
				{
					int tmp=aNextVertex; aNextVertex=aVertex; aVertex=tmp;
				}
				// set its own label and the other's label; for intevals these are the rhs and lhs end points, resp.
				aNewHull[k] = new HullPoint(aHull[aVertex].itsX - aHull[aNextVertex].itsX, aHull[aVertex].itsY - aHull[aNextVertex].itsY, aHull[aVertex].itsLabel1, aHull[aNextVertex].itsLabel1);
			}

			aResult.itsHullPoints[aSide] = aNewHull;
		}

		if (thePruneDegenerate)
			aResult.grahamScanSorted();

		return aResult;
	}
}
