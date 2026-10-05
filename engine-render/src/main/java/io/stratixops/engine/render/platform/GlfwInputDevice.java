package io.stratixops.engine.render.platform;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_M;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;

import io.stratixops.engine.input.Key;
import io.stratixops.engine.spi.InputDevice;
import java.util.Arrays;
import org.lwjgl.glfw.GLFWKeyCallbackI;

/**
 * GLFW implementation of {@link InputDevice}. A key callback records down state
 * and buffers press/release edges; {@link #poll()} (called once per frame after
 * {@code glfwPollEvents}) publishes them for querying.
 *
 * <p>Only the keys the MVP can bind are mapped ({@link Key}); other keys are
 * ignored. Key repeat is filtered out.
 */
public final class GlfwInputDevice implements InputDevice, AutoCloseable {

    private static final int KEY_COUNT = Key.values().length;

    private final long window;
    private final boolean[] down = new boolean[KEY_COUNT];
    private final boolean[] pressedPending = new boolean[KEY_COUNT];
    private final boolean[] releasedPending = new boolean[KEY_COUNT];
    private final boolean[] pressedFrame = new boolean[KEY_COUNT];
    private final boolean[] releasedFrame = new boolean[KEY_COUNT];
    private final GLFWKeyCallbackI keyCallback;
    private boolean closed;

    /**
     * Registers the key callback on a window.
     *
     * @param window the window to read input from
     */
    public GlfwInputDevice(GlfwWindow window) {
        this.window = window.handle();
        keyCallback = (win, key, scancode, action, mods) -> {
            Key mapped = map(key);
            if (mapped == null) {
                return;
            }
            int index = mapped.ordinal();
            if (action == GLFW_PRESS) {
                down[index] = true;
                pressedPending[index] = true;
            } else if (action == GLFW_RELEASE) {
                down[index] = false;
                releasedPending[index] = true;
            }
        };
        glfwSetKeyCallback(this.window, keyCallback);
    }

    @Override
    public void poll() {
        System.arraycopy(pressedPending, 0, pressedFrame, 0, KEY_COUNT);
        System.arraycopy(releasedPending, 0, releasedFrame, 0, KEY_COUNT);
        Arrays.fill(pressedPending, false);
        Arrays.fill(releasedPending, false);
    }

    @Override
    public boolean isKeyDown(Key key) {
        return down[key.ordinal()];
    }

    @Override
    public boolean wasKeyPressed(Key key) {
        return pressedFrame[key.ordinal()];
    }

    @Override
    public boolean wasKeyReleased(Key key) {
        return releasedFrame[key.ordinal()];
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        glfwSetKeyCallback(window, null);
    }

    private static Key map(int glfwKey) {
        switch (glfwKey) {
            case GLFW_KEY_W:
                return Key.W;
            case GLFW_KEY_A:
                return Key.A;
            case GLFW_KEY_S:
                return Key.S;
            case GLFW_KEY_D:
                return Key.D;
            case GLFW_KEY_UP:
                return Key.UP;
            case GLFW_KEY_LEFT:
                return Key.LEFT;
            case GLFW_KEY_DOWN:
                return Key.DOWN;
            case GLFW_KEY_RIGHT:
                return Key.RIGHT;
            case GLFW_KEY_SPACE:
                return Key.SPACE;
            case GLFW_KEY_ENTER:
                return Key.ENTER;
            case GLFW_KEY_ESCAPE:
                return Key.ESCAPE;
            case GLFW_KEY_P:
                return Key.P;
            case GLFW_KEY_M:
                return Key.M;
            default:
                return null;
        }
    }
}
