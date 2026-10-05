package io.stratixops.engine.editor.ui;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseButton;
import imgui.type.ImBoolean;
import io.stratixops.engine.editor.PanelKind;
import io.stratixops.engine.editor.command.Command;
import io.stratixops.engine.editor.view.EditorCamera;
import io.stratixops.engine.editor.view.GizmoController;
import io.stratixops.engine.editor.view.GizmoMode;
import io.stratixops.engine.editor.view.SceneBounds;
import io.stratixops.engine.editor.view.ViewGrid;
import io.stratixops.engine.editor.view.ViewRect;
import io.stratixops.engine.editor.view.ViewportSelection;
import io.stratixops.engine.imgui.theme.Theme;
import io.stratixops.engine.imgui.widget.Ui;
import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Scene;

import java.util.List;

/**
 * The Scene view (STR-41): an interactive editor viewport with an independent
 * pan/zoom editor camera, a reference grid, object bounds and selection
 * outlines, marquee selection, and move/rotate/scale gizmos with snapping.
 *
 * <p>The viewport is an editor representation: object bounds, the grid and the
 * gizmos are drawn on the ImGui draw list, while the Game view shows the actual
 * rendered runtime frame through the shared {@code SceneRenderer}. Gizmo drags
 * always resolve to a single {@code EditCommands.setField} command, so every
 * edit is undoable. All viewport math lives in the headless
 * {@code io.stratixops.engine.editor.view} package.
 */
final class SceneViewPanel {

    private static final float HANDLE_LENGTH = 64f;
    private static final float ROTATE_RADIUS = 56f;
    private static final float HANDLE_PICK = 8f;
    private static final float FRAME_PADDING = 0.1f;

    private enum Drag {
        NONE,
        PAN,
        MARQUEE,
        GIZMO
    }

    private final EditorCamera camera = new EditorCamera();
    private final GizmoController gizmo;
    private final ImBoolean snapToggle = new ImBoolean(true);

    private Drag drag = Drag.NONE;
    private float dragStartWorldX;
    private float dragStartWorldY;
    private float dragCenterWorldX;
    private float dragCenterWorldY;
    private float dragStartAngle;
    private float marqueeStartX;
    private float marqueeStartY;
    private ViewRect marqueeWorld;

    /**
     * @param registry the component registry (the gizmo resolves the Transform
     *                 field descriptors through it)
     */
    SceneViewPanel(ComponentRegistry registry) {
        this.gizmo = new GizmoController(registry);
    }

    void render(EditorContext ctx) {
        Theme theme = ctx.theme();
        if (!ImGui.begin(PanelKind.SCENE.title())) {
            ImGui.end();
            return;
        }
        if (ImGui.isWindowFocused()) {
            ctx.focusedPanel = PanelKind.SCENE;
        }
        updateGizmoModeFromKeyboard(ctx);
        TransportToolbar.render(ctx);
        ImGui.separator();
        toolbar(ctx, theme);

        float width = Math.max(ImGui.getContentRegionAvailX(), 1f);
        float height = Math.max(ImGui.getContentRegionAvailY(), 1f);
        camera.setViewport(width, height);

        ImVec2 origin = new ImVec2(ImGui.getCursorScreenPos());
        ImGui.invisibleButton("##scene-canvas", width, height);
        boolean hovered = ImGui.isItemHovered();

        float mouseX = ImGui.getMousePosX() - origin.x;
        float mouseY = ImGui.getMousePosY() - origin.y;

        handleInput(ctx, hovered, mouseX, mouseY);
        drawGrid(theme, origin);
        drawObjects(ctx, theme, origin);
        drawGizmo(ctx, theme, origin);
        drawMarquee(theme, origin);
        ImGui.end();
    }

    // ------------------------------------------------------------------
    // Toolbar
    // ------------------------------------------------------------------

