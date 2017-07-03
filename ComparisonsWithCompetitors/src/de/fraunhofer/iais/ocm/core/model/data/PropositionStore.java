package de.fraunhofer.iais.ocm.core.model.data;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.ocm.core.model.data.propositions.EqualsConstraint;
import de.fraunhofer.iais.ocm.core.model.data.propositions.NamedIntervalConstraint;
import de.fraunhofer.iais.ocm.core.model.data.propositions.Proposition;

public class PropositionStore {

	public enum PropositionFactory {

		CATEGORIC_EQUALiTY {
			@Override
			public void constructPropositions(Attribute attribute,
					PropositionStore store) {
				if (attribute.isCategoric()) {
					for (String category : ((CategoricalAttribute) attribute)
							.getCategories()) {
						store.propositions.add(new Proposition(attribute,
								new EqualsConstraint(category),
								store.propositions.size()));
					}
				}
			}
		},

		// ABOVE_MEAN {
		// @Override
		// public void constructPropositions(Attribute attribute,
		// PropositionStore store) {
		// if (attribute.isNumeric()) {
		// NumericAttribute numericAttribute = (NumericAttribute) attribute;
		// store.propositions.add(new Proposition(numericAttribute,
		// new NamedIntervalConstraint(numericAttribute
		// .getMean(), numericAttribute.getMax(),
		// "upper half"), store.propositions.size()));
		// }
		// }
		// },
		//
		// BELOW_MEAN {
		// @Override
		// public void constructPropositions(Attribute attribute,
		// PropositionStore store) {
		// if (attribute.isNumeric()) {
		// NumericAttribute numericAttribute = (NumericAttribute) attribute;
		// store.propositions.add(new Proposition(numericAttribute,
		// new NamedIntervalConstraint(numericAttribute
		// .getMin(), numericAttribute.getMean(),
		// "lower half"), store.propositions.size()));
		// }
		// }
		// },

		ABOVE_MEDIAN {
			@Override
			public void constructPropositions(Attribute attribute,
					PropositionStore store) {
				if (attribute.isNumeric()) {
					NumericAttribute numericAttribute = (NumericAttribute) attribute;
					store.propositions.add(new Proposition(numericAttribute,
							new NamedIntervalConstraint(numericAttribute
									.getMedian(), numericAttribute.getMax(),
									"upper half"), store.propositions.size()));
				}
			}
		},

		BELOW_MEDIAN {
			@Override
			public void constructPropositions(Attribute attribute,
					PropositionStore store) {
				if (attribute.isNumeric()) {
					NumericAttribute numericAttribute = (NumericAttribute) attribute;
					store.propositions.add(new Proposition(numericAttribute,
							new NamedIntervalConstraint(numericAttribute
									.getMin(), numericAttribute.getMedian(),
									"lower half"), store.propositions.size()));
				}
			}
		},

		// FIRST_QUARTILE {
		// @Override
		// public void constructPropositions(Attribute attribute,
		// PropositionStore store) {
		// if (attribute.isNumeric()) {
		// NumericAttribute numericAttribute = (NumericAttribute) attribute;
		// store.propositions.add(new Proposition(numericAttribute,
		// new NamedIntervalConstraint(numericAttribute
		// .getMin(), numericAttribute
		// .getLowerQuartile(), "very low"),
		// store.propositions.size()));
		// }
		// }
		// },
		//
		// SECOND_QUARTILE {
		// @Override
		// public void constructPropositions(Attribute attribute,
		// PropositionStore store) {
		// if (attribute.isNumeric()) {
		// NumericAttribute numericAttribute = (NumericAttribute) attribute;
		// store.propositions.add(new Proposition(numericAttribute,
		// new NamedIntervalConstraint(numericAttribute
		// .getLowerQuartile(), numericAttribute
		// .getMedian(), "low"), store.propositions
		// .size()));
		// }
		// }
		// },
		//
		// THIRD_QUARTILE {
		// @Override
		// public void constructPropositions(Attribute attribute,
		// PropositionStore store) {
		// if (attribute.isNumeric()) {
		// NumericAttribute numericAttribute = (NumericAttribute) attribute;
		// store.propositions.add(new Proposition(numericAttribute,
		// new NamedIntervalConstraint(numericAttribute
		// .getMedian(), numericAttribute
		// .getUpperQuartile(), "high"),
		// store.propositions.size()));
		// }
		// }
		// },
		//
		// FOURTH_QUARTILE {
		// @Override
		// public void constructPropositions(Attribute attribute,
		// PropositionStore store) {
		// if (attribute.isNumeric()) {
		// NumericAttribute numericAttribute = (NumericAttribute) attribute;
		// store.propositions.add(new Proposition(numericAttribute,
		// new NamedIntervalConstraint(numericAttribute
		// .getUpperQuartile(), numericAttribute
		// .getMax(), "very high"), store.propositions
		// .size()));
		// }
		// }
		// },

//		POSITIVE_AND_NEGATIVE {
//			@Override
//			public void constructPropositions(Attribute attribute,
//					PropositionStore store) {
//				if (attribute.isNumeric()) {
//					NumericAttribute numericAttribute = (NumericAttribute) attribute;
//					if (numericAttribute.getMin() < 0) {
//						store.propositions.add(new Proposition(
//								numericAttribute, new LessThanConstraint(0),
//								store.propositions.size()));
//						store.propositions.add(new Proposition(
//								numericAttribute, new GreaterThanConstraint(0),
//								store.propositions.size()));
//					}
//				}
//			}
		// }
		;

		public abstract void constructPropositions(Attribute attribute,
				PropositionStore store);

	}

	private DataTable dataTable;

	private List<Proposition> propositions;

	public PropositionStore(DataTable dataTable) {
		this.dataTable = dataTable;
		this.propositions = new ArrayList<Proposition>();
		this.init();
	}

	public List<Proposition> getPropositions() {
		return propositions;
	}

	public List<Proposition> getPropositionsAbout(Attribute attribute) {
		List<Proposition> result = new ArrayList<Proposition>();
		for (Proposition prop : this.propositions) {
			if (prop.getAttribute() == attribute) {
				result.add(prop);
			}
		}
		return result;
	}

	private void init() {
		for (Attribute attribute : dataTable.getAttributes()) {
			if (attribute.isId()) {
				continue;
			}
			if (attribute.getName().contains("pdays")) {
				continue;
			}
			for (PropositionFactory propFactory : PropositionFactory.values()) {
				propFactory.constructPropositions(attribute, this);
			}
		}
	}
}
