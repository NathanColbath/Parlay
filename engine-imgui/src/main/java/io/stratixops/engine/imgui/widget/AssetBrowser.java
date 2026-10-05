package io.stratixops.engine.imgui.widget;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseButton;
import imgui.type.ImString;
import io.stratixops.engine.imgui.theme.Icon;
import io.stratixops.engine.imgui.theme.Theme;
import java.util.List;

/**
 * The Project asset browser (design system section 3.4): breadcrumb, filter
 * chips, view toggle, search and the grid/list body, wired to one shared
 * selection and sort model. Domain-agnostic — it renders {@link AssetItem}s and
 * reports {@link AssetEvent}s for the editor to act on.
 *
 * <p>Keyboard (arrows, Shift extend, Enter open, F2 rename, Delete, Backspace
 * up, Ctrl+F search, Ctrl+A select all) is handled only while the browser body
 * owns the keyboard, so it never steals shortcuts from focused inputs.
 */
public final class AssetBrowser {

    /** The two body layouts. */
    public enum View {
        GRID,
        LIST
    }

    /** What a frame produced: an editor action and/or a breadcrumb navigation. */
    public record Outcome(AssetEvent event, String navigatedPath) {

        /** A no-op frame. */
        public static final Outcome NONE = new Outcome(AssetEvent.NONE, null);

        /** @return whether the frame changed selection only. */
        public boolean isIdle() {
            return !event.isPresent() && navigatedPath == null;
        }
    }

    /** Search field width in logical px (design system 3.4). */
    private static final float SEARCH_WIDTH = 180f;
    private static final String SEARCH_HINT = "Search\u2026";
    private static final int DEFAULT_SEARCH_CAPACITY = 64;

    private final AssetSelection selection = new AssetSelection();
    private final AssetSort sort = new AssetSort();
    private final AssetListColumns columns = new AssetListColumns();
    private final ImString search = new ImString(DEFAULT_SEARCH_CAPACITY);

    private AssetFilter.Criterion filter = AssetFilter.Criterion.ALL;
    private View view = View.GRID;
    private boolean focusSearchRequested;

    public AssetSelection selection() {
        return selection;
    }

    public AssetSort sort() {
        return sort;
    }

    public AssetListColumns columns() {
        return columns;
    }

    public ImString search() {
        return search;
    }

    public View view() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public AssetFilter.Criterion filter() {
        return filter;
    }

    public void setFilter(AssetFilter.Criterion filter) {
        this.filter = filter;
    }

    /** Renders without a drag drop-target highlight. */
    public Outcome render(Theme t, List<AssetItem> items, List<Breadcrumb.Segment> path,
            int activeSegment) {
        return render(t, items, path, activeSegment, -1);
    }

    /**
     * Renders the whole panel at the current cursor.
     *
     * @param path          breadcrumb segments, root first
     * @param activeSegment index of the current folder in {@code path}
     * @param dropSegment   segment highlighted as a drag target, or {@code -1}
     */
    public Outcome render(Theme t, List<AssetItem> items, List<Breadcrumb.Segment> path,
            int activeSegment, int dropSegment) {
        Breadcrumb.Result breadcrumb = breadcrumbRow(t, path, activeSegment, dropSegment);
        if (breadcrumb.activated() != null) {
            return new Outcome(AssetEvent.NONE, breadcrumb.activated().path());
        }

        filter = FilterChips.render(t, filter);
        ImGui.spacing();

        List<AssetItem> visible = sort.sort(AssetFilter.apply(items, filter, search.get()));
        AssetEvent event = AssetEvent.NONE;
        float childHeight = Math.max(ImGui.getContentRegionAvailY(), 1f);
        if (ImGui.beginChild("##asset-view", 0f, childHeight, false)) {
            boolean focused = ImGui.isWindowFocused();
            event = view == View.GRID
                    ? AssetGrid.render(t, visible, selection, focused)
                    : AssetListView.render(t, visible, selection, sort, columns, focused);
            AssetEvent keyboard = keyboard(t, visible);
            if (keyboard.isPresent()) {
                event = keyboard;
            }
        }
        ImGui.endChild();
        return new Outcome(event, null);
    }

