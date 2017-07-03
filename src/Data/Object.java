package Data;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.util.OpenBitSet;

import Process.Global;

public class Object {
	/*
	 * ########################################################################
	 * Declaration of the attributes of the class
	 * ########################################################################
	 */
	int id; // id of the object
	public OpenBitSet target; // Target of the object
	public int[] descriptionNumerical;
	public OpenBitSet descriptionBoolean;
	public List<OpenBitSet> descriptionSequence;

	/*
	 * ########################################################################
	 * Declaration of the methods of the class
	 * ########################################################################
	 */
	public Object(int id, DataType type) {
		this.id = id;
		this.target = new OpenBitSet(Global.targets.length);
		this.descriptionNumerical = null;
		this.descriptionBoolean = null;
		this.descriptionSequence = null;

		switch (type) {
		case BOOLEAN:
			this.descriptionBoolean = new OpenBitSet(Global.nbAttr);
			break;
		case NUMERIC:
			this.descriptionNumerical = new int[Global.nbAttr];
			break;
		case NOMINAL:
			this.descriptionNumerical = new int[Global.nbAttr];
			break;
		case SEQUENCE:
			this.descriptionSequence = new ArrayList<OpenBitSet>();
			break;
		default:
			break;
		}
	}

	public void setAttributeValue(int idAttr, int idValue) {
		this.descriptionNumerical[idAttr] = idValue;
	}

	public void setAttribute(int idAttr) {
		this.descriptionBoolean.set(idAttr);
	}

	public void setTarget(int idAttr) {
		this.target.set(idAttr);
	}

	public boolean isLessThan(int idProp, int newMinId) {
		return this.descriptionNumerical[idProp] < newMinId;
	}

	public boolean isGreaterThan(int idProp, int newMaxId) {
		return this.descriptionNumerical[idProp] > newMaxId;
	}

	public boolean containsProp(int idProp) {
		return this.descriptionBoolean.get(idProp);
	}

	public boolean containsTarget(int idTarget) {
		return this.target.get(idTarget);
	}

	public void addItemset(OpenBitSet bs) {
		this.descriptionSequence.add(bs);

	}

	public String toString() {
		String res = "ID=" + id + "\tDescription=";
		switch (Global.propType) {
		case BOOLEAN:
			res += booleanToString();
			break;
		case NUMERIC:
			res += numericalToString();
			break;
		case NOMINAL:
			res += nominalToString();
			break;
		case SEQUENCE:
			res += sequenceToString();
			break;
		default:
			break;
		}
		res += "\tTarget=" + targetToString();

		return res;
	}

	public String numericalToString() {
		String res = "";
		for (int i = 0; i < this.descriptionNumerical.length; i++) {
			if (!res.isEmpty())
				res += ", ";
			res += Global.attributes[i].getName() + "="
					+ ((AttributeNumerical) Global.attributes[i]).getOrderedValues()[descriptionNumerical[i]];
		}
		return "[" + res + "]";
	}
	
	public String nominalToString() {
		String res = "";
		for (int i = 0; i < this.descriptionNumerical.length; i++) {
			if (!res.isEmpty())
				res += ", ";
			res += Global.attributes[i].getName() + "="
					+ ((AttributeNominal) Global.attributes[i]).getOrderedValues()[descriptionNumerical[i]];
		}
		return "[" + res + "]";
	}

	public String booleanToString() {
		String res = "";
		for (int i = descriptionBoolean.nextSetBit(0); i >= 0; i = descriptionBoolean.nextSetBit(i + 1)) {
			if (!res.isEmpty()) {
				res += ", ";
			}
			res += Global.attributes[i].getName();
		}
		return "[" + res + "]";
	}

	public String sequenceToString() {
		String res = "";
		for (int i = 0; i < this.descriptionSequence.size(); i++) {
			OpenBitSet bs = this.descriptionSequence.get(i);
			String tmp = "";
			for (int j = bs.nextSetBit(0); j >= 0; j = bs.nextSetBit(j + 1)) {
				if (!tmp.isEmpty()) {
					tmp += ", ";
				}
				tmp += j;
			}
			if (!res.isEmpty())
				res += " ";
			res += "{" + tmp + "}";
		}
		return "[" + res + "]";
	}

	public String targetToString() {
		String res = "";
		for (int i = target.nextSetBit(0); i >= 0; i = target.nextSetBit(i + 1)) {
			if (!res.isEmpty()) {
				res += ", ";
			}
			res += Global.targets[i].getName();
		}
		return "[" + res + "]";
	}
}
