package io.stratixops.engine.editor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShortcutRouterTest {

    @Test
    void textFieldConsumesEditingKeys() {
        assertThat(ShortcutRouter.resolve(Focus.TEXT, false, false, ShortcutRouter.Key.ENTER))
                .isEqualTo(Shortcut.NONE);
        assertThat(ShortcutRouter.resolve(Focus.TEXT, false, false, ShortcutRouter.Key.F2))
                .isEqualTo(Shortcut.NONE);
        assertThat(ShortcutRouter.resolve(Focus.TEXT, false, false, ShortcutRouter.Key.DELETE))
                .isEqualTo(Shortcut.NONE);
        assertThat(ShortcutRouter.resolve(Focus.TEXT, true, false, ShortcutRouter.Key.A))
                .isEqualTo(Shortcut.NONE);
        assertThat(ShortcutRouter.resolve(Focus.TEXT, true, false, ShortcutRouter.Key.C))
                .isEqualTo(Shortcut.NONE);
        assertThat(ShortcutRouter.resolve(Focus.TEXT, true, false, ShortcutRouter.Key.V))
                .isEqualTo(Shortcut.NONE);
        assertThat(ShortcutRouter.resolve(Focus.TEXT, true, false, ShortcutRouter.Key.O))
                .isEqualTo(Shortcut.NONE);
    }

    @Test
    void copyPasteAndOpenChords() {
        assertThat(ShortcutRouter.resolve(Focus.HIERARCHY, true, false, ShortcutRouter.Key.C))
                .isEqualTo(Shortcut.COPY);
        assertThat(ShortcutRouter.resolve(Focus.HIERARCHY, true, false, ShortcutRouter.Key.V))
                .isEqualTo(Shortcut.PASTE);
        assertThat(ShortcutRouter.resolve(Focus.SCENE, true, false, ShortcutRouter.Key.O))
                .isEqualTo(Shortcut.OPEN);
    }

    @Test
    void paletteChordIsGlobalEvenFromTextField() {
        assertThat(ShortcutRouter.resolve(Focus.TEXT, true, true, ShortcutRouter.Key.P))
                .isEqualTo(Shortcut.OPEN_COMMAND_PALETTE);
        assertThat(ShortcutRouter.resolve(Focus.HIERARCHY, true, true, ShortcutRouter.Key.P))
                .isEqualTo(Shortcut.OPEN_COMMAND_PALETTE);
    }

    @Test
    void undoRedoYAndShiftZ() {
        assertThat(ShortcutRouter.resolve(Focus.HIERARCHY, true, false, ShortcutRouter.Key.Z))
                .isEqualTo(Shortcut.UNDO);
        assertThat(ShortcutRouter.resolve(Focus.HIERARCHY, true, false, ShortcutRouter.Key.Y))
                .isEqualTo(Shortcut.REDO);
        assertThat(ShortcutRouter.resolve(Focus.HIERARCHY, true, true, ShortcutRouter.Key.Z))
                .isEqualTo(Shortcut.REDO);
    }

    @Test
    void selectionKeysMatchTheGtdResolution() {
        assertThat(ShortcutRouter.resolve(Focus.HIERARCHY, false, false, ShortcutRouter.Key.ENTER))
                .isEqualTo(Shortcut.ACTIVATE);
        assertThat(ShortcutRouter.resolve(Focus.HIERARCHY, false, false, ShortcutRouter.Key.F2))
                .isEqualTo(Shortcut.RENAME);
        assertThat(ShortcutRouter.resolve(Focus.HIERARCHY, true, false, ShortcutRouter.Key.A))
                .isEqualTo(Shortcut.SELECT_ALL_PANEL);
        assertThat(ShortcutRouter.resolve(Focus.HIERARCHY, true, false, ShortcutRouter.Key.D))
                .isEqualTo(Shortcut.DUPLICATE);
        assertThat(ShortcutRouter.resolve(Focus.HIERARCHY, false, false, ShortcutRouter.Key.DELETE))
                .isEqualTo(Shortcut.DELETE);
    }

    @Test
    void saveAndPlayChords() {
        assertThat(ShortcutRouter.resolve(Focus.SCENE, true, false, ShortcutRouter.Key.S))
                .isEqualTo(Shortcut.SAVE);
        assertThat(ShortcutRouter.resolve(Focus.GAME, false, false, ShortcutRouter.Key.SPACE))
                .isEqualTo(Shortcut.PLAY_TOGGLE);
        assertThat(ShortcutRouter.resolve(Focus.HIERARCHY, false, false, ShortcutRouter.Key.SPACE))
                .isEqualTo(Shortcut.NONE);
    }

    @Test
    void unknownChordIsNone() {
        assertThat(ShortcutRouter.resolve(Focus.NONE, false, false, ShortcutRouter.Key.A))
                .isEqualTo(Shortcut.NONE);
        assertThat(ShortcutRouter.resolve(Focus.NONE, false, false, null))
                .isEqualTo(Shortcut.NONE);
    }

    @Test
    void selectionActionsOnlyApplyToSelectionPanels() {
        assertThat(ShortcutRouter.isSelectionAction(Shortcut.RENAME, Focus.HIERARCHY)).isTrue();
        assertThat(ShortcutRouter.isSelectionAction(Shortcut.RENAME, Focus.CONSOLE)).isFalse();
        assertThat(ShortcutRouter.isSelectionAction(Shortcut.UNDO, Focus.HIERARCHY)).isFalse();
        assertThat(ShortcutRouter.isSelectionAction(Shortcut.COPY, Focus.HIERARCHY)).isTrue();
        assertThat(ShortcutRouter.isSelectionAction(Shortcut.PASTE, Focus.INSPECTOR)).isTrue();
    }
}
