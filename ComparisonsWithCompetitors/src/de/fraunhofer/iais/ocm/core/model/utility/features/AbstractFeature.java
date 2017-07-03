package de.fraunhofer.iais.ocm.core.model.utility.features;

import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;

public abstract class AbstractFeature {

	private String name;
	
	private double defaultValue;

	public AbstractFeature(String description) {
		this(description, 0.0);
	}
	
	public AbstractFeature(String description, double defaultValue) {
		this.name = description;
		this.defaultValue=defaultValue;
	}

	/**
	 * Returns textual description of feature that can, e.g., be displayed in
	 * research view.
	 */
	public String getDescription() {
		return this.name;
	}

	/**
	 * Computes the feature value for a single pattern. Dependency on data table
	 * is not required because per definition feature values are only non-zero
	 * for all quantities that can be computed from information that is shown
	 * with pattern.
	 * 
	 * @param pattern
	 */
	public abstract double value(Pattern pattern);

	/**
	 * Assume 0/1 at the moment
	 * 
	 * @return true if feature is categorical
	 */
	public abstract boolean isCategorical();

	/**
	 * Default value for model coefficient of this feature in a linear model
	 */
	public double getDefaultValue() {
		return this.defaultValue;
	}

}
