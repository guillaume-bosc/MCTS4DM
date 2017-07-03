package de.fraunhofer.iais.ocm.core.mining.patternsampling;

import de.fraunhofer.iais.ocm.core.mining.MiningAlgorithm;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import edu.uab.consapt.sampling.TwoStepPatternSampler;
import edu.uab.consapt.sampling.TwoStepPatternSamplerFactory;

public class AreaDistributionTimesPowerOfFrequencyFactory implements
		DistributionFactory {

	private int power;

	public AreaDistributionTimesPowerOfFrequencyFactory(int powerOfFrequency) {
		this.power = powerOfFrequency;
	}

	@Override
	public TwoStepPatternSampler getDistribution(MiningAlgorithm algorithm,
			DataTable dataTable) {
		try {
			return TwoStepPatternSamplerFactory
					.createAreaTimesFreqDistribution(ConsaptUtils
							.createTransactionDBfromDataTable(dataTable), power);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		return "P=area"
				+ ((power > 0) ? ("*freq" + ((power > 1) ? "^" + power : ""))
						: "");
	}

}