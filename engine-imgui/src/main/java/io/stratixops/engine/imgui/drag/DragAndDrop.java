package io.stratixops.engine.imgui.drag;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseButton;
import io.stratixops.engine.imgui.theme.Theme;

/**
 * ImGui glue for the shared {@link DragSession}: samples pointer and keyboard
 * input, lets widgets register sources and targets, then draws the overlay and
 * returns the drop outcome.
 *
 * <p>One instance is owned by a panel (or a window) and driven once per frame:
 * widgets call {@link #source} and {@link #target} during layout, then the panel
 * calls {@link #endFrame}. The gesture logic itself lives in {@code DragSession}
 * and stays display-free.
 */
public final class DragAndDrop {

    private final DragSession session;
    private Theme theme;
    private boolean reduceMotion;

    public DragAndDrop() {
        this(new DragSession());
    }

    public DragAndDrop(DragSession session) {
        this.session = session;
    }

    /** Resets per-frame state and selects the theme for this frame. */
    public void beginFrame(Theme currentTheme) {
        this.theme = currentTheme;
        session.beginFrame();
    }

    /**
     * Declares a drag source. A press on the hovered item starts a pending drag.
     *
     * @param payload  the selection to drag
     * @param hovered  whether the source item is hovered this frame
     * @param x,y,w,h  source rect in screen space (the ghost snapshot size)
     */
    public void source(DragPayload payload, boolean hovered, float x, float y, float w, float h) {
        if (hovered && ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
            ImVec2 pointer = ImGui.getMousePos();
            session.press(pointer.x, pointer.y, w, h, payload);
        }
    }

    /** Declares a drop target for this frame; ignored unless a drag is active. */
    public void target(DropTarget target) {
        session.hover(target);
    }

    /** Convenience: registers a row target resolved from the pointer position. */
    public void target(String id, float x, float y, float w, float h, boolean acceptsInto,
            boolean actionable) {
        if (!session.isDragging()) {
            return;
        }
        DropZone zone = DropZones.resolve(ImGui.getMousePosY(), y, h, acceptsInto, actionable);
        session.hover(DropTarget.row(id, zone, x, y, w, h));
    }

    /** @return the session driving this controller. */
    public DragSession session() {
        return session;
    }

    public boolean isDragging() {
        return session.isDragging();
    }

    public boolean isActive() {
        return session.isActive();
    }

    public boolean isPending() {
        return session.isPending();
    }

    /** Enables reduced-motion behaviour (instant snap-back) for this controller. */
    public void setReduceMotion(boolean reduceMotion) {
        this.reduceMotion = reduceMotion;
    }

    /**
     * Advances the gesture, draws the overlay and returns the drop outcome.
     *
     * @param rowWidth full width used by reorder insertion lines
     * @return the completed drop, or {@code null} when no drag ended this frame
     */
    public DropResult endFrame(float rowWidth) {
        ImGuiIO io = ImGui.getIO();
        ImVec2 pointer = ImGui.getMousePos();
        if (session.isPending() || session.isDragging()) {
            session.move(pointer.x, pointer.y);
            session.setModifier(DragModifier.of(io.getKeyCtrl(), io.getKeyAlt()));
        }

        if (session.isActive() && ImGui.isKeyPressed(ImGuiKey.Escape)) {
            float snap = theme == null ? 0f
                    : theme.motion.duration(theme.motion.MOTION_FAST, reduceMotion);
            session.cancel(snap);
        }

        DropResult result = null;
        if (ImGui.isMouseReleased(ImGuiMouseButton.Left)) {
            result = session.release();
        }

        session.advance(io.getDeltaTime());
        if (theme != null) {
            DragOverlay.draw(theme, session, rowWidth);
        }
        return result;
    }
}
