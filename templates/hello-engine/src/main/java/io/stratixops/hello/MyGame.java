package io.stratixops.hello;

import io.stratixops.engine.EngineContext;
import io.stratixops.engine.GameLifecycle;
import io.stratixops.engine.Renderer;
import io.stratixops.engine.Sprite;

/**
 * The "Hello Engine" starter.
 *
 * <p>This is the only gameplay file you need to touch for the first three steps
 * of {@code docs/GETTING_STARTED.md}. It loads one image and draws it, centered,
 * with a line of text. Each {@code TODO} points at the guide step that changes it.
 */
public final class MyGame implements GameLifecycle {

    // TODO 1 (GETTING_STARTED step 5): point this at your own PNG.
    // The file lives in src/main/resources/, so "images/logo.png" means
    // src/main/resources/images/logo.png. The path is case-sensitive.
    private static final String LOGO = "images/logo.png";

    private Sprite logo;

    /** Called once after the window and GPU context exist. Load assets here. */
    @Override
    public void onCreate(EngineContext ctx) {
        // Cached. A missing path logs the exact path and draws a magenta
        // placeholder instead of crashing; registration/config errors throw.
        logo = ctx.assets().sprite(LOGO);
    }

    /**
     * Called every frame. Draw world content here.
     *
     * @param r     the renderer for this frame
     * @param alpha interpolation factor in [0,1) between the previous and
     *              current update; forwarded to {@code r.draw} for smooth motion
     */
    @Override
    public void onRender(Renderer r, float alpha) {
        r.clear(0x1B1B2FFF);

        // TODO 3 (GETTING_STARTED step 5): move/resize the sprite by changing
        // the x/y below. Coordinates are in logical pixels; (320, 180) is the
        // center of the default 640x360 viewport.
        r.draw(logo, 320, 180);

        // TODO 2 (GETTING_STARTED step 4): change this greeting.
        r.drawText("Hello, engine!", 8, 8);
    }
}
