package de.fraunhofer.iais.ocm.core.model.utility;

import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

public interface ModelLearner {

	/**
	 * Train on difference vectors. Features that are important for making a
	 * decision will have high values in the difference vector, thus will get
	 * higher weights during training, thus new examples that have high values
	 * of those features will get high prediction score.
	 * 
	 * @param superior
	 *            relatively good pattern
	 * @param inferior
	 *            relatively bad pattern
	 */
	public abstract void tellPreference(Pattern superior, Pattern inferior);

	public abstract void doUpdate();
	
	public abstract PatternUtilityModel getModel();

}