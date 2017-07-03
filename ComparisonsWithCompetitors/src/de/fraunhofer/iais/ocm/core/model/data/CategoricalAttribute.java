package de.fraunhofer.iais.ocm.core.model.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CategoricalAttribute extends Attribute {

	private final List<String> categories;

	private final List<Double> categoryFrequencies;

	public CategoricalAttribute(String name, String description,
			List<String> values, int indexInTable, DataTable dataTable) {
		super(name, false, description, values, indexInTable, dataTable);
		// numOfDataTableRows = values.size();
		categories = (new ArrayList<String>());
		categoryFrequencies = (new ArrayList<Double>());
		Map<String, Double> categoryToFrequency = new HashMap<String, Double>();
		for (String value : values) {
			if (categoryToFrequency.containsKey(value)) {
				double newCount = categoryToFrequency.get(value) + 1.0;
				categoryToFrequency.remove(value);
				categoryToFrequency.put(value, newCount);
			} else {
				categoryToFrequency.put(value, 1.0);
			}
		}
		for (String category : categoryToFrequency.keySet()) {
			getCategories().add(category);
			getCategoryFrequencies().add(
					categoryToFrequency.get(category) / getValues().size());
		}
	}

	public List<Double> getCategoryFrequencies() {
		return categoryFrequencies;
	}

	public List<String> getCategories() {
		return categories;
	}

	public List<Double> getCategoryFrequenciesOnRows(Set<Integer> rowSet) {
		Map<String, Double> categoryToFrequency = new HashMap<String, Double>();
		for (Integer row : rowSet) {
			String value = getValues().get(row);
			if (categoryToFrequency.containsKey(value)) {
				double newCount = categoryToFrequency.get(value) + 1.0;
				categoryToFrequency.remove(value);
				categoryToFrequency.put(value, newCount);
			} else {
				categoryToFrequency.put(value, 1.0);
			}
		}

		List<Double> result = new ArrayList<Double>(getCategories().size());
		for (int i = 0; i < getCategories().size(); i++) {
			if (categoryToFrequency.containsKey(getCategories().get(i))) {
				result.add(categoryToFrequency.get(getCategories().get(i))
						/ (double) rowSet.size());
			} else {
				result.add(0.0);
			}
		}

		return result;
	}

	@Override
	public String getStatistic() {
		String result = "";
		for (int i = 0; i < categories.size(); i++) {
			result += categories.get(i)
					+ ": "
					+ String.format(Locale.ENGLISH, "%.4f",
							categoryFrequencies.get(i)) + "\r\n";
		}

		return result;
	}

}
