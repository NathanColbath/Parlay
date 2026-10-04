package io.stratixops.gemdash;

import io.stratixops.engine.Renderer;
import io.stratixops.engine.math.Vec2;

/** Minimal sample-owned level used only to type-check the doc snippet. */
final class Level {

    private final Vec2 spawn = new Vec2(32f, 32f);

    Vec2 spawn() {
        return spawn;
    }

    void update(float dt, Player player) {
    }

    void render(Renderer r, float alpha) {
    }

    boolean portalActive() {
        return false;
    }

    boolean playerAtPortal(Player player) {
        return false;
    }

    float timeLeft() {
        return 90f;
    }

    int score() {
        return 0;
    }
}
