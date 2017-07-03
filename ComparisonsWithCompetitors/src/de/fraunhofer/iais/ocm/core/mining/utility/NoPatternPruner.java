package de.fraunhofer.iais.ocm.core.mining.utility;

import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

public class NoPatternPruner implements SinglePatternPostProcessor {
	

	@Override
	public Pattern prune(Pattern origin) {
		return origin;
	}
	
	public void setStop(boolean isStop) {
	}

	@Override
	public String toString() {
		return "NoPruner";
	}

}
