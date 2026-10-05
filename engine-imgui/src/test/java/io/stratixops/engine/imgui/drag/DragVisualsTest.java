package io.stratixops.engine.imgui.drag;

import static org.assertj.core.api.Assertions.assertThat;

import io.stratixops.engine.imgui.drag.DrawCommand.Kind;
import io.stratixops.engine.imgui.theme.Rgba;
import io.stratixops.engine.imgui.theme.Theme;
import io.stratixops.engine.imgui.theme.ThemeLoader;
import io.stratixops.engine.imgui.theme.ThemeMode;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Headless assertion of the rendered drag draw commands (design system 3.8):
 * every color, radius and elevation must resolve from {@link Theme}.
 */
class DragVisualsTest {

    private static final Theme DARK = ThemeLoader.load(ThemeMode.DARK);
    private static final Theme LIGHT = ThemeLoader.load(ThemeMode.LIGHT);

    @Test
    void idleSessionProducesNoCommands() {
        DragSession session = new DragSession();
        assertThat(DragVisuals.build(DARK, session, 200f)).isEmpty();
    }

    @Test
    void ghostUsesElevationThreeFiftyPercentOpacityAndAccentBorder() {
        DragSession session = dragging(20f, 30f, 100f, 22f);
        List<DrawCommand> commands = DragVisuals.build(DARK, session, 200f);

        DrawCommand shadow = find(commands, Kind.SHADOW);
        assertThat(shadow.color()).isEqualTo(DARK.elev.ELEV_3.rgba());
        assertThat(shadow.x()).isEqualTo(32f + DARK.elev.ELEV_3.dx() - DARK.elev.ELEV_3.blur() * 0.5f);

        DrawCommand fill = find(commands, Kind.GHOST_FILL);
        assertThat(fill.x()).isEqualTo(32f);
        assertThat(fill.y()).isEqualTo(42f);
        assertThat(fill.rounding()).isEqualTo(DARK.radius.RADIUS_MD);
        assertThat(Rgba.alpha(fill.color())).isEqualTo(128);
        assertThat(fill.color() & 0xFFFFFF00).isEqualTo(DARK.color.BG_ELEVATED & 0xFFFFFF00);

        DrawCommand border = find(commands, Kind.GHOST_BORDER);
        assertThat(border.color()).isEqualTo(DARK.elev.ELEV_3_BORDER);
        assertThat(border.thickness()).isEqualTo(1f);
    }

    @Test
    void ghostFillSamplesTheProvidedSurfaceColor() {
        DragSession session = dragging(20f, 30f, 100f, 22f);
        int surface = DARK.color.SURFACE_SELECTED;
        DrawCommand fill = find(DragVisuals.build(DARK, session, 200f, surface), Kind.GHOST_FILL);
        assertThat(fill.color() & 0xFFFFFF00).isEqualTo(surface & 0xFFFFFF00);
        assertThat(Rgba.alpha(fill.color())).isEqualTo(128);
    }

    @Test
    void multiSelectGhostShowsACountBadge() {
        DragSession session = new DragSession();
        session.press(0f, 0f, 100f, 22f,
                new DragPayload("hierarchy", List.of("a", "b", "c")));
        session.move(10f, 10f);

        List<DrawCommand> commands = DragVisuals.build(DARK, session, 200f);
        DrawCommand badge = find(commands, Kind.BADGE_FILL);
        assertThat(badge.color()).isEqualTo(DARK.color.ACCENT_DEFAULT);
        assertThat(badge.rounding()).isEqualTo(DARK.radius.RADIUS_PILL);
        assertThat(find(commands, Kind.BADGE_TEXT).text()).isEqualTo("3");
    }

    @Test
    void singleSelectionHasNoCountBadge() {
        DragSession session = dragging(10f, 10f, 100f, 22f);
        List<DrawCommand> commands = DragVisuals.build(DARK, session, 200f);
        assertThat(commands).noneMatch(c -> c.kind() == Kind.BADGE_FILL);
    }

    @Test
    void copyModifierDrawsThePlusBadge() {
        DragSession session = dragging(10f, 10f, 100f, 22f);
        session.setModifier(DragModifier.COPY);
        List<DrawCommand> commands = DragVisuals.build(DARK, session, 200f);
        assertThat(find(commands, Kind.MODIFIER_FILL).color()).isEqualTo(DARK.color.ACCENT_DEFAULT);
        assertThat(find(commands, Kind.MODIFIER_TEXT).text()).isEqualTo("+");
    }

