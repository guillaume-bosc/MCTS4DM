package de.fraunhofer.iais.ocm.core.mining.utility;

import java.util.Collection;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;

public class PropositionFilter {

	public static boolean oneTargetIsPartOfMacroAttributeWith(
			Collection<Attribute> targets, Attribute attribute) {
		for (Attribute target : targets) {
			if (target.isPartOfMacroAttributeWith(attribute))
				return true;
		}
		return false;
	}

}