    private Breadcrumb.Result breadcrumbRow(Theme t, List<Breadcrumb.Segment> path,
            int activeSegment, int dropSegment) {
        float available = Math.max(ImGui.getContentRegionAvailX(), 1f);
        float toggleWidth = t.size.SIZE_CONTROL_MD * 2f;
        float rightReserve = toggleWidth + t.space.SPACE_2 + SEARCH_WIDTH;
        float breadcrumbAvailable = Math.max(available - rightReserve - t.space.SPACE_2, 1f);

        Breadcrumb.Result result = Breadcrumb.render(t, path, activeSegment, breadcrumbAvailable,
                dropSegment);

        ImGui.sameLine(Math.max(0f, available - rightReserve));
        viewToggle(t);
        ImGui.sameLine(0f, t.space.SPACE_2);
        ImGui.setNextItemWidth(SEARCH_WIDTH);
        if (focusSearchRequested) {
            ImGui.setKeyboardFocusHere();
            focusSearchRequested = false;
        }
        ImGui.inputTextWithHint("##asset-search", SEARCH_HINT, search, ImGuiInputTextFlags.None);
        return result;
    }

    private void viewToggle(Theme t) {
        ImDrawList draw = ImGui.getWindowDrawList();
        View[] views = View.values();
        for (int i = 0; i < views.length; i++) {
            View candidate = views[i];
            if (i > 0) {
                ImGui.sameLine(0f, t.space.SPACE_1);
            }
            float size = t.size.SIZE_CONTROL_MD;
            ImVec2 min = new ImVec2(ImGui.getCursorScreenPos());
            ImGui.invisibleButton("##asset-view-" + candidate.name(), size, size);
            boolean hovered = ImGui.isItemHovered();
            boolean selected = candidate == view;
            int fill = selected ? t.color.ACCENT_SUBTLE
                    : hovered ? t.color.SURFACE_HOVER : t.color.BG_ELEVATED;
            draw.addRectFilled(min.x, min.y, min.x + size, min.y + size, Ui.u32(fill),
                    t.radius.RADIUS_SM);
            draw.addRect(min.x, min.y, min.x + size, min.y + size, Ui.u32(t.color.BORDER_DEFAULT),
                    t.radius.RADIUS_SM, 1f);
            Icon icon = candidate == View.GRID ? Icon.SCENE : Icon.PANEL_HIERARCHY;
            Ui.drawIcon(t, draw, min.x + (size - t.size.SIZE_ICON_MD) * 0.5f,
                    Ui.textBaseline(min.y, size),
                    selected ? t.color.ACCENT_TEXT : t.color.TEXT_SECONDARY, icon);
            if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
                view = candidate;
            }
        }
    }

    private AssetEvent keyboard(Theme t, List<AssetItem> visible) {
        ImGuiIO io = ImGui.getIO();
        if (io.getWantCaptureKeyboard()) {
            return AssetEvent.NONE;
        }
        boolean shift = io.getKeyShift();
        int horizontal = view == View.GRID ? 1 : 0;
        int vertical = view == View.GRID ? gridColumns(t) : 1;

        if (ImGui.isKeyPressed(ImGuiKey.DownArrow)) {
            selection.move(visible, vertical, shift);
        }
        if (ImGui.isKeyPressed(ImGuiKey.UpArrow)) {
            selection.move(visible, -vertical, shift);
        }
        if (horizontal != 0 && ImGui.isKeyPressed(ImGuiKey.RightArrow)) {
            selection.move(visible, horizontal, shift);
        }
        if (horizontal != 0 && ImGui.isKeyPressed(ImGuiKey.LeftArrow)) {
            selection.move(visible, -horizontal, shift);
        }
        if (io.getKeyCtrl() && ImGui.isKeyPressed(ImGuiKey.A)) {
            selection.selectAll(visible);
        }
        if (io.getKeyCtrl() && ImGui.isKeyPressed(ImGuiKey.F)) {
            focusSearchRequested = true;
        }

        String cursor = selection.cursor();
        if (ImGui.isKeyPressed(ImGuiKey.Enter) && cursor != null) {
            return AssetEvent.of(AssetEvent.Action.OPEN, cursor);
        }
        if (ImGui.isKeyPressed(ImGuiKey.F2) && cursor != null) {
            return AssetEvent.of(AssetEvent.Action.RENAME, cursor);
        }
        if (ImGui.isKeyPressed(ImGuiKey.Delete) && cursor != null) {
            return AssetEvent.of(AssetEvent.Action.DELETE, cursor);
        }
        if (ImGui.isKeyPressed(ImGuiKey.Backspace)) {
            return AssetEvent.of(AssetEvent.Action.NAVIGATE_UP, null);
        }
        return AssetEvent.NONE;
    }

    private int gridColumns(Theme t) {
        AssetGridLayout layout = new AssetGridLayout(t.size.SIZE_THUMB_LG, t.space.SPACE_2,
                t.typography.TYPE_SM.lineHeight() * 2f);
        float available = Math.max(ImGui.getContentRegionAvailX(), layout.tile());
        return layout.columns(available);
    }
}
