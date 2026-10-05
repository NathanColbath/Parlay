package io.stratixops.engine.imgui.widget;

import java.util.Objects;

/**
 * A single element of the Project panel (design system section 3.4). The model
 * is deliberately small and engine-agnostic: the descriptor to asset mapping
 * lives in {@code engine-editor} (ADR-0007).
 *
 * @param id                  stable identity (asset guid or project-relative path)
 * @param name                display label; may include an extension
 * @param kind                category for the type column and filter chips
 * @param sizeBytes           file size; ignored for folders
 * @param modifiedEpochSeconds last-modified time; ignored for folders
 * @param status              import status controlling the tile placeholder
 */
public record AssetItem(String id, String name, AssetKind kind, long sizeBytes,
        long modifiedEpochSeconds, AssetStatus status) {

    public AssetItem {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(status, "status");
        if (sizeBytes < 0L) {
            sizeBytes = 0L;
        }
    }

    /** A ready leaf asset with no size/time metadata. */
    public static AssetItem of(String id, String name, AssetKind kind) {
        return new AssetItem(id, name, kind, 0L, 0L, AssetStatus.READY);
    }

    /** A folder container. */
    public static AssetItem folder(String path, String name) {
        return new AssetItem(path, name, AssetKind.FOLDER, 0L, 0L, AssetStatus.READY);
    }

    /** @return whether this element is a folder container. */
    public boolean isFolder() {
        return kind.isContainer();
    }
}
