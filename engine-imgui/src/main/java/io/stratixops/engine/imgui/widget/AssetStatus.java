package io.stratixops.engine.imgui.widget;

/**
 * Import status of an asset tile/row (design system section 3.4):
 * {@code READY} draws its thumbnail, {@code MISSING} draws the magenta
 * placeholder with a warning mark, and {@code UNIMPORTED} draws a spinner.
 */
public enum AssetStatus {
    READY,
    MISSING,
    UNIMPORTED
}
