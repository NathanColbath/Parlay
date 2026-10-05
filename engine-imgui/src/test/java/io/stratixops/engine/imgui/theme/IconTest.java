package io.stratixops.engine.imgui.theme;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * STR-36: every semantic icon resolves to a Lucide Private Use Area codepoint,
 * and alias constants share the canonical glyph.
 */
class IconTest {

    @Test
    void everyIconMapsToAPrivateUseCodepoint() {
        for (Icon icon : Icon.values()) {
            assertThat(icon.lucide()).as(icon.name()).isNotBlank();
            assertThat(icon.codepoint())
                    .as("%s (U+%04X) is in the Lucide PUA range", icon.name(), icon.codepoint())
                    .isBetween(0xE000, 0xF8FF);
        }
    }

    @Test
    void allCodepointsAreDistinctAndComplete() {
        Set<Integer> fromEnum = new HashSet<>();
        for (Icon icon : Icon.values()) {
            fromEnum.add(icon.codepoint());
        }
        int[] all = Icon.allCodepoints();
        Set<Integer> fromArray = new HashSet<>();
        for (int codepoint : all) {
            fromArray.add(codepoint);
        }
        assertThat(all).hasSize(fromEnum.size());
        assertThat(fromArray).isEqualTo(fromEnum);
    }

    @Test
    void aliasesShareTheCanonicalGlyph() {
        assertThat(Icon.SUBMENU.codepoint()).isEqualTo(Icon.CHEVRON_RIGHT.codepoint());
        assertThat(Icon.BREADCRUMB_SEPARATOR.codepoint()).isEqualTo(Icon.CHEVRON_RIGHT.codepoint());
        assertThat(Icon.SCENE_ASSET.codepoint()).isEqualTo(Icon.SCENE.codepoint());
        assertThat(Icon.IMAGE_ASSET.codepoint()).isEqualTo(Icon.SPRITE_RENDERER.codepoint());
    }

    @Test
    void iconCodepointPredicateMatchesTheEnum() {
        assertThat(Icon.isIconCodepoint(Icon.PLAY.codepoint())).isTrue();
        assertThat(Icon.isIconCodepoint(0xE000)).isFalse();
        assertThat(Icon.isIconCodepoint('A')).isFalse();
    }
}