    private void toolbar(EditorContext ctx, Theme theme) {
        for (GizmoMode mode : GizmoMode.values()) {
            boolean active = gizmo.mode() == mode;
            if (active) {
                Ui.pushColor(ImGuiCol.Button, theme.color.ACCENT_DEFAULT);
            }
            if (ImGui.button(mode.label() + " (" + mode.key() + ")")) {
                gizmo.setMode(mode);
            }
            if (active) {
                Ui.popColor();
            }
            ImGui.sameLine();
        }
        if (ImGui.checkbox("Snap", snapToggle)) {
            gizmo.setSnapping(snapToggle.get());
        }
        ImGui.sameLine();
        if (ImGui.button("Frame (F)")) {
            frameSelection(ctx);
        }
        ImGui.sameLine();
        ImGui.textUnformatted(String.format(java.util.Locale.ROOT, "%.0f%%", camera.zoom() * 100f));
    }

    private void updateGizmoModeFromKeyboard(EditorContext ctx) {
        if (!ImGui.isWindowFocused() || ImGui.getIO().getWantTextInput()) {
            return;
        }
        if (ImGui.isKeyPressed(ImGuiKey.W)) {
            gizmo.setMode(GizmoMode.MOVE);
        } else if (ImGui.isKeyPressed(ImGuiKey.E)) {
            gizmo.setMode(GizmoMode.ROTATE);
        } else if (ImGui.isKeyPressed(ImGuiKey.R)) {
            gizmo.setMode(GizmoMode.SCALE);
        } else if (ImGui.isKeyPressed(ImGuiKey.F)) {
            frameSelection(ctx);
        }
        snapToggle.set(gizmo.snapping());
    }

    private void frameSelection(EditorContext ctx) {
        List<GameObject> objects = ctx.state.selection().objects();
        if (objects.isEmpty()) {
            camera.centerOn(0f, 0f);
            camera.setZoom(1f);
            return;
        }
        ViewRect bounds = SceneBounds.union(objects);
        if (bounds != null) {
            camera.frame(bounds, FRAME_PADDING);
        }
    }

    // ------------------------------------------------------------------
    // Input
    // ------------------------------------------------------------------

    private void handleInput(EditorContext ctx, boolean hovered, float mouseX, float mouseY) {
        if (hovered) {
            float wheel = ImGui.getIO().getMouseWheel();
            if (wheel != 0f) {
                camera.zoomBy((float) Math.pow(1.1, wheel), mouseX, mouseY);
            }
        }
        if (hovered && (ImGui.isMouseDown(ImGuiMouseButton.Middle)
                || ImGui.isMouseDown(ImGuiMouseButton.Right))) {
            camera.panScreen(ImGui.getIO().getMouseDeltaX(), ImGui.getIO().getMouseDeltaY());
        }

        if (drag == Drag.NONE && hovered && ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
            GameObject primary = ctx.state.selection().primary();
            int axis = primary == null ? -1 : gizmoHandleAt(primary, mouseX, mouseY);
            if (axis >= 0) {
                beginGizmoDrag(ctx, primary, mouseX, mouseY);
            } else {
                GameObject hit = pick(ctx.state.scene(), mouseX, mouseY);
                if (hit != null) {
                    if (ImGui.getIO().getKeyCtrl()) {
                        ctx.state.selection().toggle(hit);
                    } else {
                        ctx.state.selection().select(hit);
                    }
                } else {
                    drag = Drag.MARQUEE;
                    marqueeStartX = mouseX;
                    marqueeStartY = mouseY;
                }
            }
        }

        updateDrag(ctx, mouseX, mouseY);
        if (drag != Drag.NONE && !ImGui.isMouseDown(ImGuiMouseButton.Left)) {
            endDrag(ctx, mouseX, mouseY);
        }
    }

    private void beginGizmoDrag(EditorContext ctx, GameObject primary, float mouseX, float mouseY) {
        drag = Drag.GIZMO;
        gizmo.begin(primary);
        Vec2 world = camera.screenToWorld(mouseX, mouseY);
        dragStartWorldX = world.x;
        dragStartWorldY = world.y;
        Vec2 center = primary.transform().worldPosition();
        dragCenterWorldX = center.x;
        dragCenterWorldY = center.y;
        dragStartAngle = (float) Math.atan2(world.y - center.y, world.x - center.x);
    }

