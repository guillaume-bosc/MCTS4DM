package de.fraunhofer.iais.ocm.core.model.utility.modelFactories;

import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.utility.BatchPrefL1RegUtilityModel;
import de.fraunhofer.iais.ocm.core.model.utility.ModelLearner;

public class DefaultModelFactory implements UtilityModelFactory {

	@Override
	public ModelLearner getUtilityModel(DataTable dataTable) {
		return new BatchPrefL1RegUtilityModel(
				BasicFeatureListFactory.getBasicFeatures(dataTable));
	}

	public String getDescription() {
		return "Default Model";
	}
}
