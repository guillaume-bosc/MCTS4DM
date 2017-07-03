package de.fraunhofer.iais.ocm.core.model.utility.modelFactories;

import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.utility.ModelLearner;
import de.fraunhofer.iais.ocm.core.model.utility.OnlineAddUpdateUtilityModel;

public class OnlineAdditiveModelFactory implements UtilityModelFactory {

	@Override
	public ModelLearner getUtilityModel(DataTable dataTable) {
		ModelLearner utilityModel = new OnlineAddUpdateUtilityModel(
				BasicFeatureListFactory.getBasicFeatures(dataTable));
		return utilityModel;
	}

	@Override
	public String getDescription() {
		return "Online Additive Model";
	}

}
