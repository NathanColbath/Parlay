package io.stratixops.engine.input;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Verifies duplicate-action semantics (UX review S6): registering the same
 * action twice merges bindings rather than replacing them.
 */
class ActionMapTest {

    @Test
    void registeringAxisTwiceMergesKeys() {
        ActionMap map = ActionMap.def()
            .axis("Move", Key.W, Key.A, Key.S, Key.D)
            .axis("Move", Key.UP, Key.LEFT, Key.DOWN, Key.RIGHT)
            .build();

        assertThat(map.axisKeys("Move"))
            .containsExactly(Key.W, Key.A, Key.S, Key.D, Key.UP, Key.LEFT, Key.DOWN, Key.RIGHT);
    }

    @Test
    void duplicateBindingOfSameKeyIsIdempotent() {
        ActionMap map = ActionMap.def()
            .axis("Move", Key.W, Key.A)
            .axis("Move", Key.W)
            .build();

        assertThat(map.axisKeys("Move")).containsExactly(Key.W, Key.A);
    }

    @Test
    void buttonBindingsAlsoMerge() {
        ActionMap map = ActionMap.def()
            .button("Confirm", Key.SPACE)
            .button("Confirm", Key.ENTER)
            .build();

        assertThat(map.buttonKeys("Confirm")).containsExactly(Key.SPACE, Key.ENTER);
        assertThat(map.axisKeys("Confirm")).isEmpty();
    }
}
