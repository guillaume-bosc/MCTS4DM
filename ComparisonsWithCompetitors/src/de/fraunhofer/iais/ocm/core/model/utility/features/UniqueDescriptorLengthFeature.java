package de.fraunhofer.iais.ocm.core.model.utility.features;


import java.util.HashSet;
import java.util.Set;

import de.fraunhofer.iais.ocm.core.model.data.propositions.Proposition;
import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

public class UniqueDescriptorLengthFeature extends AbstractFeature {

	public UniqueDescriptorLengthFeature() {
		super("UniqDescrLength");
	}

	@Override
	public double value(Pattern pattern) {
		Set<String> p = new HashSet<String>();
		for (Proposition pro : pattern.getDescription()) {
			p.add(pro.getAttribute().getName());
		}
		return p.size();
	}

    @Override
    public boolean isCategorical() {
        return false;
    }
}
