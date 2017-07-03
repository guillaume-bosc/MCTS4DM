package nl.liacs.subdisc;

import java.util.*;

import javax.swing.*;

import nl.liacs.subdisc.gui.*;

public class Process
{
	// leave at false in svn head
	private static final boolean CAUC_LIGHT = false;
	private static boolean CAUC_HEAVY = false;
	private static final boolean CAUC_HEAVY_CONVEX = false; // select subgroups on convex hull if true, select top-1 if false

	public static SubgroupDiscovery runSubgroupDiscovery(Table theTable, int theFold, BitSet theBitSet, SearchParameters theSearchParameters, boolean showWindows, int theNrThreads, JFrame theMainWindow)
	{
		TargetType aTargetType = theSearchParameters.getTargetConcept().getTargetType();

		if (!TargetType.isImplemented(aTargetType))
			return null;

		SubgroupDiscovery aSubgroupDiscovery = null;
		echoMiningStart();
		long aBegin = System.currentTimeMillis();

		switch (aTargetType)
		{
			case SINGLE_NOMINAL :
			{
				//recompute itsPositiveCount, as we may be dealing with cross-validation here, and hence a smaller number
				TargetConcept aTargetConcept = theSearchParameters.getTargetConcept();
				String aTargetValue = aTargetConcept.getTargetValue();
				int itsPositiveCount = aTargetConcept.getPrimaryTarget().countValues(aTargetValue);
				aSubgroupDiscovery = new SubgroupDiscovery(theSearchParameters, theTable, itsPositiveCount, theMainWindow);
				break;
			}

			case SINGLE_NUMERIC:
			{
				// new runCAUC() receives result after SD.mine()
				// not fully implemented yet
				if (CAUC_HEAVY)
				{
					caucHeavy(theTable, theFold, theBitSet, theSearchParameters, showWindows, theNrThreads);
					return null;
				}
				else
				{
					//recompute this number, as we may be dealing with cross-validation here, and hence a different value
					float itsTargetAverage = theSearchParameters.getTargetConcept().getPrimaryTarget().getAverage();
					Log.logCommandLine("average: " + itsTargetAverage);
					aSubgroupDiscovery = new SubgroupDiscovery(theSearchParameters, theTable, itsTargetAverage, theMainWindow);
				}
				break;
			}
			case MULTI_LABEL :
			{
				aSubgroupDiscovery = new SubgroupDiscovery(theSearchParameters, theTable, theMainWindow);
				break;
			}
			case DOUBLE_REGRESSION :
			{
				aSubgroupDiscovery = new SubgroupDiscovery(theSearchParameters, theTable, true, theMainWindow);
				break;
			}
			case DOUBLE_CORRELATION :
			{
				aSubgroupDiscovery = new SubgroupDiscovery(theSearchParameters, theTable, false, theMainWindow);
				break;
			}
			default :
			{
				throw new AssertionError(String.format("%s: %s '%s' not implemented",
									Process.class.getName(),
									TargetType.class.getName(),
									aTargetType));
			}
		}
		aSubgroupDiscovery.mine(System.currentTimeMillis(), theNrThreads);
		// if 2nd argument to above mine() is 0, you effectively run:
		//aSubgroupDiscovery.mine(System.currentTimeMillis());

		long anEnd = System.currentTimeMillis();
		float aMaxTime = theSearchParameters.getMaximumTime();

		if (aMaxTime > 0.0f && (anEnd > (aBegin + aMaxTime*60*1000)))
		{
			String aMessage = "Mining process ended prematurely due to time limit.";
			if (showWindows)
				JOptionPane.showMessageDialog(null,
								aMessage,
								"Time Limit",
								JOptionPane.INFORMATION_MESSAGE);
			else
				Log.logCommandLine(aMessage);
		}

		echoMiningEnd(anEnd - aBegin, aSubgroupDiscovery.getNumberOfSubgroups());

		if (showWindows)
			new ResultWindow(theTable, aSubgroupDiscovery, theFold, theBitSet);

		if (CAUC_LIGHT)
			caucLight(aSubgroupDiscovery, theBitSet);

/*		// temporary bonus results for CAUC experimentation
		SubgroupSet aSDResult = aSubgroupDiscovery.getResult();
		boolean aCommandlinelogState = Log.COMMANDLINELOG;
		Log.COMMANDLINELOG = false;
		SubgroupSet aSubgroupSetWithEntropy = aSDResult.getPatternTeam(theTable, aSDResult.size());

		Log.COMMANDLINELOG = aCommandlinelogState;
		Log.logCommandLine("======================================================");
		Log.logCommandLine("Simple Subgroup Set Size  : " + aSubgroupSetWithEntropy.size());
		Log.logCommandLine("Joint Entropy             : " + aSubgroupSetWithEntropy.getJointEntropy());
		Log.logCommandLine("Entropy / Set Size        : " + aSubgroupSetWithEntropy.getJointEntropy()/aSubgroupSetWithEntropy.size());
		Log.logCommandLine("Subgroups : ");
		for (Subgroup s : aSubgroupSetWithEntropy)
			Log.logCommandLine("    "+s.getConditions().toString());
*/		// end temp

		return aSubgroupDiscovery;
	}

