package io.stratixops.engine.imgui.glue;

import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import io.stratixops.engine.imgui.theme.ColorTokens;
import io.stratixops.engine.imgui.theme.Rgba;
import io.stratixops.engine.imgui.theme.Theme;

/**
 * Maps a {@link Theme} onto an {@link ImGuiStyle}, one token to one ImGui
 * enum (design system section 2.1). The reverse mapping — panel header
 * underline, selection bar, focus ring, drag ghost, elevation shadows — is the
 * custom draw layer in {@code Ui}, because ImGui has no color slot for them.
 *
 * <p>GL/ImGui-native glue: excluded from the headless coverage gate.
 */
public final class ImGuiStyleMapper {

    // Colors the design system renders through the custom draw layer, not ImGui.
    private static final int TRANSPARENT = 0x00000000;

    private ImGuiStyleMapper() {
    }

    /** Applies the dark/light semantic tokens and the size style-vars to ImGui. */
    public static void apply(Theme theme, ImGuiStyle style) {
        applyColors(theme.color, style);
        applySizes(theme, style);
    }

    private static void applyColors(ColorTokens c, ImGuiStyle style) {
        set(style, ImGuiCol.Text, c.TEXT_PRIMARY);
        set(style, ImGuiCol.TextDisabled, c.TEXT_DISABLED);
        set(style, ImGuiCol.WindowBg, c.BG_PANEL);
        set(style, ImGuiCol.ChildBg, TRANSPARENT);
        set(style, ImGuiCol.PopupBg, c.BG_ELEVATED);
        set(style, ImGuiCol.Border, c.BORDER_DEFAULT);
        set(style, ImGuiCol.BorderShadow, TRANSPARENT);
        set(style, ImGuiCol.FrameBg, c.BG_INPUT);
        set(style, ImGuiCol.FrameBgHovered, c.SURFACE_HOVER);
        set(style, ImGuiCol.FrameBgActive, c.SURFACE_ACTIVE);
        set(style, ImGuiCol.TitleBg, c.BG_TOOLBAR);
        set(style, ImGuiCol.TitleBgActive, c.BG_PANEL);
        set(style, ImGuiCol.TitleBgCollapsed, c.BG_TOOLBAR);
        set(style, ImGuiCol.MenuBarBg, c.BG_TOOLBAR);
        set(style, ImGuiCol.ScrollbarBg, TRANSPARENT);
        set(style, ImGuiCol.ScrollbarGrab, c.BORDER_STRONG);
        set(style, ImGuiCol.ScrollbarGrabHovered, c.TEXT_MUTED);
        set(style, ImGuiCol.ScrollbarGrabActive, c.ACCENT_DEFAULT);
        set(style, ImGuiCol.CheckMark, c.ACCENT_TEXT);
        set(style, ImGuiCol.SliderGrab, c.ACCENT_DEFAULT);
        set(style, ImGuiCol.SliderGrabActive, c.ACCENT_HOVER);
        set(style, ImGuiCol.Button, c.BG_ELEVATED);
        set(style, ImGuiCol.ButtonHovered, c.SURFACE_HOVER);
        set(style, ImGuiCol.ButtonActive, c.SURFACE_ACTIVE);
        set(style, ImGuiCol.Header, c.SURFACE_SELECTED);
        set(style, ImGuiCol.HeaderHovered, c.SURFACE_HOVER);
        set(style, ImGuiCol.HeaderActive, c.SURFACE_SELECTED);
        set(style, ImGuiCol.Separator, c.BORDER_SUBTLE);
        set(style, ImGuiCol.SeparatorHovered, c.ACCENT_DEFAULT);
        set(style, ImGuiCol.SeparatorActive, c.ACCENT_HOVER);
        set(style, ImGuiCol.ResizeGrip, TRANSPARENT);
        set(style, ImGuiCol.ResizeGripHovered, withAlpha(c.ACCENT_DEFAULT, 0.5f));
        set(style, ImGuiCol.ResizeGripActive, c.ACCENT_DEFAULT);
        set(style, ImGuiCol.Tab, c.BG_PANEL);
        set(style, ImGuiCol.TabHovered, c.SURFACE_HOVER);
        set(style, ImGuiCol.TabSelected, c.BG_ELEVATED);
        set(style, ImGuiCol.TabSelectedOverline, c.ACCENT_DEFAULT);
        set(style, ImGuiCol.TabDimmed, c.BG_PANEL);
        set(style, ImGuiCol.TabDimmedSelected, c.BG_ELEVATED);
        set(style, ImGuiCol.PlotLines, c.ACCENT_DEFAULT);
        set(style, ImGuiCol.PlotLinesHovered, c.ACCENT_HOVER);
        set(style, ImGuiCol.PlotHistogram, c.ACCENT_DEFAULT);
        set(style, ImGuiCol.PlotHistogramHovered, c.ACCENT_HOVER);
        set(style, ImGuiCol.TableHeaderBg, c.BG_TOOLBAR);
        set(style, ImGuiCol.TableBorderStrong, c.BORDER_STRONG);
        set(style, ImGuiCol.TableBorderLight, c.BORDER_SUBTLE);
        set(style, ImGuiCol.TableRowBg, TRANSPARENT);
        set(style, ImGuiCol.TableRowBgAlt, withAlpha(c.SURFACE_HOVER, 0.5f));
        set(style, ImGuiCol.TextSelectedBg, c.SURFACE_SELECTED);
        set(style, ImGuiCol.DragDropTarget, c.ACCENT_DEFAULT);
        set(style, ImGuiCol.NavCursor, c.BORDER_FOCUS);
        set(style, ImGuiCol.NavWindowingHighlight, withAlpha(c.TEXT_PRIMARY, 0.7f));
        set(style, ImGuiCol.NavWindowingDimBg, c.BG_OVERLAY);
        set(style, ImGuiCol.ModalWindowDimBg, c.BG_OVERLAY);
        set(style, ImGuiCol.TextLink, c.ACCENT_TEXT);
    }

