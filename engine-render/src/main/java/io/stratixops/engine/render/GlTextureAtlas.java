package io.stratixops.engine.render;

import io.stratixops.engine.Sprite;
import io.stratixops.engine.TextureAtlas;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A {@link TextureAtlas} backed by one GL texture. Regions resolve to
 * {@link GlSprite} handles that are cached on first use; unknown region names
 * degrade to a magenta placeholder and log the name (spec section 5.3).
 */
final class GlTextureAtlas implements TextureAtlas {

    private static final Logger LOG = Logger.getLogger(GlTextureAtlas.class.getName());

    private final GlTexture texture;
    private final AtlasMetadata metadata;
    private final Sprite placeholder;
    private final Map<String, GlSprite> cache = new LinkedHashMap<>();

    GlTextureAtlas(GlTexture texture, AtlasMetadata metadata, Sprite placeholder) {
        this.texture = texture;
        this.metadata = metadata;
        this.placeholder = placeholder;
    }

    GlTexture texture() {
        return texture;
    }

    @Override
    public Sprite region(String name) {
        GlSprite cached = cache.get(name);
        if (cached != null) {
            return cached;
        }
        AtlasRegion region = metadata.resolve(name, texture.width(), texture.height());
        if (region == null) {
            LOG.warning("Atlas region '" + name + "' is not defined; using magenta placeholder");
            return placeholder;
        }
        GlSprite sprite = GlSprite.ofRegion(texture, region);
        cache.put(name, sprite);
        return sprite;
    }

    void dispose() {
        texture.close();
    }
}
