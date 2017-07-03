package nl.liacs.subdisc;

import java.util.*;

/**
 * A Subgroup contains a number of instances from the original data. Subgroups
 * are formed by, a number of, {@link Condition Condition}s. Its members include
 * : a {@link ConditionList ConditionList}, a BitSet representing the instances
 * included in this Subgroup, the number of instances in this Subgroup (its
 * coverage), a unique identifier, and the value used to form this Subgroup. It
 * may also contain a {@link DAG DAG}, and a {@link SubgroupSet SubgroupSet}.
 * @see Condition
 * @see ConditionList
 * @see DAG
 * @see nl.liacs.subdisc.gui.MiningWindow
 * @see SubgroupDiscovery
 * @see SubgroupSet
 * @see Condition
 */
public class Subgroup implements Comparable<Subgroup>
{
	private ConditionList itsConditions;
	public BitSet itsMembers;
	private int itsID = 0;
	private int itsCoverage; // crucial to keep it in sync with itsMembers
	private DAG itsDAG;
	private double itsMeasureValue;
	private double itsSecondaryStatistic = 0;
	private double itsTertiaryStatistic = 0;
	int itsDepth;
	private final SubgroupSet itsParentSet;
	// XXX not strictly needed when setting itsPValue to NaN
	private boolean isPValueComputed;
	private double itsPValue;
	private String itsRegressionModel;

	/**
	 * Creates a Subgroup with initial measureValue of 0.0 and a depth of 0.
	 * <p>
	 * The {@link BitSet} can not be <code>null</code> and at least 1 bit
	 * must be set, each set bit represents a member of this Subgroup.
	 * <p>
	 * the {@link ConditionList} and {@link SubgroupSet} argument can be
	 * </code>null</code>, in which case new empty items are created.
	 * 
	 * @param theConditions the ConditionList for this Subgroup.
	 * @param theMembers the BitSet representing members of this Subgroup.
	 * @param theSubgroupSet the SubgroupSet this Subgroup is contained in.
	 * 
	 * @throws IllegalArgumentException if (theMembers == <code>null</code>)
	 * or (theMembers.cardinality() == 0).
	 */
	public Subgroup(ConditionList theConditions, BitSet theMembers, SubgroupSet theSubgroupSet) throws IllegalArgumentException
	{
		if (theMembers == null || theMembers.cardinality() == 0)
			throw new IllegalArgumentException("Subgroups must have members");

		itsConditions = (theConditions == null ? new ConditionList() : theConditions);
		itsDepth = itsConditions.size();

		itsMembers = theMembers;
		itsCoverage = itsMembers.cardinality();

		itsParentSet = (theSubgroupSet == null ? new SubgroupSet(0) : theSubgroupSet);

		itsMeasureValue = 0.0f;
		itsDAG = null;	//not set yet
		isPValueComputed = false;
	}

	// itsMeasureValue, itsCoverage, itsDepth are primitive types, no need
	// to deep-copy
	// itsParentSet must not be deep-copied
	// see remarks for ConditionList/ Condition, which are not true complete
	// deep-copies, but in current code this is no problem
	// itsMembers is deep-copied
	public Subgroup copy()
	{
		// sets conditions, depth, members, coverage, parentSet
		Subgroup aReturn = new Subgroup(itsConditions.copy(), (BitSet) itsMembers.clone(), itsParentSet);

		aReturn.itsMeasureValue = itsMeasureValue;
		// itsDAG = null;
		// isPValueComputed = false;

		aReturn.itsSecondaryStatistic = itsSecondaryStatistic;
		aReturn.itsTertiaryStatistic = itsTertiaryStatistic;
		return aReturn;
	}

	// significant speedup in mining algorithm
	// use old_subgroup.members and update it for new Condition
	public void addCondition(Condition theCondition)
	{
		if (theCondition == null)
		{
			Log.logCommandLine("Subgroup.addCondition(): argument can not be 'null', no Condition added.");
			return;
		}

		itsConditions.addCondition(theCondition);

		itsMembers.and(theCondition.getColumn().evaluate(theCondition));
		//itsMembers.and(theCondition.getColumn().evaluate(theCondition, true));
		// crucial to keep it in sync with itsMembers
		itsCoverage = itsMembers.cardinality();

		++itsDepth;
	}

