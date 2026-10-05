package io.stratixops.engine.serialize;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Importer for audio assets (WAV one-shots, OGG/MP3 streamed music).
 */
public final class AudioImporter implements Importer {

    @Override
    public String id() {
        return "audio";
    }

    @Override
    public int version() {
        return 1;
    }

    @Override
    public boolean supports(String extension) {
        return "wav".equals(extension) || "ogg".equals(extension) || "mp3".equals(extension);
    }

    @Override
    public Map<String, Object> defaultSettings() {
        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("stream", false);
        settings.put("volume", 1.0);
        return settings;
    }
}
