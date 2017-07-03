package de.fraunhofer.iais.ocm.core.mining.patternsampling;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mime.plain.PlainTransactionDB;
import mime.plain.weighting.PosNegDbInterface;
import mime.plain.weighting.PosNegTransactionDb;
import de.fraunhofer.iais.ocm.core.mining.utility.PosNegDecider;
import de.fraunhofer.iais.ocm.core.mining.utility.PosNegDecider.MultipleAttributesPosNegDecider;
import de.fraunhofer.iais.ocm.core.mining.utility.PosNegDecider.PCAPosNegDecider;
import de.fraunhofer.iais.ocm.core.mining.utility.PosNegDecider.SingleAttributePosNegDecider;
import de.fraunhofer.iais.ocm.core.mining.utility.PropositionFilter;
import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.data.propositions.Proposition;

public class ConsaptUtils {

	public static PlainTransactionDB createTransactionDBfromDataTable(
			DataTable dataTable) {
		PlainTransactionDB transactionDB = new PlainTransactionDB();

		for (int i = 0; i < dataTable.getSize(); i++) {
			List<String> transactionList = new ArrayList<String>();
			for (Proposition proposition : dataTable.getPropositionStore()
					.getPropositions()) {
				if (proposition.holdsFor(i)) {
					transactionList.add(String.valueOf(proposition
							.getIndexInStore()));
				}
			}
			String[] transaction = new String[transactionList.size()];
			transactionList.toArray(transaction);
			transactionDB.addTransaction(transaction);
		}

		return transactionDB;
	}

	public static PosNegTransactionDb createPosNegDb(DataTable dataTable,
			Set<Attribute> targetAttributeSet, PosNegDecider decider) {
		PosNegTransactionDb db = new PosNegTransactionDb();
		for (int i = 0; i < dataTable.getSize(); i++) {
			List<String> transactionList = new ArrayList<String>();
			for (Proposition proposition : dataTable.getPropositionStore()
					.getPropositions()) {
				if (targetAttributeSet.contains(proposition.getAttribute())
					 || PropositionFilter
					 .oneTargetIsPartOfMacroAttributeWith(
					 targetAttributeSet,
					 proposition.getAttribute())) {
					continue;
				}
				if (proposition.holdsFor(i)) {
					transactionList.add(String.valueOf(proposition
							.getIndexInStore()));
				}
			}
			db.addTransaction(transactionList.toArray(new String[] {}),
					decider.isPos(i));
		}
		// System.out.println(db.getTransactionsPos().size()); // commented by mehdi
		/// System.out.println(db.getTransactionsNeg().size()); // commented by mehdi
		return db;
	}

	public static PosNegTransactionDb createPosNegTransactionDBByFirstAttribute(
			DataTable dataTable, List<Attribute> targetAttributes) {
		Set<Attribute> targetAttributeSet = newHashSet(targetAttributes.get(0));

		PosNegDecider decider = new SingleAttributePosNegDecider(
				targetAttributes.get(0));

		return createPosNegDb(dataTable, targetAttributeSet, decider);
	}

	public static PosNegDbInterface createPosNegTransactionDBByFirstTwoAttributes(
			DataTable dataTable, List<Attribute> targetAttributes) {
		Set<Attribute> targetAttributeSet = newHashSet(targetAttributes.get(0),
				targetAttributes.get(1));

		PosNegDecider decider = new MultipleAttributesPosNegDecider(
				newArrayList(targetAttributes.get(0), targetAttributes.get(1)));

		return createPosNegDb(dataTable, targetAttributeSet, decider);
	}
}
