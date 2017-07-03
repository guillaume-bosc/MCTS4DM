package de.fraunhofer.iais.ocm.core.mining.beamsearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import de.fraunhofer.iais.ocm.core.mining.AlgorithmCategory;
import de.fraunhofer.iais.ocm.core.mining.AssociationMiningAlgorithm;
import de.fraunhofer.iais.ocm.core.mining.annotation.AlgorithmDefinition;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.data.propositions.Proposition;
import de.fraunhofer.iais.ocm.core.model.pattern.Association;
import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.model.utility.PatternUtilityModel;
import de.fraunhofer.iais.ocm.core.util.LimitedPriorityQueue;

/**
 * User: paveltokmakov Date: 02/11/13
 */
public class AssociationMiningBeamSearch extends BeamSearch implements AssociationMiningAlgorithm, AlgorithmDefinition {

	@Override
	public Pattern generateSeed(DataTable dataTable) {
		return new Association(dataTable, new ArrayList<Proposition>());
	}

	@Override
	protected void initialize(DataTable dataTable,
			PatternUtilityModel patternUtilityModel) {
		propositions = dataTable.getPropositionStore().getPropositions();
	}

	private boolean partOfdescriptionInMetaAttributeWithAugmentation(
			Pattern previousPattern, Proposition augmentation) {
		for (Proposition prop : previousPattern.getDescription()) {
			if (prop.getAttribute().isPartOfMacroAttributeWith(
					augmentation.getAttribute())) {
				return true;
			}
		}
		return false;
	}

	protected PriorityQueue<Pattern> generateNextLevelViaPropositions(
			PriorityQueue<Pattern> previousLevel) {

		PriorityQueue<Pattern> nextLevel = new LimitedPriorityQueue<Pattern>(
				INITIAL_QUEUE_CAPACITY, previousLevel.comparator(), beamWidth);
		// (Integer) beamWidth.getValue());

		for (Pattern previousPattern : previousLevel) {
			for (Proposition augmentation : this.propositions) {
				if (isStop()) {
					return nextLevel;
				}

				if (previousPattern.containsAttribute(augmentation
						.getAttribute())) {
					continue;
				}

				Set<Proposition> newDescription = new HashSet<Proposition>(
						previousPattern.getDescription());
				newDescription.add(augmentation);
				if (containsDescription(newDescription, nextLevel)
						|| partOfdescriptionInMetaAttributeWithAugmentation(
								previousPattern, augmentation)) {
					continue;
				}

				Pattern newPattern = previousPattern
						.generateSpecialization(augmentation);

				if (newPattern.getFrequency() != 0) {
					nextLevel.add(newPattern);
				}
			}
		}

		return nextLevel;
	}

	@Override
	protected void postProcessing(Collection<Pattern> results) {
		List<Pattern> temp = new ArrayList<Pattern>();

		for (Pattern pattern : results) {
			if (pattern.getDescriptionSize() > 1) {
				temp.add(pattern);
			}
		}

		results.clear();
		results.addAll(temp);
	}

	@Override
	public String toString() {
		return "AssociationBeamSearch";
	}

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public AlgorithmCategory getCategory() {
        return AlgorithmCategory.ASSOCIATION_MINING;
    }

    @Override
    public String getDescription() {
        return "Association Mining using BeamSearch strategy";
    }
}