	private static void caucLight(SubgroupDiscovery theSubgroupDiscovery, BitSet theBitSet)
	{
		assert theSubgroupDiscovery.getSearchParameters().getTargetConcept().getTargetType() == TargetType.SINGLE_NUMERIC;

		final Column aTarget = theSubgroupDiscovery.getSearchParameters().getTargetConcept().getPrimaryTarget();
		final SubgroupSet aSet = theSubgroupDiscovery.getResult();
		final BitSet aMembers = membersCheck(theBitSet, aTarget.size());
		final float[] aDomain = aTarget.getUniqueNumericDomain(aMembers);

		// last index is whole dataset
		List<List<Float>> statistics = new ArrayList<List<Float>>(aDomain.length-1);
		for (int i = 0, j = aDomain.length-1; i < j; ++i)
		{
			BitSet aCAUCSet = (BitSet) aMembers.clone();
			caucMembers(aTarget, aDomain[i], aCAUCSet);

			// hack to use binary target for numeric target
			aSet.setBinaryTarget(aCAUCSet);

			statistics.add(compileStatistics(aDomain[i],
							aCAUCSet.cardinality(),
							aSet));
		}
		// dump results
		caucWrite("caucLight", aTarget, statistics);
	}

	private static void caucHeavy(Table theTable, int theFold, BitSet theBitSet, SearchParameters theSearchParameters, boolean showWindows, int theNrThreads)
	{
		// set to false, so normal SD is run
		CAUC_HEAVY = false;
		// use 'showWindows = false' below to avoid numerous windows
		final boolean showWindowsSetting = showWindows;

		final Column aBackup = theSearchParameters.getTargetConcept().getPrimaryTarget().copy();
		final BitSet aMembers = membersCheck(theBitSet, aBackup.size());
		final float[] aDomain = aBackup.getUniqueNumericDomain(aMembers);

		final List<Column> aColumns = theTable.getColumns();
		final String aName = aBackup.getName();
		final String aShort = aBackup.getShort();
		final int anIndex = aBackup.getIndex();
		final int aNrRows = aBackup.getIndex();

		// column will be binary instead of numeric
		final TargetConcept tc = theSearchParameters.getTargetConcept();
		tc.setTargetType(TargetType.SINGLE_NOMINAL.GUI_TEXT);
		tc.setTargetValue("1");
		// set an alternative quality measure
		final QM backupQM = theSearchParameters.getQualityMeasure();
		final QM altQM = QM.WRACC;
		theSearchParameters.setQualityMeasure(altQM);
		// set an alternative quality measure minimum
		final float backupMM = theSearchParameters.getQualityMeasureMinimum();
		theSearchParameters.setQualityMeasureMinimum(Float.parseFloat(altQM.MEASURE_DEFAULT));

		SubgroupSet aHeavySubgroupSet = new SubgroupSet(-1);

		// last index is whole dataset
		for (int i = 0, j = aDomain.length-1; i < j; ++i)
		{
			BitSet aCAUCSet = (BitSet) aMembers.clone();
			caucMembers(aBackup, aDomain[i], aCAUCSet);

			// create temporary Column
			Column aColumn = new Column(aName,
							aShort,
							AttributeType.BINARY,
							anIndex,
							aNrRows);

			// set Column members
			for (int k = 0, m = aBackup.size(); k < m; ++k)
				aColumn.add(aCAUCSet.get(k));

			// use Column in Table
			aColumns.set(anIndex, aColumn);

			// set the new column as primary target
			theSearchParameters.getTargetConcept().setPrimaryTarget(aColumn);

			// run SD
			boolean aCommandlinelogState = Log.COMMANDLINELOG;
			Log.COMMANDLINELOG = false;
			SubgroupDiscovery sd =
				runSubgroupDiscovery(theTable, theFold,	aMembers, theSearchParameters, false, theNrThreads, null);
			Log.COMMANDLINELOG = aCommandlinelogState;

			// For seeing the intermediate ROC curves, uncomment the next line
			//new ROCCurveWindow(sd.getResult(), theSearchParameters, sd.getQualityMeasure());

			Log.logCommandLine("Threshold value : " + aDomain[i]);

			if (CAUC_HEAVY_CONVEX)
			{
				// this seems pointless, but the ROC curve needs to be computed to prevent the next line from NullPointerError'ing
				ROCCurve aROCCurve = new ROCCurve(sd.getResult(), theSearchParameters, sd.getQualityMeasure());

				SubgroupSet ROCSubgroups = sd.getResult().getROCListSubgroupSet();

				// force update(), should have been in .getROCListSubgroupSet()
				ROCSubgroups.size();

				//Log.logCommandLine("ROC subgroups : " + aSize);
				for (Subgroup s : ROCSubgroups)
					Log.logCommandLine("    " + s.getConditions().toString());

				//select convex hull subgroups from the resulting subgroup set
				aHeavySubgroupSet.addAll(ROCSubgroups);

				// compile statistics
//				statistics.add(compileStatistics(aDomain[i],
//								aCAUCSet.cardinality(),
//								sd.getResult()));
			}
			else
			{
				SubgroupSet aResult = sd.getResult();
				int aSize = aResult.size();
				if (aSize>0)
				{
					Subgroup aTopOneSubgroup = aResult.first();
					Log.logCommandLine("Subgroup : ");
					Log.logCommandLine("    " + aTopOneSubgroup.getConditions().toString());
					aHeavySubgroupSet.add(aTopOneSubgroup);
				}
			}
		}

		// dump results
//		caucWrite("caucHeavy", aBackup, statistics);

		boolean aCommandlinelogState = Log.COMMANDLINELOG;
		Log.COMMANDLINELOG = false;
		SubgroupSet aSubgroupSetWithEntropy = aHeavySubgroupSet.getPatternTeam(theTable, aHeavySubgroupSet.size());
		Log.COMMANDLINELOG = aCommandlinelogState;

		Log.logCommandLine("======================================================");
		Log.logCommandLine("Diverse Subgroup Set Size : " + aHeavySubgroupSet.size());
//		Log.logCommandLine("Joint Entropy             : " + aHeavySubgroupSet.getJointEntropy());
//		Log.logCommandLine("Entropy / Set Size        : " + aHeavySubgroupSet.getJointEntropy()/aHeavySubgroupSet.size());
		Log.logCommandLine("Joint Entropy             : " + aSubgroupSetWithEntropy.getJointEntropy());
		Log.logCommandLine("Entropy / Set Size        : " + aSubgroupSetWithEntropy.getJointEntropy()/aHeavySubgroupSet.size());
		Log.logCommandLine("Subgroups : ");
		for (Subgroup s : aHeavySubgroupSet)
			Log.logCommandLine("    "+s.getConditions().toString());

		// restore original Column
		aColumns.set(anIndex, aBackup);
		tc.setPrimaryTarget(aBackup);
		tc.setTargetType(TargetType.SINGLE_NUMERIC.GUI_TEXT);
		// restore original SearchParameters
		theSearchParameters.setQualityMeasure(backupQM);
		theSearchParameters.setQualityMeasureMinimum(backupMM);

		// back to start
		CAUC_HEAVY = true;
		showWindows = showWindowsSetting;
	}

