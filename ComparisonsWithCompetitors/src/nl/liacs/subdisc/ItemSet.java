package nl.liacs.subdisc;

import java.util.*;

// FIXME @author, this class redefines default BitSet methods, why?
public class ItemSet extends BitSet
{
	private static final long serialVersionUID = 1L;
	private int itsDimensions;	// MM BitSet.size() ?
	private double itsJointEntropy = Double.NaN;

	//empty itemset
	public ItemSet(int theDimensions)
	{
		super(theDimensions);
		itsDimensions = theDimensions;
	}

	//itemset with first theCount items set.
	public ItemSet(int theDimensions, int theCount)
	{
		super(theDimensions);
		itsDimensions = theDimensions;

		if (theCount>itsDimensions)
			set(0, itsDimensions);
		else
			set(0, theCount);
	}

	// MM did you mean BitSet.size() ?
	public int getDimensions()
	{
		return itsDimensions;
	}

	// MM did you mean  BitSet.cardinality() ?
	public int getItemCount()
	{
		int aCount = 0;

		for (int i=0; i<itsDimensions; i++)
		{
			if (get(i))
				aCount++;
		}
		return aCount;
	}

	/**
	 * Returns the index of the <em>n</em>-th set bit.
	 * 
	 * @param theIndex
	 * 
	 * @return the index of the <em>n</em>-th set bit, or <code>-1</code> if
	 * it can not be found.
	 */
	// never used
	@Deprecated
	private int getItem(int theIndex)
	{
// MM why are default BitSet methods not used for this?
//		if (theIndex <= 0 || theIndex > length())
//			return -1;
//		for (int i = nextSetBit(0), j = 0; i >= 0; i = nextSetBit(i+1))
//			if (theIndex == ++j)
//				return i;
//		return -1;

		int aCount = 0;

		for (int i=0; i<itsDimensions; i++)
		{
			if (get(i))
			{
				aCount++;
				if (aCount == theIndex)
					return i;
			}
		}
		return -1;
	}

	// MM could/ should be defined in terms of BitSet.xor()
	// ((BitSet)theSet.clone()).xor(this);
	public ItemSet symmetricDifference(ItemSet theSet)
	{
		ItemSet aSet = new ItemSet(itsDimensions);

		for (int i=0; i<itsDimensions; i++)
			if (get(i) ^ theSet.get(i))
				aSet.set(i);
		return aSet;
	}

	public ItemSet getExtension(int theIndex)
	{
		// NOTE only clones BitSet, not itsDimensions and itsEntropy
		ItemSet aSet = (ItemSet) clone();
		aSet.set(theIndex);
		return aSet;
	}

	// never used
	@Deprecated
	private ItemSet getNextItemSet()
	{
		int aCount = 0;
		int aLast = 0;
		boolean aFound = false;

		//find last occurence of ...10...
		for (int i=itsDimensions-1; i>0; i--)
		{
			if (get(i))
				aCount++;
			if (!get(i) && get(i-1))
			{
				aLast = i-1;
				aFound = true;
				break;
			}
		}
		if (!aFound)//last itemset
			return null;

		//create new itemset
		ItemSet aSet = new ItemSet(itsDimensions);
		for (int i=0; i<itsDimensions; i++)
		{
			//copy, or..
			if ((i<aLast) && get(i))
				aSet.set(i);
			//add new
			if ((i>aLast) && (aCount>=0))
			{
				aSet.set(i);
				aCount--;
			}
		}

		return aSet;
	}

	// l+1 = number consecutive bits that need to be set (when counting back
	// from index)
	// never used
	@Deprecated
	boolean isFresh(int l)
	{
// MM fast, concise alternative
//		final int i = length()-1; // index of highest set bit
//		return (i < 0) ? true : (previousClearBit(i) < (i - l));

		int aCount = 0;
		boolean aStart = false;

		// i>0 or i>=0, current loop does not test for get(0)
		// also as soon as (++aCount == l+1) the loop can break
		// as it will never return false anymore
		for (int i=itsDimensions-1; i>0; i--)
		{
			if (aStart == true)
			{
				if (!get(i) && (aCount < l+1))
					return false;
			}
			if (get(i))
			{
				aStart = true;
				aCount++;
			}
		}
		return true;
	}

	//skip all itemsets with same first l items, and proceed with next
	// never used
	@Deprecated
	private ItemSet skipItemSets(int l)
	{
		int aCount = 0;
		int aLast = 0;

		//find lth itemset
		for (int i=0; i<itsDimensions; i++)
		{
			if (get(i))
				aCount++;
			if (aCount == l)
			{
				aLast = i;
				break;
			}
		}

		//create new itemset
		ItemSet aSet = new ItemSet(itsDimensions);
		for (int i=0; i<itsDimensions; i++)
		{
			//copy, or..
			if ((i<=aLast) && get(i))
				aSet.set(i);
			//add new
			if ((i == itsDimensions+aCount-getItemCount()) && (aCount<getItemCount()))
			{
				aSet.set(i);
				aCount++;
			}
		}

		if (aCount < getItemCount())//skipping to the end
			return null;
		else
			return aSet;
	}

	public double getJointEntropy() { return itsJointEntropy; }
	public void setJointEntropy(double theJointEntropy) { itsJointEntropy = theJointEntropy; }
}
