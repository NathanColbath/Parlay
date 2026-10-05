package io.stratixops.engine.serialize;

import com.google.gson.JsonObject;
import io.stratixops.engine.scene.Guid;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The asset database (ADR-0006 §7): imports assets, manages {@code .meta}
 * sidecars, and resolves {@link Guid} to {@link Asset}.
 *
 * <p>Identity is GUID-based, never path-based. Renaming or moving an asset
 * together with its sidecar preserves its GUID — and therefore every reference
 * to it — which is exactly what {@link #moveAsset(Path, Path)} guarantees.
 */
public final class AssetDatabase {

    private final Path root;
    private final ImporterRegistry importers;
    private final Map<Guid, Asset> byGuid = new LinkedHashMap<>();
    private final Map<Path, Asset> byPath = new LinkedHashMap<>();

    /**
     * Creates a database over an assets root with the default importers.
     *
     * @param root the assets directory (may not exist yet)
     */
    public AssetDatabase(Path root) {
        this(root, ImporterRegistry.withDefaults());
    }

    /**
     * Creates a database over an assets root.
     *
     * @param root      the assets directory
     * @param importers the importer registry
     */
    public AssetDatabase(Path root, ImporterRegistry importers) {
        this.root = root.toAbsolutePath().normalize();
        this.importers = importers;
    }

    /**
     * @return the assets root
     */
    public Path root() {
        return root;
    }

    /**
     * @return the importer registry
     */
    public ImporterRegistry importers() {
        return importers;
    }

    /**
     * Imports an asset, creating a `.meta` sidecar (with a fresh GUID) when
     * none exists. The file itself need not exist yet for the sidecar to be
     * created, which makes import deterministic in tests and CI.
     *
     * @param asset path to the asset file
     * @return the imported asset
     */
    public Asset importAsset(Path asset) {
        Path absolute = asset.toAbsolutePath().normalize();
        Path metaPath = metaPath(absolute);
        MetaFile meta;
        if (Files.exists(metaPath)) {
            meta = MetaFile.fromJson(JsonFiles.parseObject(metaPath), metaPath.toString());
        } else {
            meta = createMeta(absolute);
            JsonFiles.writeAtomic(metaPath, meta.toJson());
        }
        Asset imported = new Asset(meta.guid(), absolute, meta);
        index(imported);
        return imported;
    }

    /**
     * Re-imports if the registered importer version differs from the sidecar's.
     * The GUID never changes (ADR-0006 §7).
     *
     * @param asset path to the asset file
     * @return the current asset
     */
    public Asset reimport(Path asset) {
        Asset current = importAsset(asset);
        Importer importer = importers.forExtension(extension(current.path()));
        if (importer != null && importer.version() != current.meta().importerVersion()) {
            MetaFile refreshed = new MetaFile(current.guid(), importer.id(), importer.version(),
                    importer.defaultSettings());
            JsonFiles.writeAtomic(metaPath(current.path()), refreshed.toJson());
            current = new Asset(current.guid(), current.path(), refreshed);
            index(current);
        }
        return current;
    }

    /**
     * Scans the assets root, indexing every {@code .meta} sidecar.
     *
     * @return the number of assets indexed
     */
    public int scan() {
        if (!Files.isDirectory(root)) {
            return 0;
        }
        List<Path> metas = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".meta"))
                    .forEach(metas::add);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot scan " + root, e);
        }
        for (Path metaPath : metas) {
            MetaFile meta = MetaFile.fromJson(JsonFiles.parseObject(metaPath), metaPath.toString());
            Path asset = Path.of(metaPath.toString().substring(0,
                    metaPath.toString().length() - ".meta".length()));
            index(new Asset(meta.guid(), asset.toAbsolutePath().normalize(), meta));
        }
        return metas.size();
    }

    /**
     * Resolves an asset by GUID.
     *
     * @param guid the identity
     * @return the asset, or empty when unknown
     */
    public Optional<Asset> resolve(Guid guid) {
        return Optional.ofNullable(byGuid.get(guid));
    }

    /**
     * Moves/renames an asset together with its sidecar, preserving its GUID.
     *
     * @param from the current path
     * @param to   the new path
     * @return the moved asset
     */
    public Asset moveAsset(Path from, Path to) {
        Path source = from.toAbsolutePath().normalize();
        Path target = to.toAbsolutePath().normalize();
        try {
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            if (Files.exists(source)) {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
            Path sourceMeta = metaPath(source);
            Path targetMeta = metaPath(target);
            if (Files.exists(sourceMeta)) {
                Files.move(sourceMeta, targetMeta, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("cannot move " + source + " to " + target, e);
        }

        Asset existing = byPath.remove(source);
        MetaFile meta = existing != null ? existing.meta()
                : MetaFile.fromJson(JsonFiles.parseObject(metaPath(target)), metaPath(target).toString());
        Asset moved = new Asset(meta.guid(), target, meta);
        index(moved);
        return moved;
    }

    /**
     * The sidecar path for an asset.
     *
     * @param asset the asset path
     * @return the {@code <asset>.meta} path
     */
    public Path metaPath(Path asset) {
        return asset.resolveSibling(asset.getFileName().toString() + ".meta");
    }

    private MetaFile createMeta(Path asset) {
        String extension = extension(asset);
        Importer importer = importers.forExtension(extension);
        if (importer == null) {
            return new MetaFile(Guid.random(), "raw", 0, Map.of());
        }
        return new MetaFile(Guid.random(), importer.id(), importer.version(),
                importer.defaultSettings());
    }

    private void index(Asset asset) {
        byGuid.put(asset.guid(), asset);
        byPath.put(asset.path(), asset);
    }

    private static String extension(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
