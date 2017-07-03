package de.fraunhofer.iais.ocm.core.model.pattern.emm;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.data.NumericAttribute;

public class TheilSenLinearRegressionModel extends AbstractModel {
	private Double slope = null;
	private Double intercept = null;
	private List<Double> covariateValues;
	private List<Double> regressandValues;
	public Double getSlope() {
		return this.slope;
	}
	public Double getIntercept() {
		return this.intercept;
	}

	public TheilSenLinearRegressionModel(List<Attribute> attributes)
			throws Exception {
		super(attributes);
		
		estimateParametersOn(attributes.get(0).getDataTable().getDataTable());
	}

	public TheilSenLinearRegressionModel(List<Attribute> attributes,
			Set<Integer> rows) throws Exception {
		super(attributes, rows);
		
		DataTable dataTable = attributes.get(0).getDataTable();
		List<List<String>> fullRows = newArrayList();
		for (int rowIndex : getRows()) {
			fullRows.add(dataTable.getRow(rowIndex));
		}

		estimateParametersOn(fullRows);
	}
	
	public Double predict(Double x) {
		if (this.slope == null || this.intercept == null) {
			return null;
		}
		return this.slope * x + intercept;
	}

	// we approximate values of attributes[1] as a function of attributes[0]
	private void estimateParametersOn(List<List<String>> fullRows) throws Exception {
		ensureNumericalityOfAttributes();
		ensureOnlyTwoAttributes();
		
		covariateValues = new ArrayList<Double>();
		regressandValues = new ArrayList<Double>();
		int covariateAttributeIndex = getAttributes().get(0).getIndexInTable();
		int regressandAttributeIndex = getAttributes().get(1).getIndexInTable();
		for (List<String> fullRow : fullRows) {
			covariateValues.add(Double.parseDouble(fullRow.get(covariateAttributeIndex)));
			regressandValues.add(Double.parseDouble(fullRow.get(regressandAttributeIndex)));
		}
		
		estimateSlope(fullRows);
		estimateIntercept(fullRows);
	}

	private void estimateSlope(List<List<String>> fullRows) {
		List<Double> slopes = new ArrayList<Double>();
		for (int i = 0; i < covariateValues.size() - 1; i++) {
			for (int j = i + 1; j < covariateValues.size(); j++) {
				double covDiff = covariateValues.get(j) - covariateValues.get(i);
				if (covDiff != 0d) {
					slopes.add((regressandValues.get(j) - regressandValues
							.get(i)) / covDiff);
				}
			}
		}
		if (slopes.size() > 0) {
			this.slope = medianOf(slopes);
		}
	}

	private void estimateIntercept(List<List<String>> fullRows) {
		if (this.slope == null) {
			return;
		}
		List<Double> intercepts = new ArrayList<Double>();
		for (int i = 0; i < covariateValues.size(); i++) {
			intercepts.add(regressandValues.get(i) - this.slope * covariateValues.get(i));
		}
		if (intercepts.size() > 0) {
			this.intercept = medianOf(intercepts);
		}
	}

	private void ensureNumericalityOfAttributes() throws Exception {
		if (!(getAttributes().get(0) instanceof NumericAttribute)
				|| !(getAttributes().get(1) instanceof NumericAttribute)) {
			throw new Exception("Both of targets attributes must be numeric!");
		}
	}
	
	private void ensureOnlyTwoAttributes() throws Exception {
		if (2 != getAttributes().size()) {
			throw new Exception("There must be two target attributes only.");
		}
	}
	
	private Double medianOf(List<Double> values) {
		Double median;
		Collections.sort(values);
		if (values.size() % 2 == 0) {
			int ind = values.size() / 2 - 1;
			median = (values.get(ind) + values.get(ind + 1)) / 2.0;
		} else {
			median = values.get(values.size() / 2);
		}
		return median;
	}
}
