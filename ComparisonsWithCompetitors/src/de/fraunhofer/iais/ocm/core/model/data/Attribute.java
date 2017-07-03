package de.fraunhofer.iais.ocm.core.model.data;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.ocm.core.model.data.attributegroups.AttributeGroup;
import de.fraunhofer.iais.ocm.core.model.data.attributegroups.AttributeGroupType;

public class Attribute {

	private final String name;

	private final String description;

	private List<String> values;

	private List<Integer> missingPositions;

	private final int indexInTable;

	private final DataTable dataTable;

	private final boolean isId;

	public Attribute(String name, boolean isId, String description,
			List<String> values, int indexInTable, DataTable dataTable) {
		this.name = name;
		this.isId = isId;
		this.description = description;
		this.values = values;
		this.missingPositions = new ArrayList<Integer>(values.size());
		this.indexInTable = indexInTable;
		this.dataTable = dataTable;
	}

	public List<AttributeGroup> getAttributeGroups() {
		List<AttributeGroup> result = new ArrayList<AttributeGroup>();
		List<AttributeGroup> allAttributeGroups = this.dataTable
				.getAttributeGroupsStore().getAttributeGroups();
		for (AttributeGroup attributeGroup : allAttributeGroups) {
			for (Attribute attribute : attributeGroup.getMembers()) {
				if (attribute == this) {
					result.add(attributeGroup);
				}
			}
		}
		return result;
	}

	public boolean isPartOfMacroAttributeWith(Attribute attribute) {
		if (this.dataTable != attribute.dataTable)
			return false;

		for (AttributeGroup group : this.getAttributeGroups()) {
			if (group.getAttributeGroupType() != AttributeGroupType.JOINT_MACRO_ATTRIBUTE)
				continue;
			if (group.getMembers().contains(attribute))
				return true;
		}
		
		return false;
	}

	public DataTable getDataTable() {
		return dataTable;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public List<String> getValues() {
		return values;
	}

	public List<Integer> getMissingPositions() {
		return missingPositions;
	}

	public String getStatistic() {
		return "";
	}

	public boolean isNumeric() {
		return (this instanceof NumericAttribute);
	}

	public boolean isCategoric() {
		return (this instanceof CategoricalAttribute);
	}

	public boolean isId() {
		return isId;
	}

	public int getIndexInTable() {
		return indexInTable;
	}

	public String toString() {
		return name;
	}

}
