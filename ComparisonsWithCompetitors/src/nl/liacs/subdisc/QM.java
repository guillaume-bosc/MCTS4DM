package nl.liacs.subdisc;

import java.util.*;

public enum QM implements EnumInterface
{
	// TargetType is a single value, but could be an EnumSet if needed
	// ENUM		GUI text	default measure minimum	TargetType

	// SINGLE_NOMINAL quality measures
	WRACC		("WRAcc",		"0.02",	TargetType.SINGLE_NOMINAL),
	ABSWRACC	("Abs WRAcc",		"0.02",	TargetType.SINGLE_NOMINAL),
	CHI_SQUARED	("Chi-squared",		"50",	TargetType.SINGLE_NOMINAL),
	INFORMATION_GAIN("Information gain",	"0.02",	TargetType.SINGLE_NOMINAL),
	BINOMIAL	("Binomial test",	"0.05",	TargetType.SINGLE_NOMINAL),
	ACCURACY	("Accuracy",		"0.0",	TargetType.SINGLE_NOMINAL),
	PURITY		("Purity",		"0.5",	TargetType.SINGLE_NOMINAL),
	JACCARD		("Jaccard",		"0.2",	TargetType.SINGLE_NOMINAL),
	COVERAGE	("Coverage",		"10",	TargetType.SINGLE_NOMINAL),
	SPECIFICITY	("Specificity",		"0.5",	TargetType.SINGLE_NOMINAL),
	SENSITIVITY	("Sensitivity",		"0.5",	TargetType.SINGLE_NOMINAL),
	LAPLACE		("Laplace",		"0.2",	TargetType.SINGLE_NOMINAL),
	F_MEASURE	("F-measure",		"0.2",	TargetType.SINGLE_NOMINAL),
	G_MEASURE	("G-measure",		"0.2",	TargetType.SINGLE_NOMINAL),
	CORRELATION	("Correlation",		"0.1",	TargetType.SINGLE_NOMINAL),
	PROP_SCORE_WRACC("Propensity score wracc",	"-0.25",	TargetType.SINGLE_NOMINAL),
	PROP_SCORE_RATIO("Propensity score ratio",	"1.0",		TargetType.SINGLE_NOMINAL),
	BAYESIAN_SCORE	("Bayesian Score",	"0.0",	TargetType.SINGLE_NOMINAL),
	LIFT		("Lift",		"1.0",	TargetType.SINGLE_NOMINAL),

	// SINGLE_NUMERIC quality measures
	// NOTE when adding a new SINGLE_NUMERIC QM -> add it to requiredStats()
	Z_SCORE		("Z-Score",		"1.0",	TargetType.SINGLE_NUMERIC),
	INVERSE_Z_SCORE	("Inverse Z-Score",	"1.0",	TargetType.SINGLE_NUMERIC),
	ABS_Z_SCORE	("Abs Z-Score",		"1.0",	TargetType.SINGLE_NUMERIC),
	AVERAGE		("Average",		"0.0",	TargetType.SINGLE_NUMERIC),	// bogus minimum value, should come from data
	INVERSE_AVERAGE	("Inverse Average",	"0.0",	TargetType.SINGLE_NUMERIC),	// bogus minimum value, should come from data
	MEAN_TEST	("Mean Test",		"0.01",	TargetType.SINGLE_NUMERIC),
	INVERSE_MEAN_TEST("Inverse Mean Test",	"0.01",	TargetType.SINGLE_NUMERIC),
	ABS_MEAN_TEST	("Abs Mean Test",	"0.01",	TargetType.SINGLE_NUMERIC),
	T_TEST		("t-Test",		"1.0",	TargetType.SINGLE_NUMERIC),
	INVERSE_T_TEST	("Inverse t-Test",	"1.0",	TargetType.SINGLE_NUMERIC),
	ABS_T_TEST	("Abs t-Test",		"1.0",	TargetType.SINGLE_NUMERIC),
	SQUARED_HELLINGER			("Squared Hellinger distance",		"0.0",	TargetType.SINGLE_NUMERIC),
	SQUARED_HELLINGER_WEIGHTED		("Weighted Squared Hellinger distance",	"0.0",	TargetType.SINGLE_NUMERIC),
	SQUARED_HELLINGER_WEIGHTED_ADJUSTED	("Adjusted Squared Hellinger distance",	"0.0",	TargetType.SINGLE_NUMERIC),
	KULLBACK_LEIBLER			("Kullback-Leibler divergence",		"0.0",	TargetType.SINGLE_NUMERIC),
	KULLBACK_LEIBLER_WEIGHTED		("Weighted Kullback-Leibler divergence","0.0",	TargetType.SINGLE_NUMERIC),
	CWRACC		("CWRAcc",		"0.0",	TargetType.SINGLE_NUMERIC),

