package edu.uab.emmSampling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.lucene.util.OpenBitSet;

import com.google.common.collect.Lists;
import com.opencsv.CSVReader;

import de.fraunhofer.iais.ocm.core.mining.patternsampling.DiscriminativityDistributionFactory;
import de.fraunhofer.iais.ocm.core.mining.patternsampling.DistributionFactory;
import de.fraunhofer.iais.ocm.core.mining.patternsampling.ExceptionalModelSampler;
import de.fraunhofer.iais.ocm.core.mining.utility.NoPatternPruner;
import de.fraunhofer.iais.ocm.core.mining.utility.PatternPruner;
import de.fraunhofer.iais.ocm.core.mining.utility.PosNegDatabaseCreator;
import de.fraunhofer.iais.ocm.core.mining.utility.SinglePatternPostProcessor;
import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.CategoricalAttribute;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.data.NumericAttribute;
import de.fraunhofer.iais.ocm.core.model.pattern.ExceptionalModelPattern;
import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.model.pattern.emm.ExceptionalContingencyTablePatternFactory;
import de.fraunhofer.iais.ocm.core.model.pattern.emm.ExceptionalMeanDeviationPatternFactory;
import de.fraunhofer.iais.ocm.core.model.pattern.emm.ExceptionalModelPatternFactory;

/**
 *
 */
public class EMMSampler {

	private static final int ATTRIBUTE_NAME_COLUMN = 0;
	private static final int ATTRIBUTE_TYPE_COLUMN = 1;
	private static final int ATTRIBUTE_DESCRIPTION_COLUMN = 2;

	private static final String TYPE_NUMERIC = "numeric";
	private static final String TYPE_CATEGORICAL = "categoric";
	private static final String TYPE_ID = "id";

	private static enum TargetModelClass {
		MeanModel, ContingencyTable;

		public static TargetModelClass targetModelClass(String targetModelClass) {
			if (targetModelClass.toLowerCase().equals(MeanModel.name().toLowerCase())) {
				return TargetModelClass.MeanModel;
			} else if (targetModelClass.toLowerCase().equals(ContingencyTable.name().toLowerCase())) {
				return TargetModelClass.ContingencyTable;
			}
			throw new IllegalArgumentException(
					String.format("Unknown model class '%s'. Please specify one of the following ['%s']",
							targetModelClass, String.join("', '", Arrays.stream(TargetModelClass.values())
									.map(m -> m.toString().toLowerCase()).collect(Collectors.toList()))));
		}
	}

	private static class Configuration {

		public TargetModelClass targetModelClass;
		public String targetAttributes;
		public int positiveFrequencyCount;
		public int negativeFrequencyCount;
		public boolean prune;
		public String attributeFile;
		public String dataFile;
		public int resultCount;

	}

	public static void printHelp() {
		System.err.println("Please specify the following parameters:");
		System.err.println(
				"\t<modelClass> <targetAttributes> <positiveFrequencyCount> <negativeFrequencyCount> <prune> <attributeFile> <dataFile> <resultCount>");
		System.err.println("");
		System.err.println(
				String.format("modelClass:\n\teither '%s' or '%s'", TargetModelClass.MeanModel.toString().toLowerCase(),
						TargetModelClass.ContingencyTable.toString().toLowerCase()));
		System.err.println("targetAttributes:\n\tcomma separated list of attribute names");
		System.err.println("positiveFrequencyCount:\n\tpositive frequency count (integral type)");
		System.err.println("negativeFrequencyCount:\n\tnegative frequency count (integral type)");
		System.err.println(
				"prune:\n\tindicates if patterns should be pruned after sampling to optimize the quality ('true' or false')");
		System.err.println("attributeFile:\n\tfile with information about the attributes");
		System.err.println("dataFile:\n\tfile containing the data");
		System.err.println(
				"resultCount:\n\tnumber of patterns to find. If set to -1, the sampler continues untill it is killed");
	}

	public static void main(String[] args2) {
		String[] args = { "MeanModel", "Bankruptcies", "1", "1", "yes", "./data/germany/attributes.txt",
				"./data/germany/data.txt", "10" };
		emm(args);
	}
	
