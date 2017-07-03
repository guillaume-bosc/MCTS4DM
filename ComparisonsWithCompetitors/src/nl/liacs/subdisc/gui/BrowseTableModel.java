package nl.liacs.subdisc.gui;

import javax.swing.table.*;

import nl.liacs.subdisc.*;

/**
 * BrowseTableModel is the Model for a <code>JTable<code> containing all data
 * from a {@link Table Table}. BrowseTableModel extends
 * <code>AbstractTableModel</code> and all methods it overrides are
 * straightforward. The only noteworthy change is the return <code>String</code>
 * for the {@link #getColumnName(int) getColumnName()} method, which returns a
 * 2-line <code>String</code> that contains both the name of the
 * {@link Column Column}, and its number of distinct values.
  */
public class BrowseTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = 1L;

	private Table itsTable;

	public BrowseTableModel(Table theTable)
	{
		if (theTable == null)
		{
			Log.logCommandLine("BrowseTableModel Constructor()");
			return;
		}
		else
			itsTable = theTable;
	}

	@Override
	public int getColumnCount()
	{
		if (itsTable == null)
		{
			LogError(".getColumnCount()");
			return 0;
		}
		else
			return itsTable.getNrColumns();
	}

	// TODO Put in SwingWorker background thread.
	@Override
	public String getColumnName(int theColumnIndex)
	{
		if (itsTable == null)
		{
			LogError(".getColumnName()");
			return "Incorrect column index.";
		}
		else
		{
			Column aColumn = itsTable.getColumn(theColumnIndex);
			return String.format("<html><center>%s<br>(%d distinct)</html>",
									aColumn.getName(),
									aColumn.getCardinality());
		}
	}

	@Override
	public int getRowCount()
	{
		if (itsTable == null)
		{
			LogError(".getRowCount()");
			return 0;
		}
		else
			return itsTable.getNrRows();
	}

	@Override
	public Object getValueAt(int theRow, int theColumn)
	{
		if (itsTable == null || itsTable.getNrRows() == 0)
		{
			LogError(".getValueAt()");
			return null;
		}
		else
		{
			Column aColumn = itsTable.getColumn(theColumn);
			switch (aColumn.getType())
			{
				case NOMINAL : return aColumn.getNominal(theRow);
				case NUMERIC :
				case ORDINAL : return aColumn.getFloat(theRow);
				/*
				 * NOTE DefaultCellRenderer draws check boxes
				 * in JTable for Boolean return type.
				 */
				case BINARY : 
					return aColumn.getBinary(theRow);
				default : {
					Log.logCommandLine(
						String.format(
							"%s.getValueAt(%d, %d), Unknown AttributeType: %s",
							getClass().getSimpleName(),
							theRow,
							theColumn,
							aColumn.getType()));
					return null;
				}
			}
		}
	}

	// for sorting
	@Override
	public Class<?> getColumnClass(int theColumn) {
		if (itsTable == null || itsTable.getNrRows() == 0)
			return null;
		else
			return getValueAt(0, theColumn).getClass();
	}

	private void LogError(String theMethod)
	{
		Log.logCommandLine(
			"Error in BrowseTableWindow" + theMethod + ": Table is 'null'.");
	}
}
