package io.stratixops.engine.imgui.widget;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiMouseCursor;
import io.stratixops.engine.imgui.theme.Theme;
import java.util.List;

/**
 * Asset list view (design system section 3.4): sortable, resizable
 * {@code Name/Type/Size/Modified} columns under a 28 px {@code bg.toolbar}
 * header with 22 px rows using the shared row state model. Sorting writes to
 * {@link AssetSort}; resizing writes to {@link AssetListColumns}.
 */
public final class AssetListView {

    private AssetListView() {
    }

    /** Grab width for a column resize handle. */
    private static final float GRIP = 6f;

    /** Renders the list, headers included. */
    public static AssetEvent render(Theme t, List<AssetItem> items, AssetSelection selection,
            AssetSort sort, AssetListColumns columns, boolean active) {
        return render(t, items, selection, sort, columns, active, -1);
    }

    /**
     * Renders the list.
     *
     * @param dropIndex row highlighted as a drop target, or {@code -1}
     * @return an activation/context event, or {@link AssetEvent#NONE}
     */
    public static AssetEvent render(Theme t, List<AssetItem> items, AssetSelection selection,
            AssetSort sort, AssetListColumns columns, boolean active, int dropIndex) {
        float available = Math.max(ImGui.getContentRegionAvailX(), 1f);
        float headerHeight = t.size.SIZE_PANEL_HEADER_H;
        float rowHeight = t.size.SIZE_ROW_H;
        float totalHeight = headerHeight + items.size() * rowHeight;
        ImVec2 origin = new ImVec2(ImGui.getCursorScreenPos());
        ImGui.dummy(available, Math.max(totalHeight, headerHeight));

        ImDrawList draw = ImGui.getWindowDrawList();
        AssetEvent event = header(t, draw, sort, columns, origin, available, headerHeight);

        float[] starts = columns.columnStarts(available);
        float[] widths = {
            columns.nameWidth(available), columns.typeWidth(), columns.sizeWidth(),
            columns.modifiedWidth()
        };

        for (int i = 0; i < items.size(); i++) {
            AssetItem item = items.get(i);
            float y = origin.y + headerHeight + i * rowHeight;
            ImGui.setCursorScreenPos(origin.x, y);
            ImGui.invisibleButton("##asset-row-" + item.id(), available, rowHeight);
            boolean hovered = ImGui.isItemHovered();
            boolean selected = selection.contains(item.id());
            boolean focused = active && item.id().equals(selection.cursor());
            rowBackground(t, draw, origin.x, y, available, rowHeight, hovered, selected,
                    i == dropIndex, focused);
            rowContent(t, draw, item, origin.x, y, rowHeight, starts, widths, selected);

            if (hovered) {
                ImGui.setTooltip(item.name());
            }
            if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
                if (ImGui.isMouseDoubleClicked(ImGuiMouseButton.Left)) {
                    event = AssetEvent.of(AssetEvent.Action.OPEN, item.id());
                } else {
                    selection.click(items, item.id(), ImGui.getIO().getKeyCtrl(),
                            ImGui.getIO().getKeyShift());
                }
            }
        }

