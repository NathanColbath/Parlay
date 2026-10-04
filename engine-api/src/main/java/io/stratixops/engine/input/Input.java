package io.stratixops.engine.input;

import io.stratixops.engine.math.Vec2;

/**
 * Poll-based input. All queries are read during the fixed update; edge queries
 * are buffered so a press is never lost between frames.
 *
 * <h2>Axis semantics (frozen by the API contract)</h2>
 * <ul>
 *   <li>{@link #axis(String)} returns the movement direction for an action with
 *       magnitude preserved for analog sources (a gamepad stick keeps its
 *       deflection). Digital sources (keys / D-pad) are normalized as a group,
 *       so holding two orthogonal keys gives a unit-length diagonal and no
 *       diagonal speed boost. Contributions are summed and clamped to unit
 *       length. At rest it returns {@code (0,0)}.</li>
 *   <li>{@link #axisNormalized(String)} returns the same direction forced to
 *       length {@code 0} or {@code 1}: it discards analog magnitude. Use it when
 *       a game wants constant speed regardless of stick deflection.</li>
 * </ul>
 * Both methods return a per-frame reused {@link Vec2}; see
 * {@link Vec2} ownership notes.
 */
public interface Input {

    /**
     * Applies an action map and returns this input for chaining.
     *
     * @param map named action bindings
     * @return this input
     */
    Input bind(ActionMap map);

    /**
     * Whether an action was pressed this frame (buffered edge).
     *
     * @param action action name
     * @return true on the frame the action went down
     */
    boolean pressed(String action);

    /**
     * Whether a button action is currently held.
     *
     * @param action action name
     * @return true while held
     */
    boolean down(String action);

    /**
     * Movement direction with analog magnitude preserved; digital diagonals are
     * normalized. See the type-level documentation.
     *
     * @param action action name
     * @return a reused vector, {@code (0,0)} at rest
     */
    Vec2 axis(String action);

    /**
     * Movement direction forced to unit length (or zero at rest), discarding
     * analog magnitude.
     *
     * @param action action name
     * @return a reused vector, {@code (0,0)} at rest
     */
    Vec2 axisNormalized(String action);
}
