package Data;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.lucene.util.OpenBitSet;

import Process.Global;

public class PatternNominal extends Pattern {
	/*
	 * ########################################################################
	 * Declaration of the attributes of the class
	 * ########################################################################
	 */
	public Map<Integer, Integer> description;

	/*
	 * ########################################################################
	 * Declaration of the specific methods of the class
	 * ########################################################################
	 */
	public PatternNominal() {
		this.description = new HashMap<Integer, Integer>();
		this.isTarget = false;
		this.support = new OpenBitSet(Global.objects.length);
		this.support.set(0, Global.objects.length);
		this.supportSize = Global.objects.length;

		if (this.isTarget)
			if (!Global.extendsWithLabels)
				this.candidates = new OpenBitSet(1);
			else {
				this.candidates = new OpenBitSet(Global.targets.length);
				this.candidates.set(0, Global.targets.length);
			}
		else {
			this.candidates = (OpenBitSet) Global.candidatesNominal.clone();
		}
		this.lastIdAttr = -1;
	}

	public PatternNominal(PatternNominal descr) {
		this.description = descr.description;
		this.isTarget = descr.isTarget;
		this.support = descr.support;
		this.supportSize = descr.supportSize;
		this.lastIdAttr = descr.lastIdAttr;

		if (this.isTarget)
			if (!Global.extendsWithLabels)
				this.candidates = new OpenBitSet(1);
			else {
				this.candidates = new OpenBitSet(Global.targets.length);
				this.candidates.set(0, Global.targets.length);
			}
		else {
			this.candidates = (OpenBitSet) Global.candidatesNominal.clone();
		}
	}

	public PatternNominal(PatternNominal pattern, int idAttr, int idValue, OpenBitSet tmp, int supportSizeChild) {
		this.description = new HashMap<Integer, Integer>();
		for (Entry<Integer, Integer> entry : pattern.description.entrySet()) {
			int id = entry.getKey();
			int value = entry.getValue();
			this.description.put(id, value);
		}
		this.description.put(idAttr, idValue);

		this.support = tmp;
		this.supportSize = supportSizeChild;

		this.isTarget = pattern.isTarget;

		if (this.isTarget)
			if (!Global.extendsWithLabels)
				this.candidates = new OpenBitSet(1);
			else {
				this.candidates = new OpenBitSet(Global.targets.length);
				this.candidates.set(0, Global.targets.length);
			}
		else {
			this.candidates = (OpenBitSet) Global.candidatesNominal.clone();
		}

	}

