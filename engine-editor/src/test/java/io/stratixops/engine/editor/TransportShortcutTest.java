package io.stratixops.engine.editor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Transport keyboard chords (STR-41): Play {@code Ctrl+P}, Pause
 * {@code Shift+P}, Step {@code Ctrl+Alt+P}, with the command palette
 * {@code Ctrl+Shift+P} preserved as a global chord.
 */
class TransportShortcutTest {

    @Test
    void playPauseAndStepChords() {
        assertThat(ShortcutRouter.resolve(Focus.SCENE, true, false, false, ShortcutRouter.Key.P))
                .isEqualTo(Shortcut.PLAY);
        assertThat(ShortcutRouter.resolve(Focus.SCENE, false, true, false, ShortcutRouter.Key.P))
                .isEqualTo(Shortcut.PAUSE);
        assertThat(ShortcutRouter.resolve(Focus.SCENE, true, false, true, ShortcutRouter.Key.P))
                .isEqualTo(Shortcut.STEP);
    }

    @Test
    void paletteChordWinsOverPlayAndStaysGlobal() {
        assertThat(ShortcutRouter.resolve(Focus.SCENE, true, true, false, ShortcutRouter.Key.P))
                .isEqualTo(Shortcut.OPEN_COMMAND_PALETTE);
        assertThat(ShortcutRouter.resolve(Focus.TEXT, true, true, false, ShortcutRouter.Key.P))
                .isEqualTo(Shortcut.OPEN_COMMAND_PALETTE);
    }

    @Test
    void transportChordsIgnoredWhileTextInputHasFocus() {
        assertThat(ShortcutRouter.resolve(Focus.TEXT, true, false, false, ShortcutRouter.Key.P))
                .isEqualTo(Shortcut.NONE);
        assertThat(ShortcutRouter.resolve(Focus.TEXT, false, true, false, ShortcutRouter.Key.P))
                .isEqualTo(Shortcut.NONE);
        assertThat(ShortcutRouter.resolve(Focus.TEXT, true, false, true, ShortcutRouter.Key.P))
                .isEqualTo(Shortcut.NONE);
    }

    @Test
    void legacySpaceToggleStillResolvesInViews() {
        assertThat(ShortcutRouter.resolve(Focus.GAME, false, false, false, ShortcutRouter.Key.SPACE))
                .isEqualTo(Shortcut.PLAY_TOGGLE);
    }
}
