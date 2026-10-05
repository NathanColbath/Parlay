package io.stratixops.engine.editor.ui;

import io.stratixops.engine.editor.DockLayout;
import io.stratixops.engine.editor.PanelKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class EditorLayoutStoreTest {

    @TempDir
    Path directory;

    @Test
    void savedLayoutReloadsIdentically() {
        EditorLayoutStore store = new EditorLayoutStore(directory.resolve("layout.txt"));
        DockLayout original = DockLayout.defaultLayout();
        original.close(PanelKind.GAME);
        original.place(PanelKind.CONSOLE, DockLayout.Area.RIGHT);

        assertThat(store.save(original)).isNull();
        DockLayout restored = store.load(DockLayout.defaultLayout());

        assertThat(restored).isEqualTo(original);
    }

    @Test
    void missingOrDisabledStoreReturnsFallback() {
        EditorLayoutStore disabled = new EditorLayoutStore(null);
        DockLayout fallback = DockLayout.defaultLayout();
        assertThat(disabled.load(fallback)).isSameAs(fallback);
        assertThat(disabled.save(fallback)).isNull();

        EditorLayoutStore missing = new EditorLayoutStore(directory.resolve("nope.txt"));
        assertThat(missing.load(fallback)).isEqualTo(fallback);
    }

    @Test
    void corruptLayoutFallsBackInsteadOfThrowing() throws Exception {
        Path file = directory.resolve("corrupt.txt");
        Files.writeString(file, "not a layout");
        EditorLayoutStore store = new EditorLayoutStore(file);
        DockLayout fallback = DockLayout.defaultLayout();
        assertThat(store.load(fallback)).isEqualTo(fallback);
    }
}
