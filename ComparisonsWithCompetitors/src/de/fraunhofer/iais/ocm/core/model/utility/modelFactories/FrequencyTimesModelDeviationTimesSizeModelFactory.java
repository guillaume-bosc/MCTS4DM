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
import de.fraunhofer.iais.ocm.core.model.utility.features.UniqueDescriptorLengthFeature;

public class FrequencyTimesModelDeviationTimesSizeModelFactory implements
		UtilityModelFactory {

	@Override
	public ModelLearner getUtilityModel(DataTable dataTable) {
		return new BatchPrefL1RegUtilityModel(
				addFeatures(dataTable));
	}

	private List<AbstractFeature> addFeatures(DataTable dataTable) {
		List<AbstractFeature> result = new ArrayList<AbstractFeature>();
		ModelDeviationFeature deviation = new ModelDeviationFeature(1.0);
		AbstractFeature frequency = new FrequencyFeature(1.0);
		double power = 1.0 / dataTable.getNumOfNonIDAttrs();
		AbstractFeature lengthFeature = new PowerFeature(
				new UniqueDescriptorLengthFeature(),
 power / 2);
		ProductFeature f1 = new ProductFeature(new ProductFeature(frequency,
				deviation, 1.0), lengthFeature, 1.0);
		result.add(f1);
		return result;
	}

	@Override
	public String getDescription() {
		return "Model Deviation weight: 1.0";
	}

}
