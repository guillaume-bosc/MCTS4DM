package de.fraunhofer.iais.ocm.core.mining.patternsampling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import de.fraunhofer.iais.ocm.common.parameter.rangebounder.ExtensionalRangeBounder;
import de.fraunhofer.iais.ocm.core.mining.EMMAlgorithm;
import de.fraunhofer.iais.ocm.core.mining.TargetListProposer;
import de.fraunhofer.iais.ocm.core.mining.parameter.ListMiningParameterAnnotation;
import de.fraunhofer.iais.ocm.core.mining.utility.PatternPruner;
import de.fraunhofer.iais.ocm.core.mining.utility.PosNegDatabaseCreator;
import de.fraunhofer.iais.ocm.core.mining.utility.PosNegDatabaseCreator.PosNegDatabaseByFirstAttribute;
import de.fraunhofer.iais.ocm.core.mining.utility.SinglePatternPostProcessor;
import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.data.propositions.Proposition;
import de.fraunhofer.iais.ocm.core.model.pattern.ExceptionalModelPattern;
import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.model.pattern.emm.ExceptionalModelPatternFactory;
import de.fraunhofer.iais.ocm.core.model.utility.PatternUtilityModel;
import mime.plain.PlainItem;
import mime.plain.PlainItemSet;
import mime.plain.weighting.PosNegDbInterface;

public class ExceptionalModelSampler extends ConsaptBasedSamplingMiner implements EMMAlgorithm {

	private List<Attribute> targets = null;

	private ExceptionalModelPatternFactory patternFactory = null;

	private SinglePatternPostProcessor postProcessor = new PatternPruner(ExceptionalModelPattern.DEVIATION_COMPARATOR);

	private DistributionFactory distributionFactory = new DiscriminativityDistributionFactory(0, 1, 1);

	private PosNegDatabaseCreator posNegDatabaseCreator = new PosNegDatabaseByFirstAttribute();

	private int maxNumResults = 100;

	@Override
	public void setStop(boolean isStop) {
		super.setStop(isStop);
		this.postProcessor.setStop(isStop);
	}

	public void setDistributionFactory(DistributionFactory distributionFactory) {
		this.distributionFactory = distributionFactory;
	}

	public void setPosNegDatabaseCreator(PosNegDatabaseCreator posNegDatabaseCreator) {
		this.posNegDatabaseCreator = posNegDatabaseCreator;
	}

	@Override
	public void mine(DataTable dataTable, PatternUtilityModel patternUtilityModel) throws Exception {
		setStop(false);

		if (this.targets == null) {
			this.targets = TargetListProposer.INSTANCE.proposeTargets(dataTable, patternUtilityModel, 1, null);
		}

		Collection<Pattern> results = new HashSet<Pattern>();
		setResults(results);

		
		setSampler(this.distributionFactory.getDistribution(this, dataTable));
		
		if (isStop()) {
			setSampler(null);
			return;
		}

		do {
			// long beg = System.currentTimeMillis();
			PlainItemSet plainPattern = this.sampler.getNext();
			// System.out.println("sample " + (System.currentTimeMillis() -
			// beg));
			// beg = System.currentTimeMillis();
			ExceptionalModelPattern pattern = parseRawDescription(plainPattern, dataTable);
			// System.out.println("convert " + (System.currentTimeMillis() -
			// beg));
			// System.out.println("Sample: " + pattern);

			// beg = System.currentTimeMillis();
			pattern = (ExceptionalModelPattern) this.postProcessor.prune(pattern);
			// System.out.println("prune " + (System.currentTimeMillis() -
			// beg));
			
			results.add(pattern);

			// check stop condition before adding pattern to the results list to
			// avoid concurrent modification
			if (isStop()) {
				setSampler(null);
				return;
			}

		} while (results.size() != getMaxNumResults() || getMaxNumResults() == -1);
		setSampler(null);

		setStop(true);
	}

	protected ExceptionalModelPattern parseRawDescription(Iterable<PlainItem> rawPattern, DataTable dataTable) {

		List<Proposition> description = new ArrayList<Proposition>();
		for (PlainItem item : rawPattern) {
			Proposition proposition = dataTable.getPropositionStore().getPropositions()
					.get(Integer.parseInt(item.getName()));
			description.add(proposition);
		}
		return this.patternFactory.newExceptionModel(dataTable, description, this.targets);
	}

	public PosNegDbInterface getPosNegDatabase(DataTable dataTable) {
		return this.posNegDatabaseCreator.createDb(dataTable, this.targets);
	}

	@Override
	@ListMiningParameterAnnotation(getDescription = "The attributes according to which subgroups are supposed to stand out", getName = "Target Attributes", getRangeBounder = ExtensionalRangeBounder.NON_ID_ATTRIBUTES)
	public void setTargetAttributes(List<Attribute> targets) {
		this.targets = targets;
	}

	@Override
	public List<Attribute> getTargetAttributes() {
		return this.targets;
	}

	@Override
	@ListMiningParameterAnnotation(getDescription = "The kind of model which is fitted to target attributes", getName = "Model Class", getRangeBounder = ExtensionalRangeBounder.NON_ID_ATTRIBUTES)
	public void setEMPatternFactory(ExceptionalModelPatternFactory factory) {
		this.patternFactory = factory;
	}

	@Override
	public String toString() {
		return "SubgroupSampler|" + this.distributionFactory + "|" + this.postProcessor;
	}

	public SinglePatternPostProcessor getPostProcessor() {
		return this.postProcessor;
	}

	public void setPostProcessor(SinglePatternPostProcessor postProcessor) {
		this.postProcessor = postProcessor;
	}

	@Override
	protected int getMaxNumResults() {
		return this.maxNumResults;
	}

	public void setMaxNumResults(int maxNumResults) {
		this.maxNumResults = maxNumResults;
	}

}
