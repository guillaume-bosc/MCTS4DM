package de.fraunhofer.iais.ocm.core.mining.patternsampling;

import java.util.List;

import mime.plain.weighting.PosNegDbInterface;
import mime.plain.weighting.PosNegTransactionDb;
import de.fraunhofer.iais.ocm.core.mining.EMMAlgorithm;
import de.fraunhofer.iais.ocm.core.mining.MiningAlgorithm;
import de.fraunhofer.iais.ocm.core.mining.utility.RowWeightComputer;
import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import edu.uab.consapt.sampling.TwoStepPatternSampler;
import edu.uab.consapt.sampling.TwoStepPatternSamplerFactory;

public class WeightedDiscriminativityDistributionFactory implements
		DistributionFactory {

	private int powerOfFrequency;
	private int powerOfPosFrequency;
	private int powerOfNegFrequency;

	private RowWeightComputer rowWeightComputer;

	public WeightedDiscriminativityDistributionFactory(int powerOfFrequency,
			int powerOfPosFrequency, int powerOfNegFrequency) {
		this.powerOfFrequency = powerOfFrequency;
		this.powerOfPosFrequency = powerOfPosFrequency;
		this.powerOfNegFrequency = powerOfNegFrequency;
	}

	public void setRowWeightComputer(RowWeightComputer transactionWeightComputer) {
		this.rowWeightComputer = transactionWeightComputer;
	}

	@Override
	public TwoStepPatternSampler getDistribution(MiningAlgorithm algorithm,
			DataTable dataTable) {

		if (!(algorithm instanceof EMMAlgorithm)) {
			throw new IllegalArgumentException(
					"can only build sampling distribution for EMM algorithm");
		}
		EMMAlgorithm emmAlgorithm = (EMMAlgorithm) algorithm;

		List<Attribute> targetAttributes = emmAlgorithm.getTargetAttributes();

		if (targetAttributes.size() < 1) {
			throw new IllegalArgumentException(
					"can only build sampling distribution for EMM algorithm with more one or more targets selected");
		}

		try {
			if (!(algorithm instanceof ExceptionalModelSampler)) {
				throw new IllegalArgumentException(
						"can only build sampling distribution for EMM samplers");
			}
			PosNegDbInterface db = ((ExceptionalModelSampler) algorithm)
					.getPosNegDatabase(dataTable);
			return TwoStepPatternSamplerFactory
					.createWeightedDiscrTimesFreqDistribution(db,
							powerOfFrequency, powerOfPosFrequency - 1,
							powerOfNegFrequency - 1, rowWeightComputer
									.getRowWeights(targetAttributes,
											(PosNegTransactionDb) db));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		return "Weighted P=" + ((powerOfPosFrequency > 0) ? "pfreq" : "")
				+ ((powerOfPosFrequency > 1) ? "^" + powerOfPosFrequency : "")
				+ ((powerOfNegFrequency > 0) ? "(1-nfreq)" : "")
				+ ((powerOfNegFrequency > 1) ? "^" + powerOfNegFrequency : "")
				+ ((powerOfFrequency > 0) ? "freq" : "")
				+ ((powerOfFrequency > 1) ? "^" + powerOfFrequency : "");
	}
}