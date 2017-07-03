package liris.cnrs.fr.dm2l.mcts4dm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.lucene.util.OpenBitSet;
import org.vikamine.kernel.data.Attribute;
import org.vikamine.kernel.data.DataRecord;
import org.vikamine.kernel.data.Ontology;
import org.vikamine.kernel.data.converters.CSVDataConverter;
import org.vikamine.kernel.data.converters.CSVDataConverterConfig;
import org.vikamine.kernel.data.creators.DataFactory;
import org.vikamine.kernel.subgroup.SG;
import org.vikamine.kernel.subgroup.SGSet;
import org.vikamine.kernel.subgroup.quality.functions.WRAccQF;
import org.vikamine.kernel.subgroup.search.MiningTask;
import org.vikamine.kernel.subgroup.search.SDBeamSearch;
import org.vikamine.kernel.subgroup.search.SDMap;
import org.vikamine.kernel.subgroup.selectors.DefaultSGSelector;
import org.vikamine.kernel.subgroup.selectors.SGSelector;
import org.vikamine.kernel.subgroup.selectors.SGSelectorGeneratorFactory;
import org.vikamine.kernel.subgroup.selectors.SelectorGeneratorUtils;
import org.vikamine.kernel.subgroup.target.SelectorTarget;

import Data.Subgroup;
import Process.Global;
import dp2.Avaliador;
import dp2.Const;
import dp2.D;
import dp2.Pattern;
import edu.uab.emmSampling.EMMSampler;
import evolucionario.SSDP_MxC_Auto_3x3;
import nl.liacs.subdisc.AttributeType;
import nl.liacs.subdisc.FileLoaderARFF;
import nl.liacs.subdisc.Process;
import nl.liacs.subdisc.QM;
import nl.liacs.subdisc.SearchParameters;
import nl.liacs.subdisc.SubgroupDiscovery;
import nl.liacs.subdisc.Table;
import nl.liacs.subdisc.TargetConcept;

/**
 * Class which allows you to read a MCTS4DM data file and run
 * <ul>
 * <li>SDMAP of Vikamine (PKDD 2006)</li>
 * <li>BeamSearch of Vikamine (DAMI 2012)</li>
 * <li>Genetic algorithm of SSDP (Applied Soft Computing 2017)</li>
 * <li>EMM Pattern sampler (IDA 2014)</li>
 * 
 * @author Mehdi Kaytoue
 *
 */
public class Main {
	public final static int BEAM_SEARCH = 0;
	public final static int SD_MAP = 1;
	public static List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> result;
	public static int[] NB_ITERATIONS = { 1000, 5000, 10000, 50000,
			100000/* , 500000, 1000000 */ };
	public static double[] MAX_SIMILARITY = { 0.2, 0.4, 0.6, 0.8 };
	public static double[] MIN_SUPPORT = { /* 0.5, 0.1, 0.05, 0.01, 0.005 */ 0.05 };
	public static int[] TOP_K = { 1, 5, 10, 50, 100, 500, 1000 };
	public static double[] MIN_QUAL = { 0.1, 0.2, 0.3, 0.4, 0.5 };
	public static int[] BEAM_WIDTH = { 50, 100, 500 };
	public static String[] artificialData;
	public static String[] benchmarkData = { "bibtex", "breastCancer", "cal500", "emotions", "ionosphere", "iris",
			"mushroom", "nursery", "olfaction", "scene", "tictactoe",
			"yeast"/* , "bookmarks", "mediamill" */ };
	public static Data.DataType[] dataType = { Data.DataType.BOOLEAN, Data.DataType.NUMERIC, Data.DataType.NUMERIC,
			Data.DataType.NUMERIC, Data.DataType.NUMERIC, Data.DataType.NUMERIC, Data.DataType.NOMINAL,
			Data.DataType.NOMINAL, Data.DataType.NUMERIC, Data.DataType.NUMERIC, Data.DataType.NOMINAL,
			Data.DataType.NUMERIC/*
									 * , Data.DataType.BOOLEAN,
									 * Data.DataType.NUMERIC
									 */ };

	public static boolean runSDMap = false;
	public static boolean runMCTS = true;
	public static boolean runMisere = true;
	public static boolean runSSDP = false;
	public static boolean runBeamSearch = false;

	public static int nbRunsNonDeterministic = 1;

	public static int timeout = 300;

	public static Data.Enum.Measure theMeasure = Data.Enum.Measure.Acc;

	public static void main(String[] args) throws Exception {
		/**
		 * Data are set in ./data folder A subfolder "base" is the name of a
		 * dataset, e.g. "iris"
		 * 
		 * A subfolder MUST contain the MCTS4DM two files (properties and
		 * qualities)
		 * 
		 * createDataset() will build in base vikamine and ssdp file format
		 * 
		 * It returns String[0]= attributeNames
		 * 
		 * and String[1]=targetNames Vikamine dataset consider only the first
		 * label as {true, false} target SSDP work on the last attribute by
		 * default and assume it to takes values in {true, false} EMMSampling
		 * does not need the target file, but to choose two numerical attributes
		 * as target
		 * 
		 */

		/*
		 * Generates the artificial data
		 */
		artificialData = new String[5];
		artificialData[0] = generateArtificialDataset(5000, 10, 200);
		artificialData[1] = generateArtificialDataset(20000, 10, 200);
		artificialData[2] = generateArtificialDataset(5000, 50, 50);
		artificialData[3] = generateArtificialDataset(5000, 50, 200);
		artificialData[4] = generateArtificialDataset(20000, 50, 200);

		// Runs the experiments on the artificial data
		runArtificialXP();

		// Runs the experiments on the benchmark data
		runBenchmarkXP();

		// Generates the data files to plot the figures
		for (String base : benchmarkData)
			generateBenchmarkDataFile(base, "result");
		for (String base : artificialData)
			generateArtificialDataFile(base, "result");

	}