	public static List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> emm(String[] args) {
		//printHelp();
		Configuration configuration = new Configuration();
		try {
			configuration.targetModelClass = TargetModelClass.targetModelClass(args[0]);
			configuration.targetAttributes = args[1];
			configuration.positiveFrequencyCount = Integer.parseInt(args[2]);
			configuration.negativeFrequencyCount = Integer.parseInt(args[3]);
			configuration.prune = Boolean.parseBoolean(args[4]);
			configuration.attributeFile = args[5];
			configuration.dataFile = args[6];
			configuration.resultCount = Integer.parseInt(args[7]);

			if (!new File(configuration.attributeFile).exists()) {
				throw new FileNotFoundException(String.format("File '%s' does not exist", configuration.attributeFile));
			}
			if (!new File(configuration.dataFile).exists()) {
				throw new FileNotFoundException(String.format("File '%s' does not exist", configuration.dataFile));
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			printHelp();
			System.exit(1);
		}

		return createSampler(configuration);
	}

	private static List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> createSampler(Configuration configuration) {
		List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> resultPatternSet = new ArrayList<liris.cnrs.fr.dm2l.mcts4dm.Pattern>();
		try {
			DataTable dataTable = getDataTable(configuration);

			ExceptionalModelSampler ems = new ExceptionalModelSampler();

			ems.setDistributionFactory(getDistributionFactory(configuration));
			ems.setEMPatternFactory(getEMMPatternFactory(configuration));
			ems.setPosNegDatabaseCreator(getPosNegDatabaseCreator(configuration));
			ems.setPostProcessor(getPostProcessor(configuration));
			ems.setTargetAttributes(getAttributes(configuration, dataTable));
			ems.setMaxNumResults(configuration.resultCount);

			ems.mine(dataTable, null);
			
			Collection<Pattern> c = ems.getResults();

			for (Pattern p : c) {
				OpenBitSet extent = new OpenBitSet();
				for (Integer a: p.getSupportSet())
					extent.set(a);
	            resultPatternSet.add(new liris.cnrs.fr.dm2l.mcts4dm.Pattern(
	            		((ExceptionalModelPattern) p).getModelDeviation(), extent));
				/*System.out.print( ((ExceptionalModelPattern) p).getModelDeviation() + "\t");
				System.out.print(p.getSupportSet() + "\t");
				System.out.println(p);*/
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultPatternSet;
	}

	private static DataTable getDataTable(Configuration configuration) throws Exception {
		List<List<String>> rawAttributeData = readData(configuration.attributeFile, ';');
		List<List<String>> rawData = readData(configuration.dataFile, ';');

		DataTable dataTable = new DataTable();
		dataTable.setFile_id(0);
		dataTable.setData_file_name(configuration.attributeFile);

		for (int i = 0; i < rawAttributeData.size(); i++) {
			Attribute attribute = null;
			int indexInTable = dataTable.getAttributes().size();
			List<String> values = new ArrayList<String>(rawData.size());
			for (int j = 0; j < rawData.size(); j++) {
				values.add(rawData.get(j).get(i));
			}
			if (rawAttributeData.get(i).get(ATTRIBUTE_TYPE_COLUMN).equals(TYPE_CATEGORICAL)) {
				attribute = new CategoricalAttribute(rawAttributeData.get(i).get(ATTRIBUTE_NAME_COLUMN),
						rawAttributeData.get(i).get(ATTRIBUTE_DESCRIPTION_COLUMN), values, indexInTable, dataTable);
			} else if (rawAttributeData.get(i).get(ATTRIBUTE_TYPE_COLUMN).equals(TYPE_NUMERIC)) {
				attribute = new NumericAttribute(rawAttributeData.get(i).get(ATTRIBUTE_NAME_COLUMN),
						rawAttributeData.get(i).get(ATTRIBUTE_DESCRIPTION_COLUMN), values, indexInTable, dataTable);
			} else if (rawAttributeData.get(i).get(ATTRIBUTE_TYPE_COLUMN).equals(TYPE_ID)) {
				attribute = new Attribute(rawAttributeData.get(i).get(ATTRIBUTE_NAME_COLUMN), true,
						rawAttributeData.get(i).get(ATTRIBUTE_DESCRIPTION_COLUMN), values, indexInTable, dataTable);
			}
			dataTable.getAttributes().add(attribute);
			if (attribute instanceof NumericAttribute) {
				for (int missingPosition : attribute.getMissingPositions()) {
					rawData.get(missingPosition).set(i, String.valueOf(((NumericAttribute) attribute).getMedian()));
				}
			}
		}
		List<String> nameList = new ArrayList<String>(dataTable.getAttributes().size());

		for (Attribute attribute : dataTable.getAttributes()) {
			nameList.add(attribute.getName());
		}

		dataTable.initPropositionStore();
		return dataTable;

	}

	private static List<List<String>> readData(String fileName, char delimiter) throws Exception {
		List<List<String>> data = Lists.newArrayList();

		CSVReader reader = new CSVReader(new FileReader(fileName), delimiter);

		String[] row;
		while ((row = reader.readNext()) != null) {
			data.add(Lists.newArrayList(row));
		}

		reader.close();

		return data;
	}

	private static DistributionFactory getDistributionFactory(Configuration configuration) {
		return new DiscriminativityDistributionFactory(0, configuration.positiveFrequencyCount,
				configuration.negativeFrequencyCount);
	}

	private static ExceptionalModelPatternFactory getEMMPatternFactory(Configuration configuration) {
		if (configuration.targetModelClass.equals(TargetModelClass.MeanModel)) {
			return ExceptionalMeanDeviationPatternFactory.INSTANCE;
		} else if (configuration.targetModelClass.equals(TargetModelClass.ContingencyTable)) {
			return ExceptionalContingencyTablePatternFactory.INSTANCE;
		}
		// Should never occur
		return null;
	}

	private static PosNegDatabaseCreator getPosNegDatabaseCreator(Configuration configuration) {
		if (configuration.targetModelClass.equals(TargetModelClass.MeanModel)) {
			return new PosNegDatabaseCreator.PosNegDatabaseUsingPCA();
		} else if (configuration.targetModelClass.equals(TargetModelClass.ContingencyTable)) {
			return new PosNegDatabaseCreator.InverseProbabilityPosNegDatabase();
		}
		// Should never occur
		return null;
	}

	private static SinglePatternPostProcessor getPostProcessor(Configuration configuration) {
		if (configuration.prune) {
			return new PatternPruner(ExceptionalModelPattern.FREQUENCYDEVIATION_COMPARATOR);
		}
		return new NoPatternPruner();
	}

	private static List<Attribute> getAttributes(Configuration configuration, DataTable dataTable) {
		List<Attribute> attributes = Lists.newArrayList();

		List<String> notFound = Lists.newArrayList();

		for (String attributeName : configuration.targetAttributes.split(",")) {
			int index = dataTable.getAttributeNames().indexOf(attributeName);
			if (index == -1) {
				notFound.add(attributeName);
			} else {
				attributes.add(dataTable.getAttribute(index));
			}
		}

		if (!notFound.isEmpty()) {
			throw new IllegalArgumentException(
					String.format("The following attributes have not been found: %s", String.join(", ", notFound)));
		}

		return attributes;
	}

}
