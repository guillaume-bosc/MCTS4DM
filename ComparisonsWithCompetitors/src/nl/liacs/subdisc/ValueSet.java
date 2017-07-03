package nl.liacs.subdisc;

import java.util.*;

/*
 * ValueSet does not extend any class, its values are stored in a
 * SortedSet<String>.
 * ValueSets are mainly build from TreeSets, which are sorted also.
 * Insertion order seems not to be relevant to any code.
 * A Set<String> natively avoids duplicates.
 * Having it is a member avoids the need to Override every method that could
 * modify the ValueSet after its creation.
 */
/**
 * ValueSets are sorted sets that hold a number of <code>String</code> values.
 */
public class ValueSet
{
	private final SortedSet<String >itsValues;

	/**
	 * Creates a ValueSet, it can not be modified in any way after creation.
	 * 
	 * @param theDomain the values to use for this ValueSet.
	 * 
	 * @throws IllegalArgumentException if theDomain does not contain at
	 * least one value. 
	 */
	public ValueSet(SortedSet<String> theDomain) throws IllegalArgumentException
	{
		// throws a NullPointerException in case of null
		if (theDomain.size() == 0)
			throw new IllegalArgumentException("Domains must be > 0");

		itsValues = new TreeSet<String>(theDomain);
	}

	/**
	 * Returns whether the supplied value is present in this ValueSet.
	 * 
	 * @param theValue the value to check.
	 * 
	 * @return <code>true</code> if this ValueSet contains the supplied
	 * parameter, <code>false</code> otherwise.
	 */
	public boolean contains(String theValue)
	{
		return itsValues.contains(theValue);
	};

	@Override
	public String toString()
	{
		final int size = itsValues.size();
		if (size == 0)
			return "{}";

		StringBuilder aResult = new StringBuilder(size << 5);
		aResult.append("{");
		final Iterator<String> i = itsValues.iterator();
		aResult.append(i.next());
		while (i.hasNext())
		{
			aResult.append(",");
			aResult.append(i.next());
		}
		return aResult.append("}").toString();
	}
}
