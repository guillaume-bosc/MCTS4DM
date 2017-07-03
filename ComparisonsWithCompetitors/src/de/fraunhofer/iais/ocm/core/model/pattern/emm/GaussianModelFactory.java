package de.fraunhofer.iais.ocm.core.model.pattern.emm;

import java.util.List;
import java.util.Set;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;


public class GaussianModelFactory implements ModelFactory {

	public static GaussianModelFactory INSTANCE = new GaussianModelFactory();

	private GaussianModelFactory() {
		;
	}

	@Override
	public Class<? extends AbstractModel> getModelClass() {
		return GaussianModel.class;
	}

	@Override
	public AbstractModel getModel(List<Attribute> attributes) {
		return new GaussianModel(attributes.get(0));
	}

	@Override
	public AbstractModel getModel(List<Attribute> attributes, Set<Integer> rows) {
		return new GaussianModel(attributes.get(0), rows);
	}

}
