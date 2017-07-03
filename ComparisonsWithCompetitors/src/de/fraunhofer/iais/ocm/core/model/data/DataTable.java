package de.fraunhofer.iais.ocm.core.model.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.fraunhofer.iais.ocm.core.model.data.propositions.Proposition;

public class DataTable implements Serializable {
	private static final long serialVersionUID = 1L;

	private int file_id;
	private String data_file_name = "";

    private PropositionStore propositionStore;
    private AttributeGroupsStore attributeGroupsStore;


	/**
	 * This also holds the actual data
	 */
	private List<Attribute> attributes;

	public DataTable() {
		this.attributes = new ArrayList<Attribute>();
		this.attributeGroupsStore = new AttributeGroupsStore();
	}

    public void initPropositionStore() {
        propositionStore = new PropositionStore(this);
        //System.out.println("Proposition Store Initialized:"); /// commented by mehdi
        //for (Proposition prop: propositionStore.getPropositions()) {
        	//System.out.println(prop);
        //}
    }

	public List<String> getHeaderWithoutID() {
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < attributes.size(); i++) {
			if (!isID(i)) {
				result.add(attributes.get(i).getName());
			}
		}
		return result;
	}

	public boolean isNumeric(int attributeIndex) {
		return attributes.get(attributeIndex).isNumeric();
	}

	public boolean isID(int attributeIndex) {
		return attributes.get(attributeIndex).isId();
	}

	public boolean isEmpty() {
		return this.attributes.size() == 0;
	}

	public int getNumOfNonIDAttrs() {
		int result = 0;
		for (int i = 0; i < attributes.size(); i++) {
			if (!isID(i))
				result++;
		}
		return result;
	}

	/**
	 * Returns the length of the longest row in the data table
	 * 
	 * @return length of the longest row in the data table
	 */
	public int getMaxColSize() {
		return attributes.size();
	}

	public String getValue(int objectIndex, int attributeIndex) {
		return attributes.get(attributeIndex).getValues().get(objectIndex);
	}

	public int getSize() {
		if (isEmpty())
			return 0;
		return attributes.get(0).getValues().size();
	}

	public String getData_file_name() {
		return data_file_name;
	}

	public List<String> getAttributeNames() {
		List<String> res = new ArrayList<String>();
		for (Attribute attribute : getAttributes()) {
			res.add(attribute.getName());
		}
		return res;
	}

	public void setData_file_name(String data_file_name) {
		this.data_file_name = data_file_name;
	}

	public List<List<String>> getDataTable() {
		List<List<String>> result = new ArrayList<List<String>>(this.getSize());
		for (int i = 0; i < this.getSize(); i++) {
			result.add(this.getRow(i));
		}
		return result;
	}

	public List<String> getRow(int index) {
		List<String> result = new ArrayList<String>(this.attributes.size());
		for (Attribute attribute : this.attributes) {
			result.add(attribute.getValues().get(index));
		}
		return result;
	}

	public int getFile_id() {
		return file_id;
	}

	public void setFile_id(int file_id) {
		this.file_id = file_id;
	}

	public List<String> getDescriptorList() {
		List<String> res = new ArrayList<String>(attributes.size());
		for (Attribute attribute : attributes) {
			res.add(attribute.getDescription());
		}
		return res;
	}

	public List<List<Double>> getTransactionFeatureList() {
		List<List<Double>> transFeatureList = new ArrayList<List<Double>>();
		for (int i = 1; i < getSize(); i++) {
			transFeatureList.add(createFeatureVectorFromTrans(getRow(i)));
		}
		return transFeatureList;
	}

	private List<Double> createFeatureVectorFromTrans(List<String> trans) {
		List<Double> transFeature = new ArrayList<Double>();
		List<List<String>> categoricalAttrValue = getCategoricalAttrValueList();
		for (int i = 0; i < trans.size(); i++) {
			if (attributes.get(i).isCategoric()) {
				for (List<String> values : categoricalAttrValue) {
					if (values.get(0).equals(String.valueOf(i))) {
						for (int j = 1; j < values.size(); j++) {
							if (trans.get(i).equals(values.get(j))) {
								transFeature.add(1.);
							} else {
								transFeature.add(0.);
							}
						}
						break;
					}
				}
			} else if (attributes.get(i).isNumeric()) {
				transFeature.add(Double.valueOf(trans.get(i).replace(",", ".")));
			}
		}
		return transFeature;
	}

	private List<List<String>> getCategoricalAttrValueList() {
		List<List<String>> attrValue = new ArrayList<List<String>>();
		for (int i = 0; i < attributes.size(); i++) {
			if (attributes.get(i).isCategoric()) {
				List<String> values = new ArrayList<String>();
				values.add(String.valueOf(i));
				for (int j = 1; j < getSize(); j++) {
					if (!values.contains(getValue(j, i))) {
						values.add(getValue(j, i));
					}
				}
				attrValue.add(values);
			}
		}
		return attrValue;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public Attribute getAttribute(int attributeIndex) {
		return attributes.get(attributeIndex);
	}

    public PropositionStore getPropositionStore() {
        return propositionStore;
    }

	public AttributeGroupsStore getAttributeGroupsStore() {
		return attributeGroupsStore;
	}

}
