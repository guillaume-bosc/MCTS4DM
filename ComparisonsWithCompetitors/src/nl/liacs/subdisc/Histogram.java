package nl.liacs.subdisc;

@Deprecated
public class Histogram extends DataCube
{
	private String[] itsValues = new String[0];
	private int[] itsCounts = new int[0];
	private String itsType;

/*	public Histogram(DatabaseConnection theDBC, String theQuery) throws Exception
	{
		Log.logSql("Histogram:");
		ArrayList<ArrayList> aResult = theDBC.doDoubleArrayQuery(theQuery);

		int aNrRows = aResult.get(0).size();
		itsValues = new String[aNrRows];
		itsCounts = new int[aNrRows];

		for (int i=0; i<aNrRows; i++)
		{
			itsValues[i] = (String) aResult.get(0).get(i);
			itsCounts[i] = (Integer) aResult.get(1).get(i);
		}
	}
*/
	// used by DataExplorerWindow
	public Histogram(String[] theValues, int[] theCounts, String theType)
	{
		itsValues = theValues;
		itsCounts = theCounts;
		itsType = theType;
	}

	public String getType() { return itsType; }

	public String getValue(int theIndex) { return itsValues[theIndex]; }

	public String[] getValues() { return itsValues; }

	public int getCount(int theIndex)
	{
		if ( theIndex >= itsCounts.length )
			return 0;
		else
			return itsCounts[theIndex];
	}

	public int getCount(String theValue)
	{
		int anIndex = getIndex(theValue);

		if (anIndex == -1)
			return 0;
		else
			return getCount(anIndex);
	}

	public int[] getCounts()	{ return itsCounts;	}

	public int getTotalCount()
	{
		int aTotalCount = 0;
		for (int i = 0; i < itsCounts.length; i++)
			aTotalCount += itsCounts[i];
		return aTotalCount;
	}

	public int getMaxCount()
	{
		int aMaxCount = 0;
		for(int i=0; i < itsCounts.length; i++)
		{
			if(itsCounts[i] > aMaxCount)
				aMaxCount = itsCounts[i];
		}
		return aMaxCount;
	}

	public int getIndex(String theValue)
	{
		for (int i = 0; i < itsValues.length; i++)
			if (theValue.equals(itsValues[i]))
				return i;

		//value doesn't exist
		return -1;
	}

	public String toString()
	{
		String aString = new String("");

		for (int i = 0; i < itsValues.length; i++)
			aString += "\n  " + itsValues[i] + "	" + itsCounts[i];
		if (itsValues.length > 0)
			return aString.substring(1);
		else
			return "  < empty > ";
	}

	public void debug()
	{
		Log.debug("Histogram:\n");
		for (int i = 0; i < itsCounts.length; i++)
			Log.debug( "  " + itsValues[i] + "	" + itsCounts[i] );
		Log.debug("");
	}
}
