package de.fraunhofer.iais.ocm.core.model.data;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.ocm.core.model.data.attributegroups.AttributeGroup;

public class AttributeGroupsStore {
	
	private List<AttributeGroup> attributeGroups;

	public AttributeGroupsStore() {
		
		this.attributeGroups = new ArrayList<AttributeGroup>();
	}

	public List<AttributeGroup> getAttributeGroups() {
		return attributeGroups;
	}

}
