package nl.liacs.subdisc.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import java.text.*;
import java.util.*;

import javax.swing.*;

import nl.liacs.subdisc.*;
import nl.liacs.subdisc.FileHandler.Action;

public class ResultWindow extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;

	private Table itsTable;
	private SearchParameters itsSearchParameters;
	private SubgroupDiscovery itsSubgroupDiscovery;
	private SubgroupSet itsSubgroupSet;
	private QualityMeasure itsQualityMeasure;

	private RegressionMeasure itsRegressionMeasureBase;
	private int itsFold;
	private BitSet itsBitSet;

	private ResultTableModel itsResultTableModel;
	private JTable itsSubgroupTable;

	//from mining window
	public ResultWindow(Table theTable, SubgroupDiscovery theSubgroupDiscovery, int theFold, BitSet theBitSet)
	{
		if (theTable == null || theSubgroupDiscovery == null)
		{
			Log.logCommandLine("ResultWindow Constructor: parameter(s) can not be 'null'.");
			return;
		}

		itsTable = theTable;
		itsSubgroupDiscovery = theSubgroupDiscovery;
		itsSubgroupSet = theSubgroupDiscovery.getResult();
		itsSearchParameters = theSubgroupDiscovery.getSearchParameters();
		itsQualityMeasure = theSubgroupDiscovery.getQualityMeasure();

		// only for DOUBLE_REGRESSION, avoids recalculation
		itsRegressionMeasureBase = theSubgroupDiscovery.getRegressionMeasureBase();

		// only used in MULTI_LABEL setting for now
		// if theFold == 0, itsBitSet is never used
		itsFold = theFold;
		itsBitSet = theBitSet;

		itsResultTableModel = new ResultTableModel(itsSubgroupSet, itsSearchParameters.getTargetConcept().getTargetType());
		itsSubgroupTable = new JTable(itsResultTableModel);
		if (!itsSubgroupSet.isEmpty())
			itsSubgroupTable.addRowSelectionInterval(0, 0);
		initialise();
	}

	//from parent result window (pattern team)
	public ResultWindow(ResultWindow theParentWindow, SubgroupSet thePatternTeam)
	{
		itsTable = theParentWindow.itsTable;
		itsSubgroupDiscovery = theParentWindow.itsSubgroupDiscovery;
		itsSubgroupSet = thePatternTeam;
		itsSearchParameters = itsSubgroupDiscovery.getSearchParameters();
		itsQualityMeasure = itsSubgroupDiscovery.getQualityMeasure();

		// only for DOUBLE_REGRESSION, avoids recalculation
		itsRegressionMeasureBase = itsSubgroupDiscovery.getRegressionMeasureBase();

		itsResultTableModel = new ResultTableModel(itsSubgroupSet, itsSearchParameters.getTargetConcept().getTargetType());
		itsSubgroupTable = new JTable(itsResultTableModel);
		if (!itsSubgroupSet.isEmpty())
			itsSubgroupTable.addRowSelectionInterval(0, 0);
		initialise();
	}

	private void initialise()
	{
		initComponents();

		// NOTE scaling is based on 8 columns, there are 20 unitWidths
		int aUnitWidth = (int)(0.05f * GUI.WINDOW_DEFAULT_SIZE.width);

		itsSubgroupTable.getColumnModel().getColumn(0).setPreferredWidth(aUnitWidth);
		itsSubgroupTable.getColumnModel().getColumn(1).setPreferredWidth(aUnitWidth);
		itsSubgroupTable.getColumnModel().getColumn(2).setPreferredWidth((int)(1.75f * aUnitWidth));
		itsSubgroupTable.getColumnModel().getColumn(3).setPreferredWidth((int)(1.75f * aUnitWidth));
		itsSubgroupTable.getColumnModel().getColumn(4).setPreferredWidth((int)(1.75f * aUnitWidth));
		itsSubgroupTable.getColumnModel().getColumn(5).setPreferredWidth((int)(1.75f * aUnitWidth));
		itsSubgroupTable.getColumnModel().getColumn(6).setPreferredWidth(2 * aUnitWidth);
		itsSubgroupTable.getColumnModel().getColumn(7).setPreferredWidth(9 * aUnitWidth);

		itsScrollPane.add(itsSubgroupTable);
		itsScrollPane.setViewportView(itsSubgroupTable);

		itsSubgroupTable.setRowSelectionAllowed(true);
		itsSubgroupTable.setColumnSelectionAllowed(false);
		itsSubgroupTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		itsSubgroupTable.addKeyListener(new KeyListener()
		{
			public void keyPressed(KeyEvent key)
			{
				if (key.getKeyCode() == KeyEvent.VK_ENTER)
					if (itsSubgroupTable.getRowCount() > 0 && itsSearchParameters.getTargetType() != TargetType.SINGLE_NOMINAL)
						jButtonShowModelActionPerformed();
				if (key.getKeyCode() == KeyEvent.VK_DELETE)
					if(itsSubgroupTable.getRowCount() > 0)
						jButtonDeleteSubgroupsActionPerformed();
			}

			public void keyReleased(KeyEvent key) {}

			public void keyTyped(KeyEvent key) {}
		});

		setTitle();
		setIconImage(MiningWindow.ICON);
		setLocation(100, 100);
		setSize(GUI.WINDOW_DEFAULT_SIZE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	private void initComponents()
	{
		jPanelSouth = new JPanel(new GridLayout(3, 1));
		JPanel aSubgroupPanel = new JPanel();
		JPanel aSubgroupSetPanel = new JPanel();
		JPanel aClosePanel = new JPanel();

		itsScrollPane = new JScrollPane();

		//selected subgroups ********************************

		jButtonShowModel = GUI.buildButton("Show Model", 'M', "model", this);
		aSubgroupPanel.add(jButtonShowModel);

		jButtonBrowseSubgroups = GUI.buildButton("Browse", 'B', "browse", this);
		aSubgroupPanel.add(jButtonBrowseSubgroups);

		jButtonDeleteSubgroups = GUI.buildButton("Delete", 'D', "delete", this);
		aSubgroupPanel.add(jButtonDeleteSubgroups);

		//subgroup set *********************************

		jButtonPatternTeam = GUI.buildButton("Pattern Team", 'P', "patternteam", this);
		aSubgroupSetPanel.add(jButtonPatternTeam);

		jButtonROC = GUI.buildButton("ROC", 'R', "roc", this);
		jButtonROC.setPreferredSize(GUI.BUTTON_MEDIUM_SIZE);
		aSubgroupSetPanel.add(jButtonROC);

		jButtonSave = GUI.buildButton("Save", 'S', "save", this);
		jButtonSave.setPreferredSize(GUI.BUTTON_MEDIUM_SIZE);
		aSubgroupSetPanel.add(jButtonSave);

		jButtonPrint = GUI.buildButton("Print", 'I', "print", this);
		jButtonPrint.setPreferredSize(GUI.BUTTON_MEDIUM_SIZE);
		aSubgroupSetPanel.add(jButtonPrint);

		jButtonPValues = GUI.buildButton("Gaussian p-Values", 'V', "compute_p", this);
		aSubgroupSetPanel.add(jButtonPValues);

		jButtonRegressionTest = GUI.buildButton("Regression Test", 'T', "regression", this);
		aSubgroupSetPanel.add(jButtonRegressionTest);

		jButtonEmpirical = GUI.buildButton("Empirical p-Values", 'E', "empirical_p", this);
		aSubgroupSetPanel.add(jButtonEmpirical);

		jButtonFold = GUI.buildButton("Fold members", 'F', "fold", this);
		aSubgroupSetPanel.add(jButtonFold);

		//close *********************************

		jButtonCloseWindow = GUI.buildButton("Close", 'C', "close", this);
		aClosePanel.add(jButtonCloseWindow);

		enableButtonsCheck();
		jPanelSouth.add(aSubgroupPanel);
		jPanelSouth.add(aSubgroupSetPanel);
		jPanelSouth.add(aClosePanel);
		getContentPane().add(jPanelSouth, BorderLayout.SOUTH);
		getContentPane().add(itsScrollPane, BorderLayout.CENTER);
	}

	public void setTitle()
	{
		StringBuilder s = new StringBuilder(150);
		if (itsSubgroupSet.isEmpty())
			s.append("No subgroups found that match the set criterion");
		else
			s.append(itsSubgroupSet.size() + " subgroups found");

		s.append(";  target table = ");
		s.append(itsTable.getName());

		s.append(";  quality measure = ");
		s.append(itsSearchParameters.getQualityMeasure().GUI_TEXT);

		TargetType aTargetType = itsSearchParameters.getTargetType();
		if (TargetType.hasTargetValue(aTargetType))
		{
			s.append(";  target value = ");
			if (aTargetType == TargetType.SINGLE_NOMINAL)
				s.append(itsSearchParameters.getTargetConcept().getTargetValue());
			// else DOUBLE_REGRESSION or DOUBLE_CORRELATION
			else
				s.append(itsSearchParameters.getTargetConcept().getSecondaryTarget().getName());
		}
		if (itsFold != 0)
			s.append(";  fold = " + itsFold);

		NumberFormat aFormatter = NumberFormat.getNumberInstance();
		aFormatter.setMaximumFractionDigits(3);
		if (!Double.isNaN(itsSubgroupSet.getJointEntropy()))
			s.append(";  joint entropy = " + aFormatter.format(itsSubgroupSet.getJointEntropy()));
		setTitle(s.toString());
	}

	@Override
	public void actionPerformed(ActionEvent theEvent)
	{
		String aCommand = theEvent.getActionCommand();
		if ("model".equals(aCommand))
			jButtonShowModelActionPerformed();
		else if ("patternteam".equals(aCommand))
			jButtonPatternTeamActionPerformed();
		else if ("roc".equals(aCommand))
			jButtonROCActionPerformed();
		else if ("browse".equals(aCommand))
			jButtonBrowseSubgroupsActionPerformed();
		else if ("delete".equals(aCommand))
			jButtonDeleteSubgroupsActionPerformed();
		else if ("compute_p".equals(aCommand))
			jButtonPValuesActionPerformed();
		else if ("regression".equals(aCommand))
			jButtonRegressionTestActionPerformed();
		else if ("empirical_p".equals(aCommand))
			jButtonEmpiricalActionPerformed();
		else if ("fold".equals(aCommand))
			jButtonFoldActionPerformed();
		else if ("save".equals(aCommand))
			jButtonSaveActionPerformed();
		else if ("print".equals(aCommand))
			jButtonPrintActionPerformed();
		else if ("close".equals(aCommand))
			dispose();
	}

	private void enableButtonsCheck()
	{
		if (itsSubgroupSet.isEmpty())
		{
			jButtonShowModel.setEnabled(false);
			jButtonROC.setEnabled(false);
			jButtonPatternTeam.setEnabled(false);
			jButtonBrowseSubgroups.setEnabled(false);
			jButtonDeleteSubgroups.setEnabled(false);
			jButtonPValues.setEnabled(false);
			jButtonRegressionTest.setEnabled(false);
			jButtonEmpirical.setEnabled(false);
			jButtonFold.setEnabled(false);
			jButtonSave.setEnabled(false);
			jButtonPrint.setEnabled(false);
			return;
		}
		else
		{
			TargetType aTargetType = itsSearchParameters.getTargetType();

			jButtonShowModel.setVisible(TargetType.hasBaseModel(aTargetType));
			jButtonROC.setVisible(aTargetType == TargetType.SINGLE_NOMINAL);
			// TargetType.SINGLE_NUMERIC shortcut for cmsb versions
			jButtonPValues.setVisible(aTargetType != TargetType.SINGLE_NUMERIC);
			jButtonRegressionTest.setVisible(aTargetType != TargetType.SINGLE_NUMERIC);
			jButtonEmpirical.setVisible(aTargetType != TargetType.SINGLE_NUMERIC);

			jButtonFold.setVisible(itsFold != 0);
		}
	}

	/*
	 * TODO use Subgroup.getID/getConditions() for title
	 * Current naming relies on non-sortable columns
	 */
	private void jButtonShowModelActionPerformed()
	{
		int[] aSelectionIndex = itsSubgroupTable.getSelectedRows();
		if (aSelectionIndex.length == 0)
			return;

		switch (itsSearchParameters.getTargetType())
		{
			case SINGLE_NUMERIC :
			{
				Column aTarget = itsSearchParameters.getTargetConcept().getPrimaryTarget();
				ProbabilityDensityFunction aPDF = new ProbabilityDensityFunction(aTarget);
				aPDF.smooth();

				int[] aSelection = itsSubgroupTable.getSelectedRows();
				Iterator<Subgroup> anIterator = itsSubgroupSet.iterator();

				for (int i = 0, j = aSelection.length, k = 0; i < j; ++k)
				{
					int aNext = aSelection[k];
					while (i++ < aNext)
						anIterator.next();
					Subgroup aSubgroup = anIterator.next();

					ProbabilityDensityFunction aSubgroupPDF = new ProbabilityDensityFunction(aPDF, aSubgroup.getMembers());
					aSubgroupPDF.smooth();
					new ModelWindow(aTarget, aPDF, aSubgroupPDF, "Subgroup " + aSubgroup.getID());
				}

				break;
			}
			case DOUBLE_CORRELATION :
			{
				modelWindowHelper(TargetType.DOUBLE_CORRELATION);
				break;
			}
			case MULTI_LABEL :
			{
				/*
				 * TODO inefficient loop
				 * use convertViewToModel(i) to select correct s, then use s.ID
				 */
				int i = 0;
				while (i < aSelectionIndex.length)
				{
					Log.logCommandLine("subgroup " + (aSelectionIndex[i]+1));
					int aCount = 0;
					for (Subgroup s : itsSubgroupSet)
					{
						if (aCount == aSelectionIndex[i])
							new ModelWindow(s.getDAG(), 1000, 800)
								.setTitle("Subgroup " +
									Integer.toString(aSelectionIndex[i]+1) +
									": induced Bayesian Network");

						aCount++;
					}

					i++;
				}
				break;
			}
			case DOUBLE_REGRESSION :
			{
				modelWindowHelper(TargetType.DOUBLE_REGRESSION);
				break;
// TODO for stable jar, disable, added in revision 844
/*
				int i=0;
				Log.logCommandLine("======================================================");
				Log.logCommandLine("Global model:");
				Log.logCommandLine(itsSearchParameters.getTargetConcept().getGlobalRegressionModel());
				Log.logCommandLine("------------------------------------------------------");
				while (i<aSelectionIndex.length)
				{
					Log.logCommandLine("Model for subgroup " + (aSelectionIndex[i]+1) + ":");
					int aCount = 0;
					for (Subgroup s : itsSubgroupSet)
					{
						if (aCount == aSelectionIndex[i])
							Log.logCommandLine(s.getRegressionModel());
						aCount++;
					}

					i++;
				}
				Log.logCommandLine("======================================================");
				break;
*/
			}
			default :
				break;
		}
	}

	private void modelWindowHelper(TargetType theTargetType)
	{
		final TargetConcept aTargetConcept = itsSearchParameters.getTargetConcept();
		final boolean isRegressionSetting = (theTargetType == TargetType.DOUBLE_REGRESSION);
		RegressionMeasure aRM = null;

		int[] aSelection = itsSubgroupTable.getSelectedRows();
		Iterator<Subgroup> anIterator = itsSubgroupSet.iterator();

		for (int i = 0, j = aSelection.length, k = 0; i < j; ++k)
		{
			int aNext = aSelection[k];
			while (i++ < aNext)
				anIterator.next();
			Subgroup aSubgroup = anIterator.next();

			if (isRegressionSetting)
			{
				aRM = new RegressionMeasure(itsRegressionMeasureBase, aSubgroup.getMembers());
				aRM.getEvaluationMeasureValue();
			}

			new ModelWindow(aTargetConcept.getPrimaryTarget(),
					aTargetConcept.getSecondaryTarget(),
					aRM,
					aSubgroup);
		}
	}

	private void jButtonROCActionPerformed()
	{
		new ROCCurveWindow(itsSubgroupSet, itsSearchParameters, itsQualityMeasure);
	}

	private void jButtonPatternTeamActionPerformed()
	{
		String anInputString= JOptionPane.showInputDialog("Set pattern team size:");
		if (anInputString == null || anInputString.equals(""))
			return;

		setBusy(true);
		try
		{
			int aValue = new Integer(anInputString).intValue();
			SubgroupSet aPatternTeam = itsSubgroupSet.getPatternTeam(itsTable, aValue);
			new ResultWindow(this, aPatternTeam);
		}
		catch (Exception e)
		{
			Log.logCommandLine(e.toString());
			JOptionPane.showMessageDialog(null, "Not a valid input value!", "Warning", JOptionPane.ERROR_MESSAGE);
		}
		setBusy(false);
	}

	private void jButtonBrowseSubgroupsActionPerformed()
	{
		if (itsSubgroupSet.isEmpty())
			return;

		int[] aSelection = itsSubgroupTable.getSelectedRows();
		Iterator<Subgroup> anIterator = itsSubgroupSet.iterator();

		for (int i = 0, j = aSelection.length, k = 0; i < j; ++k)
		{
			int aNext = aSelection[k];
			while (i++ < aNext)
				anIterator.next();
			new BrowseWindow(itsTable, anIterator.next());
		}
	}

	private void jButtonDeleteSubgroupsActionPerformed()
	{
		if (itsSubgroupSet.isEmpty())
			return;

		int aSubgroupSetIndex = itsSubgroupSet.size();
		int[] aSelectionIndex = itsSubgroupTable.getSelectedRows();
		Iterator<Subgroup> anIterator = itsSubgroupSet.descendingIterator();

		for (int i = aSelectionIndex.length - 1; i >= 0; i--)
		{
			while (--aSubgroupSetIndex >= aSelectionIndex[i])
				anIterator.next();
			anIterator.remove();

			if (anIterator.hasNext())
				anIterator.next();
		}
		itsSubgroupTable.getSelectionModel().clearSelection();
		itsSubgroupTable.repaint();

		if (!itsSubgroupSet.isEmpty())
			itsSubgroupTable.addRowSelectionInterval(0, 0);
		else
			jButtonDeleteSubgroups.setEnabled(false);
	}

	private void jButtonPValuesActionPerformed()
	{
		setBusy(true);
		// Obtain input
		double[] aQualities = obtainRandomQualities();
		if (aQualities == null)
			return;
		NormalDistribution aDistro = new NormalDistribution(aQualities);

		for (Subgroup aSubgroup : itsSubgroupSet)
			aSubgroup.setPValue(aDistro);

		itsSubgroupTable.repaint();
		setBusy(false);
	}

	private void jButtonRegressionTestActionPerformed()
	{
		setBusy(true);
		// Obtain input
		double[] aQualities = obtainRandomQualities();
		if (aQualities == null)
			return;
		Validation aValidation = new Validation(itsSearchParameters, itsTable, itsQualityMeasure);
		double[] aRegressionTestScore = aValidation.performRegressionTest(aQualities, itsSubgroupSet);
		setBusy(false);
		JOptionPane.showMessageDialog(null, "The regression test score equals\nfor k =  1 : "+aRegressionTestScore[0]+"\nfor k = 10 : "+aRegressionTestScore[1]);
	}

	private void jButtonEmpiricalActionPerformed()
	{
		setBusy(true);
		// Obtain input
		double[] aQualities = obtainRandomQualities();
		if ( aQualities == null)
			return;

		Validation aValidation = new Validation(itsSearchParameters, itsTable, itsQualityMeasure);
		double aPValue = aValidation.computeEmpiricalPValue(aQualities, itsSubgroupSet);
		JOptionPane.showMessageDialog(this, "The empirical p-value is p = " + aPValue);

		for (Subgroup aSubgroup : itsSubgroupSet)
			aSubgroup.setEmpiricalPValue(aQualities);

		itsSubgroupTable.repaint();
		setBusy(false);
	}

	private void jButtonFoldActionPerformed()
	{
		Log.logCommandLine("Members of the training set of fold " + itsFold);
		Log.logCommandLine(itsBitSet.toString());
	}

	private double[] obtainRandomQualities()
	{
		String[] aSetup = new RandomQualitiesWindow(itsSearchParameters.getTargetType()).getSettings();

		Log.logCommandLine(aSetup[0] + "=" + aSetup[1]);
		if (!RandomQualitiesWindow.isValidRandomQualitiesSetup(aSetup))
			return null;

		// Compute qualities
		Validation aValidation = new Validation(itsSearchParameters, itsTable, itsQualityMeasure);
		return aValidation.getQualities(aSetup);
	}

	private void jButtonSaveActionPerformed()
	{
		File aFile = new FileHandler(Action.SAVE).getFile();
		if (aFile == null)
			return; // cancelled

		XMLAutoRun.save(itsSubgroupSet, aFile.getAbsolutePath(), itsSearchParameters.getTargetType());
	}

	private void jButtonPrintActionPerformed()
	{
		try
		{
			itsSubgroupTable.print();
		}
		catch (PrinterException e)
		{
			JOptionPane.showMessageDialog(this,
							"Print error!",
							"Warning",
							JOptionPane.WARNING_MESSAGE);
			e.printStackTrace();
		}
	}

	private void setBusy(boolean isBusy)
	{
		if (isBusy)
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		else
			this.setCursor(Cursor.getDefaultCursor());
	}

	private JPanel jPanelSouth;
	private JButton jButtonShowModel;
	private JButton jButtonBrowseSubgroups;
	private JButton jButtonDeleteSubgroups;
	private JButton jButtonFold;
	private JButton jButtonPValues;
	private JButton jButtonRegressionTest;
	private JButton jButtonEmpirical;
	private JButton jButtonPatternTeam;
	private JButton jButtonROC;
	private JButton jButtonSave;
	private JButton jButtonPrint;
	private JButton jButtonCloseWindow;
	private JScrollPane itsScrollPane;
}
