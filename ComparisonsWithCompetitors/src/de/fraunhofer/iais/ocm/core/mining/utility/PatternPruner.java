package de.fraunhofer.iais.ocm.core.mining.utility;

import java.util.Comparator;
import java.util.List;

import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.util.Sampling;

public class PatternPruner implements SinglePatternPostProcessor {
	
	private Comparator<Pattern> condition;
	private boolean isStop;

	public PatternPruner(Comparator<Pattern> condition) {
		this.condition=condition;
	}
	
	@Override
	public Pattern prune(Pattern origin) {
		Pattern current = origin;
		List<Integer> priorityList = Sampling.getPermutation(origin
				.getDescription().size());
		for (Integer index : priorityList) {
			if (isStop) {
				return current;
			}
			Pattern candidate = current.generateGeneralization(origin
					.getDescription().get(index));
			if (candidate.getDescriptionSize() == 0) {
				continue;
			}
			if (condition.compare(current, candidate) < 0) {
				current = candidate;
			}
		}
		return current;
	}

	public void setStop(boolean isStop) {
		this.isStop = isStop;
	}

	// public Pattern prune(Pattern origin) {
	// isStop = false;
	// Pattern current=origin;
	// Pattern tmp = origin;
	// List<Integer>
	// priorityList=Sampling.getPermutation(origin.getDescription().size());
	// for (Integer index: priorityList) {
	// if (isStop) {
	// break;
	// }
	// Pattern candidate = tmp.generateGeneralization(origin
	// .getDescription().get(index));
	// if (candidate.getDescriptionSize() == 0) {
	// continue;
	// }
	// if (condition.compare(current, candidate) <= 0) {
	// current=candidate;
	// tmp = candidate;
	// } else if (condition.compare(current, candidate) == 0) {
	// tmp = candidate;
	// }
	// }
	// return current;
	// }
	
	@Override
	public String toString() {
		return "RandomPruner("+condition+")";
	}

}
