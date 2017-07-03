package de.fraunhofer.iais.ocm.core.model.utility.features;


import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

public class ScaledFeature extends AbstractFeature {

	private double scaleFactor;
	private AbstractFeature baseFeature;

	public ScaledFeature(AbstractFeature baseFeature, double scaleFactor) {
		super(String.valueOf(scaleFactor)+"*"+baseFeature.getDescription());
		this.scaleFactor=scaleFactor;
		this.baseFeature=baseFeature;
	}

	@Override
	public double value(Pattern pattern) {
		return scaleFactor*baseFeature.value(pattern);	
	}

    @Override
    public boolean isCategorical() {
        return baseFeature.isCategorical();
    }
}
