package de.fraunhofer.iais.ocm.core.model.utility.modelFactories;

import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.utility.ModelLearner;

public interface UtilityModelFactory {

	ModelLearner getUtilityModel(DataTable dataTable);

	String getDescription();

}
