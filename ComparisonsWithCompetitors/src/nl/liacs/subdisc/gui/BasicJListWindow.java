package nl.liacs.subdisc.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import nl.liacs.subdisc.*;

public class BasicJListWindow extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	protected final JList itsJList;
	protected final int itsJListSize; // modality only blocks other WINDOWS
	private JLabel itsFeedBackLabel = new JLabel();

	public BasicJListWindow(JList theJList)
	{
		super.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
		itsJList = theJList;

		if (itsJList == null)
			itsJListSize = 0;
		else if ((itsJListSize = itsJList.getModel().getSize()) == 0)
			;
		else
			initComponents();
	}

	protected void constructorWarning(String theClass, boolean isNullWarning)
	{
		if (isNullWarning)
			Log.logCommandLine(theClass +
				" Constructor: parameter can not be 'null'.");
		else
			Log.logCommandLine(theClass +
				" Constructor: the list can not be empty.");
	}

	protected void display(String theTitle)
	{
		setTitle(theTitle);
		setIconImage(MiningWindow.ICON);
		setLocation(100, 100);
		pack();
		setVisible(true);
	}

	private void initComponents()
	{
		JPanel aMasterPanel = new JPanel();
		aMasterPanel.setLayout(new BoxLayout(aMasterPanel, BoxLayout.Y_AXIS));
		aMasterPanel.setBorder(GUI.buildBorder("Select"));

		aMasterPanel.add(new JScrollPane(itsJList), BorderLayout.CENTER);

		JPanel aButtonPanel = new JPanel();
		aButtonPanel.setLayout(new BoxLayout(aButtonPanel, BoxLayout.Y_AXIS));

		// selection buttons
		JPanel aSelectPanel = new JPanel();
		aSelectPanel.add(GUI.buildButton("Select All", 'A', "all", this));
		aSelectPanel.add(GUI.buildButton("Select None", 'N', "none", this));
		aSelectPanel.add(GUI.buildButton("Invert Selection", 'I', "invert", this));
		aButtonPanel.add(aSelectPanel);

		// confirm and cancel buttons
		final JPanel aClosePanel = new JPanel();
		aClosePanel.add(GUI.buildButton("OK", 'O', "ok", this));
		aClosePanel.add(GUI.buildButton("Cancel", 'C', "cancel", this));
		aButtonPanel.add(aClosePanel);

		aMasterPanel.add(aButtonPanel, BorderLayout.SOUTH);
		getContentPane().add(aMasterPanel, BorderLayout.CENTER);

		// feedback label
		Box aFeedBackBox = Box.createHorizontalBox();
		itsFeedBackLabel.setText(getFeedBackText());
		itsFeedBackLabel.setFont(GUI.DEFAULT_TEXT_FONT);
		aFeedBackBox.add(itsFeedBackLabel);
		aFeedBackBox.add(Box.createHorizontalGlue());
		getContentPane().add(aFeedBackBox, BorderLayout.SOUTH);

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowOpened(WindowEvent e)
			{
				aClosePanel.getComponent(0).requestFocusInWindow();
			}

			@Override
			public void windowClosing(WindowEvent e)
			{
				disposeCancel();
			}
		});
	}

	private String getFeedBackText()
	{
		return String.format(" %d of %d selected",
					itsJList.getSelectedIndices().length,
					itsJListSize);
	}

	@Override
	public void actionPerformed(ActionEvent theEvent)
	{
		String anAction = theEvent.getActionCommand();

		if ("all".equals(anAction))
			itsJList.setSelectionInterval(0, itsJListSize - 1);
		else if ("none".equals(anAction))
			itsJList.clearSelection();
		else if ("invert".equals(anAction))
		{
			int[] aSelection = itsJList.getSelectedIndices();
			itsJList.clearSelection();

			int i = 0;
			for (int j = 0, k = aSelection.length; j < k; i++)
			{
				if (i == aSelection[j])
					++j;
				else
					itsJList.setSelectedIndex(i);
			}
			if (i < itsJListSize)
				itsJList.addSelectionInterval(i, itsJListSize - 1);
		}
		else if ("ok".equals(anAction))
			disposeOk();
		else if ("cancel".equals(anAction))
			disposeCancel();

		itsFeedBackLabel.setText(getFeedBackText());
	}

	protected void disposeOk()
	{
		dispose();
	}

	protected void disposeCancel()
	{
		itsJList.clearSelection();
		dispose();
	}

/*
	// TODO update feedBackLabel on mouseSelections
	@Override
	public void valueChanged(ListSelectionEvent theEvent)
	{
//			compute selected targets and update TargetConcept
//			int[] aSelection = jListSecondaryTargets.getSelectedIndices();
//			ArrayList<Attribute> aList = new ArrayList<Attribute>(aSelection.length);
//			for (int anIndex : aSelection)
//				aList.add(itsTable.getAttribute(itsTable.getBinaryIndex(anIndex)));
		int aNrBinary = itsJList.getSelectedIndices().length;
		ArrayList<Attribute> aList = new ArrayList<Attribute>(aNrBinary);
		for (Column c : itsTable.getColumns())
		{
			if (c.getAttribute().isBinaryType())
			{
				aList.add(c.getAttribute());
				if (--aNrBinary == 0)
					break;
			}
		}

		itsTargetConcept.setMultiTargets(aList);

		//update GUI
		initTargetInfo();
	}
*/
}
