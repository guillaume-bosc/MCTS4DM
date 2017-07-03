package Data;

import java.util.Random;

import org.apache.lucene.util.OpenBitSet;

import Process.Global;

public class PatternBoolean extends Pattern {
	/*
	 * ########################################################################
	 * Declaration of the attributes of the class
	 * ########################################################################
	 */
	public OpenBitSet description;

	/*
	 * ########################################################################
	 * Declaration of the methods of the class
	 * ########################################################################
	 */
	public PatternBoolean(boolean isTarget) {
		if (isTarget)
			this.description = new OpenBitSet(Global.targets.length);
		else
			this.description = new OpenBitSet(Global.nbAttr);
		this.isTarget = isTarget;

		this.support = new OpenBitSet(Global.objects.length);
		this.support.set(0, Global.objects.length);
		this.supportSize = Global.objects.length;

		if (this.isTarget)
			if (!Global.extendsWithLabels) {
				this.candidates = new OpenBitSet(1);
			} else {
				this.candidates = new OpenBitSet(Global.targets.length);
				this.candidates.set(0, Global.targets.length);
			}
		else {
			this.candidates = (OpenBitSet) Global.candidatesBoolean.clone();
		}

		this.lastIdAttr = -1;
	}

	public PatternBoolean(PatternBoolean descr) {
		this.description = descr.description;
		this.isTarget = descr.isTarget;
		this.support = descr.support;
		this.supportSize = descr.supportSize;
		this.lastIdAttr = descr.lastIdAttr;

		if (this.isTarget)
			if (!Global.extendsWithLabels) {
				this.candidates = new OpenBitSet(1);
			} else {
				this.candidates = new OpenBitSet(Global.targets.length);
				this.candidates.set(0, Global.targets.length);
			}
		else {
			this.candidates = (OpenBitSet) Global.candidatesBoolean.clone();
		}
	}

	public PatternBoolean(Pattern currentPattern, int idTarget) {
		this.isTarget = currentPattern.isTarget;
		this.description = (OpenBitSet) ((PatternBoolean) currentPattern).description.clone();
		this.description.set(idTarget);
		this.support = (OpenBitSet) currentPattern.support.clone();
		this.supportSize = currentPattern.supportSize;
		this.lastIdAttr = idTarget;

		// Update the support
		for (int idObj = this.support.nextSetBit(0); idObj >= 0; idObj = this.support.nextSetBit(idObj + 1)) {
			if (this.isTarget) {
				if (!Global.objects[idObj].containsTarget(idTarget)) {
					this.support.clear(idObj);
					this.supportSize--;
				}
			} else if (!Global.objects[idObj].containsProp(idTarget)) {
				this.support.clear(idObj);
				this.supportSize--;
			}
		}

		OpenBitSet suppTarget = Global.bsSet.get(this.support);
		if (suppTarget == null) {
			suppTarget = this.support;
			Global.bsSet.put(suppTarget, suppTarget);
		}
		this.support = suppTarget;

		if (this.isTarget)
			if (!Global.extendsWithLabels)
				this.candidates = new OpenBitSet(1);
			else {
				this.candidates = new OpenBitSet(Global.targets.length);
				this.candidates.set(0, Global.targets.length);
			}
		else {
			this.candidates = (OpenBitSet) Global.candidatesBoolean.clone();
		}
	}

	/*
	 * ########################################################################
	 * Declaration of the override methods of the class
	 * ########################################################################
	 */
	@Override
	public Pattern expand(int idChild, OpenBitSet supportDual) {
		this.candidates.clear(idChild);
		if (this.description.get(idChild))
			return null;

		if (!isTarget && this.description.cardinality() == Global.maxLength && !this.description.get(idChild))
			return null;

		PatternBoolean child = new PatternBoolean(this, idChild);

		if (isTarget && child.supportSize == 0)
			return null;
		if (!isTarget && child.supportSize < Global.minSup)
			return null;

		this.lastIdAttr = idChild;
		// Remove redundancy
		if (Global.duplicatesExpand == Enum.DuplicatesExpand.Order)
			child.candidates.clear(0, idChild);
		child.candidates.clear(idChild);
		return child;
	}

