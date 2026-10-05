package io.stratixops.engine.input;

import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.spi.InputDevice;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Default {@link Input} implementation over the backend {@link InputDevice} SPI.
 *
 * <p>{@link #beginFrame()} polls the device once per frame; the game then
 * queries actions during its fixed updates. Buffering of press/release edges
 * lives in the device, so a tap between polls is never lost. Multiple fixed
 * updates in one frame all observe the same edges.
 *
 * <p>Axis semantics are those frozen by ADR-0001: contributions from bound keys
 * are summed, and a sum longer than one is clamped to unit length so a digital
 * diagonal has no speed boost. The engine currently maps only keyboard keys, so
 * analog magnitude is not yet produced; the clamp is written so adding a
 * magnitude-preserving source later needs no API change.
 */
public final class DefaultInput implements Input {

    private final InputDevice device;
    private final List<ActionMap> maps = new ArrayList<>();

    private final Vec2 axisScratch = new Vec2();
    private final Vec2 axisNormalizedScratch = new Vec2();

    /**
     * Creates input over the given device.
     *
     * @param device raw keyboard device
     */
    public DefaultInput(InputDevice device) {
        this.device = Objects.requireNonNull(device, "device");
    }

    /**
     * Polls the device once for the current frame. Call before updates.
     */
    public void beginFrame() {
        device.poll();
    }

    @Override
    public Input bind(ActionMap map) {
        if (map != null) {
            maps.add(map);
        }
        return this;
    }

    @Override
    public boolean pressed(String action) {
        for (int i = 0; i < maps.size(); i++) {
            for (Key key : maps.get(i).buttonKeys(action)) {
                if (device.wasKeyPressed(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean down(String action) {
        for (int i = 0; i < maps.size(); i++) {
            for (Key key : maps.get(i).buttonKeys(action)) {
                if (device.isKeyDown(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Vec2 axis(String action) {
        sumAxis(action, axisScratch);
        if (axisScratch.lengthSquared() > 1f) {
            axisScratch.normalize();
        }
        return axisScratch;
    }

    @Override
    public Vec2 axisNormalized(String action) {
        sumAxis(action, axisNormalizedScratch);
        if (axisNormalizedScratch.lengthSquared() > 1e-12f) {
            axisNormalizedScratch.normalize();
        } else {
            axisNormalizedScratch.set(0f, 0f);
        }
        return axisNormalizedScratch;
    }

    private void sumAxis(String action, Vec2 out) {
        float x = 0f;
        float y = 0f;
        for (int i = 0; i < maps.size(); i++) {
            for (Key key : maps.get(i).axisKeys(action)) {
                if (device.isKeyDown(key)) {
                    x += dx(key);
                    y += dy(key);
                }
            }
        }
        out.set(x, y);
    }

    private static float dx(Key key) {
        switch (key) {
            case A:
            case LEFT:
                return -1f;
            case D:
            case RIGHT:
                return 1f;
            default:
                return 0f;
        }
    }

    private static float dy(Key key) {
        // World space is y-down (ADR-0001), so "up" is negative y.
        switch (key) {
            case W:
            case UP:
                return -1f;
            case S:
            case DOWN:
                return 1f;
            default:
                return 0f;
        }
    }
}
