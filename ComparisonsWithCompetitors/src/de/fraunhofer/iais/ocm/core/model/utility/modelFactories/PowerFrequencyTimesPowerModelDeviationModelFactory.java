package de.fraunhofer.iais.ocm.core.model.utility.modelFactories;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.utility.BatchPrefL1RegUtilityModel;
import de.fraunhofer.iais.ocm.core.model.utility.ModelLearner;
import de.fraunhofer.iais.ocm.core.model.utility.features.AbstractFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.FrequencyFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.ModelDeviationFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.PowerFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.ProductFeature;

public class PowerFrequencyTimesPowerModelDeviationModelFactory implements
		UtilityModelFactory {

	@Override
	public ModelLearner getUtilityModel(DataTable dataTable) {
		ModelLearner utilityModel = new BatchPrefL1RegUtilityModel(
				getFeatures(dataTable));

		return utilityModel;
	}

	private List<AbstractFeature> getFeatures(DataTable dataTable) {
		// deviation features
		List<AbstractFeature> result = new ArrayList<AbstractFeature>();
		ModelDeviationFeature deviation = new ModelDeviationFeature();
		result.add(new ProductFeature(new PowerFeature(deviation, 2),
				new PowerFeature(new FrequencyFeature(), 0.5), 1.0));
		return result;
	}

	@Override
	public String getDescription() {
		return "Model Deviation weight: 1.0";
	}

}
