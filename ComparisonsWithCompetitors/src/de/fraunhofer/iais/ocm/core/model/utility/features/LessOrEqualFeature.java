package de.fraunhofer.iais.ocm.core.model.utility.features;

import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

public class LessOrEqualFeature extends AbstractFeature {

	private double threshold;
	private AbstractFeature baseFeature;

	public LessOrEqualFeature(AbstractFeature baseFeature, double threshold) {
		this(baseFeature, threshold, 0.0);
	}

	public LessOrEqualFeature(AbstractFeature baseFeature, double threshold,
			double defaultValue) {
		super("(" + baseFeature.getDescription() + ")<="
				+ String.valueOf(threshold), defaultValue);
		this.threshold = threshold;
		this.baseFeature = baseFeature;
	}

	@Override
	public double value(Pattern pattern) {
		if (this.baseFeature.value(pattern) <= this.threshold) {
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
