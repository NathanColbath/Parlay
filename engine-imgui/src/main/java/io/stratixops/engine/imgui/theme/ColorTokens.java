// GENERATED from docs/design/tokens.json by the STR-22 theme generator. Do not edit by hand.
package io.stratixops.engine.imgui.theme;

/**
 * Semantic color tokens, one public field per token in `tokens.json`.
 * Field names are the SCREAMING_SNAKE form of the token path, e.g.
 * `bg.canvas` maps to `BG_CANVAS` and `state.error.subtle` maps to
 * `STATE_ERROR_SUBTLE`.
 *
 * <p>Values are packed `0xRRGGBBAA` ints so they line up with the
 * engine's public Renderer color convention.
 */
public final class ColorTokens {

    public final int BG_CANVAS;
    public final int BG_PANEL;
    public final int BG_TOOLBAR;
    public final int BG_ELEVATED;
    public final int BG_INPUT;
    public final int BG_OVERLAY;
    public final int SURFACE_HOVER;
    public final int SURFACE_ACTIVE;
    public final int SURFACE_SELECTED;
    public final int BORDER_SUBTLE;
    public final int BORDER_DEFAULT;
    public final int BORDER_STRONG;
    public final int BORDER_FOCUS;
    public final int TEXT_PRIMARY;
    public final int TEXT_SECONDARY;
    public final int TEXT_MUTED;
    public final int TEXT_DISABLED;
    public final int TEXT_ON_ACCENT;
    public final int TEXT_INVERSE;
    public final int ACCENT_DEFAULT;
    public final int ACCENT_HOVER;
    public final int ACCENT_ACTIVE;
    public final int ACCENT_SUBTLE;
    public final int ACCENT_TEXT;
    public final int STATE_SUCCESS;
    public final int STATE_SUCCESS_SUBTLE;
    public final int STATE_WARN;
    public final int STATE_WARN_SUBTLE;
    public final int STATE_ERROR;
    public final int STATE_ERROR_SUBTLE;
    public final int STATE_INFO;
    public final int STATE_INFO_SUBTLE;
    public final int AXIS_X;
    public final int AXIS_Y;
    public final int AXIS_Z;
    public final int GIZMO_MOVE;
    public final int GIZMO_ROTATE;
    public final int GIZMO_SCALE;
    public final int GRID_LINE;
    public final int GRID_LINE_MAJOR;
    public final int GUIDE;
    public final int PLACEHOLDER_MISSING;
    public final int PREFAB_OVERRIDE;
    public final int LINKING;

    ColorTokens(TokenDocument doc, String path) {
                BG_CANVAS = doc.color(path + "bg.canvas");
        BG_PANEL = doc.color(path + "bg.panel");
        BG_TOOLBAR = doc.color(path + "bg.toolbar");
        BG_ELEVATED = doc.color(path + "bg.elevated");
        BG_INPUT = doc.color(path + "bg.input");
        BG_OVERLAY = doc.color(path + "bg.overlay");
        SURFACE_HOVER = doc.color(path + "surface.hover");
        SURFACE_ACTIVE = doc.color(path + "surface.active");
        SURFACE_SELECTED = doc.color(path + "surface.selected");
        BORDER_SUBTLE = doc.color(path + "border.subtle");
        BORDER_DEFAULT = doc.color(path + "border.default");
        BORDER_STRONG = doc.color(path + "border.strong");
        BORDER_FOCUS = doc.color(path + "border.focus");
        TEXT_PRIMARY = doc.color(path + "text.primary");
        TEXT_SECONDARY = doc.color(path + "text.secondary");
        TEXT_MUTED = doc.color(path + "text.muted");
        TEXT_DISABLED = doc.color(path + "text.disabled");
        TEXT_ON_ACCENT = doc.color(path + "text.onAccent");
        TEXT_INVERSE = doc.color(path + "text.inverse");
        ACCENT_DEFAULT = doc.color(path + "accent.default");
        ACCENT_HOVER = doc.color(path + "accent.hover");
        ACCENT_ACTIVE = doc.color(path + "accent.active");
        ACCENT_SUBTLE = doc.color(path + "accent.subtle");
        ACCENT_TEXT = doc.color(path + "accent.text");
        STATE_SUCCESS = doc.color(path + "state.success");
        STATE_SUCCESS_SUBTLE = doc.color(path + "state.success.subtle");
        STATE_WARN = doc.color(path + "state.warn");
        STATE_WARN_SUBTLE = doc.color(path + "state.warn.subtle");
        STATE_ERROR = doc.color(path + "state.error");
        STATE_ERROR_SUBTLE = doc.color(path + "state.error.subtle");
        STATE_INFO = doc.color(path + "state.info");
        STATE_INFO_SUBTLE = doc.color(path + "state.info.subtle");
        AXIS_X = doc.color(path + "axis.x");
        AXIS_Y = doc.color(path + "axis.y");
        AXIS_Z = doc.color(path + "axis.z");
        GIZMO_MOVE = doc.color(path + "gizmo.move");
        GIZMO_ROTATE = doc.color(path + "gizmo.rotate");
        GIZMO_SCALE = doc.color(path + "gizmo.scale");
        GRID_LINE = doc.color(path + "grid.line");
        GRID_LINE_MAJOR = doc.color(path + "grid.line.major");
        GUIDE = doc.color(path + "guide");
        PLACEHOLDER_MISSING = doc.color(path + "placeholder.missing");
        PREFAB_OVERRIDE = doc.color(path + "prefab.override");
        LINKING = doc.color(path + "linking");
    }

    /** @return the token paths (without theme mode) covered by this group. */
    public static String[] paths() {
        return new String[] { "bg.canvas", "bg.panel", "bg.toolbar", "bg.elevated", "bg.input", "bg.overlay", "surface.hover", "surface.active", "surface.selected", "border.subtle", "border.default", "border.strong", "border.focus", "text.primary", "text.secondary", "text.muted", "text.disabled", "text.onAccent", "text.inverse", "accent.default", "accent.hover", "accent.active", "accent.subtle", "accent.text", "state.success", "state.success.subtle", "state.warn", "state.warn.subtle", "state.error", "state.error.subtle", "state.info", "state.info.subtle", "axis.x", "axis.y", "axis.z", "gizmo.move", "gizmo.rotate", "gizmo.scale", "grid.line", "grid.line.major", "guide", "placeholder.missing", "prefab.override", "linking" };
    }
}
