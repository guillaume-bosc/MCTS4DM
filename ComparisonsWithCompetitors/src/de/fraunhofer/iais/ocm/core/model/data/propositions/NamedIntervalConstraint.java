package de.fraunhofer.iais.ocm.core.model.data.propositions;

public class NamedIntervalConstraint implements Constraint {

	private double min;

	private double max;

	private String name;

	public NamedIntervalConstraint(double min, double max, String name) {
		this.min = min;
		this.max = max;
		this.name = name;
	}

	@Override
	public boolean holds(String value) {
		double doubleValue=Double.parseDouble(value);
		return (doubleValue>=min && doubleValue<=max);
	}
	
	@Override
	public String toString() {
		return "=" + getName();
	}
	
	public double getMin() {
		return min;
	}
	
	public double getMax() {
		return max;
	}
	
	public String getName() {
		return name+" ["+(float)min+","+(float)max+"]";
	}

}
