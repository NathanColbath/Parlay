package io.stratixops.engine.imgui;

import imgui.ImDrawData;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.ImInt;
import io.stratixops.engine.imgui.glue.ImGuiStyleMapper;
import io.stratixops.engine.imgui.theme.Theme;

/**
 * Headless-safe ImGui frame host for tests and CI (design system AC8,
 * ADR-0005 "headless testability").
 *
 * <p>ImGui's core is CPU-only; only the GL3 backend needs a context. This host
 * creates an ImGui context, fixes the display size, and runs begin/render so
 * panel code can be exercised without a display or GPU. Draw data is produced
 * but never uploaded.
 */
public final class HeadlessUi implements AutoCloseable {

    private final boolean ownedContext;

    public HeadlessUi(float width, float height, Theme theme) {
        // imgui-java's getCurrentContext() never returns Java-null; a null native
        // context surfaces as a wrapper whose ptr is 0.
        boolean existing = ImGui.getCurrentContext().ptr != 0;
        if (!existing) {
            ImGui.createContext();
        }
        this.ownedContext = !existing;

        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.setDisplaySize(width, height);
        io.setDeltaTime(1f / 60f);
        io.getFonts().addFontDefault();
        // No renderer backend is attached, so ImGui expects the legacy built
        // atlas (GetTexDataAsRGBA32) rather than a RendererHasTextures backend.
        io.getFonts().getTexDataAsRGBA32(new ImInt(), new ImInt());
        ImGuiStyleMapper.apply(theme, ImGui.getStyle());
    }

    /** Begins a frame. */
    public void beginFrame() {
        ImGui.newFrame();
    }

    /** Ends the frame and builds draw data. */
    public void endFrame() {
        ImGui.render();
    }

    /** @return the command-list count of the last rendered frame. */
    public int commandListCount() {
        ImDrawData data = ImGui.getDrawData();
        return data == null ? 0 : data.getCmdListsCount();
    }

    /** @return whether the last frame produced valid geometry. */
    public boolean lastFrameValid() {
        ImDrawData data = ImGui.getDrawData();
        return data != null && data.getValid();
    }

    @Override
    public void close() {
        if (ownedContext) {
            ImGui.destroyContext();
        }
    }
}
