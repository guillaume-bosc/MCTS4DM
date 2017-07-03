package nl.liacs.subdisc;

import java.util.*;
import weka.core.Instances;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;

/**
 * The propensity score is the column with expected values per point.
 * It is calculated by regressing X on the target, it can either be calculated
 * by Bayes Rule, or with the use of logistic regression.
 * To calculate it we need X and a target vector.
 */
public class PropensityScore
{
	public static final String BAYES_RULE = "BayesRule";
	public static final String LOGISTIC_REGRESSION = "LogisticRegression";

	private final String itsMethod;
	private final BitSet itsTarget;
	private final LocalKnowledge itsLocalKn;
	private final GlobalKnowledge itsGlobalKn;
	private final Subgroup itsSubgroup;
	private final double[] itsPropensityScore;
	private final double itsPropensityScoreSum;

	public PropensityScore(Subgroup theSubgroup, BitSet theTarget, LocalKnowledge theLocalKn, GlobalKnowledge theGlobalKn, String theMethod)
	{
		itsMethod = theMethod;
		itsTarget = theTarget;
		itsLocalKn = theLocalKn;
		itsGlobalKn = theGlobalKn;
		itsSubgroup = theSubgroup;
		itsPropensityScore = new double[itsTarget.size()];

		if (BAYES_RULE.equals(itsMethod))
		{
			calculateBayesRule();
//			/*calculate Bayes Rule here */
//			Set<statisticsBayesRule> aAllStatisticsBayesRule = new HashSet<statisticsBayesRule>();
//			aAllStatisticsBayesRule.addAll(itsGlobalKn.getStatisticsBayesRule());
//			aAllStatisticsBayesRule.addAll(itsLocalKn.getStatisticsBayesRule(itsSubgroup));
//			System.out.println("Size overlapping subgroups:");
//			System.out.println(aAllStatisticsBayesRule.size());
//
//			//now calculate the statistics for the propensity score
//			//P(AB|T)P(T) / P(AB)
//
//			//System.out.println("calculating statistics");
//			double aPT = ((double)itsTarget.cardinality()) / itsTarget.size();
//			double aPNotT = 1-aPT;
//			double[] aTerm1 =  new double[(itsTarget.size())];
//			double[] aTerm2 =  new double[(itsTarget.size())];
//			for (int i=0; i<aTerm1.length; i++)
//			{
//				aTerm1[i] = aPT;
//				aTerm2[i] = aPNotT;
//			}
//
//			for (statisticsBayesRule s : aAllStatisticsBayesRule)
//			{
//				for (int j=0; j<aTerm1.length; j++)
//				{
//					aTerm1[j]=aTerm1[j]*s.getProbabilitiesDataPXGivenT()[j];
//					aTerm2[j]=aTerm2[j]*s.getProbabilitiesDataPXGivenNotT()[j];
//				}
//			}
//
//			itsPropensityScore = new double[aTerm1.length];
//
//			for (int i=0; i<aTerm1.length; i++)
//				itsPropensityScore[i] = aTerm1[i]/(aTerm1[i] + aTerm2[i]);
//
//			if (aAllStatisticsBayesRule.isEmpty())
//			{
//				System.out.println("Knowledge variables are empty!!!");
//				itsPropensityScore = new double[(itsTarget.size())];
//				aPT = ((double) itsTarget.cardinality()) /  itsTarget.size();
//				for (int i=0; i<itsPropensityScore.length; i++)
//					itsPropensityScore[i] = aPT;
//			}
		}
		else if (LOGISTIC_REGRESSION.equals(itsMethod))
		{
			calculateLogisticRegression();
//			//System.out.println("LogisticRegression");
//			itsMethod = theMethod;
//			itsTarget = theTarget;
//			itsLocalKn = theLocalKn;
//			itsGlobalKn = theGlobalKn;
//			itsPropensityScore = new double[itsTarget.size()];
//			Set<BitSet> explanatoryVariables = new HashSet<BitSet>();
//			explanatoryVariables.addAll(itsGlobalKn.getBitSets());
//			explanatoryVariables.addAll(itsLocalKn.getBitSets(itsSubgroup));
//			//java.lang.String name, Fastvector attInfo, int capacity
//			//create attribute list with names
//			double nameNumber = 1.0;
//			String s1 = String.valueOf(nameNumber);
//			FastVector attributeList = new FastVector(explanatoryVariables.size() + 1); // plus one because target attribute is also in there
//			for (BitSet v : explanatoryVariables)
//			{
//				Attribute at = new Attribute(s1);
//				attributeList.addElement(at);
//				nameNumber++;
//				s1 = String.valueOf(nameNumber);
//			}
//			// XXX safer alternative, avoid String reference trouble
////			for (int i = 0, j = explanatoryVariables.size(); i < j; )
////				attributeList.addElement(new Attribute(Double.toString(++i)));
//
//			FastVector valuesTarget = new FastVector(2);
//			valuesTarget.addElement("0");
//			valuesTarget.addElement("1");
//			Attribute target = new Attribute("target", valuesTarget);
//			attributeList.addElement(target);
//
//			//attribute list contains the information about attributes
//
//			//System.out.println("Fastvector with attributes created");
//			Instances data = new Instances("explanatoryVariables", attributeList, itsTarget.size());
//			//System.out.println("empty instances created");
//			//String st1 = "Size attributeList = " + attributeList.size();
//			//System.out.println(st1);
//
//			for (int i=0; i<itsTarget.size(); i++)
//			{
//				Instance ins = new Instance(attributeList.size());
//				ins.setDataset(data);
//				//System.out.println("empty instance created");
//				//double[] attValues = new double[attributeList.size()];
//				int j=0;
//				for (BitSet v : explanatoryVariables)
//				{
//					double value = (v.get(i) ? 1 : 0);
////					if (v.get(i))
////						value=1;
////					else
////						value=0;
//					ins.setValue(j, value);
//					j++;
//				}// now fill the last (target) attribute
//
//				if (itsTarget.get(i))
//				{
//					//attValues[attValues.length]=1;
//					ins.setValue(j, "1");
//				}
//				else
//				{
//					//attValues[attValues.length]=0;
//					ins.setValue(j, "0");
//				}
//
//				//System.out.println("single Instance created");
//				data.add(ins);
//			}
//			//System.out.println("instances created");
//
//			data.setClassIndex(data.numAttributes()-1);
//			//String st = "" + (data.numAttributes()-1);
//			//System.out.println(st);
//			//System.out.println("Set Class Index");
//
//			Logistic logisticClassifier = new Logistic();
//			logisticClassifier.setRidge(0);
//			try
//			{
//				logisticClassifier.buildClassifier(data);
//			}
//			catch (Exception e)
//			{
//				System.out.println("no classifier built!");
//				e.printStackTrace();
//			}
//			System.out.println("Logistic Regression model created");
//
//			//now classify instances
//			for (int i=0; i<itsTarget.size(); i++)
//			{
//				double[] distributionPoint;
//				try
//				{
//					distributionPoint = logisticClassifier.distributionForInstance(data.instance(i));
//					itsPropensityScore[i] = distributionPoint[1];
//				}
//				catch (Exception e)
//				{
//					System.out.println("Class distribution could not be computed");
//					e.printStackTrace();
//				}
//			}
//
//			System.out.println("propensity score filled");
		}
		else
		{
			itsPropensityScoreSum = Double.NaN;
			return;
		}

		double aSum = 0.0;
		for (double d : itsPropensityScore)
			aSum += d;
		itsPropensityScoreSum = aSum;
	}

