package de.fraunhofer.iais.ocm.core.model.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class NumericAttribute extends Attribute {

	private class IndexComparator implements Comparator<Integer> {

		private NumericAttribute attribute;

		public IndexComparator(NumericAttribute attribute) {
			this.attribute = attribute;
		}

		public int compare(Integer i, Integer j) {
			if (Double.parseDouble(attribute.getValues().get(i)) < Double
					.parseDouble(attribute.getValues().get(j))) {
				return -1;
			} else if (Double.parseDouble(attribute.getValues().get(i)) > Double
					.parseDouble(attribute.getValues().get(j))) {
				return 1;
			}
			return 0;
		}

	}

	private double min, max, mean, median;

	private List<Integer> sortedIndices;

	private double variance, thirdCentralMoment;

	private double lowerQuartile;

	private double upperQuartile;

	public NumericAttribute(String name, String description,
			List<String> values, int indexInTable, DataTable dataTable) {
		super(name, false, description, values, indexInTable, dataTable);

		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;
		mean = 0.0;
		median = 0.0;

		for (int i = 0; i < values.size(); i++) {
			String value = values.get(i);
			try {
				double d = Double.parseDouble(value);
				if (d < min) {
					min = d;
				}
				if (d > max) {
					max = d;
				}
				mean += d;
			} catch (NumberFormatException nfe) {
				getMissingPositions().add(i);
			}
		}

		mean /= (values.size() - getMissingPositions().size());

		variance = 0.0;
		thirdCentralMoment = 0.0;

		List<Integer> sortedNonMissinRowIndices = new ArrayList<Integer>(
				values.size());
		for (int i = 0; i < values.size(); i++) {
			if (!getMissingPositions().contains(i)) {
				sortedNonMissinRowIndices.add(i);
				variance += (mean - Double.parseDouble(values.get(i)))
						* (mean - Double.parseDouble(values.get(i)))
						/ values.size();
				thirdCentralMoment += (mean - Double.parseDouble(values.get(i)))
						* (mean - Double.parseDouble(values.get(i)))
						* (mean - Double.parseDouble(values.get(i)))
						/ values.size();

			}
		}
		Collections.sort(sortedNonMissinRowIndices, new IndexComparator(this));

		this.sortedIndices = sortedNonMissinRowIndices;

		int lowerHalfPosition = (int) (sortedNonMissinRowIndices.size() / 2);
		if (sortedNonMissinRowIndices.size() % 2 == 0) {
			median = (Double.parseDouble(values.get(sortedNonMissinRowIndices
					.get(lowerHalfPosition))) + Double.parseDouble(values
					.get(sortedNonMissinRowIndices.get(lowerHalfPosition + 1)))) / 2.0;
		} else {
			median = Double.parseDouble(values.get(sortedNonMissinRowIndices
					.get(lowerHalfPosition + 1)));
		}

		int lowerQuarterPosition = (int) (sortedNonMissinRowIndices.size() / 4);
		if (sortedNonMissinRowIndices.size() % 4 == 0) {
			lowerQuartile = (Double.parseDouble(values
					.get(sortedNonMissinRowIndices.get(lowerQuarterPosition))) + Double
					.parseDouble(values.get(sortedNonMissinRowIndices
							.get(lowerQuarterPosition + 1)))) / 2.0;
		} else {
			lowerQuartile = Double.parseDouble(values
					.get(sortedNonMissinRowIndices
							.get(lowerQuarterPosition + 1)));
		}

		int upperQuarterPosition = (int) (3 * sortedNonMissinRowIndices.size() / 4);
		if ((3 * sortedNonMissinRowIndices.size()) % 4 == 0) {
			upperQuartile = (Double.parseDouble(values
					.get(sortedNonMissinRowIndices.get(upperQuarterPosition))) + Double
					.parseDouble(values.get(sortedNonMissinRowIndices
							.get(upperQuarterPosition + 1)))) / 2.0;
		} else {
			upperQuartile = Double.parseDouble(values
					.get(sortedNonMissinRowIndices
							.get(upperQuarterPosition + 1)));
		}

		for (Integer i : getMissingPositions()) {
			values.set(i, String.valueOf(median));
		}
	}

	public double getMean() {
		return mean;
	}

	public double getMedian() {
		return median;
	}

	public double getMax() {
		return max;
	}

	public double getMin() {
		return min;
	}

	public double getVariance() {
		return variance;
	}

	public double getStandardDeviation() {
		return Math.sqrt(variance);
	}

	public double getSkewness() {
		return thirdCentralMoment / Math.pow(getStandardDeviation(), 3);
	}

	public List<Integer> getSortedNonMissingRowIndices() {
		return sortedIndices;
	}

	public double getLowerQuartile() {
		return lowerQuartile;
	}

	public double getUpperQuartile() {
		return upperQuartile;
	}

	public double getMeanOnRows(Set<Integer> rowSet) {
		double result = 0.0;
		if (rowSet.size() == 0) {
			return 0.0;
		}
		for (int rowIndex : rowSet) {
			result += Double.parseDouble(getValues().get(rowIndex));
		}
		return result / rowSet.size();
	}

	public double getMedianOnRows(Set<Integer> rowSet) {
		double median;
		List<Double> _values = new ArrayList<Double>();
		if (rowSet.size() == 0) {
			return 0.0;
		}
		for (int rowIndex : rowSet) {
			_values.add(Double.parseDouble(getValues().get(rowIndex)));
		}
		Collections.sort(_values);
		if (_values.size() % 2 == 0) {
			int ind = _values.size() / 2 - 1;
			median = (_values.get(ind) + _values.get(ind + 1)) / 2.0;
		} else {
			median = _values.get(_values.size() / 2);
		}
		return median;
	}

	@Override
	public String getStatistic() {
		String result = "";
		result = "Maximum: " + String.format(Locale.ENGLISH, "%.4f", max)
				+ "\r\n";
		result += "Lwr. Qrt.: "
				+ String.format(Locale.ENGLISH, "%.4f", lowerQuartile) + "\r\n";
		result += "Median: " + String.format(Locale.ENGLISH, "%.4f", median)
				+ "\r\n";
		result += "Upr. Qrt.: "
				+ String.format(Locale.ENGLISH, "%.4f", upperQuartile) + "\r\n";
		result += "Minimum: " + String.format(Locale.ENGLISH, "%.4f", min)
				+ "\r\n";
		result += "Mean: " + String.format(Locale.ENGLISH, "%.4f", mean)
				+ "\r\n";
		result += "Standard deviation: "
				+ String.format(Locale.ENGLISH, "%.4f", getStandardDeviation())
				+ "\r\n";
		result += "Variance: "
				+ String.format(Locale.ENGLISH, "%.4f", variance) + "\r\n";
		result += "Skew: "
				+ String.format(Locale.ENGLISH, "%.4f", getSkewness()) + "\r\n";

		return result;
	}
}
