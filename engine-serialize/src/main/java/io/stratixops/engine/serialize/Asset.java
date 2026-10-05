package io.stratixops.engine.serialize;

import io.stratixops.engine.scene.Guid;

import java.nio.file.Path;

/**
 * An imported asset: its canonical GUID, its current filesystem location, and
 * the parsed {@code .meta} sidecar that owns its identity and importer
 * settings.
 */
public record Asset(Guid guid, Path path, MetaFile meta) {
}
