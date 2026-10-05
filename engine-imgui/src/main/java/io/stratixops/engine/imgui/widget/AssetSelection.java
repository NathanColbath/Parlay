package io.stratixops.engine.imgui.widget;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Multi-select state shared by the Project grid and list (design system
 * section 3.4): click selects, Ctrl toggles, Shift extends from an anchor,
 * Ctrl+A selects all, arrows move a roving cursor. Kept free of ImGui so the
 * interaction rules are unit-testable.
 */
public final class AssetSelection {

    private final Set<String> selected = new LinkedHashSet<>();
    private String anchor;
    private String cursor;

    /** @return an unmodifiable view of the selected ids in selection order. */
    public Set<String> ids() {
        return Collections.unmodifiableSet(selected);
    }

    /** @return the number of selected items. */
    public int size() {
        return selected.size();
    }

    /** @return whether nothing is selected. */
    public boolean isEmpty() {
        return selected.isEmpty();
    }

    /** @return whether an id is selected. */
    public boolean contains(String id) {
        return id != null && selected.contains(id);
    }

    /** @return the keyboard cursor id, or {@code null}. */
    public String cursor() {
        return cursor;
    }

    /** @return the shift-range anchor id, or {@code null}. */
    public String anchor() {
        return anchor;
    }

    /** Clears the selection and cursor. */
    public void clear() {
        selected.clear();
        anchor = null;
        cursor = null;
    }

    /** Selects one item (plain click). */
    public void setSingle(String id) {
        selected.clear();
        if (id != null) {
            selected.add(id);
            cursor = id;
            anchor = id;
        }
    }

    /** Toggles one item (Ctrl+click). */
    public void toggle(String id) {
        if (id == null) {
            return;
        }
        if (!selected.remove(id)) {
            selected.add(id);
        }
        cursor = id;
        anchor = id;
    }

    /**
     * Applies a pointer gesture in the current ordered view.
     *
     * @param ordered the visible, ordered items
     * @param id      the clicked id
     * @param ctrl    Ctrl held (toggle)
     * @param shift   Shift held (extend from anchor)
     */
    public void click(List<AssetItem> ordered, String id, boolean ctrl, boolean shift) {
        if (shift) {
            rangeTo(ordered, id);
        } else if (ctrl) {
            toggle(id);
        } else {
            setSingle(id);
        }
    }

    /** Selects every item in the ordered view. */
    public void selectAll(List<AssetItem> ordered) {
        selected.clear();
        for (AssetItem item : ordered) {
            selected.add(item.id());
        }
        if (!ordered.isEmpty()) {
            cursor = ordered.get(ordered.size() - 1).id();
            anchor = ordered.get(0).id();
        }
    }

    /**
     * Selects the inclusive range from the anchor to {@code id}. When no anchor
     * exists the cursor (or the target itself) seeds the range.
     */
    public void rangeTo(List<AssetItem> ordered, String id) {
        int to = indexOf(ordered, id);
        if (to < 0) {
            setSingle(id);
            return;
        }
        int from = indexOf(ordered, anchor != null ? anchor : cursor);
        if (from < 0) {
            from = to;
        }
        selected.clear();
        int lo = Math.min(from, to);
        int hi = Math.max(from, to);
        for (int i = lo; i <= hi; i++) {
            selected.add(ordered.get(i).id());
        }
        cursor = id;
        if (anchor == null) {
            anchor = ordered.get(from).id();
        }
    }

    /**
     * Moves the cursor by {@code delta} positions in the ordered view and applies
     * the resulting selection. Shift extends; otherwise the selection collapses.
     *
     * @return the new cursor id, or the current one when nothing moves
     */
    public String move(List<AssetItem> ordered, int delta, boolean shift) {
        if (ordered.isEmpty()) {
            return null;
        }
        int current = indexOf(ordered, cursor);
        if (current < 0) {
            current = shift ? ordered.size() - 1 : 0;
        }
        int next = Math.max(0, Math.min(ordered.size() - 1, current + delta));
        String id = ordered.get(next).id();
        if (shift) {
            if (anchor == null) {
                anchor = ordered.get(current).id();
            }
            rangeTo(ordered, id);
        } else {
            setSingle(id);
        }
        return id;
    }

    private static int indexOf(List<AssetItem> ordered, String id) {
        if (id == null) {
            return -1;
        }
        for (int i = 0; i < ordered.size(); i++) {
            if (ordered.get(i).id().equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