	private void calculateBayesRule()
	{
		/* calculate Bayes Rule here */
		Set<StatisticsBayesRule> aAllStatisticsBayesRule = new HashSet<StatisticsBayesRule>();
		aAllStatisticsBayesRule.addAll(itsGlobalKn.getStatisticsBayesRule());
		aAllStatisticsBayesRule.addAll(itsLocalKn.getStatisticsBayesRule(itsSubgroup));
		System.out.println("Size overlapping subgroups:");
		System.out.println(aAllStatisticsBayesRule.size());

		final int aTargetSize = itsTarget.size();
		final double aPT = ((double)itsTarget.cardinality()) / aTargetSize;
		final double aPNotT = 1.0-aPT;

// check this first, no need to do other complicated thing if true
		// set all propensity scores to prior expectation and return
		if (aAllStatisticsBayesRule.isEmpty())
		{
			System.out.println("Knowledge variables are empty!!!");
			for (int i=0; i<aTargetSize; i++)
				itsPropensityScore[i] = aPT;
			return;
		}

		//now calculate the statistics for the propensity score
		//P(AB|T)P(T) / P(AB)

		//System.out.println("calculating statistics");
		double[] aTerm1 =  new double[aTargetSize];
		Arrays.fill(aTerm1, aPT);
		double[] aTerm2 =  new double[aTargetSize];
		Arrays.fill(aTerm2, aPNotT);

// replaced by Arrays.fill()
//		for (int i=0; i<aTerm1.length; i++)
//		{
//			aTerm1[i] = aPT;
//			aTerm2[i] = aPNotT;
//		}

		for (StatisticsBayesRule s : aAllStatisticsBayesRule)
		{
			// get double[]s once per outer-loop and reuse them
			double[] pGivenT = s.getProbabilitiesDataPXGivenT();
			double[] pNotGivenT = s.getProbabilitiesDataPXGivenT();

			for (int i=0; i<aTargetSize; i++)
			{
//				aTerm1[j]=aTerm1[j]*s.getProbabilitiesDataPXGivenT()[j];
//				aTerm2[j]=aTerm2[j]*s.getProbabilitiesDataPXGivenNotT()[j];
				aTerm1[i] *= pGivenT[i];
				aTerm2[i] *= pNotGivenT[i];
			}
		}

// created already, does not change
//		itsPropensityScore = new double[aTerm1.length];

		for (int i=0; i<aTargetSize; i++)
			itsPropensityScore[i] = aTerm1[i]/(aTerm1[i] + aTerm2[i]);
	}

