package de.fraunhofer.iais.ocm.core.model.pattern.emm;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activity.InvalidActivityException;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.CategoricalAttribute;
import de.fraunhofer.iais.ocm.core.model.data.NumericAttribute;
import de.fraunhofer.iais.ocm.core.model.pattern.emm.ContingencyTable.KeyComputer;


public class ContingencyTableModel extends ProbabilisticModel {

	private static Map<List<Attribute>, ContingencyTable> cache = newHashMap();

	private ContingencyTable contingencyTable;

	public ContingencyTableModel(List<Attribute> attributes)
			throws InvalidActivityException {
		super(attributes);
		contingencyTable = cache.get(attributes);
		if (contingencyTable == null) {
			contingencyTable = computeTable();
			cache.put(attributes, contingencyTable);
		}
	}

	public ContingencyTableModel(List<Attribute> attributes, Set<Integer> rows)
			throws InvalidActivityException {
		super(attributes, rows);
		contingencyTable = computeTableForRows(rows);
	}

	private ContingencyTable computeTableForRows(Collection<Integer> rows)
			throws InvalidActivityException {
		List<ContingencyTable.KeyComputer> keyComputers = getKeyComputers();
		ContingencyTable table = new ContingencyTable(keyComputers);
		for (int row : rows) {
			List<String> key = computeKey(row, keyComputers);
			ContingencyTableCellKey cellKey = new ContingencyTableCellKey(key);
			table.incrementValue(cellKey);
		}

		return table;
	}

	private List<String> computeKey(int row, List<KeyComputer> keyComputers) {
		List<String> key = newArrayListWithCapacity(attributes.size());

		Iterator<ContingencyTable.KeyComputer> itKC = keyComputers.iterator();
		Iterator<Attribute> itA = attributes.iterator();

		while (itKC.hasNext()) {
			key.add(itKC.next().computeKey(itA.next().getValues().get(row)));
		}

		return key;
	}

	private ContingencyTable computeTable() throws InvalidActivityException {
		int size = attributes.get(0).getDataTable().getSize();
		Set<Integer> set = newHashSetWithExpectedSize(size);
		for (int i = 0; i < size; i++) {
			set.add(i);
		}
		return computeTableForRows(set);
	}

	private List<ContingencyTable.KeyComputer> getKeyComputers() {
		List<ContingencyTable.KeyComputer> keyComputers = newArrayListWithCapacity(attributes
				.size());
		for (Attribute attribute : attributes) {
			keyComputers.add(getKeyComputer(attribute));
		}
		return keyComputers;
	}

	private ContingencyTable.KeyComputer getKeyComputer(Attribute attribute) {
		if (attribute.isCategoric()) {
			return new CategoricalKeyComputer((CategoricalAttribute) attribute);
		}
			return new NumericalKeyComputer((NumericAttribute) attribute);
	}

	public ContingencyTable getProbabilities() {
		return contingencyTable;
	}
}