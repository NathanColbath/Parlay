package io.stratixops.engine.imgui.widget;

import imgui.ImDrawList;
import imgui.ImVec2;
import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import io.stratixops.engine.imgui.theme.Icon;
import io.stratixops.engine.imgui.theme.Theme;
import java.util.ArrayList;
import java.util.List;

/**
 * Asset grid view (design system section 3.4): 96 px tiles with a 72 px
 * thumbnail, {@code radius.sm}, hover-revealed checkbox and ellipsis, an
 * {@code accent.subtle} selection fill with an {@code accent.default} border,
 * and the missing/unimported placeholders. Selection and activation are
 * reported through {@link AssetEvent}; {@link AssetSelection} is updated in
 * place for pointer gestures.
 */
public final class AssetGrid {

    private AssetGrid() {
    }

    /** The two-line filename block beneath a tile. */
    private static final int LABEL_LINES = 2;

    /**
     * Renders the tile grid at the current cursor.
     *
     * @param selection shared selection/cursor state, updated for clicks
     * @param active    whether the owning panel has keyboard focus
     * @return the activation/context event produced this frame, or
     *         {@link AssetEvent#NONE}
     */
    public static AssetEvent render(Theme t, List<AssetItem> items, AssetSelection selection,
            boolean active) {
        AssetGridLayout layout = new AssetGridLayout(t.size.SIZE_THUMB_LG, t.space.SPACE_2,
                t.typography.TYPE_SM.lineHeight() * LABEL_LINES);
        float available = Math.max(ImGui.getContentRegionAvailX(), layout.tile());
        int columns = layout.columns(available);
        float contentHeight = layout.contentHeight(items.size(), columns);
        ImVec2 origin = new ImVec2(ImGui.getCursorScreenPos());
        ImGui.dummy(available, Math.max(contentHeight, 1f));

        ImDrawList draw = ImGui.getWindowDrawList();
        AssetEvent event = AssetEvent.NONE;
        for (int i = 0; i < items.size(); i++) {
            AssetItem item = items.get(i);
            float x = layout.tileX(i, columns, origin.x);
            float y = layout.tileY(i, columns, origin.y);
            float x1 = x + layout.tile();
            float y1 = y + layout.tile();

            ImGui.setCursorScreenPos(x, y);
            ImGui.setNextItemAllowOverlap();
            ImGui.invisibleButton("##asset-tile-" + item.id(), layout.tile(), layout.tile());
            boolean hovered = ImGui.isItemHovered();
            boolean selected = selection.contains(item.id());
            boolean focused = active && item.id().equals(selection.cursor());

            drawTile(t, draw, item, x, y, x1, y1, hovered, selected, focused);
            drawLabel(t, draw, item, x, x1, y1, selected);

            boolean checkboxClicked = false;
            boolean moreClicked = false;
            if (hovered || selected) {
                checkboxClicked = checkbox(t, draw, item, selection, x, y);
                moreClicked = ellipsis(t, draw, item, x, y, y1);
            }

            if (ImGui.isItemHovered() && !selected && !checkboxClicked && !moreClicked) {
                ImGui.setTooltip(item.name());
            }

            if (checkboxClicked) {
                selection.toggle(item.id());
            } else if (!moreClicked && ImGui.isItemClicked(ImGuiMouseButton.Left)) {
                if (ImGui.isMouseDoubleClicked(ImGuiMouseButton.Left)) {
                    event = AssetEvent.of(AssetEvent.Action.OPEN, item.id());
                } else {
                    selection.click(items, item.id(), ImGui.getIO().getKeyCtrl(),
                            ImGui.getIO().getKeyShift());
                }
            }

            if (moreClicked) {
                ImGui.openPopup("##asset-menu-" + item.id());
            }
            AssetEvent menu = contextMenu(item);
            if (menu.isPresent()) {
                event = menu;
            }
        }

        ImGui.setCursorScreenPos(origin.x, origin.y + contentHeight);
        return event;
    }

    private static void drawTile(Theme t, ImDrawList draw, AssetItem item, float x, float y,
            float x1, float y1, boolean hovered, boolean selected, boolean focused) {
        float rounding = t.radius.RADIUS_SM;
        if (selected) {
            draw.addRectFilled(x, y, x1, y1, Ui.u32(t.color.ACCENT_SUBTLE), rounding);
        } else if (hovered) {
            draw.addRectFilled(x, y, x1, y1, Ui.u32(t.color.SURFACE_HOVER), rounding);
        }

        float thumb = t.size.SIZE_THUMB_MD;
        float tx = x + (t.size.SIZE_THUMB_LG - thumb) * 0.5f;
        float ty = y + (t.size.SIZE_THUMB_LG - thumb) * 0.5f;
        int fill = item.status() == AssetStatus.MISSING
                ? t.color.PLACEHOLDER_MISSING
                : t.color.BG_ELEVATED;
        draw.addRectFilled(tx, ty, tx + thumb, ty + thumb, Ui.u32(fill), rounding);
        draw.addRect(tx, ty, tx + thumb, ty + thumb, Ui.u32(t.color.BORDER_DEFAULT), rounding, 1f);

        float iconX = tx + thumb * 0.5f - t.size.SIZE_ICON_MD * 0.5f;
        float iconY = ty + thumb * 0.5f - ImGui.getTextLineHeight() * 0.5f;
        switch (item.status()) {
            case MISSING -> Ui.drawIcon(t, draw, iconX, iconY, t.color.TEXT_PRIMARY,
                    Icon.CONSOLE_WARN);
            case UNIMPORTED -> spinner(t, draw, tx + thumb * 0.5f, ty + thumb * 0.5f,
                    thumb * 0.25f);
            case READY -> Ui.drawIcon(t, draw, iconX, iconY, t.color.TEXT_MUTED,
                    item.kind().icon());
        }

        if (selected) {
            draw.addRect(x, y, x1, y1, Ui.u32(t.color.ACCENT_DEFAULT), rounding, 1f);
        }
        if (focused) {
            Ui.focusRing(t, x, y, x1 - x, y1 - y);
        }
    }

