package de.fraunhofer.iais.ocm.core.model.utility.modelFactories;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.utility.BatchPrefL1RegUtilityModel;
import de.fraunhofer.iais.ocm.core.model.utility.ModelLearner;
import de.fraunhofer.iais.ocm.core.model.utility.features.AbstractFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.AdditiveLiftFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.DescriptorContainsAttributeFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.DescriptorLengthFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.FrequencyFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.GreaterOrEqualFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.LessOrEqualFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.ModelDeviationFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.PowerFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.ProductFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.RelativeShortnessFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.TargetsContainAttributeFeature;

public class SubgroupModelFactory implements UtilityModelFactory {

	@Override
	public ModelLearner getUtilityModel(DataTable dataTable) {
		return new BatchPrefL1RegUtilityModel(getFeatures(dataTable));
	}

	private List<AbstractFeature> getFeatures(DataTable dataTable) {
		List<AbstractFeature> features = new ArrayList<AbstractFeature>();

		// add descriptor features
		for (Attribute attribute : dataTable.getAttributes()) {
			if (attribute.isId())
				continue;
			features.add(new DescriptorContainsAttributeFeature(attribute));
		}

		// add target features and combined target features for target proposal
		for (Attribute attribute : dataTable.getAttributes()) {
			if (attribute.isId())
				continue;
			features.add(new TargetsContainAttributeFeature(attribute));
		}

		// deviation features
		ModelDeviationFeature deviation = new ModelDeviationFeature(1.0);
		features.add(deviation);
		features.add(new ProductFeature(new PowerFeature(
				new FrequencyFeature(), 0.5), deviation, 1.0));
		features.add(new ProductFeature(new FrequencyFeature(), deviation, 1.0));

		// lift features
		AdditiveLiftFeature lift = new AdditiveLiftFeature();
		features.add(lift);
		features.add(new ProductFeature(new PowerFeature(
				new FrequencyFeature(), 0.5), lift));
		features.add(new ProductFeature(new FrequencyFeature(), lift));

		// shortness
		DescriptorLengthFeature length = new DescriptorLengthFeature();
		features.add(new RelativeShortnessFeature());
		features.add(new LessOrEqualFeature(length, 2.0));
		features.add(new LessOrEqualFeature(length, 4.0));

		// frequency
		FrequencyFeature frequency = new FrequencyFeature();
		features.add(new GreaterOrEqualFeature(frequency, 0.05));
		features.add(new GreaterOrEqualFeature(frequency, 0.1));

		return features;
	}

	@Override
	public String getDescription() {
		return "Basic Features with deviation weights: 1.0, 1.0, 1.0;  lift weights: 0.0, 0.0, 0.0";
	}

}
