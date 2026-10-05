package io.stratixops.engine.testutil;

import io.stratixops.engine.input.Key;
import io.stratixops.engine.spi.InputDevice;
import java.util.EnumSet;
import java.util.Set;

/**
 * Headless {@link InputDevice} fake. Edges accumulate between {@link #poll()}
 * calls and become the current frame's edges, matching the SPI contract; a
 * press+release before a poll is therefore never lost.
 */
public final class FakeInputDevice implements InputDevice {

    private final Set<Key> down = EnumSet.noneOf(Key.class);
    private final Set<Key> pressed = EnumSet.noneOf(Key.class);
    private final Set<Key> released = EnumSet.noneOf(Key.class);
    private final Set<Key> pendingPressed = EnumSet.noneOf(Key.class);
    private final Set<Key> pendingReleased = EnumSet.noneOf(Key.class);

    private int pollCount;

    /** Marks a key as newly pressed (edge + held). */
    public void press(Key key) {
        pendingPressed.add(key);
        down.add(key);
    }

    /** Marks a key as released (edge). */
    public void release(Key key) {
        pendingReleased.add(key);
        down.remove(key);
    }

    /** Holds a key down without producing a press edge. */
    public void hold(Key key) {
        down.add(key);
    }

    /** Releases a key silently, without producing a release edge. */
    public void clear(Key key) {
        down.remove(key);
    }

    /** Number of times {@link #poll()} has been called. */
    public int pollCount() {
        return pollCount;
    }

    @Override
    public void poll() {
        pollCount++;
        pressed.clear();
        released.clear();
        pressed.addAll(pendingPressed);
        released.addAll(pendingReleased);
        pendingPressed.clear();
        pendingReleased.clear();
    }

    @Override
    public boolean isKeyDown(Key key) {
        return down.contains(key);
    }

    @Override
    public boolean wasKeyPressed(Key key) {
        return pressed.contains(key);
    }

    @Override
    public boolean wasKeyReleased(Key key) {
        return released.contains(key);
    }
}
