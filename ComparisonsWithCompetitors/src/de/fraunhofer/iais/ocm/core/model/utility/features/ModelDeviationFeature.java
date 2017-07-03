package de.fraunhofer.iais.ocm.core.model.utility.features;


import de.fraunhofer.iais.ocm.core.model.pattern.ExceptionalModelPattern;
import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

public class ModelDeviationFeature extends AbstractFeature {

	public ModelDeviationFeature() {
		super("modDev");
	}
	
	public ModelDeviationFeature(double defaultValue) {
		super("modDev",defaultValue);
	}

	@Override
	public double value(Pattern pattern) {
		if (pattern instanceof ExceptionalModelPattern) {
			return ((ExceptionalModelPattern) pattern).getModelDeviation();
		}
		return 0;
	}

	@Override
	public boolean isCategorical() {
		return false;
	}
}