	// SINGLE_ORDINAL quality measures
	// NOTE when adding a new SINGLE_ORDINAL QM -> add it to requiredStats()
	AUC		("AUC of ROC",		"0.5",	TargetType.SINGLE_ORDINAL),
	WMW_RANKS	("WMW-Ranks test",	"1.0",	TargetType.SINGLE_ORDINAL),
	INVERSE_WMW_RANKS("Inverse WMW-Ranks test",	"1.0",	TargetType.SINGLE_ORDINAL),
	ABS_WMW_RANKS	("Abs WMW-Ranks test",	"1.0",	TargetType.SINGLE_ORDINAL),
	MMAD		("Median MAD metric",	"0",	TargetType.SINGLE_ORDINAL),

	// MULTI_LABEL quality measures
	WEED		("Wtd Ent Edit Dist",	"0",	TargetType.MULTI_LABEL),
	EDIT_DISTANCE	("Edit Distance",	"0",	TargetType.MULTI_LABEL),

	// DOUBLE_CORRELATION  quality measures
	CORRELATION_R		("r",			"0.2",	TargetType.DOUBLE_CORRELATION),
	CORRELATION_R_NEG	("Negative r",		"0.2",	TargetType.DOUBLE_CORRELATION),
	CORRELATION_R_NEG_SQ	("Neg Sqr r",		"0.2",	TargetType.DOUBLE_CORRELATION),
	CORRELATION_R_SQ	("Squared r",		"0.2",	TargetType.DOUBLE_CORRELATION),
	CORRELATION_DISTANCE	("Distance",		"0.0",	TargetType.DOUBLE_CORRELATION),
	CORRELATION_P		("p-Value Distance",	"0.0",	TargetType.DOUBLE_CORRELATION),
	CORRELATION_ENTROPY	("Wtd Ent Distance",	"0.0",	TargetType.DOUBLE_CORRELATION),
	ADAPTED_WRACC		("Adapted WRAcc",	"0.0",	TargetType.DOUBLE_CORRELATION),
	COSTS_WRACC		("Costs WRAcc",		"0.0",	TargetType.DOUBLE_CORRELATION),

	// DOUBLE_REGRESSION quality measures
	LINEAR_REGRESSION	("Significance of Slope Difference", "0.0", TargetType.DOUBLE_REGRESSION),
	COOKS_DISTANCE		("Cook's Distance",	"0.0",	TargetType.DOUBLE_REGRESSION);

	// to enforce implementation of SINGLE_NUMERIC and SINGLE_ORDINAL QMs
	static { requiredStatsTest(); };

	public final String GUI_TEXT;
	public final String MEASURE_DEFAULT;
	public final TargetType TARGET_TYPE;

	private QM(String theGuiText, String theMeasureDefault, TargetType theTargetType)
	{
		this.GUI_TEXT = theGuiText;
		this.MEASURE_DEFAULT = theMeasureDefault;
		this.TARGET_TYPE = theTargetType;
	}

	public static final Set<QM> getQualityMeasures(TargetType theTargetType)
	{
		Set<QM> aSet = EnumSet.noneOf(QM.class);
		for (QM qm : QM.values())
			if (qm.TARGET_TYPE == theTargetType)
				aSet.add(qm);

		// remove non implemented methods
		if (TargetType.SINGLE_NOMINAL == theTargetType)
		{
			aSet.remove(PROP_SCORE_WRACC);
			aSet.remove(PROP_SCORE_RATIO);
		}
		else if (TargetType.DOUBLE_REGRESSION == theTargetType)
			aSet.remove(COOKS_DISTANCE);

		return aSet;
	}

