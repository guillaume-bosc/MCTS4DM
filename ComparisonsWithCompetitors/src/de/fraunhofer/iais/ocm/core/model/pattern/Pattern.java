package de.fraunhofer.iais.ocm.core.model.pattern;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.primitives.Ints;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.data.propositions.Proposition;

/**
 * User: paveltokmakov Date: 1/8/13
 */
public abstract class Pattern implements Serializable {

    public enum TYPE {emm, sgd, ass};

	private static final long serialVersionUID = 1L;

	private final long id;

	private final Description description;

	private final DataTable dataTable;

	private final Set<Integer> supportSet;

	private void calculateSupport() {
		if (getDescription().isEmpty()) {
			for (int i = 0; i < dataTable.getSize(); i++) {
				supportSet.add(i);
			}
			return;
		}

		List<Proposition> description2 = new ArrayList<Proposition>(
				getDescription());
		Collections.sort(description2, new Comparator<Proposition>() {
			@Override
			public int compare(Proposition o1, Proposition o2) {
				return Ints.compare(o1.getSupportSet().size(), o2
						.getSupportSet().size());
			}
		});
		Iterator<Proposition> iterator = description2.iterator();
		if (iterator.hasNext()) {
			supportSet.addAll(iterator.next().getSupportSet());
			while (iterator.hasNext()) {
				supportSet.retainAll(iterator.next().getSupportSet());
			}
		 }
	}

    public abstract TYPE getType();
	
	public Pattern(DataTable dataTable, List<Proposition> description) {
		this.dataTable = dataTable;
		id = PatternIdGenerator.INSTANCE.getNextId();

		this.description = new Description(description);
		
		supportSet = new HashSet<Integer>();
		calculateSupport();
	}

	public Pattern(Pattern oldPattern, Proposition augmentation) {
		this.dataTable = oldPattern.getDataTable();
		id = PatternIdGenerator.INSTANCE.getNextId();
		
		List<Proposition> newDescriptionElements = new ArrayList<Proposition>(
				oldPattern.getDescription());
		newDescriptionElements.add(augmentation);
		description = new Description(newDescriptionElements);
		
		supportSet = new HashSet<Integer>();
		supportSet.addAll(oldPattern.getSupportSet());
		supportSet.retainAll(augmentation.getSupportSet());
	}

	public double getFrequency() {
		return (double) supportSet.size() / (double) dataTable.getSize();
	}

	public boolean containsAttribute(Attribute attribute) {
		for (Proposition proposition : description.getElements()) {
			if (proposition.getAttribute() == attribute) {
				return true;
			}
		}

		return false;
	}

	public int getDescriptionSize() {
		return description.getElements().size();
	}

	public long getId() {
		return id;
	}

	public List<Proposition> getDescription() {
		return description.getElements();
	}

	public List<String> getTextDescriptionList() {
		List<String> descriptionList = new ArrayList<String>();
		for (Proposition proposition : description.getElements()) {
			descriptionList.add(proposition.toString() + " ");
		}

		return descriptionList;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Pattern that = (Pattern) o;

		if (!this.description.equals(that.description)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		result = description != null ? new HashSet<Proposition>(
				description.getElements()).hashCode() : 0;
		// temp = featureDescriptor.getFrequency() != +0.0d ?
		// Double.doubleToLongBits(featureDescriptor.getFrequency()) : 0L;
		temp = getFrequency() != +0.0d ? Double
				.doubleToLongBits(getFrequency()) : 0L;
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		// temp = featureDescriptor.getDeviation() != +0.0d ?
		// Double.doubleToLongBits(featureDescriptor.getDeviation()) : 0L;
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		// result = 31 * result + targetIndex;
		return result;
	}

	@Override
	public String toString() {
		return description.toString();
		// return String.valueOf(id);
	}

	public DataTable getDataTable() {
		return dataTable;
	}

	public Set<Integer> getSupportSet() {
		return supportSet;
	}

	public abstract Pattern generateSpecialization(Proposition augmentation);

	public abstract Pattern generateGeneralization(Proposition reductionElement);

}
