package de.fraunhofer.iais.ocm.core.mining.beamsearch;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import de.fraunhofer.iais.ocm.common.parameter.annotation.ExtensionalMiningParameterAnnotation;
import de.fraunhofer.iais.ocm.common.parameter.annotation.IntentionalMiningParameterAnnotation;
import de.fraunhofer.iais.ocm.common.parameter.rangebounder.ExtensionalRangeBounder;
import de.fraunhofer.iais.ocm.common.parameter.rangebounder.IntentionalRangeBounder;
import de.fraunhofer.iais.ocm.core.mining.AbstractMiner;
import de.fraunhofer.iais.ocm.core.mining.annotation.ParameterMultiplicity;
import de.fraunhofer.iais.ocm.core.mining.parameter.ListMiningParameterAnnotation;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.data.propositions.Proposition;
import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.model.utility.PatternUtilityModel;
import de.fraunhofer.iais.ocm.core.util.LimitedPriorityQueue;

/**
 * User: paveltokmakov Date: 5/11/13
 */
public abstract class BeamSearch extends AbstractMiner {

	protected static final int INITIAL_QUEUE_CAPACITY = 11;

	protected List<Proposition> propositions;

	private Comparator<Pattern> comparator;

    @IntentionalMiningParameterAnnotation(
            getDescription = "Number of nodes that are expanded per level",
            getName = "Beam-width",
            getRangeBounder = IntentionalRangeBounder.POSITIVE_INTEGER,
            getDisplayPosition = 1,
            getParameterMultiplicity = ParameterMultiplicity.SINGLE
    )
	protected Integer beamWidth = 5;

    @ExtensionalMiningParameterAnnotation(
            getDescription = "TestDescription",
            getDisplayPosition = 42,
            getName = "TestName",
            getParameterMultiplicity = ParameterMultiplicity.SINGLE,
            getRangeBounder = ExtensionalRangeBounder.NON_ID_ATTRIBUTES
    )
    private String test = "Hallo";

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    private Integer numberOfResults = 10;

	protected abstract Pattern generateSeed(DataTable dataTable);

	public void mine(DataTable dataTable,
			PatternUtilityModel patternUtilityModel) {
		setStop(false);

		Collection<Pattern> results = new LimitedPriorityQueue<Pattern>(
				INITIAL_QUEUE_CAPACITY, comparator, numberOfResults);
		setResults(results);

		initialize(dataTable, patternUtilityModel);

		PriorityQueue<Pattern> b = new LimitedPriorityQueue<Pattern>(
				INITIAL_QUEUE_CAPACITY, comparator, beamWidth);
		// (Integer) beamWidth.getValue());
		try {
			b.add(generateSeed(dataTable));
			PriorityQueue<Pattern> nextLevel;

			do {
				// apply refinement operator to generate patterns
				// for current level from previous level
				nextLevel = generateNextLevelViaPropositions(b);

				// keep only most interesting {beamSize} number patterns
				b = new LimitedPriorityQueue<Pattern>(nextLevel, beamWidth);
				/*
				 * combine these interesting patterns with previous ones and
				 * keep only most interesting {resultsLimit} number of patterns
				 * in {results}
				 */
				results.addAll(b);
			} while (!isStop() && !nextLevel.isEmpty());
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}

		postProcessing(results);
		setStop(true);
	}

	protected abstract void postProcessing(Collection<Pattern> results);

	protected PriorityQueue<Pattern> generateNextLevelViaPropositions(
			PriorityQueue<Pattern> previousLevel) {

		PriorityQueue<Pattern> nextLevel = new LimitedPriorityQueue<Pattern>(
				INITIAL_QUEUE_CAPACITY, previousLevel.comparator(), beamWidth);
		// (Integer) beamWidth.getValue());

		for (Pattern previousPattern : previousLevel) {
			for (Proposition proposition : this.propositions) {
				if (isStop()) {
					return nextLevel;
				}

				if (previousPattern.containsAttribute(proposition
						.getAttribute())) {
					continue;
				}

				Set<Proposition> newDescription = new HashSet<Proposition>(
						previousPattern.getDescription());
				newDescription.add(proposition);
				if (containsDescription(newDescription, nextLevel)) {
					continue;
				}

				Pattern newPattern = previousPattern
						.generateSpecialization(proposition);

				if (newPattern.getFrequency() != 0) {
					nextLevel.add(newPattern);
				}
			}
		}

		return nextLevel;
	}

	protected boolean containsDescription(Set<Proposition> description,
			Collection<Pattern> patternCollection) {
		for (Pattern pattern : patternCollection) {
			Set<Proposition> patternDescription = new HashSet<Proposition>(
					pattern.getDescription());
			if (patternDescription.equals(description)) {
				return true;
			}
		}
		return false;
	}

	// private boolean isDuplicate(Pattern newPattern,
	// PriorityQueue<Pattern> nextLevel) {
	// outer: for (Pattern previouslyGenerated : nextLevel) {
	// for (Proposition literal : newPattern.getDescription()) {
	// if (!previouslyGenerated.getDescription().contains(literal)) {
	// continue outer;
	// }
	// }
	//
	// return true;
	// }
	//
	// return false;
	// }

	protected abstract void initialize(DataTable dataTable,
			PatternUtilityModel patternUtilityModel);

	public void setComparator(Comparator<Pattern> comparator) {
		this.comparator = comparator;
	}

    public Integer getBeamWidth() {
        return beamWidth;
    }

    @ListMiningParameterAnnotation(getName = "Beam Width",
			getDescription = "Number of nodes that are expanded per level", 
			getRangeBounder = ExtensionalRangeBounder.ONE_TO_NUMBER_OF_NON_ID_ATTRIBUTES)
	public void setBeamWidth(Integer beamWidth) {
		this.beamWidth = beamWidth;
	}

	@ListMiningParameterAnnotation(getName = "No. Results",
			getDescription = "Number of patterns to produce",
			getRangeBounder = ExtensionalRangeBounder.UNBOUNDED)
	public void setNumberOfResults(Integer resultsLimit) {
		this.numberOfResults=resultsLimit;
	}

}
