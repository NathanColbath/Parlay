package io.stratixops.engine.serialize;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettingsIOTest {

    @Test
    void missingFileYieldsDefaults(@TempDir Path dir) {
        SettingsIO io = new SettingsIO();
        SettingsIO.WindowSettings settings = io.loadWindow(dir.resolve("missing.json"));
        assertThat(settings.title()).isEqualTo("Stratix Game");
        assertThat(settings.width()).isEqualTo(1280);
        assertThat(settings.height()).isEqualTo(720);
        assertThat(settings.vsync()).isTrue();
    }

    @Test
    void roundTripsWindowSettings(@TempDir Path dir) {
        SettingsIO io = new SettingsIO();
        Path file = dir.resolve("window.json");
        SettingsIO.WindowSettings settings =
                new SettingsIO.WindowSettings("Gem Dash", 640, 360, false);
        io.saveWindow(settings, file);

        SettingsIO.WindowSettings loaded = io.loadWindow(file);
        assertThat(loaded).isEqualTo(settings);
    }

    @Test
    void unsupportedVersionFailsFast(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("window.json");
        Files.writeString(file, "{\"schema\":\"stratix.settings.window\",\"version\":99,\"content\":{}}");
        SettingsIO io = new SettingsIO();
        assertThatThrownBy(() -> io.loadWindow(file)).isInstanceOf(SerializationException.class);
    }
}
