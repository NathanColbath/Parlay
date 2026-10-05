package io.stratixops.engine.inspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Presentation hint: renders a section header before the field in the
 * Inspector (ADR-0007). No effect on serialization.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Header {

    /**
     * @return the header text
     */
    String value();
}
