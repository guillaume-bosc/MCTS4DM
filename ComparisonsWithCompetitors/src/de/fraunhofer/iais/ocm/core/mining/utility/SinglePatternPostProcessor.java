package de.fraunhofer.iais.ocm.core.mining.utility;

import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

public interface SinglePatternPostProcessor {

	public Pattern prune(Pattern origin);

	public void setStop(boolean isStop);

}