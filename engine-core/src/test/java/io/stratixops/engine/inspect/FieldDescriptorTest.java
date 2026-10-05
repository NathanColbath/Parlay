package io.stratixops.engine.inspect;

import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.scene.AssetRef;
import io.stratixops.engine.scene.Guid;
import io.stratixops.engine.testutil.SampleBehaviour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieldDescriptorTest {

    private ComponentDescriptor descriptor;

    @BeforeEach
    void setUp() {
        ComponentRegistry registry = ComponentRegistry.withBuiltins();
        descriptor = registry.register(SampleBehaviour.class);
    }

    private Map<String, FieldDescriptor> byName() {
        return descriptor.fields().stream()
                .collect(Collectors.toMap(FieldDescriptor::name, Function.identity()));
    }

    @Test
    void exposureRules() {
        List<String> serialized = descriptor.serializedFields().stream()
                .map(FieldDescriptor::name).collect(Collectors.toList());
        assertThat(serialized).contains("publicLabel", "secretLabel", "speed", "power",
                "tint", "big", "ratio", "enabledFlag", "point", "mood", "weights", "moods");
        assertThat(serialized).doesNotContain("ignoredLabel", "hiddenLabel", "constant",
                "staticLabel", "unsupported");
        assertThat(byName().get("unsupported").kind()).isEqualTo(PropertyKind.UNSUPPORTED);
        assertThat(byName().get("unsupported").serialized()).isFalse();
    }

    @Test
    void kindsAreMapped() {
        Map<String, FieldDescriptor> fields = byName();
        assertThat(fields.get("speed").kind()).isEqualTo(PropertyKind.FLOAT);
        assertThat(fields.get("power").kind()).isEqualTo(PropertyKind.INT);
        assertThat(fields.get("tint").kind()).isEqualTo(PropertyKind.COLOR);
        assertThat(fields.get("big").kind()).isEqualTo(PropertyKind.LONG);
        assertThat(fields.get("ratio").kind()).isEqualTo(PropertyKind.DOUBLE);
        assertThat(fields.get("enabledFlag").kind()).isEqualTo(PropertyKind.BOOLEAN);
        assertThat(fields.get("point").kind()).isEqualTo(PropertyKind.VEC2);
        assertThat(fields.get("mood").kind()).isEqualTo(PropertyKind.ENUM);
        assertThat(fields.get("sprite").kind()).isEqualTo(PropertyKind.ASSET);
        assertThat(fields.get("target").kind()).isEqualTo(PropertyKind.GAMEOBJECT);
        assertThat(fields.get("prefab").kind()).isEqualTo(PropertyKind.PREFAB);
        assertThat(fields.get("weights").kind()).isEqualTo(PropertyKind.LIST);
        assertThat(fields.get("weights").elementKind()).isEqualTo(PropertyKind.FLOAT);
        assertThat(fields.get("moods").elementKind()).isEqualTo(PropertyKind.ENUM);
        assertThat(fields.get("moods").elementType()).isEqualTo(SampleBehaviour.Mood.class);
        assertThat(fields.get("big").type()).isEqualTo(long.class);
    }

    @Test
    void annotationsAreCaptured() {
        FieldDescriptor power = byName().get("power");
        assertThat(power.header()).isEqualTo("Combat");
        assertThat(power.tooltip()).isEqualTo("How hard it hits");
        assertThat(power.order()).isEqualTo(1);
        assertThat(byName().get("speed").hasRange()).isTrue();
        assertThat(byName().get("speed").rangeMin()).isEqualTo(0f);
        assertThat(byName().get("speed").rangeMax()).isEqualTo(10f);
        assertThat(byName().get("speed").displayName()).isEqualTo("Speed");
    }

    @Test
    void orderingPutsOrderedFieldsFirst() {
        assertThat(descriptor.fields().get(0).name()).isEqualTo("power");
        assertThat(descriptor.fields().get(0).order()).isEqualTo(1);
    }

    @Test
    void defaultsAreCaptured() {
        assertThat(byName().get("speed").defaultValue()).isEqualTo(2.5f);
        assertThat(byName().get("enabledFlag").defaultValue()).isEqualTo(true);
    }

    @Test
    void getAndSet() {
        SampleBehaviour behaviour = new SampleBehaviour();
        FieldDescriptor tint = byName().get("tint");
        assertThat(tint.get(behaviour)).isEqualTo(0x11223344);
        tint.set(behaviour, 0x00000000);
        assertThat(tint.get(behaviour)).isEqualTo(0x00000000);
    }

    @Test
    void fieldLookupByName() {
        assertThat(descriptor.field("speed")).isNotNull();
        assertThat(descriptor.field("missing")).isNull();
        assertThat(descriptor.typeId()).isEqualTo("behaviour:" + SampleBehaviour.class.getName());
        assertThat(descriptor.category()).isEqualTo("Engine");
    }

    @Test
    void newInstanceCreatesComponent() {
        assertThat(descriptor.newInstance()).isInstanceOf(SampleBehaviour.class);
    }

    @Test
    void vec2AndRefsAreReadable() {
        SampleBehaviour behaviour = new SampleBehaviour();
        FieldDescriptor point = byName().get("point");
        Vec2 value = (Vec2) point.get(behaviour);
        assertThat(value.x).isEqualTo(4f);
        assertThat(value.y).isEqualTo(5f);

        FieldDescriptor sprite = byName().get("sprite");
        sprite.set(behaviour, AssetRef.of(Guid.random()));
        assertThat(sprite.get(behaviour)).isInstanceOf(AssetRef.class);
    }
}
