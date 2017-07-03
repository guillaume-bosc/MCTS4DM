package de.fraunhofer.iais.ocm.core.mining.patternsampling;

import de.fraunhofer.iais.ocm.core.mining.AbstractMiner;
import edu.uab.consapt.sampling.TwoStepPatternSampler;

public abstract class ConsaptBasedSamplingMiner extends AbstractMiner {

	protected TwoStepPatternSampler sampler;

	public void setSampler(TwoStepPatternSampler sampler) {
		this.sampler = sampler;
	}

	@Override
	public void setStop(boolean isStop) {
		super.setStop(isStop);
		if (this.sampler != null) {
			this.sampler.setStop(isStop);
		}
	}

	protected int getMaxNumResults() {
		return 100;
	}
}