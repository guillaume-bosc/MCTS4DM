package de.fraunhofer.iais.ocm.core.model.utility.modelFactories;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.model.utility.features.AbstractFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.AdditiveLiftFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.DescriptorContainsAttributeFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.DescriptorLengthFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.FrequencyFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.GreaterOrEqualFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.LessOrEqualFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.ModelDeviationFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.PatternIsTypeFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.PowerFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.ProductFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.RelativeShortnessFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.ScaledFeature;
import de.fraunhofer.iais.ocm.core.model.utility.features.TargetsContainAttributeFeature;

class BasicFeatureListFactory{

	static List<AbstractFeature> getBasicFeatures(DataTable dataTable) {

		List<AbstractFeature> result=new ArrayList<AbstractFeature>();
		
		// pattern type features
		result.add(new PatternIsTypeFeature(Pattern.TYPE.ass));
		result.add(new PatternIsTypeFeature(Pattern.TYPE.sgd));
        result.add(new PatternIsTypeFeature(Pattern.TYPE.emm));

		// deviation features
		result.add(new ModelDeviationFeature(1.0));
		ModelDeviationFeature deviation = new ModelDeviationFeature();
		// uim.addFeature(deviation, 1.);
		result.add(new ProductFeature(new PowerFeature(
				new FrequencyFeature(), 0.5), deviation));
		result.add(new ProductFeature(new FrequencyFeature(), deviation));

		// lift features
		result.add(new AdditiveLiftFeature(1.0));
		AdditiveLiftFeature lift = new AdditiveLiftFeature();
		// uim.addFeature(lift, 1.0);
		result.add(new ProductFeature(new PowerFeature(
				new FrequencyFeature(), 0.5), lift));
		result.add(new ProductFeature(new FrequencyFeature(), lift));

		// shortness
		DescriptorLengthFeature length = new DescriptorLengthFeature();
		result.add(new RelativeShortnessFeature());
		result.add(new LessOrEqualFeature(length, 2.0));
		result.add(new LessOrEqualFeature(length, 4.0, 1.0));

		// frequency
		FrequencyFeature frequency = new FrequencyFeature();
		result.add(new GreaterOrEqualFeature(frequency, 0.05, 1.0));
		result.add(new GreaterOrEqualFeature(frequency, 0.1));

		List<Integer> nonIdAttributes = new ArrayList<Integer>();
		for (Attribute attribute : dataTable.getAttributes()) {
			if (attribute.isId()) {
				continue;
			}
			nonIdAttributes.add(attribute.getIndexInTable());
		}

		// add descriptor features
		for (Attribute attribute : dataTable.getAttributes()) {
			if (attribute.isId())
				continue;
			result.add(new ScaledFeature(
					new DescriptorContainsAttributeFeature(attribute),
					1.0 / 2.0));
//			uim.addFeature(new ScaledFeature(
//					new DescriptorContainsAttributeFeature(attribute),
//					1.0 / dataTable.getNumOfNonIDAttrs()), 0.0);
		}

		// add target features and combined target features for target proposal
		for (Attribute attribute : dataTable.getAttributes()) {
			if (attribute.isId())
				continue;
			result.add(new TargetsContainAttributeFeature(attribute));
		}

		return result;
	}

}
