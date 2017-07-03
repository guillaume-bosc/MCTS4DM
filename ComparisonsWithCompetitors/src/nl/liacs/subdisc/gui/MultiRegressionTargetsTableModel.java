package nl.liacs.subdisc.gui;

//import javax.swing.*;
import javax.swing.table.*;

import nl.liacs.subdisc.*;
import nl.liacs.subdisc.gui.MetaDataTableModel.MetaDataTableHeader;

public class MultiRegressionTargetsTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = 1L;

	private Table itsTable;

	public enum MultiRegressionTargetsTableHeader
	{
		ATTRIBUTE(0, "Attribute"),
		TARGET_STATUS(1, "Target status"),
		TYPE(2, "Type");

		public final int columnNr;
		public final String guiText;

		private MultiRegressionTargetsTableHeader(int theColumnNr, String theGuiText)
		{
			columnNr = theColumnNr;
			guiText = theGuiText;
		}

		public static String getColumnName(int theColumnIndex)
		{
			for (MultiRegressionTargetsTableHeader h : MultiRegressionTargetsTableHeader.values())
				if (h.columnNr == theColumnIndex)
						return h.guiText;
			Log.logCommandLine(
				"Error in MetaDataTableHeader.getColumnName(): invalid index '"
				+ theColumnIndex + "'.");
			return "Incorrect column index.";
		}
	};

	public MultiRegressionTargetsTableModel(Table theTable)
	{
		if (theTable == null)
		{
			LogError(" Constructor()");
			return;
		}
		else
			itsTable = theTable;
	}

	@Override
	public int getColumnCount() { return MultiRegressionTargetsTableHeader.values().length; }

	@Override
	public String getColumnName(int theColumnIndex)
	{
		return MultiRegressionTargetsTableHeader.getColumnName(theColumnIndex);
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
			return itsTable.getNrColumns();
	}

	@Override
	public Object getValueAt(int row, int col)
	{
		if (itsTable == null)
		{
			LogError(".getValueAt()");
			return null;
		}
		else
		{
			if (col == MultiRegressionTargetsTableHeader.ATTRIBUTE.columnNr)
				return itsTable.getColumn(row).getName();
			else if (col == MultiRegressionTargetsTableHeader.TARGET_STATUS.columnNr)
				return itsTable.getColumn(row).displayTargetStatus();
			else if (col == MetaDataTableHeader.TYPE.columnNr)
				return itsTable.getColumn(row).getType();
			else
			{
				Log.logCommandLine(
					"Error in MultiRegressionTargetsTableModel.getValueAt(): " +
					"invalid index: '" + col + "' for MultiRegressionTargetsTableHeader.");
				return null;
			}
		}
	}
	
	private void LogError(String theMethod)
	{
		Log.logCommandLine(
			"Error in MetaDataTableWindow" + theMethod + ": Table is 'null'.");
	}
}