package Data;

import java.util.Arrays;

/**
 * The AttributeNominal class extend the Attribute class. It is designed for
 * nominal attributes.
 * 
 * @author guillaume
 *
 */
public class AttributeNominal extends Attribute {
	/*
	 * ########################################################################
	 * Declaration of the attributes of the class
	 * ########################################################################
	 */
	/**
	 * The array containing the ordered values taken by the Attribute
	 */
	String[] values;

	/*
	 * ########################################################################
	 * Declaration of the methods of the class
	 * ########################################################################
	 */
	public AttributeNominal(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public void setValues(String[] orderedValues) {
		this.values = new String[orderedValues.length];
		for (int i = 0; i < orderedValues.length; i++) {
			this.values[i] = orderedValues[i];
		}
	}

	public String[] getOrderedValues() {
		return this.values;
	}

	@Override
	public String toString() {
		return "Numerical attribute [id=" + this.id + ", name=" + this.name + ", orderedValues="
				+ Arrays.toString(this.values) + "]";
	}
}
