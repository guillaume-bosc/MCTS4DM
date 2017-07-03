package de.fraunhofer.iais.ocm.core.model.utility.modelFactories;

import java.util.Random;

import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.utility.BatchPrefL1RegUtilityModel;
import de.fraunhofer.iais.ocm.core.model.utility.LinearModel;
import de.fraunhofer.iais.ocm.core.model.utility.ModelLearner;

public class RandomModelFactory implements UtilityModelFactory {

	private final int expectedNonzeroWeightsNum = 8;

	@Override
	public ModelLearner getUtilityModel(DataTable dataTable) {
		ModelLearner utilityModel = new BatchPrefL1RegUtilityModel(
				BasicFeatureListFactory.getBasicFeatures(dataTable));
		randomizeTheModel(utilityModel);
		return utilityModel;
	}

	private void randomizeTheModel(ModelLearner utilityModel) {
		randomizeTheModel(utilityModel, 0, 2, expectedNonzeroWeightsNum / 3);
		randomizeTheModel(utilityModel, 3, 13, expectedNonzeroWeightsNum / 3);
		randomizeTheModel(utilityModel, 13,
				((LinearModel)utilityModel.getModel()).getWeights().size() - 1,
				expectedNonzeroWeightsNum / 3);
	}

	private void randomizeTheModel(ModelLearner utilityModel,
			int from, int to, int localMass) {
		double weightNonzeroProb = localMass / (double) (to - from + 1);
		Random rand = new Random();
		for (int i = from; i < to; i++) {
			double weight = ((LinearModel)utilityModel.getModel()).getWeights().get(i);
			((LinearModel)utilityModel.getModel()).getWeights().set(i,
					getPerturbedWeight(weight, weightNonzeroProb, rand));
		}
	}

	private double getPerturbedWeight(double weight, double weightNonzeroProb,
			Random rand) {
		// generate a random perturbation in [-1, 1]
		double randPertubation = (rand.nextDouble() - 0.5) * 2;
		if (rand.nextDouble() < weightNonzeroProb) {
			weight += randPertubation;
		}

		return weight;
	}

	public String getDescription() {
		return "Random Model";
	}
}
