package de.fraunhofer.iais.ocm.core.mining.patternsampling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import mime.plain.PlainItem;
import mime.plain.PlainItemSet;
import de.fraunhofer.iais.ocm.core.mining.AssociationMiningAlgorithm;
import de.fraunhofer.iais.ocm.core.mining.utility.PatternPruner;
import de.fraunhofer.iais.ocm.core.mining.utility.SinglePatternPostProcessor;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.data.propositions.Proposition;
import de.fraunhofer.iais.ocm.core.model.pattern.Association;
import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.model.utility.PatternUtilityModel;
import edu.uab.consapt.sampling.TwoStepPatternSampler;

public class AssociationSampler extends ConsaptBasedSamplingMiner implements
		AssociationMiningAlgorithm {

	protected DistributionFactory distributionFactory;

	private SinglePatternPostProcessor postProcessor = new PatternPruner(
			Association.LIFT_COMPARATOR);

	public void setDistributionFactory(DistributionFactory distributionFactory) {
		this.distributionFactory = distributionFactory;
	}

	@Override
	public String toString() {
		return "AssociationSampler|" + distributionFactory+"|"+postProcessor;
	}

	public void mine(DataTable dataTable,
			PatternUtilityModel patternUtilityModel) throws Exception {
		mine(dataTable, patternUtilityModel, null);
	}

	public void mine(DataTable dataTable,
			PatternUtilityModel patternUtilityModel, Object parameters)
			throws Exception {
		Collection<Pattern> results = new HashSet<Pattern>();
		setResults(results);

		TwoStepPatternSampler sampler = initSampler(dataTable);

		setStop(false);

		do {
			PlainItemSet plainPattern = sampler.getNext();

			Association pattern = parseRawDescriptionAssViaPropositionStore(
					plainPattern, dataTable);

			if (pattern != null && pattern.getAssociationMeasure() > 0) {
				pattern = (Association) postProcessor.prune(pattern);
			}
			// check stop condition before adding pattern to the results list to
			// avoid concurrent modification
			if (isStop()) {
				return;
			}

			results.add(pattern);

		} while (results.size() != getMaxNumResults());

		setStop(true);
	}

	private TwoStepPatternSampler initSampler(DataTable dataTable)
			throws Exception {
		return this.distributionFactory.getDistribution(this, dataTable);
	}

	private Association parseRawDescriptionAssViaPropositionStore(
			Iterable<PlainItem> rawPattern, DataTable dataTable) {

		List<Proposition> description = new ArrayList<Proposition>();
		for (PlainItem item : rawPattern) {
			Proposition proposition = dataTable.getPropositionStore()
					.getPropositions().get(Integer.parseInt(item.getName()));
			description.add(proposition);
		}
		return new Association(dataTable, description);
	}

	public SinglePatternPostProcessor getPostProcessor() {
		return postProcessor;
	}

	public void setPostProcessor(SinglePatternPostProcessor postProcessor) {
		this.postProcessor = postProcessor;
	}

}