	public void print()
	{
		Log.logCommandLine("conditions: " + itsConditions.toString());
		Log.logCommandLine("bitset: " + itsMembers.toString());
	}

	public String toString()
	{
		return itsConditions.toString();
	}

	/**
	 * Returns a {@link BitSet} where each set bits represents a member of
	 * this Subgroup.
	 * <p>
	 * Each returned BitSet is a new clone of the actual members, so
	 * changing the returned BitSet has no effect on this Subgroup.
	 * This is unlikely to be a performance penalty in most situations, but
	 * some may want to cache the return BitSet.
	 * Most callers need the returned BitSet for nothing more than looping
	 * over all members, or retrieve the cardinality.
	 *
	 * @return a BitSet representing this Subgroups members.
	 */
	public BitSet getMembers() { return (BitSet) itsMembers.clone(); }

	public boolean covers(int theRow) { return itsMembers.get(theRow); }

	public int getID() { return itsID; }
	public void setID(int theID) { itsID = theID; }

	public double getMeasureValue() { return itsMeasureValue; }
	public void setMeasureValue(double theMeasureValue) { itsMeasureValue = theMeasureValue; }
	public double getSecondaryStatistic() { return itsSecondaryStatistic; }
	public void setSecondaryStatistic(double theSecondaryStatistic) { itsSecondaryStatistic = theSecondaryStatistic; }
	public double getTertiaryStatistic() { return itsTertiaryStatistic; }
	public void setTertiaryStatistic(double theTertiaryStatistic) { itsTertiaryStatistic = theTertiaryStatistic; }

	public void setDAG(DAG theDAG) { itsDAG = theDAG; }
	public DAG getDAG() { return itsDAG; }

	public int getCoverage() { return itsCoverage; }

	public ConditionList getConditions() { return itsConditions; }
	public int getNrConditions() { return itsConditions.size(); }

	public int getDepth() { return itsDepth; }

	// NOTE Map interface expects compareTo and equals to be consistent.
	@Override
	public int compareTo(Subgroup theSubgroup)
	{
		// why not throw NullPointerException if theCondition is null?
		if (theSubgroup == null)
			return 1;
		else if (getMeasureValue() > theSubgroup.getMeasureValue())
			return -1;
		else if (getMeasureValue() < theSubgroup.getMeasureValue())
			return 1;
		else if (getCoverage() > theSubgroup.getCoverage())
			return -1;
		else if (getCoverage() < theSubgroup.getCoverage())
			return 1;
		else
		{
			int aTest = itsConditions.compareTo(theSubgroup.itsConditions);
			if (aTest != 0)
				return aTest;
		}

		return 0;
//		return itsMembers.equals(s.itsMembers);
	}

/*	@Override
	public int compareTo(Subgroup theSubgroup)
	{
		int aTest = itsConditions.compareTo(theSubgroup.itsConditions);
		return aTest;
	}
*/
	/**
	 * NOTE For now this equals implementation is only used for the ROCList
	 * HashSet implementation.
	 * Two subgroups are considered equal if:
	 * for each condition(Attribute-Operator pair) in this.conditionList there
	 * is a matching condition(Attribute-Operator pair) in other.conditionList
	 * and both itsMembers are equal.
	 */
/*
	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof Subgroup))
			return false;

		Subgroup s = (Subgroup) o;

		for(Condition c : itsConditions.itsConditions)
		{
			boolean hasSameAttributeAndOperator = false;
			for(Condition sc : s.itsConditions.itsConditions)
			{
				if(c.getAttribute().getName().equalsIgnoreCase(sc.getAttribute().getName()) &&
						c.getOperatorString().equalsIgnoreCase(sc.getOperatorString()))
				{
					hasSameAttributeAndOperator = true;
					System.out.println(this.getID()+ " " + s.getID());
					this.print();
					s.print();
					break;
				}
			}
			if(!hasSameAttributeAndOperator)
				return false;
		}

		return itsMembers.equals(s.itsMembers);
		//getTruePositiveRate().equals(s.getTruePositiveRate()) &&
			//	getFalsePositiveRate().equals(s.getFalsePositiveRate());
	}
*/
	/*
	 * TODO Even for the SubgroupSet.getROCList code this is NOT enough.
	 * All subgroups are from the same SubgroupSet/ experiment with the same target.
	 * However, two subgroups formed from different Attributes in itsConditions
	 * should be considered unequal. This requires an @Override from itsConditions
	 * hashCode(), as it should not include condition values.
	 * Eg. two subgroups that have the same members and are formed from:
	 * (x < 10) and (x < 11) should be considered equal
	 * (y < 10) and (x < 10) should be considered different
	 */
/*
	@Override
	public int hashCode()
	{
		int hashCode = 0;
		for(Condition c : itsConditions.itsConditions)
			hashCode += (c.getAttribute().getName().hashCode() + c.getOperatorString().hashCode());
		return 31*itsMembers.hashCode() + hashCode;
	}
*/
	// used to determine TP/FP
	public SubgroupSet getParentSet()
	{
		return itsParentSet;
	}

