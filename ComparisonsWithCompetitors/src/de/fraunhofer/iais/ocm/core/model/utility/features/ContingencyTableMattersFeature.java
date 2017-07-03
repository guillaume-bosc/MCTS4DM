package de.fraunhofer.iais.ocm.core.model.utility.features;

import java.util.ArrayList;
import java.util.List;

import javax.activity.InvalidActivityException;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.pattern.ExceptionalModelPattern;
import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.model.pattern.emm.ContingencyTableModel;
import de.fraunhofer.iais.ocm.core.model.pattern.emm.ProbabilisticModel;

public class ContingencyTableMattersFeature extends AbstractFeature {

	public ContingencyTableMattersFeature(String description) {
		super(description);
	}
	
	public ContingencyTableMattersFeature(String description, double defaultValue) {
		super(description, defaultValue);
	}

	@Override
	public double value(Pattern pattern) {
		ExceptionalModelPattern emmPattern = (ExceptionalModelPattern) pattern;
		if (emmPattern.getTargetAttributes().size() != 2) {
			return 0.0;
		}
		double maximumTotalVariationWithOneAttribute = 0.0;
		try {
			for (Attribute targetAttribute : emmPattern.getTargetAttributes()) {
				List<Attribute> listWithOneAttribute = new ArrayList<Attribute>();
				listWithOneAttribute.add(targetAttribute);
				
				ContingencyTableModel globalModelWithTheOnlyAttribute = new ContingencyTableModel(listWithOneAttribute); 
				ContingencyTableModel localModelWithTheOnlyAttribute = new ContingencyTableModel(listWithOneAttribute, emmPattern.getSupportSet());
				double totalDeviation = ProbabilisticModel.TOTALVARIATION.distance(globalModelWithTheOnlyAttribute, localModelWithTheOnlyAttribute);
				if (maximumTotalVariationWithOneAttribute < totalDeviation) {
					maximumTotalVariationWithOneAttribute = totalDeviation;
				}
			}
		} catch (InvalidActivityException e) {
			e.printStackTrace();
		}
		// return negated value of maximumtotalvariationwithoneattr. because 
		// we want to favor the patterns which has minimum maximum_total_variation_with_an_attribute
		return emmPattern.getModelDeviation() - maximumTotalVariationWithOneAttribute;
	}

	@Override
	public boolean isCategorical() {
		return false;
	}

}