        ImGui.setCursorScreenPos(origin.x, origin.y + totalHeight);
        return event;
    }

    private static AssetEvent header(Theme t, ImDrawList draw, AssetSort sort,
            AssetListColumns columns, ImVec2 origin, float available, float headerHeight) {
        AssetEvent event = AssetEvent.NONE;
        draw.addRectFilled(origin.x, origin.y, origin.x + available, origin.y + headerHeight,
                Ui.u32(t.color.BG_TOOLBAR));
        draw.addRectFilled(origin.x, origin.y + headerHeight - 1f, origin.x + available,
                origin.y + headerHeight, Ui.u32(t.color.BORDER_SUBTLE));

        AssetSort.Column[] keys = {
            AssetSort.Column.NAME, AssetSort.Column.TYPE, AssetSort.Column.SIZE,
            AssetSort.Column.MODIFIED
        };
        float[] starts = columns.columnStarts(available);
        float[] widths = {
            columns.nameWidth(available), columns.typeWidth(), columns.sizeWidth(),
            columns.modifiedWidth()
        };
        float pad = t.space.SPACE_2;
        float baseline = Ui.textBaseline(origin.y, headerHeight);
        for (int c = 0; c < keys.length; c++) {
            float cellX = origin.x + starts[c];
            float cellWidth = Math.max(widths[c], 1f);
            ImGui.setCursorScreenPos(cellX, origin.y);
            ImGui.setNextItemAllowOverlap();
            ImGui.invisibleButton("##asset-head-" + c, cellWidth, headerHeight);
            boolean hovered = ImGui.isItemHovered();
            String label = keys[c].label();
            if (sort.column() == keys[c]) {
                label += sort.direction() == AssetSort.Direction.ASC ? " \u25b2" : " \u25bc";
            }
            int color = hovered || sort.column() == keys[c]
                    ? t.color.TEXT_PRIMARY : t.color.TEXT_MUTED;
            float textX = c == 2
                    ? cellX + cellWidth - pad - ImGui.calcTextSizeX(label)
                    : cellX + pad;
            draw.addText(textX, baseline, Ui.u32(color), label);

            boolean onGrip = columns.handleAt(ImGui.getMousePosX() - origin.x, available) >= 0;
            if (ImGui.isItemClicked(ImGuiMouseButton.Left) && !onGrip) {
                sort.toggle(keys[c]);
            }
        }

        for (int h = 0; h < AssetListColumns.HANDLE_COUNT; h++) {
            float boundary = origin.x + starts[h + 1];
            ImGui.setCursorScreenPos(boundary - GRIP * 0.5f, origin.y);
            ImGui.invisibleButton("##asset-grip-" + h, GRIP, headerHeight);
            boolean hovered = ImGui.isItemHovered();
            boolean resizing = ImGui.isItemActive();
            if (hovered || resizing) {
                ImGui.setMouseCursor(ImGuiMouseCursor.ResizeEW);
            }
            if (resizing && ImGui.getIO().getMouseDeltaX() != 0f) {
                columns.resize(h, ImGui.getIO().getMouseDeltaX());
            }
            int line = resizing ? t.color.ACCENT_DEFAULT : t.color.BORDER_DEFAULT;
            draw.addRectFilled(boundary - 0.5f, origin.y + t.space.SPACE_1, boundary + 0.5f,
                    origin.y + headerHeight - t.space.SPACE_1, Ui.u32(line));
        }
        return event;
    }

    private static void rowBackground(Theme t, ImDrawList draw, float x, float y, float width,
            float height, boolean hovered, boolean selected, boolean dropTarget, boolean focused) {
        float rounding = t.radius.RADIUS_SM;
        if (dropTarget) {
            draw.addRectFilled(x, y, x + width, y + height, Ui.u32(t.color.ACCENT_SUBTLE),
                    rounding);
            draw.addRect(x, y, x + width, y + height, Ui.u32(t.color.ACCENT_DEFAULT), rounding,
                    1f);
        } else if (selected) {
            draw.addRectFilled(x, y, x + width, y + height, Ui.u32(t.color.SURFACE_SELECTED),
                    rounding);
            draw.addRectFilled(x, y, x + 2f, y + height, Ui.u32(t.color.ACCENT_DEFAULT));
        } else if (hovered) {
            draw.addRectFilled(x, y, x + width, y + height, Ui.u32(t.color.SURFACE_HOVER),
                    rounding);
        }
        if (focused) {
            Ui.focusRing(t, x, y, width, height);
        }
    }

    private static void rowContent(Theme t, ImDrawList draw, AssetItem item, float originX,
            float rowY, float rowHeight, float[] starts, float[] widths, boolean selected) {
        float baseline = Ui.textBaseline(rowY, rowHeight);
        float pad = t.space.SPACE_2;

        float nameX = originX + starts[0] + pad;
        Ui.drawIcon(t, draw, nameX, baseline,
                selected ? t.color.ACCENT_TEXT : t.color.TEXT_MUTED, item.kind().icon());
        nameX += t.size.SIZE_ICON_MD;
        float nameRoom = widths[0] - (nameX - (originX + starts[0])) - pad;
        draw.addText(nameX, baseline, Ui.u32(selected ? t.color.TEXT_PRIMARY : t.color.TEXT_SECONDARY),
                Ui.ellipsize(item.name(), nameRoom));

        draw.addText(originX + starts[1] + pad, baseline, Ui.u32(t.color.TEXT_MUTED),
                Ui.ellipsize(item.kind().label(), widths[1] - pad));

        String size = item.isFolder() ? "--" : AssetFormat.size(item.sizeBytes());
        float sizeX = originX + starts[2] + widths[2] - pad - ImGui.calcTextSizeX(size);
        draw.addText(sizeX, baseline, Ui.u32(t.color.TEXT_MUTED), size);

        String modified = item.isFolder() ? "--" : AssetFormat.modified(item.modifiedEpochSeconds());
        draw.addText(originX + starts[3] + pad, baseline, Ui.u32(t.color.TEXT_MUTED),
                Ui.ellipsize(modified, widths[3] - pad * 2f));
    }
}
