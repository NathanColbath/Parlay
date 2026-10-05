package io.stratixops.engine.imgui.theme;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Typed access to the design-token document shipped as a module resource.
 *
 * <p>Token names themselves contain dots ({@code bg.canvas}), so the document is
 * flattened with {@code /} as the path separator: {@code color/dark/bg.canvas}.
 * Every accessor records the path it resolved, letting the test suite assert
 * that the theme consumes <em>every</em> leaf in {@code tokens.json} with no
 * token silently dropped (design system acceptance criterion AC1).
 */
public final class TokenDocument {

    private static final String SEP = "/";

    private final Map<String, Object> leaves;
    private final Set<String> consumed = new LinkedHashSet<>();

    private TokenDocument(Map<String, Object> leaves) {
        this.leaves = leaves;
    }

    /** Loads the document from a classpath resource such as {@code /.../tokens.json}. */
    public static TokenDocument fromResource(String resource) {
        try (InputStream in = TokenDocument.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("missing classpath resource: " + resource);
            }
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return fromJson(json);
        } catch (IOException e) {
            throw new IllegalStateException("failed to read classpath resource: " + resource, e);
        }
    }

    /** Parses an inline document (used by tests). */
    @SuppressWarnings("unchecked")
    public static TokenDocument fromJson(String json) {
        Map<String, Object> flat = new LinkedHashMap<>();
        flatten("", (Map<String, Object>) MiniJson.parse(json), flat);
        return new TokenDocument(flat);
    }

    public String string(String path) {
        consumed.add(path);
        Object value = require(path);
        if (value == null) {
            throw new IllegalArgumentException("token is null: " + path);
        }
        return String.valueOf(value);
    }

    public String optionalString(String path, String fallback) {
        if (!leaves.containsKey(path)) {
            return fallback;
        }
        consumed.add(path);
        return String.valueOf(leaves.get(path));
    }

    public float number(String path) {
        consumed.add(path);
        return toFloat(require(path), path);
    }

    public float optionalNumber(String path, float fallback) {
        if (!leaves.containsKey(path)) {
            return fallback;
        }
        consumed.add(path);
        return toFloat(leaves.get(path), path);
    }

    public int color(String path) {
        consumed.add(path);
        return Rgba.parse(string(path));
    }

    public float[] numberArray(String path) {
        consumed.add(path);
        Object value = require(path);
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("token is not an array: " + path);
        }
        float[] out = new float[list.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = toFloat(list.get(i), path + "[" + i + "]");
        }
        return out;
    }

    /** @return the set of token paths resolved so far. */
    public Set<String> consumedPaths() {
        return Collections.unmodifiableSet(consumed);
    }

    /** @return every leaf path in the document, excluding metadata. */
    public Set<String> leafPaths() {
        Set<String> out = new TreeSet<>(leaves.keySet());
        out.removeIf(p -> p.isEmpty() || p.equals("$schema")
                || p.equals("meta") || p.startsWith("meta/"));
        return out;
    }

    private static void flatten(String prefix, Map<String, Object> node, Map<String, Object> out) {
        for (Map.Entry<String, Object> entry : node.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + SEP + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) map;
                flatten(key, nested, out);
            } else {
                out.put(key, value);
            }
        }
    }

    private Object require(String path) {
        Object value = leaves.get(path);
        if (value == null) {
            throw new IllegalArgumentException("missing token: " + path);
        }
        return value;
    }

    private static float toFloat(Object value, String path) {
        if (value instanceof Number number) {
            return number.floatValue();
        }
        throw new IllegalArgumentException("token is not a number: " + path);
    }
}
