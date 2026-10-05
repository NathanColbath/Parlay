package io.stratixops.engine.editor.ui;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import io.stratixops.engine.editor.play.PlayController;
import io.stratixops.engine.editor.play.PlayState;
import io.stratixops.engine.editor.play.TransportActions;
import io.stratixops.engine.imgui.theme.Theme;
import io.stratixops.engine.imgui.widget.Ui;

/**
 * The Play/Pause/Step transport toolbar (STR-41). Every button routes through
 * the shared {@link PlayController}, and the current transport state is shown
 * as a label. The same actions are reachable by the keyboard shortcuts
 * {@code Ctrl+P} (Play), {@code Shift+P} (Pause) and {@code Ctrl+Alt+P} (Step).
 */
final class TransportToolbar {

    private TransportToolbar() {
    }

    static void render(EditorContext ctx) {
        Theme theme = ctx.theme();
        PlayController play = ctx.state.play();

        boolean stopped = play.isStopped();
        boolean playing = play.isPlaying();
        boolean paused = play.isPaused();

        if (playing || paused) {
            Ui.pushColor(ImGuiCol.Button, theme.color.ACCENT_DEFAULT);
            Ui.pushColor(ImGuiCol.ButtonHovered, theme.color.ACCENT_HOVER);
        }
        if (ImGui.button(playing ? "Pause" : "Play")) {
            if (stopped || paused) {
                TransportActions.play(ctx.state);
            } else {
                TransportActions.pause(ctx.state);
            }
        }
        if (playing || paused) {
            Ui.popColor();
            Ui.popColor();
        }
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip(stopped ? "Play (Ctrl+P)" : "Pause (Shift+P)");
        }

        ImGui.sameLine();
        ImGui.beginDisabled(!paused);
        if (ImGui.button("Step")) {
            TransportActions.step(ctx.state);
        }
        ImGui.endDisabled();
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Step one fixed tick (Ctrl+Alt+P)");
        }

        ImGui.sameLine();
        ImGui.beginDisabled(stopped);
        if (ImGui.button("Stop")) {
            TransportActions.stop(ctx.state);
        }
        ImGui.endDisabled();

        ImGui.sameLine();
        ImGui.textUnformatted(stateLabel(play));
    }

    private static String stateLabel(PlayController play) {
        PlayState state = play.state();
        return switch (state) {
            case STOPPED -> "Stopped";
            case PLAYING -> String.format(java.util.Locale.ROOT, "Playing  %.1fs", play.elapsed());
            case PAUSED -> String.format(java.util.Locale.ROOT, "Paused  %.1fs", play.elapsed());
        };
    }
}
