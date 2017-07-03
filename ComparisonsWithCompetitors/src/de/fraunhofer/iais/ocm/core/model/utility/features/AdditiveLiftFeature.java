package de.fraunhofer.iais.ocm.core.model.utility.features;

import de.fraunhofer.iais.ocm.core.model.pattern.Association;
import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

public class AdditiveLiftFeature extends AbstractFeature {

	public AdditiveLiftFeature() {
		super("lift");
	}

	public AdditiveLiftFeature(double defaultValue) {
		super("lift", defaultValue);
	}

	@Override
	public double value(Pattern pattern) {
		if (pattern instanceof Association) {
			return Math.abs(((Association) pattern).getAssociationMeasure());
		} else {
			return 0.0;
		}
	}

	@Override
	public boolean isCategorical() {
		return false;
	}
}
