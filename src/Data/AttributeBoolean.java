package Data;

/**
 * The AttributeBoolean class extend the Attribute class. It is designed for
 * boolean attributes.
 * 
 * @author guillaume
 *
 */
public class AttributeBoolean extends Attribute {
	/*
	 * ########################################################################
	 * Declaration of the methods of the class
	 * ########################################################################
	 */
	public AttributeBoolean(int id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public String toString() {
		return "Boolean attribute [id=" + this.id + ", name=" + this.name + "]";
	}

}
