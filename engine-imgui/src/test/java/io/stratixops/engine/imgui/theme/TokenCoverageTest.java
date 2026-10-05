package io.stratixops.engine.imgui.theme;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/**
 * Design system AC1: {@code Theme} exposes every token in {@code tokens.json}
 * and no token is silently dropped.
 */
class TokenCoverageTest {

    @Test
    void themeConsumesEveryLeafToken() {
        TokenDocument document = TokenDocument.fromResource(ThemeLoader.DEFAULT_RESOURCE);
        for (ThemeMode mode : ThemeMode.values()) {
            ThemeLoader.build(document, mode);
        }

        Set<String> leaves = document.leafPaths();
        Set<String> consumed = document.consumedPaths();

        Set<String> missing = new TreeSet<>(leaves);
        missing.removeAll(consumed);
        Set<String> unknown = new TreeSet<>(consumed);
        unknown.removeAll(leaves);

        assertThat(leaves).as("tokens.json should define tokens").isNotEmpty();
        assertThat(missing).as("tokens not consumed by Theme").isEmpty();
        assertThat(unknown).as("paths consumed by Theme but absent from tokens.json").isEmpty();
    }

    @Test
    void darkAndLightDefineTheSameColorTokens() {
        TokenDocument document = TokenDocument.fromResource(ThemeLoader.DEFAULT_RESOURCE);
        Theme dark = ThemeLoader.build(document, ThemeMode.DARK);
        Theme light = ThemeLoader.build(document, ThemeMode.LIGHT);

        assertThat(ColorTokens.paths()).isNotEmpty();
        assertThat(dark.color.BG_PANEL).isNotEqualTo(light.color.BG_PANEL);
        assertThat(dark.color.TEXT_PRIMARY).isNotEqualTo(light.color.TEXT_PRIMARY);
    }
}
