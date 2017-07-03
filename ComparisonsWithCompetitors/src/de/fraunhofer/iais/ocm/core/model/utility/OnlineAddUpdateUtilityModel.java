package de.fraunhofer.iais.ocm.core.model.utility;

import java.util.List;

import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.model.utility.features.AbstractFeature;

public class OnlineAddUpdateUtilityModel implements ModelLearner {

	private int t = 1;
	
	private LinearModel model;

	public OnlineAddUpdateUtilityModel(List<AbstractFeature> features) {
		this.model = new LinearModel(features);
	}

	@Override
	public void tellPreference(Pattern superior, Pattern inferior) {
		double theta = 1 / (2. * Math.sqrt(Math.pow(2.,
				Math.floor(Math.log(t++) / Math.log(2.0)))));

		List<AbstractFeature> features = ((LinearFeatureSpace) this.model
				.getFeatureSpace()).getFeatures();
		for (int i = 0; i < features.size(); i++) {
			model.getWeights().set(
					i,
					model.getWeights().get(i)
							+ theta
							* (features.get(i).value(superior) - features
									.get(i).value(inferior)));
		}
	}

	@Override
	public void doUpdate() {
		; // updates are already performed online after each received preference
	}

	public String toString() {
		return this.getClass().toString();
	}

	public PatternUtilityModel getModel() {
		return model;
	}

}
