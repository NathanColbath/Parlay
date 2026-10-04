package io.stratixops.gemdash;

import io.stratixops.engine.Renderer;
import io.stratixops.engine.TextureAtlas;
import io.stratixops.engine.input.Input;
import io.stratixops.engine.math.Vec2;

/** Minimal sample-owned player used only to type-check the doc snippet. */
final class Player {

    private final Vec2 position = new Vec2();

    Player(TextureAtlas atlas, Vec2 spawn) {
        position.set(spawn);
    }

    void update(float dt, Vec2 move, Level level, Input input) {
    }

    void render(Renderer r, float alpha) {
    }

    Vec2 position() {
        return position;
    }

    int hearts() {
        return 3;
    }
}
