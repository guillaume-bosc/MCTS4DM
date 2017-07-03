package nl.liacs.subdisc;

import org.w3c.dom.*;

/**
 * SearchParameters contains all search parameters for an experiment.
 */
public class SearchParameters implements XMLNodeInterface
{
	public static final float ALPHA_EDIT_DISTANCE = 0.0f;
	public static final float ALPHA_DEFAULT = 0.5f;
	public static final float BETA_DEFAULT = 1.0f;
	public static final int POST_PROCESSING_COUNT_DEFAULT = 20;

	// when adding/removing members be sure to update addNodeTo() and loadNode()
	private TargetConcept	itsTargetConcept;
	private QM		itsQualityMeasure;
	private float		itsQualityMeasureMinimum;

	private int		itsSearchDepth;
	private int		itsMinimumCoverage;
	private float		itsMaximumCoverageFraction;
	private int		itsMaximumSubgroups;
	private float		itsMaximumTime;

	private SearchStrategy	itsSearchStrategy;
	private int		itsSearchStrategyWidth;
	private boolean	itsNominalSets;
	private NumericOperatorSetting itsNumericOperatorSetting;
	private NumericStrategy	itsNumericStrategy;
	private int		itsNrBins;
	private int		itsNrThreads;

	private float		itsAlpha;
	private float		itsBeta;
	private boolean	itsPostProcessingDoAutoRun;
	private int		itsPostProcessingCount;

	public SearchParameters(Node theSearchParametersNode)
	{
		loadData(theSearchParametersNode);
	}

	public SearchParameters()
	{
		/*
		 * There are no MiningWindow text fields for the following.
		 * But they need to be available for MULTI_LABELs
		 * 'Targets and Settings'.
		 * They are no longer 'static', but can be changed upon users
		 * discretion, therefore they can not be set in
		 * initSearchParameters.
		 */
		itsAlpha = ALPHA_DEFAULT;
		itsBeta = BETA_DEFAULT;
		itsPostProcessingCount = POST_PROCESSING_COUNT_DEFAULT;
		itsPostProcessingDoAutoRun = true;
	}

	/* QUALITY MEASURE */
	public TargetConcept getTargetConcept() { return itsTargetConcept; }
	public void setTargetConcept(TargetConcept theTargetConcept) { itsTargetConcept = theTargetConcept; }
	public TargetType getTargetType() { return itsTargetConcept.getTargetType(); }
	public QM getQualityMeasure() { return itsQualityMeasure; }
	public float getQualityMeasureMinimum() { return itsQualityMeasureMinimum; }
	public void setQualityMeasureMinimum(float theQualityMeasureMinimum) { itsQualityMeasureMinimum = theQualityMeasureMinimum; }
	public void setQualityMeasure(QM theQualityMeasure) { itsQualityMeasure = theQualityMeasure; }

	/* SEARCH CONDITIONS */
	public int getSearchDepth() { return itsSearchDepth; }
	public void setSearchDepth(int theSearchDepth) { itsSearchDepth = theSearchDepth; }
	public int getMinimumCoverage() { return itsMinimumCoverage; }
	public void setMinimumCoverage(int theMinimumCoverage) { itsMinimumCoverage = theMinimumCoverage; }
	public float getMaximumCoverageFraction() { return itsMaximumCoverageFraction; }
	public void setMaximumCoverageFraction(float theMaximumCoverageFraction) { itsMaximumCoverageFraction = theMaximumCoverageFraction; }
	public int getMaximumSubgroups() { return itsMaximumSubgroups; }
	public void setMaximumSubgroups(int theMaximumSubgroups) { itsMaximumSubgroups  = theMaximumSubgroups; }
	public float getMaximumTime() { return itsMaximumTime; }
	public void setMaximumTime(float theMaximumTime) { itsMaximumTime = theMaximumTime; }

	/* SEARCH STRATEGY */
	public SearchStrategy getSearchStrategy() { return itsSearchStrategy; }

