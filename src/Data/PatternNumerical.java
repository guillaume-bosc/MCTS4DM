package Data;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.lucene.util.OpenBitSet;

import Process.Global;

public class PatternNumerical extends Pattern {
	/*
	 * ########################################################################
	 * Declaration of the attributes of the class
	 * ########################################################################
	 */
	public Map<Integer, Literal> description;
	boolean lastRightMove;

	/*
	 * ########################################################################
	 * Declaration of the specific methods of the class
	 * ########################################################################
	 */
	public PatternNumerical() {
		this.description = new HashMap<Integer, Literal>();
		this.isTarget = false;
		this.support = new OpenBitSet(Global.objects.length);
		this.support.set(0, Global.objects.length);
		this.supportSize = Global.objects.length;

		if (this.isTarget)
			if (!Global.extendsWithLabels)
				this.candidates = new OpenBitSet(1);
			else {
				this.candidates = new OpenBitSet(2 * Global.targets.length);
				this.candidates.set(0, 2 * Global.targets.length);
			}
		else {
			this.candidates = new OpenBitSet(Global.nbChild);
			this.candidates.set(0, Global.nbChild);
		}
		
		this.lastIdAttr = -1;
	}

	public PatternNumerical(PatternNumerical descr) {
		this.description = new HashMap<Integer, Literal>();
		this.description.putAll(descr.description);
		
		this.isTarget = descr.isTarget;
		this.support = (OpenBitSet) descr.support.clone();
		
		this.supportSize = descr.supportSize;
		this.lastIdAttr = descr.lastIdAttr;

		if (this.isTarget)
			if (!Global.extendsWithLabels)
				this.candidates = new OpenBitSet(1);
			else {
				this.candidates = new OpenBitSet(2 * Global.targets.length);
				this.candidates.set(0, 2 * Global.targets.length);
			}
		else {
			this.candidates = new OpenBitSet(Global.nbChild);
			this.candidates.set(0, Global.nbChild);
		}
	}

	public PatternNumerical(PatternNumerical pattern, Literal childLitProp, OpenBitSet suppProp, int suppSize) {
		this.support = suppProp;
		this.supportSize = suppSize;

		this.description = new HashMap<Integer, Literal>();
		for (Map.Entry<Integer, Literal> entry : pattern.description.entrySet()) {
			this.description.put(entry.getKey(), entry.getValue());
		}
		this.description.put(childLitProp.attr.id, childLitProp);

		this.isTarget = pattern.isTarget;

		if (this.isTarget)
			if (!Global.extendsWithLabels)
				this.candidates = new OpenBitSet(1);
			else {
				this.candidates = new OpenBitSet(2 * Global.targets.length);
				this.candidates.set(0, 2 * Global.targets.length);
			}
		else {
			this.candidates = new OpenBitSet(Global.nbChild);

			if (this.getDescriptionSize() == Global.maxLength) {
				this.candidates.clear(0, Global.nbChild);
				for (Integer idProp : this.description.keySet()) {
					this.candidates.set(2 * idProp);
					this.candidates.set((2 * idProp) + 1);
				}
			} else {
				this.candidates.set(0, Global.nbChild);
			}
		}
	}

