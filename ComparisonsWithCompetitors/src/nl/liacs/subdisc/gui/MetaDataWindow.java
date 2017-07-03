/*
 * TODO if changes are made also update other opened windows, eg. BrowseWindow
 * TODO all methods should update itsFeedBackLabel on failure/success
 */
package nl.liacs.subdisc.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import nl.liacs.subdisc.*;
import nl.liacs.subdisc.gui.MetaDataTableModel.MetaDataTableHeader;

public class MetaDataWindow extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private final MiningWindow itsMiningWindow;
	private final Table itsTable;
	private JTable itsJTable;
	private ButtonGroup aNewType = new ButtonGroup();
	private JTextField aNewMissingValue =
		new JTextField(AttributeType.getDefault().DEFAULT_MISSING_VALUE);
	private JLabel itsFeedBackLabel;

	public MetaDataWindow(MiningWindow theMiningWindow, Table theTable)
	{
		itsMiningWindow = theMiningWindow;
		itsTable = theTable;

		if (theTable == null || theMiningWindow == null)
		{
			Log.logCommandLine("MetaDataWindow Constructor: parameters can not be 'null'.");
			return;
		}
		else
		{
			initJTable(itsTable);
			initComponents();
			setTitle("Meta Data for: " + itsTable.getName());
			setIconImage(MiningWindow.ICON);
			setLocation(100, 100);
			setSize(GUI.WINDOW_DEFAULT_SIZE);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setVisible(true);
		}
	}

	private void initJTable(Table theTable)
	{
		itsJTable = new JTable(new MetaDataTableModel(theTable));
		itsJTable.setPreferredScrollableViewportSize(GUI.WINDOW_DEFAULT_SIZE);
		itsJTable.setFillsViewportHeight(true);

		float aScalar = 0.3f;
		int anAttributeWidth = (int)(aScalar * GUI.WINDOW_DEFAULT_SIZE.width);
		int anOtherWidth = (int)((1.0f - aScalar / MetaDataTableHeader.values().length -1) * GUI.WINDOW_DEFAULT_SIZE.width);

		itsJTable.getColumnModel().getColumn(MetaDataTableHeader.ATTRIBUTE.columnNr).setPreferredWidth(anAttributeWidth);
		itsJTable.getColumnModel().getColumn(MetaDataTableHeader.CARDINALITY.columnNr).setPreferredWidth(anOtherWidth);
		itsJTable.getColumnModel().getColumn(MetaDataTableHeader.TYPE.columnNr).setPreferredWidth(anOtherWidth);
		itsJTable.getColumnModel().getColumn(MetaDataTableHeader.ENABLED.columnNr).setPreferredWidth(anOtherWidth);
		itsJTable.getColumnModel().getColumn(MetaDataTableHeader.HAS_MISSING.columnNr).setPreferredWidth(anOtherWidth);
		itsJTable.getColumnModel().getColumn(MetaDataTableHeader.MISSING_VALUE.columnNr).setPreferredWidth(anOtherWidth);
	}

	private void initComponents()
	{
		final JPanel aSouthPanel = new JPanel();
		JPanel anActionPanel = new JPanel(new GridLayout(1, 4));
		JPanel aSelectionPanel = new JPanel();
		JPanel aDisablePanel = new JPanel();
		JPanel aRadioButtonPanel = new JPanel();
		JPanel aChangeTypePanel = new JPanel();
		JPanel aSetMissingPanel = new JPanel();

		JScrollPane jScrollPane = new JScrollPane(itsJTable);

		// selection buttons
		aSelectionPanel.setBorder(GUI.buildBorder("Select"));
		aSelectionPanel.setLayout(new BoxLayout(aSelectionPanel, BoxLayout.PAGE_AXIS));

		addCentered(aSelectionPanel, GUI.buildButton("All", 'A', "all", this));
		// TODO could use generic loop over all AttributeTypes
		addCentered(aSelectionPanel, GUI.buildButton("All Nominal", 'N', AttributeType.NOMINAL.toString(), this));
		addCentered(aSelectionPanel, GUI.buildButton("All Numeric", 'U', AttributeType.NUMERIC.toString(), this));
//		addCentered(aSelectionPanel, GUI.buildButton("All Ordinal", 'O', AttributeType.ORDINAL.toString(), this));
		addCentered(aSelectionPanel, GUI.buildButton("All Binary", 'B', AttributeType.BINARY.toString(), this));
//		addCentered(aSelectionPanel, GUI.buildButton("Invert Selection", 'I', "invert", this));
		addCentered(aSelectionPanel, GUI.buildButton("Clear Selection", 'X', "clear", this));
		anActionPanel.add(aSelectionPanel);

		// change type
		aChangeTypePanel.setBorder(GUI.buildBorder("Set Type"));
		aChangeTypePanel.setLayout(new BoxLayout(aChangeTypePanel, BoxLayout.PAGE_AXIS));
		aChangeTypePanel.add(Box.createVerticalGlue());

		aRadioButtonPanel.setLayout(new BoxLayout(aRadioButtonPanel, BoxLayout.PAGE_AXIS));

		for (AttributeType at : AttributeType.values())
		{
			String aType = at.toString();
			JRadioButton aRadioButton = new JRadioButton(aType.toLowerCase());
			aRadioButton.setActionCommand(aType);	// UPPERCASE
			aRadioButtonPanel.add(aRadioButton);
		}

		for (Component rb : aRadioButtonPanel.getComponents())
			aNewType.add((AbstractButton) rb);

		if (aRadioButtonPanel.getComponents().length > 0)
			((JRadioButton) aRadioButtonPanel.getComponent(0)).setSelected(true);

		// TODO for now, disable ORDINAL, will change when implemented
		aNewType.remove((AbstractButton) aRadioButtonPanel.getComponent(2));
		aRadioButtonPanel.remove(2);

		addCentered(aChangeTypePanel, aRadioButtonPanel);
		aChangeTypePanel.add(Box.createVerticalGlue());
		addCentered(aChangeTypePanel, GUI.buildButton("Change Type", 'C', "type", this));
		anActionPanel.add(aChangeTypePanel);

		// disable / enable
		aDisablePanel.setBorder(GUI.buildBorder("Set Disabled/Enabled"));
		aDisablePanel.setLayout(new BoxLayout(aDisablePanel, BoxLayout.PAGE_AXIS));
		aDisablePanel.add(Box.createVerticalGlue());
		addCentered(aDisablePanel, GUI.buildButton("Disable Selected", 'D', "disable", this));
		aDisablePanel.add(Box.createVerticalGlue());
		addCentered(aDisablePanel, GUI.buildButton("Enable Selected", 'E', "enable", this));
		aDisablePanel.add(Box.createVerticalGlue());
		addCentered(aDisablePanel, GUI.buildButton("Toggle Selected", 'T', "toggle", this));
//		aDisablePanel.add(Box.createVerticalGlue());
		anActionPanel.add(aDisablePanel);

		// set missing
		aSetMissingPanel.setBorder(GUI.buildBorder("Set Value for Missing"));
		aSetMissingPanel.setLayout(new BoxLayout(aSetMissingPanel, BoxLayout.PAGE_AXIS));

		aSetMissingPanel.add(Box.createVerticalGlue());
		aNewMissingValue.setMaximumSize(GUI.BUTTON_MAXIMUM_SIZE);
		addCentered(aSetMissingPanel, aNewMissingValue);
		aSetMissingPanel.add(Box.createVerticalGlue());
		addCentered(aSetMissingPanel, GUI.buildButton("Change Value", 'M', "missing", this));
//		aSetMissingPanel.add(Box.createVerticalGlue());
		anActionPanel.add(aSetMissingPanel);

		anActionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		// Y_AXIS
		aSouthPanel.setLayout(new BoxLayout(aSouthPanel, BoxLayout.Y_AXIS));
		aSouthPanel.add(anActionPanel);

		// close button
		addCentered(aSouthPanel, GUI.buildButton("Close", 'C', "close", this));

		// feedback label
		Box aFeedBackBox = Box.createHorizontalBox();
		aFeedBackBox.add(GUI.buildLabel(" Last Action: ", null));
		itsFeedBackLabel = GUI.buildLabel("Meta Data loaded for " + itsTable.getName(), null);
		aFeedBackBox.add(itsFeedBackLabel);
		aFeedBackBox.add(Box.createHorizontalGlue());
		aSouthPanel.add(aFeedBackBox);

		getContentPane().add(jScrollPane, BorderLayout.CENTER);
		getContentPane().add(aSouthPanel, BorderLayout.SOUTH);

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowOpened(WindowEvent e)
			{
				aSouthPanel.getComponent(1).requestFocusInWindow();
			}
		});
	}

	private void addCentered(JPanel thePanel, JComponent theComponent)
	{
		theComponent.setAlignmentX(CENTER_ALIGNMENT);
		thePanel.add(theComponent);
	}

	@Override
	public void actionPerformed(ActionEvent theEvent)
	{
		String aCommand = theEvent.getActionCommand();
		// generic loop prevents hard coding all AttributeTypes
		for (AttributeType at : AttributeType.values())
		{
			if (at.toString().equals(aCommand))
			{
				for (int i = 0, j = itsTable.getNrColumns(); i < j; i++)
					if (itsTable.getColumn(i).getType() == at)
						itsJTable.addRowSelectionInterval(i, i);
				return;
			}
		}

		if ("all".equals(aCommand))
			itsJTable.selectAll();
/*
		else if ("invert".equals(aCommand))
		{
			for (int i = 0, j = itsTable.getColumns().size(); i < j; ++i)
			{
				if (jTable.isRowSelected(i))
					jTable.removeRowSelectionInterval(i, i);
				else
					jTable.addRowSelectionInterval(i, i);
			}
		}
*/
		else if ("clear".equals(aCommand))
			itsJTable.clearSelection();
		else if ("close".equals(aCommand))
			dispose();
		else
		{
			if ("disable".equals(aCommand) || "enable".equals(aCommand))
			{
				boolean enable = "enable".equals(aCommand);
				for (int i : itsJTable.getSelectedRows())
					itsTable.getColumn(i).setIsEnabled(enable);
			}
			else if ("toggle".equals(aCommand))
			{
				Column aColumn = null;
				for (int i : itsJTable.getSelectedRows())
				{
					aColumn = itsTable.getColumn(i);
					aColumn.setIsEnabled(!aColumn.getIsEnabled());
				}
			}
			else if ("type".equals(aCommand))
			{
				for (int i : itsJTable.getSelectedRows())
					itsTable.getColumn(i).setType(AttributeType.fromString(aNewType.getSelection().getActionCommand()));
					// TODO show messageDialog asking to treat first value as
					// 'true' or 'false' (see Column.toBinary())
					// TODO failed to change type warning
			}
			else if ("missing".equals(aCommand))
			{
				String aNewValue = aNewMissingValue.getText().trim();
				aNewMissingValue.setText(aNewValue);
				ArrayList<Integer> aWrongType = new ArrayList<Integer>(itsJTable.getSelectedRows().length);
				for (int i : itsJTable.getSelectedRows())
				{
					if (itsTable.getColumn(i).getHasMissingValues() &&
						!itsTable.getColumn(i).setNewMissingValue(aNewValue))
						aWrongType.add(i);
				}

				if (aWrongType.size() > 0)
				{
					itsJTable.getSelectionModel().clearSelection();
					for (int i : aWrongType)
						itsJTable.addRowSelectionInterval(i, i);

					String anIndicator;
					if (aWrongType.size() == 1)
					{
						Column aColumn = itsTable.getColumn(aWrongType.get(0));
						anIndicator = String.format("attribute '%s', which is of type '%s'.%n",
										aColumn.getName(),
										aColumn.getType());
					}
					else
						anIndicator = "some attributes. \nThey are of an incompatible type. See selection.";
					JOptionPane.showMessageDialog(null,
										String.format(
											"'%s' is not a valid value for %s",
											aNewValue,
											anIndicator),
										"alert",
										JOptionPane.ERROR_MESSAGE);
				}
			}
			itsJTable.repaint();
			itsMiningWindow.update();
		}
	}

}
