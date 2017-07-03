package de.fraunhofer.iais.ocm.core.model.pattern.emm;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Maps.newHashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.activity.InvalidActivityException;

import com.google.common.collect.Sets;

public class ContingencyTable {

	public static interface KeyComputer {
		public Set<String> getDistinctKeys();

		public String computeKey(String value);
	}

	private int normalizationFactor;
	private Map<ContingencyTableCellKey, AtomicInteger> table;

	public ContingencyTable(List<KeyComputer> keyComputers)
			throws InvalidActivityException {
		table = newHashMap();
		normalizationFactor = 0;
		initializeCompoundKeys(keyComputers);
	}

	private void initializeCompoundKeys(List<KeyComputer> keyComputers)
			throws InvalidActivityException {
		List<Set<String>> v = newArrayListWithCapacity(keyComputers.size());
		for (KeyComputer keyComputer : keyComputers) {
			v.add(keyComputer.getDistinctKeys());
		}
		for (List<String> key : Sets.cartesianProduct(v)) {
			ContingencyTableCellKey cellKey = new ContingencyTableCellKey(key);
			table.put(cellKey, new AtomicInteger(0));
		}
	}

	public void incrementValue(ContingencyTableCellKey cellKey) {
		AtomicInteger value = table.get(cellKey);
		if (value == null) {
			value = new AtomicInteger();
			table.put(cellKey, value);
		}
		value.incrementAndGet();
		normalizationFactor++;
	}

	public Set<ContingencyTableCellKey> getKeys() {
		return table.keySet();
	}

	public double getNormalizedValue(ContingencyTableCellKey key) {
		AtomicInteger value = table.get(key);
		if (value == null) {
			return 0;
		}
		return 1.0 * value.get() / normalizationFactor;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder("ContingencyTable: [");
		for (ContingencyTableCellKey cellKey : getKeys()) {
			builder.append(cellKey + ": " + getNormalizedValue(cellKey) + ", ");
		}
		builder.setLength(builder.length() - 2);
		builder.append("]");
		return builder.toString();
	}
}
