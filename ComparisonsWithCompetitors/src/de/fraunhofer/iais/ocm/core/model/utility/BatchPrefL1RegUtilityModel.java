package de.fraunhofer.iais.ocm.core.model.utility;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.model.utility.features.AbstractFeature;
import de.fraunhofer.iais.ocm.core.util.Pair;

/**
 * Based on Stochastic methods for l 1-regularized loss minimization by
 * Shalev-Shwartz, Shai and Tewari, Ambuj. This version uses Logistic Loss based
 * on the differences between provided pairwise comparisons
 */
public class BatchPrefL1RegUtilityModel implements ModelLearner {

	private static final int ITERATIONS_FACTOR = 1000;

	private List<Pair<Pattern, Pattern>> trainingData;

	private List<List<Double>> differences;

	private double gamma = 0.01;

	protected LinearModel model;

	// private boolean isCalibrated = false;

	public BatchPrefL1RegUtilityModel(List<AbstractFeature> features) {
		this.model = new LinearModel(features);
		this.trainingData = new ArrayList<Pair<Pattern, Pattern>>();
		this.differences = new ArrayList<List<Double>>();
	}

	public void tellPreference(Pattern superior, Pattern inferior) {
		Pair<Pattern, Pattern> pair = new Pair<Pattern, Pattern>(superior,
				inferior);

		trainingData.add(pair);
		differences.add(getDifference(pair));
	}

	private List<Double> getDifference(Pair<Pattern, Pattern> pair) {
		ArrayList<Double> newDifferenceVector = new ArrayList<Double>(
				((LinearFeatureSpace) this.model.getFeatureSpace())
						.getFeatures().size());

		for (AbstractFeature feature : ((LinearFeatureSpace) this.model
				.getFeatureSpace()).getFeatures()) {
			newDifferenceVector.add(feature.value(pair.getElement0())
					- feature.value(pair.getElement1()));
		}

		return newDifferenceVector;
	}

	private double getScoreForExample(int i) {
		double res = 0.0;

		for (int j = 0; j < model.getWeights().size(); j++) {
			res += model.getWeights().get(j) * differences.get(i).get(j);
		}

		return res;
	}

	private double computePartialDerivative(int coordinate) {
		double res = 0.0;

		for (int i = 0; i < differences.size(); i++) {
			res += differences.get(i).get(coordinate)
					/ (1 + Math.exp(getScoreForExample(i)));
		}

		return -1.0 * res / differences.size();
	}

	public void doUpdate() {
		if (differences.size() == 0) {
			return;
		}

		int maxIterations = model.getWeights().size() * ITERATIONS_FACTOR;

		System.out.println("performing batch optimization with "
				+ String.valueOf(differences.size()) + " examples (max "
				+ String.valueOf(maxIterations) + " iterations)...");

		int t = -1;
		int freeWeights = model.getWeights().size();
		boolean[] freeToChange = new boolean[model.getWeights().size()];
		for (int i = 0; i < model.getWeights().size(); i++) {
			freeToChange[i] = true;
		}

		int skipCounter = 0;
		while (t < maxIterations && freeWeights > 0) {
			t++;
			int coordinate = (int) (Math.random() * model.getWeights().size());

			if (!freeToChange[coordinate]) {
				skipCounter++;
				continue;
			}

			double oldValue = model.getWeights().get(coordinate);
			double g = computePartialDerivative(coordinate);
			double wAfterGradStep = model.getWeights().get(coordinate) - g
					* 4.0;

			// move weight closer to zero to account for L1 regularization
			// set weight to zero if we cross zero while updating

			double defaultValue = ((LinearFeatureSpace) this.model
					.getFeatureSpace()).getFeatures().get(coordinate)
					.getDefaultValue();

			if (wAfterGradStep > (4.0 * gamma) + defaultValue) {
				model.getWeights()
						.set(coordinate, wAfterGradStep - 4.0 * gamma);
			} else if (wAfterGradStep < (-4.0 * gamma) + defaultValue) {
				model.getWeights()
						.set(coordinate, wAfterGradStep + 4.0 * gamma);
			} else {
				model.getWeights().set(coordinate, defaultValue);
			}

			if (model.getWeights().get(coordinate).equals(oldValue)) {
				freeToChange[coordinate] = false;
				freeWeights--;
			} else {
				for (int i = 0; i < model.getWeights().size(); i++) {
					freeToChange[i] = true;
				}
				freeWeights = model.getWeights().size();
			}

		}

		System.out.println("done after " + String.valueOf(t - skipCounter)
				+ " iterations");
	}

	public String toString() {
		return this.getClass().toString();
	}

	public PatternUtilityModel getModel() {
		return model;
	}

	// public void addFeature(AbstractFeature feature) {
	// this.weights.add(initialWeight);
	//
	// if (isCalibrated) {
	// if (feature.isCategorical()) {
	// this.features.add(new CalibratedCategoricalFeature(feature,
	// trainingData));
	// } else {
	// this.features.add(new CalibratedNumericFeature(feature,
	// trainingData));
	// }
	// } else {
	// this.features.add(feature);
	// }
	// }

	// public void setCalibrated(boolean calibrated) {
	// isCalibrated = calibrated;
	// }
}
