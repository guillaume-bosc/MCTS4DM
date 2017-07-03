package de.fraunhofer.iais.ocm.core.mining.patternsampling;

import de.fraunhofer.iais.ocm.core.mining.EMMAlgorithm;
import de.fraunhofer.iais.ocm.core.mining.MiningAlgorithm;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import edu.uab.consapt.sampling.TwoStepPatternSampler;
import edu.uab.consapt.sampling.TwoStepPatternSamplerFactory;
import mime.plain.weighting.PosNegDbInterface;

public class DiscriminativityDistributionFactory implements DistributionFactory {

	private int powerOfFrequency;
	private int powerOfPosFrequency;
	private int powerOfNegFrequency;

	public DiscriminativityDistributionFactory(int powerOfFrequency, int powerOfPosFrequency, int powerOfNegFrequency) {
		this.powerOfFrequency = powerOfFrequency;
		this.powerOfPosFrequency = powerOfPosFrequency;
		this.powerOfNegFrequency = powerOfNegFrequency;
	}

	@Override
	public TwoStepPatternSampler getDistribution(MiningAlgorithm algorithm, DataTable dataTable) {

		if (!(algorithm instanceof EMMAlgorithm)) {
			throw new IllegalArgumentException("can only build sampling distribution for EMM algorithm");
		}
		EMMAlgorithm emmAlgorithm = (EMMAlgorithm) algorithm;

		if (emmAlgorithm.getTargetAttributes().size() < 1) {
			throw new IllegalArgumentException(
					"can only build sampling distribution for EMM algorithm with more one or more targets selected");
		}

		// if (emmAlgorithm.getTargetAttributes().size() != 1) {
		// throw new IllegalArgumentException(
		// "can only build sampling distribution for EMM algorithm with exactly
		// one target selected");
		// }

		try {
			if (!(algorithm instanceof ExceptionalModelSampler)) {
				throw new IllegalArgumentException("can only build sampling distribution for EMM samplers");
			}
			PosNegDbInterface db = ((ExceptionalModelSampler) algorithm).getPosNegDatabase(dataTable);

			return TwoStepPatternSamplerFactory.createDiscrTimesFreqDistribution(db, this.powerOfFrequency,
					this.powerOfPosFrequency - 1, this.powerOfNegFrequency - 1);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		return "P=" + ((this.powerOfPosFrequency > 0) ? "pfreq" : "")
				+ ((this.powerOfPosFrequency > 1) ? "^" + this.powerOfPosFrequency : "")
				+ ((this.powerOfNegFrequency > 0) ? "(1-nfreq)" : "")
				+ ((this.powerOfNegFrequency > 1) ? "^" + this.powerOfNegFrequency : "")
				+ ((this.powerOfFrequency > 0) ? "freq" : "")
				+ ((this.powerOfFrequency > 1) ? "^" + this.powerOfFrequency : "");
	}
}