    private static void applySizes(Theme t, ImGuiStyle style) {
        style.setWindowPadding(t.space.SPACE_2, t.space.SPACE_1);
        style.setWindowBorderSize(1f);
        style.setWindowRounding(0f);
        style.setChildRounding(t.radius.RADIUS_SM);
        style.setChildBorderSize(1f);
        style.setPopupRounding(t.radius.RADIUS_MD);
        style.setPopupBorderSize(1f);
        style.setFramePadding(t.space.SPACE_1 + 1f, t.space.SPACE_1);
        style.setFrameRounding(t.radius.RADIUS_SM);
        style.setFrameBorderSize(1f);
        style.setItemSpacing(t.space.SPACE_2, t.space.SPACE_1);
        style.setItemInnerSpacing(t.space.SPACE_1, t.space.SPACE_1);
        style.setCellPadding(t.space.SPACE_1, 2f);
        style.setIndentSpacing(t.size.SIZE_INDENT_PER_LEVEL);
        style.setColumnsMinSpacing(t.space.SPACE_2);
        style.setScrollbarSize(t.size.SIZE_SCROLLBAR_W);
        style.setScrollbarRounding(t.radius.RADIUS_MD);
        style.setGrabMinSize(10f);
        style.setGrabRounding(t.radius.RADIUS_SM);
        style.setTabRounding(t.radius.RADIUS_SM);
        style.setTabBorderSize(0f);
        style.setButtonTextAlign(0.5f, 0.5f);
        style.setSelectableTextAlign(0f, 0.5f);
        style.setWindowMinSize(240f, 120f);
        style.setWindowTitleAlign(0f, 0.5f);
        style.setDisabledAlpha(0.45f);
        style.setSeparatorSize(1f);
        style.setDockingSeparatorSize(1f);
    }

    private static void set(ImGuiStyle style, int index, int packed) {
        style.setColor(index,
                Rgba.red(packed) / 255f,
                Rgba.green(packed) / 255f,
                Rgba.blue(packed) / 255f,
                Rgba.alpha(packed) / 255f);
    }

    private static int withAlpha(int packed, float alpha) {
        return Rgba.pack(Rgba.red(packed), Rgba.green(packed), Rgba.blue(packed),
                Math.round(alpha * 255f));
    }
}
