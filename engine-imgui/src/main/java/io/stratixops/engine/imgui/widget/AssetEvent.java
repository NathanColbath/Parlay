package io.stratixops.engine.imgui.widget;

/**
 * A named editor action produced by an asset view for the host to run
 * (design system section 3.4 keyboard map). The widget kit stays
 * domain-agnostic, so rename/delete/open are reported, not performed here;
 * {@code engine-editor} wires them to the project model.
 */
public record AssetEvent(Action action, String id) {

    public enum Action {
        NONE,
        OPEN,
        RENAME,
        DELETE,
        NAVIGATE_UP
    }

    /** A no-op event. */
    public static final AssetEvent NONE = new AssetEvent(Action.NONE, null);

    /** @return an event for an action against one item. */
    public static AssetEvent of(Action action, String id) {
        return new AssetEvent(action, id);
    }

    /** @return whether this event carries a real action. */
    public boolean isPresent() {
        return action != Action.NONE;
    }
}
