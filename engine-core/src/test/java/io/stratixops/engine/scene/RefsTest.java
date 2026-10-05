package io.stratixops.engine.scene;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RefsTest {

    @Test
    void assetRefHoldsAndComparesGuid() {
        Guid guid = Guid.random();
        AssetRef ref = AssetRef.of(guid);
        assertThat(ref.guid()).isEqualTo(guid);
        assertThat(ref.isSet()).isTrue();
        assertThat(ref).isEqualTo(new AssetRef(guid));
        assertThat(ref).hasSameHashCodeAs(new AssetRef(guid));
        assertThat(ref).isNotEqualTo(new AssetRef(Guid.random()));
        assertThat(ref.toString()).contains(guid.value());
    }

    @Test
    void emptyAssetRefIsUnset() {
        AssetRef empty = new AssetRef(null);
        assertThat(empty.isSet()).isFalse();
        assertThat(empty.toString()).contains("none");
        assertThat(empty).isEqualTo(new AssetRef(null));
    }

    @Test
    void objectRefBehavesSimilarly() {
        Guid guid = Guid.random();
        ObjectRef ref = ObjectRef.of(guid);
        assertThat(ref.guid()).isEqualTo(guid);
        assertThat(ref.isSet()).isTrue();
        assertThat(ref).isEqualTo(new ObjectRef(guid));
        assertThat(new ObjectRef(null).isSet()).isFalse();
        assertThat(new ObjectRef(null)).isEqualTo(new ObjectRef(null));
    }

    @Test
    void prefabRefBehavesSimilarly() {
        Guid guid = Guid.random();
        PrefabRef ref = PrefabRef.of(guid);
        assertThat(ref.guid()).isEqualTo(guid);
        assertThat(ref.isSet()).isTrue();
        assertThat(ref).isEqualTo(new PrefabRef(guid));
        assertThat(new PrefabRef(null).isSet()).isFalse();
        assertThat(new PrefabRef(null)).isEqualTo(new PrefabRef(null));
    }
}
