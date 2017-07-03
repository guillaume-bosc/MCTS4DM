package de.fraunhofer.iais.ocm.core.mining.utility;

import java.util.List;

import weka.attributeSelection.PrincipalComponents;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.CategoricalAttribute;

public class PCAEvaluator {

	private Instances transformedData;

	public PCAEvaluator(List<Attribute> targetAttributes) {
		Instances instances = convertToInstances(targetAttributes);
		PrincipalComponents pca = new PrincipalComponents();
		transformedData = null;


		try {
			// pca.setCenterData(true);
			pca.buildEvaluator(instances);
			transformedData = pca.transformedData(instances);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public double getDevFirstDimension(int index) {
		return transformedData.instance(index).value(0);
	}

	public double getDevForDimension(int index, int dimension) {
		if (dimension >= transformedData.instance(index).numValues()) {
			return 0;
		}
		return transformedData.instance(index).value(dimension);
	}

	public int getNumberOfComponents() {
		return transformedData.numAttributes();
	}

	private static Instances convertToInstances(List<Attribute> targetAttributes) {
		Instances instances = new Instances("converted",
				convertTargetAttributes(targetAttributes), targetAttributes
						.get(0).getValues().size());
		for (int i = 0; i < targetAttributes.get(0).getValues().size(); i++) {
			double[] values = new double[targetAttributes.size()];
			for (int j = 0; j < targetAttributes.size(); j++) {
				values[j] = Double.parseDouble(targetAttributes.get(j)
						.getValues().get(i));
			}
			instances.add(new Instance(1., values));
		}
		return instances;
	}

	private static FastVector convertTargetAttributes(
			List<Attribute> targetAttributes) {
		FastVector attributes = new FastVector(targetAttributes.size());
		for (Attribute attribute : targetAttributes) {
			if (attribute.isCategoric()) {
				FastVector values = new FastVector();
				for (String value : ((CategoricalAttribute) attribute)
						.getCategories()) {
					values.addElement(value);
				}
				attributes.addElement(new weka.core.Attribute(attribute
						.getName(), values));
			} else {
				attributes.addElement(new weka.core.Attribute(attribute
						.getName()));
			}
		}
		return attributes;
	}
}
