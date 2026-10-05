package io.stratixops.engine.serialize;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Importer for raster image assets. Defaults to crisp nearest-neighbour
 * filtering, matching the engine's pixel-art default.
 */
public final class PngImporter implements Importer {

    @Override
    public String id() {
        return "png";
    }

    @Override
    public int version() {
        return 1;
    }

    @Override
    public boolean supports(String extension) {
        return "png".equals(extension) || "jpg".equals(extension) || "jpeg".equals(extension);
    }

    @Override
    public Map<String, Object> defaultSettings() {
        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("filter", "nearest");
        settings.put("sprite", true);
        return settings;
    }
}
