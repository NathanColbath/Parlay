package io.stratixops.engine.serialize;

/**
 * Schema identifiers and the current on-disk format version for the versioned
 * JSON envelope (ADR-0006).
 */
public final class Schemas {

    /** Scene envelope schema id. */
    public static final String SCENE = "stratix.scene";
    /** Prefab envelope schema id. */
    public static final String PREFAB = "stratix.prefab";
    /** Window settings envelope schema id. */
    public static final String SETTINGS_WINDOW = "stratix.settings.window";
    /** Input settings envelope schema id. */
    public static final String SETTINGS_INPUT = "stratix.settings.input";
    /** Tags &amp; layers settings envelope schema id. */
    public static final String SETTINGS_TAGS_LAYERS = "stratix.settings.tagsLayers";

    /** The format version this build writes and can read. */
    public static final int CURRENT_VERSION = 1;

    private Schemas() {
    }
}
