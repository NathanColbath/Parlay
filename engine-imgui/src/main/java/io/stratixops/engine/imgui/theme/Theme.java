package io.stratixops.engine.imgui.theme;

/**
 * A resolved theme: one instance per {@link ThemeMode}, with every design token
 * exposed as a public field in grouped token objects.
 *
 * <p>Naming follows the design system contract: the SCREAMING_SNAKE form of the
 * token path, so {@code bg.panel} is {@code theme.color.BG_PANEL} and
 * {@code type.mdMedium} is {@code theme.typography.TYPE_MD_MEDIUM}. No widget
 * may reference a literal color, size, or radius outside this object.
 */
public final class Theme {

    public final ThemeMode mode;
    public final ColorTokens color;
    public final TypographyTokens typography;
    public final SpaceTokens space;
    public final SizeTokens size;
    public final RadiusTokens radius;
    public final ElevationTokens elev;
    public final MotionTokens motion;
    public final IconTokens icon;

    Theme(ThemeMode mode, ColorTokens color, TypographyTokens typography, SpaceTokens space,
            SizeTokens size, RadiusTokens radius, ElevationTokens elev, MotionTokens motion,
            IconTokens icon) {
        this.mode = mode;
        this.color = color;
        this.typography = typography;
        this.space = space;
        this.size = size;
        this.radius = radius;
        this.elev = elev;
        this.motion = motion;
        this.icon = icon;
    }
}
