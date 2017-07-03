package de.fraunhofer.iais.ocm.core.model.pattern;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.data.propositions.Proposition;
import de.fraunhofer.iais.ocm.core.model.pattern.emm.AbstractModel;
import de.fraunhofer.iais.ocm.core.model.pattern.emm.ContingencyTableModel;
import de.fraunhofer.iais.ocm.core.model.pattern.emm.MeanDeviationModel;
import de.fraunhofer.iais.ocm.core.model.pattern.emm.ModelDistanceFunction;
import de.fraunhofer.iais.ocm.core.model.pattern.emm.ModelFactory;

public class ExceptionalModelPattern extends Pattern {

	// test

	private static class FrequencyDeviationComparator implements Comparator<Pattern> {
		@Override
		public int compare(Pattern pattern1, Pattern pattern2) {
			if (!(pattern1 instanceof ExceptionalModelPattern) || !(pattern2 instanceof ExceptionalModelPattern)) {
				throw new IllegalArgumentException("Can only compare exceptional model patterns");
			}
			double freqTimesDeviation = Math.sqrt(pattern1.getFrequency())
					* Math.max(((ExceptionalModelPattern) pattern1).getModelDeviation(), 0);
			double freqTimesDeviation1 = Math.sqrt(pattern2.getFrequency())
					* Math.max(((ExceptionalModelPattern) pattern2).getModelDeviation(), 0);
			// double freqTimesDeviation = pattern1.getFrequency()
			// * Math.max(((ExceptionalModelPattern) pattern1)
			// .getModelDeviation(), 0);
			// double freqTimesDeviation1 = pattern2.getFrequency()
			// * Math.max(((ExceptionalModelPattern) pattern2)
			// .getModelDeviation(), 0);
			return Double.compare(freqTimesDeviation, freqTimesDeviation1);

		}

		@Override
		public String toString() {
			return "freq*deviation_order";
		}
	}

	private static class FrequencyDeviationUniqSizeComparator implements Comparator<Pattern> {
		@Override
		public int compare(Pattern pattern1, Pattern pattern2) {
			if (!(pattern1 instanceof ExceptionalModelPattern) || !(pattern2 instanceof ExceptionalModelPattern)) {
				throw new IllegalArgumentException("Can only compare exceptional model patterns");
			}
			Set<String> p1 = new HashSet<String>();
			Set<String> p2 = new HashSet<String>();
			for (Proposition pro : pattern1.getDescription()) {
				p1.add(pro.getAttribute().getName());
			}
			for (Proposition pro : pattern2.getDescription()) {
				p2.add(pro.getAttribute().getName());
			}
			double power = 1. / pattern1.getDataTable().getNumOfNonIDAttrs();
			double freqTimesDeviation = pattern1.getFrequency()
					* Math.max(((ExceptionalModelPattern) pattern1).getModelDeviation(), 0)
					* Math.pow(p1.size(), power / 2);
			double freqTimesDeviation1 = pattern2.getFrequency()
					* Math.max(((ExceptionalModelPattern) pattern2).getModelDeviation(), 0)
					* Math.pow(p2.size(), power / 2);
			return Double.compare(freqTimesDeviation, freqTimesDeviation1);

		}

		@Override
		public String toString() {
			return "freq*deviation_order*uniqsize^.1";
		}
	}

	private static class DeviationComparator implements Comparator<Pattern> {
		@Override
		public int compare(Pattern pattern1, Pattern pattern2) {
			if (!(pattern1 instanceof ExceptionalModelPattern) || !(pattern2 instanceof ExceptionalModelPattern)) {
				throw new IllegalArgumentException("Can only compare exceptional model patterns");
			}

			return Double.compare(((ExceptionalModelPattern) pattern1).getModelDeviation(),
					((ExceptionalModelPattern) pattern2).getModelDeviation());
		}

		@Override
		public String toString() {
			return "deviation_order";
		}

	}

	private static final long serialVersionUID = -319107551956281708L;

	public static final Comparator<Pattern> DEVIATION_COMPARATOR = new DeviationComparator();
	public static final Comparator<Pattern> FREQUENCYDEVIATION_COMPARATOR = new FrequencyDeviationComparator();
	public static final Comparator<Pattern> FREQUENCYDEVIATIONUNIQSIZE_COMPARATOR = new FrequencyDeviationUniqSizeComparator();

	private AbstractModel globalModel;
	private AbstractModel localModel;
	private ModelDistanceFunction modelDistanceFunction;

	private ModelFactory modelFactory;

	@Override
	public TYPE getType() {
		if (this.globalModel.getAttributes().size() == 1) {
			return TYPE.sgd;
		} else {
			return TYPE.emm;
		}
	}

	public ExceptionalModelPattern(DataTable dataTable, List<Proposition> description, List<Attribute> targets,
			ModelFactory modelFactory, ModelDistanceFunction modelDistanceFunction) {
		super(dataTable, description);
		List<Attribute> unmodTargets = unmodifiableList(targets);
		initModels(modelFactory, unmodTargets);
		this.modelDistanceFunction = modelDistanceFunction;
	}

	private void initModels(ModelFactory modelFactory, List<Attribute> targets) {
		this.modelFactory = modelFactory;
		this.globalModel = this.modelFactory.getModel(targets);
		this.localModel = this.modelFactory.getModel(targets, getSupportSet());
	}

	public ExceptionalModelPattern(ExceptionalModelPattern oldPattern, Proposition augmentation) {
		super(oldPattern, augmentation);
		initModels(oldPattern.modelFactory, oldPattern.getTargetAttributes());
		this.modelDistanceFunction = oldPattern.modelDistanceFunction;
	}

	public AbstractModel getGlobalModel() {
		return this.globalModel;
	}

	public AbstractModel getLocalModel() {
		return this.localModel;
	}

	public List<Attribute> getTargetAttributes() {
		return this.globalModel.getAttributes();
	}

	public double getModelDeviation() {
		
		/*try {
			System.out.println("local:" + ((ContingencyTableModel) this.localModel).getProbabilities());
			System.out.println("global:" + ((ContingencyTableModel) this.globalModel).getProbabilities());
		} catch (java.lang.ClassCastException e) {
			System.out.println("local:" + ((MeanDeviationModel) this.localModel).getMean());
			System.out.println("global:" + ((MeanDeviationModel) this.globalModel).getMean());
		}
		*/ // test mehdi

		return this.modelDistanceFunction.distance(this.globalModel, this.localModel);
	}

	@Override
	public Pattern generateSpecialization(Proposition augmentation) {
		return new ExceptionalModelPattern(this, augmentation);
	}

	@Override
	public Pattern generateGeneralization(Proposition reductionElement) {
		if (!getDescription().contains(reductionElement)) {
			throw new IllegalArgumentException("reduction element not part of exceptional model pattern description");
		}
		List<Proposition> newDescription = new ArrayList<Proposition>(getDescription());
		newDescription.remove(reductionElement);
		return new ExceptionalModelPattern(getDataTable(), newDescription, this.getTargetAttributes(),
				this.modelFactory, this.modelDistanceFunction);
	}

	@Override
	public String toString() {
		return super.toString() + "EM" + this.globalModel.getAttributes();
	}

}
