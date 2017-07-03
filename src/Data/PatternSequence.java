package Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.lucene.util.OpenBitSet;

import Process.Global;

public class PatternSequence extends Pattern {
	/*
	 * ########################################################################
	 * Declaration of the attributes of the class
	 * ########################################################################
	 */
	List<OpenBitSet> description;
	public List<Integer> projectedBase;

	/*
	 * ########################################################################
	 * Declaration of the methods of the class
	 * ########################################################################
	 */
	public PatternSequence() {
		this.description = new ArrayList<OpenBitSet>();
		this.isTarget = false;
		this.support = new OpenBitSet(Global.objects.length);
		this.support.set(0, Global.objects.length);
		this.projectedBase = new ArrayList<Integer>();
		for (int i = 0; i < Global.objects.length; i++)
			this.projectedBase.add(0);

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
	}

	public PatternSequence(PatternSequence descr) {
		this.description = new ArrayList<OpenBitSet>();
		for (int i = 0; i < descr.description.size(); i++) {
			this.description.add((OpenBitSet) descr.description.get(i).clone());
		}

		this.projectedBase = descr.projectedBase;
		// this.projectedBase = new ArrayList<Integer>();
		// for (int i = 0; i < descr.projectedBase.size(); i++)
		// this.projectedBase.add(descr.projectedBase.get(i));

		this.isTarget = descr.isTarget;
		this.support = (OpenBitSet) descr.support.clone();
		this.supportSize = descr.supportSize;

		if (this.isTarget)
			if (!Global.extendsWithLabels)
				this.candidates = new OpenBitSet(1);
			else {
				this.candidates = new OpenBitSet(2 * Global.targets.length);
				this.candidates.set(0, 2 * Global.targets.length);
			}
		else {
			if (Global.refineExpand == Enum.RefineExpand.Prefix) {
				this.candidates = new OpenBitSet(Global.nbChild);
				this.candidates.set(0, Global.nbChild);
			} else {
				int nbChildren = (Global.nbAttr) * ((2 * this.description.size()) + 1);
				this.candidates = new OpenBitSet(nbChildren);
				this.candidates.set(0, nbChildren);
			}
		}
	}

	@Override
	public Pattern expand(int idChild, OpenBitSet supportDual) {
		this.candidates.clear(idChild);

		PatternSequence p = null;
		if (Global.refineExpand == Enum.RefineExpand.Prefix)
			p = (PatternSequence) refinePrefix(idChild, false);
		else if (Global.refineExpand == Enum.RefineExpand.Direct)
			p = (PatternSequence) refineAll(idChild, false);
		else
			System.err.println("[error] bad refinement for the expand in sequences");

		return p;
	}

	private Pattern refineAll(int idChild, boolean selfModif) {
		List<OpenBitSet> previousDescription = this.description;
		PatternSequence refined = this;

		if (!selfModif) {
			refined = new PatternSequence(this);
		}

		refined.description = new ArrayList<OpenBitSet>();

		int idAttr = idChild % (Global.nbAttr);
		int position = idChild / Global.nbAttr;

		boolean isNewItemset = (position % 2) == 0;
		int insertBeforePosition = (position / 2);

		// Add unchanged itemsets
		int pos = 0;
		for (pos = 0; pos < insertBeforePosition; pos++) {
			OpenBitSet bs = previousDescription.get(pos);
			refined.addItemset(bs);

			// Remove candidates
			for (int anIdAttr = bs.nextSetBit(0); anIdAttr >= 0; anIdAttr = bs.nextSetBit(anIdAttr + 1)) {
				refined.candidates.clear((1 + pos) * Global.nbAttr + anIdAttr);
			}
		}

		// Add or modify the itemset
		if (isNewItemset) {
			OpenBitSet itemset = new OpenBitSet(Global.nbAttr);
			itemset.clear(0, Global.nbAttr);
			itemset.set(idAttr);
			if (!refined.addItemset(itemset))
				return null;

			refined.candidates.clear((1 + pos) * Global.nbAttr + idAttr);

			pos++;

			if (pos < previousDescription.size()) {
				OpenBitSet bs = previousDescription.get(pos);
				if (!refined.addItemset(bs))
					return null;

				// Remove candidates
				for (int anIdAttr = bs.nextSetBit(0); anIdAttr >= 0; anIdAttr = bs.nextSetBit(anIdAttr + 1)) {
					refined.candidates.clear((1 + pos) * Global.nbAttr + anIdAttr);
				}
			}
		} else {
			OpenBitSet bs = previousDescription.get(pos);
			bs.set(idAttr);
			if (!refined.addItemset(bs))
				return null;

			// Remove candidates
			for (int anIdAttr = bs.nextSetBit(0); anIdAttr >= 0; anIdAttr = bs.nextSetBit(anIdAttr + 1)) {
				refined.candidates.clear((1 + pos) * Global.nbAttr + anIdAttr);
			}

			pos++;
		}

		// Add last itemsets
		for (; pos < previousDescription.size(); pos++) {
			OpenBitSet bs = description.get(pos);
			if (!refined.addItemset(bs))
				return null;

			// Remove candidates
			for (int anIdAttr = bs.nextSetBit(0); anIdAttr >= 0; anIdAttr = bs.nextSetBit(anIdAttr + 1)) {
				refined.candidates.clear((1 + pos) * Global.nbAttr + anIdAttr);
			}
		}
		
		if (refined.projectedBase.size() != refined.supportSize)
			System.out.println("pb : " + refined.projectedBase.size() + " - " + refined.supportSize + " - " + refined.support.cardinality() ) ;
		
		return refined;
	}

