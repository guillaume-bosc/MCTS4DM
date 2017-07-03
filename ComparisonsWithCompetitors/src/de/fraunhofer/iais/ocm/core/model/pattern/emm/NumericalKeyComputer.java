package de.fraunhofer.iais.ocm.core.model.pattern.emm;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Double.parseDouble;

import java.util.Set;

import de.fraunhofer.iais.ocm.core.model.data.NumericAttribute;

public class NumericalKeyComputer implements ContingencyTable.KeyComputer {


	private double splitValue;

	private String lower;
	private String upper;

	public NumericalKeyComputer(NumericAttribute attribute) {
		splitValue = attribute.getMedian();
		lower = attribute.getName() + "_" + "lower";
		upper = attribute.getName() + "_" + "upper";
	}

	public String computeKey(String value) {
		if (parseDouble(value) < splitValue) {
			return lower;
		}
		return upper;
	}

	@Override
	public Set<String> getDistinctKeys() {
		return newHashSet(lower, upper);
	}
}