	/**
	 * Runs the experiments for the benchmark data
	 * @throws IOException
	 */
	public static void runBenchmarkXP() throws IOException {
		for (int idBase = 0; idBase < benchmarkData.length; idBase++) {
			String base = benchmarkData[idBase];
			createDataset2(base);
			System.out.println("\n*****\nBase = " + base);
			if (!base.equals("mushroom"))
				continue;

			List<List<List<liris.cnrs.fr.dm2l.mcts4dm.Pattern>>> resultSSDP = new ArrayList<List<List<liris.cnrs.fr.dm2l.mcts4dm.Pattern>>>();
			List<List<Integer>> runtimeSSDP = new ArrayList<List<Integer>>();
			Map<Integer, Boolean> alreadyRunSSDP = new HashMap<Integer, Boolean>();

			Set<String> timeoutMap = new HashSet<String>();
			// Map<String, Map<Integer, Long>> memory = new HashMap<String,
			// Map<Integer, Long>>();
			// List<String> algoList = new ArrayList<String>();
			// List<Integer> minSuppList = new ArrayList<Integer>();

			for (double minsuppD : MIN_SUPPORT) {
				int minsupp = (int) (minsuppD * getNbInstances(base));
				// minSuppList.add(minsupp);
				System.out.println("minSupp = " + minsupp);

				/* Create writers */
				File repository = new File("result/" + base);
				repository.mkdirs();
				BufferedWriter[] writers = new BufferedWriter[MAX_SIMILARITY.length];
				for (int i = 0; i < MAX_SIMILARITY.length; i++) {
					writers[i] = new BufferedWriter(new FileWriter(
							"result/" + base + "/minSupp_" + minsupp + "-maxSim_" + MAX_SIMILARITY[i] + ".csv"));
					writers[i].write("Algorithm");
					for (int j = 0; j < TOP_K.length; j++) {
						writers[i].write("\tTop_" + TOP_K[j]);
					}
					for (int j = 0; j < MIN_QUAL.length; j++) {
						writers[i].write("\tMinQual_" + MIN_QUAL[j]);
					}
					writers[i].write("\tRuntime\tRedundancy\n");
				}

				/* Create tasks */
				List<Task> tasks = new ArrayList<Task>();
				if (runSDMap)
					tasks.add(new TaskVikamine(SD_MAP, base, minsupp));
				for (int i = 0; i < BEAM_WIDTH.length; i++)
					if (runBeamSearch)
						tasks.add(new TaskCortana(base, minsupp, BEAM_WIDTH[i], dataType[idBase]));
				for (int i = 0; i < NB_ITERATIONS.length; i++)
					if (runMCTS)
						tasks.add(new TaskMCTS4DM(base, theMeasure, NB_ITERATIONS[i], minsupp, dataType[idBase], 1));
				for (int i = 0; i < NB_ITERATIONS.length; i++)
					if (runMisere)
						tasks.add(new TaskMisere(base, theMeasure, NB_ITERATIONS[i], minsupp, dataType[idBase], 1));
				for (int i = 0; i < NB_ITERATIONS.length; i++)
					if (runSSDP) {
						if (alreadyRunSSDP.size() <= i) {
							alreadyRunSSDP.put(i, false);
							resultSSDP.add(new ArrayList<List<liris.cnrs.fr.dm2l.mcts4dm.Pattern>>());
							runtimeSSDP.add(new ArrayList<Integer>());
						}
						tasks.add(new TaskSSDP(base, minsupp, NB_ITERATIONS[i] / 10));
					}

				int idSSDP = -1;
				/* Launch tasks */
				for (Task task : tasks) {
					// if (!memory.containsKey(task.name)) {
					// memory.put(task.name, new HashMap<Integer, Long>());
					// algoList.add(task.name);
					// }

					int nbRuns = 1;
					if (task instanceof TaskMCTS4DM || task instanceof TaskMisere || task instanceof TaskSSDP)
						nbRuns = nbRunsNonDeterministic;

					if (task instanceof TaskSSDP)
						idSSDP++;

					double[][] meanArea = new double[MAX_SIMILARITY.length][TOP_K.length];
					for (int sim = 0; sim < MAX_SIMILARITY.length; sim++)
						for (int topK = 0; topK < TOP_K.length; topK++)
							meanArea[sim][topK] = 0;

					int[][] meanNb = new int[MAX_SIMILARITY.length][MIN_QUAL.length];

					for (int sim = 0; sim < MAX_SIMILARITY.length; sim++)
						for (int minQual = 0; minQual < MIN_QUAL.length; minQual++)
							meanNb[sim][minQual] = 0;

					double[] meanRedundancy = new double[MAX_SIMILARITY.length];
					for (int sim = 0; sim < MAX_SIMILARITY.length; sim++)
						meanRedundancy[sim] = 0;

					int meanRuntime = 0;

					if (!timeoutMap.contains(task.name)) {
						for (int idRun = 1; idRun <= nbRuns; idRun++) {
							// Let's run !
							if (task instanceof TaskSSDP) {
								if (!(alreadyRunSSDP.get(idSSDP))) {
									// AtomicBoolean stopMemMonitor = new
									// AtomicBoolean(false);
									// System.gc();
									// MemoryMonitor monitor =
									// MemoryMonitor.startDefaultMemoryMonitor(500,
									// stopMemMonitor);
									resultSSDP.get(idSSDP).add(launchTask(task, TimeUnit.SECONDS, timeout));
									// memory.get(task.name).put(minsupp,
									// monitor.getMaxMemoryUsed());
									// System.out.println("Mem usage in parent:
									// " + monitor.getMaxMemoryUsed());
									// stopMemMonitor.set(true);
									runtimeSSDP.get(idSSDP).add((int) task.runtime);
								}
								task.runtime = runtimeSSDP.get(idSSDP).get(idRun - 1);
								result = resultSSDP.get(idSSDP).get(idRun - 1);
							} else {
								// AtomicBoolean stopMemMonitor = new
								// AtomicBoolean(false);
								// System.gc();
								// MemoryMonitor monitor =
								// MemoryMonitor.startDefaultMemoryMonitor(500,
								// stopMemMonitor);
								result = launchTask(task, TimeUnit.SECONDS, timeout);
								// memory.get(task.name).put(minsupp,
								// monitor.getMaxMemoryUsed());
								// System.out.println("Mem usage in parent: " +
								// monitor.getMaxMemoryUsed());
								// stopMemMonitor.set(true);
							}

							if (result != null) {
								if (task instanceof TaskSSDP) {
									result = filterFrequent(minsupp, result);
								}

								meanRuntime = (int) (meanRuntime * (idRun - 1) + task.runtime) / idRun;

								int sizeBeforeSim = result.size();
								/* Post processing similarity */
								for (int sim = 0; sim < MAX_SIMILARITY.length; sim++) {
									List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> diverse = postProcessRedundancy(result,
											MAX_SIMILARITY[sim]);
									int sizeAfterSim = diverse.size();
									double redundancyRate = 1. - (double) (sizeAfterSim) / ((double) sizeBeforeSim);
									meanRedundancy[sim] = (meanRedundancy[sim] * (idRun - 1) + redundancyRate) / idRun;

									double[] area = getAreaTopK(diverse);
									for (int topK = 0; topK < TOP_K.length; topK++)
										meanArea[sim][topK] = (meanArea[sim][topK] * (idRun - 1) + area[topK]) / idRun;

									int[] nb = getAreaMinQual(diverse);
									for (int minQual = 0; minQual < MIN_QUAL.length; minQual++)
										meanNb[sim][minQual] = (int) (meanNb[sim][minQual] * (idRun - 1) + nb[minQual])
												/ idRun;
								}
								// Similarity
							} else {
								meanRuntime = -1;
								break;
							}
						}
					} else {
						// Timeout for previous minsupp
						System.out.println("Launch " + task.name);
						System.out.println("Timeout in previous xp !");
						meanRuntime = -1;
					}
					if (task instanceof TaskSSDP) {
						alreadyRunSSDP.put(idSSDP, true);
					}

					// Write
					for (int sim = 0; sim < MAX_SIMILARITY.length; sim++) {
						writers[sim].write(task.name);
						if (meanRuntime != -1) {
							for (int topK = 0; topK < TOP_K.length; topK++) {
								writers[sim].write("\t" + meanArea[sim][topK]);
							}
							for (int minQual = 0; minQual < MIN_QUAL.length; minQual++) {
								writers[sim].write("\t" + meanNb[sim][minQual]);
							}
							writers[sim].write("\t" + meanRuntime + "\t" + meanRedundancy[sim] + "\n");
						} else {
							timeoutMap.addAll(task.getTimeout());
							for (int j = 0; j < TOP_K.length; j++) {
								writers[sim].write("\t-");
							}
							for (int j = 0; j < MIN_QUAL.length; j++) {
								writers[sim].write("\t-");
							}
							writers[sim].write("\ttimeout\t-\n");
						}
						writers[sim].flush();
					}
				}
				for (int sim = 0; sim < MAX_SIMILARITY.length; sim++) {
					writers[sim].flush();
					writers[sim].close();
				}
			}
			// BufferedWriter bwMemory = new BufferedWriter(new
			// FileWriter("result/" + base + "/memory.csv"));
			// bwMemory.write("minSupp");
			// for (String algo : algoList) {
			// bwMemory.write("\t" + algo);
			// }
			// bwMemory.write("\n");
			// for (int minSupp : minSuppList) {
			// bwMemory.write(String.valueOf(minSupp));
			// for (String algo : algoList) {
			// bwMemory.write("\t" + memory.get(algo).get(minSupp));
			// }
			// bwMemory.write("\n");
			// }
			// bwMemory.flush();
			// bwMemory.close();
		}

	}

