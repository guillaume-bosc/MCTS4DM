package de.fraunhofer.iais.ocm.core.mining;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.data.propositions.Proposition;
import de.fraunhofer.iais.ocm.core.model.pattern.ExceptionalModelPattern;
import de.fraunhofer.iais.ocm.core.model.pattern.emm.ExceptionalContingencyTablePatternFactory;
import de.fraunhofer.iais.ocm.core.model.utility.PatternUtilityModel;
import de.fraunhofer.iais.ocm.core.util.Sampling;

public class TargetListProposer {

	private static final double EXPONENTIAL_SCALING_PARAMETER = 1.0;
	public static TargetListProposer INSTANCE = new TargetListProposer();

	private TargetListProposer() {
	}

	public List<Attribute> proposeTargets(DataTable dataTable,
			PatternUtilityModel patternUtilityModel, int number,
			Class<? extends Attribute> typeFilter) {

		List<Attribute> result = new ArrayList<Attribute>();

		Attribute augmentation = null;

		do {
			augmentation = sampleAugmentationElement(dataTable,
					patternUtilityModel, result, typeFilter);
			result.add(augmentation);
		} while (result.size() < number);

		return result;
	}

	private Attribute sampleAugmentationElement(DataTable dataTable,
			PatternUtilityModel patternUtilityModel,
			List<Attribute> prefix, Class<? extends Attribute> typeFilter) {
		List<Double> probabilities = new ArrayList<Double>();

		for (int i = 0; i < dataTable.getAttributes().size(); i++) {
			Attribute target = dataTable.getAttributes().get(i);
			if (target.isId()
					|| prefix.contains(target)
					|| (typeFilter != null && !(target.getClass()
							.equals(typeFilter)))) {
				probabilities.add(0.0);
				continue;
			}
			prefix.add(target);
			ExceptionalModelPattern queryPattern = ExceptionalContingencyTablePatternFactory.INSTANCE
					.newExceptionModel(dataTable, new ArrayList<Proposition>(),
							prefix);
			double weight = patternUtilityModel.score(queryPattern);
			probabilities.add(Math.exp(EXPONENTIAL_SCALING_PARAMETER * weight));
			prefix.remove(prefix.size() - 1);
		}

		// normalization
		double sum = 0;
		for (Double prob : probabilities) {
			sum += prob;
		}
		for (int i = 0; i < probabilities.size(); i++) {
			probabilities.set(i, probabilities.get(i) / sum);
		}

		int augmentationIndex = Sampling
				.exhaustiveSamplingFromWeights(probabilities);

		return dataTable.getAttribute(augmentationIndex);
	}

}
