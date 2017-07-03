package de.fraunhofer.iais.ocm.core.model.utility;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.model.utility.features.AbstractFeature;

public class LinearModel implements PatternUtilityModel {
	
	private final PatternUtilityComparator PATTERNUTILITY_COMPARATOR = new PatternUtilityComparator(
			this);

	private LinearFeatureSpace featureSpace;

	private List<Double> weights;

	public LinearModel(List<AbstractFeature> features) {
		this.weights=new ArrayList<Double>(features.size());
		this.featureSpace=new LinearFeatureSpace(features);
		for (AbstractFeature feature : features) {
			this.addFeature(feature);
		}
	}
	
	public List<Double> getWeights() {
		return weights;
	}

	@Override
	public double score(Pattern p) {
		double res = 0.0;

		for (int i = 0; i < this.featureSpace.getFeatures().size(); i++) {
			res += this.featureSpace.getFeatures().get(i).value(p)
					* weights.get(i);
		}

		return res;
	}
	
	@Override
	public FeatureSpace getFeatureSpace() {
		return featureSpace;
	}

	@Override
	public Comparator<Pattern> getUtilityComparator() {
		return PATTERNUTILITY_COMPARATOR;
	}

	private void addFeature(AbstractFeature feature) {
		this.weights.add(feature.getDefaultValue());
	}



}
