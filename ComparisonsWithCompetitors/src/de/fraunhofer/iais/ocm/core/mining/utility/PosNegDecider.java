package de.fraunhofer.iais.ocm.core.mining.utility;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Double.parseDouble;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.activity.InvalidActivityException;

import mime.tool.Utils;

import com.google.common.collect.Sets;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.CategoricalAttribute;
import de.fraunhofer.iais.ocm.core.model.data.NumericAttribute;
import de.fraunhofer.iais.ocm.core.model.pattern.emm.ContingencyTableCellKey;
import de.fraunhofer.iais.ocm.core.model.pattern.emm.ContingencyTableModel;

public interface PosNegDecider {

	public class SingleAttributePosNegDecider implements PosNegDecider {

		private Attribute attribute;
		private double thresholdValue;
		private String positiveValue;

		public SingleAttributePosNegDecider(Attribute attribute) {
			this.attribute = attribute;
			if (attribute.isNumeric()) {
				thresholdValue = ((NumericAttribute) attribute).getMedian();
			} else {
				positiveValue = ((CategoricalAttribute) attribute)
						.getCategories().get(0);
			}
		}

		public boolean isPos(int rowIx) {
			if (attribute.isNumeric()) {
				return parseDouble(attribute.getValues().get(rowIx)) > thresholdValue;
			}
			return attribute.getValues().get(rowIx).equals(positiveValue);
		}
	}

	public class MultipleAttributesPosNegDecider implements PosNegDecider {

		List<PosNegDecider> deciders;

		public MultipleAttributesPosNegDecider(List<Attribute> attributes) {
			deciders = newArrayListWithCapacity(attributes.size());
			for (Attribute attribute : attributes) {
				deciders.add(new SingleAttributePosNegDecider(attribute));
			}
		}

		public boolean isPos(int rowIx) {
			for (PosNegDecider decider : deciders) {
				if (!decider.isPos(rowIx)) {
					return false;
				}
			}
			return true;
		}
	}

	public class InverseOfDominantPosNegDecider implements PosNegDecider {

		public static boolean inverse = true;
		public static boolean useMedian = true;

		private List<Attribute> attributes;
		private List<List<String>> keys;
		private List<Boolean> isPos = newArrayList();

		public InverseOfDominantPosNegDecider(List<Attribute> attributes) {
			this.attributes = attributes;
			ContingencyTableModel ct;
			try {
				ct = new ContingencyTableModel(attributes);

				List<Set<String>> values = new ArrayList<Set<String>>();
				for (int i = attributes.size() - 1; i >= 0; i--) {
					values.add(newHashSet(attributes.get(i).getName()
							+ "_lower", attributes.get(i).getName() + "_upper"));
				}
				keys = newArrayList(Sets.cartesianProduct(values));

				for (List<String> key : keys) {
					if ((ct.getProbabilities().getNormalizedValue(
							new ContingencyTableCellKey(key)) >= 1. / keys
							.size() && inverse)
							|| (ct.getProbabilities().getNormalizedValue(
									new ContingencyTableCellKey(key)) < 1. / keys
									.size() && !inverse)) {
						isPos.add(false);
					} else {
						isPos.add(true);
					}
				}
				//System.out.println("isPos: " + isPos);
			} catch (InvalidActivityException e) {
				e.printStackTrace();
			}

		}

		public boolean isPos(int rowIx) {
			List<String> key = newArrayList();
			for (int i = attributes.size() - 1; i >= 0; i--) {
				double splitValue = useMedian ? ((NumericAttribute) attributes
						.get(i)).getMedian() : ((NumericAttribute) attributes
						.get(i)).getMean();
				if (Double
						.parseDouble(attributes.get(i).getValues().get(rowIx)) >= splitValue) {
					key.add(attributes.get(i).getName() + "_upper");
				} else {
					key.add(attributes.get(i).getName() + "_lower");
				}
			}
			return isPos.get(keys.indexOf(key));
		}
	}
	
	public class InverseProbabilityPosNegDecider implements PosNegDecider {

		public static boolean inverse = true;
		public static boolean useMedian = true;

		private List<Attribute> attributes;
		private List<List<String>> keys;
		private List<Boolean> isPos = newArrayList();

		public InverseProbabilityPosNegDecider(List<Attribute> attributes) {
			this.attributes = attributes;
			ContingencyTableModel ct;
			try {
				ct = new ContingencyTableModel(attributes);

				List<Set<String>> values = new ArrayList<Set<String>>();
				for (int i = attributes.size() - 1; i >= 0; i--) {
					values.add(newHashSet(attributes.get(i).getName()
							+ "_lower", attributes.get(i).getName() + "_upper"));
				}
				keys = newArrayList(Sets.cartesianProduct(values));

				getPositives(ct);
				//System.out.println("isPos: " + isPos);
			} catch (InvalidActivityException e) {
				e.printStackTrace();
			}

		}

