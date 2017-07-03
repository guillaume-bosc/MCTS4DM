package de.fraunhofer.iais.ocm.core.model.data.propositions;

public class GreaterThanConstraint implements Constraint {

	private double value;

	public GreaterThanConstraint(double value) {
		this.value=value;
	}
	
	@Override
	public boolean holds(String value) {
		return Double.parseDouble(value)>this.value;
	}
	
	@Override
	public String toString() {
		return ">"+value;
	}

	@Override
	public String getName() {
		return "greater than "+String.valueOf(value);
	}

}
