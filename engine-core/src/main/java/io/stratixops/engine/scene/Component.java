package io.stratixops.engine.scene;

/**
 * Base class for everything attachable to a {@link GameObject}.
 *
 * <p>Lifecycle mirrors Unity and is driven by the owning {@link GameObject}
 * (via its {@link Scene}): {@code awake -> onEnable -> start ->
 * (fixedUpdate)* -> update -> lateUpdate -> onDisable -> onDestroy}. Callbacks
 * are only invoked on the main thread; override the ones you need.
 *
 * <p>A component is {@code enabled} by default. Disabling it stops
 * {@code update}/{@code fixedUpdate}/{@code lateUpdate} but keeps the component
 * attached.
 */
public abstract class Component {

    private GameObject gameObject;
    private boolean enabled = true;
    private boolean awakeCalled;
    private boolean started;
    private boolean destroyed;

    /**
     * The object this component is attached to.
     *
     * @return the owning {@link GameObject}, or {@code null} before attachment
     */
    public final GameObject gameObject() {
        return gameObject;
    }

    /**
     * Whether this component is enabled.
     *
     * @return true when enabled
     */
    public final boolean enabled() {
        return enabled;
    }

    /**
     * Enables or disables the component, firing {@link #onEnable()} /
     * {@link #onDisable()} once the component has awoken.
     *
     * @param value the new enabled state
     */
    public final void setEnabled(boolean value) {
        if (this.enabled == value) {
            return;
        }
        this.enabled = value;
        if (destroyed || !awakeCalled) {
            return;
        }
        if (value) {
            onEnable();
        } else {
            onDisable();
        }
    }

    /**
     * Called once when the component becomes active, before {@link #start()}.
     */
    public void awake() {
    }

    /**
     * Called when the component becomes enabled.
     */
    public void onEnable() {
    }

    /**
     * Called once before the first {@link #update(float)}.
     */
    public void start() {
    }

    /**
     * Fixed-step update (engine default 1/60 s). Gameplay rules belong here.
     *
     * @param dt seconds elapsed
     */
    public void update(float dt) {
    }

    /**
     * Per-frame update after all {@link #update(float)} calls.
     *
     * @param dt seconds elapsed
     */
    public void lateUpdate(float dt) {
    }

    /**
     * Fixed-timestep update for physics-like work.
     *
     * @param dt fixed seconds step
     */
    public void fixedUpdate(float dt) {
    }

    /**
     * Called when the component becomes disabled.
     */
    public void onDisable() {
    }

    /**
     * Called once when the component (or its object) is destroyed.
     */
    public void onDestroy() {
    }

    // ------------------------------------------------------------------
    // Lifecycle driving — package-private so only GameObject/Scene call it.
    // ------------------------------------------------------------------

    /** @return true once {@link #awake()} has run. */
    final boolean awakeCalled() {
        return awakeCalled;
    }

    /** @return true once {@link #start()} has run. */
    final boolean started() {
        return started;
    }

    final void attach(GameObject owner) {
        this.gameObject = owner;
    }

    final void detach() {
        this.gameObject = null;
    }

    final void driveAwake() {
        if (awakeCalled) {
            return;
        }
        awakeCalled = true;
        awake();
        if (enabled) {
            onEnable();
        }
    }

    final void driveStart() {
        if (started || !awakeCalled) {
            return;
        }
        started = true;
        if (enabled) {
            start();
        }
    }

    final void driveUpdate(float dt) {
        if (enabled) {
            update(dt);
        }
    }

    final void driveLateUpdate(float dt) {
        if (enabled) {
            lateUpdate(dt);
        }
    }

    final void driveFixedUpdate(float dt) {
        if (enabled) {
            fixedUpdate(dt);
        }
    }

    final void driveDestroy() {
        if (destroyed) {
            return;
        }
        destroyed = true;
        if (awakeCalled) {
            if (enabled) {
                onDisable();
            }
            onDestroy();
        }
    }
}
