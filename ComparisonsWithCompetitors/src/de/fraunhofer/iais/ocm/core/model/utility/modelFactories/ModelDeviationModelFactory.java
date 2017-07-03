package de.fraunhofer.iais.ocm.core.model.utility.modelFactories;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.utility.BatchPrefL1RegUtilityModel;
import de.fraunhofer.iais.ocm.core.model.utility.ModelLearner;
import de.fraunhofer.iais.ocm.core.model.utility.features.AbstractFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.ModelDeviationFeature;

public class ModelDeviationModelFactory implements UtilityModelFactory {

	@Override
	public ModelLearner getUtilityModel(DataTable dataTable) {
		ModelLearner utilityModel = new BatchPrefL1RegUtilityModel(addFeatures(dataTable));
		return utilityModel;
	}

	private List<AbstractFeature> addFeatures(DataTable dataTable) {
		List<AbstractFeature> result=new ArrayList<AbstractFeature>();
		ModelDeviationFeature deviation = new ModelDeviationFeature(1.0);
		result.add(deviation);
		return result;
	}

	@Override
	public String getDescription() {
		return "Model Deviation weight: 1.0";
	}

}