	/**
	 * Runs the experiment for the artificial data
	 * @throws Exception
	 */
	public static void runArtificialXP() throws Exception {
		for (int idBase = 0; idBase < artificialData.length; idBase++) {
			String base = artificialData[idBase];
			System.out.println("\n*****\nBase = " + base);
			createDataset2(base);

			List<List<List<liris.cnrs.fr.dm2l.mcts4dm.Pattern>>> resultSSDP = new ArrayList<List<List<liris.cnrs.fr.dm2l.mcts4dm.Pattern>>>();
			List<List<Integer>> runtimeSSDP = new ArrayList<List<Integer>>();
			Map<Integer, Boolean> alreadyRunSSDP = new HashMap<Integer, Boolean>();

			Set<String> timeoutMap = new HashSet<String>();

			for (double minsuppD : MIN_SUPPORT) {
				int minsupp = (int) (minsuppD * getNbInstances(base));
				System.out.println("minSupp = " + minsupp);

				/* Create writer */
				File repository = new File("result/" + base);
				repository.mkdirs();
				BufferedWriter bw = new BufferedWriter(
						new FileWriter("result/" + base + "/minSupp_" + minsupp + ".csv"));
				bw.write("Algorithm\tJaccard\tRuntime\n");

				/* Create tasks */
				List<Task> tasks = new ArrayList<Task>();
				if (runSDMap)
					tasks.add(new TaskVikamine(SD_MAP, base, minsupp));
				for (int i = 0; i < BEAM_WIDTH.length; i++)
					if (runBeamSearch)
						tasks.add(new TaskCortana(base, minsupp, BEAM_WIDTH[i], Data.DataType.NOMINAL));

				for (int i = 0; i < NB_ITERATIONS.length; i++)
					if (runMCTS)
						tasks.add(new TaskMCTS4DM(base, Data.Enum.Measure.WRAcc, NB_ITERATIONS[i], minsupp,
								Data.DataType.NOMINAL, 1));
				for (int i = 0; i < NB_ITERATIONS.length; i++)
					if (runMisere)
						tasks.add(new TaskMisere(base, Data.Enum.Measure.WRAcc, NB_ITERATIONS[i], minsupp,
								Data.DataType.NOMINAL, 1));
				for (int i = 0; i < NB_ITERATIONS.length; i++)
					if (runSSDP) {
						if (alreadyRunSSDP.size() <= i) {
							alreadyRunSSDP.put(i, false);
							resultSSDP.add(new ArrayList<List<liris.cnrs.fr.dm2l.mcts4dm.Pattern>>());
							runtimeSSDP.add(new ArrayList<Integer>());
						}
						tasks.add(new TaskSSDP(base, minsupp, NB_ITERATIONS[i] / 10));
					}

				/* Launch task */
				int idSSDP = -1;
				for (Task task : tasks) {
					int nbRuns = 1;
					if (task instanceof TaskMCTS4DM || task instanceof TaskMisere || task instanceof TaskSSDP)
						nbRuns = nbRunsNonDeterministic;

					if (task instanceof TaskSSDP)
						idSSDP++;

					double meanJaccard = 0;
					int meanRuntime = 0;
					if (!timeoutMap.contains(task.name)) {
						for (int idRun = 1; idRun <= nbRuns; idRun++) {
							// Let's run !
							if (task instanceof TaskSSDP) {
								if (!(alreadyRunSSDP.get(idSSDP))) {
									resultSSDP.get(idSSDP).add(launchTask(task, TimeUnit.SECONDS, timeout));
									runtimeSSDP.get(idSSDP).add((int) task.runtime);
								}
								task.runtime = runtimeSSDP.get(idSSDP).get(idRun - 1);
								result = resultSSDP.get(idSSDP).get(idRun - 1);
							} else
								result = launchTask(task, TimeUnit.SECONDS, timeout);

							// Handle result
							if (result != null) {
								if (task instanceof TaskSSDP) {
									result = filterFrequent(minsupp, result);
								}
								meanRuntime = (int) (meanRuntime * (idRun - 1) + task.runtime) / idRun;
								meanJaccard = (meanJaccard * (idRun - 1) + DataGen.compare(result, base)) / idRun;
							} else {
								meanRuntime = -1;
								break;
							}
						}
					} else {
						// Timeout for previous minsupp
						System.out.println("Launch " + task.name);
						System.out.println("Timeout in previous xp !");
						meanRuntime = -1;
					}
					if (task instanceof TaskSSDP) {
						alreadyRunSSDP.put(idSSDP, true);
					}
					// Write result
					bw.write(task.name + "\t");
					if (meanRuntime != -1)
						bw.write(meanJaccard + "\t" + meanRuntime + "\n");
					else {
						timeoutMap.addAll(task.getTimeout());
						bw.write("-\ttimeout\n");
					}
					bw.flush();
				}
				bw.flush();
				bw.close();
			}
		}

	}


