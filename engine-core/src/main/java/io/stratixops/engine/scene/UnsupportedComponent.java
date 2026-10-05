package io.stratixops.engine.scene;

import java.util.Objects;

/**
 * Placeholder for a component whose {@code typeId} is unknown to this build
 * (ADR-0006). It preserves the original component JSON verbatim so a save
 * round-trips losslessly even though the type cannot be interpreted.
 *
 * <p>The editor shows it as a warning in the Inspector; the loader never
 * crashes on unknown content.
 */
public final class UnsupportedComponent extends Component {

    private final String typeId;
    private final String rawJson;

    /**
     * Creates a placeholder.
     *
     * @param typeId  the unknown serialized type id
     * @param rawJson the original component object as JSON text
     */
    public UnsupportedComponent(String typeId, String rawJson) {
        this.typeId = Objects.requireNonNull(typeId, "typeId");
        this.rawJson = Objects.requireNonNull(rawJson, "rawJson");
    }

    /**
     * @return the unknown serialized type id
     */
    public String typeId() {
        return typeId;
    }

    /**
     * @return the preserved raw component JSON
     */
    public String rawJson() {
        return rawJson;
    }
}
