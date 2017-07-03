package nl.liacs.subdisc.cui;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;

import nl.liacs.subdisc.*;
import nl.liacs.subdisc.gui.*;

public class IdentifierChooser extends JDialog implements ActionListener
{
	private static final long serialVersionUID = 1L;

	private final ButtonGroup itsHeaderButtons = new ButtonGroup();
	private final ButtonGroup itsIdentifierTypeButtons = new ButtonGroup();

	private int itsIdentifierColumnIndex = -1;
	private String itsIdentifierType = "entrez";

	public IdentifierChooser(List<Column> theColumns)
	{
		super.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);

		// TODO ErrorLog
		if (theColumns == null)
			Log.logCommandLine(
			"IdentifierChooser Constructor: parameter can not be 'null'.");
		else
		{
			setTitle("Identifier Chooser");
			setIconImage(MiningWindow.ICON);
			setLocation(100, 100);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			initComponents(theColumns);
			pack();
			setVisible(true);
		}
	}

	private void initComponents(List<Column> theColumns)
	{
		JPanel aColumnsPanel = new JPanel();
		JPanel anIdentifierTypePanel = new JPanel();
		JPanel aButtonPanel = new JPanel();
		JRadioButton aRadioButton;

		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		if (theColumns.size() ==  0)
			aColumnsPanel.add(new JLabel("No Column Headers Found"));
		else
		{
			aColumnsPanel.setLayout(new BoxLayout(aColumnsPanel,
								BoxLayout.PAGE_AXIS));

			for (int i = 0, j = theColumns.size(); i < j; ++i)
			{
				aRadioButton = new JRadioButton(theColumns.get(i).getName());
				aRadioButton.setActionCommand(String.valueOf(i));
				aColumnsPanel.add(aRadioButton);
			}

			for (Component c : aColumnsPanel.getComponents())
				itsHeaderButtons.add((AbstractButton) c);

			((JRadioButton) aColumnsPanel.getComponent(0)).setSelected(true);

			aRadioButton = new JRadioButton("Entrez");
			aRadioButton.setActionCommand("entrez");
			anIdentifierTypePanel.add(aRadioButton);
			aRadioButton = new JRadioButton("GO");
			aRadioButton.setActionCommand("go");
			anIdentifierTypePanel.add(aRadioButton);

			((JRadioButton) anIdentifierTypePanel.getComponent(0)).setSelected(true);

			for (Component c : anIdentifierTypePanel.getComponents())
				itsIdentifierTypeButtons.add((AbstractButton) c);

			aButtonPanel.add(
				GUI.buildButton("Use Column", KeyEvent.VK_U, "column", this));
			aButtonPanel.add(
				GUI.buildButton("Cancel", KeyEvent.VK_C, "cancel", this));
		}
		getContentPane().add(new JLabel("Select the identifier column:"));
		getContentPane().add(Box.createVerticalStrut(5));
		getContentPane().add(new JScrollPane(aColumnsPanel));
		getContentPane().add(Box.createVerticalStrut(10));
		getContentPane().add(new JLabel("Select the identifier type:"));
		getContentPane().add(anIdentifierTypePanel);
		getContentPane().add(aButtonPanel);
	}

	@Override
	public void actionPerformed(ActionEvent theEvent)
	{
		String aCommand = theEvent.getActionCommand();

		if ("column".equals(aCommand))
		{
			itsIdentifierColumnIndex =
				Integer.parseInt(itsHeaderButtons
							.getSelection()
							.getActionCommand());
			itsIdentifierType = itsIdentifierTypeButtons
						.getSelection()
						.getActionCommand();
			dispose();
		}
		else if ("cancel".equals(aCommand))
			dispose();
	}

	public int getIdentifierColumnIndex()
	{
		if (itsIdentifierColumnIndex == -1)
			Log.logCommandLine("No identifier column selected.");

		return itsIdentifierColumnIndex;
	}

	public String getIdentifierType()
	{
		if (itsIdentifierType == null)
			Log.logCommandLine("No identifier type selected.");

		return itsIdentifierType;
	}
}