	/**
	 * Generates the data file to plot the figures for artificial data
	 * @param base: the name of the database
	 * @param folderName : the filder containing the result
	 */
	public static void generateArtificialDataFile(String base, String folderName) {
		try {
			File repository = new File(folderName + "/" + base + "/data");
			repository.mkdirs();
			File fig = new File(folderName + "/" + base + "/data/fig");
			fig.mkdirs();
			fig = new File(folderName + "/" + base + "/data/fig/SDMap");
			fig.mkdirs();
			fig = new File(folderName + "/" + base + "/data/fig/BeamSearch");
			fig.mkdirs();
			fig = new File(folderName + "/" + base + "/data/fig/Misere");
			fig.mkdirs();
			fig = new File(folderName + "/" + base + "/data/fig/SSDP");
			fig.mkdirs();
			File folder = new File(folderName + "/" + base);
			BufferedWriter bw = new BufferedWriter(new FileWriter(folderName + "/" + base + "/data/runtime.dat"));

			Map<String, Map<Integer, String>> data = new HashMap<String, Map<Integer, String>>();
			List<Integer> minSuppList = new ArrayList<Integer>();
			List<String> algoList = new ArrayList<String>();

			boolean first = true;

			File[] listOfFiles = folder.listFiles();
			System.out.println(base);
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile() && listOfFiles[i].getName().startsWith("minS")) {
					System.out.println("File " + listOfFiles[i].getName());
					String filename = listOfFiles[i].getName().replace(".csv", "");
					String minsupp = filename.split("_")[1];
					minSuppList.add(Integer.parseInt(minsupp));

					BufferedReader br = new BufferedReader(new FileReader(listOfFiles[i]));
					String line = "";
					boolean firstLine = true;
					while ((line = br.readLine()) != null) {
						if (firstLine) {
							firstLine = false;
							continue;
						}
						String[] t = line.split("\t");
						if (first) {
							data.put(t[0], new HashMap<Integer, String>());
							algoList.add(t[0]);
						}
						data.get(t[0]).put(Integer.parseInt(minsupp), t[2]);
					}
					br.close();
					first = false;
				}
			}
			Collections.sort(minSuppList, Collections.reverseOrder());
			bw.write("minSupp");
			for (String algo : algoList) {
				bw.write("\t" + algo);
			}
			bw.write("\n");
			for (int minsupp : minSuppList) {
				bw.write(String.valueOf(minsupp));
				for (String algo : algoList) {
					bw.write("\t" + data.get(algo).get(minsupp));
				}
				bw.write("\n");
			}

			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Generates the data file to plot the figures for benchmark data
	 * @param base: the name of the database
	 * @param folderName : the filder containing the result
	 */
	public static void generateBenchmarkDataFile(String base, String folderName) {
		try {
			File folder = new File(folderName + "/" + base);
			if (!folder.exists()) {
				System.out.println(base + " is missing...");
				return;
			}
			System.out.println(base);
			File[] listOfFiles = folder.listFiles();
			Map<Double, List<File>> files = new HashMap<Double, List<File>>();

			List<Integer> minSuppList = new ArrayList<Integer>();
			List<String> algoList = new ArrayList<String>();

			List<Double> maxSimList = new ArrayList<Double>();
			Map<String, Map<Integer, Map<Double, String>>> redundancy = new HashMap<String, Map<Integer, Map<Double, String>>>();

			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile() && listOfFiles[i].getName().startsWith("minS")) {
					String filename = listOfFiles[i].getName().replace(".csv", "");
					Double minSim = Double.parseDouble(filename.split("-")[1].split("_")[1]);
					if (!files.containsKey(minSim)) {
						files.put(minSim, new ArrayList<File>());
					}
					files.get(minSim).add(listOfFiles[i]);
				}
			}

			for (Entry<Double, List<File>> entry : files.entrySet()) {
				Double maxSim = entry.getKey();
				maxSimList.add(maxSim);
				List<File> listFiles = entry.getValue();
				File repository = new File(folderName + "/" + base + "/data");
				repository.mkdirs();
				File fig = new File(folderName + "/" + base + "/data/fig");
				fig.mkdirs();
				fig = new File(folderName + "/" + base + "/data/fig/SDMap");
				fig.mkdirs();
				fig = new File(folderName + "/" + base + "/data/fig/BeamSearch");
				fig.mkdirs();
				fig = new File(folderName + "/" + base + "/data/fig/Misere");
				fig.mkdirs();
				fig = new File(folderName + "/" + base + "/data/fig/SSDP");
				fig.mkdirs();
				BufferedWriter bwRuntime = new BufferedWriter(
						new FileWriter(folderName + "/" + base + "/data/runtime.dat"));

				Map<String, Map<Integer, String>> data = new HashMap<String, Map<Integer, String>>();
				minSuppList.clear();
				algoList.clear();

				boolean first = true;

				for (File file : listFiles) {
					String filename = file.getName().replace(".csv", "");
					String minsupp = filename.split("-")[0].split("_")[1];
					minSuppList.add(Integer.parseInt(minsupp));

					BufferedWriter bwTopK = new BufferedWriter(new FileWriter(
							folderName + "/" + base + "/data/topK-minSupp_" + minsupp + "-maxSim_" + maxSim + ".dat"));
					BufferedWriter bwQual = new BufferedWriter(new FileWriter(folderName + "/" + base
							+ "/data/minQual-minSupp_" + minsupp + "-maxSim_" + maxSim + ".dat"));

					List<List<String>> topK = new ArrayList<List<String>>();
					List<List<String>> qual = new ArrayList<List<String>>();
					List<String> header = new ArrayList<String>();

					BufferedReader br = new BufferedReader(new FileReader(file));
					String line = "";
					boolean firstLine = true;
					while ((line = br.readLine()) != null) {
						line = line.replace("\t\t", "\t");
						String[] t = line.split("\t");
						if (firstLine) {
							header.add(t[0]);
							for (int i = 1; i <= 7; i++) {
								topK.add(new ArrayList<String>());
								topK.get(i - 1).add(t[i].replace("Top_", ""));
							}
							for (int i = 8; i <= 12; i++) {
								qual.add(new ArrayList<String>());
								qual.get(i - 8).add(t[i].replace("MinQual_", ""));
							}
							firstLine = false;
							continue;
						}
						if (first) {
							data.put(t[0], new HashMap<Integer, String>());
							if (!redundancy.containsKey(t[0]))
								redundancy.put(t[0], new HashMap<Integer, Map<Double, String>>());
							algoList.add(t[0]);
						}
						header.add(t[0]);
						for (int i = 1; i <= 7; i++) {
							topK.get(i - 1).add(t[i]);
						}
						for (int i = 8; i <= 12; i++) {
							qual.get(i - 8).add(t[i]);
						}
						data.get(t[0]).put(Integer.parseInt(minsupp), t[13]);
						if (!redundancy.get(t[0]).containsKey(Integer.parseInt(minsupp)))
							redundancy.get(t[0]).put(Integer.parseInt(minsupp), new HashMap<Double, String>());
						redundancy.get(t[0]).get(Integer.parseInt(minsupp)).put(maxSim, t[14]);

					}
					br.close();
					first = false;

					String headers = "";
					for (String s : header) {
						if (!headers.isEmpty())
							headers += "\t";
						headers += s;
					}
					bwTopK.write(headers + "\n");
					bwQual.write(headers + "\n");
					for (List<String> l : topK) {
						String myLine = "";
						for (String s : l) {
							if (!myLine.isEmpty())
								myLine += "\t";
							myLine += s;
						}
						bwTopK.write(myLine + "\n");
					}
					for (List<String> l : qual) {
						String myLine = "";
						for (String s : l) {
							if (!myLine.isEmpty())
								myLine += "\t";
							myLine += s;
						}
						bwQual.write(myLine + "\n");
					}
					bwTopK.flush();
					bwTopK.close();
					bwQual.flush();
					bwQual.close();

				}
				Collections.sort(minSuppList, Collections.reverseOrder());
				bwRuntime.write("minSupp");
				for (String algo : algoList) {
					bwRuntime.write("\t" + algo);
				}
				bwRuntime.write("\n");
				for (int minsupp : minSuppList) {
					bwRuntime.write(String.valueOf(minsupp));
					for (String algo : algoList) {
						bwRuntime.write("\t" + data.get(algo).get(minsupp));
					}
					bwRuntime.write("\n");
				}

				bwRuntime.flush();
				bwRuntime.close();
			}

