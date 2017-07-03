package nl.liacs.subdisc;

import java.util.*;

public class GlobalKnowledge {
/**
 * The global information contains the variables and conditions that are known
 * to correlate with the target.
 * This knowledge is captured in two lists. One with explanatory variables (as
 * columns), as well as known attribute-value conditions that correlate with the
 * target.
 * This knowledge can be 'transformed' into {@link BitSet}s.
 * This is useful for including these BitSets into a prediction model (i.e.
 * logistic regression).
 * For now we assume the explanatory variables to be 'binned' so they can be
 * represented as subgroup descriptions.
 */
	//private Column[] itsExplanatoryVariables;

	private List<ConditionList> itsExplanatoryConditions;
	//private String[][] itsColumnsInvolved; //describes the columns that are involved in a subgroup 
	//private BitSet[] tableWithExplanatoryVariables; //a table containing the members of each condition. 
	//private Map<Column, List<ConditionList>> mapColumnToConditionList;
	//private Map<Column, List<BitSet>> mapColumnToBitSet;
	private Map<ConditionList, BitSet> mapConditionListToBitSet;
	//each knowledge component is described by one (or more) conditions from the condition list
	private Map<ConditionList, StatisticsBayesRule> mapConditionListBayesRule;

	public GlobalKnowledge(List<ConditionList> explanatoryConditions, BitSet target)
	{
		itsExplanatoryConditions = explanatoryConditions;

		//mapColumnToConditionList = new HashMap<Column, List<ConditionList>>();
		//for (ConditionList cl : itsExplanatoryConditions) {
		//	for (Condition c : cl) {
				//if (!mapColumnToConditionList.containsKey(c.getColumn())) {
				//	List<ConditionList> aList = new ArrayList<ConditionList>();
				//	aList.add(cl);
				//	mapColumnToConditionList.put(c.getColumn(), aList);
				//}
				///else {
					
			//		List<ConditionList> aList = mapColumnToConditionList.get(c.getColumn());
			//		aList.add(cl);
			//		mapColumnToConditionList.put(c.getColumn(), aList);
		//		}
		//	}
		//	}

		mapConditionListToBitSet = new HashMap<ConditionList, BitSet>();
		mapConditionListBayesRule = new HashMap<ConditionList, StatisticsBayesRule>();

		for (ConditionList cl: itsExplanatoryConditions)
		{
			BitSet aBitSetCl = new BitSet(cl.get(0).getColumn().size()); // the bit set of the conditionlist, also set the size here, and all bits to one
			aBitSetCl.set(0, cl.get(0).getColumn().size());// or col.size -1? 
			for (Condition c : cl)
			{
				BitSet aBitSetCondition = new BitSet();
				aBitSetCondition = c.getColumn().evaluate(c);
				aBitSetCl.and(aBitSetCondition);
			}

			mapConditionListToBitSet.put(cl,aBitSetCl);
			// now calculate the statistics for Bayes Rule
			StatisticsBayesRule aStatisticsBR = new StatisticsBayesRule(aBitSetCl,target);
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
	public Set<BitSet> getBitSets()
	{
		//returns bitsets corresponding to all global knowledge
		Set<BitSet> aBitSetsExplanatoryConditionLists = new HashSet<BitSet>();
		//now get the bitsets from  conditionLists involved from the mapping 
		for (ConditionList cl : itsExplanatoryConditions)
			aBitSetsExplanatoryConditionLists.add(mapConditionListToBitSet.get(cl));

		return aBitSetsExplanatoryConditionLists;
	}

	public Set<StatisticsBayesRule> getStatisticsBayesRule()
	{
		//returns statistics corresponding to the attributes that are involved in the subgroup
		Set<StatisticsBayesRule> aSetStatisticsBayesRule = new HashSet<StatisticsBayesRule>();
		for (ConditionList cl : itsExplanatoryConditions)
			if (mapConditionListBayesRule.get(cl) != null)
				aSetStatisticsBayesRule.add(mapConditionListBayesRule.get(cl));

		return aSetStatisticsBayesRule;
	}
}
