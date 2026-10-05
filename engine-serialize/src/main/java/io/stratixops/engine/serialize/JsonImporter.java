package io.stratixops.engine.serialize;

import java.util.Collections;
import java.util.Map;

/**
 * Importer for JSON data assets (atlases, level data).
 */
public final class JsonImporter implements Importer {

    @Override
    public String id() {
        return "json";
    }

    @Override
    public int version() {
        return 1;
    }

    @Override
    public boolean supports(String extension) {
        return "json".equals(extension);
    }

    @Override
    public Map<String, Object> defaultSettings() {
        return Collections.emptyMap();
    }
}
