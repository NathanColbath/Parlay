package io.stratixops.engine.inspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Presentation hint: hover help for a field in the Inspector (ADR-0007).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Tooltip {

    /**
     * @return the tooltip text
     */
    String value();
}
