package io.stratixops.engine.render;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads atlas metadata from the small engine JSON format:
 *
 * <pre>{@code
 * {
 *   "image": "atlas.png",
 *   "regions": {
 *     "player": { "x": 0, "y": 0, "w": 16, "h": 16 }
 *   }
 * }
 * }</pre>
 *
 * <p>Malformed metadata fails fast with an {@link IllegalArgumentException} that
 * names the offending key or value (spec section 5.3); missing region names are
 * handled by the caller as a content problem.
 */
public final class AtlasReader {

    private AtlasReader() {
    }

    /**
     * Parses atlas metadata.
     *
     * @param json the document text
     * @return the parsed metadata
     * @throws IllegalArgumentException if the document is malformed
     */
    public static AtlasMetadata parse(String json) {
        Object root = MiniJson.parse(json);
        if (!(root instanceof Map)) {
            throw new IllegalArgumentException("atlas metadata must be a JSON object");
        }
        Map<?, ?> rootMap = (Map<?, ?>) root;

        Object imageValue = rootMap.get("image");
        if (!(imageValue instanceof String) || ((String) imageValue).isEmpty()) {
            throw new IllegalArgumentException("atlas metadata needs a non-empty \"image\" string");
        }

        Object regionsValue = rootMap.get("regions");
        if (!(regionsValue instanceof Map)) {
            throw new IllegalArgumentException("atlas metadata needs a \"regions\" object");
        }

        Map<String, AtlasMetadata.Rect> regions = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) regionsValue).entrySet()) {
            String name = String.valueOf(entry.getKey());
            Object regionValue = entry.getValue();
            if (!(regionValue instanceof Map)) {
                throw new IllegalArgumentException("atlas region '" + name + "' must be an object");
            }
            Map<?, ?> region = (Map<?, ?>) regionValue;
            regions.put(name, new AtlasMetadata.Rect(
                intField(region, name, "x"),
                intField(region, name, "y"),
                intField(region, name, "w"),
                intField(region, name, "h")));
        }
        return new AtlasMetadata((String) imageValue, regions);
    }

    private static int intField(Map<?, ?> region, String name, String field) {
        Object value = region.get(field);
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException(
                "atlas region '" + name + "' is missing numeric field '" + field + "'");
        }
        double number = ((Number) value).doubleValue();
        if (number != Math.rint(number)) {
            throw new IllegalArgumentException(
                "atlas region '" + name + "' field '" + field + "' must be an integer but was " + number);
        }
        return (int) number;
    }
}