    @Test
    void linkModifierDrawsTheVioletBadge() {
        DragSession session = dragging(10f, 10f, 100f, 22f);
        session.setModifier(DragModifier.LINK);
        List<DrawCommand> commands = DragVisuals.build(DARK, session, 200f);
        assertThat(find(commands, Kind.MODIFIER_FILL).color()).isEqualTo(DARK.color.LINKING);
    }

    @Test
    void reorderTargetDrawsATwoPixelAccentInsertionLine() {
        DragSession session = dragging(10f, 10f, 100f, 22f);
        session.hover(new DropTarget("row", DropZone.BEFORE, 6f, 100f, 180f, 22f));

        DrawCommand line = find(DragVisuals.build(DARK, session, 300f), Kind.INSERTION_LINE);
        assertThat(line.color()).isEqualTo(DARK.color.ACCENT_DEFAULT);
        assertThat(line.thickness()).isEqualTo(0f);
        assertThat(line.h()).isEqualTo(DragConfig.INSERTION_THICKNESS);
        assertThat(line.x()).isEqualTo(6f);
        assertThat(line.y()).isEqualTo(100f);
        assertThat(line.w()).isEqualTo(300f);
    }

    @Test
    void afterTargetPlacesTheLineAtTheBottomEdge() {
        DragSession session = dragging(10f, 10f, 100f, 22f);
        session.hover(new DropTarget("row", DropZone.AFTER, 6f, 100f, 180f, 22f));
        DrawCommand line = find(DragVisuals.build(DARK, session, 300f), Kind.INSERTION_LINE);
        assertThat(line.y()).isEqualTo(100f + 22f - DragConfig.INSERTION_THICKNESS);
    }

    @Test
    void dropIntoTargetUsesSubtleFillAndAccentBorder() {
        DragSession session = dragging(10f, 10f, 100f, 22f);
        session.hover(new DropTarget("folder", DropZone.INTO, 4f, 40f, 220f, 22f));

        List<DrawCommand> commands = DragVisuals.build(DARK, session, 300f);
        DrawCommand fill = find(commands, Kind.DROP_FILL);
        assertThat(fill.color()).isEqualTo(DARK.color.ACCENT_SUBTLE);
        assertThat(fill.x()).isEqualTo(4f);
        DrawCommand border = find(commands, Kind.DROP_BORDER);
        assertThat(border.color()).isEqualTo(DARK.color.ACCENT_DEFAULT);
        assertThat(border.thickness()).isEqualTo(1f);
    }

    @Test
    void invalidTargetUsesTheErrorBorder() {
        DragSession session = dragging(10f, 10f, 100f, 22f);
        session.hover(new DropTarget("self", DropZone.INVALID, 4f, 40f, 220f, 22f));
        DrawCommand border = find(DragVisuals.build(DARK, session, 300f), Kind.INVALID_BORDER);
        assertThat(border.color()).isEqualTo(DARK.color.STATE_ERROR);
        assertThat(border.thickness()).isEqualTo(1f);
    }

    @Test
    void lightThemeResolvesDifferentAccentTokens() {
        DragSession session = dragging(10f, 10f, 100f, 22f);
        session.hover(new DropTarget("row", DropZone.BEFORE, 0f, 0f, 100f, 22f));
        DrawCommand darkLine = find(DragVisuals.build(DARK, session, 100f), Kind.INSERTION_LINE);
        DrawCommand lightLine = find(DragVisuals.build(LIGHT, session, 100f), Kind.INSERTION_LINE);
        assertThat(darkLine.color()).isEqualTo(DARK.color.ACCENT_DEFAULT);
        assertThat(lightLine.color()).isEqualTo(LIGHT.color.ACCENT_DEFAULT);
        assertThat(darkLine.color()).isNotEqualTo(lightLine.color());
    }

    @Test
    void snappingBackStillDrawsTheGhostWithoutATarget() {
        DragSession session = dragging(10f, 10f, 100f, 22f);
        session.hover(new DropTarget("row", DropZone.INTO, 0f, 0f, 100f, 22f));
        session.cancel(120f);
        List<DrawCommand> commands = DragVisuals.build(DARK, session, 200f);
        assertThat(commands).anyMatch(c -> c.kind() == Kind.GHOST_FILL);
        assertThat(commands).noneMatch(c -> c.kind() == Kind.DROP_FILL || c.kind() == Kind.INSERTION_LINE);
    }

    private static DragSession dragging(float x, float y, float w, float h) {
        DragSession session = new DragSession();
        session.press(0f, 0f, w, h, DragPayload.single("hierarchy", "player"));
        session.move(x, y);
        return session;
    }

    private static DrawCommand find(List<DrawCommand> commands, Kind kind) {
        return commands.stream().filter(c -> c.kind() == kind).findFirst()
                .orElseThrow(() -> new AssertionError("missing draw command " + kind));
    }
}
