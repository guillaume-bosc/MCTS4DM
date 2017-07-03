package nl.liacs.subdisc.gui;

import java.util.*;

import javax.swing.table.*;

import nl.liacs.subdisc.*;

public class ResultTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = 1L;
	public static final int COLUMN_COUNT = 8;

	private SubgroupSet itsSubgroupSet;
	private TargetType itsTargetType;

	public ResultTableModel(SubgroupSet theSubgroupSet, TargetType theType)
	{
		itsSubgroupSet = theSubgroupSet;
		itsTargetType = theType;
	}

	@Override
	public int getRowCount()
	{
		return itsSubgroupSet.size();
	}

	@Override
	public int getColumnCount()
	{
		return COLUMN_COUNT;
	}

	@Override
	public String getColumnName(int theColumnIndex)
	{
		return getColumnName(theColumnIndex, itsTargetType);
	}

	// used by XMLAutoRun.save() to retrieve correct column name
	public static String getColumnName(int theColumnIndex, TargetType theTargetType)
	{
		switch(theColumnIndex)
		{
			case 0 : return "Nr.";
			case 1 : return "Depth";
			case 2 : return "Coverage";
			case 3 : return "Quality";
			case 4 : {
				switch (theTargetType) {
					case SINGLE_NOMINAL : return "Probability";
					case SINGLE_NUMERIC : return "Average";
					case DOUBLE_CORRELATION : return "Correlation";
					case DOUBLE_REGRESSION : return "Slope";
					case MULTI_LABEL : return "Edit Distance";
					default : return "";
				}
			}
			case 5 : {
				switch (theTargetType) {
					case SINGLE_NOMINAL : return "Positives";
					case SINGLE_NUMERIC : return "St. Dev.";
					case DOUBLE_CORRELATION : return "Distance";
					case DOUBLE_REGRESSION : return "Intercept";
					case MULTI_LABEL : return "Entropy";
					default : return "";
				}
			}
			case 6 : return "p-Value";
			case 7 : return "Conditions";
			default : return "";
		}
	}

	@Override
	public Object getValueAt(int theRowIndex, int theColumnIndex)
	{
		// will crash if (!anIterator.hasNext())
		// !(0 <= theRowIndex < itsSubgroupSet.size())
		Iterator<Subgroup> anIterator;
		if (theRowIndex <= (itsSubgroupSet.size() / 2))
		{
			anIterator = itsSubgroupSet.iterator();
			for (int i = 0 ; i < theRowIndex; ++i)
				anIterator.next();
		}
		else
		{
			anIterator = itsSubgroupSet.descendingIterator();
			for (int i = itsSubgroupSet.size()-1; i > theRowIndex; --i)
				anIterator.next();
		}
		Subgroup aSubgroup = anIterator.next();

		switch(theColumnIndex)
		{
			case 0: return aSubgroup.getID();
			case 1: return aSubgroup.getNrConditions();
			case 2: return aSubgroup.getCoverage();
			case 3: return RendererNumber.FORMATTER.format(aSubgroup.getMeasureValue());
			case 4: return RendererNumber.FORMATTER.format(aSubgroup.getSecondaryStatistic());
			case 5: return RendererNumber.FORMATTER.format(aSubgroup.getTertiaryStatistic());
			case 6:
			{
				double aPValue = aSubgroup.getPValue();
				return ((Double.isNaN(aPValue)) ? "  -" : (float)aPValue);
			}
			case 7: return aSubgroup.getConditions().toString();
			default : return "---";
		}
	}
}
