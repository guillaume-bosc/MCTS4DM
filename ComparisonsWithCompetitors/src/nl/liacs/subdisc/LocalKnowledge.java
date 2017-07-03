package nl.liacs.subdisc;

import java.util.*;

public class LocalKnowledge
{
	private List<ConditionList> itsExplanatoryConditions;
	//private String[][] itsColumnsInvolved; //describes the columns that are involved in a subgroup 
	//private BitSet[] tableWithExplanatoryVariables; //a table containing the members of each condition. 
	private Map<Column, List<ConditionList>> mapColumnToConditionList;
	//private Map<Column, List<BitSet>> mapColumnToBitSet;
	private Map<ConditionList, BitSet> mapConditionListToBitSet;
	//each knowledge component is described by one (or more) conditions from the condition list
	private Map<ConditionList, StatisticsBayesRule> mapConditionListBayesRule;
	private BitSet itsTarget;

	public LocalKnowledge(List<ConditionList> theExplanatoryConditions, BitSet theTarget)
	{
		itsExplanatoryConditions = theExplanatoryConditions;
		itsTarget = theTarget;

		mapColumnToConditionList = new HashMap<Column, List<ConditionList>>();
		for (ConditionList cl : itsExplanatoryConditions)
		{
			for (Condition c : cl)
			{
				System.out.println("Local Knowledge variables");
				System.out.println(c.getColumn().getName());
				if (!mapColumnToConditionList.containsKey(c.getColumn()))
				{
					List<ConditionList> aList = new ArrayList<ConditionList>();
					aList.add(cl);
					mapColumnToConditionList.put(c.getColumn(), aList);
				}
				else
				{
//					List<ConditionList> aList = mapColumnToConditionList.get(c.getColumn());
//					aList.add(cl);
//					mapColumnToConditionList.put(c.getColumn(), aList);
					mapColumnToConditionList.get(c.getColumn()).add(cl);
				}
			}
		}

		mapConditionListToBitSet = new HashMap<ConditionList, BitSet>();
		mapConditionListBayesRule = new HashMap<ConditionList, StatisticsBayesRule>();

		for (ConditionList cl: itsExplanatoryConditions)
		{
			BitSet aBitSetCl = new BitSet(cl.get(0).getColumn().size()); // the bit set of the conditionlist, also set the size here, and all bits to one
			aBitSetCl.set(0, cl.get(0).getColumn().size());// or col.size -1? 
			for (Condition c : cl)
			{
				BitSet aBitSetCondition = c.getColumn().evaluate(c);
				System.out.println("cardinality condition");
				System.out.println(aBitSetCondition.cardinality());
				aBitSetCl.and(aBitSetCondition);
			}

			mapConditionListToBitSet.put(cl,aBitSetCl);
			// now calculate the statistics for Bayes Rule
			System.out.println("cardinality target");
			System.out.println(itsTarget.cardinality());
			System.out.println("cardinality known subgroup");
			System.out.println(aBitSetCl.cardinality());
			StatisticsBayesRule aStatisticsBR = new StatisticsBayesRule(aBitSetCl,itsTarget);
			mapConditionListBayesRule.put(cl, aStatisticsBR);
		}
	}//constructor

/*
		mapColumnToBitSet = new HashMap<Column, List<BitSet>>();
		for (Column col : mapColumnToConditionList.keySet()){
			//if (mapColumnToConditionList.containsKey(c))
			List<ConditionList> aListOfCl;
			aListOfCl = mapColumnToConditionList.get(col); 
			List<BitSet> aList = new ArrayList<BitSet>();
			for (ConditionList cl : aListOfCl){
				BitSet aBitSetCl = new BitSet(col.size()); // the bit set of the conditionlist, also set the size here, and all bits to one
				aBitSetCl.set(0, col.size());// or col.size -1? 
				for (Condition c : cl){
					BitSet aBitSetCondition = new BitSet();
					aBitSetCondition = c.getColumn().evaluate(c);
					aBitSetCl.and(aBitSetCondition);
				}
				aList.add(aBitSetCl);
			}
			mapColumnToBitSet.put(col, aList);
		}
*/

/*
		itsColumnsInvolved = new String[explanatoryConditions.length][];
		for (int i=0; i<explanatoryConditions.length; i++){
			itsColumnsInvolved[i] = new String[itsExplanatoryConditions[i].size()];
		}
		// fill list with strings to match later
		for (int i=0; i<explanatoryConditions.length; i++){
			for (int j=1;j<itsExplanatoryConditions[i].size();j++)
			{
				itsColumnsInvolved[i][j] = itsExplanatoryConditions[i].get(j).getColumn().getName();
			}
		}
	*/

/*
	public BitSet[] getBitSetsExplanatoryConditions(String[] attributeNames){ //returns an array of explanatory variables to be used as input into a global model estimator.
		//get dummy variables as input for logistic regression from known subgroups.
		
		BitSet[] aBitSetsExplanatoryConditions;
		aBitSetsExplanatoryConditions = new BitSet[itsExplanatoryConditions.length];
		for (int i=1;i<itsExplanatoryConditions.length;i++){
			aBitSetsExplanatoryConditions[i] =  itsExplanatoryConditions[i].getMembers();
		}
	return aBitSetsExplanatoryConditions;
	}
*/

	//Use HashSet or List??
	public Set<BitSet> getBitSets(Subgroup theSubgroupToEvaluate)
	{
		//returns bitsets corresponding to the attributes that are involved in the subgroup
		Set<BitSet> aBitSetsExplanatoryConditionLists = new HashSet<BitSet>();
		Set<ConditionList> aConditionListsInvolvedWithColumn = new HashSet<ConditionList>();
		// First obtain conditionLists that are involved with the subgroup
		for (Condition c : theSubgroupToEvaluate.getConditions())
			if (mapColumnToConditionList.get(c.getColumn()) != null)
				aConditionListsInvolvedWithColumn.addAll(mapColumnToConditionList.get(c.getColumn()));

		//now get the bitsets from  conditionLists involved from the mapping 
		for (ConditionList cl : aConditionListsInvolvedWithColumn)
			aBitSetsExplanatoryConditionLists.add(mapConditionListToBitSet.get(cl));

		return aBitSetsExplanatoryConditionLists;
	}

	public Set<StatisticsBayesRule> getStatisticsBayesRule(Subgroup theSubgroupToEvaluate)
	{
		//returns statistics corresponding to the attributes that are involved in the subgroup
		Set<StatisticsBayesRule> aSetStatisticsBayesRule = new HashSet<StatisticsBayesRule>();
		Set<ConditionList> aConditionListsInvolvedWithColumn = new HashSet<ConditionList>();
		// First obtain conditionLists that are involved with the subgroup
		for (Condition c : theSubgroupToEvaluate.getConditions())
		{
			System.out.println(c.getColumn().getName());
			if (mapColumnToConditionList.get(c.getColumn()) != null)
				aConditionListsInvolvedWithColumn.addAll(mapColumnToConditionList.get(c.getColumn()));

		}
		//now get the statistics from  conditionLists involved from the mapping 
		for (ConditionList cl : aConditionListsInvolvedWithColumn)
			aSetStatisticsBayesRule.add(mapConditionListBayesRule.get(cl));

		return aSetStatisticsBayesRule;
	}
}
