package de.fraunhofer.iais.ocm.core.model.utility.features;

import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

public class GreaterOrEqualFeature extends AbstractFeature {

	private double threshold;
	
	private AbstractFeature baseFeature;

	public GreaterOrEqualFeature(AbstractFeature baseFeature, double threshold) {
		super("(" + baseFeature.getDescription() + ")>="
				+ String.valueOf(threshold));
		this.threshold = threshold;
		this.baseFeature = baseFeature;
	}

	public GreaterOrEqualFeature(AbstractFeature baseFeature, double threshold,
			double defaultValue) {
		super("(" + baseFeature.getDescription() + ")>="
				+ String.valueOf(threshold),defaultValue);
		this.threshold = threshold;
		this.baseFeature = baseFeature;
	}

	@Override
	public double value(Pattern pattern) {
		if (this.baseFeature.value(pattern) >= this.threshold) {
			return 1.0;
		} else {
			return 0.0;
		}
	}

	@Override
	public boolean isCategorical() {
		return true;
	}

}
