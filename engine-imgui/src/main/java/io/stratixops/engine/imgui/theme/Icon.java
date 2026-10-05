package io.stratixops.engine.imgui.theme;

/**
 * Semantic icon names mapped to Lucide identifiers (design system section 1.6).
 *
 * <p>Icons are rasterized into the font atlas from the vendored Lucide subset
 * ({@code fonts/lucide.ttf}) at the DPI target size; widgets reference this
 * enum, never a raw ligature/name. Each constant also carries the glyph's
 * Private Use Area codepoint from the pinned Lucide release, so the atlas can
 * reserve the exact icon ranges and text can be drawn without a lookup table.
 * An icon that is not in the map uses the {@link #PLACEHOLDER} ({@code circle})
 * and must be raised against the design spec rather than guessed.
 *
 * <p>Codepoints mirror Lucide {@code codepoints.json} and are verified against
 * the shipped {@code lucide.ttf} cmap by {@code FontResourcesTest}.
 */
public enum Icon {

    GAME_OBJECT("box", 0xE061),
    TRANSFORM("move-3d", 0xE2E5),
    SPRITE_RENDERER("image", 0xE0F6),
    ANIMATOR("film", 0xE0D0),
    CAMERA("video", 0xE1A5),
    AUDIO_SOURCE("volume-2", 0xE1AB),
    COLLIDER_2D("box-select", 0xE1CB),
    BEHAVIOUR("braces", 0xE36A),
    PREFAB("package", 0xE129),
    SCENE("layout-template", 0xE207),
    ASSET_OBJECT("database", 0xE0AD),

    FOLDER("folder", 0xE0D7),
    FOLDER_OPEN("folder-open", 0xE247),
    IMAGE_ASSET("image", 0xE0F6),
    AUDIO_ASSET("music", 0xE122),
    TEXT_ASSET("file-text", 0xE0CC),
    SCENE_ASSET("layout-template", 0xE207),
    MATERIAL("palette", 0xE1DD),

    PANEL_HIERARCHY("list-tree", 0xE408),
    PANEL_INSPECTOR("sliders-horizontal", 0xE29A),
    PANEL_PROJECT("folder-tree", 0xE33C),
    PANEL_CONSOLE("terminal", 0xE181),
    PANEL_STATS("activity", 0xE038),
    COMMAND_PALETTE("command", 0xE09A),

    CLOSE("x", 0xE1B2),
    CHEVRON_RIGHT("chevron-right", 0xE06F),
    CHEVRON_DOWN("chevron-down", 0xE06D),
    SUBMENU("chevron-right", 0xE06F),
    BREADCRUMB_SEPARATOR("chevron-right", 0xE06F),
    DRAG_HANDLE("grip-vertical", 0xE0EB),
    ADD("plus", 0xE13D),
    CREATE_CHILD("corner-down-right", 0xE0A2),
    SEARCH("search", 0xE151),

    PLAY("play", 0xE13C),
    PAUSE("pause", 0xE12E),
    STEP("skip-forward", 0xE160),
    SAVE("save", 0xE14D),
    UNDO("undo-2", 0xE2A1),
    REDO("redo-2", 0xE2A0),
    DUPLICATE("copy", 0xE09E),
    DELETE("trash-2", 0xE18E),
    RENAME("pencil", 0xE1F9),
    SETTINGS("settings", 0xE154),

    CONSOLE_INFO("info", 0xE0F9),
    CONSOLE_WARN("triangle-alert", 0xE193),
    CONSOLE_ERROR("circle-x", 0xE084),
    CONSOLE_SUCCESS("circle-check", 0xE226),

    VISIBILITY_ON("eye", 0xE0BA),
    VISIBILITY_OFF("eye-off", 0xE0BB),
    LOCK_ON("lock", 0xE10B),
    LOCK_OFF("unlock", 0xE10C),

    GIZMO_MOVE("move", 0xE121),
    GIZMO_ROTATE("rotate-cw", 0xE149),
    GIZMO_SCALE("maximize", 0xE112),
    SNAP("magnet", 0xE2B5),
    PIVOT("locate-fixed", 0xE1DB),
    ZOOM_FIT("scan", 0xE257),
    REFRESH("refresh-cw", 0xE145),
    REVERT("rotate-ccw", 0xE148),
    EYEDROPPER("pipette", 0xE13B),
    MORE("ellipsis", 0xE0B6),
    LINK("link", 0xE102),
    RECENTS("history", 0xE1F5),

    /** Fallback for an unmapped icon; file a note instead of picking a random glyph. */
    PLACEHOLDER("circle", 0xE076);

    private final String lucide;
    private final int codepoint;

    Icon(String lucide, int codepoint) {
        this.lucide = lucide;
        this.codepoint = codepoint;
    }

    /** @return the Lucide identifier for this icon. */
    public String lucide() {
        return lucide;
    }

    /** @return the Lucide Private Use Area codepoint of the glyph. */
    public int codepoint() {
        return codepoint;
    }

    /**
     * @return whether {@code codepoint} is one of the icon glyphs reserved by
     *         this enum. Used to scope the icon range registered in the atlas.
     */
    public static boolean isIconCodepoint(int codepoint) {
        for (Icon icon : values()) {
            if (icon.codepoint == codepoint) {
                return true;
            }
        }
        return false;
    }

    /** @return every distinct icon codepoint, in declaration order. */
    public static int[] allCodepoints() {
        int[] raw = new int[values().length];
        int count = 0;
        for (Icon icon : values()) {
            boolean seen = false;
            for (int i = 0; i < count; i++) {
                if (raw[i] == icon.codepoint) {
                    seen = true;
                    break;
                }
            }
            if (!seen) {
                raw[count++] = icon.codepoint;
            }
        }
        int[] unique = new int[count];
        System.arraycopy(raw, 0, unique, 0, count);
        return unique;
    }

    @Override
    public String toString() {
        return lucide;
    }
}
