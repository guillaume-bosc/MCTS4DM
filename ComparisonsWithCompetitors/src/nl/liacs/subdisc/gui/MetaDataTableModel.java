package nl.liacs.subdisc.gui;

import javax.swing.table.*;

import nl.liacs.subdisc.*;

public class MetaDataTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = 1L;

	private Table itsTable;

	public enum MetaDataTableHeader
	{
		ATTRIBUTE(0, "Attribute"),
		CARDINALITY(1, "Cardinality"),
		TYPE(2, "Type"),
		ENABLED(3, "Enabled"),
		HAS_MISSING(4, "Values Missing"),
		MISSING_VALUE(5, "Value for Missing");

		public final int columnNr;
		public final String guiText;

		private MetaDataTableHeader(int theColumnNr, String theGuiText)
		{
			columnNr = theColumnNr;
			guiText = theGuiText;
		}

		public static String getColumnName(int theColumnIndex)
		{
			for (MetaDataTableHeader h : MetaDataTableHeader.values())
				if (h.columnNr == theColumnIndex)
						return h.guiText;
			Log.logCommandLine(
				"Error in MetaDataTableHeader.getColumnName(): invalid index '"
				+ theColumnIndex + "'.");
			return "Incorrect column index.";
		}
	};

	public MetaDataTableModel(Table theTable)
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
	public int getColumnCount() { return MetaDataTableHeader.values().length; }

	@Override
	public String getColumnName(int theColumnIndex)
	{
		return MetaDataTableHeader.getColumnName(theColumnIndex);
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
			if (col == MetaDataTableHeader.ATTRIBUTE.columnNr)
				return itsTable.getColumn(row).getName();
			else if (col == MetaDataTableHeader.CARDINALITY.columnNr)
				return itsTable.getColumn(row).getCardinality();
			else if (col == MetaDataTableHeader.TYPE.columnNr)
				return itsTable.getColumn(row).getType();
			else if (col == MetaDataTableHeader.ENABLED.columnNr)
				return itsTable.getColumn(row).getIsEnabled() ? "yes" : "no";
			else if (col == MetaDataTableHeader.HAS_MISSING.columnNr)
				return itsTable.getColumn(row).getHasMissingValues() ? "yes" : "no";
			else if (col == MetaDataTableHeader.MISSING_VALUE.columnNr)
				return itsTable.getColumn(row).getMissingValue();
			else
			{
				Log.logCommandLine(
					"Error in MetaDataTableModel.getValueAt(): " +
					"invalid index: '" + col + "' for MetaDataTableHeader.");
				return null;
			}
		}
	}

//	@Override
//	public Class<?> getColumnClass(int c) { return getValueAt(0, c).getClass(); }

	private void LogError(String theMethod)
	{
		Log.logCommandLine(
			"Error in MetaDataTableWindow" + theMethod + ": Table is 'null'.");
	}
/*
	@Override
	public boolean isCellEditable(int row, int col)
	{
		return (col == ColumnHeader.SELECT.columnNr || col == ColumnHeader.TYPE.columnNr);
	}

	public void setValueAt(Object value, int row, int col)
	{
		switch(col)
		{
//			case 0 : itsSelectedAttributes.flip(row); break;
			case 2 : itsTable.getColumns().get(row).setType(((String) value)); break;
		}
		fireTableCellUpdated(row, col);
	}
*/
/*
//	public static BitSet getSelectedAttributes() { return (BitSet)itsSelectedAttributes.clone(); }
	public static void setSelectedAttributes(Selection theSelection)	// TODO will change
	{
		switch (theSelection)
		{
//		case ALL : itsSelectedAttributes.set(0, itsSelectedAttributes.size()); break;
//		case INVERT : itsSelectedAttributes.flip(0, itsSelectedAttributes.size()); break;
//		default : selectType(theSelection);
		}
	}
*/
/*
	public void selectAllType(AttributeType theType, boolean selected)
	{
		for (int i = 0, j = itsTable.getColumns().size(); i < j; ++i)
		{
			if (itsTable.getColumn(i).getType() == theType)
			{
//				if(selected)
//					itsSelectedAttributes.set(i);
//				else
//					itsSelectedAttributes.clear(i);
			}
		}
		// TODO update table/window
		
	}
*/
}

