package io.stratixops.engine.editor.inspect;

import io.stratixops.engine.editor.fixture.TestBehaviour;
import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Guid;
import io.stratixops.engine.scene.UnsupportedComponent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InspectorModelTest {

    private static ComponentRegistry registry() {
        ComponentRegistry registry = ComponentRegistry.withBuiltins();
        registry.register(TestBehaviour.class);
        return registry;
    }

    @Test
    void componentsListTransformFirstThenAttachments() {
        GameObject object = new GameObject(Guid.random(), "Host");
        TestBehaviour behaviour = object.addComponent(new TestBehaviour());
        InspectorModel inspector = new InspectorModel(registry());

        List<io.stratixops.engine.scene.Component> components = inspector.componentsOf(object);
        assertThat(components).hasSize(2);
        assertThat(components.get(0)).isSameAs(object.transform());
        assertThat(components.get(1)).isSameAs(behaviour);
    }

    @Test
    void rowsIncludeSupportedFieldsOnly() {
        TestBehaviour behaviour = new TestBehaviour();
        new GameObject(Guid.random(), "Host").addComponent(behaviour);
        InspectorModel inspector = new InspectorModel(registry());

        assertThat(inspector.rows(behaviour))
                .extracting(row -> row.field().name())
                .contains("health", "speed", "velocity", "target", "label")
                .doesNotContain("unsupported");
    }

    @Test
    void headerPropagatesToFollowingRows() {
        TestBehaviour behaviour = new TestBehaviour();
        InspectorModel inspector = new InspectorModel(registry());

        List<FieldRow> rows = inspector.rows(behaviour);
        String velocityHeader = rows.stream()
                .filter(row -> row.field().name().equals("velocity"))
                .findFirst().orElseThrow().header();
        String targetHeader = rows.stream()
                .filter(row -> row.field().name().equals("target"))
                .findFirst().orElseThrow().header();

        assertThat(velocityHeader).isEqualTo("Motion");
        assertThat(targetHeader).isEqualTo("Motion");
    }

    @Test
    void unsupportedComponentsAreNotInspectable() {
        InspectorModel inspector = new InspectorModel(registry());
        assertThat(inspector.isInspectable(new UnsupportedComponent("x", "{}"))).isFalse();
        assertThat(inspector.componentsOf(null)).isEmpty();
    }
}
