package io.stratixops.engine.editor.ui;

import imgui.ImGui;
import imgui.flag.ImGuiKey;
import imgui.type.ImInt;
import imgui.type.ImString;
import io.stratixops.engine.imgui.widget.CommandPalette;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Owns the command palette overlay (STR-40): open state, the search buffer, the
 * grouped filtered results, and activation. The surface itself lives in
 * engine-imgui; this adapter maps it to the editor's {@link PaletteCommand}s.
 */
final class CommandPaletteController {

    private final EditorContext ctx;
    private final ImString query = new ImString(64);
    private final ImInt selected = new ImInt(0);
    private boolean open;
    private String lastQuery = "";

    CommandPaletteController(EditorContext ctx) {
        this.ctx = ctx;
    }

    /** Opens the palette and resets its transient state. */
    void open() {
        open = true;
        query.clear();
        selected.set(0);
        lastQuery = "";
    }

    /** @return whether the palette is open. */
    boolean isOpen() {
        return open;
    }

    /** Closes the palette. */
    void close() {
        open = false;
    }

    /** Renders the overlay when open, running the activated command. */
    void render() {
        if (!open) {
            return;
        }
        if (ImGui.isKeyPressed(ImGuiKey.Escape)) {
            open = false;
            return;
        }
        String q = query.get() == null ? "" : query.get().trim();
        if (!q.equals(lastQuery)) {
            lastQuery = q;
            selected.set(0);
        }
        List<PaletteCommand> filtered = filter(q);
        Map<String, List<PaletteCommand>> groups = EditorCommandCatalog.group(filtered);
        List<PaletteCommand> rows = new ArrayList<>();
        List<String> results = new ArrayList<>();
        for (Map.Entry<String, List<PaletteCommand>> group : groups.entrySet()) {
            results.add(CommandPalette.GROUP + group.getKey());
            rows.add(null);
            for (PaletteCommand command : group.getValue()) {
                results.add(command.label());
                rows.add(command);
            }
        }
        int activated = CommandPalette.render(ctx.theme(), query, results, selected);
        if (activated >= 0 && activated < rows.size()) {
            PaletteCommand command = rows.get(activated);
            if (command != null && command.enabled()) {
                command.action().run();
            }
            open = false;
        }
    }

    private List<PaletteCommand> filter(String rawQuery) {
        List<PaletteCommand> all = EditorCommandCatalog.commands(ctx);
        if (rawQuery.isEmpty()) {
            return all;
        }
        String needle = rawQuery.toLowerCase(Locale.ROOT);
        List<PaletteCommand> matches = new ArrayList<>();
        for (PaletteCommand command : all) {
            if (command.label().toLowerCase(Locale.ROOT).contains(needle)
                    || command.group().toLowerCase(Locale.ROOT).contains(needle)) {
                matches.add(command);
            }
        }
        return matches;
    }
}
