package de.fraunhofer.iais.ocm.core.mining;

import java.util.Collection;
import java.util.List;

import de.fraunhofer.iais.ocm.core.mining.parameter.ParameterSelector;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.model.utility.PatternUtilityModel;

public class ParameterChoosingMetaAlgorithm<T extends MiningAlgorithm> extends AbstractMiner implements MetaAlgorithm {

	private T entailedAlgorithm;

	private List<ParameterSelector<T>> parameterSelectors;

	public ParameterChoosingMetaAlgorithm(T entailedAlgorithm,
			List<ParameterSelector<T>> parameterSelectors) {
		this.entailedAlgorithm = entailedAlgorithm;
		this.parameterSelectors = parameterSelectors;
	}

	@Override
	public void mine(DataTable dataTable,
			PatternUtilityModel patternUtilityModel) throws Exception {
		for (ParameterSelector<T> selector : this.parameterSelectors) {
			selector.setParameter(entailedAlgorithm, dataTable,
					patternUtilityModel);
		}
		this.entailedAlgorithm.mine(dataTable, patternUtilityModel);
	}

	@Override
	public void setStop(boolean stop) {
		entailedAlgorithm.setStop(stop);
	}

	@Override
	public boolean isStop() {
		return entailedAlgorithm.isStop();
	}

	@Override
	public Collection<Pattern> getResults() throws IllegalStateException {
		return entailedAlgorithm.getResults();
	}

	@Override
	public String toString() {
		StringBuilder resultBuilder = new StringBuilder(
				entailedAlgorithm.toString());
		for (ParameterSelector<T> selector : this.parameterSelectors) {
			resultBuilder.append("|" + selector.toString());
		}
		return resultBuilder.toString();
	}
}
