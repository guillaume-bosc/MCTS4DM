package de.fraunhofer.iais.ocm.core.mining.annotation;

import de.fraunhofer.iais.ocm.core.mining.AlgorithmCategory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: bjacobs
 * Date: 11.03.14
 * Time: 15:19
 */

public interface AlgorithmDefinition {
    String getName();

    AlgorithmCategory getCategory();

    String getDescription();
}
