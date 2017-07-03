package de.fraunhofer.iais.ocm.core.model.pattern;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.data.propositions.Proposition;

public class Association extends Pattern {
	
	private static class AssociationComparator implements Comparator<Pattern> {

		@Override
		public int compare(Pattern assocation1, Pattern association2) {
	    	if (!(assocation1 instanceof Association) || !(association2 instanceof Association)) {
	    		throw new IllegalArgumentException("Can only compare associtions");
	    	}
	    	if (((Association)assocation1).getAssociationMeasure()<((Association)association2).getAssociationMeasure()) {
	    		return -1;
	    	} else if (((Association)assocation1).getAssociationMeasure()>((Association)association2).getAssociationMeasure()) {
	    		return 1;
	    	}
			return 0;
		}
		
		@Override
		public String toString() {
			return "lift_order";
		}
		
	}
	
	public static final AssociationComparator LIFT_COMPARATOR=new AssociationComparator();
	
	private double associationMeasure;
	private double productOfIndFreqs;

	public Association(DataTable dataTable, List<Proposition> description) {
		super(dataTable,description);
		populate();
	}
	
	public Association(Association oldPattern, Proposition augmentation) {
		super(oldPattern,augmentation);
		populate();
	}

    @Override
    public TYPE getType() {
        return TYPE.ass;
    }

    public double getAssociationMeasureSquared() {
		return associationMeasure * associationMeasure;
	}

	public double getAssociationMeasure() {
		return associationMeasure;
	}

	public void setAssociationMeasure(double associationMeasure) {
		this.associationMeasure = associationMeasure;
	}

	public double getProductOfIndFreqs() {
		return productOfIndFreqs;
	}

	public void setProductOfIndFreqs(double productOfIndFreqs) {
		this.productOfIndFreqs = productOfIndFreqs;
	}

	public List<Double> getIndividualFrequences() {
		List<Double> result = new ArrayList<Double>(this.getDescription().size());
		for (Proposition proposition : this.getDescription()) {
			result.add(proposition.getSupportSet().size()
					/ (double) getDataTable().getSize());
		}
		return result;
	}

	private void populate() {
		double patternFrequency = getFrequency();
		double denominator = Math.pow(2, getDescriptionSize() - 2.);

		double lift;
		double product = 1.;

		for (Proposition literal : getDescription()) {
			product *= literal.getSupportSet().size()
					/ (double) getDataTable().getSize();
		}

		lift = (patternFrequency - product) / denominator;

		setAssociationMeasure(lift);
		setProductOfIndFreqs(product);
	}

	@Override
	public Pattern generateSpecialization(Proposition augmentation) {
		return new Association(this,augmentation);
	}
	
	public Pattern generateGeneralization(Proposition reductionElement) {
		if (!getDescription().contains(reductionElement)) {
			throw new IllegalArgumentException("reduction element not part of association description");
		}
		List<Proposition> newDescription=new ArrayList<Proposition>(getDescription());
		newDescription.remove(reductionElement);
		return new Association(getDataTable(),newDescription);
	}

}
