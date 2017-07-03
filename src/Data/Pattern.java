package Data;

import org.apache.lucene.util.OpenBitSet;

public abstract class Pattern {
	/*
	 * ########################################################################
	 * Declaration of the attributes of the class
	 * ########################################################################
	 */
	public OpenBitSet support;
	OpenBitSet candidates;
	boolean isTarget;
	public int supportSize;
	int lastIdAttr;

	/*
	 * ########################################################################
	 * Declaration of the methods of the class
	 * ########################################################################
	 */
	public abstract Pattern expand(int idChild, OpenBitSet supportDual);

	abstract boolean rollOutDirect(int idProp);

	abstract boolean rollOutLarge(OpenBitSet attrSet);

	abstract void rollOutRandomPath();

	abstract void delete();

	public abstract long getDescriptionSize();

	abstract boolean sameTargets(Pattern target);

	abstract double similarityScore(Pattern pattern);

	abstract void performCompleteCopy();

	public void setIsTarget(boolean v) {
		this.isTarget = v;
	}
	
	@Override
	public abstract int hashCode();
	

	@Override
	public abstract boolean equals(java.lang.Object obj);
}