	/**
	 * Returns the TruePositiveRate for this Subgroup.
	 * If no itsParentSet was set for this SubGroup, or no itsBinaryTarget
	 * was set for this SubGroups' itsParentSet this function returns 0.0f.
	 * 
	 * @return the TruePositiveRate, also known as TPR.
	 */
	public Float getTruePositiveRate()
	{
		BitSet tmp = itsParentSet.getBinaryTargetClone();

		if (tmp == null)
			return 0.0f;

		tmp.and(itsMembers);
		// NOTE now tmp.cardinality() = aHeadBody

		float aTotalTargetCoverage = itsParentSet.getTotalTargetCoverage();

		// something is wrong TODO throw error
		if (aTotalTargetCoverage <= 0)
			return 0.0f;
		else
			return tmp.cardinality() / aTotalTargetCoverage;
	}

	/**
	 * Returns the FalsePositiveRate for this Subgroup.
	 * If no itsParentSet was set for this subgroup, or no itsBinaryTarget
	 * was set for this subgroups' itsParentSet this function returns 0.0f.
	 * 
	 * @return the FalsePositiveRate, also known as FPR.
	 */
	public Float getFalsePositiveRate()
	{
		BitSet tmp = itsParentSet.getBinaryTargetClone();

		if (tmp == null)
			return 0.0f;

		tmp.and(itsMembers);
		// NOTE now tmp.cardinality() = aHeadBody

		int aTotalCoverage = itsParentSet.getTotalCoverage();
		float aTotalTargetCoverage = itsParentSet.getTotalTargetCoverage();
		float aBody = (itsParentSet.getTotalCoverage() -
				itsParentSet.getTotalTargetCoverage());

		// something is wrong TODO throw error
		if (aTotalCoverage <= 0 || aTotalTargetCoverage < 0 ||
			aTotalCoverage < aTotalTargetCoverage || aBody <= 0)
			return 0.0f;
		else
			return (itsCoverage - tmp.cardinality()) / aBody;
	}

	public double getPValue()
	{
		return (isPValueComputed ? itsPValue : Double.NaN);
	}

	public void setPValue(NormalDistribution theDistro)
	{
		isPValueComputed = true;
		itsPValue = 1 - theDistro.calcCDF(itsMeasureValue);
	}

	public void setEmpiricalPValue(double[] theQualities)
	{
		isPValueComputed = true;
		int aLength = theQualities.length;
		double aP = 0.0;
		for (int i=0; i<aLength; i++)
		{
			if (theQualities[i]>=itsMeasureValue)
				aP++;
		}
		itsPValue = aP/aLength;
	}

	public void renouncePValue()
	{
		isPValueComputed = false;
	}

	public String getRegressionModel() { return itsRegressionModel; }
	public void setRegressionModel(String theModel) { itsRegressionModel = theModel; }
}