	public boolean addItemset(OpenBitSet bs) {
		List<Integer> projectedBaseRefined = new ArrayList<Integer>();
		this.description.add(bs);
		int idList = -1;

		for (int idSeq = this.support.nextSetBit(0); idSeq >= 0
				&& this.supportSize >= Global.minSup; idSeq = this.support.nextSetBit(idSeq + 1)) {
			idList++;
			int lastItemset = projectedBase.get(idList);
			boolean keepInSupport = false;

			int idAttr = bs.nextSetBit(0);

			for (int i = 0; i < Global.sequenceIndex.get(idAttr).get(idSeq).size(); i++) {
				int idItemset = Global.sequenceIndex.get(idAttr).get(idSeq).get(i);

				if (idItemset <= lastItemset)
					continue;

				if (idItemset > lastItemset) {
					if (isContainedInItemset(idSeq, idItemset, bs)) {
						keepInSupport = true;
						projectedBaseRefined.add(idItemset);
						break;
					}
				}
			}

			if (!keepInSupport) {
				this.support.clear(idSeq);
				this.supportSize--;
			}
		}

		this.projectedBase = projectedBaseRefined;
		
		return (this.supportSize >= Global.minSup);
	}

	private boolean isContainedInItemset(int idSeq, int idItemset, OpenBitSet bs) {
		for (int idAttr = bs.nextSetBit(0); idAttr >= 0; idAttr = bs.nextSetBit(idAttr + 1)) {

			boolean isContained = false;
			for (int i = 0; i < Global.sequenceIndex.get(idAttr).get(idSeq).size(); i++) {
				int anIdItemset = Global.sequenceIndex.get(idAttr).get(idSeq).get(i);

				if (idItemset == anIdItemset) {
					isContained = true;
					continue;
				}
			}

			if (!isContained)
				return false;

		}
		return true;

	}

	private Pattern refinePrefix(int idChild, boolean selfModif) {
		PatternSequence refined = this;

		if (!selfModif) {
			refined = new PatternSequence(this);

		}

		int idAttr = idChild / 2;
		boolean isItemSetExtension = (idChild % 2) == 0;

		List<Integer> projectedBaseRefined = new ArrayList<Integer>();
		int idList = -1;

		if (isItemSetExtension) {
			OpenBitSet lastItemsetDescr;
			if (refined.description.isEmpty()) {
				OpenBitSet itemset = new OpenBitSet(Global.nbAttr);
				itemset.clear(0, Global.nbAttr);
				itemset.set(idAttr);
				refined.description.add(itemset);
				lastItemsetDescr = itemset;
			} else {
				lastItemsetDescr = refined.description.get(refined.description.size() - 1);
				lastItemsetDescr.set(idAttr);
			}

			for (int idItem = lastItemsetDescr.nextSetBit(0); idItem >= 0; idItem = lastItemsetDescr
					.nextSetBit(idItem + 1)) {
				refined.candidates.clear(2 * idItem);
			}

			for (int idSeq = refined.support.nextSetBit(0); idSeq >= 0
					&& refined.supportSize >= Global.minSup; idSeq = refined.support.nextSetBit(idSeq + 1)) {
				idList++;
				int lastItemset = projectedBase.get(idList);

				boolean keepInSupport = false;
				for (int i = 0; i < Global.sequenceIndex.get(idAttr).get(idSeq).size(); i++) {
					int idItemset = Global.sequenceIndex.get(idAttr).get(idSeq).get(i);
					if (idItemset < lastItemset)
						continue;

					if (idItemset == lastItemset) {
						keepInSupport = true;
						projectedBaseRefined.add(idItemset);
						break;
					}

					if (idItemset > lastItemset) {
						OpenBitSet bs = Global.objects[idSeq].descriptionSequence.get(idItemset);
						boolean isIncluded = true;
						for (int idItem = lastItemsetDescr.nextSetBit(0); idItem >= 0; idItem = lastItemsetDescr
								.nextSetBit(idItem + 1)) {
							if (!bs.get(idItem)) {
								isIncluded = false;
								break;
							}
						}
						if (isIncluded) {
							keepInSupport = true;
							projectedBaseRefined.add(idItemset);
							break;
						}
						continue;
					}

				}
				if (!keepInSupport) {
					refined.support.clear(idSeq);
					refined.supportSize--;
					if (refined.supportSize < Global.minSup)
						return null;
				}
			}

			refined.projectedBase = projectedBaseRefined;
		} else {
			if (refined.description.isEmpty()) {
				return null;
			}
			OpenBitSet itemset = new OpenBitSet(Global.nbAttr);
			itemset.clear(0, Global.nbAttr);
			itemset.set(idAttr);
			refined.description.add(itemset);
			refined.candidates.clear(2 * idAttr);

			for (int idSeq = refined.support.nextSetBit(0); idSeq >= 0
					&& refined.supportSize >= Global.minSup; idSeq = refined.support.nextSetBit(idSeq + 1)) {
				idList++;
				int lastItemset = projectedBase.get(idList);

				boolean keepInSupport = false;
				for (int i = 0; i < Global.sequenceIndex.get(idAttr).get(idSeq).size(); i++) {
					int idItemset = Global.sequenceIndex.get(idAttr).get(idSeq).get(i);

					if (idItemset <= lastItemset)
						continue;

					if (idItemset > lastItemset) {
						keepInSupport = true;
						projectedBaseRefined.add(idItemset);
						break;
					}
				}

				if (!keepInSupport) {
					refined.support.clear(idSeq);
					refined.supportSize--;
					if (refined.supportSize < Global.minSup)
						return null;
				}
			}

			refined.projectedBase = projectedBaseRefined;
		}

		return refined;
	}

