package io.stratixops.engine.imgui.theme;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ThemeTokenTest {

    private static Map<ThemeMode, Theme> themes;
    private static Theme dark;

    @BeforeAll
    static void load() {
        themes = ThemeLoader.loadAll(ThemeLoader.DEFAULT_RESOURCE);
        dark = themes.get(ThemeMode.DARK);
    }

    @Test
    void resolvesDocumentedColorValues() {
        assertThat(dark.color.BG_CANVAS).isEqualTo(Rgba.parse("#0B0E14"));
        assertThat(dark.color.BG_PANEL).isEqualTo(Rgba.parse("#11151D"));
        assertThat(dark.color.TEXT_PRIMARY).isEqualTo(Rgba.parse("#EFF2F5"));
        assertThat(dark.color.ACCENT_DEFAULT).isEqualTo(Rgba.parse("#14C8A8"));
        assertThat(dark.color.STATE_ERROR_SUBTLE).isEqualTo(Rgba.parse("rgba(248,81,73,0.16)"));
        assertThat(dark.color.TEXT_ON_ACCENT).isEqualTo(Rgba.parse("#04110E"));
    }

    @Test
    void resolvesLightTheme() {
        Theme light = themes.get(ThemeMode.LIGHT);
        assertThat(light.mode).isEqualTo(ThemeMode.LIGHT);
        assertThat(light.color.BG_PANEL).isEqualTo(Rgba.parse("#FFFFFF"));
        assertThat(light.color.TEXT_PRIMARY).isEqualTo(Rgba.parse("#11151D"));
        assertThat(light.color.ACCENT_DEFAULT).isEqualTo(Rgba.parse("#0EA88D"));
    }

    @Test
    void resolvesTypography() {
        assertThat(dark.typography.FAMILY_UI).isEqualTo("Inter");
        assertThat(dark.typography.FAMILY_MONO).isEqualTo("JetBrains Mono");
        assertThat(dark.typography.TYPE_MD.size()).isEqualTo(13f);
        assertThat(dark.typography.TYPE_MD.lineHeight()).isEqualTo(20f);
        assertThat(dark.typography.TYPE_MD.weight()).isEqualTo(400);
        assertThat(dark.typography.TYPE_MD_MEDIUM.weight()).isEqualTo(500);
        assertThat(dark.typography.TYPE_LG.weight()).isEqualTo(600);
        assertThat(dark.typography.TYPE_2XL.size()).isEqualTo(24f);
        assertThat(dark.typography.TYPE_MONO.tracking()).isEqualTo(0f);
    }

    @Test
    void resolvesSizesAndSpacing() {
        assertThat(dark.space.SPACE_4).isEqualTo(16f);
        assertThat(dark.space.SPACE_0).isEqualTo(0f);
        assertThat(dark.size.SIZE_ROW_H).isEqualTo(22f);
        assertThat(dark.size.SIZE_CONTROL_MD).isEqualTo(24f);
        assertThat(dark.size.SIZE_MIN_WINDOW_W).isEqualTo(1024f);
        assertThat(dark.radius.RADIUS_SM).isEqualTo(4f);
        assertThat(dark.radius.RADIUS_PILL).isEqualTo(999f);
    }

    @Test
    void resolvesElevationShadowsPerMode() {
        assertThat(dark.elev.ELEV_0.isNone()).isTrue();
        Shadow e1 = dark.elev.ELEV_1;
        assertThat(e1.dx()).isEqualTo(0);
        assertThat(e1.dy()).isEqualTo(2);
        assertThat(e1.blur()).isEqualTo(8);
        assertThat(Rgba.alpha(e1.rgba())).isEqualTo(89);
        assertThat(dark.elev.ELEV_3_BORDER).isEqualTo(dark.color.ACCENT_DEFAULT);

        Theme light = themes.get(ThemeMode.LIGHT);
        assertThat(light.elev.ELEV_1.rgba()).isNotEqualTo(dark.elev.ELEV_1.rgba());
        assertThat(dark.elev.level(2)).isEqualTo(dark.elev.ELEV_2);
    }

    @Test
    void resolvesMotion() {
        assertThat(dark.motion.MOTION_INSTANT).isEqualTo(0f);
        assertThat(dark.motion.MOTION_FAST).isEqualTo(120f);
        assertThat(dark.motion.MOTION_BASE).isEqualTo(160f);
        assertThat(dark.motion.MOTION_SLOW).isEqualTo(240f);
        assertThat(dark.motion.EASING_OUT).containsExactly(0.2f, 0f, 0f, 1f);
        assertThat(dark.motion.EASING_IN_OUT).containsExactly(0.4f, 0f, 0.2f, 1f);
        assertThat(dark.motion.duration(dark.motion.MOTION_BASE, true))
                .isEqualTo(dark.motion.MOTION_INSTANT);
    }

    @Test
    void resolvesIcons() {
        assertThat(dark.icon.ICON_SM).isEqualTo(14f);
        assertThat(dark.icon.ICON_MD).isEqualTo(16f);
        assertThat(dark.icon.ICON_LG).isEqualTo(20f);
        assertThat(dark.icon.lucide(Icon.GAME_OBJECT)).isEqualTo("box");
        assertThat(dark.icon.lucide(Icon.PLACEHOLDER)).isEqualTo("circle");
        assertThat(Icon.valueOf("COMMAND_PALETTE").lucide()).isEqualTo("command");
    }
}
