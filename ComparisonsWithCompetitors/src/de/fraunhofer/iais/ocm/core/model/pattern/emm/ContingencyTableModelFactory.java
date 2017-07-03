package de.fraunhofer.iais.ocm.core.model.pattern.emm;

import java.util.List;
import java.util.Set;

import javax.activity.InvalidActivityException;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;


public class ContingencyTableModelFactory implements ModelFactory {

	public static ContingencyTableModelFactory INSTANCE = new ContingencyTableModelFactory();

	private ContingencyTableModelFactory() {
		;
	}

	@Override
	public Class<? extends AbstractModel> getModelClass() {
		return ProbabilisticModel.class;
	}

	@Override
	public AbstractModel getModel(List<Attribute> attributes) {
		try {
			return new ContingencyTableModel(attributes);
		} catch (InvalidActivityException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public AbstractModel getModel(List<Attribute> attributes, Set<Integer> rows) {
		try {
			return new ContingencyTableModel(attributes, rows);
		} catch (InvalidActivityException e) {
			e.printStackTrace();
		}
		return null;
	}
}
