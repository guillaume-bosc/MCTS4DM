package de.fraunhofer.iais.ocm.core.model.data.attributegroups;

public enum AttributeGroupType {

	JOINT_MACRO_ATTRIBUTE("joint_macro_attribute"), HIERARCHY("hierarchy"), CATEGORY_TAG(
			"category_tag");

	private String databaseRepresentation;

	public static AttributeGroupType getTypeMatchingDBRepresentation(
			String dbRepresentation) {
		for (AttributeGroupType attributeGroupType : AttributeGroupType
				.values()) {
			if (attributeGroupType.getDatabaseRepresentation().equals(
					dbRepresentation)) {
				return attributeGroupType;
			}
		}
		throw new IllegalArgumentException(
				"no AttributeGroupType matching string representation");
	}

	public String getDatabaseRepresentation() {
		return databaseRepresentation;
	}

	AttributeGroupType(String dbRepresentation) {
		this.databaseRepresentation = dbRepresentation;
	}

}
