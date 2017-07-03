package de.fraunhofer.iais.ocm.core.model.pattern.emm;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activity.InvalidActivityException;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.CategoricalAttribute;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.data.NumericAttribute;


public class ContingencyTableModelBak extends ProbabilisticModel {

	private static Map<List<Attribute>, ContingencyTable> cache = newHashMap();

	private ContingencyTable contingencyTable;

	public ContingencyTableModelBak(List<Attribute> attributes)
			throws InvalidActivityException {
		super(attributes);
		contingencyTable = cache.get(attributes);
		if (contingencyTable == null) {
			contingencyTable = computeTable2();
			cache.put(attributes, contingencyTable);
		}
	}

	public ContingencyTableModelBak(List<Attribute> attributes, Set<Integer> rows)
			throws InvalidActivityException {
		super(attributes, rows);
		contingencyTable = computeTableForRows2();
	}

	private ContingencyTable computeTable2() throws InvalidActivityException {
		return computeTableForRows2(attributes.get(0).getDataTable()
				.getDataTable());
	}

	private ContingencyTable computeTableForRows2()
			throws InvalidActivityException {
		DataTable dataTable = attributes.get(0).getDataTable();
		List<List<String>> rows = newArrayListWithExpectedSize(getRows().size());
		for (int rowIx : getRows()) {
			rows.add(dataTable.getRow(rowIx));
		}
		return computeTableForRows2(rows);
	}

	private ContingencyTable computeTableForRows2(List<List<String>> rows)
			throws InvalidActivityException {
		List<ContingencyTable.KeyComputer> keyComputers = getKeyComputers();
		List<Integer> attributeIndicesInTable = getAttributeIndicesInTable();
		ContingencyTable table = new ContingencyTable(keyComputers);

		for (List<String> row : rows) {
			List<String> key = computeKey(row, keyComputers,
					attributeIndicesInTable);
			ContingencyTableCellKey cellKey = new ContingencyTableCellKey(key);
			table.incrementValue(cellKey);
		}

		return table;
	}

	private List<String> computeKey(List<String> row,
			List<ContingencyTable.KeyComputer> keyComputers,
			List<Integer> attributeIndicesInTable) {
		List<String> key = newArrayListWithCapacity(attributes.size());

		Iterator<ContingencyTable.KeyComputer> itKC = keyComputers.iterator();
		Iterator<Integer> itAIIT = attributeIndicesInTable.iterator();

		while (itKC.hasNext()) {
			key.add(itKC.next().computeKey(row.get(itAIIT.next())));
		}

		return key;
	}

	private List<Integer> getAttributeIndicesInTable() {
		List<Integer> indices = newArrayListWithCapacity(attributes.size());
		for (Attribute attribute : attributes) {
			indices.add(attribute.getIndexInTable());
		}
		return indices;
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