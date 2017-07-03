package de.fraunhofer.iais.ocm.common.parameter.rangebounder;

import de.fraunhofer.iais.ocm.core.model.data.DataTable;

/**
 * Defines all intentional RangeBounders.
 * NOTE: If you add a new RangeBounder here, be sure to add it
 * as well in the file reference-new.js
 * @link /WebContent/client/js/reference-new.js
 */
public enum IntentionalRangeBounder implements RangeBounder {
    POSITIVE_INTEGER {
        @Override
        public boolean isInRange(DataTable dataTable, Object object) {
            return parseInt(object) > 0;
        }
    },

    ALL_INTEGER {
        @Override
        public boolean isInRange(DataTable dataTable, Object object) throws IllegalArgumentException {
            parseInt(object);
            return true;
        }
    };

    public abstract boolean isInRange(DataTable dataTable, Object object) throws IllegalArgumentException;

    public String getName() {
        return this.name();
    }

    int parseInt(Object o) throws IllegalArgumentException {
        try {
            return Integer.parseInt(o.toString());
        } catch(Exception e) {
            throw new IllegalArgumentException("Value is not a valid integer");
        }
    }
}
