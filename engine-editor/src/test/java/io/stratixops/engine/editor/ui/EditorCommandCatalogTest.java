package io.stratixops.engine.editor.ui;

import io.stratixops.engine.editor.EditorState;
import io.stratixops.engine.inspect.ComponentRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Catalogue-level tests for the editor command palette (STR-40). These stay
 * headless: the catalogue only touches the editor model and its commands, so
 * they prove the palette entries route through the undo stack without needing
 * an ImGui context.
 */
class EditorCommandCatalogTest {

    private static EditorContext context() {
        EditorState state = new EditorState(ComponentRegistry.withBuiltins());
        return new EditorContext(state, new ThemeProvider(), new EditorLayoutStore(null));
    }

    @Test
    void catalogCoversGameObjectEditViewAndPlayGroups() {
        List<PaletteCommand> commands = EditorCommandCatalog.commands(context());

        assertThat(commands).extracting(PaletteCommand::id)
                .contains("go.create", "go.duplicate", "go.delete",
                        "edit.undo", "edit.redo", "file.save",
                        "view.reset-layout", "view.theme",
                        "play.play", "play.stop");
    }

    @Test
    void createCommandAddsAnUndoableObject() {
        EditorContext ctx = context();
        PaletteCommand create = command(ctx, "go.create");

        create.action().run();
        assertThat(ctx.state.scene().gameObjects()).hasSize(1);

        ctx.state.undo();
        assertThat(ctx.state.scene().gameObjects()).isEmpty();
    }

    @Test
    void togglePanelCommandFlipsLayoutVisibility() {
        EditorContext ctx = context();
        var kind = io.stratixops.engine.editor.PanelKind.INSPECTOR;
        assertThat(ctx.state.layout().isVisible(kind)).isTrue();

        PaletteCommand close = command(ctx, "view.inspector");
        close.action().run();

        assertThat(ctx.state.layout().isVisible(kind)).isFalse();
    }

    private static PaletteCommand command(EditorContext ctx, String id) {
        return EditorCommandCatalog.commands(ctx).stream()
                .filter(command -> command.id().equals(id))
                .findFirst()
                .orElseThrow();
    }
}
