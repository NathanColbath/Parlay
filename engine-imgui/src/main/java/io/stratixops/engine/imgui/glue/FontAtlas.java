package io.stratixops.engine.imgui.glue;

import imgui.ImFontAtlas;
import imgui.ImFontConfig;
import io.stratixops.engine.imgui.theme.Theme;
import java.io.IOException;
import java.io.InputStream;

/**
 * DPI-aware font atlas builder (design system section 5.4).
 *
 * <p>Fonts are embedded resources, never OS fonts: Inter (UI), JetBrains Mono
 * (numbers/console) and a Lucide subset (icons). Atlases are rasterized at each
 * supported scale — including 1.25, which the dpi scale set uses — and the
 * nearest is selected; a bitmap is never upscaled.
 *
 * <p>When a font resource is absent (for example before the licensed font
 * assets land), the layer falls back to ImGui's built-in font so headless
 * rendering and tests still work. This is GL/native glue and is excluded from
 * the headless coverage gate.
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

    private FontAtlas() {
    }

    /**
     * Populates the atlas for the requested DPI scale.
     *
     * @return true when at least one design-system font was loaded
     */
    public static boolean load(ImFontAtlas atlas, Theme theme, float dpiScale) {
        float scale = nearestScale(dpiScale);
        boolean any = false;
        any |= add(atlas, UI_REGULAR, theme.typography.TYPE_MD.size() * scale, false);
        any |= add(atlas, UI_MEDIUM, theme.typography.TYPE_MD.size() * scale, false);
        any |= add(atlas, UI_SEMIBOLD, theme.typography.TYPE_LG.size() * scale, false);
        any |= add(atlas, MONO_REGULAR, theme.typography.TYPE_MONO.size() * scale, false);
        any |= add(atlas, ICON_FONT, theme.icon.ICON_MD * scale, false);
        if (!any) {
            atlas.addFontDefault();
        }
        return any;
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

    private static boolean add(ImFontAtlas atlas, String resource, float sizePixels, boolean merge) {
        byte[] data = read(resource);
        if (data == null) {
            return false;
        }
        ImFontConfig config = new ImFontConfig();
        config.setName(resource);
        config.setMergeMode(merge);
        config.setPixelSnapH(true);
        atlas.addFontFromMemoryTTF(data, data.length, sizePixels, config);
        return true;
    }

    private static byte[] read(String resource) {
        try (InputStream in = FontAtlas.class.getResourceAsStream(resource)) {
            return in == null ? null : in.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }
}
