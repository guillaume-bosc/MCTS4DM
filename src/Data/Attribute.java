package Data;

/**
 * The abstract class Attribute. It is extended by AttributeBoolean and
 * AttributeNumerical.
 * 
 * @author guillaume
 *
 */
public abstract class Attribute {
	/*
	 * ########################################################################
	 * Declaration of the attributes of the class
	 * ########################################################################
	 */
	/**
	 * The ID of the Attribute in [0,nAtt-1]
	 */
	public int id;

	/**
	 * The name of the Attribute
	 */
	public String name;

	/*
	 * ########################################################################
	 * Declaration of the methods of the class
	 * ########################################################################
	 */
	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(java.lang.Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Attribute other = (Attribute) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
