package de.fraunhofer.iais.ocm.common.parameter.rangebounder;

import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines all extensional RangeBounders.
 * NOTE: If you add a new RangeBounder here, be sure to add it
 * as well in the file reference-new.js
 * @link /WebContent/client/js/reference-new.js
 */
public enum ExtensionalRangeBounder implements RangeBounder {
	
	UNBOUNDED {
		@Override
		public List<Object> getRange(DataTable dataTable) {
			return null;
		}
	},
	
	ONE_TO_NUMBER_OF_NON_ID_ATTRIBUTES {
		@Override
		public List<Object> getRange(DataTable dataTable) {
			List<Object> result=new ArrayList<Object>();
			for (int i=1; i<dataTable.getNumOfNonIDAttrs(); i++) {
				result.add(i);
			}
			return result;
		}
	},
	
	NON_ID_ATTRIBUTES {
		@Override
		public List<Object> getRange(DataTable dataTable) {
			List<Object> result=new ArrayList<Object>();
			for (Attribute attribute: dataTable.getAttributes()) {
				if (!attribute.isId()) {
					result.add(attribute);
				}
			}
			return result;
		}
	};

    public String getName() {
        return this.name();
    }
	
	public abstract List<Object> getRange(DataTable dataTable);
}
