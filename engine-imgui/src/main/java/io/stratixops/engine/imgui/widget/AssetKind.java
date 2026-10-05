package io.stratixops.engine.imgui.widget;

import io.stratixops.engine.imgui.theme.Icon;

/**
 * The asset categories surfaced by the Project panel filter chips and the list
 * view type column (design system section 3.4). The set is intentionally
 * domain-agnostic: it describes import kinds, not engine components
 * (ADR-0006/0007). {@link #FOLDER} is not a filter chip but is a first-class
 * grid/list element.
 */
public enum AssetKind {

    SCENE("Scene", Icon.SCENE_ASSET),
    PREFAB("Prefab", Icon.PREFAB),
    SCRIPT("Script", Icon.BEHAVIOUR),
    IMAGE("Image", Icon.IMAGE_ASSET),
    AUDIO("Audio", Icon.AUDIO_ASSET),
    FONT("Font", Icon.TEXT_ASSET),
    FOLDER("Folder", Icon.FOLDER);

    private final String label;
    private final Icon icon;

    AssetKind(String label, Icon icon) {
        this.label = label;
        this.icon = icon;
    }

    /** @return the human label used in the type column and chips. */
    public String label() {
        return label;
    }

    /** @return the semantic icon for this kind. */
    public Icon icon() {
        return icon;
    }

    /** @return whether this kind is a container rather than a leaf asset. */
    public boolean isContainer() {
        return this == FOLDER;
    }
}
