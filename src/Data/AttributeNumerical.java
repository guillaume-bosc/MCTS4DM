package Data;

import java.util.Arrays;

/**
 * The AttributeNumerical class extend the Attribute class. It is designed for
 * boolean attributes.
 * 
 * @author guillaume
 *
 */
public class AttributeNumerical extends Attribute {
	/*
	 * ########################################################################
	 * Declaration of the attributes of the class
	 * ########################################################################
	 */
	/**
	 * The array containing the ordered values taken by the Attribute
	 */
	double[] orderedValues;

	/*
	 * ########################################################################
	 * Declaration of the methods of the class
	 * ########################################################################
	 */
	public AttributeNumerical(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public void setOrderedValues(double[] orderedValues) {
		this.orderedValues = new double[orderedValues.length];
		for (int i = 0; i < orderedValues.length; i++) {
			this.orderedValues[i] = orderedValues[i];
		}
	}

	public double[] getOrderedValues() {
		return this.orderedValues;
	}

	@Override
	public String toString() {
		return "Numerical attribute [id=" + this.id + ", name=" + this.name + ", orderedValues="
				+ Arrays.toString(this.orderedValues) + "]";
	}
}
