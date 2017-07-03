package de.fraunhofer.iais.ocm.core.model.pattern.emm;

import java.util.List;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.data.propositions.Proposition;
import de.fraunhofer.iais.ocm.core.model.pattern.ExceptionalModelPattern;

public class ExceptionalLinearRegressionPatternFactory implements
		ExceptionalModelPatternFactory {

	public static ExceptionalLinearRegressionPatternFactory INSTANCE = 
			new ExceptionalLinearRegressionPatternFactory();
	
	private ExceptionalLinearRegressionPatternFactory() {
		;
	}

	@Override
	public ExceptionalModelPattern newExceptionModel(DataTable dataTable,
			List<Proposition> description, List<Attribute> targets) {
		return new ExceptionalModelPattern(dataTable, description, targets,
				TheilSenLinearRegressionModelFactory.INSTANCE,
				new TheilSenLinearRegressionModelDistanceFunction());
	}

}
