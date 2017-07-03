package de.fraunhofer.iais.ocm.core.model.pattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.fraunhofer.iais.ocm.core.model.data.propositions.Proposition;

public class Description {

	private static class CanonicalOrder implements Comparator<Proposition> {

		@Override
		public int compare(Proposition o1, Proposition o2) {
			return Integer.compare(o1.getIndexInStore(), o2.getIndexInStore());
		}

	}

	public Comparator<Proposition> PROPOSITION_ORDER = new CanonicalOrder();

	private final List<Proposition> elements;

	public Description(Collection<Proposition> elements) {
		List<Proposition> orderedElements = new ArrayList<Proposition>(elements);
		Collections.sort(orderedElements, PROPOSITION_ORDER);
		this.elements = Collections.unmodifiableList(orderedElements);
	}

	public List<Proposition> getElements() {
		return this.elements;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Description))
			return false;

		Description other = (Description) o;

		if (other.getElements().size() != this.getElements().size()) {
			return false;
		}

		for (int i = 0; i < this.getElements().size(); i++) {
			if (this.getElements().get(i) != other.getElements().get(i)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return elements.toString();
	}
}
