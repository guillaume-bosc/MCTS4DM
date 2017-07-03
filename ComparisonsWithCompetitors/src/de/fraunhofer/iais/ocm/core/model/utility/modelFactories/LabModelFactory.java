package de.fraunhofer.iais.ocm.core.model.utility.modelFactories;

import java.util.List;

import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.utility.BatchPrefL1RegUtilityModel;
import de.fraunhofer.iais.ocm.core.model.utility.ModelLearner;
import de.fraunhofer.iais.ocm.core.model.utility.features.AbstractFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.ContingencyTableMattersFeature;

public class LabModelFactory implements UtilityModelFactory {
	@Override
	public ModelLearner getUtilityModel(DataTable dataTable) {
		List<AbstractFeature> features = BasicFeatureListFactory
				.getBasicFeatures(dataTable);
		ContingencyTableMattersFeature feature = new ContingencyTableMattersFeature(
				"-max(totalVariationWithFirstAttribute, totalVariationWithSecondAttribute)",
				0.0);
		features.add(feature);
		return new BatchPrefL1RegUtilityModel(features);
	}

	public String getDescription() {
		return "Lab Model";
	}
}
