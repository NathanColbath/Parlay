package io.stratixops.engine.imgui.drag;

import java.util.List;

/**
 * The dragged selection shared across panels. {@code ids} are stable guid-like
 * keys; the panel that started the drag owns their meaning.
 *
 * @param sourcePanel identifier of the originating panel ({@code "hierarchy"},
 *                    {@code "project"}, {@code "breadcrumb"}, {@code "inspector"})
 * @param ids         one id for a single drag, several for a multi-select drag
 */
public record DragPayload(String sourcePanel, List<String> ids) {

    public DragPayload {
        sourcePanel = sourcePanel == null ? "" : sourcePanel;
        ids = ids == null ? List.of() : List.copyOf(ids);
    }

    /** @return a single-item payload. */
    public static DragPayload single(String sourcePanel, String id) {
        return new DragPayload(sourcePanel, List.of(id));
    }

    /** @return the number of dragged items. */
    public int count() {
        return ids.size();
    }

    /** @return whether more than one item is being dragged. */
    public boolean multi() {
        return ids.size() > 1;
    }

    /** @return the primary (first) dragged id, or {@code null} when empty. */
    public String primary() {
        return ids.isEmpty() ? null : ids.get(0);
    }

    /** @return whether this payload contains the given id. */
    public boolean contains(String id) {
        return ids.contains(id);
    }
}