	public void setAttributeValue(int idAttr, int idValue) {
		this.description.put(idAttr, new Literal((AttributeNumerical) (Global.attributes[idAttr]), idValue));
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
		PatternNumerical child = null;

		OpenBitSet supportToExtand = this.support;

		this.candidates.clear(idChild);
		int idProp = idChild / 2;
		Literal theLitProp = this.description.get(idProp);

		// IF the Literal is not instantiated yet, we create it
		if (theLitProp == null) {
			if (isTarget)
				theLitProp = new Literal((AttributeNumerical) Global.targets[idProp]);
			else {
				if (this.description.size() == Global.maxLength)
					return null;
				theLitProp = new Literal((AttributeNumerical) Global.attributes[idProp]);
			}
		}

		// Copies for both children of this subgroup
		Literal childLitProp = new Literal(theLitProp);

		// The current bound values (indices) of the current Literal
		int idMin = theLitProp.getIdMin();
		int idMax = theLitProp.getIdMax();

		if (idMin == idMax)
			return null;

		/*
		 * Searches for the value of minIdValueObject and maxIdValueObject
		 * Depending on the exploration strategy
		 */
		int minIdValueObject;
		int maxIdValueObject;

		if (Global.refineExpand == Enum.RefineExpand.Direct) {
			minIdValueObject = idMin + 1;
			maxIdValueObject = idMax - 1;
		} else {
			OpenBitSet exploredBitset = supportToExtand;
			if (Global.refineExpand == Enum.RefineExpand.TunedGenerator) {
				exploredBitset = (OpenBitSet) supportToExtand.clone();
				exploredBitset.and(supportDual);
			}
			int[] res = computeClosedMinMaxIndex(exploredBitset, idProp, idMin, idMax);
			minIdValueObject = res[0];
			maxIdValueObject = res[1];
		}

		// Check which child it has to expand
		if (idChild % 2 == 0) {
			// Tests for the lower bounds of the interval
			OpenBitSet supportChild = (OpenBitSet) supportToExtand.clone();
			int supportSizeChild = this.supportSize;

			// Iterates on the objects in the support of the current literal
			// and tests if it is still in the support of the new literal
			for (int idObj = supportChild.nextSetBit(0); idObj >= 0
					&& supportSizeChild >= Global.minSup; idObj = supportChild.nextSetBit(idObj + 1)) {
				// TODO: Handle if isTarget = true
				int value = Global.objects[idObj].descriptionNumerical[idProp];
				if (value < minIdValueObject) {
					supportChild.clear(idObj);
					supportSizeChild--;
				}
			}

			// Tests if the support of the subgroup with this new literal is
			// greater than minSup
			if ((isTarget && supportSizeChild > 0) || (!isTarget && supportSizeChild >= Global.minSup)) {
				childLitProp.setIdMin(minIdValueObject);
				OpenBitSet suppProp = Global.bsSet.get(supportChild);
				if (suppProp == null) {
					suppProp = supportChild;
					Global.bsSet.put(suppProp, suppProp);
				}

				child = new PatternNumerical(this, childLitProp, suppProp, supportSizeChild);
				child.lastIdAttr = idProp;
				child.lastRightMove = false;
				// Do not generate redundant subgroups

				if (Global.duplicatesExpand == Enum.DuplicatesExpand.Order)
					child.candidates.clear(0, idChild);
			}

		} else {
			// Tests for the upper bounds of the interval
			OpenBitSet supportChild = (OpenBitSet) supportToExtand.clone();
			int supportSizeChild = this.supportSize;

			// Iterates on the objects in the support of the current
			// literal
			// and tests if it is still in the support of the new
			// literal
			for (int idObj = supportChild.nextSetBit(0); idObj >= 0
					&& supportSizeChild >= Global.minSup; idObj = supportChild.nextSetBit(idObj + 1)) {
				// TODO : Handle if isTarget = true
				int value = Global.objects[idObj].descriptionNumerical[idProp];
				if (value > maxIdValueObject) {
					supportChild.clear(idObj);
					supportSizeChild--;
				}

			}

			// Tests if the support of the subgroup with this new
			// literal is
			// greater than minSup
			if ((isTarget && supportSizeChild > 0) || (!isTarget && supportSizeChild >= Global.minSup)) {
				childLitProp.setIdMax(maxIdValueObject);
				OpenBitSet suppProp = Global.bsSet.get(supportChild);
				if (suppProp == null) {
					suppProp = supportChild;
					Global.bsSet.put(suppProp, suppProp);
				}
				child = new PatternNumerical(this, childLitProp, suppProp, supportSizeChild);
				child.lastIdAttr = idProp;
				child.lastRightMove = true;

				// Do not generate redundant subgroups
				if (Global.duplicatesExpand == Enum.DuplicatesExpand.Order)
					child.candidates.clear(0, idChild);
			}

		}

		return child;
	}

	@Override
	boolean rollOutDirect(int idProp) {
		Random r = new Random();
		boolean minSide = r.nextBoolean();

		if (Global.duplicatesExpand == Enum.DuplicatesExpand.Order && idProp == lastIdAttr) {
			if (lastRightMove)
				minSide = false;
		}

		Literal theLitProp = description.get(idProp);
		// IF the Literal is not instantiated yet, we create it
		if (theLitProp == null) {
			if (description.size() == Global.maxLength)
				return false;

			theLitProp = new Literal((AttributeNumerical) Global.attributes[idProp]);
			description.put(idProp, theLitProp);
		}

		// if we can't increase the lower bound, we do not do anything
		// and continue to roll it differently
		if (theLitProp.getIdMin() == theLitProp.getIdMax())
			return false;

		if (minSide) {
			// Increase by 1 the lower bound
			theLitProp.setIdMin(theLitProp.getIdMin() + 1);

			// Updates the support of this literal
			for (int idObject = support.nextSetBit(0); idObject >= 0; idObject = support.nextSetBit(idObject + 1)) {
				int value = Global.objects[idObject].descriptionNumerical[idProp];
				if (value < theLitProp.idMin) {
					support.clear(idObject);
					supportSize--;
				}
			}
		} else {
			// Decrease by 1 the lower bound
			theLitProp.setIdMax(theLitProp.getIdMax() - 1);

			// Updates the support of this literal
			for (int idObject = support.nextSetBit(0); idObject >= 0; idObject = support.nextSetBit(idObject + 1)) {
				int value = Global.objects[idObject].descriptionNumerical[idProp];
				if (value > theLitProp.idMax) {
					support.clear(idObject);
					supportSize--;
				}
			}
		}

		return true;
	}

