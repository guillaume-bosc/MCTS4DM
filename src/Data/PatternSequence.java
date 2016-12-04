package Data;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.util.OpenBitSet;

import Process.Global;

public class PatternSequence extends Pattern {
	/*
	 * ########################################################################
	 * Declaration of the attributes of the class
	 * ########################################################################
	 */
	List<OpenBitSet> description;

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

		if (this.isTarget)
			if (Global.measure == Enum.Measure.WKL)
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
		this.description = descr.description;
		this.isTarget = descr.isTarget;
		this.support = descr.support;

		if (this.isTarget)
			if (Global.measure == Enum.Measure.WKL)
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

	@Override
	Pattern expand(int idChild, OpenBitSet supportDual) {
		PatternSequence child = null;
		int idAttr = idChild / 2;
		boolean itemizeExtension = idChild % 2 == 0;

		return child;
	}

	@Override
	void delete() {
		this.description = null;
		this.support = null;
		this.candidates = null;
	}

	@Override
	public
	long getDescriptionSize() {
		return this.description.size();
	}

	@Override
	boolean sameTargets(Pattern target) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	double similarityScore(Pattern pattern) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	void performCompleteCopy() {
		// TODO Auto-generated method stub

	}

	@Override
	boolean rollOutDirect(int idProp) {
		// TODO Auto-generated method stub
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

}
