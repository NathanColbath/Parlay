package io.stratixops.engine.serialize;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImporterRegistryTest {

    @Test
    void defaultsResolveKnownExtensions() {
        ImporterRegistry registry = ImporterRegistry.withDefaults();
        assertThat(registry.forExtension("png")).isInstanceOf(PngImporter.class);
        assertThat(registry.forExtension("jpg")).isInstanceOf(PngImporter.class);
        assertThat(registry.forExtension("wav")).isInstanceOf(AudioImporter.class);
        assertThat(registry.forExtension("ogg")).isInstanceOf(AudioImporter.class);
        assertThat(registry.forExtension("json")).isInstanceOf(JsonImporter.class);
        assertThat(registry.forExtension("xyz")).isNull();
        assertThat(registry.extensions()).contains("png", "wav", "json");
    }

    @Test
    void importersReportIdentityAndSettings() {
        PngImporter png = new PngImporter();
        assertThat(png.id()).isEqualTo("png");
        assertThat(png.version()).isEqualTo(1);
        assertThat(png.supports("png")).isTrue();
        assertThat(png.supports("wav")).isFalse();
        assertThat(png.defaultSettings()).containsKey("filter");

        AudioImporter audio = new AudioImporter();
        assertThat(audio.supports("mp3")).isTrue();
        assertThat(audio.defaultSettings()).containsKey("volume");

        JsonImporter json = new JsonImporter();
        assertThat(json.supports("json")).isTrue();
        assertThat(json.defaultSettings()).isEmpty();
    }
}
