package nl.liacs.subdisc;

import java.util.*;

public class CrossValidation
{
	private int itsSize;
	private int itsK;
	private static Random itsRandom;
	private int[] itsSets;

	public CrossValidation(int theSize, int theK)
	{
		itsSize = theSize;
		itsK = theK;
		itsRandom = new Random(System.currentTimeMillis());
		createTestSets();
	}

	/**
	 * returns a random permutation of the integers [1,...,itsSize].
	 * To be used for cross-validation.
	 */
	public int[] getRandomPermutation()
	{
		int[] result = new int[itsSize];

		// initialize result array to be [1,2,...,itsSize]
		for (int i=0; i<itsSize; i++)
			result[i] = i+1;

		// Knuth shuffle
		// notice i>1 in for-loop; for i=1 we will always swap the first
		// element with itself, hence we can skip this step
		for (int i=itsSize; i>1; i--)
		{
			int aSwitchIndex = itsRandom.nextInt(i);
			int aTemp = result[aSwitchIndex];
			result[aSwitchIndex] = result[i-1];
			result[i-1] = aTemp;
		}

		return result;
	}

	/**
	 * Generates k test sets for cross-validation. Used by constructor, but
	 * can also be used to recompute random test-sets.
	 */
	public void createTestSets()
	{
		// generate the random permutation on basis of which the k test sets will be filled
		int[] aRandomPermutation = getRandomPermutation();
		itsSets = new int[itsSize];

		for (int i=0; i<itsSize; i++)
			itsSets[aRandomPermutation[i]-1] = i % itsK;
	}

	public boolean isInTestSet(int theIndex, int theTestSet)
	{
		return (itsSets[theIndex] == theTestSet);
	}

	public int getTestSetNumber(int theIndex)
	{
		return itsSets[theIndex];
	}

	/**
	 * Produces a BitSet based on the different folds computed.
	 * @param theInvert determines whether you want the (small) test-set
	 * (false), or the inverse (large) training-set (true).
	 * Typical value in Cortana is "false".
	 */
	public BitSet getSet(int theTestSet, boolean theInvert)
	{
		BitSet aResult = new BitSet(itsSize);

		for (int i=0; i<itsSize; i++)
		{
			boolean aWithin = ((isInTestSet(i, theTestSet)) != theInvert);
			aResult.set(i, aWithin);
		}

//		TODO test
//		for (int i = 0, j = itsSize; i < j; ++i)
//			if ((isInTestSet(i, theTestSet)) != theInvert)
//				aResult.set(i);

		return aResult;
	}
}