    private static void drawLabel(Theme t, ImDrawList draw, AssetItem item, float x, float x1,
            float tileBottom, boolean selected) {
        float maxWidth = (x1 - x) - t.space.SPACE_1;
        List<String> lines = wrap(item.name(), maxWidth, LABEL_LINES);
        float lineHeight = t.typography.TYPE_SM.lineHeight();
        int color = selected ? t.color.TEXT_PRIMARY : t.color.TEXT_SECONDARY;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            float width = ImGui.calcTextSizeX(line);
            float lineX = x + Math.max(0f, ((x1 - x) - width) * 0.5f);
            draw.addText(lineX, tileBottom + t.space.SPACE_1 + i * lineHeight, Ui.u32(color), line);
        }
    }

    /** Greedy word wrap to at most {@code maxLines}, ellipsizing the last line. */
    private static List<String> wrap(String text, float maxWidth, int maxLines) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty() || maxWidth <= 0f) {
            return lines;
        }
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (int w = 0; w < words.length; w++) {
            String candidate = current.length() == 0 ? words[w] : current + " " + words[w];
            if (ImGui.calcTextSizeX(candidate) <= maxWidth || current.length() == 0) {
                current.setLength(0);
                current.append(candidate);
                continue;
            }
            lines.add(current.toString());
            current.setLength(0);
            current.append(words[w]);
            if (lines.size() == maxLines - 1) {
                break;
            }
        }
        if (lines.size() < maxLines && current.length() > 0) {
            lines.add(current.toString());
        }
        if (!lines.isEmpty() && lines.size() == maxLines) {
            int last = lines.size() - 1;
            lines.set(last, Ui.ellipsize(lines.get(last), maxWidth));
        }
        return lines;
    }

    private static boolean checkbox(Theme t, ImDrawList draw, AssetItem item,
            AssetSelection selection, float tileX, float tileY) {
        float box = t.size.SIZE_ICON_SM;
        float x = tileX + t.space.SPACE_1;
        float y = tileY + t.space.SPACE_1;
        ImGui.setCursorScreenPos(x, y);
        ImGui.setNextItemAllowOverlap();
        ImGui.invisibleButton("##asset-check-" + item.id(), box, box);
        boolean hovered = ImGui.isItemHovered();
        boolean checked = selection.contains(item.id());
        int fill = checked ? t.color.ACCENT_DEFAULT : t.color.BG_ELEVATED;
        draw.addRectFilled(x, y, x + box, y + box, Ui.u32(fill), t.radius.RADIUS_XS);
        draw.addRect(x, y, x + box, y + box,
                Ui.u32(hovered ? t.color.BORDER_FOCUS : t.color.BORDER_DEFAULT),
                t.radius.RADIUS_XS, 1f);
        if (checked) {
            draw.addLine(x + box * 0.25f, y + box * 0.5f, x + box * 0.45f, y + box * 0.72f,
                    Ui.u32(t.color.TEXT_ON_ACCENT), 1.5f);
            draw.addLine(x + box * 0.45f, y + box * 0.72f, x + box * 0.75f, y + box * 0.28f,
                    Ui.u32(t.color.TEXT_ON_ACCENT), 1.5f);
        }
        return ImGui.isItemClicked(ImGuiMouseButton.Left);
    }

    private static boolean ellipsis(Theme t, ImDrawList draw, AssetItem item, float tileX,
            float tileY, float tileBottom) {
        float box = t.size.SIZE_ICON_SM;
        float x = tileX + t.size.SIZE_THUMB_LG - box - t.space.SPACE_1;
        float y = tileY + t.space.SPACE_1;
        ImGui.setCursorScreenPos(x, y);
        ImGui.setNextItemAllowOverlap();
        ImGui.invisibleButton("##asset-more-" + item.id(), box, box);
        boolean hovered = ImGui.isItemHovered();
        Ui.drawIcon(t, draw, x, y, hovered ? t.color.TEXT_PRIMARY : t.color.TEXT_MUTED,
                Icon.MORE);
        return ImGui.isItemClicked(ImGuiMouseButton.Left);
    }

    private static AssetEvent contextMenu(AssetItem item) {
        AssetEvent event = AssetEvent.NONE;
        if (ImGui.beginPopup("##asset-menu-" + item.id())) {
            if (ImGui.menuItem("Open")) {
                event = AssetEvent.of(AssetEvent.Action.OPEN, item.id());
            }
            if (ImGui.menuItem("Rename")) {
                event = AssetEvent.of(AssetEvent.Action.RENAME, item.id());
            }
            if (ImGui.menuItem("Delete")) {
                event = AssetEvent.of(AssetEvent.Action.DELETE, item.id());
            }
            ImGui.endPopup();
        }
        return event;
    }

    private static void spinner(Theme t, ImDrawList draw, float cx, float cy, float radius) {
        float start = (float) (ImGui.getTime() * 4.0 % (Math.PI * 2.0));
        draw.pathClear();
        draw.pathArcTo(cx, cy, radius, start, start + (float) (Math.PI * 1.4), 24);
        draw.pathStroke(Ui.u32(t.color.ACCENT_DEFAULT), 0, 2f);
    }
}
