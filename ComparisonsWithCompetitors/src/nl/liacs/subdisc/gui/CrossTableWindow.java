package nl.liacs.subdisc.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.jfree.data.category.*;
import org.jfree.data.general.*;

/*
 * TODO create TableModel which includes:
 * columnHeaders of correct width (see BrowseJTable initColumnSizes())
 * adds Total column with row counts
 * adds Total row with column counts

 * setDefaultRenderer for Float/ Boolean columns (relies on columnType)
 * (Dataset theDataset, Table theTable, int attributeColumn, in targhetColumn)
 * need to be passed to constructor, or String parsing of dataset row/columnKeys
 * 
 * TODO save() as XMLAutoRun.save()
 * should be merged code that writes any JTable header + data to csv/tsv/tex
 */
public class CrossTableWindow extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;

	private Dataset itsDataset = new DefaultCategoryDataset();

	// may only work correctly for (Default)CategoryDatasets
	public CrossTableWindow(Dataset theDataset)
	{
		if (theDataset == null)
			return;

		// NOTE Attributes = ColumnKeys, Targets = RowKeys
		itsDataset = theDataset;

		initComponents();

		setTitle("CrossTable");
		setIconImage(MiningWindow.ICON);
		setLocation(50, 50);
		//setSize(GUI.WINDOW_DEFAULT_SIZE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	}

	private void initComponents()
	{
		setLayout(new BorderLayout());

		// JViewport with table (without header)
		final JTable aTable = new JTable(createData(), createHeader());
		//initColumnSizes(aTable);
		aTable.setTableHeader(null);
		final JScrollPane aPane = new JScrollPane(aTable);
		add(aPane, BorderLayout.CENTER);

		// button panel
		final JPanel aPanel = new JPanel();
		//aPanel.add(GUI.buildButton("Save", "save", this));
		//aPanel.add(GUI.buildButton("Print", "print", this));
		aPanel.add(GUI.buildButton("Close", "close", this));
		add(aPanel, BorderLayout.SOUTH);
	}

	// table header is not shown for now
	private Object[] createHeader()
	{
		DefaultCategoryDataset aSet = (DefaultCategoryDataset)itsDataset;
		int aNrTargets = aSet.getRowCount();

		Object[] aTargets = new Object[aNrTargets+1];
		aTargets[0] = "Attribute v / Target >";
		for (int i = 0, j = aNrTargets; i < j; ++i)
			aTargets[i+1] = aSet.getRowKey(i);

		return aTargets;
	}

	private Object[][] createData()
	{
		DefaultCategoryDataset aSet = (DefaultCategoryDataset)itsDataset;
		int aNrTargets = aSet.getRowCount();
		int aNrAttributes = aSet.getColumnCount();

		Object[][] aData = new Object[aNrAttributes+1][aNrTargets+1];

		// show table without header, use first row for target values
		aData[0][0] = "Attribute v / Target >";
		for (int i = 0, j = aNrTargets; i < j; ++i)
			aData[0][i+1] = aSet.getRowKey(i);

		for (int i = 0, j = aNrAttributes; i < j; ++i)
		{
			aData[i+1][0] = aSet.getColumnKey(i);
			for (int k = 0, m = aNrTargets; k < m; ++k)
				aData[i+1][k+1] = aSet.getValue(k, i);
		}

		return aData;
	}

	@Override
	public void actionPerformed(ActionEvent theEvent)
	{
		String anEvent = theEvent.getActionCommand();

		if ("save".equals(anEvent))
			; // TODO as csv, tsv, tex
		if ("print".equals(anEvent))
			; // TODO
		else if ("close".equals(anEvent))
			dispose();
	}
}
