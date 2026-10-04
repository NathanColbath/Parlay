package io.stratixops.engine;

/**
 * A packed texture plus named sub-regions. Requested once from
 * {@link Assets#atlas(String)} and shared.
 */
public interface TextureAtlas {

    /**
     * Looks up a named region.
     *
     * @param name region name from the atlas metadata
     * @return the region as a sprite
     */
    Sprite region(String name);
}
