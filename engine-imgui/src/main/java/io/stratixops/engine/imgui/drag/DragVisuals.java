package io.stratixops.engine.imgui.drag;

import io.stratixops.engine.imgui.theme.Rgba;
import io.stratixops.engine.imgui.theme.Shadow;
import io.stratixops.engine.imgui.theme.Theme;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the resolved draw commands for an in-flight drag (design system 3.8):
 * the 50 %-opacity {@code elev.3} ghost with its 1 px accent border, the count
 * badge, the modifier badge, the reorder insertion line, the drop-into fill and
 * the invalid-target error border.
 *
 * <p>Every command reads its color, radius and elevation from {@link Theme} — no
 * literal color or token-backed size appears here. The result is pure data, so
 * it is asserted headlessly and then replayed by {@link DragOverlay}.
 */
public final class DragVisuals {

    private DragVisuals() {
    }

    /** @return the draw commands for the current drag state. */
    public static List<DrawCommand> build(Theme theme, DragSession session, float rowWidth) {
        return build(theme, session, rowWidth, theme.color.BG_ELEVATED);
    }

    /**
     * @param ghostFillColor surface color sampled from the source element; the
     *                       ghost applies {@link DragConfig#GHOST_OPACITY} to it
     */
    public static List<DrawCommand> build(Theme theme, DragSession session, float rowWidth,
            int ghostFillColor) {
        List<DrawCommand> commands = new ArrayList<>();
        if (!session.isActive()) {
            return commands;
        }

        float ghostX = session.ghostX();
        float ghostY = session.ghostY();
        float ghostW = Math.max(1f, session.snapshotWidth());
        float ghostH = Math.max(1f, session.snapshotHeight());
        float rounding = theme.radius.RADIUS_MD;

        Shadow shadow = theme.elev.ELEV_3;
        if (!shadow.isNone()) {
            float blur = shadow.blur();
            commands.add(DrawCommand.fill(DrawCommand.Kind.SHADOW,
                    ghostX + shadow.dx() - blur * 0.5f,
                    ghostY + shadow.dy() - blur * 0.5f,
                    ghostW + blur, ghostH + blur, shadow.rgba(), rounding));
        }
        commands.add(DrawCommand.fill(DrawCommand.Kind.GHOST_FILL, ghostX, ghostY, ghostW, ghostH,
                Rgba.withAlpha(ghostFillColor, DragConfig.GHOST_OPACITY), rounding));
        commands.add(DrawCommand.stroke(DrawCommand.Kind.GHOST_BORDER, ghostX, ghostY, ghostW, ghostH,
                theme.elev.ELEV_3_BORDER, rounding, 1f));

        if (session.payload().multi()) {
            float badge = theme.size.SIZE_ICON_MD + theme.space.SPACE_1;
            float bx = ghostX + ghostW - badge * 0.5f;
            float by = ghostY - badge * 0.5f;
            commands.add(DrawCommand.fill(DrawCommand.Kind.BADGE_FILL, bx, by, badge, badge,
                    theme.color.ACCENT_DEFAULT, theme.radius.RADIUS_PILL));
            commands.add(DrawCommand.stroke(DrawCommand.Kind.BADGE_BORDER, bx, by, badge, badge,
                    theme.color.BORDER_DEFAULT, theme.radius.RADIUS_PILL, 1f));
            commands.add(DrawCommand.text(DrawCommand.Kind.BADGE_TEXT, bx + badge * 0.25f,
                    by + theme.space.SPACE_1, theme.color.TEXT_ON_ACCENT,
                    Integer.toString(session.payload().count())));
        }

        if (session.modifier().hasBadge()) {
            float badge = theme.size.SIZE_ICON_MD + theme.space.SPACE_1;
            float bx = ghostX + ghostW - badge * 0.5f;
            float by = ghostY + ghostH - badge * 0.5f;
            int fill = session.modifier() == DragModifier.LINK
                    ? theme.color.LINKING : theme.color.ACCENT_DEFAULT;
            commands.add(DrawCommand.fill(DrawCommand.Kind.MODIFIER_FILL, bx, by, badge, badge,
                    fill, theme.radius.RADIUS_PILL));
            commands.add(DrawCommand.text(DrawCommand.Kind.MODIFIER_TEXT, bx + badge * 0.25f,
                    by + theme.space.SPACE_1, theme.color.TEXT_ON_ACCENT, session.modifier().badge()));
        }

        DropTarget candidate = session.candidate();
        if (candidate != null) {
            float width = Math.max(1f, rowWidth);
            switch (candidate.zone()) {
                case BEFORE, AFTER -> commands.add(DrawCommand.fill(DrawCommand.Kind.INSERTION_LINE,
                        candidate.x(), candidate.insertionY(theme), width, DragConfig.INSERTION_THICKNESS,
                        theme.color.ACCENT_DEFAULT, 0f));
                case INTO -> {
                    commands.add(DrawCommand.fill(DrawCommand.Kind.DROP_FILL, candidate.x(), candidate.y(),
                            candidate.w(), candidate.h(), theme.color.ACCENT_SUBTLE,
                            theme.radius.RADIUS_SM));
                    commands.add(DrawCommand.stroke(DrawCommand.Kind.DROP_BORDER, candidate.x(),
                            candidate.y(), candidate.w(), candidate.h(), theme.color.ACCENT_DEFAULT,
                            theme.radius.RADIUS_SM, 1f));
                }
                case INVALID -> commands.add(DrawCommand.stroke(DrawCommand.Kind.INVALID_BORDER,
                        candidate.x(), candidate.y(), candidate.w(), candidate.h(),
                        theme.color.STATE_ERROR, theme.radius.RADIUS_SM, 1f));
                default -> {
                    // NONE produces no overlay.
                }
            }
        }
        return commands;
    }
}
