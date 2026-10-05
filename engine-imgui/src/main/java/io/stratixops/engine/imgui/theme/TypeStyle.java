package io.stratixops.engine.imgui.theme;

/**
 * A typography style token ({@code type.*}).
 *
 * @param size       font size in logical pixels
 * @param lineHeight line box height in logical pixels
 * @param weight     font weight (400/500/600 in the design system)
 * @param tracking   letter spacing in logical pixels
 */
public record TypeStyle(float size, float lineHeight, int weight, float tracking) {
}
