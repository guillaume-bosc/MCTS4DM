package de.fraunhofer.iais.ocm.core.model.utility.features;

import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

public class LinearFeature extends AbstractFeature {

	private AbstractFeature factor1;

	private AbstractFeature factor2;

	public LinearFeature(AbstractFeature factor1, AbstractFeature factor2) {
		super(factor1.getDescription() + "+" + factor2.getDescription());
		this.factor1 = factor1;
		this.factor2 = factor2;
	}

	public LinearFeature(AbstractFeature factor1, AbstractFeature factor2,
			double defaultValue) {
		super(factor1.getDescription() + "+" + factor2.getDescription(),
				defaultValue);
		this.factor1 = factor1;
		this.factor2 = factor2;
	}

	@Override
	public double value(Pattern pattern) {
		return factor1.value(pattern) + factor2.value(pattern);
	}

	@Override
	public boolean isCategorical() {
		if (!factor1.isCategorical() || !factor2.isCategorical()) {
			return false;
		}

		return true;
	}

}
