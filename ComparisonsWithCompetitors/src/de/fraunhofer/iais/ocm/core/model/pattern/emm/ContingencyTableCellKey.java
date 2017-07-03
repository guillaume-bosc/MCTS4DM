package de.fraunhofer.iais.ocm.core.model.pattern.emm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * the output of Cartesian product of set of string 
 */
public class ContingencyTableCellKey {
	private List<String> key;

	public ContingencyTableCellKey(List<String> key) {
		/*
		 * we have to sort so because we want to treat (a_v1, b_v2) same as
		 * (b_v2, a_v1)
		 */
		this.key = new ArrayList<String>(key);
		Collections.sort(this.key);
	}

	@Override
	public int hashCode() {
		String hashStr = "";
		for (String keyElement : key) {
			hashStr += keyElement;
		}
		return hashStr.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		return this.hashCode() == other.hashCode();
	}

	public String toString() {
		return key.toString();
	}
}