    private void updateDrag(EditorContext ctx, float mouseX, float mouseY) {
        if (drag == Drag.MARQUEE) {
            // The selected set is applied on release so the live marquee only
            // drives the overlay; the world rectangle is recomputed directly.
            marqueeWorld = new ViewRect(
                    camera.screenToWorldX(Math.min(marqueeStartX, mouseX)),
                    camera.screenToWorldY(Math.min(marqueeStartY, mouseY)),
                    camera.screenToWorldX(Math.max(marqueeStartX, mouseX)),
                    camera.screenToWorldY(Math.max(marqueeStartY, mouseY)));
        }
    }

    private void endDrag(EditorContext ctx, float mouseX, float mouseY) {
        switch (drag) {
            case MARQUEE -> applyMarquee(ctx);
            case GIZMO -> commitGizmo(ctx, mouseX, mouseY);
            default -> {
            }
        }
        drag = Drag.NONE;
        marqueeWorld = null;
    }

    private void applyMarquee(EditorContext ctx) {
        if (marqueeWorld == null) {
            return;
        }
        List<GameObject> hits = ViewportSelection.objectsIn(ctx.state.scene(), marqueeWorld);
        if (hits.isEmpty()) {
            if (!ImGui.getIO().getKeyCtrl()) {
                ctx.state.selection().clear();
            }
            return;
        }
        if (ImGui.getIO().getKeyCtrl()) {
            for (GameObject object : hits) {
                ctx.state.selection().add(object);
            }
        } else {
            ctx.state.selection().set(hits);
        }
    }

    private void commitGizmo(EditorContext ctx, float mouseX, float mouseY) {
        if (!gizmo.isDragging()) {
            return;
        }
        Vec2 world = camera.screenToWorld(mouseX, mouseY);
        Command command = switch (gizmo.mode()) {
            case MOVE -> gizmo.moveCommand(new Vec2(world.x - dragStartWorldX, world.y - dragStartWorldY));
            case ROTATE -> gizmo.rotateCommand(normalizeAngle(
                    (float) Math.atan2(world.y - dragCenterWorldY, world.x - dragCenterWorldX)
                            - dragStartAngle));
            case SCALE -> gizmo.scaleCommand(scaleFactor(world.x, world.y, 0), scaleFactor(world.x, world.y, 1));
        };
        ctx.execute(command);
        gizmo.cancel();
    }

    private float scaleFactor(float worldX, float worldY, int axis) {
        float start;
        float current;
        if (axis == 0) {
            start = dragStartWorldX - dragCenterWorldX;
            current = worldX - dragCenterWorldX;
        } else {
            start = dragStartWorldY - dragCenterWorldY;
            current = worldY - dragCenterWorldY;
        }
        if (Math.abs(start) < 1e-4f) {
            return 1f;
        }
        return current / start;
    }

    // ------------------------------------------------------------------
    // Drawing
    // ------------------------------------------------------------------