	@Override
	void delete() {
		System.out.println("Delete");
		this.description = null;
		this.support = null;
		this.candidates = null;
		this.projectedBase.clear();
		this.projectedBase = null;
	}

	@Override
	public long getDescriptionSize() {
		return this.description.size();
	}

	@Override
	boolean sameTargets(Pattern target) {
		// TODO
		return false;
	}

	@Override
	double similarityScore(Pattern pattern) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	void performCompleteCopy() {
		List<Integer> descr = this.projectedBase;
		this.projectedBase = new ArrayList<Integer>();
		for (int i = 0; i < descr.size(); i++)
			this.projectedBase.add(descr.get(i));
		// List<OpenBitSet> temp = this.description;
		// this.description = new ArrayList<OpenBitSet>();
		// for (int i = 0; i < temp.size(); i++) {
		// this.description.add((OpenBitSet) temp.get(i).clone());
		// }
		//
		// Map<Integer, Integer> tempMap = this.projectedBase;
		// this.projectedBase = new HashMap<Integer, Integer>();
		// this.projectedBase.putAll(tempMap);
		//
		// this.support = (OpenBitSet) this.support.clone();
	}

	@Override
	boolean rollOutDirect(int idProp) {
		Random r = new Random();
		boolean isItemsetExtension = r.nextBoolean();
		int idChild = 2 * idProp + 1;
		if (isItemsetExtension) {
			idChild--;
			if (this.description.get(this.description.size() - 1).get(idChild)) {
				// Already contained
				return false;
			}
		}

		this.refinePrefix(idChild, true);
		return true;
	}

	@Override
	boolean rollOutLarge(OpenBitSet attrSet) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	void rollOutRandomPath() {
		// TODO Auto-generated method stub

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
		PatternSequence other = (PatternSequence) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String res = "";
		for (int idItemset = 0; idItemset < description.size(); idItemset++) {
			OpenBitSet itemset = description.get(idItemset);
			for (int i = itemset.nextSetBit(0); i >= 0; i = itemset.nextSetBit(i + 1)) {
				if (!res.isEmpty()) {
					res += " ";
				}
				res += i;
			}
			res += " -1";
		}
		return res + " -2";
	}

	public void notAChildOfParent(int idOfChild) {
		if (description.get(description.size() - 1).cardinality() != 1) {
			candidates.clear(idOfChild);
		} else {
			if ((idOfChild % 2) == 1) {
				candidates.clear(idOfChild);
			}
		}

	}

	public void generatePrefixCandidates() {
		this.candidates = new OpenBitSet(Global.nbAttr * 2);
		this.candidates.set(0, Global.nbAttr * 2);
		
		OpenBitSet bs = this.description.get(this.description.size()-1);
		for (int idAttr = bs.nextSetBit(0); idAttr >= 0; idAttr = bs.nextSetBit(idAttr + 1)) {
			int idChild = idAttr * 2;
			this.candidates.clear(idChild);
		}		
	}
}
