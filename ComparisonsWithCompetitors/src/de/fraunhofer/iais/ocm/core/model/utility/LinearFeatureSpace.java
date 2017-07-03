package de.fraunhofer.iais.ocm.core.model.utility;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.model.utility.features.AbstractFeature;
import de.fraunhofer.iais.ocm.core.util.Miscellaneous;

public class LinearFeatureSpace implements FeatureSpace {
	
	private List<AbstractFeature> features;

	public LinearFeatureSpace(List<AbstractFeature> features) {
		this.features=features;
	}
	
	public List<AbstractFeature> getFeatures() {
		return features;
	}

	private List<Double> getFeatureValues(Pattern p) {
		List<Double> values = new ArrayList<Double>(features.size());

		for (AbstractFeature feature : features) {
			values.add(feature.value(p));
		}

		return values;
	}

	@Override
	public double cosine(Pattern p1, Pattern p2) {
		return Miscellaneous.cosineSimilarity(getFeatureValues(p1),
				getFeatureValues(p2));
	}

	@Override
	public double distance(Pattern a, Pattern b) {
		double aSquaredNorm = 0.0;
		double bSquaredNorm = 0.0;
		double innerProd = 0.0;
		double a_i, b_i;

		for (AbstractFeature feature : features) {
			a_i = feature.value(a);
			b_i = feature.value(b);

			aSquaredNorm += a_i * a_i;
			bSquaredNorm += b_i * b_i;
			innerProd += a_i * b_i;
		}

		return Math.sqrt(aSquaredNorm + bSquaredNorm - 2 * innerProd);
	}

}
