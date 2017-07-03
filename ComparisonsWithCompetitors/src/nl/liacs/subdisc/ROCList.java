package nl.liacs.subdisc;

import java.util.*;

public class ROCList extends ArrayList<SubgroupROCPoint>
{
	private static final long serialVersionUID = 1L;

	public ROCList(SubgroupSet theSubgroupSet)
	{
		for(Subgroup s : theSubgroupSet)
			add(new SubgroupROCPoint(s));
	}

	@Override
	public boolean add(SubgroupROCPoint theSubgroupROCPoint)
	{
		int anIndex = size();
		float aTPR = theSubgroupROCPoint.getTPR();
		float aFPR = theSubgroupROCPoint.getFPR();

		if (aTPR <= aFPR) //always under curve
			return false;

		super.add(theSubgroupROCPoint);

		if (size() > 1)
		{
			//move new rule from end to correct position
			while ((anIndex > 0) && (getFalsePositiveRateAt(anIndex - 1) >= aFPR))
			{
				SubgroupROCPoint anotherPoint = get(anIndex - 1);
				set(anIndex - 1, theSubgroupROCPoint);
				set(anIndex, anotherPoint);
				anIndex--;
			}

			if (getFalsePositiveRateAt(anIndex + 1) == getFalsePositiveRateAt(anIndex))
			{
				if(getTruePositiveRateAt(anIndex + 1) > getTruePositiveRateAt(anIndex))
					remove(anIndex);
				else
					remove(anIndex + 1);
			}

			if (getSlopeAt(anIndex - 1) <= getSlopeAt(anIndex))
			{
				remove(anIndex);
//					return false;
			}
			while ((anIndex > 0) && (getSlopeAt(anIndex - 2) <= getSlopeAt(anIndex - 1)))
			{
				remove(anIndex - 1);
				anIndex--;
			}
			while ((anIndex < size() - 1) && (getSlopeAt(anIndex) <= getSlopeAt(anIndex + 1)))
			{
				remove(anIndex + 1);
			}
		}

		return true;
	}

	private float getTruePositiveRateAt(int theIndex)
	{
		if (theIndex == -1)
			return 0.0f;
		else if (theIndex == size())
			return 1.0f;
		else
			return get(theIndex).getTPR();
	}

	private float getFalsePositiveRateAt(int theIndex)
	{
		if (theIndex == -1)
			return 0.0f;
		else if (theIndex == size())
			return 1.0f;
		else
			return get(theIndex).getFPR();
	}

	private float getSlopeAt(int theIndex)
	{
		if (size() == 0)
			return 0.0f;
		else if(theIndex == -1)
			return getTruePositiveRateAt(0) / getFalsePositiveRateAt(0);
		else if(theIndex == size())
			return 0.0f;
		else if(theIndex == size() - 1)
			return (1.0f - getTruePositiveRateAt(theIndex)) / (1.0f - getFalsePositiveRateAt(theIndex));
		else if(getFalsePositiveRateAt(theIndex + 1) == getFalsePositiveRateAt(theIndex))
			return 0.0f;
		else
			return (getTruePositiveRateAt(theIndex + 1) - getTruePositiveRateAt(theIndex)) /
					(getFalsePositiveRateAt(theIndex + 1) - getFalsePositiveRateAt(theIndex));
	}

	public float getAreaUnderCurve()
	{
		float anArea = 0;

		for (int i = -1; i < size(); i++)
		{
			float aWidth = getFalsePositiveRateAt(i + 1) - getFalsePositiveRateAt(i);
			anArea += aWidth * ((getTruePositiveRateAt(i) + getTruePositiveRateAt(i+1)) / 2.0f);
		}
		return anArea;
	}
}
