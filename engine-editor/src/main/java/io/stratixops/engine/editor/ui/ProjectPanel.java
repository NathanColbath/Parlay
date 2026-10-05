package io.stratixops.engine.editor.ui;

import imgui.ImGui;
import io.stratixops.engine.editor.PanelKind;
import io.stratixops.engine.editor.assets.AssetBrowserModel;
import io.stratixops.engine.editor.console.LogLevel;
import io.stratixops.engine.imgui.widget.AssetBrowser;
import io.stratixops.engine.imgui.widget.AssetEvent;
import io.stratixops.engine.imgui.widget.AssetItem;
import io.stratixops.engine.imgui.widget.Breadcrumb;

import java.nio.file.Path;
import java.util.List;

/**
 * Project panel (STR-40): the engine-imgui asset browser over the
 * {@link AssetBrowserModel}, which browses the project root and derives import
 * status from the {@code AssetDatabase} {@code .meta} sidecars.
 */
final class ProjectPanel {

    private final AssetBrowser browser = new AssetBrowser();
    private final AssetBrowserModel model;
    private Path currentDirectory;

    ProjectPanel(Path projectRoot) {
        Path root = projectRoot != null ? projectRoot
                : Path.of(System.getProperty("user.dir", "."));
        this.model = new AssetBrowserModel(root);
        this.currentDirectory = model.root();
    }

    AssetBrowserModel model() {
        return model;
    }

    void render(EditorContext ctx) {
        if (!ImGui.begin(PanelKind.PROJECT.title())) {
            ImGui.end();
            return;
        }
        if (ImGui.isWindowFocused()) {
            ctx.focusedPanel = PanelKind.PROJECT;
        }
        List<AssetItem> items = model.list(currentDirectory);
        List<Breadcrumb.Segment> path = model.breadcrumb(currentDirectory);
        int active = Math.max(path.size() - 1, 0);
        AssetBrowser.Outcome outcome = browser.render(ctx.theme(), items, path, active);

        if (outcome.navigatedPath() != null) {
            currentDirectory = model.pathFor(outcome.navigatedPath());
        } else if (outcome.event().isPresent()) {
            handleEvent(ctx, outcome.event());
        }
        ImGui.end();
    }

    private void handleEvent(EditorContext ctx, AssetEvent event) {
        switch (event.action()) {
            case OPEN -> {
                AssetItem item = find(event.id());
                if (item != null && item.isFolder()) {
                    currentDirectory = model.resolve(item);
                } else if (item != null) {
                    ctx.log(LogLevel.INFO, "Open " + item.name() + " (asset preview is P2)");
                }
            }
            case NAVIGATE_UP -> {
                Path parent = currentDirectory.getParent();
                if (parent != null && parent.startsWith(model.root())) {
                    currentDirectory = parent;
                } else {
                    currentDirectory = model.root();
                }
            }
            case RENAME, DELETE ->
                    ctx.log(LogLevel.WARNING, event.action() + " is not wired yet on "
                            + event.id());
            default -> {
            }
        }
    }

    private AssetItem find(String id) {
        if (id == null) {
            return null;
        }
        for (AssetItem item : model.list(currentDirectory)) {
            if (id.equals(item.id())) {
                return item;
            }
        }
        return null;
    }
}