	public void setSearchStrategy(String theSearchStrategyName)
	{
		itsSearchStrategy = SearchStrategy.fromString(theSearchStrategyName);
	}

	public boolean getNominalSets()
	{
		if (itsTargetConcept.getTargetType() != TargetType.SINGLE_NOMINAL) //other than SINGLE_NOMINAL?
			return false;
		else
			return itsNominalSets;
	}
	public void setNominalSets(boolean theValue) {itsNominalSets = theValue;}

	public NumericOperatorSetting getNumericOperatorSetting()
	{
		if (itsNumericStrategy == NumericStrategy.NUMERIC_INTERVALS) //intervals automatically imply "in"
			return NumericOperatorSetting.NUMERIC_INTERVALS;
		else
			return itsNumericOperatorSetting;
	}
	public void setNumericOperators(String theNumericOperatorsName)
	{
		itsNumericOperatorSetting = NumericOperatorSetting.fromString(theNumericOperatorsName);
	}

	public NumericStrategy getNumericStrategy() { return itsNumericStrategy; }
	public void setNumericStrategy(String theNumericStrategyName)
	{
		itsNumericStrategy = NumericStrategy.fromString(theNumericStrategyName);
	}

	public int getSearchStrategyWidth()			{ return itsSearchStrategyWidth; }
	public void setSearchStrategyWidth(int theWidth)	{ itsSearchStrategyWidth = theWidth; }
	public int getNrBins()					{ return itsNrBins; }
	public void setNrBins(int theNrBins)			{ itsNrBins = theNrBins; }
	public int getNrThreads()				{ return itsNrThreads; }
	public void setNrThreads(int theNrThreads)		{ itsNrThreads = theNrThreads; }
	public float getAlpha()				{ return itsAlpha; }
	public void setAlpha(float theAlpha)			{ itsAlpha = theAlpha; }
	public float getBeta()					{ return itsBeta; }
	public void setBeta(float theBeta)			{ itsBeta = theBeta; }
	public boolean getPostProcessingDoAutoRun()		{ return itsPostProcessingDoAutoRun; }
	public void setPostProcessingDoAutoRun(boolean theAutoRunSetting) { itsPostProcessingDoAutoRun = theAutoRunSetting; }
	public int getPostProcessingCount()			{ return itsPostProcessingCount; }
	public void setPostProcessingCount(int theNr)		{ itsPostProcessingCount = theNr; }

	/**
	 * Creates an {@link XMLNode XMLNode} representation of this
	 * SearchParameters.
	 * @param theParentNode the Node of which this Node will be a ChildNode
//	 * @return a Node that contains all the information of this SearchParameters
	 */
	@Override
	public void addNodeTo(Node theParentNode)
	{
		Node aNode = XMLNode.addNodeTo(theParentNode, "search_parameters");
		// itsTargetConcept is added through its own Node
		XMLNode.addNodeTo(aNode, "quality_measure", itsQualityMeasure.GUI_TEXT);
		XMLNode.addNodeTo(aNode, "quality_measure_minimum", getQualityMeasureMinimum());
		XMLNode.addNodeTo(aNode, "search_depth", getSearchDepth());
		XMLNode.addNodeTo(aNode, "minimum_coverage", getMinimumCoverage());
		XMLNode.addNodeTo(aNode, "maximum_coverage_fraction", getMaximumCoverageFraction());
		XMLNode.addNodeTo(aNode, "maximum_subgroups", getMaximumSubgroups());
		XMLNode.addNodeTo(aNode, "maximum_time", getMaximumTime());
		XMLNode.addNodeTo(aNode, "search_strategy", getSearchStrategy().GUI_TEXT);
		XMLNode.addNodeTo(aNode, "use_nominal_sets", getNominalSets());
		XMLNode.addNodeTo(aNode, "search_strategy_width", getSearchStrategyWidth());
		XMLNode.addNodeTo(aNode, "numeric_operators", getNumericOperatorSetting().GUI_TEXT);
		XMLNode.addNodeTo(aNode, "numeric_strategy", getNumericStrategy().GUI_TEXT);
		XMLNode.addNodeTo(aNode, "nr_bins", getNrBins());
		XMLNode.addNodeTo(aNode, "nr_threads", getNrThreads());
		XMLNode.addNodeTo(aNode, "alpha", getAlpha());
		XMLNode.addNodeTo(aNode, "beta", getBeta());
		XMLNode.addNodeTo(aNode, "post_processing_do_autorun", getPostProcessingDoAutoRun());
		XMLNode.addNodeTo(aNode, "post_processing_count", getPostProcessingCount());
	}