    private void drawGrid(Theme theme, ImVec2 origin) {
        ImDrawList draw = ImGui.getWindowDrawList();
        ViewRect visible = camera.visibleBounds();
        float spacing = ViewGrid.spacing(camera.zoom());
        int minor = Ui.u32(theme.color.GRID_LINE);
        int major = Ui.u32(theme.color.GRID_LINE_MAJOR);
        float minX = Math.min(visible.minX(), visible.maxX());
        float maxX = Math.max(visible.minX(), visible.maxX());
        float minY = Math.min(visible.minY(), visible.maxY());
        float maxY = Math.max(visible.minY(), visible.maxY());
        for (float x : ViewGrid.verticalLines(minX, maxX, spacing)) {
            float sx = origin.x + camera.worldToScreenX(x);
            draw.addLine(sx, origin.y, sx, origin.y + camera.viewportHeight(),
                    ViewGrid.isMajor(x, spacing) ? major : minor);
        }
        for (float y : ViewGrid.horizontalLines(minY, maxY, spacing)) {
            float sy = origin.y + camera.worldToScreenY(y);
            draw.addLine(origin.x, sy, origin.x + camera.viewportWidth(), sy,
                    ViewGrid.isMajor(y, spacing) ? major : minor);
        }
        // World axes.
        float axisX = origin.x + camera.worldToScreenX(0f);
        float axisY = origin.y + camera.worldToScreenY(0f);
        if (axisX >= origin.x && axisX <= origin.x + camera.viewportWidth()) {
            draw.addLine(axisX, origin.y, axisX, origin.y + camera.viewportHeight(),
                    Ui.u32(theme.color.AXIS_Y), 1.5f);
        }
        if (axisY >= origin.y && axisY <= origin.y + camera.viewportHeight()) {
            draw.addLine(origin.x, axisY, origin.x + camera.viewportWidth(), axisY,
                    Ui.u32(theme.color.AXIS_X), 1.5f);
        }
    }

    private void drawObjects(EditorContext ctx, Theme theme, ImVec2 origin) {
        ImDrawList draw = ImGui.getWindowDrawList();
        for (GameObject root : ctx.state.scene().gameObjects()) {
            drawObject(ctx, theme, draw, origin, root);
        }
    }

    private void drawObject(EditorContext ctx, Theme theme, ImDrawList draw, ImVec2 origin,
                            GameObject object) {
        boolean selected = ctx.state.selection().contains(object);
        boolean primary = ctx.state.selection().primary() == object;
        ViewRect bounds = SceneBounds.of(object);
        float x0 = origin.x + camera.worldToScreenX(bounds.minX());
        float y0 = origin.y + camera.worldToScreenY(bounds.minY());
        float x1 = origin.x + camera.worldToScreenX(bounds.maxX());
        float y1 = origin.y + camera.worldToScreenY(bounds.maxY());
        if (selected) {
            draw.addRectFilled(x0, y0, x1, y1, Ui.u32(theme.color.ACCENT_SUBTLE));
            draw.addRect(x0, y0, x1, y1, Ui.u32(theme.color.ACCENT_DEFAULT), 0f, 0,
                    primary ? 2.5f : 1.5f);
        } else {
            draw.addRect(x0, y0, x1, y1, Ui.u32(theme.color.BORDER_DEFAULT), 0f, 0, 1f);
        }
        for (GameObject child : object.children()) {
            drawObject(ctx, theme, draw, origin, child);
        }
    }

    private void drawGizmo(EditorContext ctx, Theme theme, ImVec2 origin) {
        GameObject primary = ctx.state.selection().primary();
        if (primary == null) {
            return;
        }
        Vec2 world = primary.transform().worldPosition();
        float cx = origin.x + camera.worldToScreenX(world.x);
        float cy = origin.y + camera.worldToScreenY(world.y);
        ImDrawList draw = ImGui.getWindowDrawList();
        if (gizmo.mode() == GizmoMode.ROTATE) {
            draw.addCircle(cx, cy, ROTATE_RADIUS, Ui.u32(theme.color.GIZMO_ROTATE), 48, 2f);
            return;
        }
        int colorX = Ui.u32(theme.color.AXIS_X);
        int colorY = Ui.u32(theme.color.AXIS_Y);
        draw.addLine(cx, cy, cx + HANDLE_LENGTH, cy, colorX, 2.5f);
        draw.addLine(cx, cy, cx, cy + HANDLE_LENGTH, colorY, 2.5f);
        if (gizmo.mode() == GizmoMode.SCALE) {
            float box = 5f;
            draw.addRectFilled(cx + HANDLE_LENGTH - box, cy - box, cx + HANDLE_LENGTH + box, cy + box, colorX);
            draw.addRectFilled(cx - box, cy + HANDLE_LENGTH - box, cx + box, cy + HANDLE_LENGTH + box, colorY);
        } else {
            draw.addCircleFilled(cx + HANDLE_LENGTH, cy, 4f, colorX, 12);
            draw.addCircleFilled(cx, cy + HANDLE_LENGTH, 4f, colorY, 12);
        }
    }