			Collections.sort(maxSimList, Collections.reverseOrder());

			for (int minsupp : minSuppList) {
				BufferedWriter bwRedundancy = new BufferedWriter(
						new FileWriter(folderName + "/" + base + "/data/redundancy-minSupp_" + minsupp + ".dat"));
				bwRedundancy.write("maxSim");
				for (String algo : algoList) {
					bwRedundancy.write("\t" + algo);
				}
				bwRedundancy.write("\n");

				for (double maxSim : maxSimList) {
					bwRedundancy.write(String.valueOf(maxSim));
					for (String algo : algoList) {
						bwRedundancy.write("\t" + redundancy.get(algo).get(minsupp).get(maxSim));
					}
					bwRedundancy.write("\n");
				}

				bwRedundancy.flush();
				bwRedundancy.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Filters out the unfrequent patterns from a pattern set given a minimum support threshold
	 * @param minsupp: the minimum support threshold
	 * @param list: the pattern set
	 * @return the updated pattern set
	 */
	public static List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> filterFrequent(int minsupp,
			List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> list) {
		List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> res = new ArrayList<liris.cnrs.fr.dm2l.mcts4dm.Pattern>();
		for (liris.cnrs.fr.dm2l.mcts4dm.Pattern p : list) {
			if (p.extent.cardinality() >= minsupp)
				res.add(p);
		}
		return res;
	}

	/**
	 * Computes the sum of the quality measures of the top-k best patterns of a pattern set
	 * @param result: the result set
	 * @return the array containing the sum for different values of k
	 */
	public static double[] getAreaTopK(List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> result) {
		double[] area = new double[TOP_K.length];

		for (int i = 0; i < TOP_K.length; i++) {
			area[i] = 0;
			for (int t = 0; t < TOP_K[i] && t < result.size(); t++) {
				area[i] += result.get(t).quality;
			}
		}

		return area;
	}

	/**
	 * Computes the sum of the quality measures of the patterns for which the
	 * quality measure is greater than a given threshold
	 * 
	 * @param result : the list of the patterns
	 * @return the updated of patterns
	 */
	public static int[] getAreaMinQual(List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> result) {
		int[] nb = new int[MIN_QUAL.length];

		for (int i = 0; i < MIN_QUAL.length; i++) {
			nb[i] = 0;
			double minQual = MIN_QUAL[i];
			for (int t = 0; t < result.size() && result.get(t).quality >= minQual; t++) {
				nb[i]++;
			}
		}

		return nb;
	}

	/**
	 * Runs the task
	 * 
	 * @param task
	 *            : the task to run
	 * @param time
	 *            : the time unit for the timeout
	 * @param timeOut
	 *            : the value of the timeout
	 * @return
	 */
	public static List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> launchTask(
			Callable<List<liris.cnrs.fr.dm2l.mcts4dm.Pattern>> task, TimeUnit time, int timeOut) {
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		final Future<List<liris.cnrs.fr.dm2l.mcts4dm.Pattern>> future = executor.submit(task);
		executor.shutdown(); // This does not cancel the already-scheduled task.
		List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> result = null;
		try {
			result = future.get(timeOut, time);
			return filterNegativeMeasures(result);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException te) {
			// Handle time out
			System.out.println("TimeoutException");
			if (task instanceof TaskMCTS4DM) {
				Global.root = null;
				Global.initialize();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		if (!executor.isTerminated()) {
			executor.shutdownNow(); // If you want to stop the code that hasn't
		}
		return null;
	}

	/**
	 * Filters out the patterns with negative quality measures
	 * 
	 * @param list
	 * @return
	 */
	public static List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> filterNegativeMeasures(
			List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> list) {
		if (list == null)
			return null;
		List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> res = new ArrayList<liris.cnrs.fr.dm2l.mcts4dm.Pattern>();

		for (liris.cnrs.fr.dm2l.mcts4dm.Pattern p : list) {
			if (p.quality >= 0) {
				res.add(p);
			}
		}
		return res;
	}

	/**
	 * Merges the results from two different folders
	 * 
	 * @param folderDest
	 * @param folderOther
	 */
	public static void mergeResult(String folderDest, String folderOther) {
		try {
			File folder = new File(folderDest);
			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isDirectory()) {
					String base = listOfFiles[i].getName();
					System.out.println(base);
					File[] listOfData = listOfFiles[i].listFiles();
					for (int j = 0; j < listOfData.length; j++) {
						if (listOfData[j].isFile() && listOfData[j].getName().endsWith(".csv")
								&& !listOfData[j].getName().startsWith("memory")) {
							String fileName = listOfData[j].getName();
							System.out.println(folderDest + "/" + base + "/" + fileName);
							List<String> listDest = Files.readAllLines(
									new File(folderDest + "/" + base + "/" + fileName).toPath(),
									Charset.defaultCharset());
							List<String> listOther = Files.readAllLines(
									new File(folderOther + "/" + base + "/" + fileName).toPath(),
									Charset.defaultCharset());
							File output = new File("merged/" + base);
							if (!output.exists())
								output.mkdirs();
							BufferedWriter bw = new BufferedWriter(new FileWriter("merged/" + base + "/" + fileName));
							for (int id = 0; id < listDest.size(); id++) {
								if (id >= 2 && id <= 4) {
									bw.write(listOther.get(id - 1) + "\n");
								} else
									bw.write(listDest.get(id) + "\n");
							}
							bw.flush();
							bw.close();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update the parameter conf file to the specified data
	 * 
	 * @param base:
	 *            the name of the data
	 * @param filename:
	 *            the configuration file
	 * @param nbLabels:
	 *            the number of labels to take into account (1 for traditional
	 *            measures, 2 for contingency tables)
	 */
	public static void updateData(String base, String filename, int nbLabels) {
		List<String> conf = null;
		try {
			conf = Files.readAllLines(Paths.get(filename));
		} catch (IOException e) {
			System.out.println("Can't read configuratin files " + filename);
			e.printStackTrace();
			System.exit(-1);
		}
		String content = "";
		for (String line : conf) {
			if (line.startsWith("attrFile")) {
				content += "attrFile = data/" + base + "/properties.csv\n";
				continue;
			}

			if (line.startsWith("targetFile")) {
				content += "targetFile = data/" + base + "/qualities_" + nbLabels + ".csv\n";
				continue;
			}

			content += line + "\n";
		}

		try {
			Files.write(Paths.get(filename), content.getBytes());
		} catch (IOException e) {
			System.out.println("Can't write configuration file " + filename);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Specify the minimum support threshold to use
	 * 
	 * @param minSupp:
	 *            the threshold is an integer
	 * @param filename:
	 *            the name of the configuration file
	 */
	public static void updateMinSupp(int minSupp, String filename) {
		List<String> conf = null;
		try {
			conf = Files.readAllLines(Paths.get(filename));
		} catch (IOException e) {
			System.out.println("Can't read configuratin files " + filename);
			e.printStackTrace();
			System.exit(-1);
		}
		String content = "";
		for (String line : conf) {
			if (line.startsWith("minSupp")) {
				content += "minSupp = " + minSupp + "\n";
				continue;
			}
			content += line + "\n";
		}

		try {
			Files.write(Paths.get(filename), content.getBytes());
		} catch (IOException e) {
			System.out.println("Can't write configuration file " + filename);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Specify to number of iterations (or samples) to perform
	 * 
	 * @param nbIter:
	 *            the number of iteration is an integer
	 * @param filename:
	 *            the configuration file
	 */
	public static void updateNbIterations(int nbIter, String filename) {
		List<String> conf = null;
		try {
			conf = Files.readAllLines(Paths.get(filename));
		} catch (IOException e) {
			System.out.println("Can't read configuratin files " + filename);
			e.printStackTrace();
			System.exit(-1);
		}
		String content = "";
		for (String line : conf) {
			if (line.startsWith("nbIter")) {
				content += "nbIter = " + nbIter + "\n";
				continue;
			}
			content += line + "\n";
		}

		try {
			Files.write(Paths.get(filename), content.getBytes());
		} catch (IOException e) {
			System.out.println("Can't write configuration file " + filename);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Change the measure of the method in the parameter.conf file
	 * 
	 * @param measure:
	 *            It is in [ WRAcc | F1 | RelativeF1 | WeightedRelativeF1 | WKL
	 *            | FBeta | RelativeFBeta | WeightedRelativeFBeta | RAcc | Acc |
	 *            HammingLoss | ZeroOneLoss | ContingencyTable]
	 * @param filename:
	 *            the name of the configuration file
	 */
	public static void updateMeasure(Data.Enum.Measure measure, String filename) {
		List<String> conf = null;
		try {
			conf = Files.readAllLines(Paths.get(filename));
		} catch (IOException e) {
			System.out.println("Can't read configuratin files " + filename);
			e.printStackTrace();
			System.exit(-1);
		}
		String content = "";
		for (String line : conf) {
			if (line.startsWith("measure")) {
				content += "measure = " + measure + "\n";
				continue;
			}
			content += line + "\n";
		}

		try {
			Files.write(Paths.get(filename), content.getBytes());
		} catch (IOException e) {
			System.out.println("Can't write configuration file " + filename);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Change the data type of the method in the parameter.conf file
	 * 
	 * @param dataType:
	 *            It is in [ Numeric | Boolean | Nominal | Sequence | Graph ]
	 * @param filename:
	 *            the name of the configuration file
	 */
	public static void updateDataType(Data.DataType dataType, String filename) {
		List<String> conf = null;
		try {
			conf = Files.readAllLines(Paths.get(filename));
		} catch (IOException e) {
			System.out.println("Can't read configuratin files " + filename);
			e.printStackTrace();
			System.exit(-1);
		}
		String content = "";
		for (String line : conf) {
			if (line.startsWith("attrType")) {
				content += "attrType = " + dataType + "\n";
				continue;
			}
			content += line + "\n";
		}

		try {
			Files.write(Paths.get(filename), content.getBytes());
		} catch (IOException e) {
			System.out.println("Can't write configuration file " + filename);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static liris.cnrs.fr.dm2l.mcts4dm.Pattern convertSubgroupToPattern(Subgroup s) {
		return (new liris.cnrs.fr.dm2l.mcts4dm.Pattern(s.measure, s.description.support));
	}

	/**
	 * Generates an artificial data set, with properties set as DataGen.* fields
	 * 
	 * @return Filename of the directory (base) containing the dataset in
	 *         MCTS4DM format
	 */
	public static String generateArtificialDataset(int nbTrans, int nbAtt, int attDomain) {
		DataGen.noise_rate = 0.05;
		DataGen.out_factor = 0.05;
		DataGen.nb_patterns = 5;
		DataGen.pattern_size = 200;
		DataGen.nb_trans = nbTrans;
		DataGen.nb_att = nbAtt;
		DataGen.att_domain_size = attDomain;
		DataGen.addFrequentPatterns = false;

		String df;
		try {
			while ((df = DataGen.generate()).equals("-1"))
				;
			return df;
		} catch (IOException e) {
			System.err.println("Error while generating artificial data");
			e.printStackTrace();
		}
		return null;
	}

	public static List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> postProcessRedundancy(
			List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> patternSet, double maxRedundancy) {
		Collections.sort(patternSet);
		int size = 0;
		List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> res = new ArrayList<liris.cnrs.fr.dm2l.mcts4dm.Pattern>();
		for (int i = 0; i < patternSet.size(); i++) {
			boolean isRedundant = false;
			for (liris.cnrs.fr.dm2l.mcts4dm.Pattern p : res) {
				if (jaccard(patternSet.get(i).extent, p.extent) > maxRedundancy) {
					isRedundant = true;
					break;
				}
			}
			if (!isRedundant) {
				res.add(patternSet.get(i));
				size++;
				if (size == 3000) {
					System.out.println("Break post-process : Limited to 3000 patterns");
					break;
				}
			}

		}
		return res;
	}

	public static double jaccard(OpenBitSet a, OpenBitSet b) {
		return (double) OpenBitSet.intersectionCount(a, b) / (double) OpenBitSet.unionCount(a, b);
	}

	public static int getNbInstances(String base) {
		List<String> classes = null;
		try {
			classes = Files.readAllLines(Paths.get("data/" + base + "/qualities.csv"));
		} catch (IOException e) {
			System.out.println("Can't read qualities data");
			e.printStackTrace();
			System.exit(-1);
		}

		return (classes.size() - 1);
	}

	public static String[][] createDataset2(String base) {
		BufferedReader brQual;
		BufferedReader brProp;

		String lineQual = "";
		String lineProp = "";
		String cvsSplitBy = "\t";
		String[] attributesNames = null;
		String[] labelNames = null;
		try {
			brQual = new BufferedReader(new FileReader("data/" + base + "/qualities.csv"));
			brProp = new BufferedReader(new FileReader("data/" + base + "/properties.csv"));

			BufferedWriter bwVikamine = new BufferedWriter(new FileWriter("data/" + base + "/wikamine.csv"));
			BufferedWriter bwDssp = new BufferedWriter(new FileWriter("data/" + base + "/dssp.csv"));
			BufferedWriter bwSamplerData = new BufferedWriter(new FileWriter("data/" + base + "/emm-data.txt"));
			BufferedWriter bwSamplerAttributes = new BufferedWriter(
					new FileWriter("data/" + base + "/emm-attributes.txt"));
			BufferedWriter bwMcts1 = new BufferedWriter(new FileWriter("data/" + base + "/qualities_1.csv"));
			BufferedWriter bwMcts2 = new BufferedWriter(new FileWriter("data/" + base + "/qualities_2.csv"));

			boolean first = true;
			while ((lineQual = brQual.readLine()) != null) {
				lineProp = brProp.readLine();
				if (first) {
					attributesNames = lineProp.split(cvsSplitBy);
					labelNames = lineQual.split(cvsSplitBy);
					bwVikamine.write(lineProp + "\tClass\n");
					bwDssp.write(lineProp + "\tclass\n");
					bwMcts1.write(lineQual.split("\t")[0] + "\n");
					bwMcts2.write(lineQual.split("\t")[0] + "\t" + lineQual.split("\t")[1] + "\n");

					for (String s : lineProp.split("\t"))
						bwSamplerAttributes.write(s + ";numeric;some descriptions" + "\n");

					first = false;
					continue;
				}
				bwVikamine.write("\"s" + lineProp.replaceAll("\t", "\"\t\"s") + "\"" + "\t"
						+ (lineQual.split("\t")[0].equals("1") ? "true" : "false") + "\n");
				bwDssp.write(lineProp + "\t" + (lineQual.split("\t")[0].equals("1") ? "true" : "false") + "\n");
				bwSamplerData.write(lineProp.replaceAll("\t", ";") + "\n");
				bwMcts1.write(lineQual.split("\t")[0] + "\n");
				bwMcts2.write(lineQual.split("\t")[0] + "\t" + lineQual.split("\t")[1] + "\n");
			}
			brQual.close();
			brProp.close();
			bwVikamine.flush();
			bwVikamine.close();
			bwDssp.flush();
			bwDssp.close();
			bwSamplerData.flush();
			bwSamplerData.close();
			bwSamplerAttributes.flush();
			bwSamplerAttributes.close();
			bwMcts1.flush();
			bwMcts1.close();
			bwMcts2.flush();
			bwMcts2.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String[][] { attributesNames, labelNames };
	}

	// example: labelPair = "sepal_length,sepal_width"
	public static List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> emm(String base, String labelPair, int resultCount) {
		String[] args = { "contingencytable", labelPair, "1", "1", "yes", "./data/" + base + "/emm-attributes.txt",
				"./data/" + base + "/emm-data.txt", "" + resultCount };
		return EMMSampler.emm(args);
	}

	public static List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> vikamine(String base, String label, int algo, int minSupp)
			throws IOException {

		Ontology onto = DataFactory.createOntologyFromCSVFile(new File("./data/" + base + "/wikamine.csv"),
				new CSVDataConverterConfig('\t', '\"', CSVDataConverter.getStandardNumberFormat(), '%'));

		String classAttributeName = label;
		String classTrueValue = "true";
		MiningTask task = new MiningTask(onto);
		Iterator<Attribute> iter = onto.getAttributes().iterator();
		Attribute classAttribute = null;
		while (iter.hasNext()) {
			Attribute a = iter.next();
			if (a.getDescription().equals(classAttributeName)) {
				classAttribute = a;
				break;
			}
		}
		task.setTarget(new SelectorTarget(new DefaultSGSelector(onto, classAttributeName, classTrueValue)));
		@SuppressWarnings("unchecked")
		LinkedHashSet<Attribute> att = (LinkedHashSet<Attribute>) onto.getAttributes().clone();
		att.remove(classAttribute);

		List<SGSelector> selectors = SelectorGeneratorUtils
				.generateSelectors(SGSelectorGeneratorFactory.createStandardGenerator(), att, onto.getDataView());
		task.setSearchSpace(selectors);
		task.setQualityFunction(new WRAccQF());

		if (algo == SD_MAP)
			task.setMethodType(SDMap.class);
		if (algo == BEAM_SEARCH)
			task.setMethodType(SDBeamSearch.class);

		SG initialSG = new SG(onto.getDataView(), task.getTarget());
		initialSG.createStatistics(null);
		task.setInitialSG(initialSG);
		task.setMaxSGCount(Integer.MAX_VALUE);
		task.setMaxSGDSize(Integer.MAX_VALUE);
		task.setSuppressStrictlyIrrelevantSubgroups(false);
		// task.setMinTPSupportAbsolute(minSupp);
		task.setMinQualityLimit(Double.NEGATIVE_INFINITY);
		task.setMinSubgroupSize(minSupp);
		SGSet result = task.performSubgroupDiscovery();

		List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> resultPatternSet = new ArrayList<liris.cnrs.fr.dm2l.mcts4dm.Pattern>();
		Iterator<SG> sgIter = result.iterator();
		while (sgIter.hasNext()) {
			SG s = sgIter.next();
			Iterator<DataRecord> iterRecords = s.subgroupInstanceIterator();
			OpenBitSet extent = new OpenBitSet();
			while (iterRecords.hasNext())
				extent.set((int) iterRecords.next().getID());
			resultPatternSet.add(new liris.cnrs.fr.dm2l.mcts4dm.Pattern(s.getQuality(), extent));
		}
		return resultPatternSet;
	}

	public static List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> ssdp(String base, int resultCount, boolean debug)
			throws FileNotFoundException {
		String caminhoBase = "./data/" + base + "/dssp.csv"; // caminho +
		// nomeBase;
		D.SEPARADOR = "\t"; // separator database
		Const.random = new Random(Const.SEEDS[0]); // Seed - 30 options
		// Parameters of the algorithm
		int k = resultCount; // 10; // number of DPs
		String tipoAvaliacao = Avaliador.TIPO_WRACC; // Fitness
		// tipoAvaliacao = Avaliador.TIPO_QG; //Fitness
		// tipoAvaliacao = Avaliador.TIPO_SUB; //Fitness
		D.valorAlvo = "true"; // target value of dataset
		D.CarregarArquivo(caminhoBase, D.TIPO_CSV); // Loading database
		Pattern.numeroIndividuosGerados = 0; // Initializing count of generated
		// individuals by SSDP
		// Rodando SSDP
		long t0 = System.currentTimeMillis(); // Initial time
		Pattern[] p = SSDP_MxC_Auto_3x3.run(k, tipoAvaliacao); // run SSDP
		double tempo = (System.currentTimeMillis() - t0) / 1000.0; // time

		// Informations about top-k DPs:
		if (debug) {
			System.out.println("### Base:" + D.nomeBase); // database name
			System.out.println("Average " + tipoAvaliacao + ": " + Avaliador.avaliarMedia(p, k));
			System.out.println("Time(s): " + tempo);
			System.out.println("Average size: " + Avaliador.avaliarMediaDimensoes(p, k));
			System.out.println(
					"Coverage of all k DPs in relation to D+: " + Avaliador.coberturaPositivo(p, k) * 100 + "%");
			System.out.println("Number of individuals generated: " + Pattern.numeroIndividuosGerados);
			System.out.println("\n### Top-k DPs:");
		}
		return Avaliador.imprimirRegras(p, k);
	}

	public static List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> cortanaBeamSearch(String base, AttributeType type,
			int minSupp, int beamwidth) {
		FileLoaderARFF loader = new FileLoaderARFF(new File("./data/" + base + "/cortana.arff"));
		Table table = loader.getTable();
		table.update();
		SearchParameters searchParameters = getSearchParameters(table, minSupp, beamwidth);
		int i = 0;
		for (i = 0; i < table.getColumns().size(); i++) {
			table.getColumn(i).setTargetStatus(" none");
			table.getColumn(i).setType(type);
			table.getColumn(i).setIsEnabled(true);
		}
		table.getColumn(i - 1).setTargetStatus(" primary");
		table.getColumn(i - 1).setType(AttributeType.NOMINAL);

		SubgroupDiscovery sgd = Process.runSubgroupDiscovery(table, 0, null, searchParameters, false, 1, null);
		List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> resultPatternSet = new ArrayList<liris.cnrs.fr.dm2l.mcts4dm.Pattern>();
		for (nl.liacs.subdisc.Subgroup s : sgd.getResult()) {
			resultPatternSet.add(new liris.cnrs.fr.dm2l.mcts4dm.Pattern(s.getMeasureValue(),
					new OpenBitSet(s.itsMembers.toLongArray(), s.itsMembers.toLongArray().length)));
		}
		return resultPatternSet;
	}

	public static SearchParameters getSearchParameters(Table itsTable, int minSupp, int beamwidth) {
		TargetConcept itsTargetConcept = new TargetConcept(); // todo :/
		SearchParameters itsSearchParameters = new SearchParameters();
		itsSearchParameters.setTargetConcept(itsTargetConcept);
		/*
		 * TARGET CONCEPT some cleaning is done to create proper AutoRun-XMLs
		 */
		// TargetType aType = TargetType.getDefault(); //
		// itsTargetConcept.getTargetType();

		// if (TargetType.hasTargetAttribute(aType))
		itsTargetConcept.setPrimaryTarget(itsTable.getColumn("Class"));// getTargetAttributeName()));
		// else
		// itsTargetConcept.setPrimaryTarget(null);

		// if (aType == TargetType.SINGLE_NOMINAL)
		itsTargetConcept.setTargetValue("cortana");// getMiscFieldName());
		// else
		// itsTargetConcept.setTargetValue(null);

		// if (TargetType.hasSecondaryTarget(aType))
		// itsTargetConcept.setSecondaryTarget(itsTable.getColumn(getMiscFieldName()));
		// else
		itsTargetConcept.setSecondaryTarget(null);

		// are already set when needed, remove possible old values
		// if (!TargetType.hasMultiTargets(aType))
		// itsTargetConcept.setMultiTargets(new ArrayList<Column>(0));
		// assumes COOKS_DISTANCE is only valid for DOUBLE_REGRESSION
		// if (QM.COOKS_DISTANCE.GUI_TEXT.equals(getQualityMeasureName()))
		// itsTargetConcept.setMultiRegressionTargets(new ArrayList<Column>(0));

		/*
		 * SEARCH PARAMETERS
		 */
		itsSearchParameters.setQualityMeasure(QM.fromString("wracc"));// getQualityMeasureName()));
		itsSearchParameters.setQualityMeasureMinimum(new Float(0.0));// getQualityMeasureMinimum());
		itsSearchParameters.setSearchDepth(10); // getSearchDepthMaximum());
		itsSearchParameters.setMinimumCoverage(minSupp); // getSearchCoverageMinimum());
		itsSearchParameters.setMaximumCoverageFraction(new Float(1));// getSearchCoverageMaximum());
		itsSearchParameters.setMaximumSubgroups(0); // 0 c'est infini
		itsSearchParameters.setMaximumTime(3 * timeout);// getSearchTimeMaximum());
		itsSearchParameters.setSearchStrategy("beam");// getSearchStrategyName());
		// set to last known value even for SearchStrategy.BEST_FIRST
		itsSearchParameters.setSearchStrategyWidth(beamwidth); // getStrategyWidth());
		itsSearchParameters.setNominalSets(false); // getSetValuedNominals());
		itsSearchParameters.setNumericStrategy("all");// getNumericStrategy());
		itsSearchParameters.setNumericOperators("<html>&#8804;, &#8805;</html>"); // getNumericOperators());
		// set to last known value even for NumericStrategy.NUMERIC_BINS
		itsSearchParameters.setNrBins(8); // getNrBins());
		itsSearchParameters.setNrThreads(1);
		return itsSearchParameters;
	}
}
