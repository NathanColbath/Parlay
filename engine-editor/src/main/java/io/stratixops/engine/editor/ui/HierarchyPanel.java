package io.stratixops.engine.editor.ui;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseButton;
import imgui.type.ImString;
import io.stratixops.engine.editor.command.EditCommands;
import io.stratixops.engine.editor.EditorState;
import io.stratixops.engine.editor.PanelKind;
import io.stratixops.engine.imgui.drag.DropResult;
import io.stratixops.engine.imgui.drag.DropZone;
import io.stratixops.engine.imgui.theme.Icon;
import io.stratixops.engine.imgui.theme.Theme;
import io.stratixops.engine.imgui.widget.ContextMenu;
import io.stratixops.engine.imgui.widget.CreateMenu;
import io.stratixops.engine.imgui.widget.TreeRow;
import io.stratixops.engine.inspect.ComponentDescriptor;
import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.scene.AudioSource;
import io.stratixops.engine.scene.Camera;
import io.stratixops.engine.scene.Component;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.SpriteRenderer;
import io.stratixops.engine.scene.Transform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hierarchy panel (STR-40): a recursive scene tree with single/Ctrl/marquee
 * selection, drag-reparent through the shared engine-imgui drag system, inline
 * rename, and a context menu whose every entry dispatches through the
 * {@code CommandStack}.
 */
final class HierarchyPanel {

    private final CreateMenu.State createMenu = new CreateMenu.State();
    private final ContextMenu.State contextMenu = new ContextMenu.State();
    private final ImString search = new ImString(64);
    private final RenameState rename = new RenameState();
    private final Set<GameObject> collapsed = new HashSet<>();
    private final List<RowHit> rowHits = new ArrayList<>();

    private GameObject contextTarget;
    private boolean marqueeActive;
    private float marqueeStartX;
    private float marqueeStartY;

    private static final class RenameState {
        GameObject target;
        final ImString buffer = new ImString(64);
        boolean focusPending;

        void begin(GameObject object) {
            this.target = object;
            buffer.set(object.name());
            focusPending = true;
        }

        void cancel() {
            target = null;
        }
    }

    private record RowHit(GameObject object, float minX, float minY, float maxX, float maxY) {
        boolean intersects(float x0, float y0, float x1, float y1) {
            return maxX >= x0 && minX <= x1 && maxY >= y0 && minY <= y1;
        }
    }

    void render(EditorContext ctx) {
        Theme theme = ctx.theme();
        EditorState state = ctx.state;
        rowHits.clear();

        ImGui.setNextWindowSize(320f, 400f, imgui.flag.ImGuiCond.FirstUseEver);
        if (!ImGui.begin(PanelKind.HIERARCHY.title())) {
            ImGui.end();
            return;
        }
        if (ImGui.isWindowFocused()) {
            ctx.focusedPanel = PanelKind.HIERARCHY;
        }

        toolbar(ctx, theme, state);

        boolean overRow = ImGui.isAnyItemHovered();
        if (ImGui.isWindowHovered() && ImGui.isMouseClicked(ImGuiMouseButton.Left) && !overRow
                && !ImGui.isAnyItemActive()) {
            marqueeActive = true;
            marqueeStartX = ImGui.getMousePosX();
            marqueeStartY = ImGui.getMousePosY();
        }

        if (ImGui.beginChild("##hierarchy-tree", 0f, -1f, false)) {
            for (GameObject root : state.scene().gameObjects()) {
                row(ctx, root, 0);
            }
            handleMarquee(ctx, state, theme);
        }
        ImGui.endChild();

        handleDrop(ctx, state);
        handleContextMenu(ctx, state, theme);
        handleCreateMenu(ctx, state);

        ImGui.end();
    }

    private void toolbar(EditorContext ctx, Theme theme, EditorState state) {
        if (ImGui.button(UiGlyph.plus(), 0f, 0f)) {
            createMenu.openAt(ImGui.getMousePosX(), ImGui.getMousePosY());
        }
        ImGui.sameLine();
        ImGui.setNextItemWidth(-1f);
        if (ImGui.inputTextWithHint("##hierarchy-search", "Search\u2026", search,
                ImGuiInputTextFlags.None)) {
            // Live filter is applied in matches().
        }
        ImGui.separator();
    }

    private boolean matches(GameObject object) {
        String needle = search.get();
        if (needle == null || needle.isBlank()) {
            return true;
        }
        String lower = needle.toLowerCase(java.util.Locale.ROOT);
        if (object.name().toLowerCase(java.util.Locale.ROOT).contains(lower)) {
            return true;
        }
        for (GameObject child : object.children()) {
            if (matches(child)) {
                return true;
            }
        }
        return false;
    }

