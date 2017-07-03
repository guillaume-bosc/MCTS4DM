package de.fraunhofer.iais.ocm.core.model.data.propositions;

public class LessThanConstraint implements Constraint {

	private double value;

	public LessThanConstraint(double value) {
		this.value=value;
	}
	
	@Override
	public boolean holds(String value) {
		return Double.parseDouble(value)<this.value;
	}
	
	@Override
	public String toString() {
		return "<"+value;
	}

	@Override
	public String getName() {
		return "less than "+String.valueOf(value);
	}

}
