package de.fraunhofer.iais.ocm.core.mining.patternsampling;

import de.fraunhofer.iais.ocm.core.mining.MiningAlgorithm;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import edu.uab.consapt.sampling.TwoStepPatternSampler;
import edu.uab.consapt.sampling.TwoStepPatternSamplerFactory;

public class FrequencyDistributionFactory implements DistributionFactory {

	private int power;

	public FrequencyDistributionFactory(int powerOfFrequency) {
		this.power = powerOfFrequency;
	}

	@Override
	public TwoStepPatternSampler getDistribution(MiningAlgorithm algorithm,
			DataTable dataTable) {
		try {
			return TwoStepPatternSamplerFactory
					.createFreqTimesFreqDistribution(ConsaptUtils
							.createTransactionDBfromDataTable(dataTable),
							power - 1);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		return "P=freq" + ((power > 1) ? "^" + power : "");
	}

}