    private void row(EditorContext ctx, GameObject object, int depth) {
        if (!matches(object)) {
            return;
        }
        EditorState state = ctx.state;
        Theme theme = ctx.theme();
        boolean hasChildren = !object.children().isEmpty();
        boolean expanded = !collapsed.contains(object);
        boolean selected = state.selection().contains(object);
        boolean primary = state.selection().primary() == object;

        if (rename.target == object) {
            ImGui.indent(depth * 14f);
            ImGui.setNextItemWidth(-1f);
            if (rename.focusPending) {
                ImGui.setKeyboardFocusHere();
                rename.focusPending = false;
            }
            boolean submitted = ImGui.inputText("##hierarchy-rename", rename.buffer,
                    ImGuiInputTextFlags.EnterReturnsTrue);
            if (submitted || ImGui.isItemDeactivatedAfterEdit()) {
                commitRename(ctx, object);
            } else if (ImGui.isKeyPressed(ImGuiKey.Escape)) {
                rename.cancel();
            }
            ImGui.unindent(depth * 14f);
        } else {
            TreeRow.Action action = TreeRow.row(theme, ctx.drag, "hierarchy",
                    List.of(object.guid().value()), object.guid().value(), object.name(),
                    iconFor(object), depth, hasChildren, expanded, selected, primary, true);

            captureHit(object);
            if (ImGui.isItemHovered() && ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
                contextTarget = object;
                contextMenu.openAt(ImGui.getMousePosX(), ImGui.getMousePosY());
            }
            if (action == TreeRow.Action.SELECTED) {
                if (ImGui.getIO().getKeyCtrl()) {
                    state.selection().toggle(object);
                } else {
                    state.selection().select(object);
                }
            } else if (action == TreeRow.Action.TOGGLED) {
                if (expanded) {
                    collapsed.add(object);
                } else {
                    collapsed.remove(object);
                }
            }
        }

        if (hasChildren && expanded) {
            for (GameObject child : object.children()) {
                row(ctx, child, depth + 1);
            }
        }
    }

    private void captureHit(GameObject object) {
        rowHits.add(new RowHit(object, ImGui.getItemRectMinX(), ImGui.getItemRectMinY(),
                ImGui.getItemRectMaxX(), ImGui.getItemRectMaxY()));
    }

    private void handleMarquee(EditorContext ctx, EditorState state, Theme theme) {
        if (!marqueeActive) {
            return;
        }
        if (!ImGui.isMouseDown(ImGuiMouseButton.Left)) {
            marqueeActive = false;
            return;
        }
        float x = Math.min(marqueeStartX, ImGui.getMousePosX());
        float y = Math.min(marqueeStartY, ImGui.getMousePosY());
        float w = Math.abs(ImGui.getMousePosX() - marqueeStartX);
        float h = Math.abs(ImGui.getMousePosY() - marqueeStartY);
        ImGui.getWindowDrawList().addRectFilled(marqueeStartX, marqueeStartY,
                marqueeStartX + (ImGui.getMousePosX() - marqueeStartX),
                marqueeStartY + (ImGui.getMousePosY() - marqueeStartY),
                io.stratixops.engine.imgui.widget.Ui.u32(theme.color.ACCENT_SUBTLE));
        List<GameObject> hits = new ArrayList<>();
        for (RowHit hit : rowHits) {
            if (hit.intersects(x, y, x + w, y + h)) {
                hits.add(hit.object());
            }
        }
        if (!hits.isEmpty()) {
            if (ImGui.getIO().getKeyCtrl()) {
                for (GameObject object : hits) {
                    state.selection().add(object);
                }
            } else {
                state.selection().set(hits);
            }
        }
    }

    private void handleDrop(EditorContext ctx, EditorState state) {
        DropResult result = ctx.drag.endFrame(Math.max(ImGui.getContentRegionAvailX(), 1f));
        if (result == null || !result.accepted() || !"hierarchy".equals(result.payload().sourcePanel())) {
            return;
        }
        GameObject target = state.scene().findObject(
                io.stratixops.engine.scene.Guid.of(result.targetId()));
        if (target == null) {
            return;
        }
        GameObject newParent = result.zone() == DropZone.INTO ? target : target.parent();
        for (String id : result.payload().ids()) {
            GameObject dragged = state.scene().findObject(io.stratixops.engine.scene.Guid.of(id));
            if (dragged == null || dragged == target || isAncestor(dragged, target)) {
                continue;
            }
            ctx.execute(EditCommands.reparent(state.scene(), dragged, newParent));
        }
    }

    private static boolean isAncestor(GameObject candidate, GameObject object) {
        for (GameObject cursor = object; cursor != null; cursor = cursor.parent()) {
            if (cursor == candidate) {
                return true;
            }
        }
        return false;
    }

