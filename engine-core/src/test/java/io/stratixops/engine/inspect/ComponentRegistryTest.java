package io.stratixops.engine.inspect;

import io.stratixops.engine.scene.Camera;
import io.stratixops.engine.scene.Component;
import io.stratixops.engine.scene.SpriteRenderer;
import io.stratixops.engine.scene.Transform;
import io.stratixops.engine.testutil.SampleBehaviour;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ComponentRegistryTest {

    @Test
    void builtinsRegisterWithStableIds() {
        ComponentRegistry registry = ComponentRegistry.withBuiltins();
        assertThat(registry.typeId(Transform.class)).isEqualTo("transform");
        assertThat(registry.typeId(SpriteRenderer.class)).isEqualTo("spriteRenderer");
        assertThat(registry.typeId(Camera.class)).isEqualTo("camera");
        assertThat(registry.descriptor("transform").type()).isEqualTo(Transform.class);
        assertThat(registry.contains("spriteRenderer")).isTrue();
        assertThat(registry.contains("unknown")).isFalse();
        assertThat(registry.typeIds()).contains("transform", "animator", "audioSource", "collider2D");
        assertThat(registry.descriptors()).isNotEmpty();
        assertThat(registry.descriptor(Camera.class).displayName()).isEqualTo("Camera");
    }

    @Test
    void behaviourTypeIdsAreNamespaced() {
        assertThat(ComponentRegistry.defaultTypeId(SampleBehaviour.class))
                .isEqualTo("behaviour:" + SampleBehaviour.class.getName());
    }

    @Test
    void newInstanceAndUnknownType() {
        ComponentRegistry registry = ComponentRegistry.withBuiltins();
        assertThat(registry.newInstance("camera")).isInstanceOf(Camera.class);
        assertThatThrownBy(() -> registry.newInstance("nope"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> registry.typeId(SampleBehaviour.class))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsDuplicateTypeIdAndClass() {
        ComponentRegistry registry = new ComponentRegistry();
        registry.register(Camera.class, "camera");
        assertThatThrownBy(() -> registry.register(Camera.class, "camera"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> registry.register(Camera.class, "camera-2"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> registry.register(SpriteRenderer.class, "camera"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void missingNoArgConstructorFailsFast() {
        ComponentRegistry registry = new ComponentRegistry();
        assertThatThrownBy(() -> registry.register(NoDefaultCtor.class, "bad"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no-arg constructor");
    }

    @Test
    void registerAllAddsEveryType() {
        ComponentRegistry registry = new ComponentRegistry();
        registry.registerAll(List.of(Camera.class, SpriteRenderer.class));
        assertThat(registry.contains(Camera.class)).isTrue();
        assertThat(registry.contains(SpriteRenderer.class)).isTrue();
    }

    /** A component without a usable no-arg constructor. */
    public static final class NoDefaultCtor extends Component {
        /**
         * @param value ignored
         */
        public NoDefaultCtor(int value) {
        }
    }
}