	/**
	 * Returns the QM corresponding to the supplied {@code String} parameter
	 *  based on the various {@link QM#GUI_TEXT}s.
	 * 
	 * @param theText the {@code String} corresponding to a QM.
	 * 
	 * @return a QM, or {@code null} if no corresponding QM is found.
	 */
	public static QM fromString(String theText)
	{
		for (QM qm : QM.values())
			if (qm.GUI_TEXT.equalsIgnoreCase(theText))
				return qm;

		return null;
	}

	// NOTE EnumSets are modifiable like any other set, prevent this
	// EnumSet < 65 items are internally represented as a single long
	private static final Set<Stat> SUM = Collections.unmodifiableSet(EnumSet.of(Stat.SUM));
	private static final Set<Stat> SUM_SSD = Collections.unmodifiableSet(EnumSet.of(Stat.SUM, Stat.SSD));
	private static final Set<Stat> MEDIAN_MAD = Collections.unmodifiableSet(EnumSet.of(Stat.MEDIAN, Stat.MAD));
	private static final Set<Stat> PDF = Collections.unmodifiableSet(EnumSet.of(Stat.PDF));

	/*
	 * In general, the splitting of an Enum declaration and its logic is a
	 * bad practice. However, the file structure would suffer greatly by
	 * adding a getRequiredStats() method to each Enum declaration.
	 * Also, only SINGLE_NUMERIC QMs require such a method.
	 * But, when a new SINGLE_NUMERIC QM declaration is added, it should
	 * also be added here.
	 */
	/**
	 * 
	 * 
	 * @param theQM the single numeric QM for which to query the Stat types.
	 * 
	 * @return a Set 
	 * 
	 * @see Stat
	 * @see Column#getStatistics(BitSet, Set)
	 */
	public static Set<Stat> requiredStats(QM theQM)
	{
		switch(theQM)
		{
			// SINGLE_NUMERIC
			case Z_SCORE :		return SUM;
			case INVERSE_Z_SCORE :	return SUM;
			case ABS_Z_SCORE :	return SUM;
			case AVERAGE :		return SUM;
			case INVERSE_AVERAGE :	return SUM;
			case MEAN_TEST :	return SUM;
			case INVERSE_MEAN_TEST :return SUM;
			case ABS_MEAN_TEST :	return SUM;
			case T_TEST :		return SUM_SSD;
			case INVERSE_T_TEST :	return SUM_SSD;
			case ABS_T_TEST :	return SUM_SSD;
			case SQUARED_HELLINGER :		return PDF;
			case SQUARED_HELLINGER_WEIGHTED :	return PDF;
			case SQUARED_HELLINGER_WEIGHTED_ADJUSTED :	return PDF;
			case KULLBACK_LEIBLER :			return PDF;
			case KULLBACK_LEIBLER_WEIGHTED :	return PDF;
			case CWRACC :		return PDF;
			// SINGLE_ORDINAL
			case AUC :		return SUM;
			case WMW_RANKS :	return SUM;
			case INVERSE_WMW_RANKS :return SUM;
			case ABS_WMW_RANKS :	return SUM;
			case MMAD :		return MEDIAN_MAD;
			default :
			{
				// throws NullPointerException if theQM == null
				if (theQM.TARGET_TYPE == TargetType.SINGLE_NUMERIC ||
					theQM.TARGET_TYPE == TargetType.SINGLE_ORDINAL)
					throw new AssertionError(
						"QM.requiredStats() not implemented for: " + theQM);
				else
					throw new IllegalArgumentException(
						String.format("%s not for %s or %s",
								theQM,
								TargetType.SINGLE_NUMERIC,
								TargetType.SINGLE_ORDINAL));
			}
		}
	}

	/*
	 * throws an AssertionError(QM) if a SINGLE_NUMERIC or SINGLE_ORDINAL
	 * QM is completely implemented
	 */
	private static void requiredStatsTest()
	{
		for (QM qm : QM.values())
			if (qm.TARGET_TYPE == TargetType.SINGLE_NUMERIC ||
				qm.TARGET_TYPE == TargetType.SINGLE_ORDINAL)
				requiredStats(qm);
	}

	@Override
	public String toString() { return GUI_TEXT; };
}
