package io.stratixops.engine.imgui.drag;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.flag.ImGuiMouseCursor;
import io.stratixops.engine.imgui.theme.Rgba;
import io.stratixops.engine.imgui.theme.Theme;
import java.util.List;

/**
 * Replays {@link DragVisuals} draw commands onto ImGui's foreground draw list
 * and sets the no-drop cursor for invalid targets. The overlay is always drawn
 * last, so the ghost floats above every panel.
 */
public final class DragOverlay {

    private DragOverlay() {
    }

    /** Builds and draws the overlay for the current drag state. */
    public static void draw(Theme theme, DragSession session, float rowWidth) {
        List<DrawCommand> commands = DragVisuals.build(theme, session, rowWidth);
        drawCommands(commands);
        if (commands.isEmpty()) {
            return;
        }
        DropTarget candidate = session.candidate();
        if (candidate != null && candidate.zone() == DropZone.INVALID) {
            ImGui.setMouseCursor(ImGuiMouseCursor.NotAllowed);
        }
    }

    /** Replays a pre-built command list onto the foreground draw list. */
    public static void drawCommands(List<DrawCommand> commands) {
        if (commands.isEmpty()) {
            return;
        }
        ImDrawList draw = ImGui.getForegroundDrawList();
        for (DrawCommand command : commands) {
            float[] rgba = Rgba.toFloats(command.color());
            int color = ImGui.getColorU32(rgba[0], rgba[1], rgba[2], rgba[3]);
            if (command.isFill()) {
                draw.addRectFilled(command.x(), command.y(), command.x() + command.w(),
                        command.y() + command.h(), color, command.rounding());
            } else if (command.isStroke()) {
                draw.addRect(command.x(), command.y(), command.x() + command.w(),
                        command.y() + command.h(), color, command.rounding(), command.thickness());
            } else if (command.isText()) {
                draw.addText(command.x(), command.y(), color, command.text());
            }
        }
    }
}
