package io.stratixops.engine.render;

import io.stratixops.engine.Assets;
import io.stratixops.engine.Font;
import io.stratixops.engine.Sprite;
import io.stratixops.engine.TextureAtlas;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

/**
 * Desktop {@link Assets} implementation: classpath-first (then filesystem)
 * resolution, decode-once caching, {@code stb_image} texture decoding, atlas
 * metadata parsing and the built-in bitmap font (spec section 5.3).
 *
 * <p>Missing runtime content degrades: a missing sprite resolves to a 1x1
 * magenta placeholder and logs the path. Malformed atlas metadata is a
 * configuration error and fails fast.
 *
 * <p>Must be created with a current GL context and disposed before the context
 * is destroyed.
 */
public final class DesktopAssets implements Assets, AutoCloseable {

    private static final Logger LOG = Logger.getLogger(DesktopAssets.class.getName());
    private static final int MAGENTA = 0xFFFF00FF;

    private final Map<String, GlSprite> spriteCache = new LinkedHashMap<>();
    private final Map<String, GlTextureAtlas> atlasCache = new LinkedHashMap<>();
    private final BitmapFont builtInFont;
    private GlTexture magentaTexture;
    private GlSprite magentaSprite;

    /**
     * Creates the asset cache and rasterizes the built-in font. Requires a
     * current GL context.
     */
    public DesktopAssets() {
        this.builtInFont = BitmapFont.builtIn();
    }

    BitmapFont builtInFont() {
        return builtInFont;
    }

    @Override
    public Sprite sprite(String path) {
        GlSprite cached = spriteCache.get(path);
        if (cached != null) {
            return cached;
        }
        GlTexture texture = decodeTexture(path);
        GlSprite sprite;
        if (texture == null) {
            LOG.warning("Missing or undecodable sprite '" + path + "'; using magenta placeholder");
            sprite = magenta();
        } else {
            sprite = GlSprite.whole(texture);
        }
        spriteCache.put(path, sprite);
        return sprite;
    }

    @Override
    public TextureAtlas atlas(String path) {
        GlTextureAtlas cached = atlasCache.get(path);
        if (cached != null) {
            return cached;
        }
        String json = readText(path);
        if (json == null) {
            LOG.warning("Missing atlas metadata '" + path + "'; using empty atlas (magenta regions)");
            GlTextureAtlas empty = new GlTextureAtlas(magentaTexture(), new AtlasMetadata("", Map.of()), magenta());
            atlasCache.put(path, empty);
            return empty;
        }
        AtlasMetadata metadata = AtlasReader.parse(json);
        String imagePath = resolveSibling(path, metadata.image());
        GlTexture texture = decodeTexture(imagePath);
        if (texture == null) {
            LOG.warning("Missing atlas image '" + imagePath + "' for '" + path + "'; using magenta regions");
            GlTextureAtlas empty = new GlTextureAtlas(magentaTexture(), metadata, magenta());
            atlasCache.put(path, empty);
            return empty;
        }
        GlTextureAtlas atlas = new GlTextureAtlas(texture, metadata, magenta());
        atlasCache.put(path, atlas);
        return atlas;
    }

    @Override
    public Font font(String path) {
        // The MVP ships exactly one built-in font; any path resolves to it and a
        // missing font file cannot fail the game (spec section 5.3).
        return builtInFont;
    }

    @Override
    public void close() {
        for (GlTextureAtlas atlas : atlasCache.values()) {
            atlas.dispose();
        }
        atlasCache.clear();
        for (GlSprite sprite : spriteCache.values()) {
            if (sprite != magentaSprite && sprite.texture() != magentaTexture) {
                sprite.texture().close();
            }
        }
        spriteCache.clear();
        builtInFont.dispose();
        if (magentaTexture != null) {
            magentaTexture.close();
            magentaTexture = null;
            magentaSprite = null;
        }
    }

    private GlSprite magenta() {
        if (magentaSprite == null) {
            magentaTexture = GlTexture.fromPixels(new int[] {MAGENTA}, 1, 1, true);
            magentaSprite = GlSprite.whole(magentaTexture);
        }
        return magentaSprite;
    }

    private GlTexture magentaTexture() {
        magenta();
        return magentaTexture;
    }

    private GlTexture decodeTexture(String path) {
        byte[] bytes = readBytes(path);
        if (bytes == null) {
            return null;
        }
        ByteBuffer input = MemoryUtil.memAlloc(bytes.length);
        try {
            input.put(bytes).flip();
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);
                IntBuffer channels = stack.mallocInt(1);
                ByteBuffer pixels = STBImage.stbi_load_from_memory(input, width, height, channels, 4);
                if (pixels == null) {
                    LOG.warning("Failed to decode '" + path + "': " + STBImage.stbi_failure_reason());
                    return null;
                }
                try {
                    return GlTexture.fromRgba(pixels, width.get(0), height.get(0), true);
                } finally {
                    STBImage.stbi_image_free(pixels);
                }
            }
        } finally {
            MemoryUtil.memFree(input);
        }
    }

    private static byte[] readBytes(String path) {
        InputStream stream = DesktopAssets.class.getClassLoader().getResourceAsStream(stripLeadingSlash(path));
        if (stream != null) {
            try (InputStream in = stream) {
                return in.readAllBytes();
            } catch (IOException e) {
                LOG.warning("Failed to read classpath resource '" + path + "': " + e.getMessage());
                return null;
            }
        }
        Path file = Paths.get(path);
        if (Files.isReadable(file)) {
            try {
                return Files.readAllBytes(file);
            } catch (IOException e) {
                LOG.warning("Failed to read file '" + path + "': " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    private static String readText(String path) {
        byte[] bytes = readBytes(path);
        return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
    }

    private static String resolveSibling(String resourcePath, String sibling) {
        if (sibling == null || sibling.isEmpty()) {
            return sibling;
        }
        if (sibling.startsWith("/") || sibling.contains(":")) {
            return stripLeadingSlash(sibling);
        }
        int slash = resourcePath.lastIndexOf('/');
        if (slash < 0) {
            return sibling;
        }
        return resourcePath.substring(0, slash + 1) + sibling;
    }

    private static String stripLeadingSlash(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
