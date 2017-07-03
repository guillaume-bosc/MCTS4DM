package de.fraunhofer.iais.ocm.core.model.utility.modelFactories;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.utility.BatchPrefL1RegUtilityModel;
import de.fraunhofer.iais.ocm.core.model.utility.ModelLearner;
import de.fraunhofer.iais.ocm.core.model.utility.features.AbstractFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.FrequencyFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.ModelDeviationFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.ProductFeature;

public class FrequencyTimesModelDeviationModelFactory implements
		UtilityModelFactory {

	@Override
	public ModelLearner getUtilityModel(DataTable dataTable) {
		return new BatchPrefL1RegUtilityModel(
				addFeatures(dataTable));
	}

	private List<AbstractFeature> addFeatures(DataTable dataTable) {
		List<AbstractFeature> result = new ArrayList<AbstractFeature>();
		ModelDeviationFeature deviation = new ModelDeviationFeature(1.0);
		// AbstractFeature frequency = new PowerFeature(new
		// FrequencyFeature(1.0),
		// .5);
		AbstractFeature frequency = new FrequencyFeature(1.0);
		ProductFeature freqDeviation = new ProductFeature(frequency, deviation,
				1.0);
		result.add(freqDeviation);
		return result;
	}

	@Override
	public String getDescription() {
		return "Model Deviation weight: 1.0";
	}

}
