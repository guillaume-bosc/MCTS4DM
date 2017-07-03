package de.fraunhofer.iais.ocm.core.model.pattern.emm;

import java.util.List;
import java.util.Set;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;

public abstract class AbstractModel {

	protected List<Attribute> attributes;
	protected Set<Integer> rows;

	public AbstractModel(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	public AbstractModel(List<Attribute> attributes, Set<Integer> rows) {
		this(attributes);
		this.rows = rows;
	}
	
	public List<Attribute> getAttributes() {
		return attributes;
	}

	public Set<Integer> getRows() {
		return this.rows;
	}
}