	@Override
	public String toString() {
		String res = "";
		for (int i = description.nextSetBit(0); i >= 0; i = description.nextSetBit(i + 1)) {
			if (!res.isEmpty()) {
				res += ", ";
			}
			if (isTarget)
				res += Global.targets[i].getName();
			else
				res += Global.attributes[i].getName();
		}
		return "[" + res + "]";
	}

	@Override
	void delete() {
		this.description = null;
		this.support = null;
		this.candidates = null;
	}

	@Override
	public long getDescriptionSize() {
		return this.description.cardinality();
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
		double res = 0.;
		long commonProperties = 0;
		long nbLitLeft = this.description.cardinality();
		long nbLitRight = ((PatternBoolean) pattern).description.cardinality();

		double totJaccardSupp = 0.;

		OpenBitSet interSupport = (OpenBitSet) (this.support.clone());
		OpenBitSet unionSupport = (OpenBitSet) (this.support.clone());

		interSupport.and(pattern.support);
		unionSupport.or(pattern.support);

		totJaccardSupp = ((double) (interSupport.cardinality())) / unionSupport.cardinality();

		OpenBitSet desc = (OpenBitSet) this.description.clone();
		desc.and(((PatternBoolean) pattern).description);

		commonProperties = desc.cardinality();

		// Redundancy score is
		// - the jaccard between supports of both subgroups
		// - the proportion of common literals
		// - the Jaccard between intervals of common literals divided by the
		// number of distinct literals
		res = totJaccardSupp + (double) (commonProperties) / (nbLitLeft + nbLitRight - commonProperties) + 1;

		return res;
	}

	@Override
	boolean sameTargets(Pattern target) {
		long n1 = this.description.cardinality();
		long n2 = ((PatternBoolean) target).description.cardinality();

		if (n1 != n2)
			return false;

		OpenBitSet interDesc = (OpenBitSet) (this.description.clone());
		interDesc.and(((PatternBoolean) target).description);
		long n3 = interDesc.cardinality();

		return (n1 == n3);
	}

	@Override
	boolean rollOutDirect(int idProp) {
		if (this.description.get(idProp))
			return false;

		this.description.set(idProp);

		// Updates the support
		for (int idObject = support.nextSetBit(0); idObject >= 0; idObject = support.nextSetBit(idObject + 1)) {
			if (!Global.objects[idObject].containsProp(idProp)) {
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
		int[] listProp = new int[jumpingCount + 1];

		for (int i = 0; i <= jumpingCount; i++) {
			int n = r.nextInt((int) attrSet.cardinality());
			if (n == 0)
				break;

			int idProp = -1;
			while (n >= 0) {
				idProp = attrSet.nextSetBit(idProp + 1);
				n--;
			}
			if (this.description.get(idProp)) {
				i--;
				attrSet.clear(idProp);
				continue;
			}

			attrSet.clear(idProp);
			this.description.set(idProp);
			listProp[i] = idProp;
		}

		// Updates the support
		for (int idObject = support.nextSetBit(0); idObject >= 0; idObject = support.nextSetBit(idObject + 1)) {
			for (int idProp : listProp) {
				if (!Global.objects[idObject].containsProp(idProp)) {
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
		OpenBitSet attrSet = (OpenBitSet) this.description.clone();
		attrSet.set(0, Global.nbAttr);
		if (Global.duplicatesExpand == Enum.DuplicatesExpand.Order)
			attrSet.clear(0, lastIdAttr + 1);

		Random r = new Random();
		int maxExpand = r.nextInt((int) Global.pathLength + 1);

		while (maxExpand > 0) {
			maxExpand--;

			// Only roll out on the attributes and not on the targets
			int n = r.nextInt((int) attrSet.cardinality());
			int idProp = -1;
			while (n >= 0) {
				idProp = attrSet.nextSetBit(idProp + 1);
				n--;
			}

			if (this.description.get(idProp)) {
				attrSet.clear(idProp);
				maxExpand++;
				continue;
			}

			attrSet.clear(idProp);
			this.description.set(idProp);

			// Updates the support
			for (int idObject = support.nextSetBit(0); idObject >= 0; idObject = support.nextSetBit(idObject + 1)) {
				if (!Global.objects[idObject].containsProp(idProp)) {
					support.clear(idObject);
					supportSize--;
				}
			}

		}
	}

	@Override
	void performCompleteCopy() {
		this.description = (OpenBitSet) this.description.clone();
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
		PatternBoolean other = (PatternBoolean) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		return true;
	}
}
