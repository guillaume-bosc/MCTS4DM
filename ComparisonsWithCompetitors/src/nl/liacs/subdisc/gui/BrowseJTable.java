package nl.liacs.subdisc.gui;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import nl.liacs.subdisc.*;

public class BrowseJTable extends JTable
{
	private static final long serialVersionUID = 1L;
	private final BitSet MEMBERS;
	private final BitSet TRUE_POSITIVES;
	private final boolean NOMINAL; // fast
	/*
	 * TODO Hacked in for now. Used to bring column into focus in
	 * findColumn(). Will be replaced with clean code.
	 * Problems occur when resizing columns. (Quick-fix: disable resizing.)
	 */
	private int[] itsOffsets;

	@SuppressWarnings("unchecked")
	public BrowseJTable(Table theTable, Subgroup theSubgroup)
	{
		if (theTable == null)
		{
			MEMBERS = null;
			TRUE_POSITIVES = null;
			NOMINAL = false;
			return;	// warning
		}
		else if (theSubgroup == null)
		{
			MEMBERS = null;
			TRUE_POSITIVES = null;
		}
		else
		{
			MEMBERS = theSubgroup.getMembers();
			TRUE_POSITIVES = theSubgroup.getParentSet().getBinaryTargetClone();
			if (TRUE_POSITIVES != null)
				TRUE_POSITIVES.and(MEMBERS);
		}
		NOMINAL = (TRUE_POSITIVES != null);

		super.setModel(new BrowseTableModel(theTable));
		super.setRowSorter(new TableRowSorter<TableModel>(super.getModel()));
		((DefaultRowSorter<BrowseTableModel, Integer>) getRowSorter()).setRowFilter(new RowFilterBitSet(MEMBERS));

		super.setRowSelectionAllowed(false);
		super.setColumnSelectionAllowed(true);
		super.setDefaultRenderer(Float.class, RendererNumber.RENDERER);
		super.setDefaultRenderer(Boolean.class, RendererBoolean.RENDERER);
		super.setPreferredScrollableViewportSize(GUI.WINDOW_DEFAULT_SIZE);
		initColumnSizes(theTable);
		super.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		super.setFillsViewportHeight(true);
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
	{
		final Component c = super.prepareRenderer(renderer, row, column);
		// Row color based on TruePositive/FalsePositive (NOMINAL only)
		if (!isColumnSelected(column))
		{
			if (NOMINAL && !TRUE_POSITIVES.get(convertRowIndexToModel(row)))
				c.setBackground(GUI.RED);
			else
				c.setBackground(getBackground());
		}
		return c;
	}

	/*
	 * Based on Swing tutorial TableRenderDemo.java.
	 * This method picks column sizes, based on column heads only.
	 * TODO Put in SwingWorker background thread.
	 */
	private void initColumnSizes(Table theTable)
	{
		int aHeaderWidth = 0;
		int aTotalWidth = 0;
		TableColumnModel aColumnModel = super.getColumnModel();
		itsOffsets = new int[aColumnModel.getColumnCount() + 1]; // ;)
		TableCellRenderer aRenderer = super.getTableHeader().getDefaultRenderer();

		for (int i = 0, j = aColumnModel.getColumnCount(); i < j; ++i)
		{
			// 91 is width of "(999 distinct)"
			aHeaderWidth = Math.max(aRenderer.getTableCellRendererComponent(
									null, theTable.getColumn(i).getName(),
									false, false, 0, 0).getPreferredSize().width,
									91);

			aColumnModel.getColumn(i).setPreferredWidth(aHeaderWidth);
			itsOffsets[i + 1] = aTotalWidth += aHeaderWidth;
		}
	}

	public void focusColumn(int theModelIndex)
	{
		super.scrollRectToVisible(new Rectangle(0, 0, 0, 0)); // HACK
		if (theModelIndex < 0 ||
				theModelIndex >= super.getColumnModel().getColumnCount())
			super.clearSelection();
		else
		{
			int i = super.convertColumnIndexToView(theModelIndex);
			super.setColumnSelectionInterval(i, i);
			super.scrollRectToVisible(new Rectangle(itsOffsets[theModelIndex],
													0,
													itsOffsets[theModelIndex + 1],
													0));
		}
	}
}
