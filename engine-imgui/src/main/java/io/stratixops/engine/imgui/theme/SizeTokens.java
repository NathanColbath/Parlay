// GENERATED from docs/design/tokens.json by the STR-22 theme generator. Do not edit by hand.
package io.stratixops.engine.imgui.theme;

/** Fixed size tokens in logical pixels (`size.*`). */
public final class SizeTokens {

    public final float SIZE_MENU_BAR_H;
    public final float SIZE_TOOLBAR_H;
    public final float SIZE_TAB_H;
    public final float SIZE_PANEL_HEADER_H;
    public final float SIZE_STATUS_BAR_H;
    public final float SIZE_BREADCRUMB_H;
    public final float SIZE_ROW_H;
    public final float SIZE_CHIP_H;
    public final float SIZE_CONTROL_SM;
    public final float SIZE_CONTROL_MD;
    public final float SIZE_CONTROL_LG;
    public final float SIZE_CONTROL_XL;
    public final float SIZE_ICON_SM;
    public final float SIZE_ICON_MD;
    public final float SIZE_ICON_LG;
    public final float SIZE_INDENT_PER_LEVEL;
    public final float SIZE_SCROLLBAR_W;
    public final float SIZE_COLOR_SWATCH;
    public final float SIZE_THUMB_SM;
    public final float SIZE_THUMB_MD;
    public final float SIZE_THUMB_LG;
    public final float SIZE_PALETTE_W;
    public final float SIZE_MIN_WINDOW_W;
    public final float SIZE_MIN_WINDOW_H;

    SizeTokens(TokenDocument doc) {
        SIZE_MENU_BAR_H = doc.number("size/size.menuBar.h");
        SIZE_TOOLBAR_H = doc.number("size/size.toolbar.h");
        SIZE_TAB_H = doc.number("size/size.tab.h");
        SIZE_PANEL_HEADER_H = doc.number("size/size.panel.header.h");
        SIZE_STATUS_BAR_H = doc.number("size/size.statusBar.h");
        SIZE_BREADCRUMB_H = doc.number("size/size.breadcrumb.h");
        SIZE_ROW_H = doc.number("size/size.row.h");
        SIZE_CHIP_H = doc.number("size/size.chip.h");
        SIZE_CONTROL_SM = doc.number("size/size.control.sm");
        SIZE_CONTROL_MD = doc.number("size/size.control.md");
        SIZE_CONTROL_LG = doc.number("size/size.control.lg");
        SIZE_CONTROL_XL = doc.number("size/size.control.xl");
        SIZE_ICON_SM = doc.number("size/size.icon.sm");
        SIZE_ICON_MD = doc.number("size/size.icon.md");
        SIZE_ICON_LG = doc.number("size/size.icon.lg");
        SIZE_INDENT_PER_LEVEL = doc.number("size/size.indent.perLevel");
        SIZE_SCROLLBAR_W = doc.number("size/size.scrollbar.w");
        SIZE_COLOR_SWATCH = doc.number("size/size.colorSwatch");
        SIZE_THUMB_SM = doc.number("size/size.thumb.sm");
        SIZE_THUMB_MD = doc.number("size/size.thumb.md");
        SIZE_THUMB_LG = doc.number("size/size.thumb.lg");
        SIZE_PALETTE_W = doc.number("size/size.palette.w");
        SIZE_MIN_WINDOW_W = doc.number("size/size.minWindow.w");
        SIZE_MIN_WINDOW_H = doc.number("size/size.minWindow.h");
    }

    /** @return every token path covered by this token group. */
    public static String[] paths() {
        return new String[] { "size.menuBar.h", "size.toolbar.h", "size.tab.h", "size.panel.header.h", "size.statusBar.h", "size.breadcrumb.h", "size.row.h", "size.chip.h", "size.control.sm", "size.control.md", "size.control.lg", "size.control.xl", "size.icon.sm", "size.icon.md", "size.icon.lg", "size.indent.perLevel", "size.scrollbar.w", "size.colorSwatch", "size.thumb.sm", "size.thumb.md", "size.thumb.lg", "size.palette.w", "size.minWindow.w", "size.minWindow.h" };
    }
}