	@Override
	boolean rollOutLarge(OpenBitSet attrSet) {
		Random r = new Random();
		int n = r.nextInt((int) attrSet.cardinality());
		int idProp = -1;
		while (n >= 0) {
			idProp = attrSet.nextSetBit(idProp + 1);
			n--;
		}

		boolean minSide = r.nextBoolean();
		if (Global.duplicatesExpand == Enum.DuplicatesExpand.Order && idProp == lastIdAttr) {
			if (lastRightMove)
				minSide = false;
		}

		Literal theLitProp = description.get(idProp);
		// IF the Literal is not instantiated yet, we create it
		if (theLitProp == null) {
			if (description.size() == Global.maxLength)
				return false;

			theLitProp = new Literal((AttributeNumerical) Global.attributes[idProp]);
			description.put(idProp, theLitProp);
		}

		// if we can't increase the lower bound, we do not do anything
		// and continue to roll it differently
		if (theLitProp.getIdMin() == theLitProp.getIdMax())
			return false;

		if (minSide) {
			int newBound = theLitProp.idMin + 1;
			newBound += r.nextInt(Math.min(theLitProp.idMax - theLitProp.idMin, Global.jumpingLarge));

			// Increase by 1 the lower bound
			theLitProp.setIdMin(newBound);

			// Updates the support of this literal
			for (int idObject = support.nextSetBit(0); idObject >= 0; idObject = support.nextSetBit(idObject + 1)) {
				int value = Global.objects[idObject].descriptionNumerical[idProp];
				if (value < theLitProp.idMin) {
					support.clear(idObject);
					supportSize--;
				}
			}
		} else {
			int newBound = theLitProp.idMax - 1;
			newBound -= r.nextInt(Math.min(theLitProp.idMax - theLitProp.idMin, Global.jumpingLarge));

			// Decrease by 1 the lower bound
			theLitProp.setIdMax(newBound);

			// Updates the support of this literal
			for (int idObject = support.nextSetBit(0); idObject >= 0; idObject = support.nextSetBit(idObject + 1)) {
				int value = Global.objects[idObject].descriptionNumerical[idProp];
				if (value > theLitProp.idMax) {
					support.clear(idObject);
					supportSize--;
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
			attrSet.clear(0, this.lastIdAttr);

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

			Literal theLitProp = this.description.get(idProp);

			// IF the Literal is not instantiated yet, we create it
			if (theLitProp == null) {
				theLitProp = new Literal((AttributeNumerical) Global.attributes[idProp]);
				this.description.put(idProp, theLitProp);
			}

			// The current bound values (indices) of the current Literal
			int idMin = theLitProp.getIdMin();
			int idMax = theLitProp.getIdMax();

			if (idMin == idMax) {
				attrSet.clear(idProp);
				maxExpand++;
				continue;
			}

			boolean minSide = r.nextBoolean();
			if (Global.duplicatesExpand == Enum.DuplicatesExpand.Order && idProp == lastIdAttr) {
				if (lastRightMove)
					minSide = false;
			}

			if (minSide)
				theLitProp.setIdMin(idMin + 1);
			else
				theLitProp.setIdMax(idMax - 1);

			idPropModif.add(idProp);
		}

		for (int idObject = support.nextSetBit(0); idObject >= 0; idObject = support.nextSetBit(idObject + 1)) {
			for (int idProp : idPropModif) {
				int indiceObj = Global.objects[idObject].descriptionNumerical[idProp];
				if (indiceObj < this.description.get(idProp).getIdMin()
						|| indiceObj > this.description.get(idProp).getIdMax()) {
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
		for (int i = 0; i < Global.nbAttr; i++) {
			if (!this.description.containsKey(i))
				continue;

			if (!res.isEmpty()) {
				res += ", ";
			}

			Literal theLiteral = description.get(i);
			if (theLiteral.idMin == theLiteral.idMax) {
				res += "[" + theLiteral.attr.name + " = " + theLiteral.attr.orderedValues[theLiteral.idMax] + "]";
			} else {
				res += "[" + theLiteral.attr.orderedValues[theLiteral.idMin] + " <= " + theLiteral.attr.name + " <= "
						+ theLiteral.attr.orderedValues[theLiteral.idMax] + "]";
			}

		}
		
		if (res.isEmpty())
			res = "[]";
		
		return res;
	}

	@Override
	public
	long getDescriptionSize() {
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
		Map<Integer, Literal> otherProp = ((PatternNumerical) pattern).description;
		double res = 0.;
		int commonProperties = 0;
		int nbLitLeft = 0;
		int nbLitRight = 0;

		double totJaccardSupp = 0.;
		double totJaccardInterv = 0.;

		OpenBitSet interSupport = (OpenBitSet) (this.support.clone());
		OpenBitSet unionSupport = (OpenBitSet) (this.support.clone());

		interSupport.and(pattern.support);
		unionSupport.or(pattern.support);

		totJaccardSupp = ((double) (interSupport.cardinality())) / unionSupport.cardinality();

		for (int i = 0; i < Global.nbAttr; i++) {
			if (this.description.get(i) != null)
				nbLitLeft++;

			if (otherProp.get(i) != null)
				nbLitRight++;

			if (this.description.get(i) == null || otherProp.get(i) == null)
				continue;

			commonProperties++;

			// Proceeds to the jaccards between both intervals
			Literal litLeft = this.description.get(i);
			Literal litRight = otherProp.get(i);

			double min1 = litLeft.getAttr().getOrderedValues()[litLeft.getIdMin()];
			double max1 = litLeft.getAttr().getOrderedValues()[litLeft.getIdMax()];
			double min2 = litRight.getAttr().getOrderedValues()[litRight.getIdMin()];
			double max2 = litRight.getAttr().getOrderedValues()[litRight.getIdMax()];

			double mini1, mini2, maxi1, maxi2;
			double inters = 0.;
			double union = 0.;

			if (min1 < min2) {
				mini1 = min1;
				mini2 = min2;
				maxi1 = max1;
				maxi2 = max2;
			} else {
				mini1 = min2;
				mini2 = min1;
				maxi1 = max2;
				maxi2 = max1;
			}

			if (maxi1 < mini2) {
				inters = 0.;
				union = maxi1 - mini1 + maxi2 - mini2;
			} else if (maxi1 < maxi2) {
				inters = maxi1 - mini2;
				union = maxi2 - mini1;
			} else {
				inters = maxi2 - mini2;
				union = maxi1 - mini1;
			}

			if (mini1 == maxi1 && mini2 == maxi2) {
				inters = 1.;
				union = 1.;
				if (mini1 != mini2) {
					inters = 0.;
					union = 1.;
				}
			}

			totJaccardInterv += inters / union;

			if (Double.isNaN(totJaccardInterv))
				System.out.println("ici");
		}

		// Redundancy score is
		// - the jaccard between supports of both subgroups
		// - the proportion of common literals
		// - the Jaccard between intervals of common literals divided by the
		// number of distinct literals
		res = totJaccardSupp;
		if (commonProperties > 0)
			res += (double) (commonProperties) / (nbLitLeft + nbLitRight - commonProperties)
					+ (double) (totJaccardInterv) / commonProperties;

		return res;
	}

	@Override
	boolean sameTargets(Pattern target) {
		Map<Integer, Literal> otherMap = ((PatternNumerical) target).description;
		for (Entry<Integer, Literal> entry : this.description.entrySet()) {
			Literal aLit = entry.getValue();
			Integer i = entry.getKey();
			Literal otherLit = otherMap.get(i);
			if (aLit != null && otherLit == null)
				return false;

			if (aLit == null && otherLit != null)
				return false;

			if (aLit.idMax != otherLit.idMax || aLit.idMin != otherLit.idMin)
				return false;
		}

		return true;
	}

	@Override
	void performCompleteCopy() {
		Map<Integer, Literal> temp = this.description;
		this.description = new HashMap<Integer, Literal>();
		for (Map.Entry<Integer, Literal> entry : temp.entrySet()) {
			this.description.put(entry.getKey(), new Literal(entry.getValue()));
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
		PatternNumerical other = (PatternNumerical) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		return true;
	}
}
