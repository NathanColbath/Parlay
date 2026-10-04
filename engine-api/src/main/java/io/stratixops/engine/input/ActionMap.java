package io.stratixops.engine.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable mapping from named actions to physical keys.
 *
 * <p>Semantics (frozen by the API contract):
 * <ul>
 *   <li>Registering an action more than once <strong>merges</strong> the
 *       bindings; it never replaces them. This is what lets
 *       {@code .axis("Move", WASD).axis("Move", arrows)} drive one action from
 *       both key groups.</li>
 *   <li>Binding the same key to the same action twice is idempotent.</li>
 *   <li>The same key may be bound to two different actions; both actions then
 *       observe it.</li>
 * </ul>
 *
 * <p>Build one with {@link #def()}.
 */
public final class ActionMap {

    private final Map<String, List<Key>> axisBindings;
    private final Map<String, List<Key>> buttonBindings;

    private ActionMap(Map<String, List<Key>> axes, Map<String, List<Key>> buttons) {
        this.axisBindings = axes;
        this.buttonBindings = buttons;
    }

    /**
     * Starts a new action map definition.
     *
     * @return a fresh builder
     */
    public static Builder def() {
        return new Builder();
    }

    /**
     * Keys bound to an axis action, in registration order.
     *
     * @param action action name
     * @return an immutable list, empty if the action is unknown
     */
    public List<Key> axisKeys(String action) {
        return axisBindings.getOrDefault(action, List.of());
    }

    /**
     * Keys bound to a button action, in registration order.
     *
     * @param action action name
     * @return an immutable list, empty if the action is unknown
     */
    public List<Key> buttonKeys(String action) {
        return buttonBindings.getOrDefault(action, List.of());
    }

    /** Fluent builder for an {@link ActionMap}. */
    public static final class Builder {

        private final Map<String, List<Key>> axes = new LinkedHashMap<>();
        private final Map<String, List<Key>> buttons = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Merges keys into an axis action. Repeated calls merge (append,
         * dedupe); they do not replace.
         *
         * @param action action name
         * @param keys   keys that contribute to the axis
         * @return this builder
         */
        public Builder axis(String action, Key... keys) {
            merge(axes, action, keys);
            return this;
        }

        /**
         * Merges keys into a button action. Repeated calls merge (append,
         * dedupe); they do not replace.
         *
         * @param action action name
         * @param keys   keys that trigger the button
         * @return this builder
         */
        public Builder button(String action, Key... keys) {
            merge(buttons, action, keys);
            return this;
        }

        /**
         * Finishes the map.
         *
         * @return an immutable action map
         */
        public ActionMap build() {
            return new ActionMap(immutableCopy(axes), immutableCopy(buttons));
        }

        private static void merge(Map<String, List<Key>> target, String action, Key[] keys) {
            List<Key> bound = target.computeIfAbsent(action, k -> new ArrayList<>());
            for (Key key : keys) {
                if (!bound.contains(key)) {
                    bound.add(key);
                }
            }
        }

        private static Map<String, List<Key>> immutableCopy(Map<String, List<Key>> source) {
            Map<String, List<Key>> copy = new LinkedHashMap<>();
            source.forEach((k, v) -> copy.put(k, List.copyOf(v)));
            return Collections.unmodifiableMap(copy);
        }
    }
}
