/*
 * TODO if changes are made also update other opened windows, eg. BrowseWindow
 * TODO all methods should update itsFeedBackLabel on failure/success
 */
package nl.liacs.subdisc.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import nl.liacs.subdisc.*;
import nl.liacs.subdisc.gui.MultiRegressionTargetsTableModel.MultiRegressionTargetsTableHeader;

public class MultiRegressionTargetsWindow extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private final MiningWindow itsMiningWindow;
	private final Table itsTable;
	private JTable itsJTable;
	private ButtonGroup aNewType = new ButtonGroup();
	private JTextField aNewMissingValue =
		new JTextField(AttributeType.getDefault().DEFAULT_MISSING_VALUE);
	private JComboBox itsInterceptRelevanceBox =
		GUI.buildComboBox(new Object[] { "No","Yes" }, null);
	private JLabel itsFeedBackLabel;
	private SearchParameters itsSearchParameters;

	public MultiRegressionTargetsWindow(JList theMultiRegressionTargets, SearchParameters theSearchParameters, Table theTable, MiningWindow theMiningWindow)
	{
		itsTable = theTable;
		itsMiningWindow = theMiningWindow;
		itsSearchParameters = theSearchParameters;

		if (theTable == null || theMiningWindow == null)
		{
			Log.logCommandLine("MultiRegressionTargetsWindow Constructor: parameters can not be 'null'.");
			return;
		}
		else
		{
			initJTable(itsTable);
			initComponents();
			setTitle("Setting secondary and tertiary regression variables for: " + itsTable.getName());
			setIconImage(MiningWindow.ICON);
			setLocation(100, 100);
			setSize(GUI.WINDOW_DEFAULT_SIZE);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setVisible(true);
		}
	}

	private void initJTable(Table theTable)
	{
		itsJTable = new JTable(new MultiRegressionTargetsTableModel(theTable));
		itsJTable.setPreferredScrollableViewportSize(GUI.WINDOW_DEFAULT_SIZE);
		itsJTable.setFillsViewportHeight(true);

		float aScalar = 0.3f;
		int anAttributeWidth = (int)(aScalar * GUI.WINDOW_DEFAULT_SIZE.width);
		int anOtherWidth = (int)((1.0f - aScalar / MultiRegressionTargetsTableHeader.values().length -1) * GUI.WINDOW_DEFAULT_SIZE.width);

		itsJTable.getColumnModel().getColumn(MultiRegressionTargetsTableHeader.ATTRIBUTE.columnNr).setPreferredWidth(anAttributeWidth);
		itsJTable.getColumnModel().getColumn(MultiRegressionTargetsTableHeader.TARGET_STATUS.columnNr).setPreferredWidth(anOtherWidth);
	}

	private void initComponents()
	{
		final JPanel aSouthPanel = new JPanel();
		JPanel anActionPanel = new JPanel(new GridLayout(1, 2));
		JPanel aRadioButtonPanel = new JPanel();
		JPanel aChangeTypePanel = new JPanel();
		JPanel anInterceptPanel = new JPanel();

		JScrollPane jScrollPane = new JScrollPane(itsJTable);

		// change type
		aChangeTypePanel.setBorder(GUI.buildBorder("Set Target Status"));
		aChangeTypePanel.setLayout(new BoxLayout(aChangeTypePanel, BoxLayout.PAGE_AXIS));
		aChangeTypePanel.add(Box.createVerticalGlue());

		aRadioButtonPanel.setLayout(new BoxLayout(aRadioButtonPanel, BoxLayout.PAGE_AXIS));

		for (int i=Column.FIRST_TARGET_STATUS; i<=Column.LAST_TARGET_STATUS; i++)
		{
			String aType = Column.getTargetText(i);
			JRadioButton aRadioButton = new JRadioButton(aType.toLowerCase());
			aRadioButton.setActionCommand(aType);	// UPPERCASE
			aRadioButtonPanel.add(aRadioButton);
		}

		for (Component rb : aRadioButtonPanel.getComponents())
			aNewType.add((AbstractButton) rb);

		if (aRadioButtonPanel.getComponents().length > 0)
			((JRadioButton) aRadioButtonPanel.getComponent(0)).setSelected(true);

		addCentered(aChangeTypePanel, aRadioButtonPanel);
		aChangeTypePanel.add(Box.createVerticalGlue());
		addCentered(aChangeTypePanel, GUI.buildButton("Change Target Status", 'C', "target status", this));
		anActionPanel.add(aChangeTypePanel);
		
		// intercept panel
		anInterceptPanel.setBorder(GUI.buildBorder("Include intercept in validation"));
		anInterceptPanel.setLayout(new BoxLayout(anInterceptPanel, BoxLayout.PAGE_AXIS));
		anInterceptPanel.add(Box.createVerticalGlue());

//		JCheckBox aCheckBox = new JCheckBox("Include intercept");
//		aCheckBox.setActionCommand("intercept check");
//		anInterceptPanel.add(aCheckBox);
		
		anInterceptPanel.add(GUI.buildLabel("Include intercept", itsInterceptRelevanceBox));
		itsInterceptRelevanceBox.setSelectedIndex(1);
		anInterceptPanel.add(itsInterceptRelevanceBox);

		anInterceptPanel.add(Box.createVerticalGlue());
		anActionPanel.add(anInterceptPanel);

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
//		for (AttributeType at : AttributeType.values())
//		{
//			if (at.toString().equals(aCommand))
//			{
//				for (int i = 0, j = itsTable.getNrColumns(); i < j; i++)
//					if (itsTable.getColumn(i).getType() == at)
//						itsJTable.addRowSelectionInterval(i, i);
//				return;
//			}
//		}

		if ("close".equals(aCommand))
			closingHook();
		else if ("target status".equals(aCommand))
		{
			for (int i : itsJTable.getSelectedRows())
				itsTable.getColumn(i).setTargetStatus(aNewType.getSelection().getActionCommand());
				// TODO show messageDialog asking to treat first value as
				// 'true' or 'false' (see Column.toBinary())
				// TODO failed to change type warning
			itsJTable.repaint();
			itsMiningWindow.update();
		}
	}

	public void closingHook()
	{
		List<Column> aSecondaryTargets = new ArrayList<Column>();
		List<Column> aTertiaryTargets = new ArrayList<Column>();
		TargetConcept aTargetConcept = itsSearchParameters.getTargetConcept();
		int aPrimaryTargetCount = 0;
		int aSecondaryTargetCount = 0;
		for (Column aColumn : itsTable.getColumns() )
		{
			if (aColumn.getType() == AttributeType.NUMERIC)
			{
				switch (aColumn.getTargetStatus())
				{
					case Column.PRIMARY :
					{
						aPrimaryTargetCount++;
						aTargetConcept.setPrimaryTarget(aColumn);
						break;
					}
					case Column.SECONDARY :
					{
						aSecondaryTargetCount++;
						aSecondaryTargets.add(aColumn);
						break;
					}
					case Column.TERTIARY :
					{
						aTertiaryTargets.add(aColumn);
						break;
					}
				}
			}
		}
		aTargetConcept.setSecondaryTargets(aSecondaryTargets);
		aTargetConcept.setTertiaryTargets(aTertiaryTargets);
		aTargetConcept.setInterceptRelevance(itsInterceptRelevanceBox.getSelectedIndex() == 1);
		if (aPrimaryTargetCount==1 && aSecondaryTargetCount>0)
			dispose();
	}
}
