package io.stratixops.engine.serialize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Low-level JSON I/O shared by the serializers: deterministic serialization
 * (stable key order, 2-space indent, LF newlines, no HTML escaping) and atomic
 * writes with a single {@code .bak} backup (ADR-0006 §8/§9).
 */
final class JsonFiles {

    static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .create();

    private JsonFiles() {
    }

    static String toJson(JsonElement element) {
        return GSON.toJson(element);
    }

    static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SerializationException("cannot read " + path, e);
        }
    }

    static JsonObject parseObject(Path path) {
        String text = read(path);
        JsonElement parsed;
        try {
            parsed = JsonParser.parseString(text);
        } catch (RuntimeException e) {
            throw new SerializationException("malformed JSON in " + path, e);
        }
        if (!parsed.isJsonObject()) {
            throw new SerializationException("expected a JSON object at the root of " + path);
        }
        return parsed.getAsJsonObject();
    }

    static void writeAtomic(Path target, JsonElement element) {
        writeAtomicText(target, toJson(element));
    }

    static void writeAtomicText(Path target, String content) {
        Path absolute = target.toAbsolutePath().normalize();
        Path directory = absolute.getParent();
        try {
            if (directory != null) {
                Files.createDirectories(directory);
            }
            if (Files.exists(absolute)) {
                Files.copy(absolute, absolute.resolveSibling(absolute.getFileName() + ".bak"),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            Path temp = Files.createTempFile(directory, absolute.getFileName().toString(), ".tmp");
            Files.writeString(temp, content, StandardCharsets.UTF_8);
            try {
                Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicFailure) {
                Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("cannot write " + absolute, e);
        }
    }
}
