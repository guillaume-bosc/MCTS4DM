package Process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.lucene.util.OpenBitSet;

import Data.DataType;
import Data.Subgroup;
import Data.Enum;

public class Main {

	public static void main(String[] args) {
		try {
			long startTime = System.currentTimeMillis();

			if (args.length > 1) {
				if (!readParameters(args))
					return;
			} else {
				if (!readParametersFromFile(args[0]))
					return;
			}

			getTargetsLength();
			switch (Global.propType) {
			case NUMERIC:
				readFileAttr();
				break;
			case BOOLEAN:
				readFileAttr();
				break;
			case SEQUENCE:
				readFileSeq();
				break;
			case NOMINAL:
				readFileAttrNominal();
				;
				break;
			default:
				break;
			}
			readFileTarget();

			// Global.displayAttributes();
			// Global.displayTargets();
			// Global.displayObjects();

			// Instanciate writters
			Global.repositoryName = "results/" + Global.resFolderName + "/resXP" + System.currentTimeMillis();
			File repository = new File(Global.repositoryName);
			repository.mkdirs();
			Global.bufferResult = new BufferedWriter(new FileWriter(Global.repositoryName + "/result.log"));
			Global.bufferInfo = new BufferedWriter(new FileWriter(Global.repositoryName + "/info.log"));
			Global.bufferSupport = new BufferedWriter(new FileWriter(Global.repositoryName + "/support.log"));
			Global.bufferSupportE11 = new BufferedWriter(new FileWriter(Global.repositoryName + "/supportE11.log"));

			// Instanciate the logList that stores the log values
			int size = Global.nbLoops;
			Global.logList = new double[size];

			if (Global.duplicatesExpand == Enum.DuplicatesExpand.AMAF)
				Global.amaf = new HashMap<Subgroup, Subgroup>();

			Global.root = new Subgroup();
			Subgroup[] startingSubgroups = Global.root.startWithTargets();

			// Iterates independently on each initial seeds
			int iFirst, iLast;
			if (Global.measure == Enum.Measure.FBeta) {
				iFirst = 0;
				iLast = startingSubgroups.length - 1;
			} else {
				iFirst = 0;
				iLast = 0;
			}
			for (int i = iFirst; i <= iLast; i++) {
				Subgroup aSub = startingSubgroups[i];
				System.out.println(aSub);
				System.out.println("\n[" + (i + 1) + "/" + startingSubgroups.length + "] Exploring :" + aSub + "...");
				runCompleteMCTS(aSub);
				aSub.delete();
				if (Global.measure == Enum.Measure.WKL)
					break;
			}

			long stopTime = System.currentTimeMillis();
			Global.runTime = stopTime - startTime;
			Global.writeInfo();

			Global.bufferResult.close();
			Global.bufferSupport.close();
			Global.bufferSupportE11.close();
			Global.bufferInfo.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Runs a basic MCTS on the root
	 * 
	 * @param root
	 */
	public static void runCompleteMCTS(Subgroup root) {
		// Runs the iterations
		Global.subRoot = root;
		long startTime = System.currentTimeMillis();
		boolean restart = false;
		for (int i = 1; i <= Global.nbLoops; i++) {
			if (i % 10000 == 0 && !restart) {
				long stopTime = System.currentTimeMillis();
				long elapsedTime = stopTime - startTime;
				int depth = root.getDepth();
				// Subgroup max = root.getMaxSubgroup();

				System.out.println(i + " iterations in " + elapsedTime + " ms.");
				System.out.println("Profondeur : " + depth);
				// System.out.println("Best sg : " + max);
				// System.out.println("max : " + root.getTotValue() + "\t" +
				// Global.subRoot.getMaxRollOut());
				// System.out.println("Evaluations in rollOut : " +
				// Global.nbEvaluationsRoll);
				Global.displayRunTime();

				startTime = stopTime;
			}
			if (!root.iterateOnce()) {
				i--;
				restart = true;
				if (root.fullTerminated) {
					long stopTime = System.currentTimeMillis();
					long elapsedTime = stopTime - startTime;

					System.out.println(
							"Search space is completly explored in " + i + " iterations in " + elapsedTime + " ms.");
					break;
				}
			} else {
				restart = false;
			}
		}

		// Checks all the redundancy of the result sets

		List<Subgroup> resultSet = new ArrayList<Subgroup>();
		while (true) {
			Subgroup aSub = Global.allSubgroups.poll();
			if (aSub == null)
				break;

			// Checks redundancy
			if (Global.maxRedundancy == -1)
				resultSet.add(aSub);
			else if (!aSub.isRedundantWith(resultSet))
				resultSet.add(aSub);

			// Breaks if the beam is full
			if (resultSet.size() >= Global.nbOutput) {
				Global.allSubgroups.clear();
				break;
			}
		}
		Global.allSubgroups.clear();

		try {
			Global.resultPatternCount = resultSet.size();
			for (Subgroup sg : resultSet) {
				Global.resultLength.add((int) sg.description.getDescriptionSize());
				Global.resultMeasures.add(sg.measure);
				
				Global.bufferResult.write(sg + "\n");
				Global.bufferSupport.write(sg.writeSupport() + "\n");
				Global.bufferSupportE11.write(sg.writeSupportE11() + "\n");
			}
			Global.bufferResult.flush();
			Global.bufferSupport.flush();
			Global.bufferSupportE11.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method that reads the attribute file given as parameter.
	 * 
	 * @param fileName
	 *            : The name of the attribute file.
	 */
	public static void readFileAttr() {
		String fileName = Global.propertiesFile;
		BufferedReader br = null;
		BufferedReader brBis = null;
		String line = "";
		String cvsSplitBy = "\t";

		try {
			boolean firstLine = true;

			// List of priority queues (one for each attribute) containing
			// values of the domain of the attribute
			List<PriorityQueue<Double>> valuesQueue = null;

			// Count the number of objects (lines)
			br = new BufferedReader(new FileReader(fileName));
			int nbLine = 0;
			while ((line = br.readLine()) != null) {
				nbLine++;
			}
			Global.objects = new Data.Object[nbLine - 1];
			br.close();

			br = new BufferedReader(new FileReader(fileName));
			// First loop on the file
			while ((line = br.readLine()) != null) {
				String[] items = line.split(cvsSplitBy);
				int length = items.length;

				// Initializes the array of attributes and the priority queues
				if (firstLine) {
					valuesQueue = new ArrayList<PriorityQueue<Double>>();
					Global.attributes = new Data.Attribute[length];
					Global.nbAttr = length;
					Global.nbChild = 2 * length;
					if (Global.propType == DataType.BOOLEAN)
						Global.nbChild = length;

					for (int i = 0; i < length; i++) {
						if (Global.propType == DataType.NUMERIC)
							Global.attributes[i] = new Data.AttributeNumerical(i, items[i]);
						else if (Global.propType == DataType.BOOLEAN)
							Global.attributes[i] = new Data.AttributeBoolean(i, items[i]);
						else
							System.err.println("Warning : Error in Main.readFileAttr.");
						valuesQueue.add(new PriorityQueue<Double>(Global.objects.length));
					}

					firstLine = false;
					continue;
				}

				// Pushes the values in the priority queues
				for (int i = 0; i < length; i++) {
					Double value = Double.parseDouble(items[i]);
					if (!valuesQueue.get(i).contains(value)) {
						valuesQueue.get(i).add(value);
					}
				}
			}

			// Builds the mapping between values of attributes and indexes
			List<HashMap<Double, Integer>> valueToIndexList = new ArrayList<HashMap<Double, Integer>>();
			for (int i = 0; i < valuesQueue.size(); i++) {
				HashMap<Double, Integer> valueToIndex = new HashMap<Double, Integer>();
				valueToIndexList.add(valueToIndex);
				Data.Attribute theAttr = Global.attributes[i];
				PriorityQueue<Double> pQueue = valuesQueue.get(i);
				double[] tab = new double[pQueue.size()];
				int cpt = 0;
				while (true) {
					Double value = pQueue.poll();
					if (value == null) {
						break;
					}
					tab[cpt] = value;
					valueToIndex.put(value, cpt);
					cpt++;
				}
				if (Global.propType == DataType.NUMERIC)
					((Data.AttributeNumerical) theAttr).setOrderedValues(tab);
			}

			// Second loop : initializes objects
			firstLine = true;
			brBis = new BufferedReader(new FileReader(fileName));
			int numLine = 0;
			while ((line = brBis.readLine()) != null) {
				String[] items = line.split(cvsSplitBy);
				int length = items.length;

				if (firstLine) {
					firstLine = false;
					continue;
				}

				Global.objects[numLine] = new Data.Object(numLine, Global.propType);
				for (int i = 0; i < length; i++) {
					Double value = Double.parseDouble(items[i]);

					if (Global.propType == DataType.NUMERIC) {
						int index = valueToIndexList.get(i).get(value);
						Global.objects[numLine].setAttributeValue(i, index);
					} else if (Global.propType == DataType.BOOLEAN && value == 1) {
						Global.objects[numLine].setAttribute(i);
					}
				}
				numLine++;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Method that reads the attribute file given as parameter.
	 * 
	 * @param fileName
	 *            : The name of the attribute file.
	 */
	public static void readFileAttrNominal() {
		String fileName = Global.propertiesFile;
		BufferedReader br = null;
		BufferedReader brBis = null;
		String line = "";
		String cvsSplitBy = "\t";

		try {
			boolean firstLine = true;

			// List of priority queues (one for each attribute) containing
			// values of the domain of the attribute
			List<HashSet<String>> valuesQueue = null;

			// Count the number of objects (lines)
			br = new BufferedReader(new FileReader(fileName));
			int nbLine = 0;
			while ((line = br.readLine()) != null) {
				nbLine++;
			}
			Global.objects = new Data.Object[nbLine - 1];
			br.close();

			br = new BufferedReader(new FileReader(fileName));
			// First loop on the file
			while ((line = br.readLine()) != null) {
				String[] items = line.split(cvsSplitBy);
				int length = items.length;

				// Initializes the array of attributes and the priority queues
				if (firstLine) {
					valuesQueue = new ArrayList<HashSet<String>>();
					Global.attributes = new Data.Attribute[length];
					Global.nbAttr = length;
					Global.nbChild = 0;
					for (int i = 0; i < length; i++) {
						Global.attributes[i] = new Data.AttributeNominal(i, items[i]);
						valuesQueue.add(new HashSet<String>());
					}

					firstLine = false;
					continue;
				}

				// Pushes the values in the priority queues
				for (int i = 0; i < length; i++) {
					String value = items[i];
					if (!valuesQueue.get(i).contains(value)) {
						valuesQueue.get(i).add(value);
					}
				}
			}

			// Builds the mapping between values of attributes and indexes
			List<HashMap<String, Integer>> valueToIndexList = new ArrayList<HashMap<String, Integer>>();
			for (int i = 0; i < valuesQueue.size(); i++) {
				HashMap<String, Integer> valueToIndex = new HashMap<String, Integer>();
				valueToIndexList.add(valueToIndex);
				Data.Attribute theAttr = Global.attributes[i];
				HashSet<String> pQueue = valuesQueue.get(i);
				String[] tab = new String[pQueue.size()];
				int cpt = 0;
				for (String value : pQueue) {
					tab[cpt] = value;
					valueToIndex.put(value, cpt);
					cpt++;
				}
				((Data.AttributeNominal) theAttr).setValues(tab);
				Global.nbChild += tab.length;
			}

			// Second loop : initializes objects
			firstLine = true;
			brBis = new BufferedReader(new FileReader(fileName));
			int numLine = 0;
			while ((line = brBis.readLine()) != null) {
				String[] items = line.split(cvsSplitBy);
				int length = items.length;

				if (firstLine) {
					firstLine = false;
					continue;
				}

				Global.objects[numLine] = new Data.Object(numLine, Global.propType);
				for (int i = 0; i < length; i++) {
					String value = items[i];
					int index = valueToIndexList.get(i).get(value);
					Global.objects[numLine].setAttributeValue(i, index);
				}
				numLine++;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Method that reads the attribute file of sequences given as parameter.
	 * 
	 * @param fileName
	 *            : The name of the attribute file.
	 */

	public static void readFileSeq() {
		String fileName = Global.propertiesFile;
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = " ";

		try { // Count the number of objects (lines)
			br = new BufferedReader(new FileReader(fileName));
			int nbLine = 0;
			int maxIdItems = 0;
			while ((line = br.readLine()) != null) {
				nbLine++;
				String[] items = line.split(cvsSplitBy);
				int length = items.length;
				for (int i = 0; i < length; i++) {
					int value = Integer.parseInt(items[i]);
					if (value > maxIdItems)
						maxIdItems = value;
				}
			}
			Global.objects = new Data.Object[nbLine];
			br.close();
			Global.nbAttr = maxIdItems + 1;
			Global.nbChild = 2 * Global.nbAttr;

			br = new BufferedReader(new FileReader(fileName));
			int numLine = 0;
			while ((line = br.readLine()) != null) {
				String[] items = line.split(cvsSplitBy);
				int length = items.length;

				Global.objects[numLine] = new Data.Object(numLine, DataType.SEQUENCE);
				OpenBitSet bs = new OpenBitSet(Global.nbAttr);
				for (int i = 0; i < length; i++) {
					int value = Integer.parseInt(items[i]);

					if (value == -1) {
						Global.objects[numLine].addItemset(bs);
						bs = new OpenBitSet(maxIdItems);
						continue;
					}

					if (value >= 0) {
						bs.set(value);
					}
				}
				numLine++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void readFileTarget() {
		String fileName = Global.qualitiesFile;
		BufferedReader br = null;
		BufferedReader brBis = null;
		String line = "";
		String cvsSplitBy = "\t";

		try {
			boolean firstLine = true;

			// List of priority queues (one for each target) containing
			// values of the domain of the target
			List<PriorityQueue<Double>> valuesQueue = null;

			br = new BufferedReader(new FileReader(fileName));

			// First loop on the file
			while ((line = br.readLine()) != null) {
				String[] items = line.split(cvsSplitBy);
				int length = items.length;

				// Initializes the array of attributes and the priority queues
				if (firstLine) {
					valuesQueue = new ArrayList<PriorityQueue<Double>>();
					Global.targets = new Data.Attribute[length];
					for (int i = 0; i < length; i++) {
						Global.targets[i] = new Data.AttributeBoolean(i, items[i]);
						valuesQueue.add(new PriorityQueue<Double>(Global.objects.length));
					}

					firstLine = false;
					continue;
				}

				// Pushes the values in the priority queues
				for (int i = 0; i < length; i++) {
					Double value = Double.parseDouble(items[i]);
					if (!valuesQueue.get(i).contains(value)) {
						valuesQueue.get(i).add(value);
					}
				}
			}

			// Second loop : initializes objects
			firstLine = true;
			brBis = new BufferedReader(new FileReader(fileName));
			int numLine = 0;
			while ((line = brBis.readLine()) != null) {
				String[] items = line.split(cvsSplitBy);
				int length = items.length;

				if (firstLine) {
					firstLine = false;
					continue;
				}

				for (int i = 0; i < length; i++) {
					Double value = Double.parseDouble(items[i]);
					if (value == 1)
						Global.objects[numLine].setTarget(i);
				}
				numLine++;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void getTargetsLength() {
		String fileName = Global.qualitiesFile;
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = "\t";

		try {
			boolean firstLine = true;

			br = new BufferedReader(new FileReader(fileName));

			// First loop on the file
			while ((line = br.readLine()) != null) {
				String[] items = line.split(cvsSplitBy);
				int length = items.length;

				// Initializes the array of attributes and the priority queues
				if (firstLine) {
					Global.targets = new Data.Attribute[length];
					break;
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Initialize parameters given by users
	 * 
	 * @param args
	 * @return
	 */
	protected static boolean readParameters(String[] args) {
		if (args.length != 9) {
			System.err.println("Bad parameters : \n" + Global.launchCommand);
			return false;
		}

		Global.qualitiesFile = args[0];
		Global.propertiesFile = args[1];
		Global.nbOutput = Integer.parseInt(args[2]);
		Global.nbLoops = Integer.parseInt(args[3]);
		Global.minSup = Integer.parseInt(args[4]);
		Global.xBeta = Double.parseDouble(args[5]);
		Global.lBeta = Double.parseDouble(args[6]);
		Global.maxRedundancy = Double.parseDouble(args[7]);
		Global.resFolderName = args[8];

		return true;
	}

	/**
	 * Initialize parameters given by users
	 * 
	 * @param args
	 * @return
	 */
	protected static boolean readParametersFromFile(String paramFile) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(paramFile));

			String line;

			while ((line = br.readLine()) != null) {
				if (line.startsWith("#") || !line.contains("="))
					continue;

				line = line.replace(" =", "=");
				line = line.replace("= ", "=");

				String[] temp = line.split("=");
				String paramName = temp[0];
				String paramValue = temp[1];

				paramName = paramName.trim();
				paramValue = paramValue.trim();

				// Dataset parameters
				if (paramName.compareTo("attrFile") == 0) {
					Global.propertiesFile = paramValue;
					continue;
				}

				if (paramName.compareTo("targetFile") == 0) {
					Global.qualitiesFile = paramValue;
					continue;
				}

				if (paramName.compareTo("attrType") == 0) {
					paramValue = paramValue.toLowerCase();

					if (paramValue.compareTo("numeric") == 0)
						Global.propType = DataType.NUMERIC;
					else if (paramValue.compareTo("boolean") == 0)
						Global.propType = DataType.BOOLEAN;
					else if (paramValue.compareTo("sequence") == 0)
						Global.propType = DataType.SEQUENCE;
					else if (paramValue.compareTo("graph") == 0)
						Global.propType = DataType.GRAPH;
					else if (paramValue.compareTo("nominal") == 0)
						Global.propType = DataType.NOMINAL;
					else {
						System.out.println("Bad attribute type. Please check it in the parameter file.");
						br.close();
						return false;
					}

					continue;
				}

				// Result folder parameter
				if (paramName.compareTo("resultFolderName") == 0) {
					Global.resFolderName = paramValue;
					continue;
				}

				// General parameters
				if (paramName.compareTo("minSupp") == 0) {
					Global.minSup = Integer.parseInt(paramValue);
					continue;
				}

				if (paramName.compareTo("nbIter") == 0) {
					Global.nbLoops = Integer.parseInt(paramValue);
					continue;
				}

				if (paramName.compareTo("maxOutput") == 0) {
					Global.nbOutput = Integer.parseInt(paramValue);
					continue;
				}

				if (paramName.compareTo("maxRedundancy") == 0) {
					Global.maxRedundancy = Double.parseDouble(paramValue);
					continue;
				}

				if (paramName.compareTo("maxLength") == 0) {
					int maxLength = Integer.parseInt(paramValue);
					if (maxLength > 0)
						Global.maxLength = maxLength;
					continue;
				}

				// Measures parameters
				if (paramName.compareTo("measure") == 0) {
					paramValue = paramValue.toLowerCase();
					if (paramValue.compareTo("wracc") == 0)
						Global.measure = Enum.Measure.WRAcc;
					else if (paramValue.compareTo("f1") == 0)
						Global.measure = Enum.Measure.F1;
					else if (paramValue.compareTo("wkl") == 0)
						Global.measure = Enum.Measure.WKL;
					else if (paramValue.compareTo("fbeta") == 0)
						Global.measure = Enum.Measure.FBeta;
					else {
						System.out.println("Bad measure value in the parameter file !");
						br.close();
						return false;
					}

					continue;
				}

				if (paramName.compareTo("xBeta") == 0) {
					Global.xBeta = Double.parseDouble(paramValue);
					continue;
				}

				if (paramName.compareTo("lBeta") == 0) {
					Global.lBeta = Double.parseDouble(paramValue);
					continue;
				}

				// MCTS policies
				// Select policy
				if (paramName.compareTo("UCB") == 0) {
					paramValue = paramValue.toLowerCase();
					if (paramValue.compareTo("ucb1") == 0)
						Global.UCB = Enum.UCB.UCB1;
					else if (paramValue.compareTo("uct") == 0)
						Global.UCB = Enum.UCB.UCT;
					else if (paramValue.compareTo("ucbsp") == 0)
						Global.UCB = Enum.UCB.UCBSP;
					else if (paramValue.compareTo("ucbtuned") == 0)
						Global.UCB = Enum.UCB.UCBTuned;
					else if (paramValue.compareTo("dfsuct") == 0)
						Global.UCB = Enum.UCB.DFSUCT;
					else {
						System.out.println("Bad UCB parameter in the parameter file");
						br.close();
						return false;
					}
					continue;
				}

				// Expand policy
				if (paramName.compareTo("refineExpand") == 0) {
					paramValue = paramValue.toLowerCase();

					if (paramValue.compareTo("direct") == 0)
						Global.refineExpand = Enum.RefineExpand.Direct;
					else if (paramValue.compareTo("generator") == 0)
						Global.refineExpand = Enum.RefineExpand.Generator;
					else if (paramValue.compareTo("tunedgenerator") == 0)
						Global.refineExpand = Enum.RefineExpand.TunedGenerator;
					else {
						System.out.println(
								"Bad refinement operator for expand value (refineExpand) in the parameter file !");
						br.close();
						return false;
					}
					continue;
				}

				if (paramName.compareTo("duplicatesExpand") == 0) {
					paramValue = paramValue.toLowerCase();

					if (paramValue.compareTo("none") == 0)
						Global.duplicatesExpand = Enum.DuplicatesExpand.None;
					else if (paramValue.compareTo("amaf") == 0)
						Global.duplicatesExpand = Enum.DuplicatesExpand.AMAF;
					else if (paramValue.compareTo("order") == 0)
						Global.duplicatesExpand = Enum.DuplicatesExpand.Order;
					else {
						System.out
								.println("Bad duplicates for expand value (duplicatesExpand) in the parameter file !");
						br.close();
						return false;
					}
					continue;
				}

				// RollOut policy
				if (paramName.compareTo("pathLength") == 0) {
					Global.pathLength = Integer.parseInt(paramValue);
					continue;
				}

				if (paramName.compareTo("refineRollOut") == 0) {
					paramValue = paramValue.toLowerCase();

					if (paramValue.compareTo("direct") == 0)
						Global.refineRollOut = Enum.RefineRollOut.Direct;
					else if (paramValue.compareTo("large") == 0)
						Global.refineRollOut = Enum.RefineRollOut.Large;
					else {
						System.out.println("Bad refine roll out value (refineRollOut) in the parameter file !");
						br.close();
						return false;
					}
					continue;
				}

				if (paramName.compareTo("jumpingLarge") == 0) {
					Global.jumpingLarge = Integer.parseInt(paramValue);
					continue;
				}

				if (paramName.compareTo("rewardPolicy") == 0) {
					paramValue = paramValue.toLowerCase();

					if (paramValue.compareTo("terminal") == 0)
						Global.rewardPolicy = Enum.RewardPolicy.Terminal;
					else if (paramValue.compareTo("randompick") == 0)
						Global.rewardPolicy = Enum.RewardPolicy.RandomPick;
					else if (paramValue.compareTo("meanpath") == 0)
						Global.rewardPolicy = Enum.RewardPolicy.MeanPath;
					else if (paramValue.compareTo("maxpath") == 0)
						Global.rewardPolicy = Enum.RewardPolicy.MaxPath;
					else if (paramValue.compareTo("meantopk") == 0)
						Global.rewardPolicy = Enum.RewardPolicy.MeanTopK;
					else {
						System.out.println("Bad reward policy value (rewardPolicy) in the parameter file !");
						br.close();
						return false;
					}
					continue;
				}

				if (paramName.compareTo("topKRollOut") == 0) {
					Global.topKRollOut = Integer.parseInt(paramValue);
					if (Global.topKRollOut < 0 && Global.rewardPolicy == Enum.RewardPolicy.MeanTopK) {
						System.out
								.println("Bad K value for topK roll out policy (rewardPolicy) in the parameter file !");
						br.close();
						return false;
					}
					continue;
				}

				// Memory policy
				if (paramName.compareTo("memoryPolicy") == 0) {
					paramValue = paramValue.toLowerCase();

					if (paramValue.compareTo("none") == 0)
						Global.memoryPolicy = Enum.MemoryPolicy.None;
					else if (paramValue.compareTo("allevaluated") == 0)
						Global.memoryPolicy = Enum.MemoryPolicy.AllEvaluated;
					else if (paramValue.compareTo("topk") == 0)
						Global.memoryPolicy = Enum.MemoryPolicy.TopK;
					else {
						System.out.println("Bad memory policy value (memoryPolicy) in the parameter file !");
						br.close();
						return false;
					}
					continue;
				}

				if (paramName.compareTo("topKMemory") == 0) {
					Global.topKMemory = Integer.parseInt(paramValue);
					if (Global.memoryPolicy == Enum.MemoryPolicy.TopK && Global.topKMemory < 0) {
						System.out.println("Bad K value for topK memory policy (topKMemory) in the parameter file !");
						br.close();
						return false;
					}
					continue;
				}

				// Update policy
				if (paramName.compareTo("updatePolicy") == 0) {
					paramValue = paramValue.toLowerCase();

					if (paramValue.compareTo("mean") == 0)
						Global.updatePolicy = Enum.UpdatePolicy.Mean;
					else if (paramValue.compareTo("max") == 0)
						Global.updatePolicy = Enum.UpdatePolicy.Max;
					else if (paramValue.compareTo("meantopk") == 0)
						Global.updatePolicy = Enum.UpdatePolicy.MeanTopK;
					else {
						System.out.println("Bad update policy value (updatePolicy) in the parameter file !");
						br.close();
						return false;
					}
					continue;
				}

				if (paramName.compareTo("topKUpdate") == 0) {
					Global.topKUpdate = Integer.parseInt(paramValue);
					if (Global.topKUpdate < 0 && Global.updatePolicy == Enum.UpdatePolicy.MeanTopK) {
						System.out.println("Bad K value for topK update policy (topKUpdate) in the parameter file !");
						br.close();
						return false;
					}
					continue;
				}
			}

			br.close();

		} catch (

		Exception e) {
			e.printStackTrace();
		}
		return true;
	}

}