    private void handleContextMenu(EditorContext ctx, EditorState state, Theme theme) {
        List<ContextMenu.Entry> entries = contextEntries(ctx);
        String activated = ContextMenu.render(theme, contextMenu, entries);
        if (activated == null) {
            return;
        }
        GameObject target = contextTarget;
        switch (activated) {
            case "duplicate" -> {
                if (target != null) {
                    state.selection().select(target);
                    ctx.controller.duplicateSelection();
                }
            }
            case "rename" -> {
                if (target != null) {
                    state.selection().select(target);
                    rename.begin(target);
                }
            }
            case "delete" -> {
                if (target != null) {
                    state.selection().select(target);
                }
                ctx.controller.deleteSelection();
            }
            case "create" -> EditorCommandCatalog.create(ctx, null);
            case "create-child" -> {
                if (target != null) {
                    EditorCommandCatalog.create(ctx, target);
                }
            }
            default -> {
                if (activated.startsWith("comp:")) {
                    ComponentCommands.add(ctx, target, activated.substring("comp:".length()));
                }
            }
        }
    }

    private List<ContextMenu.Entry> contextEntries(EditorContext ctx) {
        ComponentRegistry registry = ctx.state.registry();
        List<ContextMenu.Entry> components = new ArrayList<>();
        for (ComponentDescriptor descriptor : registry.descriptors()) {
            if (descriptor.type() == Transform.class) {
                continue;
            }
            components.add(ContextMenu.Entry.item("comp:" + descriptor.typeId(),
                    descriptor.displayName(), iconFor(descriptor.type()), null));
        }
        List<ContextMenu.Entry> entries = new ArrayList<>();
        entries.add(ContextMenu.Entry.item("duplicate", "Duplicate", Icon.DUPLICATE, "Ctrl+D"));
        entries.add(ContextMenu.Entry.item("rename", "Rename", Icon.RENAME, "F2"));
        entries.add(ContextMenu.Entry.item("delete", "Delete", Icon.DELETE, "Del"));
        entries.add(ContextMenu.Entry.separator());
        entries.add(ContextMenu.Entry.item("create", "Create Empty", Icon.ADD, null));
        entries.add(ContextMenu.Entry.item("create-child", "Create Empty Child",
                Icon.CREATE_CHILD, null));
        entries.add(ContextMenu.Entry.separator());
        entries.add(ContextMenu.Entry.submenu("add-component", "Add Component", Icon.ADD, components));
        return entries;
    }

    private void handleCreateMenu(EditorContext ctx, EditorState state) {
        List<CreateMenu.Item> items = new ArrayList<>();
        items.add(CreateMenu.Item.quick("empty", "Create Empty", Icon.ADD));
        items.add(CreateMenu.Item.quick("empty-child", "Create Empty Child", Icon.CREATE_CHILD));
        for (ComponentDescriptor descriptor : state.registry().descriptors()) {
            if (descriptor.type() == Transform.class) {
                continue;
            }
            items.add(CreateMenu.Item.of("comp:" + descriptor.typeId(), descriptor.displayName(),
                    descriptor.category(), iconFor(descriptor.type())));
        }
        String activated = CreateMenu.render(ctx.theme(), createMenu, items);
        if (activated == null) {
            return;
        }
        GameObject parent = contextTarget != null && state.selection().contains(contextTarget)
                ? contextTarget : null;
        if (activated.startsWith("comp:")) {
            GameObject object = EditorCommandCatalog.createObject(ctx, parent);
            ComponentCommands.add(ctx, object, activated.substring("comp:".length()));
        } else if ("empty-child".equals(activated)) {
            EditorCommandCatalog.create(ctx, parent);
        } else {
            EditorCommandCatalog.create(ctx, null);
        }
    }

    private void commitRename(EditorContext ctx, GameObject object) {
        String next = rename.buffer.get();
        if (next != null && !next.isBlank() && !next.equals(object.name())) {
            ctx.execute(EditCommands.rename(object, next));
        }
        rename.cancel();
    }

    /**
     * Begins inline rename of an object, selecting it first (F2 / palette).
     *
     * @param ctx    the editor context
     * @param object the object, or {@code null}
     */
    void beginRename(EditorContext ctx, GameObject object) {
        if (object == null) {
            return;
        }
        ctx.state.selection().select(object);
        rename.begin(object);
    }

    private static Icon iconFor(GameObject object) {
        if (object.getComponent(SpriteRenderer.class) != null) {
            return Icon.SPRITE_RENDERER;
        }
        if (object.getComponent(Camera.class) != null) {
            return Icon.CAMERA;
        }
        if (object.getComponent(AudioSource.class) != null) {
            return Icon.AUDIO_SOURCE;
        }
        return Icon.GAME_OBJECT;
    }

    private static Icon iconFor(Class<? extends Component> type) {
        return ComponentIcons.of(type);
    }

    /**
     * Small glyph helper so the file needs no ImGui colour constants.
     */
    private static final class UiGlyph {
        private static String plus() {
            return String.valueOf((char) Icon.ADD.codepoint());
        }
    }
}
