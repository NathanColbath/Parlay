package io.stratixops.engine.editor;

/**
 * The fixed set of editor panels in the OOB Phase 1 layout (STR-23).
 */
public enum PanelKind {
    HIERARCHY("Hierarchy"),
    INSPECTOR("Inspector"),
    SCENE("Scene"),
    GAME("Game"),
    PROJECT("Project"),
    CONSOLE("Console");

    private final String title;

    PanelKind(String title) {
        this.title = title;
    }

    /**
     * @return the human-readable panel title
     */
    public String title() {
        return title;
    }
}
