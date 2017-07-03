package de.fraunhofer.iais.ocm.core.model.pattern.emm;

import java.util.List;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.data.propositions.Proposition;
import de.fraunhofer.iais.ocm.core.model.pattern.ExceptionalModelPattern;

public class ExceptionalGaussianPatternFactory implements
		ExceptionalModelPatternFactory {

	public static ExceptionalGaussianPatternFactory INSTANCE = new ExceptionalGaussianPatternFactory();

	public ExceptionalGaussianPatternFactory() {
		;
	}

	@Override
	public ExceptionalModelPattern newExceptionModel(DataTable dataTable,
			List<Proposition> description, List<Attribute> targets)
			throws IllegalArgumentException {
		if (targets.size() != 1) {
			throw new IllegalArgumentException(
					"Target attribute for Gaussian subgroup not singleton");
		}
		if (!targets.get(0).isNumeric()) {
			throw new IllegalArgumentException(
					"Target attribute for Gaussian subgroup not numeric");
		}

		return new ExceptionalModelPattern(dataTable, description, targets,
				GaussianModelFactory.INSTANCE, GaussianModel.TOTALVARIATION);
	}
}
