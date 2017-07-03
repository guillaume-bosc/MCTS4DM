package de.fraunhofer.iais.ocm.core.model.pattern.emm;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Double.parseDouble;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.Set;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.NumericAttribute;


public class GaussianModel extends ProbabilisticModel {

	private double mean;
	private double variance;
	private double min;
	private double max;

	public GaussianModel(Attribute attribute) {
		super(newArrayList(attribute));
		mean = ((NumericAttribute) attribute).getMean();
		variance = ((NumericAttribute) attribute).getVariance();
		min = ((NumericAttribute) attribute).getMin();
		max = ((NumericAttribute) attribute).getMax();
	}

	public GaussianModel(Attribute attribute, Set<Integer> rows) {
		super(newArrayList(attribute), rows);
		mean = getMeanOnRows(attribute);
		getMinMaxVarianceOnRows(attribute);
	}

	private double getMeanOnRows(Attribute attribute) {
		return ((NumericAttribute) attribute).getMeanOnRows(getRows());
	}

	private void getMinMaxVarianceOnRows(Attribute attribute) {
		double result = 0.0;
		if (getRows().size() == 0) {
			variance = 0;
			min = 0;
			max = 0;
			return;
		}
		min = Double.MAX_VALUE;
		max = -Double.MAX_VALUE;
		for (int rowIx : getRows()) {
			double value = parseDouble(attribute.getValues().get(rowIx));
			min = min(min, value);
			max = max(max, value);
			result += (mean - value) * (mean - value);
		}
		variance = result / getRows().size();
	}

	public double getMean() {
		return mean;
	}

	public double getVariance() {
		return variance;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}
}
