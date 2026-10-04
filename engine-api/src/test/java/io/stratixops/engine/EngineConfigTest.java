package io.stratixops.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Verifies the config contract: window size and logical viewport are separate,
 * documented, defaulted calls (UX review S2).
 */
class EngineConfigTest {

    @Test
    void windowAndViewportHaveIndependentDefaults() {
        EngineConfig config = EngineConfig.builder().build();

        assertThat(config.windowWidth()).isEqualTo(1280);
        assertThat(config.windowHeight()).isEqualTo(720);
        assertThat(config.logicalWidth()).isEqualTo(640);
        assertThat(config.logicalHeight()).isEqualTo(360);
        assertThat(config.title()).isEqualTo("Stratix Engine");
        assertThat(config.resizable()).isTrue();
        assertThat(config.vsync()).isTrue();
    }

    @Test
    void windowSizeAndLogicalViewportAreSetIndependently() {
        EngineConfig config = EngineConfig.builder()
            .windowSize(1920, 1080)
            .logicalViewport(960, 540)
            .build();

        assertThat(config.windowWidth()).isEqualTo(1920);
        assertThat(config.windowHeight()).isEqualTo(1080);
        assertThat(config.logicalWidth()).isEqualTo(960);
        assertThat(config.logicalHeight()).isEqualTo(540);
    }

    @Test
    void nonPositiveDimensionsFailFast() {
        assertThatThrownBy(() -> EngineConfig.builder().windowSize(0, 720).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("windowSize.width");
        assertThatThrownBy(() -> EngineConfig.builder().logicalViewport(640, -1).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("logicalViewport.height");
    }
}
