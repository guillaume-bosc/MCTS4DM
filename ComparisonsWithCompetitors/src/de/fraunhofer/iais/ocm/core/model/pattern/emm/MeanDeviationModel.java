package de.fraunhofer.iais.ocm.core.model.pattern.emm;

import static com.google.common.collect.Lists.newArrayListWithCapacity;

import java.util.List;
import java.util.Set;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.NumericAttribute;


public class MeanDeviationModel extends AbstractModel {

	private static class MeanDeviationDistance implements ModelDistanceFunction {

		@Override
		public double distance(AbstractModel globalModel,
				AbstractModel localModel) {
			if (!(globalModel instanceof MeanDeviationModel && localModel instanceof MeanDeviationModel)) {
				return -1;
			}
			
			double distance = 0;
			List<Double> globalMeans = ((MeanDeviationModel) globalModel)
					.getMean();
			List<Double> localMeans = ((MeanDeviationModel) localModel)
					.getMean();
			for (int i = 0; i < globalMeans.size(); i++) {
//				double maxDistanceToMean = getMaxDistanceToMean((NumericAttribute) globalModel.attributes
//						.get(i));
//				distance += Math.pow(
//						Math.abs(globalMeans.get(i) - localMeans.get(i))
//								/ (maxDistanceToMean), 2);
				double variance = ((NumericAttribute)globalModel.getAttributes().get(i)).getVariance();
				distance += Math.pow(globalMeans.get(i) - localMeans.get(i), 2)
						/ variance;
			}
//			return Math.sqrt(distance / globalMeans.size());
			return Math.sqrt(distance);
		}

		private double getMaxDistanceToMean(NumericAttribute attribute) {
			return Math.max(attribute.getMax() - attribute.getMean(),
					attribute.getMean() - attribute.getMin());
		}
	}

	public static ModelDistanceFunction MEANDEVIATION = new MeanDeviationDistance();

	private List<Double> means;

	public MeanDeviationModel(List<Attribute> attributes) {
		super(attributes);
		means = computeMeans();
	}

	public MeanDeviationModel(List<Attribute> attributes, Set<Integer> rows) {
		super(attributes, rows);
		means = computeMeansOnRows();
	}

	private List<Double> computeMeans() {
		List<Double> means = newArrayListWithCapacity(attributes.size());
		for (Attribute attribute : attributes) {
			if (attribute.isNumeric()) {
				means.add(((NumericAttribute) attribute).getMean());
			}
		}
		return means;
	}

	private List<Double> computeMeansOnRows() {
		List<Double> means = newArrayListWithCapacity(attributes.size());
		for (Attribute attribute : attributes) {
			if (attribute.isNumeric()) {
				means.add(((NumericAttribute) attribute)
						.getMeanOnRows(getRows()));
			}
		}
		return means;
	}

	public List<Double> getMean() {
		return means;
	}
}