	private static BitSet membersCheck(BitSet theBitSet, int theSize)
	{
		if (theBitSet != null)
			return theBitSet;
		else
		{
			BitSet aMembers = new BitSet(theSize);
			aMembers.set(0, theSize);
			return aMembers;
		}
	}

	// simple updating of members bitset is possible if threshold test loop
	// is backwards, starting at before-last threshold, ending with first
	private static void caucMembers(Column theColumn, float theThreshold, BitSet theBitSet)
	{
		for (int k = theBitSet.nextSetBit(0); k >= 0; k = theBitSet.nextSetBit(k + 1))
			if (theColumn.getFloat(k) > theThreshold)
				theBitSet.clear(k);
	}

	public static void echoMiningStart()
	{
		Log.logCommandLine("Mining process started");
	}

	public static void echoMiningEnd(long theMilliSeconds, int theNumberOfSubgroups)
	{
		int seconds = Math.round(theMilliSeconds / 1000);
		int minutes = Math.round(theMilliSeconds / 60000);
		int secondsRemainder = seconds - (minutes * 60);
		String aString = new String("Mining process finished in " + minutes
				+ " minutes and " + secondsRemainder + " seconds.\n");

		if (theNumberOfSubgroups == 0)
			aString += "   No subgroups found that match the search criterion.\n";
		else if (theNumberOfSubgroups == 1)
			aString += "   1 subgroup found.\n";
		else
			aString += "   " + theNumberOfSubgroups + " subgroups found.\n";
		Log.logCommandLine(aString);
	}

	private static List<Float> compileStatistics(float theThreshold, int theNrMembers, SubgroupSet theSubgroupSet)
	{
		// [threshold, n, AUC, fpr_1, tpr_1, ..., fpr_h, tpr_h]
		List<Float> stats = new ArrayList<Float>();
		stats.add(theThreshold);
		stats.add((float) theNrMembers);
		stats.add(theSubgroupSet.getROCList().getAreaUnderCurve());
		stats.add(0.0f);
		stats.add(0.0f);
		for (Object[] oa : theSubgroupSet.getROCListSubgroups())
		{
			stats.add((Float) oa[1]);
			stats.add((Float) oa[2]);
		}
		stats.add(1.0f);
		stats.add(1.0f);
		return stats;
	}

	// to std.out or file
	private static void caucWrite(String theCaller, Column theTarget, List<List<Float>> theStatistics)
	{
		// write or print to std.out
		System.out.println("#" + theCaller);
		System.out.println("#" + theTarget.getName());
		System.out.println("#threshold,n,AUC,frp_1,tpr_1,...,fpr_h,tpr_h");
		for (List<Float> l : theStatistics)
		{
			String aTemp = l.toString().replaceAll(", ", ",");
			System.out.println(aTemp);
		}
		new CAUCWindow(theTarget, theStatistics);
	}
}
