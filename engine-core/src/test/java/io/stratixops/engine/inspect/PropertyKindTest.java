package io.stratixops.engine.inspect;

import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.scene.AssetRef;
import io.stratixops.engine.scene.ObjectRef;
import io.stratixops.engine.scene.PrefabRef;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyKindTest {

    /** A sample enum. */
    enum Flag {
        ON
    }

    @Test
    void mapsScalarTypes() {
        assertThat(PropertyKind.fromType(boolean.class, false)).isEqualTo(PropertyKind.BOOLEAN);
        assertThat(PropertyKind.fromType(boolean.class, true)).isEqualTo(PropertyKind.BOOLEAN);
        assertThat(PropertyKind.fromType(int.class, false)).isEqualTo(PropertyKind.INT);
        assertThat(PropertyKind.fromType(byte.class, false)).isEqualTo(PropertyKind.INT);
        assertThat(PropertyKind.fromType(Integer.class, true)).isEqualTo(PropertyKind.COLOR);
        assertThat(PropertyKind.fromType(long.class, false)).isEqualTo(PropertyKind.LONG);
        assertThat(PropertyKind.fromType(float.class, false)).isEqualTo(PropertyKind.FLOAT);
        assertThat(PropertyKind.fromType(double.class, false)).isEqualTo(PropertyKind.DOUBLE);
        assertThat(PropertyKind.fromType(String.class, false)).isEqualTo(PropertyKind.STRING);
        assertThat(PropertyKind.fromType(Flag.class, false)).isEqualTo(PropertyKind.ENUM);
        assertThat(PropertyKind.fromType(Vec2.class, false)).isEqualTo(PropertyKind.VEC2);
        assertThat(PropertyKind.fromType(AssetRef.class, false)).isEqualTo(PropertyKind.ASSET);
        assertThat(PropertyKind.fromType(ObjectRef.class, false)).isEqualTo(PropertyKind.GAMEOBJECT);
        assertThat(PropertyKind.fromType(PrefabRef.class, false)).isEqualTo(PropertyKind.PREFAB);
        assertThat(PropertyKind.fromType(List.class, false)).isEqualTo(PropertyKind.LIST);
        assertThat(PropertyKind.fromType(Runnable.class, false)).isEqualTo(PropertyKind.UNSUPPORTED);
    }

    @Test
    void supportedFlag() {
        assertThat(PropertyKind.STRING.isSupported()).isTrue();
        assertThat(PropertyKind.UNSUPPORTED.isSupported()).isFalse();
    }
}
