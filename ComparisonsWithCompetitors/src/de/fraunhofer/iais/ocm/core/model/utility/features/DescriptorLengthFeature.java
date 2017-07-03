package de.fraunhofer.iais.ocm.core.model.utility.features;


import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

public class DescriptorLengthFeature extends AbstractFeature {

	public DescriptorLengthFeature() {
		super("DescrLength");
	}

	@Override
	public double value(Pattern pattern) {
		return pattern.getDescriptionSize();
	}

    @Override
    public boolean isCategorical() {
        return false;
    }
}
