package io.stratixops.engine.serialize;

import com.google.gson.JsonObject;
import io.stratixops.engine.scene.Guid;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetaFileTest {

    @Test
    void roundTripsThroughJson() {
        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("filter", "nearest");
        settings.put("mipmaps", false);
        Guid guid = Guid.random();
        MetaFile meta = new MetaFile(guid, "png", 3, settings);

        MetaFile parsed = MetaFile.fromJson(meta.toJson(), "test");
        assertThat(parsed.guid()).isEqualTo(guid);
        assertThat(parsed.importer()).isEqualTo("png");
        assertThat(parsed.importerVersion()).isEqualTo(3);
        assertThat(parsed.settings()).containsEntry("filter", "nearest");
        assertThat(parsed.settings()).containsEntry("mipmaps", false);
    }

    @Test
    void defaultsWhenOptionalFieldsAreMissing() {
        JsonObject json = new JsonObject();
        json.addProperty("guid", Guid.random().value());
        MetaFile meta = MetaFile.fromJson(json, "test");
        assertThat(meta.importer()).isEqualTo("raw");
        assertThat(meta.importerVersion()).isZero();
        assertThat(meta.settings()).isEmpty();
    }

    @Test
    void missingGuidFailsFast() {
        assertThatThrownBy(() -> MetaFile.fromJson(new JsonObject(), "test"))
                .isInstanceOf(SerializationException.class)
                .hasMessageContaining("guid");
    }
}
