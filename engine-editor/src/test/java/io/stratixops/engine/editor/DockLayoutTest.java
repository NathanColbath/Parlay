package io.stratixops.engine.editor;

import io.stratixops.engine.editor.DockLayout.Area;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DockLayoutTest {

    @Test
    void defaultLayoutMatchesOob() {
        DockLayout layout = DockLayout.defaultLayout();

        assertThat(layout.tabs(Area.LEFT)).containsExactly(PanelKind.HIERARCHY);
        assertThat(layout.tabs(Area.CENTER)).containsExactly(PanelKind.SCENE, PanelKind.GAME);
        assertThat(layout.tabs(Area.RIGHT)).containsExactly(PanelKind.INSPECTOR);
        assertThat(layout.tabs(Area.BOTTOM)).containsExactly(PanelKind.PROJECT, PanelKind.CONSOLE);
        assertThat(layout.panels()).containsExactly(
                PanelKind.HIERARCHY, PanelKind.SCENE, PanelKind.GAME,
                PanelKind.INSPECTOR, PanelKind.PROJECT, PanelKind.CONSOLE);
    }

    @Test
    void placeMovesTabBetweenAreas() {
        DockLayout layout = new DockLayout();
        layout.place(PanelKind.CONSOLE, Area.BOTTOM);
        layout.place(PanelKind.CONSOLE, Area.RIGHT);

        assertThat(layout.tabs(Area.BOTTOM)).isEmpty();
        assertThat(layout.areaOf(PanelKind.CONSOLE)).isEqualTo(Area.RIGHT);
    }

    @Test
    void closeHidesAndOpenRestoresDefaultArea() {
        DockLayout layout = DockLayout.defaultLayout();
        layout.close(PanelKind.INSPECTOR);

        assertThat(layout.isVisible(PanelKind.INSPECTOR)).isFalse();
        assertThat(layout.hidden()).containsExactly(PanelKind.INSPECTOR);

        layout.open(PanelKind.INSPECTOR);
        assertThat(layout.isVisible(PanelKind.INSPECTOR)).isTrue();
        assertThat(layout.areaOf(PanelKind.INSPECTOR)).isEqualTo(Area.RIGHT);
    }

    @Test
    void equalityReflectsState() {
        assertThat(DockLayout.defaultLayout()).isEqualTo(DockLayout.defaultLayout());
        DockLayout changed = DockLayout.defaultLayout();
        changed.close(PanelKind.GAME);
        assertThat(changed).isNotEqualTo(DockLayout.defaultLayout());
    }

    @Test
    void unknownAreaIdFailsFast() {
        assertThatThrownBy(() -> Area.fromId("middle"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
