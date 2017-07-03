package nl.liacs.subdisc;

// Safarii legacy
@Deprecated
public class SelectStatement
{
	private String itsSelectClause;
	private String itsFromClause;
	private String itsWhereClause;
	private String itsGroupByClause;
	private String itsOrderByClause;
	private boolean itsDescending = false;

	public SelectStatement()
	{
	}

	public void addSelectClause(String theClause)
	{
		if (itsSelectClause == null)
		{
			itsSelectClause = theClause;
		}
		else
		{
			itsSelectClause += ", " + theClause;
		}
	}

	public void addFromClause(String theClause)
	{
		if (itsFromClause == null)
		{
			itsFromClause = theClause;
		}
		else
		{
			itsFromClause += ", " + theClause;
		}
	}

	public void addWhereClause(String theClause)
	{
		if (theClause == null || theClause.equals(""))
			return;

		if (itsWhereClause == null)
		{
			itsWhereClause = theClause;
		}
		else
		{
			itsWhereClause += " AND " + theClause;
		}
	}

	public void addGroupByClause(String theClause)
	{
		if (theClause == null || theClause.equals(""))
			return;
		if (itsGroupByClause == null)
		{
			itsGroupByClause = theClause;
		}
		else
		{
			itsGroupByClause += ", " + theClause;
		}
	}

	public void addOrderByClause(String theClause)
	{
		if (theClause == null || theClause.equals(""))
			return;
		if (itsOrderByClause == null)
		{
			itsOrderByClause = theClause;
		}
		else
		{
			itsOrderByClause += ", " + theClause;
		}
	}

	public void setDescending()
	{
		itsDescending = true;
	}

	public void addSampleClause(String theColumn, String theSample, int theSampleState)
	{
/*		if (theSampleState == DatabaseSample.SAMPLE_POSITIVE)
		{
			addWhereClause(theColumn + "=\'" + theSample + "\'");
		}
		else
		{*/
			addWhereClause(theColumn + "<>\'" + theSample + "\'");
//		}
	}

	public String toString()
	{
		String aSelectStatement = "SELECT " + itsSelectClause;
		aSelectStatement += " FROM " + itsFromClause;
		if (itsWhereClause != null)
			aSelectStatement += " WHERE " + itsWhereClause;
		if (itsGroupByClause != null)
			aSelectStatement += " GROUP BY " + itsGroupByClause;
		if (itsOrderByClause != null)
		{
			aSelectStatement += " ORDER BY " + itsOrderByClause;
			if (itsDescending)
				aSelectStatement += " DESC";
		}
		return aSelectStatement;
	}
}
