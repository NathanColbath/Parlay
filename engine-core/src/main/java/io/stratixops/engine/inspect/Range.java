package io.stratixops.engine.inspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Presentation hint: constrains a numeric field to a slider in the Inspector
 * (ADR-0007). It is a UI hint, not a validated constraint.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Range {

    /**
     * @return the minimum value
     */
    float min();

    /**
     * @return the maximum value
     */
    float max();
}
