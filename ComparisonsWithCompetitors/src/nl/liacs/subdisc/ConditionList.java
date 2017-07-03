package nl.liacs.subdisc;

import java.util.*;

public class ConditionList extends ArrayList<Condition> implements Comparable<ConditionList>
{
	private static final long serialVersionUID = 1L;

	public ConditionList() {}

	public ConditionList copy()
	{
		ConditionList aNewConditionList = new ConditionList();
		for(Condition aCondition : this)
			aNewConditionList.addCondition(aCondition.copy());
		return aNewConditionList;
	}

	public boolean addCondition(Condition aCondition)
	{
		if (indexOf(aCondition) == -1) //already present?
		{
			add(aCondition);
			return true;
		}
		else
			return false;
	}

	private boolean findCondition(Condition theCondition)
	{
		for (Condition aCondition : this)
			if (theCondition.equals(aCondition))
				return true;
		return false;
	}

	// throws NullPointerException if theCondition is null.
	@Override
	public int compareTo(ConditionList theConditionList)
	{
		if (this == theConditionList)
			return 0;

		else if (this.size() < theConditionList.size())
			return -1;
		else if (this.size() > theConditionList.size())
			return 1;

		for (int i = 0, j = size(); i < j; ++i)
		{
			int aTest = this.get(i).compareTo(theConditionList.get(i));
			if (aTest != 0)
				return aTest;
		}

		return 0;
	}

	//this method computes logical equivalence. This means that the actual number of conditions or the order may differ.
	//Just as long as it effectively selects the same subgroup, no matter what the database is.
	//This method currently doesn't consider equivalence of the type a<10&a<20 vs. a<10 etc.
	@Override
	public boolean equals(Object theObject)
	{
		if (theObject == null || (theObject.getClass() != this.getClass()))
			return false;
		ConditionList aCL = (ConditionList) theObject;

		//check in one direction
		for (Condition aCondition : aCL)
			if (!findCondition(aCondition))
				return false;

		//check in the other direction
		for (Condition aCondition : this)
			if (!aCL.findCondition(aCondition))
				return false;

		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder aResult = new StringBuilder(size() * 25);
		for(Condition aCondition : this)
		{
			aResult.append(aCondition);
			aResult.append(" AND ");
		}
		if (size() == 0)
			return "(empty)";
		else
			return aResult.substring(0, aResult.length() - 5);
	}
}
