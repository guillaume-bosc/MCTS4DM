package de.fraunhofer.iais.ocm.core.model.pattern.emm;

import java.util.List;
import java.util.Set;

import org.apache.commons.math3.special.Erf;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;

public abstract class ProbabilisticModel extends AbstractModel {

	private static class TotalVariationDistance implements
			ModelDistanceFunction {

		@Override
		public double distance(AbstractModel globalModel,
				AbstractModel localModel) {
			if (globalModel instanceof ContingencyTableModel
					&& localModel instanceof ContingencyTableModel) {
				return contingencyTableDistance(
						(ContingencyTableModel) globalModel,
						(ContingencyTableModel) localModel);
			} else if (globalModel instanceof GaussianModel
					&& localModel instanceof GaussianModel) {
				return gaussianDistance((GaussianModel) globalModel,
						(GaussianModel) localModel);
			}
			return 0;
		}

		private double gaussianDistance(GaussianModel globalModel,
				GaussianModel localModel) {
			//TODO: check formula
			return Math.abs(Erf.erf((localModel.getMean() - globalModel
					.getMean())
					/ (2 * Math.sqrt(2 * globalModel.getVariance()))));
		}

		private double contingencyTableDistance(
				ContingencyTableModel globalModel,
				ContingencyTableModel localModel) {
			ContingencyTable ct1 = globalModel.getProbabilities();
			ContingencyTable ct2 = localModel.getProbabilities();

			double absoluteDifference = 0.0;

			for (ContingencyTableCellKey key : ct1.getKeys()) {
				absoluteDifference += Math.abs(ct1.getNormalizedValue(key)
						- ct2.getNormalizedValue(key));
			}

			return absoluteDifference / 2.0;
		}
	}

	public static ModelDistanceFunction TOTALVARIATION = new TotalVariationDistance();

	public ProbabilisticModel(List<Attribute> attributes) {
		super(attributes);
	}

	public ProbabilisticModel(List<Attribute> attributes, Set<Integer> rows) {
		super(attributes, rows);
	}
}
