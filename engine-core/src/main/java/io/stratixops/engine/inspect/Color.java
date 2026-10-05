package io.stratixops.engine.inspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an {@code int} field as a packed {@code 0xRRGGBBAA} color, so the
 * Inspector draws a swatch and the serializer writes a {@code "#rrggbbaa"}
 * string (ADR-0007).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Color {
}
