package de.fraunhofer.iais.ocm.core.mining.patternsampling;

import de.fraunhofer.iais.ocm.core.mining.MiningAlgorithm;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import edu.uab.consapt.sampling.TwoStepPatternSampler;

public interface DistributionFactory {

	public TwoStepPatternSampler getDistribution(MiningAlgorithm algorithm,
			DataTable dataTable);

}