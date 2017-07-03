package de.fraunhofer.iais.ocm.core.mining.beamsearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.fraunhofer.iais.ocm.common.parameter.rangebounder.ExtensionalRangeBounder;
import de.fraunhofer.iais.ocm.core.mining.EMMAlgorithm;
import de.fraunhofer.iais.ocm.core.mining.TargetListProposer;
import de.fraunhofer.iais.ocm.core.mining.parameter.ListMiningParameterAnnotation;
import de.fraunhofer.iais.ocm.core.mining.utility.PropositionFilter;
import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.data.propositions.Proposition;
import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.model.pattern.emm.ExceptionalModelPatternFactory;
import de.fraunhofer.iais.ocm.core.model.utility.PatternUtilityModel;

public class ExceptionalModelBeamSearch extends BeamSearch implements
		EMMAlgorithm {

	private List<Attribute> targets;

	private ExceptionalModelPatternFactory emFactory;

	public void setEmFactory(ExceptionalModelPatternFactory emFactory) {
		this.emFactory = emFactory;
	}

	@Override
	protected Pattern generateSeed(DataTable dataTable)
			throws IllegalArgumentException {
		return emFactory.newExceptionModel(dataTable,
				new ArrayList<Proposition>(), this.targets);
	}

	@Override
	protected void postProcessing(Collection<Pattern> results) {
	}

	@Override
	/**
	 * This is called in the beginning of mining process
	 * here we define our target attributes
	 * and description attributes(propositions)
	 */
	protected void initialize(DataTable dataTable,
			PatternUtilityModel utilityModel) {
		// select target attributes randomly
		if (this.targets == null) {
			this.targets = new ArrayList<Attribute>(
					TargetListProposer.INSTANCE.proposeTargets(dataTable,
							utilityModel, 2, null));
		}

		propositions = new ArrayList<Proposition>();
		for (Proposition proposition : dataTable.getPropositionStore()
				.getPropositions()) {
			if (!targets.contains(proposition.getAttribute())
					&& !PropositionFilter.oneTargetIsPartOfMacroAttributeWith(
							this.targets, proposition.getAttribute())) {
				propositions.add(proposition);
			}
		}
	}

	@Override
	@ListMiningParameterAnnotation(getDescription = "The attributes according to which subgroups are supposed to stand out", getName = "Target Attributes", getRangeBounder = ExtensionalRangeBounder.NON_ID_ATTRIBUTES)
	public void setTargetAttributes(List<Attribute> targets) {
		this.targets = targets;
	}

	@Override
	public String toString() {
		return "EMMBeamSearch";
	}

	@Override
	public List<Attribute> getTargetAttributes() {
		return this.targets;
	}

	@Override
	@ListMiningParameterAnnotation(getDescription = "The kind of model which is fitted to target attributes", getName = "Model Class", getRangeBounder = ExtensionalRangeBounder.NON_ID_ATTRIBUTES)
	public void setEMPatternFactory(ExceptionalModelPatternFactory factory) {
		this.emFactory = factory;
	}

}
