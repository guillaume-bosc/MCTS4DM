package de.fraunhofer.iais.ocm.core.model.data.attributegroups;

import java.util.List;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;

public class AttributeGroup {
		
	private List<Attribute> members;
	
	private String name;
	
	private AttributeGroupType groupType;

	public AttributeGroup(String name,AttributeGroupType groupType,List<Attribute> members) {
		this.members = members;
		this.name = name;
		this.groupType = groupType;
	}
	
	public AttributeGroupType getAttributeGroupType() {
		return this.groupType;
	}
   
	@Override
	public String toString(){
		return members.toString();
	}
	
	public String getName() {
		return name;
	}
	
	public List<Attribute> getMembers(){
		return members;
	}
}