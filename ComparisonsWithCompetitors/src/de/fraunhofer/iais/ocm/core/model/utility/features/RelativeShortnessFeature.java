package de.fraunhofer.iais.ocm.core.model.utility.features;


import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

public class RelativeShortnessFeature extends AbstractFeature {

	public RelativeShortnessFeature() {
		super("relShort");
	}

	@Override
	public double value(Pattern pattern) {
		if (pattern.getDataTable()==null) {return 0.0;}
		int numAttr = pattern.getDataTable().getNumOfNonIDAttrs();
		int patLength = pattern.getDescriptionSize();
		return (numAttr-patLength)/numAttr;
//		return pattern.getFeatureDescriptor().getRelativeSize();
	}


    @Override
    public boolean isCategorical() {
        return false;
    }
}
