package io.stratixops.engine.imgui.theme;

/**
 * Semantic icon names mapped to Lucide identifiers (design system section 1.6).
 *
 * <p>Icons are rasterized into the font atlas from a Lucide subset at the DPI
 * target size; widgets reference this enum, never a raw ligature/name. An icon
 * that is not in the map uses the {@link #PLACEHOLDER} ({@code circle}) and must
 * be raised against the design spec rather than guessed.
 */
public enum Icon {

    GAME_OBJECT("box"),
    TRANSFORM("move-3d"),
    SPRITE_RENDERER("image"),
    ANIMATOR("film"),
    CAMERA("video"),
    AUDIO_SOURCE("volume-2"),
    COLLIDER_2D("box-select"),
    BEHAVIOUR("braces"),
    PREFAB("package"),
    SCENE("layout-template"),
    ASSET_OBJECT("database"),

    FOLDER("folder"),
    FOLDER_OPEN("folder-open"),
    IMAGE_ASSET("image"),
    AUDIO_ASSET("music"),
    TEXT_ASSET("file-text"),
    SCENE_ASSET("layout-template"),
    MATERIAL("palette"),

    PANEL_HIERARCHY("list-tree"),
    PANEL_INSPECTOR("sliders-horizontal"),
    PANEL_PROJECT("folder-tree"),
    PANEL_CONSOLE("terminal"),
    PANEL_STATS("activity"),
    COMMAND_PALETTE("command"),

    CLOSE("x"),
    CHEVRON_RIGHT("chevron-right"),
    CHEVRON_DOWN("chevron-down"),
    SUBMENU("chevron-right"),
    BREADCRUMB_SEPARATOR("chevron-right"),
    DRAG_HANDLE("grip-vertical"),
    ADD("plus"),
    CREATE_CHILD("corner-down-right"),
    SEARCH("search"),

    PLAY("play"),
    PAUSE("pause"),
    STEP("skip-forward"),
    SAVE("save"),
    UNDO("undo-2"),
    REDO("redo-2"),
    DUPLICATE("copy"),
    DELETE("trash-2"),
    RENAME("pencil"),
    SETTINGS("settings"),

    CONSOLE_INFO("info"),
    CONSOLE_WARN("triangle-alert"),
    CONSOLE_ERROR("circle-x"),
    CONSOLE_SUCCESS("circle-check"),

    VISIBILITY_ON("eye"),
    VISIBILITY_OFF("eye-off"),
    LOCK_ON("lock"),
    LOCK_OFF("unlock"),

    GIZMO_MOVE("move"),
    GIZMO_ROTATE("rotate-cw"),
    GIZMO_SCALE("maximize"),
    SNAP("magnet"),
    PIVOT("locate-fixed"),
    ZOOM_FIT("scan"),
    REFRESH("refresh-cw"),
    REVERT("rotate-ccw"),
    EYEDROPPER("pipette"),
    MORE("ellipsis"),
    LINK("link"),
    RECENTS("history"),

    /** Fallback for an unmapped icon; file a note instead of picking a random glyph. */
    PLACEHOLDER("circle");

    private final String lucide;

    Icon(String lucide) {
        this.lucide = lucide;
    }

    /** @return the Lucide identifier for this icon. */
    public String lucide() {
        return lucide;
    }

    @Override
    public String toString() {
        return lucide;
    }
}
