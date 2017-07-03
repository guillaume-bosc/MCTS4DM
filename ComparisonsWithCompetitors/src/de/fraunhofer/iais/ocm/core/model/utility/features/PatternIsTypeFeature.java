package de.fraunhofer.iais.ocm.core.model.utility.features;

import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

public class PatternIsTypeFeature extends AbstractFeature {
	
	private Pattern.TYPE type;

	public PatternIsTypeFeature(Pattern.TYPE type) {
		super("patternIs"+type);
		this.type=type;
	}

	@Override
	public double value(Pattern pattern) {
		if (pattern.getType().equals(this.type)) {
			return 1.0;
		}
		return 0.0;
	}

	@Override
	public boolean isCategorical() {
		return true;
	}

}
