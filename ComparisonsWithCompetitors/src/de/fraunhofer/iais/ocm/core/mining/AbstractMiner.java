package de.fraunhofer.iais.ocm.core.mining;

import java.util.Collection;

import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

public abstract class AbstractMiner implements MiningAlgorithm {

	private boolean isStop = false;

	private Collection<Pattern> results;

	public synchronized void setStop(boolean stop) {
		this.isStop = stop;
	}

	public synchronized boolean isStop() {
		return isStop;
	}

	public Collection<Pattern> getResults() throws IllegalStateException {
		if (!isStop()) {
			throw new IllegalStateException(
					"Miner has to be stopped before querying the results");
		}

		return results;
	}

	protected void setResults(Collection<Pattern> results) {
		this.results = results;
	}

}
