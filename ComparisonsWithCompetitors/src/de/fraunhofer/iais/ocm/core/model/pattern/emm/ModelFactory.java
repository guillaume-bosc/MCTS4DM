package de.fraunhofer.iais.ocm.core.model.pattern.emm;

import java.util.List;
import java.util.Set;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;


public interface ModelFactory {

	Class<? extends AbstractModel> getModelClass();

	AbstractModel getModel(List<Attribute> attributes);

	AbstractModel getModel(List<Attribute> attributes, Set<Integer> rows);

}
