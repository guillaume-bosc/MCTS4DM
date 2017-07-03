package de.fraunhofer.iais.ocm.core.model.data.propositions;

import java.util.HashSet;
import java.util.Set;


import de.fraunhofer.iais.ocm.core.model.data.Attribute;

public class Proposition {

	private final Attribute attribute;

	private final Constraint constraint;

	private final int indexInStore;
	
	private Set<Integer> supportSet;

	public Proposition(Attribute attribute, Constraint constraint,
			int indexInStore) {
		this.attribute = attribute;
		this.constraint = constraint;
		this.indexInStore = indexInStore;
	}

	@Override
	public String toString() {
		return attribute.getName() + constraint.toString();
	}

	public boolean holdsFor(int i) {
		return constraint.holds(attribute.getValues().get(i));
	}

	public int getIndexInStore() {
		return indexInStore;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public Constraint getConstraint() {
		return constraint;
	}
	
	public Set<Integer> getSupportSet() {
		if (supportSet==null) {
			supportSet=new HashSet<Integer>();
			for (int i=0; i<attribute.getValues().size(); i++) {
				if (holdsFor(i)) {
					supportSet.add(i);
				}
			}
		}
		return supportSet;
	}

}
