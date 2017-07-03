package de.fraunhofer.iais.ocm.core.model.utility.features;

import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

public class PowerFeature extends AbstractFeature {

	private AbstractFeature baseFeature;
	private AbstractFeature baseFeature2;
	private double power;

	public PowerFeature(AbstractFeature baseFeature, double power) {
		super(baseFeature.getDescription() + "^" + String.valueOf(power));
		this.baseFeature = baseFeature;
		this.power = power;
	}

	public PowerFeature(AbstractFeature baseFeature,
			AbstractFeature baseFeature2) {
		super(baseFeature.getDescription() + "^"
				+ baseFeature2.getDescription());
		this.baseFeature = baseFeature;
		this.baseFeature2 = baseFeature2;
		this.power = -1;
	}

	public PowerFeature(AbstractFeature baseFeature, double power,
			double defaultValue) {
		super(baseFeature.getDescription() + "^" + String.valueOf(power),
				defaultValue);
		this.baseFeature = baseFeature;
		this.power = power;
	}

	@Override
	public double value(Pattern pattern) {
		if (baseFeature2 == null) {
			return Math.pow(this.baseFeature.value(pattern), this.power);
		}
		return Math.pow(this.baseFeature.value(pattern),
				this.baseFeature2.value(pattern));
	}

	@Override
	public boolean isCategorical() {
		return baseFeature.isCategorical();
	}
}
