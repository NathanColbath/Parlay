package io.stratixops.engine.editor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes {@link DockLayout} as a small, diff-friendly text format so
 * the editor can offer savable layouts with no JSON dependency in this module.
 *
 * <pre>
 * editor-layout/1
 * left: HIERARCHY
 * center: SCENE,GAME
 * right: INSPECTOR
 * bottom: PROJECT,CONSOLE
 * hidden:
 * </pre>
 */
public final class LayoutIO {

    /** The current layout file schema id. */
    public static final String SCHEMA = "editor-layout/1";

    private LayoutIO() {
    }

    /**
     * Serializes a layout.
     *
     * @param layout the layout
     * @return the text form
     */
    public static String serialize(DockLayout layout) {
        StringBuilder out = new StringBuilder();
        out.append(SCHEMA).append('\n');
        for (DockLayout.Area area : DockLayout.Area.values()) {
            out.append(area.id()).append(':');
            List<PanelKind> tabs = layout.tabs(area);
            for (int i = 0; i < tabs.size(); i++) {
                if (i > 0) {
                    out.append(',');
                }
                out.append(tabs.get(i).name());
            }
            out.append('\n');
        }
        out.append("hidden:");
        boolean first = true;
        for (PanelKind kind : layout.hidden()) {
            if (!first) {
                out.append(',');
            }
            out.append(kind.name());
            first = false;
        }
        out.append('\n');
        return out.toString();
    }

    /**
     * Parses a layout.
     *
     * @param text the text form
     * @return the layout
     * @throws IllegalArgumentException when the schema header is missing
     */
    public static DockLayout parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("layout text is null");
        }
        String[] lines = text.split("\\r?\\n");
        if (lines.length == 0 || !SCHEMA.equals(lines[0].trim())) {
            throw new IllegalArgumentException("expected layout header " + SCHEMA);
        }
        DockLayout layout = new DockLayout();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }
            int colon = line.indexOf(':');
            if (colon < 0) {
                continue;
            }
            String key = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();
            if ("hidden".equalsIgnoreCase(key)) {
                for (String name : splitNames(value)) {
                    layout.close(PanelKind.valueOf(name));
                }
                continue;
            }
            DockLayout.Area area;
            try {
                area = DockLayout.Area.fromId(key);
            } catch (IllegalArgumentException unknown) {
                continue;
            }
            for (String name : splitNames(value)) {
                layout.place(PanelKind.valueOf(name), area);
            }
        }
        return layout;
    }

    private static List<String> splitNames(String value) {
        List<String> names = new ArrayList<>();
        if (value.isEmpty()) {
            return names;
        }
        for (String part : value.split(",")) {
            String name = part.trim();
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        return names;
    }

    /**
     * Saves a layout to disk.
     *
     * @param layout the layout
     * @param path   the destination
     * @throws IOException on write failure
     */
    public static void save(DockLayout layout, Path path) throws IOException {
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.writeString(path, serialize(layout), StandardCharsets.UTF_8);
    }

    /**
     * Loads a layout from disk.
     *
     * @param path the source
     * @return the layout
     * @throws IOException on read failure
     */
    public static DockLayout load(Path path) throws IOException {
        return parse(Files.readString(path, StandardCharsets.UTF_8));
    }
}