    private void drawMarquee(Theme theme, ImVec2 origin) {
        if (drag != Drag.MARQUEE) {
            return;
        }
        float x = origin.x + Math.min(marqueeStartX, ImGui.getMousePosX() - origin.x);
        float y = origin.y + Math.min(marqueeStartY, ImGui.getMousePosY() - origin.y);
        float x1 = origin.x + Math.max(marqueeStartX, ImGui.getMousePosX() - origin.x);
        float y1 = origin.y + Math.max(marqueeStartY, ImGui.getMousePosY() - origin.y);
        ImGui.getWindowDrawList().addRectFilled(x, y, x1, y1, Ui.u32(theme.color.ACCENT_SUBTLE));
        ImGui.getWindowDrawList().addRect(x, y, x1, y1, Ui.u32(theme.color.ACCENT_DEFAULT));
    }

    // ------------------------------------------------------------------
    // Picking
    // ------------------------------------------------------------------

    private GameObject pick(Scene scene, float mouseX, float mouseY) {
        Vec2 world = camera.screenToWorld(mouseX, mouseY);
        GameObject best = null;
        float bestArea = Float.MAX_VALUE;
        for (GameObject root : scene.gameObjects()) {
            GameObject candidate = pick(root, world.x, world.y);
            if (candidate != null) {
                ViewRect bounds = SceneBounds.of(candidate);
                float area = Math.abs(bounds.width() * bounds.height());
                if (area < bestArea) {
                    bestArea = area;
                    best = candidate;
                }
            }
        }
        return best;
    }

    private GameObject pick(GameObject object, float worldX, float worldY) {
        GameObject best = null;
        if (SceneBounds.of(object).contains(worldX, worldY)) {
            best = object;
        }
        float bestArea = best == null ? Float.MAX_VALUE : area(best);
        for (GameObject child : object.children()) {
            GameObject candidate = pick(child, worldX, worldY);
            if (candidate != null && area(candidate) <= bestArea) {
                best = candidate;
                bestArea = area(candidate);
            }
        }
        return best;
    }

    private static float area(GameObject object) {
        ViewRect bounds = SceneBounds.of(object);
        return Math.abs(bounds.width() * bounds.height());
    }

    private int gizmoHandleAt(GameObject primary, float mouseX, float mouseY) {
        Vec2 world = primary.transform().worldPosition();
        float cx = camera.worldToScreenX(world.x);
        float cy = camera.worldToScreenY(world.y);
        if (gizmo.mode() == GizmoMode.ROTATE) {
            float distance = (float) Math.hypot(mouseX - cx, mouseY - cy);
            return Math.abs(distance - ROTATE_RADIUS) <= HANDLE_PICK ? 2 : -1;
        }
        if (distanceToSegment(mouseX, mouseY, cx, cy, cx + HANDLE_LENGTH, cy) <= HANDLE_PICK) {
            return 0;
        }
        if (distanceToSegment(mouseX, mouseY, cx, cy, cx, cy + HANDLE_LENGTH) <= HANDLE_PICK) {
            return 1;
        }
        return -1;
    }

    private static float distanceToSegment(float px, float py, float x0, float y0, float x1, float y1) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float lengthSq = dx * dx + dy * dy;
        if (lengthSq <= 1e-6f) {
            return (float) Math.hypot(px - x0, py - y0);
        }
        float t = ((px - x0) * dx + (py - y0) * dy) / lengthSq;
        t = Math.max(0f, Math.min(1f, t));
        return (float) Math.hypot(px - (x0 + t * dx), py - (y0 + t * dy));
    }

    private static float normalizeAngle(float radians) {
        float twoPi = (float) (Math.PI * 2.0);
        float value = radians % twoPi;
        if (value > (float) Math.PI) {
            value -= twoPi;
        } else if (value < -(float) Math.PI) {
            value += twoPi;
        }
        return value;
    }
}
