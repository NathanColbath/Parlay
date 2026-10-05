package io.stratixops.engine.scene;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GuidTest {

    @Test
    void randomProducesDistinctValues() {
        assertThat(Guid.random()).isNotEqualTo(Guid.random());
    }

    @Test
    void ofNormalizesCaseAndIsEqual() {
        Guid upper = Guid.of("B3F1C2E0-9A44-4F1E-8B2D-0F6A5C7D9E01");
        Guid lower = Guid.of("b3f1c2e0-9a44-4f1e-8b2d-0f6a5c7d9e01");
        assertThat(upper).isEqualTo(lower);
        assertThat(upper.value()).isEqualTo("b3f1c2e0-9a44-4f1e-8b2d-0f6a5c7d9e01");
        assertThat(upper).hasSameHashCodeAs(lower);
        assertThat(upper.toString()).isEqualTo(upper.value());
    }

    @Test
    void rejectsBlankAndMalformed() {
        assertThatThrownBy(() -> Guid.of(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Guid.of("  ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Guid.of("not-a-guid")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equalityHandlesSameInstanceAndOtherTypes() {
        Guid guid = Guid.random();
        assertThat(guid.equals(guid)).isTrue();
        assertThat(guid.equals("nope")).isFalse();
    }
}
