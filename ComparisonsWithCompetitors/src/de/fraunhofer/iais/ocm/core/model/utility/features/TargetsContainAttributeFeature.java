package de.fraunhofer.iais.ocm.core.model.utility.features;



import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.propositions.Proposition;
import de.fraunhofer.iais.ocm.core.model.pattern.Association;
import de.fraunhofer.iais.ocm.core.model.pattern.ExceptionalModelPattern;
import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

public class TargetsContainAttributeFeature extends AbstractFeature {

	private Attribute attribute;

	public TargetsContainAttributeFeature(Attribute attribute) {
		super("TarHas(" + attribute.getName() + ")");
		this.attribute = attribute;
	}

	@Override
	public double value(Pattern pattern) {
		if (pattern instanceof ExceptionalModelPattern
				&& ((ExceptionalModelPattern) pattern).getTargetAttributes()
						.contains(this.attribute)) {
			return 1.0 / ((ExceptionalModelPattern) pattern)
					.getTargetAttributes().size();
		}
		if (pattern instanceof Association) {
			for (Proposition proposition : pattern.getDescription()) {
				if (proposition.getAttribute() == this.attribute) {
					return 1.0 / pattern.getDescription().size();
				}
			}
		}
		return 0.0;
	}

	@Override
	public boolean isCategorical() {
		return false;
	}
}
