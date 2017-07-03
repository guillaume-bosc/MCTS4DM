package nl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.util.OpenBitSet;

import nl.liacs.subdisc.AttributeType;
import nl.liacs.subdisc.FileLoaderARFF;
import nl.liacs.subdisc.Process;
import nl.liacs.subdisc.QM;
import nl.liacs.subdisc.SearchParameters;
import nl.liacs.subdisc.Subgroup;
import nl.liacs.subdisc.SubgroupDiscovery;
import nl.liacs.subdisc.Table;
import nl.liacs.subdisc.TargetConcept;

public class CortanaWrapper {

	public static void main(String[] args) throws Exception
	{
		String base = "gen5P200SD0.05N0.05O5000t50a50c";
		 base = "cal500";
		List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> resultPatternSet = cortanaBeamSearch(base);
		//for (liris.cnrs.fr.dm2l.mcts4dm.Pattern p : resultPatternSet)
			//System.out.println(p);
	}

	public static List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> cortanaBeamSearch(String base)
	{
		System.out.println("loading..");
		FileLoaderARFF loader = new FileLoaderARFF(new File("./data/"+ base + "/cortana.arff"));
		System.out.println("end loading");
		Table table = loader.getTable();
		table.update();
		SearchParameters searchParameters = getSearchParameters(table);
		int i=0;
		System.out.println(table.getNrColumns());
		for (i = 0; i < table.getNrColumns(); i++) {
			table.getColumn(i).setTargetStatus(" none");
			table.getColumn(i).setType(AttributeType.NUMERIC);
			table.getColumn(i).setIsEnabled(true);
		}
		table.getColumn(i-1).setTargetStatus(" primary");
		table.getColumn(i-1).setType(AttributeType.NOMINAL);

		SubgroupDiscovery sgd = Process.runSubgroupDiscovery(table, 0, null, searchParameters, false, 1, null);
		List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> resultPatternSet = new ArrayList<liris.cnrs.fr.dm2l.mcts4dm.Pattern>();
		for (Subgroup s: sgd.getResult())
		{
			System.out.println(s);
			resultPatternSet.add(new liris.cnrs.fr.dm2l.mcts4dm.Pattern(s.getMeasureValue(),
					new OpenBitSet(s.itsMembers.toLongArray(), s.itsMembers.toLongArray().length)));
		}
		return resultPatternSet;
	}


	public static SearchParameters getSearchParameters(Table itsTable)
	{
		TargetConcept itsTargetConcept = new TargetConcept(); // todo :/
		SearchParameters itsSearchParameters = new SearchParameters();
		itsSearchParameters.setTargetConcept(itsTargetConcept);
		/*
		 * TARGET CONCEPT
		 * some cleaning is done to create proper AutoRun-XMLs
		 */
		//TargetType aType = TargetType.getDefault(); // itsTargetConcept.getTargetType();

		//if (TargetType.hasTargetAttribute(aType))
		itsTargetConcept.setPrimaryTarget(itsTable.getColumn("Class")) ;//getTargetAttributeName()));
		//else
		//	itsTargetConcept.setPrimaryTarget(null);

		//if (aType == TargetType.SINGLE_NOMINAL)
		itsTargetConcept.setTargetValue("cortana");//getMiscFieldName());
		//else
		//	itsTargetConcept.setTargetValue(null);

		//if (TargetType.hasSecondaryTarget(aType))
		//	itsTargetConcept.setSecondaryTarget(itsTable.getColumn(getMiscFieldName()));
		//else
		itsTargetConcept.setSecondaryTarget(null);

		// are already set when needed, remove possible old values
		//if (!TargetType.hasMultiTargets(aType))
		//	itsTargetConcept.setMultiTargets(new ArrayList<Column>(0));
		// assumes COOKS_DISTANCE is only valid for DOUBLE_REGRESSION
		//if (QM.COOKS_DISTANCE.GUI_TEXT.equals(getQualityMeasureName()))
		//	itsTargetConcept.setMultiRegressionTargets(new ArrayList<Column>(0));

		/*
		 * SEARCH PARAMETERS
		 */
		itsSearchParameters.setQualityMeasure(QM.fromString("wracc"));//getQualityMeasureName()));
		itsSearchParameters.setQualityMeasureMinimum(new Float(0));//getQualityMeasureMinimum());
		itsSearchParameters.setSearchDepth(10); //getSearchDepthMaximum());
		itsSearchParameters.setMinimumCoverage(300); //getSearchCoverageMinimum());
		itsSearchParameters.setMaximumCoverageFraction(new Float(1));//getSearchCoverageMaximum());
		itsSearchParameters.setMaximumSubgroups(20); // 0 c'est infini
		itsSearchParameters.setMaximumTime(300);//getSearchTimeMaximum());
		itsSearchParameters.setSearchStrategy("beam");//getSearchStrategyName());
		// set to last known value even for SearchStrategy.BEST_FIRST
		itsSearchParameters.setSearchStrategyWidth(30); //getStrategyWidth());
		itsSearchParameters.setNominalSets(false); //getSetValuedNominals());
		itsSearchParameters.setNumericStrategy("all");//getNumericStrategy());
		itsSearchParameters.setNumericOperators("<html>&#8804;, &#8805;</html>"); //getNumericOperators());
		// set to last known value even for NumericStrategy.NUMERIC_BINS
		itsSearchParameters.setNrBins(8); //getNrBins());
		itsSearchParameters.setNrThreads(1);
		return itsSearchParameters;
	}



}
