package de.fraunhofer.iais.ocm.core.model.data.propositions;

public interface Constraint {

	public abstract boolean holds(String value);
	
	public abstract String getName();
	
}
