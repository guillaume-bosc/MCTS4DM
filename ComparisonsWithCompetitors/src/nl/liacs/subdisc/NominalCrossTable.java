package nl.liacs.subdisc;

import java.util.*;

public class NominalCrossTable
{
	private final String[] itsValues;
	private final int[] itsPositiveCounts;
	private final int[] itsNegativeCounts;
	private final int itsPositiveCount; //sum
	private final int itsNegativeCount; //sum

	public NominalCrossTable(Column theColumn, Subgroup theSubgroup, BitSet theTarget)
	{
		final BitSet aMembers = theSubgroup.getMembers();
		itsValues = theColumn.getUniqueNominalBinaryDomain(aMembers);
		itsPositiveCounts = new int[itsValues.length];
		itsNegativeCounts = new int[itsValues.length];

		int aPositiveCount = 0;
		int aNegativeCount = 0;
		// check only SG.members, combine 2 loops
		for (int i = aMembers.nextSetBit(0); i >= 0; i = aMembers.nextSetBit(i + 1))
		{
			int anIndex = Arrays.binarySearch(itsValues, theColumn.getNominal(i));
			if (theTarget.get(i))
			{
				++itsPositiveCounts[anIndex];
				++aPositiveCount;
			}
			else
			{
				++itsNegativeCounts[anIndex];
				++aNegativeCount;
			}
		}

		itsPositiveCount = aPositiveCount;
		itsNegativeCount = aNegativeCount;
	}

	public String getValue(int index) { return itsValues[index]; }
	public int getPositiveCount(int theIndex) { return itsPositiveCounts[theIndex]; }
	public int getNegativeCount(int theIndex) { return itsNegativeCounts[theIndex]; }
	public int size() { return itsValues.length; }

	// never used
	@Deprecated
	public int getPositiveCount() { return itsPositiveCount; }

	// never used
	@Deprecated
	public int getNegativeCount() { return itsNegativeCount; }

	// never used
	@Deprecated
	public HashSet<String> getDomain()
	{
		HashSet<String> aSet = new HashSet<String>();
		for (int i = 0; i < itsValues.length; i++)
			aSet.add(itsValues[i]);
		return aSet;
	}

	// Get the domain sorted by p/n
	// Michael says: rather cumbersome, there must be a cleaner way to do this
	// MM probably yes, will look at this later
	// TODO using / auto-(un)boxing of integer <-> Integer is extremely slow
	// TODO code inefficiently mixes / duplicates arrays (data) and List (view)
	public List<Integer> getSortedDomainIndices()
	{
		List<Integer> aSortedIndexList = new ArrayList<Integer>(itsValues.length);
		for (int i = 0; i < itsValues.length; i++)
			aSortedIndexList.add(new Integer(i));

		// as long a itsPositiveCounts / itsNegativeCounts do not change
		// CrossTableComparator could be saved as member of this class,
		// instead of being recreated for each call to getSortedDomainIndices
		// MM getSortedDomainIndices() is only called once for each
		// evaluated candidate, saving is not needed
		// also, although Subgroups build from different Conditions may
		// have the exact same members keeping every NCT for each BitSet
		// configuration is a bit over the top

		CrossTableComparator aCTC = new CrossTableComparator(itsPositiveCounts, itsNegativeCounts);

		boolean anOptimalSort = false;

		if (anOptimalSort)
		{
			sortValues(aSortedIndexList, 0, aSortedIndexList.size()-1, aCTC);
		}
		else
		{
			Collections.sort(aSortedIndexList, aCTC);
		}

		return aSortedIndexList;
	}

	public void print()
	{
		for (int i = 0; i < size(); i++)
			Log.logCommandLine(itsValues[i] + ": (" + itsPositiveCounts[i] + ", " + itsNegativeCounts[i] + ")");
	}

	/*
	 * Sort values based on pos/neg ratios.
	 * Complexity is asymptotically optimal, however, if aOptimalSort==false
	 * it reverts to java's builtin (merge) sort, which is not optimal but
	 * probably more optimized.
	 */
	private void sortValues(List<Integer> aSortedIndexList, int l, int r, CrossTableComparator aCTC)
	{
		if (r <= l)
			return;

		int i = l - 1;
		int j = r;
		int p = l - 1;
		int q = r;

		Integer arr = aSortedIndexList.get(r);
		for ( ; ; )
		{
			while (aCTC.compare(aSortedIndexList.get(++i), arr) < 0 );
			while (aCTC.compare(arr, aSortedIndexList.get(--j)) < 0 )
				if (j == l) break;
			if (i >= j) break;
			aSortedIndexList.set(j, aSortedIndexList.set(i, aSortedIndexList.get(j)));
			if (aCTC.compare(aSortedIndexList.get(i), arr) == 0) {
				p++;
				aSortedIndexList.set(i, aSortedIndexList.set(p, aSortedIndexList.get(i)));
			}
			if (aCTC.compare(arr, aSortedIndexList.get(j)) == 0) {
				q--;
				aSortedIndexList.set(q, aSortedIndexList.set(j, aSortedIndexList.get(q)));
			}
		}

		aSortedIndexList.set(r, aSortedIndexList.set(i, aSortedIndexList.get(r)));

		j = i - 1;
		i = i + 1;

		for (int k = l; k < p; k++, j--)
			aSortedIndexList.set(j, aSortedIndexList.set(k, aSortedIndexList.get(j)));
		for (int k = r-1; k > q; k--, i++)
			aSortedIndexList.set(k, aSortedIndexList.set(i, aSortedIndexList.get(k)));

		sortValues(aSortedIndexList, l, j, aCTC);
		sortValues(aSortedIndexList, i, r, aCTC);

		return;
	}

	// move to separate class upon discretion
	private class CrossTableComparator implements Comparator<Integer>
	{
		private final int[] itsPosCounts;
		private final int[] itsNegCounts;

		/** no null check, may throw null pointer exception */
		CrossTableComparator(int[] thePositiveCounts, int[] theNegativeCounts)
		{
			itsPosCounts = thePositiveCounts;
			itsNegCounts = theNegativeCounts;
		}

		@Override
		public int compare(Integer index1, Integer index2)
		{
			// avoid explicit auto-(un)boxing, gives compiler / JVM
			// more freedom to optimise
			return (itsPosCounts[index2] * itsNegCounts[index1]) -
				(itsPosCounts[index1] * itsNegCounts[index2]);
		}
	}
}
