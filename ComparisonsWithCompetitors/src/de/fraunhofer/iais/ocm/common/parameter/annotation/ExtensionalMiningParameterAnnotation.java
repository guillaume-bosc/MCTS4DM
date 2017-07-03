package de.fraunhofer.iais.ocm.common.parameter.annotation;

import de.fraunhofer.iais.ocm.common.parameter.rangebounder.ExtensionalRangeBounder;
import de.fraunhofer.iais.ocm.core.mining.annotation.ParameterMultiplicity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: bjacobs
 * Date: 13.03.14
 * Time: 16:05
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExtensionalMiningParameterAnnotation {
    public String getName();

    public String getDescription();

    public ExtensionalRangeBounder getRangeBounder();

    public int getDisplayPosition();

    public ParameterMultiplicity getParameterMultiplicity();
}
