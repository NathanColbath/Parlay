package io.stratixops.engine.editor.ui;

import io.stratixops.engine.editor.DockLayout;
import io.stratixops.engine.editor.LayoutIO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persists and restores the docking layout through {@link LayoutIO} (STR-40).
 *
 * <p>The store is optional: with a {@code null} file it is a no-op, which keeps
 * the editor host headless and CI-safe. Failures are reported through the
 * console rather than thrown into the render loop.
 */
public final class EditorLayoutStore {

    private final Path file;

    /**
     * @param file where the layout is saved, or {@code null} to disable
     *             persistence
     */
    public EditorLayoutStore(Path file) {
        this.file = file == null ? null : file.toAbsolutePath().normalize();
    }

    /** @return the layout file, or {@code null} when persistence is disabled. */
    public Path file() {
        return file;
    }

    /**
     * Saves a layout.
     *
     * @param layout the layout
     * @return an error message, or {@code null} on success / disabled store
     */
    public String save(DockLayout layout) {
        if (file == null) {
            return null;
        }
        try {
            LayoutIO.save(layout, file);
            return null;
        } catch (IOException e) {
            return "Could not save layout: " + e.getMessage();
        }
    }

    /**
     * Loads a layout, falling back to the supplied default.
     *
     * @param fallback the layout to use when no file exists or it is invalid
     * @return the restored layout
     */
    public DockLayout load(DockLayout fallback) {
        if (file == null || !Files.exists(file)) {
            return fallback;
        }
        try {
            return LayoutIO.load(file);
        } catch (IOException | IllegalArgumentException e) {
            return fallback;
        }
    }
}
