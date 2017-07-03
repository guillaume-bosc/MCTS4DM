package de.fraunhofer.iais.ocm.core.model.utility.features;


import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

public class FrequencyFeature extends AbstractFeature {

	public FrequencyFeature() {
		super("frq");
	}

	public FrequencyFeature(double defaultValue) {
		super("frq",defaultValue);
	}
	
	@Override
	public double value(Pattern pattern) {
		return pattern.getFrequency();
	}

    @Override
    public boolean isCategorical() {
        return false;
    }
}
