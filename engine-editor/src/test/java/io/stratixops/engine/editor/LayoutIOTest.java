package io.stratixops.engine.editor;

import io.stratixops.engine.editor.DockLayout.Area;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LayoutIOTest {

    @Test
    void roundTripsThroughText() {
        DockLayout layout = DockLayout.defaultLayout();
        layout.close(PanelKind.CONSOLE);

        DockLayout parsed = LayoutIO.parse(LayoutIO.serialize(layout));
        assertThat(parsed).isEqualTo(layout);
    }

    @Test
    void saveAndLoad(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("nested/layout.conf");
        DockLayout layout = DockLayout.defaultLayout();

        LayoutIO.save(layout, file);
        assertThat(LayoutIO.load(file)).isEqualTo(layout);
    }

    @Test
    void missingHeaderIsRejected() {
        assertThatThrownBy(() -> LayoutIO.parse("left: HIERARCHY"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("editor-layout/1");
    }

    @Test
    void unknownAreasAndBlankLinesAreIgnored() {
        String text = LayoutIO.SCHEMA + "\n"
                + "\n"
                + "middle: HIERARCHY\n"
                + "left: HIERARCHY\n";
        DockLayout parsed = LayoutIO.parse(text);
        assertThat(parsed.tabs(Area.LEFT)).containsExactly(PanelKind.HIERARCHY);
    }
}
