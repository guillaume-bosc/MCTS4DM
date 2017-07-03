package de.fraunhofer.iais.ocm.core.mining;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.model.utility.PatternUtilityModel;

public class LoopingMetaAlgorithm extends AbstractMiner implements MetaAlgorithm {

	private MiningAlgorithm entailedAlgorithm;

	public LoopingMetaAlgorithm(MiningAlgorithm entailedAlgorithm) {
		this.entailedAlgorithm = entailedAlgorithm;
	}

	@Override
	public void mine(DataTable dataTable,
			PatternUtilityModel patternUtilityModel) throws Exception {
		List<Pattern> results=new ArrayList<Pattern>();
		setResults(results);
		for (int i = 0; i < dataTable.getNumOfNonIDAttrs(); i++) {
			entailedAlgorithm.mine(dataTable, patternUtilityModel);
            results.addAll(entailedAlgorithm.getResults());
			if (isStop()) {
				return;
			}
		}
	}

	@Override
	public synchronized void setStop(boolean stop) {
		this.entailedAlgorithm.setStop(stop);
		super.setStop(stop);
	}

	@Override
	public String toString() {
		return "1..n:" + entailedAlgorithm.toString();
	}
}
