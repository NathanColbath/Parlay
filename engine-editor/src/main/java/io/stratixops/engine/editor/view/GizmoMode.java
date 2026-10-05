package io.stratixops.engine.editor.view;

/**
 * The active transform gizmo (STR-41).
 */
public enum GizmoMode {

    /** Translate the selection. */
    MOVE("Move", "W"),
    /** Rotate the selection. */
    ROTATE("Rotate", "E"),
    /** Scale the selection. */
    SCALE("Scale", "R");

    private final String label;
    private final String key;

    GizmoMode(String label, String key) {
        this.label = label;
        this.key = key;
    }

    /** @return the display label */
    public String label() {
        return label;
    }

    /** @return the single-key shortcut hint */
    public String key() {
        return key;
    }
}
