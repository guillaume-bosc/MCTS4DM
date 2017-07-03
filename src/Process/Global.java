package Process;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.lucene.util.OpenBitSet;

import Data.Attribute;
import Data.DataType;
import Data.Enum;
import Data.Object;
import Data.Subgroup;

public class Global {
	/*
	 * ########################################################################
	 * Declaration of the attributes of the class
	 * ########################################################################
	 */
	public static Subgroup root;
	public static Subgroup subRoot;
	public static double maxMeasure = Double.MIN_VALUE;
	public static int indexIteration = 0;

	public static Attribute[] attributes;
	public static Object[] objects;
	public static Attribute[] targets;
	public static Map<String, Attribute> targetToId = new HashMap<String, Attribute>();

	public static int nbAttr;
	public static int nbChild;

	// Parameters of the program
	public static String qualitiesFile;
	public static String propertiesFile;

	public static DataType propType;

	public static int minSup = 15;
	public static int minSupTarget = 1;
	public static int nbLoops = 500000;
	public static int beamWidth = 100;
	public static Enum.Redundancy redundancyStrategy;
	public static boolean redundancyIdenticalLabels;
	public static double maxRedundancy = 1.5;
	public static int nbOutput = 100;
	public static int maxLength = Integer.MAX_VALUE;
	public static int maxLabel = 100;
	public static Enum.MctsType mctsType;
	public static String subsetLabels;
	public static boolean extendsWithLabels;

	public static String resFolderName = "Test";

	public static Double xBeta = 100.;
	public static Double lBeta = 30.;

	public static int topKUpdate = 1;
	public static int topKRollOut = 1; // top-K value for rollOut
	public static int topKMemory = 1; // top-K value for Memory policies
	public static int pathLength = -1; // upper bound for the path length in
										// roll out. -1 if frequent constraint

	public static Enum.UCB UCB;
	public static Enum.RefineExpand refineExpand;
	public static Enum.DuplicatesExpand duplicatesExpand;
	public static Enum.RefineRollOut refineRollOut;
	public static Enum.RewardPolicy rewardPolicy;
	public static Enum.MemoryPolicy memoryPolicy;
	public static Enum.UpdatePolicy updatePolicy;
	public static Enum.Measure measure;
	public static int jumpingLarge;

	// Writers
	public static BufferedWriter bufferResult;
	public static BufferedWriter bufferSupport;
	public static BufferedWriter bufferSupportE11;
	public static BufferedWriter bufferInfo;
	public static BufferedWriter bufferMean;
	public static String repositoryName;

	public static long runTime;

	public static PriorityQueue<Subgroup> allSubgroups = new PriorityQueue<Subgroup>(nbLoops,Subgroup.subgroupComparatorMeasureReverse);
	//public static List<Subgroup> allSubgroupsList = new ArrayList<Subgroup>();
	public static HashMap<Subgroup, Subgroup> uniqueResult = new HashMap<Subgroup, Subgroup>();

	public static OpenBitSet candidatesNominal;
	public static OpenBitSet candidatesBoolean;

	// Statistic values
	public static int nbRoll = 0;
	public static int nbEvaluationsRoll = 0;
	public static long timeSelect = 0;
	public static long timeExpand = 0;
	public static long timeRollOut = 0;
	public static long timeUpdate = 0;
	public static long timeUCT = 0;
	public static long nbRecupSqrt = 0;
	public static long nbCalculSqrt = 0;
	public static long nbRecupLog = 0;
	public static long nbCalculLog = 0;
	public static long timeComputeRollOut = 0;
	public static long timeCreatePath = 0;
	public static long timeCreatePathRoll = 0;
	public static long timeHandlePath = 0;
	public static long timeMemory = 0;
	public static long numberDuplicates = 0;
	public static List<Double> resultMeasures = new ArrayList<Double>();
	public static List<Integer> resultLength = new ArrayList<Integer>();
	public static int resultPatternCount = 0;
	public static double meanMeasure = 0.;
	public static int nbPatterns = 0;

	// Optimization structure
	public static double[] logList;
	public static Map<Integer, Double> mappingBeta = new HashMap<Integer, Double>();
	public static HashMap<OpenBitSet, OpenBitSet> bsSet = new HashMap<OpenBitSet, OpenBitSet>();
	public static HashMap<Integer, OpenBitSet> attrSupport = null;
	public static int[] attrSupportSize;
	public static double log2 = Math.log(2);
	public static HashMap<Subgroup, Subgroup> amaf = null;
	public static boolean wasNotInAmaf = true;
	public static List<List<List<Integer>>> sequenceIndex;
	// public static int[] mappingObjSortedToOriginal;

