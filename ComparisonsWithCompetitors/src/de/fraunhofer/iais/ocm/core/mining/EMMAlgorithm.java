package de.fraunhofer.iais.ocm.core.mining;

import de.fraunhofer.iais.ocm.common.parameter.rangebounder.ExtensionalRangeBounder;
import de.fraunhofer.iais.ocm.core.mining.parameter.ListMiningParameterAnnotation;
import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.pattern.emm.ExceptionalModelPatternFactory;

import java.util.List;

public interface EMMAlgorithm extends MiningAlgorithm {
	
	@ListMiningParameterAnnotation(getDescription = "The attributes according to which subgroups are supposed to stand out",
			getName = "Target Attributes", 
			getRangeBounder = ExtensionalRangeBounder.NON_ID_ATTRIBUTES)
	public void setTargetAttributes(List<Attribute> targets);
	
	public List<Attribute> getTargetAttributes();

	@ListMiningParameterAnnotation(getDescription = "The kind of model which is fitted to target attributes",
			getName = "Model Class", 
			getRangeBounder = ExtensionalRangeBounder.NON_ID_ATTRIBUTES)
	public void setEMPatternFactory(ExceptionalModelPatternFactory factory);

}
