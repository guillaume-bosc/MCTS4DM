package nl.liacs.subdisc.gui;

import java.awt.*;
import java.text.*;
import java.util.*;

import javax.swing.*;

import nl.liacs.subdisc.*;

public class MultiTargetsWindow extends BasicJListWindow// implements ActionListener//, ListSelectionListener
{
	private static final long serialVersionUID = 1L;
	//alpha = '\u03B1' '&#x3b1' beta =  '\u03B2' '&#x3b2'
	private static final String ERROR =
			"Fields with invalid input: %d.\n" +
			"Alpha and Beta must be >= 0.\nNumber of repetitions must be > 1.";

	private int[] itsOriginalSelection;
	private SearchParameters itsSearchParameters;
	private JTextField itsAlphaField;
	private JTextField itsBetaField;
	private JComboBox itsRepeatedModelingBox =
		GUI.buildComboBox(new Object[] { "No","Yes" }, null);	// TODO checkBox
	private JTextField itsNrRepetitionsField;

	public MultiTargetsWindow(JList theJList, SearchParameters theSearchParameters)
	{
		super(theJList);

		if (theJList == null || theSearchParameters == null)
			constructorWarning("MultiTargetsWindow", true);
		else if (itsJList.getModel().getSize() == 0)
			constructorWarning("MultiTargetsWindow", false);
		else
		{
			itsOriginalSelection = itsJList.getSelectedIndices();
			itsSearchParameters = theSearchParameters;
			addAdditionalComponents();
			display("Multi-label details");
		}
	}

	private void addAdditionalComponents()
	{
		JPanel aPanel = new JPanel();
		aPanel.setBorder(GUI.buildBorder("Settings"));

		aPanel.setLayout(new GridLayout(4, 2));

		aPanel.add(GUI.buildLabel("Alpha", itsAlphaField));
		aPanel.add(itsAlphaField =
			GUI.buildTextField(String.valueOf(itsSearchParameters.getAlpha())));

		aPanel.add(GUI.buildLabel("Beta", itsBetaField));
		aPanel.add(itsBetaField =
			GUI.buildTextField(String.valueOf(itsSearchParameters.getBeta())));

		aPanel.add(GUI.buildLabel("Repeated modeling", itsRepeatedModelingBox));
		itsRepeatedModelingBox.setSelectedIndex(1);
		aPanel.add(itsRepeatedModelingBox);

		aPanel.add(GUI.buildLabel("Number of repetitions", itsNrRepetitionsField));
		aPanel.add(itsNrRepetitionsField = GUI.buildTextField(
				String.valueOf(itsSearchParameters.getPostProcessingCount())));

		getContentPane().add(aPanel, BorderLayout.NORTH);
	}

	private void showErrorDialog(String theMessage)
	{
		JOptionPane.showMessageDialog(this,
						theMessage,
						"Invalid input",
						JOptionPane.ERROR_MESSAGE);
	}

	@Override
	protected void disposeOk()
	{
		NumberFormat aFormat = NumberFormat.getNumberInstance(Locale.US);
		try
		{
			byte aNrInvalid = 0;

			float anAlpha = aFormat.parse(itsAlphaField.getText()).floatValue();
			if (anAlpha < 0.0f)
			{
				itsAlphaField.selectAll();
				++aNrInvalid;
			}
			else
				itsSearchParameters.setAlpha(anAlpha);

			float aBeta = aFormat.parse(itsBetaField.getText()).floatValue();
			if (aBeta < 0.0f)
			{
				itsBetaField.selectAll();
				++aNrInvalid;
			}
			else
				itsSearchParameters.setBeta(aBeta);

			itsSearchParameters.setPostProcessingDoAutoRun(
								itsRepeatedModelingBox.getSelectedIndex() == 1);

			int aNrRepetitions =
					aFormat.parse(itsNrRepetitionsField.getText()).intValue();
			// (itsNrRepetitions > itsSearchParameters.getMaximumSubgroups())
			if (aNrRepetitions <= 1)
			{
				itsNrRepetitionsField.selectAll();
				++aNrInvalid;
			}
			else
				itsSearchParameters.setPostProcessingCount(aNrRepetitions);

			if (aNrInvalid == 0)
				dispose();
			else
				showErrorDialog(String.format(ERROR, aNrInvalid));
		}
		catch (ParseException e)
		{
			showErrorDialog(e.getMessage());
		}
	}

	// constructor guarantees itsJListSize > 0
	@Override
	protected void disposeCancel()
	{
		itsJList.clearSelection();

		for (int i : itsOriginalSelection)
			itsJList.addSelectionInterval(i, i);

		dispose();
	}
}
