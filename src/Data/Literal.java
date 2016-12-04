package Data;

public class Literal {
	/*
	 * ########################################################################
	 * Declaration of the attributes of the class
	 * ########################################################################
	 */
	AttributeNumerical attr;
	int idMin;
	int idMax;
	// BitSet support;

	/*
	 * ########################################################################
	 * End of declaration of the attributes of the class
	 * ########################################################################
	 */
	public Literal() {
		this.attr = null;
		this.idMin = 0;
		this.idMax = 0;
	}

	public Literal(AttributeNumerical attr) {
		this.attr = attr;
		this.idMin = 0;
		this.idMax = attr.getOrderedValues().length - 1;
	}

	public Literal(AttributeNumerical attr, int idValue) {
		this.attr = attr;
		this.idMin = idValue;
		this.idMax = idValue;
	}

	public Literal(Literal aLit) {
		this.attr = aLit.getAttr();
		this.idMin = aLit.getIdMin();
		this.idMax = aLit.getIdMax();
	}

	public AttributeNumerical getAttr() {
		return attr;
	}

	public int getIdMin() {
		return idMin;
	}

	public int getIdMax() {
		return idMax;
	}

	/**
	 * @param attr
	 *            the attr to set
	 */
	public void setAttr(AttributeNumerical attr) {
		this.attr = attr;
	}

	/**
	 * @param idMin
	 *            the idMin to set
	 */
	public void setIdMin(int idMin) {
		this.idMin = idMin;
	}

	/**
	 * @param idMax
	 *            the idMax to set
	 */
	public void setIdMax(int idMax) {
		this.idMax = idMax;
	}

	@Override
	public String toString() {
		return "Literal [idAttr=" + this.attr.getId() + ", idMin=" + this.idMin + ", idMax=" + this.idMax + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attr == null) ? 0 : attr.hashCode());
		result = prime * result + idMax;
		result = prime * result + idMin;
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
		Literal other = (Literal) obj;
		if (attr == null) {
			if (other.attr != null)
				return false;
		} else if (!attr.equals(other.attr))
			return false;
		if (idMax != other.idMax)
			return false;
		if (idMin != other.idMin)
			return false;
		return true;
	}
}
