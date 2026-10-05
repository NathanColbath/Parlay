package io.stratixops.engine.editor.ui;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import io.stratixops.engine.editor.PanelKind;
import io.stratixops.engine.editor.console.ConsoleEntry;
import io.stratixops.engine.editor.console.ConsoleModel;
import io.stratixops.engine.editor.console.LogLevel;
import io.stratixops.engine.imgui.theme.Icon;
import io.stratixops.engine.imgui.theme.Rgba;
import io.stratixops.engine.imgui.theme.Theme;
import io.stratixops.engine.imgui.widget.Ui;

import java.util.List;
import java.util.Map;

/**
 * Console panel (STR-40): levelled, filterable entries with {@code file:line}
 * sources and expandable stack traces, rendered from the headless
 * {@link ConsoleModel}.
 */
final class ConsolePanel {

    private final ImString search = new ImString(128);
    private boolean autoScroll = true;
    private String lastSearch = "";

    void render(EditorContext ctx) {
        Theme theme = ctx.theme();
        ConsoleModel console = ctx.state.console();
        if (!ImGui.begin(PanelKind.CONSOLE.title())) {
            ImGui.end();
            return;
        }
        if (ImGui.isWindowFocused()) {
            ctx.focusedPanel = PanelKind.CONSOLE;
        }
        toolbar(theme, console);
        ImGui.separator();

        List<ConsoleEntry> entries = console.filtered();
        if (ImGui.beginChild("##console-log")) {
            boolean atBottom = ImGui.getScrollY() >= ImGui.getScrollMaxY() - 1f;
            for (int i = 0; i < entries.size(); i++) {
                entry(theme, i, entries.get(i));
            }
            if (autoScroll && atBottom) {
                ImGui.setScrollHereY(1f);
            }
        }
        ImGui.endChild();
        ImGui.end();
    }

    private void toolbar(Theme theme, ConsoleModel console) {
        ImGui.setNextItemWidth(120f);
        if (ImGui.beginCombo("##console-level", console.minLevel().name())) {
            for (LogLevel level : LogLevel.values()) {
                boolean selected = level == console.minLevel();
                if (ImGui.selectable(level.name(), selected)) {
                    console.setMinLevel(level);
                }
                if (selected) {
                    ImGui.setItemDefaultFocus();
                }
            }
            ImGui.endCombo();
        }
        ImGui.sameLine();
        if (ImGui.inputTextWithHint("##console-search", "Search\u2026", search,
                ImGuiInputTextFlags.None)) {
            // handled below
        }
        String current = search.get();
        if (!current.equals(lastSearch)) {
            lastSearch = current;
            console.setSearch(current);
        }
        ImGui.sameLine();
        if (ImGui.button("Clear")) {
            console.clear();
        }
        ImGui.sameLine();
        ImBoolean auto = new ImBoolean(autoScroll);
        if (ImGui.checkbox("Auto-scroll", auto)) {
            autoScroll = auto.get();
        }
        ImGui.sameLine();
        Map<LogLevel, Integer> counts = console.counts();
        Ui.pushColor(imgui.flag.ImGuiCol.Text, theme.color.TEXT_MUTED);
        ImGui.textUnformatted("I:" + counts.get(LogLevel.INFO) + "  W:"
                + counts.get(LogLevel.WARNING) + "  E:" + counts.get(LogLevel.ERROR));
        Ui.popColor();
    }

    private void entry(Theme theme, int index, ConsoleEntry entry) {
        int color = switch (entry.level()) {
            case INFO -> theme.color.STATE_INFO;
            case WARNING -> theme.color.STATE_WARN;
            case ERROR -> theme.color.STATE_ERROR;
        };
        Icon icon = switch (entry.level()) {
            case INFO -> Icon.CONSOLE_INFO;
            case WARNING -> Icon.CONSOLE_WARN;
            case ERROR -> Icon.CONSOLE_ERROR;
        };
        ImGui.pushID(index);
        Ui.pushColor(imgui.flag.ImGuiCol.Text, color);
        String message = Ui.iconGlyph(icon) + " " + entry.message();
        boolean hasStack = entry.stack() != null && !entry.stack().isBlank();
        if (hasStack) {
            if (ImGui.treeNode(message)) {
                if (entry.source() != null) {
                    Ui.pushColor(imgui.flag.ImGuiCol.Text, theme.color.TEXT_MUTED);
                    ImGui.textUnformatted(entry.source());
                    Ui.popColor();
                }
                ImGui.textUnformatted(entry.stack());
                ImGui.treePop();
            }
        } else {
            ImGui.textUnformatted(message);
            if (entry.source() != null) {
                ImGui.sameLine();
                Ui.pushColor(imgui.flag.ImGuiCol.Text, theme.color.TEXT_MUTED);
                ImGui.textUnformatted("(" + entry.source() + ")");
                Ui.popColor();
            }
        }
        Ui.popColor();
        ImGui.popID();
    }
}