	private void loadData(Node theSearchParametersNode)
	{
		NodeList aChildren = theSearchParametersNode.getChildNodes();
		for(int i = 0, j = aChildren.getLength(); i < j; ++i)
		{
			Node aSetting = aChildren.item(i);
			String aNodeName = aSetting.getNodeName().toLowerCase();
			if("quality_measure".equalsIgnoreCase(aNodeName))
				itsQualityMeasure = QM.fromString(aSetting.getTextContent());
			else if("quality_measure_minimum".equalsIgnoreCase(aNodeName))
				itsQualityMeasureMinimum = Float.parseFloat(aSetting.getTextContent());
			else if("search_depth".equalsIgnoreCase(aNodeName))
				itsSearchDepth = Integer.parseInt(aSetting.getTextContent());
			else if("minimum_coverage".equalsIgnoreCase(aNodeName))
				itsMinimumCoverage = Integer.parseInt(aSetting.getTextContent());
			else if("maximum_coverage_fraction".equalsIgnoreCase(aNodeName))
				itsMaximumCoverageFraction = Float.parseFloat(aSetting.getTextContent());
			else if("maximum_subgroups".equalsIgnoreCase(aNodeName))
				itsMaximumSubgroups = Integer.parseInt(aSetting.getTextContent());
			else if("maximum_time".equalsIgnoreCase(aNodeName))
				itsMaximumTime = Float.parseFloat(aSetting.getTextContent());
			else if("search_strategy".equalsIgnoreCase(aNodeName))
				itsSearchStrategy = (SearchStrategy.fromString(aSetting.getTextContent()));
			else if("use_nominal_sets".equalsIgnoreCase(aNodeName))
				itsNominalSets = Boolean.parseBoolean(aSetting.getTextContent());
			else if("search_strategy_width".equalsIgnoreCase(aNodeName))
				itsSearchStrategyWidth = Integer.parseInt(aSetting.getTextContent());
			else if("numeric_operators".equalsIgnoreCase(aNodeName))
				itsNumericOperatorSetting = (NumericOperatorSetting.fromString(aSetting.getTextContent()));
			else if("numeric_strategy".equalsIgnoreCase(aNodeName))
				itsNumericStrategy = (NumericStrategy.fromString(aSetting.getTextContent()));
			else if("nr_bins".equalsIgnoreCase(aNodeName))
				itsNrBins = Integer.parseInt(aSetting.getTextContent());
			else if("nr_threads".equalsIgnoreCase(aNodeName))
				itsNrThreads = Integer.parseInt(aSetting.getTextContent());
			else if("alpha".equalsIgnoreCase(aNodeName))
				itsAlpha = Float.parseFloat(aSetting.getTextContent());
			else if("beta".equalsIgnoreCase(aNodeName))
				itsBeta = Float.parseFloat(aSetting.getTextContent());
			else if("post_processing_do_autorun".equalsIgnoreCase(aNodeName))
				itsPostProcessingDoAutoRun = Boolean.parseBoolean(aSetting.getTextContent());
			else if("post_processing_count".equalsIgnoreCase(aNodeName))
				itsPostProcessingCount = Integer.parseInt(aSetting.getTextContent());
			else
				Log.logCommandLine("ignoring unknown XML node: " + aNodeName);
		}
	}
}
