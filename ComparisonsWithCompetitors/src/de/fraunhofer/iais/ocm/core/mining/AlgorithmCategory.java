package de.fraunhofer.iais.ocm.core.mining;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * User: bjacobs
 * Date: 11.03.14
 * Time: 15:00
 */

public enum AlgorithmCategory {
    ASSOCIATION_MINING,
    EM_MINING;

    @JsonIgnore
    public static String getFullName(AlgorithmCategory c) {
        if (c == ASSOCIATION_MINING) return "Association Pattern Mining";
        else return "Exceptional Model Mining";
    }
}
