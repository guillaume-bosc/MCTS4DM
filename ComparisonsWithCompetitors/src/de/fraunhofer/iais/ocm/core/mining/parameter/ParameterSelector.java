package de.fraunhofer.iais.ocm.core.mining.parameter;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.ocm.core.mining.EMMAlgorithm;
import de.fraunhofer.iais.ocm.core.mining.MiningAlgorithm;
import de.fraunhofer.iais.ocm.core.mining.TargetListProposer;
import de.fraunhofer.iais.ocm.core.mining.beamsearch.BeamSearch;
import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.data.NumericAttribute;
import de.fraunhofer.iais.ocm.core.model.pattern.emm.ExceptionalContingencyTablePatternFactory;
import de.fraunhofer.iais.ocm.core.model.pattern.emm.ExceptionalGaussianPatternFactory;
import de.fraunhofer.iais.ocm.core.model.pattern.emm.ExceptionalMeanDeviationPatternFactory;
import de.fraunhofer.iais.ocm.core.model.utility.PatternUtilityModel;

public interface ParameterSelector<T extends MiningAlgorithm> {

	public void setParameter(T algorithm, DataTable dataTable,
			PatternUtilityModel patternUtilityModel);

	public static class SuitableSingleTargetEMFactorySelector implements
			ParameterSelector<EMMAlgorithm> {

		@Override
		public void setParameter(EMMAlgorithm algorithm, DataTable dataTable,
				PatternUtilityModel patternUtilityModel) {
			if (algorithm.getTargetAttributes() == null) {
				throw new IllegalStateException(
						"no target selection has been performed for algorithm");
			}
			if (algorithm.getTargetAttributes().size() != 1) {
				throw new IllegalStateException(
						"must select exactly 1 target attributes for algorithm");
			}
			if (algorithm.getTargetAttributes().get(0).isNumeric()) {
				algorithm
						.setEMPatternFactory(ExceptionalGaussianPatternFactory.INSTANCE);
			} else {
				algorithm
						.setEMPatternFactory(ExceptionalContingencyTablePatternFactory.INSTANCE);
			}
		}

		@Override
		public String toString() {
			return "emFactory=matchSingleTarget()";
		}

	}

	public static class SuitableDoubleTargetEMFactorySelector implements
			ParameterSelector<EMMAlgorithm> {

		@Override
		public void setParameter(EMMAlgorithm algorithm, DataTable dataTable,
				PatternUtilityModel patternUtilityModel) {
			if (algorithm.getTargetAttributes() == null) {
				throw new IllegalStateException(
						"no target selection has been performed for algorithm");
			}
			if (algorithm.getTargetAttributes().size() != 2) {
				throw new IllegalStateException(
						"must select exactly 2 target attributes for algorithm");
			}
			if (algorithm.getTargetAttributes().get(0).isNumeric()
					&& algorithm.getTargetAttributes().get(1).isNumeric()) {
				algorithm
						.setEMPatternFactory(ExceptionalMeanDeviationPatternFactory.INSTANCE);
			} else {
				algorithm
						.setEMPatternFactory(ExceptionalContingencyTablePatternFactory.INSTANCE);
			}
		}

		@Override
		public String toString() {
			return "emFactory=matchDoubleTarget()";
		}

	}

	public static class ExponentialSingleTargetProposer implements
			ParameterSelector<EMMAlgorithm> {

		@Override
		public void setParameter(EMMAlgorithm algorithm, DataTable dataTable,
				PatternUtilityModel patternUtilityModel) {
			List<Attribute> targets = TargetListProposer.INSTANCE
					.proposeTargets(dataTable, patternUtilityModel, 1, null);
			// System.out.println(algorithm+"(targets="+targets+")");
			algorithm.setTargetAttributes(targets);
		}

		@Override
		public String toString() {
			return "targets=expRandomSelect(1)";
		}
	}

