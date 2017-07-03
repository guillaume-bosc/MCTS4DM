package de.fraunhofer.iais.ocm.core.model.data.propositions;

public class EqualsConstraint implements Constraint {

	private String value;
	
	public EqualsConstraint(String value) {
		this.value=value;
	}
	
	@Override
	public boolean holds(String queryValue) {
		return queryValue.equals(this.value);
	}
	
	@Override
	public String toString() {
		return "="+value;
	}

	@Override
	public String getName() {
		return value;
	}
	
	

//	@Override
//	public String getConstraintValue() {
//		return value;
//	}
	
	

}
