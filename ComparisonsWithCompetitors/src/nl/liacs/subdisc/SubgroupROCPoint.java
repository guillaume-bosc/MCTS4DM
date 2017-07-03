package nl.liacs.subdisc;

import java.awt.geom.*;

/**
 * A SubgroupROCPoint of a {@link Subgroup} is nothing more than a 
 * <code>Point2D.&nbsp;Float</code> with that Subgroups'
 * {@link Subgroup#getID() getID()} as identifier,
 * {@link Subgroup#getFalsePositiveRate() getFalsePositiveRate()} for
 * <code>Point2D&nbsp;.x</code> and {@link Subgroup#getTruePositiveRate()} for
 * <code>Point2D&nbsp;.y</code>.
 */
public class SubgroupROCPoint extends Point2D.Float
{
	private static final long serialVersionUID = 1L;
	public final int ID;

	/**
	 * Creates a SubgroupROCPoint for the {@link Subgroup} passed in as
	 * parameter.
	 * 
	 * @param theSubgroup the Subgroup for which to create the
	 * SubgroupROCPoint.
	 */
	public SubgroupROCPoint(Subgroup theSubgroup)
	{
		ID = theSubgroup.getID();
		super.x = theSubgroup.getFalsePositiveRate();
		super.y = theSubgroup.getTruePositiveRate();
	}

	/**
	 * Convenience method, more intuitive way to get the FalsePositiveRate
	 * then subgroupROCPoint&nbsp;.x or subgroupROCPoint&nbsp;.getX().
	 * 
	 * @return x, better known as FalsePositiveRate.
	 */
	public float getFPR() { return super.x; }

	/**
	 * Convenience method, more intuitive way to get the TruePositiveRate
	 * then subgroupROCPoint&nbsp;.y or subgroupROCPoint&nbsp;.getY().
	 * 
	 * @return y, better known as TruePositiveRate.
	 */
	public float getTPR() { return super.y; }

	/**
	 * SubgroupROCPoint overrides the setLocation method of Point2D, because
	 * we do no allow the X/Y coordinates to be changed.
	 */
	@Override
	public void setLocation(double x, double y) {}

	/**
	 * SubgroupROCPoint overrides the setLocation method of Point2D, because
	 * we do no allow the X/Y coordinates to be changed.
	 */
	@Override
	public void setLocation(float x, float y) {}

	/**
	 * Overrides <code>Object</code>s' <code>toString()</code> method to to
	 * get detailed information about this SubgroupROCPoint.
	 * 
	 * @return a <code>String</code> representation of this SubgroupROCPoint
	 * .
	 */
	@Override
	public String toString()
	{
		return "Subgroup " + ID + " (FPR " + getFPR() + ", TPR "+ getTPR() + ")";
	}
}