	public static String launchCommand = "\njava -jar OlfaMCTS.jar QualitiesFile PropertiesFile maxoutput nbLoops minSup xBeta lBeta minRedundancy resFolderName"
			+ "\n\t- QualitiesFile is the path to your qualities file"
			+ "\n\t- PropertiesFile is the path to your properties file"
			+ "\n\t- maxoutput is the number of the maxoutput best subgroups that will be resulting"
			+ "\n\t- nbLoops is the number of iterations in the MCTS for each label"
			+ "\n\t- minSup is the minimum support threshold (an integer)"
			+ "\n\t- xBeta is the center of the sigmoid function for Beta for the adapted F-Score"
			+ "\n\t- lBeta is the width of the sigmoid function for Beta for the adapted F-Score"
			+ "\n\t- minRedundancy is the minimum threshold for redundancy (values in [0 ; 3], 3 not consider redundant subgroup whereas 0 consider all couples of subgroups as redundant)"
			+ "\n\t- resFolderName The name of the folder which is created to contain the resulting files\n\n";

	/*
	 * ########################################################################
	 * End of declaration of the attributes of the class
	 * ########################################################################
	 */

	public static void displayTargets() {
		for (int i = 0; i < targets.length; i++) {
			System.out.println(targets[i]);
		}
	}

	public static void displayAttributes() {
		for (int i = 0; i < attributes.length; i++) {
			System.out.println(attributes[i]);
		}
	}

	public static void displayObjects() {
		for (int i = 0; i < objects.length; i++) {
			System.out.println(objects[i]);
		}
	}

	public static void displayRunTime() {
		long totTime = timeExpand + timeSelect + timeRollOut + timeUpdate;
		DecimalFormat df = new DecimalFormat("0.00");
		System.out.println("Select : " + timeSelect + "ms (" + df.format(((double) (timeSelect) / totTime)) + "%) dont "
				+ timeUCT + "ms pour UCT (" + df.format(((double) (timeUCT) / timeSelect)) + "%)");
		System.out.println("Expand : " + timeExpand + "ms (" + df.format(((double) (timeExpand) / totTime)) + "%)");
		System.out.println("RollOut : " + timeRollOut + "ms (" + df.format(((double) (timeRollOut) / totTime))
				+ "%) dont " + timeComputeRollOut + "ms pour computeMeasure ("
				+ df.format(((double) (timeComputeRollOut) / timeRollOut)) + "%) " + timeCreatePath
				+ "ms pour timeCreatePath (" + df.format(((double) (timeCreatePath) / timeRollOut)) + "%) "
				+ timeCreatePathRoll + "ms pour timeCreatePathRoll ("
				+ df.format(((double) (timeCreatePathRoll) / timeRollOut)) + "%) " + timeHandlePath
				+ "ms pour timeHandlePath (" + df.format(((double) (timeHandlePath) / timeRollOut)) + "%) " + timeMemory
				+ "ms pour timeMemory (" + df.format(((double) (timeMemory) / timeRollOut)) + "%) ");
		System.out.println("Update : " + timeUpdate + "ms (" + ((double) (timeUpdate) / totTime) + "%)");
		// System.out.println("Number duplicates found : " + numberDuplicates);
		// System.out.println("Max measure : " + maxMeasure);
	}

