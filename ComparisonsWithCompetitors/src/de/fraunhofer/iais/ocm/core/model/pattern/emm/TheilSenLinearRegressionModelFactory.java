package de.fraunhofer.iais.ocm.core.model.pattern.emm;

import java.util.List;
import java.util.Set;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;

public class TheilSenLinearRegressionModelFactory implements ModelFactory {
	public static TheilSenLinearRegressionModelFactory 
		INSTANCE = new TheilSenLinearRegressionModelFactory();

	private TheilSenLinearRegressionModelFactory() {
		;
	}

	@Override
	public Class<? extends AbstractModel> getModelClass() {
		return TheilSenLinearRegressionModel.class;
	}

	@Override
	public AbstractModel getModel(List<Attribute> attributes) {
		try {
			return new TheilSenLinearRegressionModel(attributes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public AbstractModel getModel(List<Attribute> attributes, Set<Integer> rows) {
		try {
			return new TheilSenLinearRegressionModel(attributes, rows);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
