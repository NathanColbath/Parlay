package io.stratixops.gemdash;

import io.stratixops.engine.TextureAtlas;

/** Minimal sample-owned loader used only to type-check the doc snippet. */
final class LevelLoader {

    private LevelLoader() {
    }

    static Level load(String path, TextureAtlas atlas) {
        return new Level();
    }
}
