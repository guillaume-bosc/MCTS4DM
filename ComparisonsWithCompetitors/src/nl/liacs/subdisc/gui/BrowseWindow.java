package nl.liacs.subdisc.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;
import javax.swing.text.*;

import nl.liacs.subdisc.*;

/**
 * A BrowseWindow contains a {@link BrowseJTable BrowseJTable} that shows all
 * data in a {@link Table Table}, which in turn is read from a <code>File</code>
 * or database. For each {@link Column Column}, the header displays both the
 * name and the number of distinct values.
 */
public class BrowseWindow extends JFrame implements ActionListener//, MouseListener
{
	private static final long serialVersionUID = 1L;
	private Table itsTable;
	private BrowseJTable itsBrowseJTable;
	private BitSet itsSubgroupMembers;	// for Table.save()
	private JComboBox itsColumnsBox;
	// problematic in case of double columnNames
	private Map<String, Integer> itsMap;

	public BrowseWindow(Table theTable, Subgroup theSubgroup)
	{
		if (theTable == null)
		{
			Log.logCommandLine(
				"BrowseWindow Constructor: theTable can not be 'null'.");
			return;
		}
		else
		{
			itsTable = theTable;
			initComponents(theSubgroup);

			if (theSubgroup == null)
				setTitle("Data for: " + itsTable.getName());
			else
				setTitle(theSubgroup.getCoverage() +
						" members in subgroup: " +
						theSubgroup.getConditions());

			setIconImage(MiningWindow.ICON);
			setLocation(100, 100);
			setSize(GUI.WINDOW_DEFAULT_SIZE);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setVisible(true);
		}
	}

	private void initComponents(Subgroup theSubgroup)
	{
		itsBrowseJTable = new BrowseJTable(itsTable, theSubgroup);
		//itsBrowseJTable.addMouseListener(this);
		getContentPane().add(new JScrollPane(itsBrowseJTable), BorderLayout.CENTER);

		final JPanel aButtonPanel = new JPanel();

		int aNrColumns = itsTable.getNrColumns();
		// TODO HistogramWindow uses a similar list, sync?
		itsMap = new LinkedHashMap<String, Integer>(aNrColumns);
		for (int i = 0, j = aNrColumns; i < j; ++i)
		{
			Column aColumn = itsTable.getColumn(i);
			itsMap.put(aColumn.getName(), aColumn.getIndex());
		}
		itsColumnsBox = GUI.buildComboBox(itsMap.keySet().toArray(), this);
		itsColumnsBox.setEditable(true);
		itsColumnsBox.setPreferredSize(GUI.BUTTON_DEFAULT_SIZE);
		aButtonPanel.add(itsColumnsBox);

		// always allow saving, useful if data is modified
		aButtonPanel.add(GUI.buildButton("Save", 'S', "save", this));

		// by default focus is on first (condition-)attribute
		if (theSubgroup == null)
			itsColumnsBox.setSelectedIndex(0);
		else
		{
			itsSubgroupMembers = theSubgroup.getMembers();
			String aName = theSubgroup.getConditions().get(0).getColumn().getName();
			((JTextComponent)itsColumnsBox.getEditor().getEditorComponent()).setText(aName);
			itsColumnsBox.setSelectedItem(aName);
		}

		final JButton aCloseButton = GUI.buildButton("Close", 'C', "close", this);
		aButtonPanel.add(aCloseButton);
		getContentPane().add(aButtonPanel, BorderLayout.SOUTH);

		GUI.focusComponent(aCloseButton, this);
	}

	@Override
	public void actionPerformed(ActionEvent theEvent)
	{
		String anEvent = theEvent.getActionCommand();
//		System.out.println(anEvent);

		// relies on only one comboBox being present
		if("comboBoxEdited".equals(anEvent))
			;//comboBoxCheck();
		else if("comboBoxChanged".equals(anEvent))
			updateItsColumnsBox();
		else if ("save".equals(anEvent))
			itsTable.toFile(itsSubgroupMembers);
		else if ("close".equals(anEvent))
			dispose();
	}

	private void updateItsColumnsBox()
	{
		String aText = ((JTextComponent)itsColumnsBox.getEditor().getEditorComponent()).getText();

		if (itsMap.containsKey(aText))
			itsBrowseJTable.focusColumn(itsMap.get(aText));
		else
		{
			String aNewText = aText.toLowerCase();
			itsColumnsBox.removeActionListener(this);
			itsColumnsBox.setSelectedIndex(-1);
			itsBrowseJTable.clearSelection();
			itsColumnsBox.removeAllItems();

			// special case (aText == ""), true for all items, resets full list
			for (Entry<String, Integer> e : itsMap.entrySet())
				if (e.getKey().toLowerCase().startsWith(aNewText))
					itsColumnsBox.addItem(e.getKey());

			if (itsColumnsBox.getItemCount() > 0)
			{
				itsColumnsBox.setSelectedIndex(0);
				itsBrowseJTable.focusColumn(itsMap.get(itsColumnsBox.getItemAt(0)));
				//((JComponent)itsColumnsBox.getEditor().getEditorComponent()).setForeground(getForeground());
			}
			else
			{
				((JTextComponent)itsColumnsBox.getEditor().getEditorComponent()).setText(aText);
				// if no column startsWith aText, colour aText
				//((JTextField)itsColumnsBox.getEditor().getEditorComponent()).setForeground(GUI.RED);
			}
			itsColumnsBox.addActionListener(this);
		}
	}
}
