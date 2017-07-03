package de.fraunhofer.iais.ocm.core.mining.patternsampling;

import de.fraunhofer.iais.ocm.core.mining.MiningAlgorithm;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import edu.uab.consapt.sampling.TwoStepPatternSampler;
import edu.uab.consapt.sampling.TwoStepPatternSamplerFactory;

public class RareDistributionTimesPowerOfFrequencyFactory implements
		DistributionFactory {

	private int power;

	public RareDistributionTimesPowerOfFrequencyFactory(int powerOfFrequency) {
		this.power = powerOfFrequency;
	}

	@Override
	public TwoStepPatternSampler getDistribution(MiningAlgorithm algorithm,
			DataTable dataTable) {
		try {
			return TwoStepPatternSamplerFactory
					.createRareTimesFreqDistribution(ConsaptUtils
							.createTransactionDBfromDataTable(dataTable), power);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		return "P=rare"
				+ ((power > 0) ? ("*freq" + ((power > 1) ? "^" + power : ""))
						: "");
	}

}