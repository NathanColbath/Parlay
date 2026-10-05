package io.stratixops.engine.inspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excludes a field from serialization and the Inspector even when it is
 * {@code public} (ADR-0007). Use for derived/cached runtime state.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NonSerialized {
}
