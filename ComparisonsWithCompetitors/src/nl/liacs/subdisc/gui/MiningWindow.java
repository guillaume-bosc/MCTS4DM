package nl.liacs.subdisc.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

import nl.liacs.subdisc.*;
import nl.liacs.subdisc.FileHandler.Action;
import nl.liacs.subdisc.XMLAutoRun.AutoRun;
import nl.liacs.subdisc.Process;
import nl.liacs.subdisc.cui.*;

public class MiningWindow extends JFrame implements ActionListener
{
	static final long serialVersionUID = 1L;

	public static final Image ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(MiningWindow.class.getResource("/icon.jpg"))).getImage();

	private Table itsTable;

	// target info
	private int itsPositiveCount; // nominal target
	private float itsTargetAverage; // numeric target

	// TODO MM there should be at most 1 MiningWindow();
	private final MiningWindow masterWindow = this;
	private SearchParameters itsSearchParameters = new SearchParameters();
	private TargetConcept itsTargetConcept = new TargetConcept();

	public MiningWindow()
	{
		initMiningWindow();
	}

	public MiningWindow(Table theTable)
	{
		if (theTable != null)
		{
			itsTable = theTable;
			initMiningWindow();
			initGuiComponents();
		}
		else
			initMiningWindow();
	}

	// loaded from XML
	public MiningWindow(Table theTable, SearchParameters theSearchParameters)
	{
		if (theTable != null)
		{
			itsTable = theTable;
			initMiningWindow();

			if (theSearchParameters != null)
				itsTargetConcept = theSearchParameters.getTargetConcept();
			itsSearchParameters = theSearchParameters;
			initGuiComponentsFromFile();
		}
		else
			initMiningWindow();
	}

	private void initMiningWindow()
	{
		// Initialise graphical components
		initComponents();
		initStaticGuiComponents();
		enableTableDependentComponents(itsTable != null);
		initTitle();
		setIconImage(ICON);
		setLocation(100, 100);
		setSize(700, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initJMenuGui(); // for GUI debugging only, DO NOT REMOVE
		setVisible(true);

		// Open log/debug files
		Log.openFileOutputStreams();
	}

	private void initComponents()
	{
		// MENU BAR ====================================================
		jMiningWindowMenuBar = new JMenuBar();
		jMiningWindowMenuBar.setFont(GUI.DEFAULT_TEXT_FONT);

		// MENU BAR - FILE
		jMenuFile = initMenu(STD.FILE);

		jMenuItemOpenFile = initMenuItem(STD.OPEN_FILE);
		jMenuFile.add(jMenuItemOpenFile);

		jMenuItemBrowse = initMenuItem(STD.BROWSE);
		jMenuFile.add(jMenuItemBrowse);

		jMenuItemExplore = initMenuItem(STD.EXPLORE);
		jMenuFile.add(jMenuItemExplore);

		jMenuItemMetaData = initMenuItem(STD.META_DATA);
		jMenuFile.add(jMenuItemMetaData);

		jMenuFile.addSeparator();

		jMenuItemSubgroupDiscovery = initMenuItem(STD.SUBGROUP_DISCOVERY);
		jMenuFile.add(jMenuItemSubgroupDiscovery);

		jMenuFile.addSeparator();

		jMenuItemCreateAutorunFile = initMenuItem(STD.CREATE_AUTORUN_FILE);
		jMenuFile.add(jMenuItemCreateAutorunFile);

		jMenuItemAddToAutorunFile = initMenuItem(STD.ADD_TO_AUTORUN_FILE);
		jMenuFile.add(jMenuItemAddToAutorunFile);

		// TODO MM add when implemented
		jMenuItemLoadSampledSubgroups = initMenuItem(STD.LOAD_SAMPLED_SUBGROUPS);
		//jMenuFile.add(jMenuItemLoadSampledSubgroups);

		jMenuFile.addSeparator();

		jMenuItemExit = initMenuItem(STD.EXIT);
		jMenuFile.add(jMenuItemExit);
		jMiningWindowMenuBar.add(jMenuFile);

		// MENU BAR - ENRICHMENT
		jMenuEnrichment = initMenu(STD.ENRICHMENT);

		jMenuItemAddCuiEnrichmentSource = initMenuItem(STD.ADD_CUI_DOMAIN);
		jMenuEnrichment.add(jMenuItemAddCuiEnrichmentSource);

		// TODO MM add when implemented
		jMenuItemAddGoEnrichmentSource  = initMenuItem(STD.ADD_GO_DOMAIN);
//		jMenuEnrichment.add(jMenuItemAddCuiEnrichmentSource);

		// TODO MM add when implemented
		jMenuItemAddCustomEnrichmentSource = initMenuItem(STD.ADD_CUSTOM_DOMAIN);
//		jMenuEnrichment.add(jMenuItemAddCuiEnrichmentSource);

		jMenuItemRemoveEnrichmentSource = initMenuItem(STD.REMOVE_ENRICHMENT_SOURCE);
		jMenuItemRemoveEnrichmentSource.setEnabled(false);
		jMenuEnrichment.add(jMenuItemRemoveEnrichmentSource);

		jMiningWindowMenuBar.add(jMenuEnrichment);

		// MENU BAR - GUI
		jMenuGui = new JMenu();
		jMenuGui.setFont(GUI.DEFAULT_TEXT_FONT);
		jMenuGui.setText("Gui");
		jMenuGui.setMnemonic('G');
		if (GUI_DEBUG)
			jMiningWindowMenuBar.add(jMenuGui);

		// MENU BAR - ABOUT
		jMenuAbout = initMenu(STD.ABOUT);
		jMenuAbout.setMnemonic(STD.ABOUT.MNEMONIC);

		jMenuItemAboutCortana = initMenuItem(STD.ABOUT_CORTANA);
		jMenuAbout.add(jMenuItemAboutCortana);

		jMiningWindowMenuBar.add(jMenuAbout);

		// DATA SET, TARGET CONCEPT, SEARCH CONDITIONS, SEARCH STRATEGY
		jPanelCenter = new JPanel();	// 4 panels
		jPanelCenter.setLayout(new GridLayout(2, 2));
		jPanelSouth = new JPanel();	// mining buttons

		// DATA SET ====================================================
		jPanelDataSet = new JPanel();
		jPanelDataSetLabels = new JPanel();
		jPanelDataSetFields = new JPanel();
		jPanelDataSetButtons = new JPanel();
		jButtonBrowse = new JButton();
		jButtonExplore = new JButton();
		jButtonMetaData = new JButton();

		jPanelDataSet.setLayout(new BorderLayout(40, 0));
		jPanelDataSet.setBorder(GUI.buildBorder("Dataset"));

		// DATA SET -labels
		jPanelDataSetLabels.setLayout(new GridLayout(7, 1));

		jLabelTargetTable = initJLabel("target table");
		jPanelDataSetLabels.add(jLabelTargetTable);

		jLabelNrExamples = initJLabel("# examples");
		jPanelDataSetLabels.add(jLabelNrExamples);

		jLabelNrColumns = initJLabel("# columns");
		jPanelDataSetLabels.add(jLabelNrColumns);

		jLabelNrNominals = initJLabel("# nominals");
		jPanelDataSetLabels.add(jLabelNrNominals);

		jLabelNrNumerics = initJLabel("# numerics");
		jPanelDataSetLabels.add(jLabelNrNumerics);

		jLabelNrBinaries = initJLabel("# binaries");
		jPanelDataSetLabels.add(jLabelNrBinaries);

		jPanelDataSet.add(jPanelDataSetLabels, BorderLayout.WEST);

		// DATA SET - 'fields' (number of instances per AttributeType)
		jPanelDataSetFields.setLayout(new GridLayout(7, 1));

		jLabelTargetTableName = initJLabel("");
		jPanelDataSetFields.add(jLabelTargetTableName);

		jLabelNrExamplesNr = initJLabel("");
		jPanelDataSetFields.add(jLabelNrExamplesNr);

		jLabelNrColumnsNr = initJLabel("");
		jPanelDataSetFields.add(jLabelNrColumnsNr);

		jLabelNrNominalsNr = initJLabel("");
		jPanelDataSetFields.add(jLabelNrNominalsNr);

		jLabelNrNumericsNr = initJLabel("");
		jPanelDataSetFields.add(jLabelNrNumericsNr);

		jLabelNrBinariesNr = initJLabel("");
		jPanelDataSetFields.add(jLabelNrBinariesNr);

		// DATA SET - buttons
		jPanelDataSet.add(jPanelDataSetFields, BorderLayout.CENTER);

		final JPanel aButtonPanel = new JPanel();
		aButtonPanel.setLayout(new GridLayout(2, 2));

		jButtonBrowse = initButton(STD.BROWSE);
		aButtonPanel.add(jButtonBrowse);

		jButtonExplore = initButton(STD.EXPLORE);
		aButtonPanel.add(jButtonExplore);

		jButtonMetaData = initButton(STD.META_DATA);
		aButtonPanel.add(jButtonMetaData);

		jPanelDataSetButtons.add(aButtonPanel);

		jPanelDataSet.add(jPanelDataSetButtons, BorderLayout.SOUTH);
		jPanelCenter.add(jPanelDataSet);

		// TARGET CONCEPT ==============================================
		jPanelTargetConcept = new JPanel();
		jPanelTargetConceptLabels = new JPanel();
		jPanelTargetConceptFields = new JPanel();
		// permanently maintained JLists
		jListMultiRegressionTargets = new JList(new DefaultListModel());
		jListMultiTargets = new JList(new DefaultListModel());

		jPanelTargetConcept.setLayout(new BoxLayout(jPanelTargetConcept, BoxLayout.X_AXIS));
		jPanelTargetConcept.setBorder(GUI.buildBorder("Target Concept"));

		// TARGET CONCEPT - labels
		jPanelTargetConceptLabels.setLayout(new GridLayout(9, 1));

		jLabelTargetType = initJLabel("target type");
		jPanelTargetConceptLabels.add(jLabelTargetType);

		jLabelQualityMeasure = initJLabel("quality measure");
		jPanelTargetConceptLabels.add(jLabelQualityMeasure);

		jLabelQualityMeasureMinimum = initJLabel("measure minimum");
		jPanelTargetConceptLabels.add(jLabelQualityMeasureMinimum);

		jLabelTargetAttribute = initJLabel("<html><u>p</u>rimary target");
		jPanelTargetConceptLabels.add(jLabelTargetAttribute);

		// used for target value or secondary target
		jLabelMiscField = initJLabel("");
		jPanelTargetConceptLabels.add(jLabelMiscField);

		jLabelMultiRegressionTargets = initJLabel("secondary/tertiary targets");
		jPanelTargetConceptLabels.add(jLabelMultiRegressionTargets);
		// TODO MM for stable jar, disable
		jLabelMultiRegressionTargets.setVisible(false);

		jLabelMultiTargets = initJLabel("targets and settings");
		jPanelTargetConceptLabels.add(jLabelMultiTargets);

		jLabelTargetInfo = initJLabel("");;
		jPanelTargetConceptLabels.add(jLabelTargetInfo);
		jPanelTargetConcept.add(jPanelTargetConceptLabels);

		// TARGET CONCEPT - fields
		jPanelTargetConceptFields.setLayout(new GridLayout(9, 1));

		jComboBoxTargetType = GUI.buildComboBox(new Object[0], TARGET_TYPE_BOX, this);
		jPanelTargetConceptFields.add(jComboBoxTargetType);

		jComboBoxQualityMeasure = GUI.buildComboBox(new Object[0], QUALITY_MEASURE_BOX, this);
		jPanelTargetConceptFields.add(jComboBoxQualityMeasure);

		jTextFieldQualityMeasureMinimum = GUI.buildTextField("0");
		jPanelTargetConceptFields.add(jTextFieldQualityMeasureMinimum);

		jComboBoxTargetAttribute = GUI.buildComboBox(new Object[0], TARGET_ATTRIBUTE_BOX, this);
		jPanelTargetConceptFields.add(jComboBoxTargetAttribute);

		// used for target value or secondary target
		// note in cui setting this is often to small
		jComboBoxMiscField = GUI.buildComboBox(new Object[0], MISC_FIELD_BOX, this);
		jPanelTargetConceptFields.add(jComboBoxMiscField);

		jButtonMultiRegressionTargets = initButton(STD.SECONDARY_TERTIARY_TARGETS);
		jPanelTargetConceptFields.add(jButtonMultiRegressionTargets);
		// TODO MM for stable jar, disable
		jButtonMultiRegressionTargets.setVisible(false);

		jButtonMultiTargets = initButton(STD.TARGETS_AND_SETTINGS);
		jPanelTargetConceptFields.add(jButtonMultiTargets);

		jLabelTargetInfoText = initJLabel("");
		jPanelTargetConceptFields.add(jLabelTargetInfoText);

		jButtonBaseModel = initButton(STD.BASE_MODEL);
		jPanelTargetConceptFields.add(jButtonBaseModel);

		jPanelTargetConcept.add(jPanelTargetConceptFields);
		jPanelCenter.add(jPanelTargetConcept);

		// SEARCH CONDITIONS ===========================================
		jPanelSearchConditions = new JPanel();
		jPanelSearchConditionsLabels = new JPanel();
		jPanelSearchParameterFields = new JPanel();

		jPanelSearchConditions.setLayout(new BoxLayout(jPanelSearchConditions, BoxLayout.X_AXIS));
		jPanelSearchConditions.setBorder(GUI.buildBorder("Search Conditions"));

		// SEARCH CONDITIONS - labels
		jPanelSearchConditionsLabels.setLayout(new GridLayout(7, 1));

		jLabelSearchDepth = initJLabel("refinement depth");
		jPanelSearchConditionsLabels.add(jLabelSearchDepth);

		jLabelSearchCoverageMinimum = initJLabel("minimum coverage");
		jPanelSearchConditionsLabels.add(jLabelSearchCoverageMinimum);

		jLabelSearchCoverageMaximum = initJLabel("maximum coverage (fraction)");
		jPanelSearchConditionsLabels.add(jLabelSearchCoverageMaximum);

		jLabelSubgroupsMaximum = initJLabel("<html> maximum subgroups (0 = &#8734;)</html>)");
		jPanelSearchConditionsLabels.add(jLabelSubgroupsMaximum);

		jLabelSearchTimeMaximum = initJLabel("<html> maximum time (min) (0 = &#8734;)</html>)");
		jPanelSearchConditionsLabels.add(jLabelSearchTimeMaximum);

		jPanelSearchConditions.add(jPanelSearchConditionsLabels);

		// SEARCH CONDITIONS - fields
		jPanelSearchParameterFields.setLayout(new GridLayout(7, 1));

		jTextFieldSearchDepth = GUI.buildTextField("0");
		jPanelSearchParameterFields.add(jTextFieldSearchDepth);

		jTextFieldSearchCoverageMinimum = GUI.buildTextField("0");
		jPanelSearchParameterFields.add(jTextFieldSearchCoverageMinimum);

		jTextFieldSearchCoverageMaximum = GUI.buildTextField("0");
		jPanelSearchParameterFields.add(jTextFieldSearchCoverageMaximum);

		jTextFieldSubgroupsMaximum = GUI.buildTextField("0");
		jPanelSearchParameterFields.add(jTextFieldSubgroupsMaximum);

		jTextFieldSearchTimeMaximum = GUI.buildTextField("0");
		jPanelSearchParameterFields.add(jTextFieldSearchTimeMaximum);

		jPanelSearchConditions.add(jPanelSearchParameterFields);
		jPanelCenter.add(jPanelSearchConditions);

		// SEARCH STRATEGY =============================================
		jPanelSearchStrategy = new JPanel();
		jPanelSearchStrategyLabels = new JPanel();
		jPanelSearchStrategyFields = new JPanel();

		jPanelSearchStrategy.setLayout(new BoxLayout(jPanelSearchStrategy, BoxLayout.X_AXIS));
		jPanelSearchStrategy.setBorder(GUI.buildBorder("Search Strategy"));

		// SEACH STRATEGY - labels
		jPanelSearchStrategyLabels.setLayout(new GridLayout(7, 1));

		jLabelStrategyType = initJLabel("strategy type");
		jPanelSearchStrategyLabels.add(jLabelStrategyType);

		jLabelStrategyWidth = initJLabel("search width");
		jPanelSearchStrategyLabels.add(jLabelStrategyWidth);

		jLabelSetValuedNominals = initJLabel("set-valued nominals");
		jPanelSearchStrategyLabels.add(jLabelSetValuedNominals);

		jLabelNumericStrategy = initJLabel("numeric strategy");
		jPanelSearchStrategyLabels.add(jLabelNumericStrategy);

		jLabelNumericOperators = initJLabel("numeric operators");
		jPanelSearchStrategyLabels.add(jLabelNumericOperators);

		jLabelNumberOfBins = initJLabel("number of bins");
		jPanelSearchStrategyLabels.add(jLabelNumberOfBins);

		jLabelNumberOfThreads = initJLabel("threads (0 = all available)");
		jPanelSearchStrategyLabels.add(jLabelNumberOfThreads);

		jPanelSearchStrategy.add(jPanelSearchStrategyLabels);

		// SEARCH STRATEGY - fields
		jPanelSearchStrategyFields.setLayout(new GridLayout(7, 1));

		jComboBoxStrategyType = GUI.buildComboBox(new Object[0], SEARCH_TYPE_BOX, this);
		jPanelSearchStrategyFields.add(jComboBoxStrategyType);

		jTextFieldStrategyWidth = GUI.buildTextField("0");
		jPanelSearchStrategyFields.add(jTextFieldStrategyWidth);

		jCheckBoxSetValuedNominals = new JCheckBox();
		jPanelSearchStrategyFields.add(jCheckBoxSetValuedNominals);

		jComboBoxNumericStrategy = GUI.buildComboBox(new Object[0], NUMERIC_STRATEGY_BOX, this);
		jPanelSearchStrategyFields.add(jComboBoxNumericStrategy);

		jComboBoxNumericOperators = GUI.buildComboBox(new Object[0], NUMERIC_OPERATORS_BOX, this);
		jPanelSearchStrategyFields.add(jComboBoxNumericOperators);

		jTextFieldNumberOfBins = GUI.buildTextField("0");
		jPanelSearchStrategyFields.add(jTextFieldNumberOfBins);

		jTextFieldNumberOfThreads = GUI.buildTextField(Integer.toString(Runtime.getRuntime().availableProcessors()));
		jPanelSearchStrategyFields.add(jTextFieldNumberOfThreads);

		jPanelSearchStrategy.add(jPanelSearchStrategyFields);
		jPanelCenter.add(jPanelSearchStrategy);

		// MINING BUTTONS
		jPanelSouth.setFont(GUI.DEFAULT_TEXT_FONT);

		jPanelMineButtons = new JPanel();
		jPanelMineButtons.setMinimumSize(new Dimension(0, 40));

		jButtonSubgroupDiscovery = initButton(STD.SUBGROUP_DISCOVERY);
		jPanelMineButtons.add(jButtonSubgroupDiscovery);

		jButtonCrossValidate = initButton(STD.CROSS_VALIDATE);
		jPanelMineButtons.add(jButtonCrossValidate);

		jButtonComputeThreshold = initButton(STD.COMPUTE_THRESHOLD);
		jPanelMineButtons.add(jButtonComputeThreshold);

		jPanelSouth.add(jPanelMineButtons);

		getContentPane().add(jPanelSouth, BorderLayout.SOUTH);
		getContentPane().add(jPanelCenter, BorderLayout.CENTER);

		setFont(GUI.DEFAULT_TEXT_FONT);
		setJMenuBar(jMiningWindowMenuBar);
	}

	private void initStaticGuiComponents()
	{
		/*
		 * normal values only, INTERVALS will be added when needed
		 * this must done before TargetType initialisation
		 * as it determines whether INTERVALS will be added
		 */
		for (NumericStrategy n : NumericStrategy.getNormalValues())
			jComboBoxNumericStrategy.addItem(n.GUI_TEXT);

		// Add all implemented TargetTypes
		for (TargetType t : TargetType.values())
			if (TargetType.isImplemented(t))
				jComboBoxTargetType.addItem(t.GUI_TEXT);

		// Add all SearchStrategies
		for (SearchStrategy s : SearchStrategy.values())
			jComboBoxStrategyType.addItem(s.GUI_TEXT);

		// Add all Numeric Operators choices
		for (NumericOperatorSetting n : NumericOperatorSetting.getNormalValues())
			jComboBoxNumericOperators.addItem(n.GUI_TEXT);
	}

	public void initTitle()
	{
		setTitle("CORTANA: Subgroup Discovery Tool");
	}

	// only called when Table is present, so using itsTotalCount is safe
	private void initGuiComponents()
	{
		//dataset
		initGuiComponentsDataSet();

		// search conditions
		setSearchDepthMaximum("1");
		setSearchCoverageMinimum("2");
		setSearchCoverageMaximum("1.0");
		setSubgroupsMaximum("0");
		setSearchTimeMaximum("1.0");

		// search strategy
		setStrategyWidth("100");
		setNrBins("8");
	}

	private void initGuiComponentsFromFile()
	{
		jComboBoxTargetType.removeActionListener(this);
		jComboBoxQualityMeasure.removeActionListener(this);
		jComboBoxTargetAttribute.removeActionListener(this);
		jComboBoxMiscField.removeActionListener(this);
		jComboBoxStrategyType.removeActionListener(this);
		jComboBoxNumericStrategy.removeActionListener(this);
		jComboBoxNumericOperators.removeActionListener(this);

		// data set
		initGuiComponentsDataSet();

		// search strategy
		setSearchStrategyType(itsSearchParameters.getSearchStrategy().GUI_TEXT);
		setStrategyWidth(String.valueOf(itsSearchParameters.getSearchStrategyWidth()));
		setSetValuedNominals(String.valueOf(itsSearchParameters.getNominalSets()));
		setNumericStrategy(itsSearchParameters.getNumericStrategy().GUI_TEXT);
		setNumericOperators(itsSearchParameters.getNumericOperatorSetting().GUI_TEXT);
		setNrBins(String.valueOf(itsSearchParameters.getNrBins()));
		setNrThreads(String.valueOf(itsSearchParameters.getNrThreads()));

		// search conditions
		setSearchDepthMaximum(String.valueOf(itsSearchParameters.getSearchDepth()));
		setSearchCoverageMinimum(String.valueOf(itsSearchParameters.getMinimumCoverage()));
		setSearchCoverageMaximum(String.valueOf(itsSearchParameters.getMaximumCoverageFraction()));
		setSubgroupsMaximum(String.valueOf(itsSearchParameters.getMaximumSubgroups()));
		setSearchTimeMaximum(String.valueOf(itsSearchParameters.getMaximumTime()));

		// target concept
		TargetType aTargetType = itsTargetConcept.getTargetType();

		/*
		 * fields that may be overwritten
		 * we want to use jComboBoxTargetTypeActionPerformed as it sets
		 * up all Labels, Fields and ComboBox items correctly
		 * it can also correct some XML-errors
		 * afterwards we (re)set the settings to their original value
		 */
		String aPrimaryTarget = null;
		String aMiscField = null;
		String aQualityMeasure = null;
		float aQualityMeasureMinimum = Float.NaN;

		if (TargetType.hasTargetAttribute(aTargetType))
		{
			aPrimaryTarget = itsTargetConcept.getPrimaryTarget().getName();

			if (TargetType.hasTargetValue(aTargetType))
			{
				if (aTargetType == TargetType.SINGLE_NOMINAL)
					aMiscField = itsTargetConcept.getTargetValue();
				else if (aTargetType == TargetType.DOUBLE_REGRESSION ||
						aTargetType == TargetType.DOUBLE_CORRELATION)
					aMiscField = itsTargetConcept.getSecondaryTarget().getName();
			}
		}
		aQualityMeasure = itsSearchParameters.getQualityMeasure().GUI_TEXT;
		aQualityMeasureMinimum = itsSearchParameters.getQualityMeasureMinimum();

		// setTargetType causes havoc, it overwrites some other values
		setTargetTypeName(itsTargetConcept.getTargetType().GUI_TEXT);
		jComboBoxTargetTypeActionPerformed();
		// reset loaded values
		if (TargetType.hasTargetAttribute(aTargetType))
		{
			setTargetAttribute(aPrimaryTarget);
			itsTargetConcept.setPrimaryTarget(itsTable.getColumn(aPrimaryTarget));

			if (TargetType.hasTargetValue(aTargetType))
			{
				if (aTargetType == TargetType.SINGLE_NOMINAL)
				{
					setMiscFieldName(aMiscField);
					itsTargetConcept.setTargetValue(aMiscField);
				}
				else if (aTargetType == TargetType.DOUBLE_REGRESSION ||
						aTargetType == TargetType.DOUBLE_CORRELATION)
				{
					setMiscFieldName(aMiscField);
					itsTargetConcept.setSecondaryTarget(itsTable.getColumn(aMiscField));
				}
			}
		}
		setQualityMeasure(aQualityMeasure);
		itsSearchParameters.setQualityMeasure(QM.fromString(aQualityMeasure));
		setQualityMeasureMinimum(Float.toString(aQualityMeasureMinimum));
		itsSearchParameters.setQualityMeasureMinimum(aQualityMeasureMinimum);

		// TODO MM some TargetTypes may not be loaded properly, example:
		// setSecondaryTargets(); -> initialise from primaryTargetList

		jComboBoxTargetType.addActionListener(this);
		jComboBoxQualityMeasure.addActionListener(this);
		jComboBoxTargetAttribute.addActionListener(this);
		jComboBoxMiscField.addActionListener(this);
		jComboBoxStrategyType.addActionListener(this);
		jComboBoxNumericStrategy.addActionListener(this);
		jComboBoxNumericOperators.addActionListener(this);
	}

	private void initGuiComponentsDataSet()
	{
		if (itsTable != null)
		{
			jLabelTargetTableName.setText(itsTable.getName());
			jLabelNrExamplesNr.setText(String.valueOf(itsTable.getNrRows()));

			int[][] aCounts = itsTable.getTypeCounts();
			int[] aTotals = { itsTable.getNrColumns(), 0 };
			for (int[] ia : aCounts)
				aTotals[1] += ia[1];
			jLabelNrColumnsNr.setText(initGuiComponentsDataSetHelper(aTotals));
			jLabelNrNominalsNr.setText(initGuiComponentsDataSetHelper(aCounts[0]));
			jLabelNrNumericsNr.setText(initGuiComponentsDataSetHelper(aCounts[1]));
//			jLFieldNrOrdinals.setText(initGuiComponentsDataSetHelper(aCounts[2]));
			jLabelNrBinariesNr.setText(initGuiComponentsDataSetHelper(aCounts[3]));

			if (aCounts[0][0] != 0)
				setTargetTypeName(TargetType.SINGLE_NOMINAL.GUI_TEXT);
			else if (aCounts[1][0] != 0)
				setTargetTypeName(TargetType.SINGLE_NUMERIC.GUI_TEXT);
			else if (aCounts[2][0] != 0)
				setTargetTypeName(TargetType.SINGLE_ORDINAL.GUI_TEXT);
			else if (aCounts[3][0] != 0)
				setTargetTypeName(TargetType.MULTI_LABEL.GUI_TEXT);
			else
				throw new AssertionError();
		}
	}

	private String initGuiComponentsDataSetHelper(int[] theCounts)
	{
		int aCount = theCounts[0];
		String aNrEnabled = (aCount == 0 ? "" : " (" + theCounts[1] + " enabled)");
		String aSpacer = "          ";
		int i = 0;
		while ((aCount /= 10) > 0)
			i+=2;
		return String.format("%d%s%s", theCounts[0], aSpacer.substring(i), aNrEnabled);
	}

	private void enableTableDependentComponents(boolean theSetting)
	{
		AbstractButton[] aButtons = {	jMenuItemBrowse,
						jMenuItemExplore,
						jMenuItemMetaData,
						jMenuItemSubgroupDiscovery,
						jMenuItemCreateAutorunFile,
						jMenuItemAddToAutorunFile,
						jMenuItemLoadSampledSubgroups,
						jMenuItemAddCuiEnrichmentSource,
						jMenuItemAddGoEnrichmentSource,
						jMenuItemAddCustomEnrichmentSource,
						jButtonBrowse,
						jButtonExplore,
						jButtonMetaData,
						jButtonCrossValidate,
						jButtonSubgroupDiscovery,
						jButtonComputeThreshold,
						jButtonMultiTargets,
						jButtonMultiRegressionTargets};
		enableBaseModelButtonCheck();

		for (AbstractButton a : aButtons)
			a.setEnabled(theSetting);

		// do the same for combo boxes
		JComboBox[] aComboBoxArray = {	jComboBoxTargetType,
						jComboBoxStrategyType,
						jComboBoxNumericStrategy,
						jComboBoxNumericOperators };
		for (JComboBox c : aComboBoxArray)
			c.setEnabled(theSetting);
	}

	/*
	 * Removes all items from:
	 * jComboBoxTargetAttributes
	 * jComboBoxMiscField (TargetValues / 2nd Target)
	 * jListMultiTargets
	 * jListMultiRegressionTargets
	 *
	 * see jComboBoxTargetTypeActionPerformed
	 */
	private void initTargetAttributes()
	{
		// avoid firing ActionEvents before were done.
		jComboBoxTargetAttribute.removeActionListener(this);
		jComboBoxMiscField.removeActionListener(this);

		final TargetType aTargetType = itsTargetConcept.getTargetType();

		removeAllTargetAttributeItems();
		removeAllMiscFieldItems();
		removeAllMultiTargetsItems();
		removeAllMultiRegressionTargetsItems();

		boolean fillMiscField = (aTargetType == TargetType.DOUBLE_REGRESSION) || (aTargetType == TargetType.DOUBLE_CORRELATION);
		// primary target and (optional) MiscField
		for (Column c : itsTable.getColumns())
		{
			AttributeType anAttributeType = c.getType();
			if ((aTargetType == TargetType.SINGLE_NOMINAL && anAttributeType == AttributeType.NOMINAL) ||
				(aTargetType == TargetType.SINGLE_NOMINAL && anAttributeType == AttributeType.BINARY) ||
				(aTargetType == TargetType.SINGLE_NUMERIC && anAttributeType == AttributeType.NUMERIC) ||
				(aTargetType == TargetType.SINGLE_ORDINAL && anAttributeType == AttributeType.NUMERIC) ||
				(aTargetType == TargetType.DOUBLE_REGRESSION && anAttributeType == AttributeType.NUMERIC) ||
				(aTargetType == TargetType.DOUBLE_CORRELATION && anAttributeType == AttributeType.NUMERIC) ||
				(aTargetType == TargetType.MULTI_LABEL && anAttributeType == AttributeType.NUMERIC) ||
				(aTargetType == TargetType.MULTI_BINARY_CLASSIFICATION && anAttributeType == AttributeType.BINARY) ||
				(aTargetType == TargetType.MULTI_BINARY_CLASSIFICATION && anAttributeType == AttributeType.NOMINAL))
			{
				addTargetAttributeItem(c.getName());

				/*
				 * might be considered TargetValue / MiscField
				 * functionality, but the entries should not be
				 * updated after creation
				 * see initTargetValues()
				 */
				if (fillMiscField)
					addMiscFieldItem(c.getName());
			}
		}

		/*
		 * NOTE
		 * setMultiTargets() is in initTargetAttributeItems()
		 * MultiTargets do not change on
		 * jComboBoxTargetAttributeActionPerformed
		 *
		 * this is contrary to DOUBLE_REGRESSION + COOKS_DISTANCE
		 * computeMultiRegressionTargets(), they change whenever the
		 * PrimaryTarget is changed, so they need to be updated on every
		 * jComboBoxTargetAttributeActionPerformed
		 */
		if (aTargetType == TargetType.MULTI_LABEL)
			setMultiTargets();

		// Re-enable ActionEvents from these two ComboBoxes.
		jComboBoxTargetAttribute.addActionListener(this);
		jComboBoxMiscField.addActionListener(this);
	}

	/*
	 * only called on TargetAttribute change through:
	 * jComboBoxTargetAttributeActionPerformed
	 *
	 * technically the List need not be completely cleared
	 * because just the primary target is changed, a simple substitution
	 * would suffice
	 * it seems unlikely that current code would be a performance drawback
	 *
	 * see jComboBoxTargetTypeActionPerformed
	 */
	private void setMultiRegressionTargets()
	{
		// assumes COOKS_DISTANCE is only valid for DOUBLE_REGRESSION
		if (!QM.COOKS_DISTANCE.GUI_TEXT.equals(getQualityMeasureName()))
			return;

		removeAllMultiRegressionTargetsItems();

		Column aPrimaryTarget = itsTargetConcept.getPrimaryTarget();
		List<Column> aList = new ArrayList<Column>();
		for (Column c: itsTable.getColumns())
		{
			if (c.getType() == AttributeType.NUMERIC && c.getIsEnabled() && (c != aPrimaryTarget))
			{
				addMultiRegressionTargetsItem(c.getName());
				aList.add(c);
			}
		}

		itsTargetConcept.setMultiRegressionTargets(aList);
		jListMultiRegressionTargets.setSelectionInterval(0, aList.size() - 1);
	}

	/*
	 * expensive recreation of up-to-date list, this always reflects the
	 * latest AttributeType / isEnabled changes for a Columns
	 * for more efficient code Table / MetaDataWindow need updates to ensure
	 * changes are immediately pushed to underlying Table (which holds the
	 * secondaryTargets Column(s))
	 *
	 * see jComboBoxTargetTypeActionPerformed
	 */
	private void setMultiTargets()
	{
		removeAllMultiTargetsItems();

		List<Column> aList = new ArrayList<Column>();
		for (Column c: itsTable.getColumns())
		{
			if (c.getType() == AttributeType.BINARY && c.getIsEnabled())
			{
				addMultiTargetsItem(c.getName());
				aList.add(c);
			}
		}
		itsTargetConcept.setMultiTargets(aList);
		jListMultiTargets.setSelectionInterval(0, aList.size() - 1);
	}

	/*
	 * MiscBox is for TargetAttributeValue or 2nd Target
	 * should only be called for SINGLE_NOMINAL/ MULTI_BINARY_CLASSIFICATION
	 * for DOUBLE_REGRESSION/ DOUBLE_CORRELATION the values in the
	 * jComboBoxMiscField do not need to be updated after a change of
	 * PrimaryTarget (jComboBoxTargetAttributeActionPerformed)
	 *
 	 * see jComboBoxTargetTypeActionPerformed
	 */
	private void initTargetValues()
	{
		// disable MiscField ActionListener while modifying
		jComboBoxMiscField.removeActionListener(this);

		removeAllMiscFieldItems();

		if (jComboBoxTargetAttribute.getItemCount() > 0)
		{
			TreeSet<String> aValues = itsTable.getColumn(getTargetAttributeName()).getDomain();
			for (String aValue : aValues)
				addMiscFieldItem(aValue);
		}

		// re-enable MiscField ActionListener
		jComboBoxMiscField.addActionListener(this);
	}

	// see jComboBoxTargetTypeActionPerformed
	private void initTargetInfo()
	{
		TargetType aTargetType = itsTargetConcept.getTargetType();
		switch (aTargetType)
		{
			case SINGLE_NOMINAL :
			{
				String aTarget = getTargetAttributeName();
				if (aTarget == null) //initTargetInfo might be called before item is actually selected
					return;
				itsPositiveCount =
					itsTable.getColumn(aTarget).countValues(getMiscFieldName());
				float aPercentage = (itsPositiveCount * 100) / (float) itsTable.getNrRows();
				NumberFormat aFormatter = NumberFormat.getNumberInstance();
				aFormatter.setMaximumFractionDigits(1);
				jLabelTargetInfo.setText(" # positive");
				jLabelTargetInfoText.setText(itsPositiveCount + " (" + aFormatter.format(aPercentage) + " %)");
				break;
			}
			case SINGLE_NUMERIC :
			{
				String aTarget = getTargetAttributeName();
				if (aTarget != null) //initTargetInfo might be called before item is actually selected
				{
					itsTargetAverage = itsTable.getColumn(aTarget).getAverage();
					jLabelTargetInfo.setText(" average");
					jLabelTargetInfoText.setText(String.valueOf(itsTargetAverage));
				}
				break;
			}
			case SINGLE_ORDINAL :
			{
				throw new AssertionError(aTargetType);
			}
			case DOUBLE_REGRESSION :
			{
				Column aPrimaryColumn = itsTargetConcept.getPrimaryTarget();
				Column aSecondaryColumn = itsTargetConcept.getSecondaryTarget();

				if (QM.COOKS_DISTANCE.GUI_TEXT.equals(getQualityMeasureName()))
				{
					jLabelTargetInfo.setText(" # multi-targets");
					jLabelTargetInfoText.setText(Integer.toString(jListMultiRegressionTargets.getModel().getSize()));
					break;
				}

				// not COOKS_DISTANCE, assume LINEAR_REGRESSION

				// may not be exactly 1.0 (rounding errors)
				if (aPrimaryColumn == aSecondaryColumn)
				{
					jLabelTargetInfo.setText(" regression");
					jLabelTargetInfoText.setText("s = 0.0 + 1.0 * p");
					break;
				}

				RegressionMeasure aRM =
					new RegressionMeasure(QM.LINEAR_REGRESSION, aPrimaryColumn, aSecondaryColumn);
				NumberFormat aFormatter = NumberFormat.getNumberInstance();
				aFormatter.setMaximumFractionDigits(2);
				jLabelTargetInfo.setText(" regression");
				jLabelTargetInfoText.setText(String.format("s = %s + %s * p",
										aFormatter.format(aRM.getIntercept()),
										aFormatter.format(aRM.getSlope())));
				break;
			}
			case DOUBLE_CORRELATION :
			{
				Column aPrimaryColumn = itsTargetConcept.getPrimaryTarget();
				Column aSecondaryColumn = itsTargetConcept.getSecondaryTarget();

				CorrelationMeasure aCM =
					new CorrelationMeasure(QM.CORRELATION_R, aPrimaryColumn, aSecondaryColumn);
				jLabelTargetInfo.setText(" correlation");
				jLabelTargetInfoText.setText(Double.toString(aCM.getEvaluationMeasureValue()));
				break;
			}
			case MULTI_LABEL :
			{
				jLabelTargetInfo.setText(" # binary targets");
				jLabelTargetInfoText.setText(String.valueOf(itsTargetConcept.getMultiTargets().size()));
				break;
			}
			case MULTI_BINARY_CLASSIFICATION :
			{
				jLabelTargetInfo.setText(" target info");
				jLabelTargetInfoText.setText("none");
				break;
			}
		}
	}

	// see jComboBoxTargetTypeActionPerformed
	private void initQualityMeasure()
	{
		jComboBoxQualityMeasure.removeActionListener(this);

		removeAllQualityMeasureItems();
		for (QM qm : QM.getQualityMeasures(itsTargetConcept.getTargetType()))
			addQualityMeasureItem(qm.GUI_TEXT);
		initEvaluationMinimum();

		jComboBoxQualityMeasure.addActionListener(this);
	}

	// see jComboBoxTargetTypeActionPerformed
	private void initEvaluationMinimum()
	{
		final String aName = getQualityMeasureName();
		if (aName == null)
			return;

		if (QM.AVERAGE.GUI_TEXT.equals(aName))
			setQualityMeasureMinimum(Float.toString(itsTargetAverage));
		else if (QM.INVERSE_AVERAGE.GUI_TEXT.equals(aName))
			setQualityMeasureMinimum(Float.toString(-itsTargetAverage));
		else
			setQualityMeasureMinimum(QM.fromString(getQualityMeasureName()).MEASURE_DEFAULT);
	}

	// see jComboBoxTargetTypeActionPerformed
	private void initNumericStrategy()
	{
		jComboBoxNumericStrategy.removeActionListener(this);

		// if SINGLE_NOMINAL add INTERVALS, else remove it
		if (itsTargetConcept.getTargetType() == TargetType.SINGLE_NOMINAL)
			jComboBoxNumericStrategy.addItem(NumericStrategy.NUMERIC_INTERVALS.GUI_TEXT);
		else
		{
			// assumes first normal value is not INTERVALS
			if (NumericStrategy.NUMERIC_INTERVALS.GUI_TEXT == jComboBoxNumericStrategy.getSelectedItem())
				jComboBoxNumericStrategy.setSelectedIndex(0);

			jComboBoxNumericStrategy.removeItem(NumericStrategy.NUMERIC_INTERVALS.GUI_TEXT);
		}

		jComboBoxNumericStrategy.addActionListener(this);
	}

	// see jComboBoxTargetTypeActionPerformed
	private void enableBaseModelButtonCheck()
	{
		TargetType aType = itsTargetConcept.getTargetType();
		boolean isEnabled = TargetType.hasBaseModel(aType);
		if (aType == TargetType.MULTI_LABEL && jListMultiTargets.getSelectedIndices().length == 0)
			isEnabled = false;
		jButtonBaseModel.setEnabled(isEnabled);
	}

	// used if Table already exists, but is changed
	// TODO MM take parameter of what to update
	public void update()
	{
		// hack on hack, prevent resetting of primary target/measure minimum
		String aPrimaryTarget = getTargetAttributeName();
		String aMeasureMinimum = jTextFieldQualityMeasureMinimum.getText();

		initGuiComponentsDataSet();
		jComboBoxTargetTypeActionPerformed();	// update hack
		setTargetAttribute(aPrimaryTarget);
		setQualityMeasureMinimum(aMeasureMinimum);
	}

	/* MENU ITEMS */
	private void jMenuItemOpenFileActionPerformed()
	{
		FileHandler aFileHandler =  new FileHandler(Action.OPEN_FILE);

		Table aTable = aFileHandler.getTable();
		SearchParameters aSearchParameters = aFileHandler.getSearchParameters();

		if (aTable != null)
		{
			// clear these JLists
			removeAllMultiTargetsItems();
			removeAllMultiRegressionTargetsItems();

			itsTable = aTable;
			enableTableDependentComponents(true);

			// loaded from regular file
			if (aSearchParameters == null)
				initGuiComponents();
			// loaded from XML
			else
			{
				itsSearchParameters = aSearchParameters;
				// should not happen
				if (itsSearchParameters.getTargetConcept() == null)
					itsTargetConcept = new TargetConcept();
				else
					itsTargetConcept = itsSearchParameters.getTargetConcept();
				// NOTE also initiates all fields and labels
				initGuiComponentsFromFile();
			}

			jMenuItemRemoveEnrichmentSource.setEnabled(false);
		}
	}

	private void jMenuItemAutoRunFileActionPerformed(final AutoRun theFileOption)
	{
		setupSearchParameters();
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				new XMLAutoRun(itsSearchParameters, itsTable, theFileOption);
			}
		});
	}

	private void jMenuItemLoadSampledSubgroupsActionPerformed()
	{
		JOptionPane.showMessageDialog(null, "load file from Fraunhofer", "Load Sampled Subgroups", JOptionPane.INFORMATION_MESSAGE);

		FileHandler aFileHandler =  new FileHandler(Action.OPEN_FILE);
		File aFile = aFileHandler.getFile();
		if (aFile == null)
			return;
		else
		{
			LoaderFraunhofer aLoader = new LoaderFraunhofer(aFile);
		}
	}

	private void jMenuItemExitActionPerformed()
	{
		Log.logCommandLine("exit");
		dispose();
		System.exit(0);
	}

	//cannot be run from the Event Dispatching Thread
	private void jMenuItemAddEnrichmentSourceActionPerformed(final EnrichmentType theType)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				new FileHandler(itsTable, theType);
				update();
				final JList aList = itsTable.getDomainList();
				jMenuItemRemoveEnrichmentSource
					.setEnabled(aList != null && aList.getComponentCount() > 0);
			}
		});
	}

	private void jMenuItemRemoveEnrichmentSourceActionPerformed()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JList aDomainList = itsTable.getDomainList();
				new RemoveDomainWindow(aDomainList);
				int aComponentCount = aDomainList.getComponentCount();

				if ((aDomainList != null) && (aComponentCount > 0))
				{
					for (int i = 0, j = aComponentCount; i < j; ++i)
						if (aDomainList.isSelectedIndex(i))
							itsTable.removeDomain(i);

					update();
					jMenuItemRemoveEnrichmentSource
						.setEnabled(aDomainList == null || aComponentCount == 0);
				}
			}
		});
	}

	// use "Cortana: Subgroup Discovery Tool r????" to indicate revision
	private void jMenuItemAboutCortanaActionPerformed()
	{
		JOptionPane.showMessageDialog(this,
						"Cortana: Subgroup Discovery Tool, r1782",
						"About Cortana",
						JOptionPane.INFORMATION_MESSAGE);
	}

	/* DATASET BUTTONS */
	//not on Event Dispatching Thread, may take a long time to load
	private void browseActionPerformed()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				new BrowseWindow(itsTable, null);
			}
		});
	}

	//not on Event Dispatching Thread, may take a long time to load
	private void exploreActionPerformed()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				new HistogramWindow(itsTable);
			}
		});
	}

	//not on Event Dispatching Thread, may take a long time to load
	private void metaDataActionPerformed()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				new MetaDataWindow(masterWindow, itsTable);
			}
		});
	}

	/*
	 * This is the Top-level ComboBox that can be changed.
	 * From here changes find their ways down to other ComboBoxes, Labels
	 * and Fields in the following order:
	 *
	 * jComboBoxTargetTypeActionPerformed() calls in order:
	 *
	 * initQualityMeasure() -> initEvaluationMinimum()
	 * initTargetAttributes() -> put Column Names in TargetAttributeComboBox
	 * misc_field.setVisible()			-> based on TargetType
	 * set-valued_nominals.setEnabled()		-> based on TargetType
	 * multi-targets_label.setEnabled()		-> based on TargetType
	 * baseModelCheck()				-> based on TargetType
	 * TargetAttributeLabel/ComboBox.setVisible()	-> based on TargetType
	 * initNumericStrategy()			-> based on TargetType
	 * jComboBoxTargetAttributeActionPerformed()
	 *
	 *
	 * jComboBoxTargetAttributeActionPerformed() calls in order:
	 *
	 * itsTargetConcept.setPrimaryTarget()
	 * initTargetValues (for TargetTypes that have TargetValues)
	 * setMultiTargets() if needed
	 * jComboBoxMiscFieldActionPerformed()
	 *
	 *
	 * jComboBoxMiscFieldActionPerformed() calls in order:
	 *
	 * setTargetValue() / setSecondaryTarget()	-> based on TargetType
	 * initTargetInfo()
	 *
	 * initTargetInfo() sets the appropriate texts for jLabelTargetInfo and
	 * jLabelTargetInfoText				-> based on TargetType
	 *
	 * After this all information should be up to date.
	 */
	private void jComboBoxTargetTypeActionPerformed()
	{
		if (itsTable == null)
			return;

		itsTargetConcept.setTargetType(getTargetTypeName());
		itsSearchParameters.setTargetConcept(itsTargetConcept);

		initQualityMeasure();
		initTargetAttributes();

		TargetType aTargetType = itsTargetConcept.getTargetType();
		// has MiscField?
		boolean hasMiscField = TargetType.hasMiscField(aTargetType);
		jComboBoxMiscField.setVisible(hasMiscField);
		jLabelMiscField.setVisible(hasMiscField);
		if (aTargetType == TargetType.SINGLE_NOMINAL)
			jLabelMiscField.setText("target value");
		else if (aTargetType == TargetType.DOUBLE_REGRESSION || aTargetType == TargetType.DOUBLE_CORRELATION)
			jLabelMiscField.setText("<html><u>s</u>econdary target");
		else
			jLabelMiscField.setText("");

		// only valid for nominal targets
		jCheckBoxSetValuedNominals.setEnabled(aTargetType == TargetType.SINGLE_NOMINAL);

/*
 * TODO MM for stable jar, disable, was added in revision 848
 * when COOKS_DISTANCE is re-enabled, there will be 2 DOUBLE_REGRESSION QMs, so
 * this part should move to jComboBoxQualityMeasreActionPerformed()
 */
/*
		boolean hasMultiRegressionTargets = TargetType.hasMultiRegressionTargets(aTargetType);
		jLabelMultiRegressionTargets.setVisible(hasMultiRegressionTargets);
		jButtonMultiRegressionTargets.setVisible(hasMultiRegressionTargets);
		// disable if not enough numeric attributes TODO should be itsTable.field
		// jListMultiRegressionTargets is populated through initTargetAttributeItems above
		jButtonMultiRegressionTargets.setEnabled(jListMultiRegressionTargets.getSelectedIndices().length > 1);
*/

		// has secondary targets (JList)?
		boolean hasMultiTargets = TargetType.hasMultiTargets(aTargetType);
		jLabelMultiTargets.setVisible(hasMultiTargets);
		jButtonMultiTargets.setVisible(hasMultiTargets);
		// disable if no binary attributes
		// initTargetAttributeItems above populates jListMultiTargets
		jButtonMultiTargets.setEnabled(jListMultiTargets.getSelectedIndices().length != 0);

		// has base model?
		enableBaseModelButtonCheck();

		// has target attribute?
		boolean hasTargetAttribute = TargetType.hasTargetAttribute(aTargetType);
		jLabelTargetAttribute.setVisible(hasTargetAttribute);
		jComboBoxTargetAttribute.setVisible(hasTargetAttribute);

		initNumericStrategy();

		jComboBoxTargetAttributeActionPerformed();
	}

	// see jComboBoxTargetTypeActionPerformed
	private void jComboBoxQualityMeasureActionPerformed()
	{
		initEvaluationMinimum();
		itsSearchParameters.setQualityMeasureMinimum(getQualityMeasureMinimum());

		// this ALWAYS resets alpha if switching TO EDIT_DISTANCE
		// remove upon discretion
		if (QM.EDIT_DISTANCE.GUI_TEXT.equals(getQualityMeasureName()))
			itsSearchParameters.setAlpha(SearchParameters.ALPHA_EDIT_DISTANCE);
	}

	// see jComboBoxTargetTypeActionPerformed
	private void jComboBoxTargetAttributeActionPerformed()
	{
		itsTargetConcept.setPrimaryTarget(itsTable.getColumn(getTargetAttributeName()));
		itsSearchParameters.setTargetConcept(itsTargetConcept); // FIXME MM not needed to reset it?

		/*
		 * for most TargetTypes, populates / clears jComboBoxMiscField
		 *
		 * for DOUBLE_REGRESSION / DOUBLE_CORRELATION the MiscField
		 * items do not need to be updated on a
		 * jComboBoxTargetAttributeActionPerformed
		 * see initTargetAttributeItems() and initTargetValueItems()
		 */
		TargetType aTargetType = itsTargetConcept.getTargetType();
		if (aTargetType != TargetType.DOUBLE_REGRESSION &&
				aTargetType != TargetType.DOUBLE_CORRELATION)
			initTargetValues();

		/*
		 * NOTE
		 * setMultiTargets() is in initTargetAttributeItems()
		 * MultiTargets do not change on
		 * jComboBoxTargetAttributeActionPerformed
		 *
		 * this is contrary to DOUBLE_REGRESSION + COOKS_DISTANCE
		 * computeMultiRegressionTargets(), they change whenever the
		 * PrimaryTarget is changed, so they need to be updated on every
		 * jComboBoxTargetAttributeActionPerformed
		 */
		setMultiRegressionTargets();

		jComboBoxMiscFieldActionPerformed();
	}

	// see jComboBoxTargetTypeActionPerformed
	private void jComboBoxMiscFieldActionPerformed()
	{
		switch (itsTargetConcept.getTargetType())
		{
			case SINGLE_NOMINAL :
			{
				itsTargetConcept.setTargetValue(getMiscFieldName());
				break;
			}
			case DOUBLE_REGRESSION :
			{
				itsTargetConcept.setSecondaryTarget(itsTable.getColumn(getMiscFieldName()));
				break;
			}
			case DOUBLE_CORRELATION :
			{
				itsTargetConcept.setSecondaryTarget(itsTable.getColumn(getMiscFieldName()));
				break;
			}
			case MULTI_BINARY_CLASSIFICATION :
			{
				itsTargetConcept.setTargetValue(getMiscFieldName());
				break;
			}
			default : break;
		}
		itsSearchParameters.setTargetConcept(itsTargetConcept);

		initTargetInfo();
	}

	// see jComboBoxTargetTypeActionPerformed
	private void jComboBoxSearchStrategyTypeActionPerformed()
	{
		String aName = getSearchStrategyName();
		if (aName != null)
		{
			itsSearchParameters.setSearchStrategy(aName);
			jTextFieldStrategyWidth.setEnabled(!SearchStrategy.BEST_FIRST.GUI_TEXT.equalsIgnoreCase(aName));
		}
	}

	// see jComboBoxTargetTypeActionPerformed
	private void jComboBoxSearchStrategyNumericActionPerformed()
	{
		String aName = getNumericStrategy();
		if (aName != null)
		{
			itsSearchParameters.setNumericStrategy(aName);
			boolean aBin = (itsSearchParameters.getNumericStrategy() == NumericStrategy.NUMERIC_BINS);
			jTextFieldNumberOfBins.setEnabled(aBin); //disable nr bins?
			boolean anIntervals = (itsSearchParameters.getNumericStrategy() == NumericStrategy.NUMERIC_INTERVALS);
			jComboBoxNumericOperators.setEnabled(!anIntervals); //disable numeric operators?
		}
	}

	// see jComboBoxTargetTypeActionPerformed
	private void jComboBoxNumericOperatorsActionPerformed()
	{
		String aName = getNumericOperators();
		if (aName != null)
			itsSearchParameters.setNumericOperators(aName);
	}

	private void jButtonMultiRegressionTargetsActionPerformed()
	{
		itsTable.getColumn(getTargetAttributeName()).makePrimaryTarget();
		new MultiRegressionTargetsWindow(jListMultiRegressionTargets, itsSearchParameters, itsTable, this);
	}

	private void jButtonMultiTargetsActionPerformed()
	{
		// is modal, blocks all input to other windows until closed
		new MultiTargetsWindow(jListMultiTargets, itsSearchParameters);

		Object[] aSelection = jListMultiTargets.getSelectedValues();
		int aNrBinary = aSelection.length;
		List<Column> aList = new ArrayList<Column>(aNrBinary);

		// aSelection is in order and names always occur in itsColumns
		for (int i = 0, j = 0; i < aNrBinary; ++j)
		{
			if (aSelection[i].equals(itsTable.getColumn(j).getName()))
			{
				aList.add(itsTable.getColumn(j));
				++i;
			}
		}
		itsTargetConcept.setMultiTargets(aList);
		// relies on disabling "Select Targets" if nrBinaries == 0
		initTargetInfo();
	}

	private void jButtonBaseModelActionPerformed()
	{
		setupSearchParameters();

		TargetType aTargetType = itsTargetConcept.getTargetType();
		switch (aTargetType)
		{
			case SINGLE_NOMINAL :
			{
				throw new AssertionError(aTargetType);
			}
			case SINGLE_NUMERIC :
			{
				Column aTarget = itsTargetConcept.getPrimaryTarget();
				ProbabilityDensityFunction aPDF = new ProbabilityDensityFunction(aTarget);
				aPDF.smooth();
				new ModelWindow(aTarget, aPDF, null, itsTable.getName());
				break;
			}
			case SINGLE_ORDINAL :
			{
				throw new AssertionError(aTargetType);
			}
			case DOUBLE_REGRESSION :
			{
				Column aPrimaryColumn = itsTargetConcept.getPrimaryTarget();
				Column aSecondaryColumn = itsTargetConcept.getSecondaryTarget();

				RegressionMeasure anRM =
					new RegressionMeasure(itsSearchParameters.getQualityMeasure(), aPrimaryColumn, aSecondaryColumn);

				new ModelWindow(aPrimaryColumn, aSecondaryColumn, anRM, null); //trendline, no subset
				break;
			}
			case DOUBLE_CORRELATION :
			{
				new ModelWindow(itsTargetConcept.getPrimaryTarget(), itsTargetConcept.getSecondaryTarget(), null, null); //no trendline, no subset
				break;
			}
			case MULTI_LABEL :
			{
				List<Column> aList = itsTargetConcept.getMultiTargets();

				// compute base model
				setBusy(true);
				Bayesian aBayesian =
					new Bayesian(new BinaryTable(itsTable, aList), aList);
				aBayesian.climb();
				DAG aBaseDAG = aBayesian.getDAG();
				aBaseDAG.print();
				setBusy(false);

				new ModelWindow(aBaseDAG, 1200, 900);
				break;
			}
			case MULTI_BINARY_CLASSIFICATION :
			{
				throw new AssertionError(aTargetType);
			}
			default :
			{
				throw new AssertionError(aTargetType);
			}
		}
	}

	/* MINING BUTTONS */
	private void subgroupDiscoveryActionPerformed()
	{
		setBusy(true);
		runSubgroupDiscovery(itsTable, 0, null);
		setBusy(false);
		initTitle(); // reset the window's title
	}

	private void runSubgroupDiscovery(Table theTable, int theFold, BitSet theBitSet)
	{
		setupSearchParameters();
		Process.runSubgroupDiscovery(theTable, theFold, theBitSet, itsSearchParameters, true, getNrThreads(), this);
	}

	private void jButtonComputeThresholdActionPerformed()
	{
		final TargetType aTargetType = itsTargetConcept.getTargetType();
		String[] aSetup = new RandomQualitiesWindow(aTargetType).getSettings();
		if (!RandomQualitiesWindow.isValidRandomQualitiesSetup(aSetup))
			return;

		// same as setup for runSubgroupDiscovery?
		setupSearchParameters();

		setBusy(true);
		QualityMeasure aQualityMeasure;
		switch (aTargetType)
		{
			case SINGLE_NOMINAL :
			{
				aQualityMeasure = new QualityMeasure(itsSearchParameters.getQualityMeasure(), itsTable.getNrRows(), itsPositiveCount);
				break;
			}
			case SINGLE_NUMERIC :
			{
				Column aTarget = itsTable.getColumn(getTargetAttributeName());
				int aNrRows = aTarget.size();

				// get Column sum and sum of squared deviations
				BitSet b = new BitSet(aNrRows);
				b.set(0, aNrRows);
				float[] aStats = aTarget.getStatistics(b, false);

				// get smoothed PDF with default number of bins
				ProbabilityDensityFunction aPDF = new ProbabilityDensityFunction(aTarget);
				aPDF.smooth();

				aQualityMeasure = new QualityMeasure(itsSearchParameters.getQualityMeasure(), aNrRows, aStats[0], aStats[1], aPDF);
				break;
			}
			case DOUBLE_REGRESSION :
			{
				aQualityMeasure = null;
				break;
			}
			case DOUBLE_CORRELATION :
			{
				aQualityMeasure = null;
				break;
			}
			case MULTI_LABEL :
			{
				// base model
				BinaryTable aBaseTable = new BinaryTable(itsTable, itsTargetConcept.getMultiTargets());
				Bayesian aBayesian = new Bayesian(aBaseTable);
				aBayesian.climb();
				aQualityMeasure = new QualityMeasure(itsSearchParameters, aBayesian.getDAG(), itsTable.getNrRows());
				break;
			}
			default :
			{
				Log.logCommandLine("Unable to compute random qualities for " + itsTargetConcept.getTargetType().GUI_TEXT);
				return;
			}
		}

		Validation aValidation = new Validation(itsSearchParameters, itsTable, aQualityMeasure);
		double[] aQualities = aValidation.getQualities(aSetup);
		if (aQualities == null)
			return;

		NormalDistribution aDistro = new NormalDistribution(aQualities);
		setBusy(false);

		int aMethod = JOptionPane.showOptionDialog(null,
			"The following quality measure thresholds were computed:\n" +
			"1% significance level: " + aDistro.getOnePercentSignificance() + "\n" +
			"5% significance level: " + aDistro.getFivePercentSignificance() + "\n" +
			"10% significance level: " + aDistro.getTenPercentSignificance() + "\n" +
			"Would you like to keep one of these thresholds as search constraint?",
			"Keep quality measure threshold?",
			JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
			new String[] {"1% significance", "5% significance", "10% significance", "Ignore statistics"},
			"1% significance");
		switch (aMethod)
		{
			case 0:
			{
				setQualityMeasureMinimum(Float.toString(aDistro.getOnePercentSignificance()));
				break;
			}
			case 1:
			{
				setQualityMeasureMinimum(Float.toString(aDistro.getFivePercentSignificance()));
				break;
			}
			case 2:
			{
				setQualityMeasureMinimum(Float.toString(aDistro.getTenPercentSignificance()));
				break;
			}
			case 3:
			{
				break; //discard statistics
			}
			default : return; // user closed dialog, do not print
		}

		// may take long, do this after window is shown
		Arrays.sort(aQualities);
		for (double aQuality : aQualities)
			Log.logCommandLine("" + aDistro.zTransform(aQuality));
		Log.logCommandLine("mu: " + aDistro.getMu());
		Log.logCommandLine("sigma: " + aDistro.getSigma());
	}

	private void jButtonCrossValidateActionPerformed()
	{
		int aStore = JOptionPane.showConfirmDialog(null,
								"Would you like to store binary tables for each fold in a file?",
								"Store results",
								JOptionPane.YES_NO_OPTION);
		if (aStore == JOptionPane.CLOSED_OPTION)
			return;;

		setBusy(true);
		long itsTimeStamp = System.currentTimeMillis();

		int aK = 10; //TODO set k from GUI
		CrossValidation aCV = new CrossValidation(itsTable.getNrRows(), aK);

		BufferedWriter aWriter = null;
		String aFileName = itsTable.getName() + "_folds_" + itsTimeStamp +".txt";

		if (aStore == 0)
			try
			{
				aWriter = new BufferedWriter(new FileWriter(aFileName));
			}
			catch (IOException e)
			{
				Log.logCommandLine("Error on file: " + aFileName);
			}

		for (int i=0; i<aK; i++)
		{
			BitSet aSet = aCV.getSet(i, true);
			Table aTable = itsTable.select(aSet);
			Log.logCommandLine("size of fold " + i + ": " + aTable.getNrRows());
			setupSearchParameters();
			itsTargetConcept.updateToNewTable(aTable); // make it point to the temporary table.
			SubgroupDiscovery aResult =
				nl.liacs.subdisc.Process.runSubgroupDiscovery(aTable, (i+1), aSet, itsSearchParameters, true, getNrThreads(), null);

			if (aStore == 0)
			{
				try
				{
					aWriter.write("Fold " + (i+1) + ":\n" + aSet.toString() + "\n");
				}
				catch (IOException e)
				{
					Log.logCommandLine("File writer error: " + e.getMessage());
				}
				aResult.getResult().saveExtent(aWriter, itsTable, aSet, itsTargetConcept);
			}
		}
		itsTargetConcept.updateToNewTable(itsTable); //point it back to the original again
		if (aStore == 0)
			try
			{
				aWriter.close();
			}
			catch (IOException e)
			{
				Log.logCommandLine("File writer error: " + e.getMessage());
			}
		setBusy(false);
	}

	private void setupSearchParameters()
	{
		/*
		 * TARGET CONCEPT
		 * some cleaning is done to create proper AutoRun-XMLs
		 */
		TargetType aType = itsTargetConcept.getTargetType();

		if (TargetType.hasTargetAttribute(aType))
			itsTargetConcept.setPrimaryTarget(itsTable.getColumn(getTargetAttributeName()));
		else
			itsTargetConcept.setPrimaryTarget(null);

		if (aType == TargetType.SINGLE_NOMINAL)
			itsTargetConcept.setTargetValue(getMiscFieldName());
		else
			itsTargetConcept.setTargetValue(null);

		if (TargetType.hasSecondaryTarget(aType))
			itsTargetConcept.setSecondaryTarget(itsTable.getColumn(getMiscFieldName()));
		else
			itsTargetConcept.setSecondaryTarget(null);

		// are already set when needed, remove possible old values
		if (!TargetType.hasMultiTargets(aType))
			itsTargetConcept.setMultiTargets(new ArrayList<Column>(0));
		// assumes COOKS_DISTANCE is only valid for DOUBLE_REGRESSION
		if (QM.COOKS_DISTANCE.GUI_TEXT.equals(getQualityMeasureName()))
			itsTargetConcept.setMultiRegressionTargets(new ArrayList<Column>(0));

		/*
		 * SEARCH PARAMETERS
		 */
		itsSearchParameters.setQualityMeasure(QM.fromString(getQualityMeasureName()));
		itsSearchParameters.setQualityMeasureMinimum(getQualityMeasureMinimum());

		itsSearchParameters.setSearchDepth(getSearchDepthMaximum());
		itsSearchParameters.setMinimumCoverage(getSearchCoverageMinimum());
		itsSearchParameters.setMaximumCoverageFraction(getSearchCoverageMaximum());
		itsSearchParameters.setMaximumSubgroups(getSubgroupsMaximum());
		itsSearchParameters.setMaximumTime(getSearchTimeMaximum());

		itsSearchParameters.setSearchStrategy(getSearchStrategyName());
		// set to last known value even for SearchStrategy.BEST_FIRST
		itsSearchParameters.setSearchStrategyWidth(getStrategyWidth());
		itsSearchParameters.setNominalSets(getSetValuedNominals());
		itsSearchParameters.setNumericStrategy(getNumericStrategy());
		itsSearchParameters.setNumericOperators(getNumericOperators());
		// set to last known value even for NumericStrategy.NUMERIC_BINS
		itsSearchParameters.setNrBins(getNrBins());
		itsSearchParameters.setNrThreads(getNrThreads());
/*
 * These values are no longer 'static', but can be changed in MultiTargetsWindow
		itsSearchParameters.setPostProcessingCount(SearchParameters.POST_PROCESSING_COUNT_DEFAULT);
//		itsSearchParameters.setMaximumPostProcessingSubgroups(100); // TODO not used

		// Bayesian stuff
		 // TODO This will overwrite values set in RandomQualitiesWindow
		if (QualityMeasure.getMeasureString(QualityMeasure.EDIT_DISTANCE).equals(getQualityMeasureName()))
			itsSearchParameters.setAlpha(SearchParameters.ALPHA_EDIT_DISTANCE);
		else
			itsSearchParameters.setAlpha(SearchParameters.ALPHA_DEFAULT);
		itsSearchParameters.setBeta(SearchParameters.BETA_DEFAULT);
*/
	}
	/* FIELD METHODS OF CORTANA COMPONENTS */
	// all setters take a String argument for now

	// target concept - target type
	private String getTargetTypeName() { return (String) jComboBoxTargetType.getSelectedItem(); }
	private void setTargetTypeName(String aName) { jComboBoxTargetType.setSelectedItem(aName); }

	// target concepts - quality measure
	private String getQualityMeasureName() { return (String) jComboBoxQualityMeasure.getSelectedItem(); }
	private void setQualityMeasure(String aName) { jComboBoxQualityMeasure.setSelectedItem(aName); }
	private void addQualityMeasureItem(String anItem) { jComboBoxQualityMeasure.addItem(anItem); }
	private void removeAllQualityMeasureItems() { jComboBoxQualityMeasure.removeAllItems(); }

	// target concept - quality measure minimum (member of itsSearchParameters)
	private float getQualityMeasureMinimum() { return getValue(0.0f, jTextFieldQualityMeasureMinimum.getText()); }
	private void setQualityMeasureMinimum(String aValue) { jTextFieldQualityMeasureMinimum.setText(aValue); }

	// target concept - target attribute
	private String getTargetAttributeName() { return (String) jComboBoxTargetAttribute.getSelectedItem(); }
	private void setTargetAttribute(String aName) { jComboBoxTargetAttribute.setSelectedItem(aName); }
	private void addTargetAttributeItem(String anItem) { jComboBoxTargetAttribute.addItem(anItem); }
	private void removeAllTargetAttributeItems() { jComboBoxTargetAttribute.removeAllItems(); }

	// target concept - misc field (target value / secondary target)
	private void addMiscFieldItem(String anItem) { jComboBoxMiscField.addItem(anItem); }
	private void removeAllMiscFieldItems() { jComboBoxMiscField.removeAllItems(); }
	private String getMiscFieldName() { return (String) jComboBoxMiscField.getSelectedItem(); }
	private void setMiscFieldName(String aValue) { jComboBoxMiscField.setSelectedItem(aValue); }

	// target concept - jList secondary targets
	private void addMultiTargetsItem(String theItem) { ((DefaultListModel) jListMultiTargets.getModel()).addElement(theItem); }
	private void removeAllMultiTargetsItems() { ((DefaultListModel) jListMultiTargets.getModel()).clear(); }

	// target concept - jList secondary targets
	private void addMultiRegressionTargetsItem(String theItem) { ((DefaultListModel) jListMultiRegressionTargets.getModel()).addElement(theItem); }
	private void removeAllMultiRegressionTargetsItems() { ((DefaultListModel) jListMultiRegressionTargets.getModel()).clear(); }

	// search conditions - search depth / refinement depth
	private int getSearchDepthMaximum() { return getValue(1, jTextFieldSearchDepth.getText()); }
	private void setSearchDepthMaximum(String aValue) { jTextFieldSearchDepth.setText(aValue); }

	// search conditions - minimum coverage
	private int getSearchCoverageMinimum() { return getValue(0, jTextFieldSearchCoverageMinimum.getText()); }
	private void setSearchCoverageMinimum(String aValue) { jTextFieldSearchCoverageMinimum.setText(aValue); }

	// search conditions - maximum coverage (fraction)
	private float getSearchCoverageMaximum() { return getValue(1.0f, jTextFieldSearchCoverageMaximum.getText()); }
	private void setSearchCoverageMaximum(String aValue) { jTextFieldSearchCoverageMaximum.setText(aValue); }

	// search conditions - maximum subgroups
	private int getSubgroupsMaximum() { return getValue(50, jTextFieldSubgroupsMaximum.getText());}
	private void setSubgroupsMaximum(String aValue) { jTextFieldSubgroupsMaximum.setText(aValue); }

	// search conditions - maximum time
	private float getSearchTimeMaximum() { return getValue(1.0f, jTextFieldSearchTimeMaximum.getText()); }
	private void setSearchTimeMaximum(String aValue) { jTextFieldSearchTimeMaximum.setText(aValue); }

	// search strategy - search strategy
	private String getSearchStrategyName() { return (String) jComboBoxStrategyType.getSelectedItem(); }
	private void setSearchStrategyType(String aValue) { jComboBoxStrategyType.setSelectedItem(aValue); }

	// search strategy - search width
	private int getStrategyWidth() { return getValue(100, jTextFieldStrategyWidth.getText()); }
	private void setStrategyWidth(String aValue) { jTextFieldStrategyWidth.setText(aValue); }

	// search strategy - nominal not equals
	private boolean getSetValuedNominals() { return jCheckBoxSetValuedNominals.isSelected(); }
	private void setSetValuedNominals(String aValue) { jCheckBoxSetValuedNominals.setSelected(Boolean.parseBoolean(aValue)); }

	// search strategy - numeric strategy
	private String getNumericStrategy() { return (String) jComboBoxNumericStrategy.getSelectedItem(); }
	private void setNumericStrategy(String aStrategy) { jComboBoxNumericStrategy.setSelectedItem(aStrategy); }

	// search strategy - numeric operators
	private String getNumericOperators() { return (String) jComboBoxNumericOperators.getSelectedItem(); }
	private void setNumericOperators(String aValue) { jComboBoxNumericOperators.setSelectedItem(aValue); }

	// search strategy - number of bins
	private int getNrBins() { return getValue(8, jTextFieldNumberOfBins.getText()); }
	private void setNrBins(String aValue) { jTextFieldNumberOfBins.setText(aValue); }

	// search strategy - number of threads
	private int getNrThreads() { return getValue(Runtime.getRuntime().availableProcessors(), jTextFieldNumberOfThreads.getText()); }
	private void setNrThreads(String aValue) { jTextFieldNumberOfThreads.setText(aValue); }

	private int getValue(int theDefaultValue, String theText)
	{
		int aValue = theDefaultValue;
		try { aValue = Integer.parseInt(theText); }
		catch (Exception ex) {}	// TODO MM warning dialog
		return aValue;
	}

	private float getValue(float theDefaultValue, String theText)
	{
		float aValue = theDefaultValue;
		try { aValue = Float.parseFloat(theText); }
		catch (Exception ex) {}	// TODO MM warning dialog
		return aValue;
	}

	private void setBusy(boolean isBusy)
	{
		if (isBusy)
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		else
			this.setCursor(Cursor.getDefaultCursor());
	}

	// MENU
	private JMenuBar jMiningWindowMenuBar;
	// MENU - File
	private JMenu jMenuFile;
	private JMenuItem jMenuItemOpenFile;
	private JMenuItem jMenuItemBrowse;
	private JMenuItem jMenuItemExplore;
	private JMenuItem jMenuItemMetaData;
	private JMenuItem jMenuItemSubgroupDiscovery;
	private JMenuItem jMenuItemCreateAutorunFile;
	private JMenuItem jMenuItemAddToAutorunFile;
	private JMenuItem jMenuItemLoadSampledSubgroups;
	// MENU - Enrichment
	private JMenu jMenuEnrichment;
	private JMenuItem jMenuItemAddCuiEnrichmentSource;
	private JMenuItem jMenuItemAddGoEnrichmentSource;
	private JMenuItem jMenuItemAddCustomEnrichmentSource;
	private JMenuItem jMenuItemRemoveEnrichmentSource;
	// Menu About
	private JMenu jMenuAbout;
	private JMenuItem jMenuItemAboutCortana;
	private JMenu jMenuGui;	// for GUI debugging only, DO NOT REMOVE
	private JMenuItem jMenuItemExit;

	// 4 PANELS: DATASET, TARGET CONCEPT, SEARCH CONDITIONS, SEARCH STRATEGY
	private JPanel jPanelCenter;

	// DATA SET
	private JPanel jPanelDataSet;
	private JPanel jPanelDataSetLabels;
	private JLabel jLabelTargetTable;
	private JLabel jLabelNrExamples;
	private JLabel jLabelNrColumns;
	private JLabel jLabelNrNominals;
	private JLabel jLabelNrNumerics;
	private JLabel jLabelNrBinaries;
	private JPanel jPanelDataSetFields;
	private JLabel jLabelTargetTableName;
	private JLabel jLabelNrExamplesNr;
	private JLabel jLabelNrColumnsNr;
	private JLabel jLabelNrNominalsNr;
	private JLabel jLabelNrNumericsNr;
	private JLabel jLabelNrBinariesNr;
	private JPanel jPanelDataSetButtons;
	private JButton jButtonBrowse;
	private JButton jButtonExplore;
	private JButton jButtonMetaData;

	// TARGET CONCEPT
	private JPanel jPanelTargetConcept;
	// TARGET CONCEPT - labels
	private JPanel jPanelTargetConceptLabels;
	private JLabel jLabelTargetType;
	private JLabel jLabelQualityMeasure;
	private JLabel jLabelQualityMeasureMinimum;
	private JLabel jLabelTargetAttribute;
	private JLabel jLabelMiscField;	// also for secondary target
	private JLabel jLabelMultiRegressionTargets;
	private JLabel jLabelMultiTargets;
	private JLabel jLabelTargetInfo;
	// TARGET CONCEPT - fields
	private JPanel jPanelTargetConceptFields;
	private JComboBox jComboBoxTargetType;
	private JComboBox jComboBoxQualityMeasure;
	private JTextField jTextFieldQualityMeasureMinimum;
	private JComboBox jComboBoxTargetAttribute;
	private JComboBox jComboBoxMiscField;
	private JButton jButtonMultiRegressionTargets;
	private JList jListMultiRegressionTargets; // maintain list of targets
	private JButton jButtonMultiTargets;
	private JList jListMultiTargets; // maintain list of targets
	private JLabel jLabelTargetInfoText;
	private JButton jButtonBaseModel;

	// SEARCH CONDITIONS
	private JPanel jPanelSearchConditions;
	// SEARCH CONDITIONS - labels
	private JPanel jPanelSearchConditionsLabels;
	private JLabel jLabelSearchDepth;
	private JLabel jLabelSearchCoverageMinimum;
	private JLabel jLabelSearchCoverageMaximum;
	private JLabel jLabelSubgroupsMaximum;
	private JLabel jLabelSearchTimeMaximum;
	// SEARCH CONDITIONS - fields
	private JPanel jPanelSearchParameterFields;
	private JTextField jTextFieldSearchDepth;
	private JTextField jTextFieldSearchCoverageMinimum;
	private JTextField jTextFieldSearchCoverageMaximum;
	private JTextField jTextFieldSubgroupsMaximum;
	private JTextField jTextFieldSearchTimeMaximum;

	// SEARCH STRATEGY
	private JPanel jPanelSearchStrategy;
	// SEARCH STRATEGY - labels
	private JPanel jPanelSearchStrategyLabels;
	private JLabel jLabelStrategyType;
	private JLabel jLabelStrategyWidth;
	private JLabel jLabelSetValuedNominals;
	private JLabel jLabelNumericStrategy;
	private JLabel jLabelNumericOperators;
	private JLabel jLabelNumberOfBins;
	private JLabel jLabelNumberOfThreads;
	// SEARCH STRATEGY - fields
	private JPanel jPanelSearchStrategyFields;
	private JComboBox jComboBoxStrategyType;
	private JTextField jTextFieldStrategyWidth;
	private JCheckBox jCheckBoxSetValuedNominals;
	private JComboBox jComboBoxNumericStrategy;
	private JComboBox jComboBoxNumericOperators;
	private JTextField jTextFieldNumberOfBins;
	private JTextField jTextFieldNumberOfThreads;

	// SOUTH PANEL - MINING BUTTONS
	private JPanel jPanelSouth;
	private JPanel jPanelMineButtons;
	private JButton jButtonSubgroupDiscovery;
	private JButton jButtonCrossValidate;
	private JButton jButtonComputeThreshold;

	// GUI defaults and convenience methods
	private JMenu initMenu(STD theDefaults)
	{
		JMenu aMenu = new JMenu(theDefaults.GUI_TEXT);
		aMenu.setFont(GUI.DEFAULT_TEXT_FONT);
		aMenu.setMnemonic(theDefaults.MNEMONIC);
		return aMenu;
	}

	private JMenuItem initMenuItem(STD theDefaults)
	{
		return GUI.buildMenuItem(theDefaults.GUI_TEXT,
						theDefaults.MNEMONIC,
						theDefaults.keyStroke(),
						this);
	}

	// TODO MM GUI.buildLabel(), link Label and Field
	private static JLabel initJLabel(String theName)
	{
		JLabel aJLable = new JLabel(theName);
		aJLable.setFont(GUI.DEFAULT_TEXT_FONT);
		return aJLable;
	}

	private JButton initButton(STD theDefaults)
	{
		return GUI.buildButton(theDefaults.GUI_TEXT,
					theDefaults.MNEMONIC,
					theDefaults.GUI_TEXT,
					this);
	}

	// for GUI debugging only, DO NOT REMOVE
	private static final boolean GUI_DEBUG = false;
	private void initJMenuGui()
	{
		JRadioButtonMenuItem aLookAndFeel;
		ButtonGroup itsLookAndFeels = new ButtonGroup();
		String aCurrent = UIManager.getLookAndFeel().getName();

		for (final LookAndFeelInfo l : UIManager.getInstalledLookAndFeels())
		{
			String aName = l.getName();
			aLookAndFeel = new JRadioButtonMenuItem(aName);
			aLookAndFeel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					jMenuGuiActionPerformed(l.getClassName());
				}
			});
			jMenuGui.add(aLookAndFeel);
			itsLookAndFeels.add(aLookAndFeel);
			if (aName.equals(aCurrent))
				aLookAndFeel.setSelected(true);
		}
	}

	private void jMenuGuiActionPerformed(String theLookAndFeelClassName)
	{
		try
		{
			UIManager.setLookAndFeel(theLookAndFeelClassName);
			SwingUtilities.updateComponentTreeUI(this);
		}
		catch (ClassNotFoundException e) { e.printStackTrace(); }
		catch (InstantiationException e) { e.printStackTrace(); }
		catch (IllegalAccessException e) { e.printStackTrace(); }
		catch (UnsupportedLookAndFeelException e) { e.printStackTrace(); }
	}
	// end GUI test

	private static final String TARGET_TYPE_BOX = "target type box";
	private static final String QUALITY_MEASURE_BOX = "quality measure box";
	private static final String MISC_FIELD_BOX = "misc field box";
	private static final String TARGET_ATTRIBUTE_BOX = "target attribute box";
	private static final String SEARCH_TYPE_BOX = "search type box";
	private static final String NUMERIC_STRATEGY_BOX = "numeric strategy box";
	private static final String NUMERIC_OPERATORS_BOX = "numeric operators box";

	private enum STD // for standard
	{
		// ENUM			GUI_TEXT		MNEMONIC	ACCELERATOR
		// MENU
		FILE(			"File",			KeyEvent.VK_F,	false),
		OPEN_FILE(		"Open File",		KeyEvent.VK_O,	true),
		BROWSE(			"Browse...",		KeyEvent.VK_B,	true),
		EXPLORE(		"Explore...",		KeyEvent.VK_E,	true),
		META_DATA(		"Meta Data...",		KeyEvent.VK_D,	true),
		SUBGROUP_DISCOVERY(	"Subgroup Discovery",	KeyEvent.VK_S,	true),
		CREATE_AUTORUN_FILE(	"Create Autorun File",	KeyEvent.VK_C,	true),
		ADD_TO_AUTORUN_FILE(	"Add to Autorun File",	KeyEvent.VK_A,	true),
		LOAD_SAMPLED_SUBGROUPS(	"Load Sampled Subgroups", KeyEvent.VK_L, true),
		EXIT(			"Exit",			KeyEvent.VK_X,	true),
		ENRICHMENT(		"Enrichment",		KeyEvent.VK_N,	false),
		ADD_CUI_DOMAIN(		"Add CUI Domain",	KeyEvent.VK_C,	false),
		ADD_GO_DOMAIN(		"Add GO Domain",	KeyEvent.VK_G,	false),
		ADD_CUSTOM_DOMAIN(	"Add Custom Domain",	KeyEvent.VK_U,	false),
		REMOVE_ENRICHMENT_SOURCE("Remove Enrichment Source", KeyEvent.VK_R, false),
		ABOUT(			"About",		KeyEvent.VK_A,	false),
		ABOUT_CORTANA(		"Cortana",		KeyEvent.VK_I,	true),
		// TARGET CONCEPT
		SECONDARY_TERTIARY_TARGETS("Secondary/Tertiary Targets", KeyEvent.VK_Y, false),
		TARGETS_AND_SETTINGS(	"Targets and Settings",	KeyEvent.VK_T,	false),
		BASE_MODEL(		"Base Model",		KeyEvent.VK_M,	false),
		// MINING
		CROSS_VALIDATE(		"Cross-Validate",	KeyEvent.VK_V,	false),
		COMPUTE_THRESHOLD(	"Compute Threshold",	KeyEvent.VK_P,	false);

		final String GUI_TEXT;
		final int MNEMONIC;
		final boolean HAS_ACCELERATOR;
		private STD(String theText, int theMnemonic, boolean hasAccelerator)
		{
			GUI_TEXT = theText;
			MNEMONIC = theMnemonic;
			HAS_ACCELERATOR = hasAccelerator;
		}

		KeyStroke keyStroke()
		{
			if (HAS_ACCELERATOR)
				return KeyStroke.getKeyStroke(MNEMONIC, InputEvent.CTRL_MASK);
			else
				return null;
		}
	}

	@Override
	public void actionPerformed(ActionEvent theEvent)
	{
		String aCommand = theEvent.getActionCommand();

		// to be replaced by Java 7 String switch someday
		if (STD.OPEN_FILE.GUI_TEXT.equals(aCommand))
			jMenuItemOpenFileActionPerformed();
		else if (STD.BROWSE.GUI_TEXT.equals(aCommand))
			browseActionPerformed();
		else if (STD.EXPLORE.GUI_TEXT.equals(aCommand))
			exploreActionPerformed();
		else if (STD.META_DATA.GUI_TEXT.equals(aCommand))
			metaDataActionPerformed();
		else if (STD.SUBGROUP_DISCOVERY.GUI_TEXT.equals(aCommand))
			subgroupDiscoveryActionPerformed();
		else if (STD.CREATE_AUTORUN_FILE.GUI_TEXT.equals(aCommand))
			jMenuItemAutoRunFileActionPerformed(AutoRun.CREATE);
		else if (STD.ADD_TO_AUTORUN_FILE.GUI_TEXT.equals(aCommand))
			jMenuItemAutoRunFileActionPerformed(AutoRun.ADD);
		else if (STD.LOAD_SAMPLED_SUBGROUPS.GUI_TEXT.equals(aCommand))
			jMenuItemLoadSampledSubgroupsActionPerformed();
		else if (STD.EXIT.GUI_TEXT.equals(aCommand))
			jMenuItemExitActionPerformed();

		else if (STD.ADD_CUI_DOMAIN.GUI_TEXT.equals(aCommand))
			jMenuItemAddEnrichmentSourceActionPerformed(EnrichmentType.CUI);
		else if (STD.ADD_GO_DOMAIN.GUI_TEXT.equals(aCommand))
			jMenuItemAddEnrichmentSourceActionPerformed(EnrichmentType.GO);
		else if (STD.ADD_CUSTOM_DOMAIN.GUI_TEXT.equals(aCommand))
			jMenuItemAddEnrichmentSourceActionPerformed(EnrichmentType.CUSTOM);
		else if (STD.REMOVE_ENRICHMENT_SOURCE.GUI_TEXT.equals(aCommand))
			jMenuItemRemoveEnrichmentSourceActionPerformed();

		else if (STD.ABOUT_CORTANA.GUI_TEXT.equals(aCommand))
			jMenuItemAboutCortanaActionPerformed();

		else if (TARGET_TYPE_BOX.equals(aCommand))
			jComboBoxTargetTypeActionPerformed();
		else if (QUALITY_MEASURE_BOX.equals(aCommand))
			jComboBoxQualityMeasureActionPerformed();
		else if (TARGET_ATTRIBUTE_BOX.equals(aCommand))
			jComboBoxTargetAttributeActionPerformed();
		else if (MISC_FIELD_BOX.equals(aCommand))
			jComboBoxMiscFieldActionPerformed();

		else if (STD.SECONDARY_TERTIARY_TARGETS.GUI_TEXT.equals(aCommand))
			jButtonMultiRegressionTargetsActionPerformed();
		else if (STD.TARGETS_AND_SETTINGS.GUI_TEXT.equals(aCommand))
			jButtonMultiTargetsActionPerformed();
		else if (STD.BASE_MODEL.GUI_TEXT.equals(aCommand))
			jButtonBaseModelActionPerformed();

		else if (SEARCH_TYPE_BOX.equals(aCommand))
			jComboBoxSearchStrategyTypeActionPerformed();
		else if (NUMERIC_STRATEGY_BOX.equals(aCommand))
			jComboBoxSearchStrategyNumericActionPerformed();
		else if (NUMERIC_OPERATORS_BOX.equals(aCommand))
			jComboBoxNumericOperatorsActionPerformed();

		else if (STD.CROSS_VALIDATE.GUI_TEXT.equals(aCommand))
			jButtonCrossValidateActionPerformed();
		else if (STD.COMPUTE_THRESHOLD.GUI_TEXT.equals(aCommand))
			jButtonComputeThresholdActionPerformed();
	}
}
