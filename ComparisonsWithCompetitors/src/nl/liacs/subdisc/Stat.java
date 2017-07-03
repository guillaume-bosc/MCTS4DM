package nl.liacs.subdisc;

/**
 * This is an auxiliary class for {@link QM}s that are used with the
 * {@link TargetType#SINGLE_NUMERIC} {@link TargetType}.
 * <p>
 * The related
 * {@link QualityMeasure#calculate(int, float, float, float, float,
 * ProbabilityDensityFunction)} method takes a lot of parameters, the value of
 * which is ignored for most {@link QM}s.
 * Obtaining the value of some of these values is computationally demanding, and
 * therefore a considerable amount of CPU-cycles is wasted for nothing if the
 * specific {@link QM} ignores the value anyway.
 * <p>
 * {@link QM}s that are used with the {@link TargetType#SINGLE_NUMERIC}
 * {@link TargetType} should indicate which statistics need to be calculated for
 * them.
 * Meaning of the names of the Stat enums are below.
 * <ul>
 * <li>SUM: sum</li>
 * <li>SSD: sum of squared deviations</li>
 * <li>MEDIAN: median</li>
 * <li>MAD: median absolute deviation</li>
 * <li>{@link ProbabilityDensityFunction PDF}: probability density function</li>
 * </ul>
 * 
 * @see QM
 * @see Column#getStatistics(java.util.BitSet, Stat)
 * @see ProbabilityDensityFunction
 * 
 * @author marvin
 */
public enum Stat
{
	SUM,	// MEAN_TEST, Z-SCORE, T_TEST, AVERAGE, AUC, WMW_RANKS
	SSD,	// T_TEST
	MEDIAN,	// MMAD
	MAD,	// MMAD
	PDF	// HELLINGER, KULLBACK_LEIBLER, CWRACC
}
