package de.fraunhofer.iais.ocm.core.model.utility.features;



import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.propositions.Proposition;
import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

public class DescriptorContainsAttributeFeature extends AbstractFeature {

	private Attribute attribute;

	public DescriptorContainsAttributeFeature(Attribute attribute) {
		super("DescrHasAttr(" + attribute.getName() + ")");
		this.attribute=attribute;
	}

	@Override
	public double value(Pattern pattern) {
		for (Proposition proposition:pattern.getDescription()) {
			if (proposition.getAttribute()==this.attribute) {
				return 1.0 / pattern.getDescription().size();
			}
		}
		return 0.0;
	}

	
	@Override
    public boolean isCategorical() {
        return false;
    }

}
