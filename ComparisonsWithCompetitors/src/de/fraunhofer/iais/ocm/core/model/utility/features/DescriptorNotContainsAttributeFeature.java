package de.fraunhofer.iais.ocm.core.model.utility.features;



import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

public class DescriptorNotContainsAttributeFeature extends AbstractFeature {

	private Attribute attribute;

	public DescriptorNotContainsAttributeFeature(Attribute attribute) {
		super("DscNot("+attribute.getName()+")");
		this.attribute=attribute;
	}

	@Override
	public double value(Pattern pattern) {
		if (pattern.containsAttribute(this.attribute)) {
			return 0.0;
		}
		else {
			return 1.0;
		}
	}
	
    @Override
    public boolean isCategorical() {
        return true;
    }
}