	/**
	 * Computes the closed new value for interval
	 * 
	 * @param bs
	 *            : the bitset
	 * @param idProp
	 *            : the id of the prop
	 * @param idMin
	 *            : the current idmin value of literal
	 * @param idMax
	 *            : the current idmax value of literal
	 * @return
	 */
	public static int[] computeClosedMinMaxIndex(OpenBitSet bs, int idProp, int idMin, int idMax) {
		int[] res = new int[2];
		res[0] = ((AttributeNumerical) Global.attributes[idProp]).getOrderedValues().length;

		res[1] = -1;
		for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
			int value = Global.objects[i].descriptionNumerical[idProp];
			if (value > idMin && value < res[0])
				res[0] = value;

			if (value < idMax && value > res[1])
				res[1] = value;
		}
		return res;
	}

	/*
	 * ########################################################################
	 * Declaration of the override methods of the class
	 * ########################################################################
	 */
	@Override
	public Pattern expand(int idChild, OpenBitSet supportDual) {
		this.candidates.clear(idChild);
		PatternNominal child = null;
		int idValue = idChild;
		int idAttr = -1;
		for (int i = 0; i < Global.attributes.length; i++) {
			int attSize = ((AttributeNominal) Global.attributes[i]).getOrderedValues().length;
			if (idValue < attSize) { // This attribute is chosen
				idAttr = i;
				break;
			} else { // switch to the next attribute
				idValue -= attSize;
			}
		}

		if (idAttr < 0) {
			System.err.println("[PatternNominal] pb expand nominal");
		}

		if (this.description.containsKey(idAttr)) {
			return null;
		}

		// Max length constraint
		if ((!this.isTarget && this.description.size() == Global.maxLength))
			return null;

		OpenBitSet tmp = (OpenBitSet) this.support.clone();
		int size = (int) tmp.cardinality();
		for (int idObj = tmp.nextSetBit(0); idObj >= 0 && size >= Global.minSup; idObj = tmp.nextSetBit(idObj + 1)) {
			if (Global.objects[idObj].descriptionNumerical[idAttr] != idValue) {
				tmp.clear(idObj);
				size--;
			}
		}

		if (size >= Global.minSup) {
			child = new PatternNominal(this, idAttr, idValue, tmp, size);
			child.lastIdAttr = idAttr;
			// Remove redundancy
			if (Global.duplicatesExpand == Enum.DuplicatesExpand.Order)
				child.candidates.clear(0,
						idChild - idValue + ((AttributeNominal) Global.attributes[idAttr]).getOrderedValues().length);

			child.candidates.clear(idChild);
		}

		return child;
	}

	@Override
	boolean rollOutDirect(int idProp) {
		if (this.description.containsKey(idProp))
			return false;

		long cpt = (long) Math.random() * (this.supportSize);
		int idObj = support.nextSetBit(0);
		while (cpt > 0) {
			idObj = support.nextSetBit(idObj + 1);
			cpt--;
		}
		int idValue = Global.objects[idObj].descriptionNumerical[idProp];
		this.description.put(idProp, idValue);

		// Updates the support
		for (int idObject = support.nextSetBit(0); idObject >= 0; idObject = support.nextSetBit(idObject + 1)) {
			if (Global.objects[idObject].descriptionNumerical[idProp] != idValue) {
				support.clear(idObject);
				supportSize--;
			}
		}

		return true;
	}

	@Override
	boolean rollOutLarge(OpenBitSet attrSet) {
		Random r = new Random();
		int jumpingCount = r.nextInt(Global.jumpingLarge);
		List<Integer> listProp = new ArrayList<Integer>();
		Map<Integer, Integer> mapPropValue = new HashMap<Integer, Integer>();

		for (int i = 0; i <= jumpingCount; i++) {
			int n = r.nextInt((int) attrSet.cardinality());
			if (n == 0)
				break;

			int idProp = -1;
			while (n >= 0) {
				idProp = attrSet.nextSetBit(idProp + 1);
				n--;
			}
			if (this.description.containsKey(idProp)) {
				i--;
				attrSet.clear(idProp);
				continue;
			}

			attrSet.clear(idProp);

			long cpt = (long) Math.random() * (this.supportSize);
			int idObj = support.nextSetBit(0);
			while (cpt > 0) {
				idObj = support.nextSetBit(idObj + 1);
				cpt--;
			}
			int idValue = Global.objects[idObj].descriptionNumerical[idProp];

			this.description.put(idProp, idValue);
			listProp.add(idProp);
			mapPropValue.put(idProp, idValue);
		}

		// Updates the support
		for (int idObject = support.nextSetBit(0); idObject >= 0; idObject = support.nextSetBit(idObject + 1)) {
			for (int idProp : listProp) {
				if (Global.objects[idObject].descriptionNumerical[idProp] != mapPropValue.get(idProp)) {
					support.clear(idObject);
					supportSize--;
					break;
				}
			}
		}

		return true;
	}

	@Override
	void rollOutRandomPath() {
		BitSet attrSet = new BitSet(Global.nbAttr);
		attrSet.set(0, Global.nbAttr);
		if (Global.duplicatesExpand == Enum.DuplicatesExpand.Order)
			attrSet.clear(0, lastIdAttr + 1);

		Random r = new Random();
		int maxExpand = r.nextInt((int) Global.pathLength + 1);
		Set<Integer> idPropModif = new HashSet<Integer>();

		while (maxExpand > 0) {
			maxExpand--;

			// Only roll out on the attributes and not on the targets
			int n = r.nextInt(attrSet.cardinality());
			int idProp = -1;
			while (n >= 0) {
				idProp = attrSet.nextSetBit(idProp + 1);
				n--;
			}

			// if it already contains a restriction on this attribute
			if (this.description.containsKey(idProp)) {
				attrSet.clear(idProp);
				maxExpand++;
				continue;
			}
			attrSet.clear(idProp);

			int idValue = r.nextInt(((AttributeNominal) Global.attributes[idProp]).getOrderedValues().length);
			this.description.put(idProp, idValue);

			idPropModif.add(idProp);
		}

		for (int idObject = support.nextSetBit(0); idObject >= 0; idObject = support.nextSetBit(idObject + 1)) {
			for (int idProp : idPropModif) {
				int indiceObj = Global.objects[idObject].descriptionNumerical[idProp];
				if (indiceObj != this.description.get(idProp)) {
					support.clear(idObject);
					supportSize--;
					break;
				}
			}
		}
	}

	@Override
	void delete() {
		this.description = null;
		this.support = null;
		this.candidates = null;
	}

	@Override
	public String toString() {
		String res = "";
		for (int idAttr = 0; idAttr < Global.nbAttr; idAttr++) {
			if (!this.description.containsKey(idAttr))
				continue;

			if (!res.isEmpty()) {
				res += ", ";
			}

			Integer idValue = description.get(idAttr);
			AttributeNominal attr = (AttributeNominal) Global.attributes[idAttr];
			res += "[" + attr.name + " = " + attr.values[idValue] + "]";

		}

		if (res.isEmpty())
			res = "[]";

		return res;
	}

	@Override
	public long getDescriptionSize() {
		return this.description.size();
	}

	@Override
	/**
	 * Computes the similarity score between this subgroup and another subgroup
	 * 
	 * @param aSub
	 *            : the other subgroup
	 * @return the similarity score
	 */
	public double similarityScore(Pattern pattern) {
		Map<Integer, Integer> otherProp = ((PatternNominal) pattern).description;
		double res = 0.;
		int commonProperties = 0;
		int sameValues = 0;
		int nbLitLeft = this.description.size();
		int nbLitRight = otherProp.size();
		double totJaccardSupp = 0.;

		OpenBitSet interSupport = (OpenBitSet) (this.support.clone());
		OpenBitSet unionSupport = (OpenBitSet) (this.support.clone());

		interSupport.and(pattern.support);
		unionSupport.or(pattern.support);

		totJaccardSupp = ((double) (interSupport.cardinality())) / unionSupport.cardinality();

		for (Entry<Integer, Integer> entry : this.description.entrySet()) {
			int idAttr = entry.getKey();
			int idValue = entry.getValue();
			Integer idValueOther = otherProp.get(idAttr);
			if (idValueOther != null) {
				commonProperties++;
				if (idValueOther == idValue) {
					sameValues++;
				}
			}
		}

		// Redundancy score is
		// - the jaccard between supports of both subgroups
		// - the proportion of common literals
		// - the Jaccard between intervals of common literals divided by the
		// number of distinct literals
		res = totJaccardSupp;
		if (commonProperties > 0)
			res += (double) (commonProperties) / (nbLitLeft + nbLitRight - commonProperties)
					+ (double) (sameValues) / commonProperties;

		return res;
	}

	@Override
	boolean sameTargets(Pattern target) {
		Map<Integer, Integer> otherMap = ((PatternNominal) target).description;
		for (Entry<Integer, Integer> entry : this.description.entrySet()) {
			Integer idValue = entry.getValue();
			Integer idAttr = entry.getKey();
			Integer idValueOther = otherMap.get(idAttr);

			if (idValueOther == null)
				return false;

			if (idValueOther != idValue)
				return false;

		}

		return true;
	}

	@Override
	void performCompleteCopy() {
		Map<Integer, Integer> temp = this.description;
		this.description = new HashMap<Integer, Integer>();
		for (Map.Entry<Integer, Integer> entry : temp.entrySet()) {
			this.description.put(entry.getKey(), entry.getValue());
		}

		this.support = (OpenBitSet) this.support.clone();

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
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
		PatternNominal other = (PatternNominal) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		return true;
	}

}
