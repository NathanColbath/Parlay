package io.stratixops.engine.inspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Overrides a field's display/serialization position (ADR-0007). Lower values
 * come first; fields without an order keep declaration order after ordered
 * fields.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Order {

    /**
     * @return the sort key
     */
    int value();
}
