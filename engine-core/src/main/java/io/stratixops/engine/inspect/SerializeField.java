package io.stratixops.engine.inspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Opts a field into serialization and the Inspector even when it is not
 * {@code public} (ADR-0007). Public non-transient fields are serialized
 * without it; {@link NonSerialized} opts a public field back out. Final fields
 * require it.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SerializeField {
}