		private void getPositives(ContingencyTableModel ct) {
			// TODO solve infinities!
			Random random = new Random(System.currentTimeMillis());
			Set<Integer> toGo = newHashSet();
			for (int i = 0; i < keys.size(); i++) {
				if (ct.getProbabilities().getNormalizedValue(
						new ContingencyTableCellKey(keys.get(i))) > 0) {
					toGo.add(i);
				}
			}
			Set<Integer> positives = newHashSet();
			int numberOfPos = inverse ? toGo.size() / 2 : 1;
			while (positives.size() < numberOfPos) {
				double[] values = getSampleArray(toGo, ct);
				int ix = Utils.logIndexSearch(values, random.nextDouble()
						* values[values.length - 1]);
				Iterator<Integer> iterator = toGo.iterator();
				for (int j = 0; j < ix; j++) {
					iterator.next();
				}
				positives.add(iterator.next());
				iterator.remove();
			}
			for (int i = 0; i < keys.size(); i++) {
				if (positives.contains(i)) {
					isPos.add(true);
				} else {
					isPos.add(false);
				}
			}
		}

		private double[] getSampleArray(Set<Integer> toGo,
				ContingencyTableModel ct) {
			double[] array = new double[toGo.size()];
			int c = 0;
			double tot = 0;
			for(int i: toGo) {
				double prob;
				if (inverse) {
					prob = 1. / ct.getProbabilities().getNormalizedValue(
							new ContingencyTableCellKey(keys.get(i)));
				} else {
					prob = ct.getProbabilities().getNormalizedValue(
							new ContingencyTableCellKey(keys.get(i)));
				}
				array[c++] = (tot += prob);
			}
			return array;
		}

		public boolean isPos(int rowIx) {
			List<String> key = newArrayList();
			for (int i = attributes.size() - 1; i >= 0; i--) {
				double splitValue = useMedian? ((NumericAttribute) attributes
						.get(i)).getMedian() : ((NumericAttribute) attributes
						.get(i)).getMean();
				if (Double
						.parseDouble(attributes.get(i).getValues().get(rowIx)) >= splitValue) {
					key.add(attributes.get(i).getName() + "_upper");
				} else {
					key.add(attributes.get(i).getName() + "_lower");
				}
			}
			return isPos.get(keys.indexOf(key));
		}
	}

	public class RandomPosNegDecider implements PosNegDecider {

		private boolean useMedian = true;

		public void setUseMedian(boolean useMedian) {
			this.useMedian = useMedian;
		}

		private List<Attribute> attributes;
		private List<List<String>> keys;
		private List<Boolean> isPos = newArrayList();

		public RandomPosNegDecider(List<Attribute> attributes) {
			this.attributes = attributes;

			List<Set<String>> values = new ArrayList<Set<String>>();
			for (int i = attributes.size() - 1; i >= 0; i--) {
				values.add(newHashSet(attributes.get(i).getName() + "_lower",
						attributes.get(i).getName() + "_upper"));
			}
			keys = newArrayList(Sets.cartesianProduct(values));

			getPositives();
			System.out.println("isPos: " + isPos);
		}

		private void getPositives() {
			Random random = new Random(System.currentTimeMillis());
			Set<Integer> positives = newHashSet();
			while (positives.size() < keys.size() / 2) {
				positives.add(random.nextInt(keys.size()));
			}
			for (int i = 0; i < keys.size(); i++) {
				if (positives.contains(i)) {
					isPos.add(true);
				} else {
					isPos.add(false);
				}
			}
		}

		public boolean isPos(int rowIx) {
			List<String> key = newArrayList();
			for (int i = attributes.size() - 1; i >= 0; i--) {
				double splitValue = useMedian? ((NumericAttribute) attributes
						.get(i)).getMedian() : ((NumericAttribute) attributes
						.get(i)).getMean();
				if (Double
						.parseDouble(attributes.get(i).getValues().get(rowIx)) >= splitValue) {
					key.add(attributes.get(i).getName() + "_upper");
				} else {
					key.add(attributes.get(i).getName() + "_lower");
				}
			}
			return isPos.get(keys.indexOf(key));
		}
	}

	public class PCAPosNegDecider implements PosNegDecider {

		PCAEvaluator pcaEvaluator;
		private boolean isPos;

		public PCAPosNegDecider(List<Attribute> attributes) {
			pcaEvaluator = new PCAEvaluator(attributes);
			isPos = new Random(System.currentTimeMillis()).nextBoolean();
		}

		@Override
		public boolean isPos(int rowIx) {
			if (isPos) {
				return pcaEvaluator.getDevFirstDimension(rowIx) >= 0;
			}
			return pcaEvaluator.getDevFirstDimension(rowIx) < 0;
		}
	}

	public class PCAMULPosNegDecider implements PosNegDecider {

		PCAEvaluator pcaEvaluator;

		public PCAMULPosNegDecider(List<Attribute> attributes) {
			pcaEvaluator = new PCAEvaluator(attributes);
			System.out.println("PCAMUL: # "
					+ pcaEvaluator.getNumberOfComponents());
		}

		@Override
		public boolean isPos(int rowIx) {
			for (int i = 0; i < Math.max(1,
					pcaEvaluator.getNumberOfComponents() - 1); i++) {
				if (pcaEvaluator.getDevForDimension(rowIx, i) < 0) {
					return false;
				}
			}
			return true;
		}
	}

	public boolean isPos(int rowIx);
}