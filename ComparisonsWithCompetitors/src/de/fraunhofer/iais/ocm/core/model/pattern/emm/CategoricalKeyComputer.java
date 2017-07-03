package de.fraunhofer.iais.ocm.core.model.pattern.emm;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Set;

import de.fraunhofer.iais.ocm.core.model.data.CategoricalAttribute;

public class CategoricalKeyComputer implements ContingencyTable.KeyComputer {

	private CategoricalAttribute attribute;

	public CategoricalKeyComputer(CategoricalAttribute attribute) {
		this.attribute = attribute;
	}

	@Override
	public String computeKey(String value) {
		return attribute.getName() + "_" + value;
	}

	@Override
	public Set<String> getDistinctKeys() {
		Set<String> keys = newHashSet();
		for (String value : newHashSet(attribute.getValues())) {
			keys.add(attribute.getName() + "_" + value);
		}
		return keys;
	}
}
