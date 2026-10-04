package io.stratixops.gemdash;

import static org.assertj.core.api.Assertions.assertThat;

import io.stratixops.engine.GameLifecycle;
import org.junit.jupiter.api.Test;

/**
 * Keeps the doc snippets on the compile path (UX review B4). The test source
 * set compiles the exact shapes shown in
 * {@code docs/design/02-api-ergonomics.md}; these references fail the build if
 * the snippets drift away from the public API.
 */
class ApiContractCompilationTest {

    @Test
    void helloEngineSnippetImplementsTheLifecycle() {
        GameLifecycle hello = new HelloEngine();
        assertThat(hello).isNotNull();
    }

    @Test
    void gemDashSnippetImplementsTheLifecycle() {
        GameLifecycle gemDash = new GemDashGame(0);
        assertThat(gemDash).isNotNull();
    }
}
