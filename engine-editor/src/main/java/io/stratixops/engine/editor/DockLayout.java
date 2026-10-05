package io.stratixops.engine.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The docking layout: which panels are docked in which area, their tab order
 * within an area, and which panels are closed. This is pure data so it can be
 * saved, restored, and tested without a display.
 */
public final class DockLayout {

    /** The dockable regions of the editor window (OOB layout, STR-20). */
    public enum Area {
        LEFT("left"),
        CENTER("center"),
        RIGHT("right"),
        BOTTOM("bottom");

        private final String id;

        Area(String id) {
            this.id = id;
        }

        /**
         * @return the stable persistence id
         */
        public String id() {
            return id;
        }

        /**
         * Resolves a persistence id.
         *
         * @param id the id
         * @return the area
         * @throws IllegalArgumentException when unknown
         */
        public static Area fromId(String id) {
            for (Area area : values()) {
                if (area.id.equalsIgnoreCase(id)) {
                    return area;
                }
            }
            throw new IllegalArgumentException("unknown dock area: " + id);
        }
    }

    private final Map<Area, List<PanelKind>> tabs = new EnumMap<>(Area.class);
    private final Set<PanelKind> hidden = EnumSet.noneOf(PanelKind.class);

    /**
     * Creates an empty layout with all panels hidden until placed.
     */
    public DockLayout() {
        for (Area area : Area.values()) {
            tabs.put(area, new ArrayList<>());
        }
    }

    /**
     * The out-of-the-box Unity-like default: Hierarchy left, Scene/Game center,
     * Inspector right, Project/Console bottom.
     *
     * @return a fresh default layout
     */
    public static DockLayout defaultLayout() {
        DockLayout layout = new DockLayout();
        layout.place(PanelKind.HIERARCHY, Area.LEFT);
        layout.place(PanelKind.SCENE, Area.CENTER);
        layout.place(PanelKind.GAME, Area.CENTER);
        layout.place(PanelKind.INSPECTOR, Area.RIGHT);
        layout.place(PanelKind.PROJECT, Area.BOTTOM);
        layout.place(PanelKind.CONSOLE, Area.BOTTOM);
        return layout;
    }

    /**
     * Docks a panel into an area, appending it as the last tab and making it
     * visible.
     *
     * @param kind the panel
     * @param area the target area
     */
    public void place(PanelKind kind, Area area) {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(area, "area");
        for (List<PanelKind> list : tabs.values()) {
            list.remove(kind);
        }
        tabs.get(area).add(kind);
        hidden.remove(kind);
    }

    /**
     * Removes a panel from its area and marks it closed.
     *
     * @param kind the panel
     */
    public void close(PanelKind kind) {
        for (List<PanelKind> list : tabs.values()) {
            list.remove(kind);
        }
        hidden.add(kind);
    }

    /**
     * Reopens a closed panel in its default area.
     *
     * @param kind the panel
     */
    public void open(PanelKind kind) {
        hidden.remove(kind);
        if (areaOf(kind) == null) {
            place(kind, defaultAreaOf(kind));
        }
    }

    /**
     * @param kind the panel
     * @return true when the panel is docked and not closed
     */
    public boolean isVisible(PanelKind kind) {
        return !hidden.contains(kind) && areaOf(kind) != null;
    }

    /**
     * @param kind the panel
     * @return the area it is docked in, or {@code null}
     */
    public Area areaOf(PanelKind kind) {
        for (Map.Entry<Area, List<PanelKind>> entry : tabs.entrySet()) {
            if (entry.getValue().contains(kind)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * @param area the area
     * @return the panels in tab order (unmodifiable)
     */
    public List<PanelKind> tabs(Area area) {
        return Collections.unmodifiableList(tabs.get(area));
    }

    /**
     * @return every docked panel, in area order then tab order
     */
    public List<PanelKind> panels() {
        List<PanelKind> all = new ArrayList<>();
        for (Area area : Area.values()) {
            all.addAll(tabs.get(area));
        }
        return all;
    }

    /**
     * @return the closed panels
     */
    public Set<PanelKind> hidden() {
        return Collections.unmodifiableSet(hidden);
    }

    private static Area defaultAreaOf(PanelKind kind) {
        return switch (kind) {
            case HIERARCHY -> Area.LEFT;
            case SCENE, GAME -> Area.CENTER;
            case INSPECTOR -> Area.RIGHT;
            case PROJECT, CONSOLE -> Area.BOTTOM;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DockLayout other)) {
            return false;
        }
        return tabs.equals(other.tabs) && hidden.equals(other.hidden);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tabs, hidden);
    }
}
