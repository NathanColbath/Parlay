package io.stratixops.engine.editor.assets;

import io.stratixops.engine.imgui.widget.AssetItem;
import io.stratixops.engine.imgui.widget.AssetKind;
import io.stratixops.engine.imgui.widget.AssetStatus;
import io.stratixops.engine.imgui.widget.Breadcrumb;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * The headless model behind the Project panel (STR-40): turns the project tree
 * into the domain-agnostic {@link AssetItem} rows the engine-imgui
 * {@code AssetBrowser} renders, and back again for navigation.
 *
 * <p>It is display-free and deterministic, so it is unit-tested directly. Asset
 * import status is derived from the presence of the {@code .meta} sidecar that
 * {@code engine-serialize}'s {@code AssetDatabase} writes: an asset with a
 * sidecar is {@link AssetStatus#READY}, a recognised asset without one is
 * {@link AssetStatus#UNIMPORTED}.
 */
public final class AssetBrowserModel {

    /** The label shown for the project root (design system 3.4). */
    public static final String ROOT_LABEL = "Project";

    private final Path root;

    /**
     * @param root the project/asset root; created lazily on first write
     */
    public AssetBrowserModel(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    /**
     * @return the absolute project root
     */
    public Path root() {
        return root;
    }

    /**
     * Lists the immediate children of a folder, folders first then files, each
     * group sorted case-insensitively by name.
     *
     * @param directory the folder to list, or {@code null} for the root
     * @return the rows (never {@code null})
     */
    public List<AssetItem> list(Path directory) {
        Path target = directory == null ? root : directory.toAbsolutePath().normalize();
        if (!Files.isDirectory(target)) {
            return List.of();
        }
        List<AssetItem> items = new ArrayList<>();
        try (Stream<Path> stream = Files.list(target)) {
            stream.filter(path -> !isHidden(path))
                    .filter(path -> !path.getFileName().toString().endsWith(".meta"))
                    .forEach(path -> items.add(toItem(path)));
        } catch (IOException e) {
            throw new UncheckedIOException("cannot list " + target, e);
        }
        items.sort(Comparator
                .comparingInt((AssetItem item) -> item.isFolder() ? 0 : 1)
                .thenComparing(item -> item.name().toLowerCase(Locale.ROOT)));
        return items;
    }

    /**
     * Builds the breadcrumb path from the root to a folder.
     *
     * @param directory the folder, or {@code null} for the root
     * @return the segments, root first
     */
    public List<Breadcrumb.Segment> breadcrumb(Path directory) {
        List<Breadcrumb.Segment> segments = new ArrayList<>();
        segments.add(new Breadcrumb.Segment(ROOT_LABEL, ""));
        Path target = directory == null ? root : directory.toAbsolutePath().normalize();
        if (!target.startsWith(root)) {
            return segments;
        }
        Path relative = root.relativize(target);
        Path cumulative = Path.of("");
        for (Path part : relative) {
            cumulative = cumulative.resolve(part);
            segments.add(new Breadcrumb.Segment(part.toString(), cumulative.toString().replace('\\', '/')));
        }
        return segments;
    }

    /**
     * Resolves a listed {@link AssetItem} id (a root-relative path) back to a
     * filesystem path.
     *
     * @param item the item
     * @return the absolute path
     */
    public Path resolve(AssetItem item) {
        if (item == null) {
            return root;
        }
        return pathFor(item.id());
    }

    /**
     * Resolves a root-relative path id (using {@code /} separators) to an
     * absolute path.
     *
     * @param relative the relative path, or {@code null}/empty for the root
     * @return the absolute path
     */
    public Path pathFor(String relative) {
        if (relative == null || relative.isEmpty()) {
            return root;
        }
        return root.resolve(relative.replace('/', java.io.File.separatorChar)).normalize();
    }

    /**
     * @param item the item
     * @return true when the item is a folder
     */
    public boolean isFolder(AssetItem item) {
        return item != null && item.isFolder();
    }

    private AssetItem toItem(Path path) {
        String name = path.getFileName().toString();
        String relative = root.relativize(path).toString().replace('\\', '/');
        if (Files.isDirectory(path)) {
            return AssetItem.folder(relative, name);
        }
        long size = sizeOf(path);
        long modified = modifiedOf(path);
        AssetKind kind = kindOf(name);
        return new AssetItem(relative, name, kind == null ? AssetKind.SCRIPT : kind, size, modified,
                statusOf(path));
    }

    private AssetStatus statusOf(Path file) {
        if (Files.exists(sidecar(file))) {
            return AssetStatus.READY;
        }
        return isRecognised(file.getFileName().toString())
                ? AssetStatus.UNIMPORTED
                : AssetStatus.READY;
    }

    private Path sidecar(Path file) {
        return file.resolveSibling(file.getFileName().toString() + ".meta");
    }

    private static boolean isHidden(Path path) {
        String name = path.getFileName().toString();
        if (name.startsWith(".")) {
            return true;
        }
        try {
            return Files.isHidden(path);
        } catch (IOException e) {
            return false;
        }
    }

    private static long sizeOf(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            return 0L;
        }
    }

    private static long modifiedOf(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis() / 1000L;
        } catch (IOException e) {
            return 0L;
        }
    }

    private static boolean isRecognised(String name) {
        return kindOf(name) != null;
    }

    /**
     * Maps a file name's extension to an {@link AssetKind}. Unknown extensions
     * fall back to {@link AssetKind#SCRIPT} (the closest icon) rather than
     * dropping the row.
     *
     * @param fileName the file name
     * @return the kind, or {@code null} when the name has no recognised
     *         extension and is not a known UID-less asset
     */
    public static AssetKind kindOf(String fileName) {
        String name = fileName.toLowerCase(Locale.ROOT);
        if (name.endsWith(".scene") || name.endsWith(".level") || name.endsWith(".lvl")) {
            return AssetKind.SCENE;
        }
        if (name.endsWith(".prefab")) {
            return AssetKind.PREFAB;
        }
        if (name.endsWith(".java") || name.endsWith(".kt")) {
            return AssetKind.SCRIPT;
        }
        if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")
                || name.endsWith(".bmp") || name.endsWith(".gif")) {
            return AssetKind.IMAGE;
        }
        if (name.endsWith(".ogg") || name.endsWith(".wav") || name.endsWith(".mp3")) {
            return AssetKind.AUDIO;
        }
        if (name.endsWith(".ttf") || name.endsWith(".otf")) {
            return AssetKind.FONT;
        }
        return null;
    }
}
