import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class Main {

	public static void main(String[] args) {
		try {
			File folder = new File("../RunExperiments/results");
			File[] listOfFilesXP = folder.listFiles();

			for (int i = 0; i < listOfFilesXP.length; i++) {
				File aFileXP = listOfFilesXP[i];
				String nameXP = aFileXP.getName();

				if (!aFileXP.isDirectory())
					continue;

				File[] listOfFilesDataset = aFileXP.listFiles();
				Arrays.sort(listOfFilesDataset);
				for (int j = 0; j < listOfFilesDataset.length; j++) {
					File aFileDataset = listOfFilesDataset[j];
					String nameDataset = aFileDataset.getName();

					if (!aFileDataset.isDirectory())
						continue;

					File fileRuntime = new File(aFileDataset.getAbsolutePath() + "/runtime.data");
					// if file doesnt exists, then create it
					if (!fileRuntime.exists()) {
						fileRuntime.createNewFile();
					}
					FileWriter fwRuntime = new FileWriter(fileRuntime.getAbsoluteFile());
					BufferedWriter bwRuntime = new BufferedWriter(fwRuntime);

					File fileLengthOut = new File(aFileDataset.getAbsolutePath() + "/outliersLength.data");
					// if file doesnt exists, then create it
					if (!fileLengthOut.exists()) {
						fileLengthOut.createNewFile();
					}
					FileWriter fwLengthOut = new FileWriter(fileLengthOut.getAbsoluteFile());
					BufferedWriter bwLengthOut = new BufferedWriter(fwLengthOut);

					File fileLength = new File(aFileDataset.getAbsolutePath() + "/length.data");
					// if file doesnt exists, then create it
					if (!fileLength.exists()) {
						fileLength.createNewFile();
					}
					FileWriter fwLength = new FileWriter(fileLength.getAbsoluteFile());
					BufferedWriter bwLength = new BufferedWriter(fwLength);

					File fileQualityOut = new File(aFileDataset.getAbsolutePath() + "/outliersQual.data");
					// if file doesnt exists, then create it
					if (!fileQualityOut.exists()) {
						fileQualityOut.createNewFile();
					}
					FileWriter fwQualityOut = new FileWriter(fileQualityOut.getAbsoluteFile());
					BufferedWriter bwQualityOut = new BufferedWriter(fwQualityOut);

					File fileQuality = new File(aFileDataset.getAbsolutePath() + "/qualities.data");
					// if file doesnt exists, then create it
					if (!fileQuality.exists()) {
						fileQuality.createNewFile();
					}
					FileWriter fwQuality = new FileWriter(fileQuality.getAbsoluteFile());
					BufferedWriter bwQuality = new BufferedWriter(fwQuality);

					List<String> qualList = new ArrayList<String>();
					List<String> lengthList = new ArrayList<String>();
					List<String> nameList = new ArrayList<String>();
					List<Integer> runtimeList = new ArrayList<Integer>();

					boolean first = true;
					int maxOutput = -1;
					int countC = 0;
					renameFolders(aFileDataset, nameXP);
					File[] listOfFilesParam = aFileDataset.listFiles();
					Arrays.sort(listOfFilesParam);
					for (int k = 0; k < listOfFilesParam.length; k++) {
						File aFileParam = listOfFilesParam[k];
						String nameParam = aFileParam.getName();
						if (isDouble(nameParam)) {
							nameParam = "iter_" + nameParam;
						}

						if (!aFileParam.isDirectory())
							continue;

						if (first) {
							bwLength.write(nameParam);
							bwQuality.write(nameParam);
							first = false;
						} else {
							bwLength.write("\t" + nameParam);
							bwQuality.write("\t" + nameParam);
						}

						int count = 0;
						int runtime = 0;
						String qual = "";
						String length = "";
						File[] listOfFilesRun = aFileParam.listFiles();
						Arrays.sort(listOfFilesRun);
						for (int l = 0; l < listOfFilesRun.length; l++) {
							File aFileRun = listOfFilesRun[l];
							String nameRun = aFileRun.getName();

							if (!aFileXP.isDirectory())
								continue;

							if (aFileRun.getName().startsWith("res")) {
								count++;
								BufferedReader brInfo = new BufferedReader(
										new FileReader(aFileRun.getAbsolutePath() + "/info.log"));
								String line;
								while ((line = brInfo.readLine()) != null) {
									if (line.startsWith("runtime")) {
										runtime += Integer.parseInt(line.replace(" : ", "#").split("#")[1]);
									} else if (line.startsWith("measures")) {
										if (!qual.isEmpty()) {
											qual += " ";
										}
										qual += line.replace(" : ", "#").split("#")[1];
									} else if (line.startsWith("descriptionLength")) {
										if (!length.isEmpty()) {
											length += " ";
										}
										length += line.replace(" : ", "#").split("#")[1];
									} else if (maxOutput == -1 && line.startsWith("maxOutput")) {
										maxOutput = Integer.parseInt(line.replace(" : ", "#").split("#")[1]);
									}
								}
								brInfo.close();

							}
						}

						countC = count;
						runtime /= count;
						nameList.add(nameParam);
						qualList.add(qual);
						runtimeList.add(runtime);
						lengthList.add(length);
					}

					for (int idList = 0; idList < nameList.size(); idList++) {
						bwRuntime.write(
								nameList.get(idList) + "\t" + runtimeList.get(idList) + "\t(" + (idList + 1) + ")\n");
					}

					maxOutput *= countC;
					bwQuality.write("\n");
					bwLength.write("\n");
					writeOutliers(nameList, maxOutput, bwQualityOut, qualList);
					writeOutliers(nameList, maxOutput, bwLengthOut, lengthList);
					writeData(nameList, maxOutput, bwQuality, qualList, true);
					writeData(nameList, maxOutput, bwLength, lengthList, false);

					bwQuality.close();
					bwRuntime.close();
					bwLength.close();
					bwQualityOut.close();
					bwLengthOut.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void renameFolders(File aFileDataset, String nameXP) {
		File[] listOfFilesParam = aFileDataset.listFiles();
		for (File aFile : listOfFilesParam) {
			String filename = aFile.getName();
			String newFilename = new String(filename);
			if (nameXP.compareTo("Expand") == 0) {
				newFilename = newFilename.replace("_AMAF", "_3AMAF");
				newFilename = newFilename.replace("_None", "_1None");
				newFilename = newFilename.replace("_Order", "_2Order");
				File newFile = new File(aFile.getPath().replace(filename, newFilename));
				aFile.renameTo(newFile);
			} else if (nameXP.compareTo("Memory") == 0) {
				if (newFilename.startsWith("All"))
					newFilename = newFilename.replace("AllE", "B_AllE");
				if (newFilename.startsWith("None"))
					newFilename = newFilename.replace("None", "A_None");
				if (newFilename.startsWith("TopK"))
					newFilename = newFilename.replace("TopK", "C_TopK");
				newFilename = newFilename.replace("_10", "_010");
				newFilename = newFilename.replace("_1", "_001");
				newFilename = newFilename.replace("_2", "_002");
				newFilename = newFilename.replace("_5", "_005");
				File newFile = new File(aFile.getPath().replace(filename, newFilename));
				aFile.renameTo(newFile);
			} else if (nameXP.compareTo("Update") == 0) {
				newFilename = newFilename.replace("_10", "_010");
				newFilename = newFilename.replace("_1", "_001");
				newFilename = newFilename.replace("_2", "_002");
				newFilename = newFilename.replace("_5", "_005");
				File newFile = new File(aFile.getPath().replace(filename, newFilename));
				aFile.renameTo(newFile);
			} else if (nameXP.compareTo("RollOut") == 0) {
				newFilename = newFilename.replace("TopK-10", "TopK-010");
				newFilename = newFilename.replace("TopK-1", "TopK-001");
				newFilename = newFilename.replace("TopK-2", "TopK-002");
				newFilename = newFilename.replace("TopK-5", "TopK-005");
				if (newFilename.startsWith("RandomPath"))
					newFilename = newFilename.replace("RandomPath", "A_RandomPath");
				newFilename = newFilename.replace("Large-10_", "Large-010_");
				newFilename = newFilename.replace("Large-20_", "Large-020_");
				newFilename = newFilename.replace("Large-50_", "Large-050_");
				File newFile = new File(aFile.getPath().replace(filename, newFilename));
				aFile.renameTo(newFile);
			} else if (nameXP.compareTo("UCB") == 0) {
				newFilename = newFilename.replace("_AMAF", "_3AMAF");
				newFilename = newFilename.replace("_None", "_1None");
				newFilename = newFilename.replace("_Order", "_2Order");
				File newFile = new File(aFile.getPath().replace(filename, newFilename));
				aFile.renameTo(newFile);
			}
		}

	}

	public static void writeOutliers(List<String> nameList, int maxOutput, BufferedWriter bwQuality,
			List<String> qualList) {
		try {
			for (int idListI = 0; idListI < nameList.size(); idListI++) {
				String[] temp = qualList.get(idListI).split(" ");
				List<Double> v = new ArrayList<Double>();
				for (int id = 0; id < temp.length; id++) {
					if (temp[id] != null && !temp[id].isEmpty())
						v.add(Double.parseDouble(temp[id]));
				}
				Collections.sort(v);

				if (v.size() > 0) {
					double lowerQ = v.get((int) Math.round((double) (v.size()) * 25 / 100));
					double upperQ = v.get((int) Math.round((double) (v.size()) * 75 / 100));
					double upperLimit = upperQ + 1.5 * (upperQ - lowerQ);
					double lowerLimit = lowerQ - 1.5 * (upperQ - lowerQ);

					HashSet<Double> hashTemp = new HashSet<Double>();
					hashTemp.addAll(v);
					v.clear();
					v.addAll(hashTemp);
					Collections.sort(v);

					for (double pointValue : v) {
						if (pointValue < lowerLimit || pointValue > upperLimit) {
							// write it
							bwQuality.write(idListI + " " + pointValue + "\n");
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeData(List<String> nameList, int maxOutput, BufferedWriter bwQuality, List<String> qualList,
			boolean isqual) {
		try {
			String[][] qualTab = new String[nameList.size()][maxOutput];
			for (int idListI = 0; idListI < nameList.size(); idListI++) {
				String[] temp = qualList.get(idListI).split(" ");
				if (temp.length > maxOutput) {
					System.err.println("Pb");
					continue;
				}

				for (int idListSecJ = 0; idListSecJ < temp.length; idListSecJ++) {
					qualTab[idListI][idListSecJ] = temp[idListSecJ];
				}
			}

			for (int idListJ = 0; idListJ < maxOutput; idListJ++) {
				for (int idListI = 0; idListI < qualTab.length; idListI++) {
					if (idListI > 0)
						bwQuality.write("\t");

					if (qualTab[idListI][idListJ] == null || qualTab[idListI][idListJ].isEmpty()) {
						bwQuality.write("m");
					} else {
						bwQuality.write(qualTab[idListI][idListJ]);
					}
				}
				bwQuality.write("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isDouble(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}
