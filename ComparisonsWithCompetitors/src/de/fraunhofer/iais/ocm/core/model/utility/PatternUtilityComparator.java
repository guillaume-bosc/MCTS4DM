package de.fraunhofer.iais.ocm.core.model.utility;

import java.util.Comparator;

import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

class PatternUtilityComparator implements Comparator<Pattern> {

	private final PatternUtilityModel patternUtilityModel;

	public PatternUtilityComparator(PatternUtilityModel patternUtilityModel) {
		this.patternUtilityModel = patternUtilityModel;
	}

	public int compare(Pattern patternBean, Pattern patternBean1) {
		double score1 = patternUtilityModel.score(patternBean);
		double score2 = patternUtilityModel.score(patternBean1);

		return Double.compare(score1, score2);
	}

}