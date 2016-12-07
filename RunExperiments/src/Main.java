import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
	// To launch several runs of the same instance
	static int nbRuns = 5;
	static int timeOut = 180000; // 3min
	static String path = "../../Experiments/Datasets/"; // Path to the folder
														// containing the
														// datasets

	// Datasets specificities
	static String[] datasets = { "BreastCancer", "Cal500", "Emotions", "Ionosphere", "Iris", "Mushroom", "Nursery",
			"TicTacToe", "Yeast", "Olfaction" };
	static Enum.Measure[] measures = { Enum.Measure.WRAcc, Enum.Measure.WRAcc, Enum.Measure.WRAcc, Enum.Measure.WRAcc,
			Enum.Measure.WRAcc, Enum.Measure.WRAcc, Enum.Measure.WRAcc, Enum.Measure.WRAcc, Enum.Measure.WRAcc,
			Enum.Measure.F1 };
	static Enum.AttrType[] attrType = { Enum.AttrType.Numeric, Enum.AttrType.Numeric, Enum.AttrType.Numeric,
			Enum.AttrType.Numeric, Enum.AttrType.Numeric, Enum.AttrType.Nominal, Enum.AttrType.Nominal,
			Enum.AttrType.Nominal, Enum.AttrType.Numeric, Enum.AttrType.Numeric };
	static int[] minSupp = { 10, 10, 10, 10, 10, 30, 50, 10, 20, 5 };
	static int[] nbIters = { 50000, 100000, 100000, 50000, 50000, 50000, 100000, 100000, 100000, 100000 };
	static Enum.RefineRollOut[] refRoll = { Enum.RefineRollOut.Large, Enum.RefineRollOut.Large,
			Enum.RefineRollOut.Large, Enum.RefineRollOut.Large, Enum.RefineRollOut.Large, Enum.RefineRollOut.Direct,
			Enum.RefineRollOut.Direct, Enum.RefineRollOut.Direct, Enum.RefineRollOut.Large, Enum.RefineRollOut.Large };

	public static void main(String[] args) {
		for (String param : args) {
			param = param.toLowerCase();
			if (param.compareTo("ucb") == 0)
				launchUCB();
			else if (param.compareTo("expand") == 0)
				launchExpand();
			else if (param.compareTo("rollout") == 0)
				launchRollOut();
			else if (param.compareTo("memory") == 0)
				launchMemory();
			else if (param.compareTo("update") == 0)
				launchUpdate();
			else if (param.compareTo("iter") == 0)
				launchIter();
		}
	}

	public static void runExperiment(ParameterSetting param) {
		// Generate the parameters file
		String parameterFileName = generateParametersFile(param);
		System.out.println("\nRun on param : \n" + param);
		// Run the algorithm several times
		for (int i = 0; i < nbRuns; i++) {
			try {
				System.out.println("\t-Run " + (i + 1) + "/" + nbRuns);
				executeCommandLine("java -jar ../MCTS4SD.jar " + parameterFileName, timeOut);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String generateParametersFile(ParameterSetting param) {
		String parameterFileName = "paramGen.conf";
		File file = new File(parameterFileName);
		try {
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();

			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("attrFile = " + param.attrFile + "\n");
			bw.write("targetFile = " + param.targetFile + "\n");
			bw.write("attrType = " + param.attrType + "\n");
			bw.write("resultFolderName = " + "../../results/" + param.resultFolderName + "\n");
			bw.write("minSupp = " + param.minSupp + "\n");
			bw.write("nbIter = " + param.nbIter + "\n");
			bw.write("maxOutput = " + param.maxOutput + "\n");
			bw.write("maxRedundancy = " + param.maxRedundancy + "\n");
			bw.write("maxLength = " + param.maxLength + "\n");
			bw.write("measure = " + param.measure + "\n");
			bw.write("xBeta = " + param.xBeta + "\n");
			bw.write("lBeta = " + param.lBeta + "\n");
			bw.write("UCB = " + param.UCB + "\n");
			bw.write("refineExpand = " + param.refineExpand + "\n");
			bw.write("duplicatesExpand = " + param.duplicatesExpand + "\n");
			bw.write("pathLength = " + param.pathLength + "\n");
			bw.write("refineRollOut = " + param.refineRollOut + "\n");
			bw.write("jumpingLarge = " + param.jumpingLarge + "\n");
			bw.write("rewardPolicy = " + param.rewardPolicy + "\n");
			bw.write("topKRollOut = " + param.topKRollOut + "\n");
			bw.write("memoryPolicy = " + param.memoryPolicy + "\n");
			bw.write("topKMemory = " + param.topKMemory + "\n");
			bw.write("updatePolicy = " + param.updatePolicy + "\n");
			bw.write("topKUpdate = " + param.topKUpdate + "\n");
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return parameterFileName;
	}

	public static void executeCommandLine(final String commandLine, final int timeout) throws IOException {
		Process process = Runtime.getRuntime().exec(commandLine);
		ProcessWithTimeOut processWithTimeout = new ProcessWithTimeOut(process);
		int exitCode = processWithTimeout.waitForProcess(timeout);

		if (exitCode == Integer.MIN_VALUE) {
			// Timeout
			System.out.println("Time out");
			process.destroy();
		} else {
			System.out.println("No time out");
		}
	}

	public static void launchUpdate() {
		ParameterSetting param = new ParameterSetting();
		String nameXP = "Update";

		System.out.println("\n\n#####################");
		System.out.println(nameXP);
		System.out.println("#####################");

		for (int idDataset = 0; idDataset < datasets.length; idDataset++) {
			param.attrFile = path + datasets[idDataset] + "/properties.csv";
			param.targetFile = path + datasets[idDataset] + "/qualities.csv";
			param.attrType = attrType[idDataset];
			param.measure = measures[idDataset];
			param.minSupp = minSupp[idDataset];
			param.refineRollOut = refRoll[idDataset];
			param.nbIter = nbIters[idDataset];

			for (Enum.UpdatePolicy update : Enum.UpdatePolicy.values()) {
				param.updatePolicy = update;
				if (update == Enum.UpdatePolicy.MeanTopK) {
					int[] topKValue = { 2, 5, 10 };
					for (int topK : topKValue) {
						param.resultFolderName = nameXP + "/" + datasets[idDataset] + "/" + update + "_" + topK;
						param.topKUpdate = topK;
						runExperiment(param);
					}
				} else {
					param.resultFolderName = nameXP + "/" + datasets[idDataset] + "/" + update;
					runExperiment(param);
				}
			}
		}
	}

	public static void launchMemory() {
		ParameterSetting param = new ParameterSetting();
		String nameXP = "Memory";

		System.out.println("\n\n#####################");
		System.out.println(nameXP);
		System.out.println("#####################");

		for (int idDataset = 0; idDataset < datasets.length; idDataset++) {
			param.attrFile = path + datasets[idDataset] + "/properties.csv";
			param.targetFile = path + datasets[idDataset] + "/qualities.csv";
			param.attrType = attrType[idDataset];
			param.measure = measures[idDataset];
			param.minSupp = minSupp[idDataset];
			param.refineRollOut = refRoll[idDataset];
			param.nbIter = nbIters[idDataset];

			for (Enum.MemoryPolicy memory : Enum.MemoryPolicy.values()) {
				param.memoryPolicy = memory;
				if (memory == Enum.MemoryPolicy.TopK) {
					int[] topKValue = { 1, 2, 5, 10 };
					for (int topK : topKValue) {
						param.resultFolderName = nameXP + "/" + datasets[idDataset] + "/" + memory + "_" + topK;
						param.topKUpdate = topK;
						runExperiment(param);
					}
				} else {
					param.resultFolderName = nameXP + "/" + datasets[idDataset] + "/" + memory;
					runExperiment(param);
				}
			}
		}
	}

	public static void launchUCB() {
		ParameterSetting param = new ParameterSetting();
		String nameXP = "UCB";

		System.out.println("\n\n#####################");
		System.out.println(nameXP);
		System.out.println("#####################");

		for (int idDataset = 0; idDataset < datasets.length; idDataset++) {
			param.attrFile = path + datasets[idDataset] + "/properties.csv";
			param.targetFile = path + datasets[idDataset] + "/qualities.csv";
			param.attrType = attrType[idDataset];
			param.measure = measures[idDataset];
			param.minSupp = minSupp[idDataset];
			param.refineRollOut = refRoll[idDataset];
			param.nbIter = nbIters[idDataset];

			for (Enum.UCB ucb : Enum.UCB.values()) {
				param.UCB = ucb;

				if (ucb == Enum.UCB.DFSUCT) {
					param.duplicatesExpand = Enum.DuplicatesExpand.Order;
					param.resultFolderName = nameXP + "/" + datasets[idDataset] + "/" + ucb + "_"
							+ Enum.DuplicatesExpand.Order;
					runExperiment(param);
				} else {
					for (Enum.DuplicatesExpand dupli : Enum.DuplicatesExpand.values()) {
						param.duplicatesExpand = dupli;
						param.resultFolderName = nameXP + "/" + datasets[idDataset] + "/" + ucb + "_" + dupli;
						runExperiment(param);
					}
				}
			}
		}
	}

	public static void launchRollOut() {
		ParameterSetting param = new ParameterSetting();
		String nameXP = "RollOut";

		System.out.println("\n\n#####################");
		System.out.println(nameXP);
		System.out.println("#####################");

		for (int idDataset = 0; idDataset < datasets.length; idDataset++) {
			param.attrFile = path + datasets[idDataset] + "/properties.csv";
			param.targetFile = path + datasets[idDataset] + "/qualities.csv";
			param.attrType = attrType[idDataset];
			param.measure = measures[idDataset];
			param.minSupp = minSupp[idDataset];
			param.refineRollOut = refRoll[idDataset];
			param.nbIter = nbIters[idDataset];

			// Path Length = 20
			param.pathLength = 20;
			param.refineRollOut = Enum.RefineRollOut.Direct;
			param.rewardPolicy = Enum.RewardPolicy.Terminal;
			param.resultFolderName = nameXP + "/" + datasets[idDataset] + "/RandomPath_Direct_Terminal";
			runExperiment(param);

			// Path Length = -1
			param.pathLength = -1;
			for (Enum.RefineRollOut rollOut : Enum.RefineRollOut.values()) {
				param.refineRollOut = rollOut;

				for (Enum.RewardPolicy rew : Enum.RewardPolicy.values()) {
					if (rew == Enum.RewardPolicy.Terminal)
						continue;
					param.rewardPolicy = rew;

					if (rew == Enum.RewardPolicy.MeanTopK) {
						int[] topKValue = { 2, 5, 10 };
						for (int topK : topKValue) {
							param.topKRollOut = topK;

							if (rollOut == Enum.RefineRollOut.Large) {
								int[] jumpingLarge = { 10, 20, 50, 100 };
								for (int jump : jumpingLarge) {
									param.resultFolderName = nameXP + "/" + datasets[idDataset] + "/FrequentPath_"
											+ rollOut + "-" + jump + "_" + rew + "-" + topK;
									runExperiment(param);
								}
							} else {
								param.resultFolderName = nameXP + "/" + datasets[idDataset] + "/FrequentPath_" + rollOut
										+ "_" + rew + "-" + topK;
								runExperiment(param);
							}
						}
					} else {
						if (rollOut == Enum.RefineRollOut.Large) {
							int[] jumpingLarge = { 10, 20, 50, 100 };
							for (int jump : jumpingLarge) {
								param.resultFolderName = nameXP + "/" + datasets[idDataset] + "/FrequentPath_" + rollOut
										+ "-" + jump + "_" + rew;
								runExperiment(param);
							}
						} else {
							param.resultFolderName = nameXP + "/" + datasets[idDataset] + "/FrequentPath_" + rollOut
									+ "_" + rew;
							runExperiment(param);
						}
					}
				}
			}
		}
	}

	public static void launchExpand() {
		ParameterSetting param = new ParameterSetting();
		String nameXP = "Expand";

		System.out.println("\n\n#####################");
		System.out.println(nameXP);
		System.out.println("#####################");

		for (int idDataset = 0; idDataset < datasets.length; idDataset++) {
			param.attrFile = path + datasets[idDataset] + "/properties.csv";
			param.targetFile = path + datasets[idDataset] + "/qualities.csv";
			param.attrType = attrType[idDataset];
			param.measure = measures[idDataset];
			param.minSupp = minSupp[idDataset];
			param.refineRollOut = refRoll[idDataset];
			param.nbIter = nbIters[idDataset];

			for (Enum.RefineExpand refineExpand : Enum.RefineExpand.values()) {
				param.refineExpand = refineExpand;

				for (Enum.DuplicatesExpand dupli : Enum.DuplicatesExpand.values()) {
					param.duplicatesExpand = dupli;
					param.resultFolderName = nameXP + "/" + datasets[idDataset] + "/" + refineExpand + "_" + dupli;
					runExperiment(param);
				}
			}
		}
	}

	public static void launchIter() {
		ParameterSetting param = new ParameterSetting();
		String nameXP = "NbIterations";

		System.out.println("\n\n#####################");
		System.out.println(nameXP);
		System.out.println("#####################");

		for (int idDataset = 0; idDataset < datasets.length; idDataset++) {
			param.attrFile = path + datasets[idDataset] + "/properties.csv";
			param.targetFile = path + datasets[idDataset] + "/qualities.csv";
			param.attrType = attrType[idDataset];
			param.measure = measures[idDataset];
			param.minSupp = minSupp[idDataset];
			param.refineRollOut = refRoll[idDataset];
			param.nbIter = nbIters[idDataset];

			int[] nbIterations = { 10, 50, 100, 500, 1000, 5000, 10000, 50000, 100000 };
			for (int valueIter : nbIterations) {
				param.nbIter = valueIter;
				param.resultFolderName = nameXP + "/" + datasets[idDataset] + "/iter_"
						+ String.format("%07d", valueIter);
				runExperiment(param);

			}
		}
	}
}
