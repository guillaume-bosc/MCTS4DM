package de.fraunhofer.iais.ocm.core.model.pattern.emm;

import java.util.List;
import java.util.Set;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;


public class MeanModelFactory implements ModelFactory {

	public static MeanModelFactory INSTANCE = new MeanModelFactory();

	private MeanModelFactory() {
		;
	}

	@Override
	public Class<? extends AbstractModel> getModelClass() {
		return MeanDeviationModel.class;
	}

	@Override
	public AbstractModel getModel(List<Attribute> attributes) {
		return new MeanDeviationModel(attributes);
	}

	@Override
	public AbstractModel getModel(List<Attribute> attributes, Set<Integer> rows) {
		return new MeanDeviationModel(attributes, rows);
	}

}