	/**
	 * Write the information file info.log
	 */
	public static void writeInfo() {
		try {
			// Write the values of the parameters of this run
			Global.bufferInfo.write(Global.writeParameters());

			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			Global.bufferInfo.write("\n\n=== Result Information ===");
			Global.bufferInfo.write("\ndate : " + sdf.format(date));
			Global.bufferInfo.write("\nruntime(ms) : " + Global.runTime);
			Global.bufferInfo.write("\nnbPatterns : " + Global.resultPatternCount);

			String measureString = "";
			DecimalFormat df = new DecimalFormat("####0.00");
			for (Double m : resultMeasures) {
				if (!measureString.isEmpty())
					measureString += " ";
				measureString += df.format(m).replace(",", ".");
			}
			Global.bufferInfo.write("\nmeasures : " + measureString);

			String lengthString = "";
			for (int m : resultLength) {
				if (!lengthString.isEmpty())
					lengthString += " ";
				lengthString += m;
			}
			Global.bufferInfo.write("\ndescriptionLength : " + lengthString);

			Global.bufferInfo.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Builds the String containing the values of the parameters used for this
	 * run
	 * 
	 * @return The String containing the values of parameters
	 */
	public static String writeParameters() {
		String parametersString = "=== Parameters ===";
		parametersString += "\nQualitiesFiles : " + qualitiesFile;
		parametersString += "\nPropertiesFiles : " + propertiesFile;
		parametersString += "\nattrType : " + propType;

		parametersString += "\nresFolder : " + resFolderName;

		parametersString += "\nminSupp : " + minSup;
		parametersString += "\nnbLoops : " + nbLoops;
		parametersString += "\nmaxOutput : " + nbOutput;
		parametersString += "\nmaxRedundancy : " + maxRedundancy;
		parametersString += "\nmaxLength : " + maxLength;

		parametersString += "\nmeasure : " + measure;
		if (measure == Enum.Measure.FBeta || measure == Enum.Measure.RelativeFBeta
				|| measure == Enum.Measure.WeightedRelativeFBeta) {
			parametersString += "\nxBeta : " + xBeta;
			parametersString += "\nlBeta : " + lBeta;
		}

		parametersString += "\nUCB : " + UCB;

		parametersString += "\nrefineExpand : " + refineExpand;

		parametersString += "\nduplicatesExpand : " + duplicatesExpand;

		parametersString += "\npathLength : " + pathLength;
		parametersString += "\nrefineRollOut : " + refineRollOut;
		if (refineRollOut == Enum.RefineRollOut.Large)
			parametersString += "\njumpingLarge : " + jumpingLarge;

		parametersString += "\nrewardPolicy : " + rewardPolicy;
		if (rewardPolicy == Enum.RewardPolicy.MeanTopK)
			parametersString += "\ntopKRollOut : " + topKRollOut;

		parametersString += "\nmemoryPolicy : " + memoryPolicy;
		if (memoryPolicy == Enum.MemoryPolicy.TopK)
			parametersString += "\ntopKMemory : " + topKMemory;

		parametersString += "\nupdatePolicy : " + updatePolicy;
		if (updatePolicy == Enum.UpdatePolicy.MeanTopK)
			parametersString += "\ntopKMemory : " + topKUpdate;

		return parametersString;
	}

	public static void addToResultSet(Collection<Subgroup> c) {
		for (Subgroup s : c) {
			Global.addToResultSet(s);
		}
	}

	public static void addToResultSet(Subgroup s) {
		Subgroup uniqueOne = Global.uniqueResult.get(s);
		if (uniqueOne == null) {
			// Global.allSubgroupsList.add(s);
			Global.allSubgroups.add(s);
			Global.uniqueResult.put(s, s);
		}
	}

	public static void initialize() {
		maxMeasure = Double.MIN_VALUE;
		indexIteration = 0;
		targetToId = new HashMap<String, Attribute>();

		 allSubgroups = new PriorityQueue<Subgroup>(nbLoops,Subgroup.subgroupComparatorMeasureReverse);
		//allSubgroupsList = new ArrayList<Subgroup>();
		uniqueResult = new HashMap<Subgroup, Subgroup>();

		candidatesNominal = null;
		candidatesBoolean = null;
		
		// Statistic values
		nbRoll = 0;
		timeSelect = 0;
		timeExpand = 0;
		timeRollOut = 0;
		timeUpdate = 0;
		timeUCT = 0;
		nbRecupSqrt = 0;
		nbCalculSqrt = 0;
		nbRecupLog = 0;
		nbCalculLog = 0;
		timeComputeRollOut = 0;
		timeCreatePath = 0;
		timeCreatePathRoll = 0;
		timeHandlePath = 0;
		timeMemory = 0;
		numberDuplicates = 0;
		resultMeasures = new ArrayList<Double>();
		resultLength = new ArrayList<Integer>();
		resultPatternCount = 0;
		meanMeasure = 0.;
		nbPatterns = 0;

		// Optimization structure
		bsSet.clear();
		;
		attrSupport = null;
		if (amaf != null)
			amaf.clear();
	}

}
