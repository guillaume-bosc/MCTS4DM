package de.fraunhofer.iais.ocm.common.parameter.annotation;

import de.fraunhofer.iais.ocm.common.parameter.rangebounder.IntentionalRangeBounder;
import de.fraunhofer.iais.ocm.core.mining.annotation.ParameterMultiplicity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: bjacobs
 * Date: 13.03.14
 * Time: 14:32
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IntentionalMiningParameterAnnotation {
    public String getName();

    public String getDescription();

    public IntentionalRangeBounder getRangeBounder();

    public int getDisplayPosition();

    public ParameterMultiplicity getParameterMultiplicity();
}