	private void calculateLogisticRegression()
	{
		//System.out.println("LogisticRegression");
// created/ set already, no need to redo this
//		itsMethod = theMethod;
//		itsTarget = theTarget;
//		itsLocalKn = theLocalKn;
//		itsGlobalKn = theGlobalKn;
//		itsPropensityScore = new double[itsTarget.size()];
		Set<BitSet> explanatoryVariables = new HashSet<BitSet>();
		explanatoryVariables.addAll(itsGlobalKn.getBitSets());
		explanatoryVariables.addAll(itsLocalKn.getBitSets(itsSubgroup));

		//java.lang.String name, Fastvector attInfo, int capacity
		//create attribute list with names
//		double nameNumber = 1.0;
//		String s1 = String.valueOf(nameNumber);
		// NOTE +1 because target attribute will also go in there
		FastVector attributeList = new FastVector(explanatoryVariables.size() + 1);
//		for (BitSet v : explanatoryVariables)
//		{
//			Attribute at = new Attribute(s1);
//			attributeList.addElement(at);
//			nameNumber++;
//			s1 = String.valueOf(nameNumber);
//		}
// XXX safer alternative, avoid String reference trouble
		for (int i = 0, j = explanatoryVariables.size(); i < j; )
			attributeList.addElement(new Attribute(Double.toString(++i)));

		FastVector valuesTarget = new FastVector(2);
		valuesTarget.addElement("0");
		valuesTarget.addElement("1");
		Attribute target = new Attribute("target", valuesTarget);
		attributeList.addElement(target);

		//attribute list contains the information about attributes

		//System.out.println("Fastvector with attributes created");
		final int aTargetSize = itsTarget.size();
		final int anAttributeListSize = attributeList.size();
		Instances data = new Instances("explanatoryVariables", attributeList, aTargetSize);
		//System.out.println("empty instances created");
		//String st1 = "Size attributeList = " + attributeList.size();
		//System.out.println(st1);

		for (int i=0; i<aTargetSize; i++)
		{
			Instance ins = new Instance(anAttributeListSize);
			ins.setDataset(data);
			//System.out.println("empty instance created");
			//double[] attValues = new double[attributeList.size()];
			int j=0;
			for (BitSet v : explanatoryVariables)
			{
				// double value = (v.get(i) ? 1 : 0);
//				if (v.get(i))
//					value=1;
//				else
//					value=0;
				ins.setValue(j, (v.get(i) ? 1 : 0));
				++j;
			}// now fill the last (target) attribute

//			if (itsTarget.get(i))
//			{
//				//attValues[attValues.length]=1;
//				ins.setValue(j, "1");
//			}
//			else
//			{
//				//attValues[attValues.length]=0;
//				ins.setValue(j, "0");
//			}
			ins.setValue(j, (itsTarget.get(i) ? 1 : 0));

			//System.out.println("single Instance created");
			data.add(ins);
		}
		//System.out.println("instances created");

		data.setClassIndex(data.numAttributes()-1);
		//String st = "" + (data.numAttributes()-1);
		//System.out.println(st);
		//System.out.println("Set Class Index");

		// replace logistic code with this
		try
		{
			logisticClassification(data, itsPropensityScore);
		}
		catch (Exception e)
		{
			Log.logCommandLine("logistic classification failed");
			e.printStackTrace();
		}

		Log.logCommandLine("propensity score filled");
	}

	// if any of the methods throw an Exception: abort this whole process
	private static void logisticClassification(Instances theData, double[] thePropensityScores) throws Exception
	{
		// build a logistic classifier
		Logistic aLC = new Logistic();
		aLC.setRidge(0);
		aLC.buildClassifier(theData);

		Log.logCommandLine("Logistic Regression model created");

		//now classify instances
		for (int i=0; i<thePropensityScores.length; i++)
			thePropensityScores[i] = aLC.distributionForInstance(theData.instance(i))[1];
	}

	@Deprecated
	public double[] getPropensityScore()
	{
		return itsPropensityScore;
	}

	public double getPropensityScoreSum()
	{
		return itsPropensityScoreSum;
	}
}
