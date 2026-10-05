package io.stratixops.engine.imgui.theme;

/**
 * Icon tokens. Sizes alias the {@code size.icon.*} tokens (the design system
 * keeps icon dimensions in the size scale); the glyph set is the Lucide map in
 * {@link Icon}.
 */
public final class IconTokens {

    /** Dense/table icon size ({@code size.icon.sm}). */
    public final float ICON_SM;
    /** Default icon size ({@code size.icon.md}). */
    public final float ICON_MD;
    /** Toolbar icon size ({@code size.icon.lg}). */
    public final float ICON_LG;

    IconTokens(SizeTokens size) {
        ICON_SM = size.SIZE_ICON_SM;
        ICON_MD = size.SIZE_ICON_MD;
        ICON_LG = size.SIZE_ICON_LG;
    }

    /** @return the Lucide identifier for a semantic icon. */
    public String lucide(Icon icon) {
        return icon.lucide();
    }

    /** @return every token path covered by this token group (aliased from size). */
    public static String[] paths() {
        return new String[] { "size.icon.sm", "size.icon.md", "size.icon.lg" };
    }
}