	public static class ExponentialTwoTargetsProposer implements
			ParameterSelector<EMMAlgorithm> {

		@Override
		public void setParameter(EMMAlgorithm algorithm, DataTable dataTable,
				PatternUtilityModel patternUtilityModel) {
			algorithm.setTargetAttributes(TargetListProposer.INSTANCE
					.proposeTargets(dataTable, patternUtilityModel, 2, null));
		}

		@Override
		public String toString() {
			return "targets=expRandomSelect(2)";
		}
	}

	public static class ExponentialTwoNumericTargetsProposer implements
			ParameterSelector<EMMAlgorithm> {

		@Override
		public void setParameter(EMMAlgorithm algorithm, DataTable dataTable,
				PatternUtilityModel patternUtilityModel) {
			algorithm.setTargetAttributes(TargetListProposer.INSTANCE
					.proposeTargets(dataTable, patternUtilityModel, 2,
							NumericAttribute.class));
		}

		@Override
		public String toString() {
			return "targets=numExpRandomSelect(2)";
		}
	}

	public static class ContingencyTableEMFactorySelector implements
			ParameterSelector<EMMAlgorithm> {
	
		@Override
		public void setParameter(EMMAlgorithm algorithm, DataTable dataTable,
				PatternUtilityModel patternUtilityModel) {
			if (algorithm.getTargetAttributes() == null) {
				throw new IllegalStateException(
						"no target selection has been performed for algorithm");
			}
			// if (algorithm.getTargetAttributes().size() != 2) {
			// throw new IllegalStateException(
			// "must select exactly 2 target attributes for algorithm");
			// }
			algorithm
					.setEMPatternFactory(ExceptionalContingencyTablePatternFactory.INSTANCE);
		}
	
		@Override
		public String toString() {
			return "emFactory=contingencyTable()";
		}
	
	}

	public static class MeanDeviationEMFactorySelector implements
			ParameterSelector<EMMAlgorithm> {

		@Override
		public void setParameter(EMMAlgorithm algorithm, DataTable dataTable,
				PatternUtilityModel patternUtilityModel) {
			if (algorithm.getTargetAttributes() == null) {
				throw new IllegalStateException(
						"no target selection has been performed for algorithm");
			}
			// if (algorithm.getTargetAttributes().size() != 2) {
			// throw new IllegalStateException(
			// "must select exactly 2 target attributes for algorithm");
			// }
			algorithm
					.setEMPatternFactory(ExceptionalMeanDeviationPatternFactory.INSTANCE);
		}

		@Override
		public String toString() {
			return "emFactory=meanDeviation()";
		}

	}

	public static class FixedTargetsProposer implements
			ParameterSelector<EMMAlgorithm> {

		List<String> targets;

		public FixedTargetsProposer(List<String> targets) {
			this.targets = targets;
		}

		@Override
		public void setParameter(EMMAlgorithm algorithm, DataTable dataTable,
				PatternUtilityModel patternUtilityModel) {
			List<Attribute> fixedAttributes = getAttributes(dataTable);

			algorithm.setTargetAttributes(fixedAttributes);
		}

		private List<Attribute> getAttributes(DataTable dataTable) {
			List<Attribute> targetAttributes = new ArrayList<Attribute>();
			for (String target : targets) {
				targetAttributes.add(dataTable.getAttribute(dataTable
						.getAttributeNames().indexOf(target)));
			}
			return targetAttributes;
		}

		@Override
		public String toString() {
			return "targets=fixed(" + targets + ")";
		}
	}

	public static class BeamWidthSelector implements
			ParameterSelector<BeamSearch> {

		private int beamWidth;

		public BeamWidthSelector(int beamWidth) {
			this.beamWidth = beamWidth;
		}

		@Override
		public void setParameter(BeamSearch algorithm, DataTable dataTable,
				PatternUtilityModel patternUtilityModel) {
			algorithm.setBeamWidth(beamWidth);
		}

		@Override
		public String toString() {
			return "beamWidth=" + beamWidth;
		}
	}
}
