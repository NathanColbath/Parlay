package io.stratixops.engine.imgui.glue;

import imgui.ImFontAtlas;
import imgui.ImFontConfig;
import imgui.ImFontGlyphRangesBuilder;
import io.stratixops.engine.imgui.theme.Icon;
import io.stratixops.engine.imgui.theme.Theme;
import java.io.IOException;
import java.io.InputStream;

/**
 * DPI-aware font atlas builder (design system section 5.4).
 *
 * <p>Fonts are embedded resources, never OS fonts: Inter (UI), JetBrains Mono
 * (numbers/console) and a Lucide subset (icons). The atlas is rasterized at the
 * supported scale nearest the request — including 1.25, which the DPI scale set
 * uses — and a bitmap is never upscaled: each requested pixel size is rounded to
 * a whole physical pixel via {@link #snapPixels(float)} so ImGui's line height
 * (which equals the rasterized font size) is an integer at fractional scales.
 *
 * <p>The Lucide subset is merged into the primary Inter face, so the default
 * font resolves icon codepoints without a second font stack. Glyph ranges add
 * General Punctuation and Geometric Shapes (the em dash, ellipsis and
 * disclosure triangles the kit draws) on top of ImGui's default Latin range.
 *
 * <p>If a font resource is absent the layer falls back to ImGui's built-in font
 * so headless rendering and tests still work. In a packaged build the resources
 * are present and the fallback never runs; {@code FontResourcesTest} pins that.
 * This is GL/native glue and is excluded from the headless coverage gate.
 */
public final class FontAtlas {

    /** dpi scales the atlas is rasterized for (design system section 5.4). */
    public static final float[] DPI_SCALES = {1.0f, 1.25f, 1.5f, 2.0f};

    public static final String UI_REGULAR =
            "/io/stratixops/engine/imgui/fonts/Inter-Regular.ttf";
    public static final String UI_MEDIUM =
            "/io/stratixops/engine/imgui/fonts/Inter-Medium.ttf";
    public static final String UI_SEMIBOLD =
            "/io/stratixops/engine/imgui/fonts/Inter-SemiBold.ttf";
    public static final String MONO_REGULAR =
            "/io/stratixops/engine/imgui/fonts/JetBrainsMono-Regular.ttf";
    public static final String ICON_FONT =
            "/io/stratixops/engine/imgui/fonts/lucide.ttf";

    /** Every embedded face, in the order {@link #load} registers them. */
    public static final String[] FONT_RESOURCES = {
        UI_REGULAR, UI_MEDIUM, UI_SEMIBOLD, MONO_REGULAR, ICON_FONT
    };

    private FontAtlas() {
    }

    /**
     * Populates the atlas for the requested DPI scale.
     *
     * @return true when the design-system faces were loaded (no fallback was used)
     */
    public static boolean load(ImFontAtlas atlas, Theme theme, float dpiScale) {
        float scale = nearestScale(dpiScale);
        boolean uiLoaded = add(atlas, UI_REGULAR,
                snapPixels(theme.typography.TYPE_MD.size() * scale), false, textRanges(atlas));
        if (!uiLoaded) {
            // Defensive only: the assets ship in the jar.
            atlas.addFontDefault();
        } else {
            // Icons share the default face so drawText() on the primary font
            // resolves PUA codepoints (design system section 1.6).
            add(atlas, ICON_FONT, snapPixels(theme.icon.ICON_MD * scale), true, iconRanges());
        }
        add(atlas, UI_MEDIUM,
                snapPixels(theme.typography.TYPE_MD.size() * scale), false, textRanges(atlas));
        add(atlas, UI_SEMIBOLD,
                snapPixels(theme.typography.TYPE_LG.size() * scale), false, textRanges(atlas));
        add(atlas, MONO_REGULAR,
                snapPixels(theme.typography.TYPE_MONO.size() * scale), false, textRanges(atlas));
        return uiLoaded;
    }

    /** @return the supported rasterization scale nearest to the request. */
    public static float nearestScale(float dpiScale) {
        float best = DPI_SCALES[0];
        float bestDelta = Float.MAX_VALUE;
        for (float candidate : DPI_SCALES) {
            float delta = Math.abs(candidate - dpiScale);
            if (delta < bestDelta) {
                bestDelta = delta;
                best = candidate;
            }
        }
        return best;
    }

    /**
     * Snaps a logical pixel size to a whole physical pixel.
     *
     * <p>ImGui's text line height equals the rasterized font size, so rounding
     * here is what keeps line height (and therefore the baseline) on the pixel
     * grid at 1.25x and 1.5x. Never returns 0.
     */
    public static int snapPixels(float logicalPixels) {
        return Math.max(1, Math.round(logicalPixels));
    }

    private static short[] textRanges(ImFontAtlas atlas) {
        ImFontGlyphRangesBuilder builder = new ImFontGlyphRangesBuilder();
        builder.addRanges(atlas.getGlyphRangesDefault());
        // General Punctuation: em dash (U+2014), ellipsis (U+2026), bullet (U+2022).
        builder.addRanges(new short[] {0x2000, 0x206F, 0});
        // Arrows: the link modifier arrow U+2197.
        builder.addRanges(new short[] {0x2190, 0x21FF, 0});
        // Geometric Shapes: sort/drop triangles U+25B2 / U+25BC.
        builder.addRanges(new short[] {0x25A0, 0x25FF, 0});
        return builder.buildRanges();
    }

    private static short[] iconRanges() {
        ImFontGlyphRangesBuilder builder = new ImFontGlyphRangesBuilder();
        for (int codepoint : Icon.allCodepoints()) {
            builder.addChar((char) codepoint);
        }
        return builder.buildRanges();
    }

    private static boolean add(ImFontAtlas atlas, String resource, float sizePixels, boolean merge,
            short[] ranges) {
        byte[] data = read(resource);
        if (data == null) {
            return false;
        }
        ImFontConfig config = new ImFontConfig();
        // ImFontConfig.Name is a fixed 40-byte buffer and the binding copies
        // with strcpy, so only the file name may be passed — never the resource
        // path (which would overflow into FontData and trip ImGui's assert).
        config.setName(shortName(resource));
        config.setMergeMode(merge);
        config.setPixelSnapH(true);
        // Use the overload without an explicit size: ImGui 1.92 asserts that
        // the config does not carry font data when the size is passed in.
        atlas.addFontFromMemoryTTF(data, sizePixels, config, ranges);
        return true;
    }

    /** @return the file name of a resource path, safe for {@code ImFontConfig.Name}. */
    static String shortName(String resource) {
        int slash = resource.lastIndexOf('/');
        String name = slash < 0 ? resource : resource.substring(slash + 1);
        return name.length() <= 39 ? name : name.substring(name.length() - 39);
    }

    private static byte[] read(String resource) {
        try (InputStream in = FontAtlas.class.getResourceAsStream(resource)) {
            return in == null ? null : in.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }
}
