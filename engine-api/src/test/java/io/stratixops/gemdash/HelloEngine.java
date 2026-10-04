package io.stratixops.gemdash;

import io.stratixops.engine.EngineContext;
import io.stratixops.engine.GameLifecycle;
import io.stratixops.engine.Renderer;
import io.stratixops.engine.Sprite;

/**
 * The "hello sprite" example from {@code docs/design/02-api-ergonomics.md}
 * section 2. Kept here verbatim so the doc's compiling claim is checked by the
 * build (the test source set compiles even though this module ships no runtime).
 */
public final class HelloEngine implements GameLifecycle {

    private Sprite logo;

    @Override
    public void onCreate(EngineContext ctx) {
        logo = ctx.assets().sprite("images/logo.png"); // cached, fails fast
    }

    @Override
    public void onRender(Renderer r, float alpha) {
        r.clear(0x1B1B2FFF);
        r.draw(logo, 320, 180);         // centered by anchor
        r.drawText("Hello, engine!", 8, 8); // built-in font
    }
}
