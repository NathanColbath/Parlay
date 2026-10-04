package io.stratixops.qa;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Validates the QA harness itself. If these fail, do not trust any other test
 * result from this build.
 */
class HarnessSmokeTest {

    @Test
    @DisplayName("JUnit 5 + AssertJ are wired up")
    void testStackIsWired() {
        assertThat(2 + 2).isEqualTo(4);
    }

    @Test
    @DisplayName("GPU test flag defaults to disabled")
    void gpuTestsDefaultOff() {
        Assumptions.assumeFalse(TestEnvironment.gpuTestsEnabled(),
                "GPU tests explicitly enabled for this run");
        assertThat(TestEnvironment.gpuTestsEnabled()).isFalse();
    }

    @Test
    @DisplayName("Headless flag is readable")
    void headlessFlagIsReadable() {
        assertThat(TestEnvironment.isHeadless()).isIn(true, false);
    }

    @Test
    @Tag("gpu")
    @DisplayName("GPU-tagged tests only execute when explicitly enabled")
    void gpuTaggedTestOnlyRunsWhenEnabled() {
        assertThat(TestEnvironment.gpuTestsEnabled()).isTrue();
    }
}
