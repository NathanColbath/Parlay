package io.stratixops.engine.imgui.widget;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiHoveredFlags;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiSelectableFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import io.stratixops.engine.imgui.theme.Icon;
import io.stratixops.engine.imgui.theme.Theme;
import java.util.ArrayList;
import java.util.List;

/**
 * Context menu (design system section 3.7).
 *
 * <p>A themed popup surface: content width 180-360, 24 px rows with an icon,
 * label and right-aligned {@code KeyHint}, 1 px {@code border.subtle}
 * separators, checkable/radio rows, disabled rows with a reason tooltip,
 * submenus opened after the 120 ms hover delay and flipped near the right edge,
 * and type-ahead. The editor supplies the entries and receives the activated id.
 *
 * <p>Rendered as a no-decoration window so every color and size comes from
 * {@link Theme} and the open state is drivable from a headless frame.
 */
public final class ContextMenu {

    /** Spec content width bounds (design system 3.7). */
    static final float MIN_WIDTH = 180f;
    static final float MAX_WIDTH = 360f;
    private static final float MAX_HEIGHT = 400f;
    /** Type-ahead buffer resets after this long with no key (design system 3.7). */
    private static final double TYPE_AHEAD_TIMEOUT = 1.0;

    private static final String ROOT_ID = "##context-menu";
    private static final String SUBMENU_ID = "##context-submenu";

    public enum Kind {
        ITEM,
        SUBMENU,
        SEPARATOR
    }

    /** One menu entry. Use the static factories rather than the constructor. */
    public record Entry(Kind kind, String id, String label, Icon icon, String shortcut,
            boolean enabled, String disabledReason, boolean showCheck, boolean radio,
            boolean checked, List<Entry> children) {

        public Entry {
            kind = kind == null ? Kind.ITEM : kind;
            icon = icon == null ? Icon.PLACEHOLDER : icon;
            children = children == null ? List.of() : List.copyOf(children);
        }

        public static Entry item(String id, String label, Icon icon) {
            return item(id, label, icon, null);
        }

        public static Entry item(String id, String label, Icon icon, String shortcut) {
            return new Entry(Kind.ITEM, id, label, icon, shortcut, true, null, false, false, false,
                    null);
        }

        public static Entry checkable(String id, String label, Icon icon, boolean checked) {
            return new Entry(Kind.ITEM, id, label, icon, null, true, null, true, false, checked,
                    null);
        }

        public static Entry radio(String id, String label, Icon icon, boolean checked) {
            return new Entry(Kind.ITEM, id, label, icon, null, true, null, true, true, checked,
                    null);
        }

        public static Entry submenu(String id, String label, Icon icon, List<Entry> children) {
            return new Entry(Kind.SUBMENU, id, label, icon, null, true, null, false, false, false,
                    children);
        }

        public static Entry disabled(String id, String label, Icon icon, String reason) {
            return new Entry(Kind.ITEM, id, label, icon, null, false, reason, false, false, false,
                    null);
        }

        public static Entry separator() {
            return new Entry(Kind.SEPARATOR, null, null, null, null, true, null, false, false,
                    false, null);
        }
    }

    /** Caller-owned open/selection/submenu state. */
    public static final class State {
        public boolean open;
        public float x;
        public float y;
        public int selected = -1;
        public String typeAhead = "";
        public double typeAheadTime;
        public int openSubmenu = -1;
        public boolean submenuVisible;
        public double submenuHoverStart;
        public float submenuAnchorX;
        public float submenuAnchorY;
        public float parentLeft;

        /** Opens at a screen position and resets transient state. */
        public void openAt(float x, float y) {
            this.open = true;
            this.x = x;
            this.y = y;
            this.selected = -1;
            this.typeAhead = "";
            this.openSubmenu = -1;
            this.submenuVisible = false;
        }
    }

    private ContextMenu() {
    }

    /**
     * Renders the menu when {@link State#open}.
     *
     * @return the activated entry id, or {@code null}. Activation closes the menu.
     */
    public static String render(Theme t, State state, List<Entry> entries) {
        if (!state.open) {
            return null;
        }
        applyTypeAhead(state, selectableLabels(entries));
        String activated = renderMenu(t, state, entries, state.x, state.y, true, ROOT_ID);
        if (activated != null) {
            state.open = false;
        }
        return activated;
    }

    private static String renderMenu(Theme t, State state, List<Entry> entries, float x, float y,
            boolean root, String windowId) {
        ImVec2 display = ImGui.getIO().getDisplaySize();
        float inset = t.space.SPACE_2;
        float width = measureWidth(t, entries);
        float height = measureHeight(t, entries);
        float px = clamp(x, inset, Math.max(inset, display.x - width - inset));
        float py = clamp(y, inset, Math.max(inset, display.y - height - inset));
        Ui.elevation(t, t.elev.ELEV_1, px, py, width, height, t.radius.RADIUS_MD);

        ImGui.setNextWindowPos(px, py, ImGuiCond.Always);
        ImGui.setNextWindowSizeConstraints(MIN_WIDTH, 0f, MAX_WIDTH, MAX_HEIGHT);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, t.radius.RADIUS_MD);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, t.space.SPACE_1, t.space.SPACE_1);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 1f);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, t.space.SPACE_1, t.space.SPACE_1);
        Ui.pushColor(ImGuiCol.WindowBg, t.color.BG_ELEVATED);
        Ui.pushColor(ImGuiCol.Border, t.color.BORDER_DEFAULT);
        Ui.pushColor(ImGuiCol.Text, t.color.TEXT_PRIMARY);
        Ui.pushColor(ImGuiCol.Header, t.color.SURFACE_SELECTED);
        Ui.pushColor(ImGuiCol.HeaderHovered, t.color.SURFACE_HOVER);
        Ui.pushColor(ImGuiCol.HeaderActive, t.color.SURFACE_SELECTED);

        int flags = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoSavedSettings
                | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoNav
                | ImGuiWindowFlags.AlwaysAutoResize
                | ImGuiWindowFlags.NoBringToFrontOnFocus;

        String activated = null;
        if (ImGui.begin(windowId, flags)) {
            if (root) {
                int count = selectableCount(entries);
                state.selected = clampSelection(state.selected, count);
            }
            float windowLeft = ImGui.getWindowPosX();
            int ordinal = -1;
            for (int i = 0; i < entries.size(); i++) {
                Entry entry = entries.get(i);
                if (entry.kind() == Kind.SEPARATOR) {
                    ImGui.separator();
                    continue;
                }
                ordinal++;
                boolean selected = root && ordinal == state.selected;
                String clicked = row(t, entry, selected, windowLeft, root, i, state);
                if (clicked != null && entry.kind() == Kind.ITEM) {
                    activated = clicked;
                }
            }
            if (root) {
                activated = keyboard(t, state, entries, activated);
            }
        }
        ImGui.end();

        int pops = 6;
        for (int i = 0; i < pops; i++) {
            Ui.popColor();
        }
        ImGui.popStyleVar(4);

        if (root && state.openSubmenu >= 0 && state.openSubmenu < entries.size()
                && state.submenuVisible) {
            Entry parent = entries.get(state.openSubmenu);
            if (parent.kind() == Kind.SUBMENU) {
                float submenuWidth = measureWidth(t, parent.children());
                float sx = state.submenuAnchorX;
                if (sx + submenuWidth > display.x - inset) {
                    sx = state.parentLeft - submenuWidth - t.space.SPACE_1;
                }
                String sub = renderMenu(t, state, parent.children(), sx, state.submenuAnchorY,
                        false, SUBMENU_ID);
                if (sub != null) {
                    activated = sub;
                }
            }
        }
        return activated;
    }

    /** Renders one non-separator row and updates submenu hover state. @return id if clicked. */
    private static String row(Theme t, Entry entry, boolean selected, float windowLeft,
            boolean root, int index, State state) {
        float rowHeight = t.size.SIZE_CONTROL_MD;
        boolean disabled = !entry.enabled();
        if (disabled) {
            ImGui.beginDisabled();
        }
        boolean clicked = ImGui.selectable("##ctx-" + windowLeft + "-" + index, selected,
                ImGuiSelectableFlags.None, 0f, rowHeight);
        if (disabled) {
            ImGui.endDisabled();
        }
        ImVec2 min = ImGui.getItemRectMin();
        ImVec2 max = ImGui.getItemRectMax();
        boolean hovered = ImGui.isItemHovered(
                disabled ? ImGuiHoveredFlags.AllowWhenDisabled : ImGuiHoveredFlags.None);

        ImDrawList draw = ImGui.getWindowDrawList();
        float textY = Ui.textBaseline(min.y, rowHeight);
        float lineHeight = ImGui.getTextLineHeight();
        float x = min.x + t.space.SPACE_2;
        int markColor = entry.checked() ? t.color.ACCENT_TEXT : t.color.TEXT_MUTED;
        if (entry.showCheck()) {
            float centerY = textY + lineHeight * 0.5f;
            float mark = t.size.SIZE_ICON_SM;
            if (entry.radio()) {
                drawRadio(draw, x + mark * 0.5f, centerY, mark * 0.45f, markColor);
            } else if (entry.checked()) {
                drawCheck(draw, x, textY + 2f, mark, markColor);
            }
            x += t.size.SIZE_ICON_MD;
        }
        int iconColor = disabled ? t.color.TEXT_DISABLED : t.color.TEXT_SECONDARY;
        Ui.drawIcon(t, draw, x, textY, iconColor, entry.icon());
        x += t.size.SIZE_ICON_MD + t.space.SPACE_1;

        float right = max.x - t.space.SPACE_2;
        if (entry.kind() == Kind.SUBMENU) {
            drawChevron(draw, right - t.size.SIZE_ICON_SM, textY + lineHeight * 0.5f,
                    t.size.SIZE_ICON_SM, t.color.TEXT_MUTED);
            right -= t.size.SIZE_ICON_SM + t.space.SPACE_1;
        } else if (entry.shortcut() != null) {
            right = Ui.keyHint(t, draw, right, textY, entry.shortcut()) - t.space.SPACE_2;
        }
        int labelColor = disabled ? t.color.TEXT_DISABLED : t.color.TEXT_PRIMARY;
        draw.addText(x, textY, Ui.u32(labelColor), Ui.ellipsize(entry.label(), right - x));

        if (disabled && entry.disabledReason() != null && hovered) {
            ImGui.setTooltip(entry.disabledReason());
        }
        if (entry.kind() == Kind.SUBMENU && root) {
            updateSubmenu(state, index, hovered, clicked, min, max, windowLeft, t);
        }
        return clicked ? entry.id() : null;
    }

    private static void updateSubmenu(State state, int index, boolean hovered, boolean clicked,
            ImVec2 min, ImVec2 max, float windowLeft, Theme t) {
        double delaySeconds = t.motion.duration(t.motion.MOTION_FAST, false) / 1000.0;
        if (clicked) {
            state.openSubmenu = index;
            state.submenuVisible = true;
        } else if (hovered) {
            if (state.openSubmenu != index) {
                state.openSubmenu = index;
                state.submenuVisible = false;
                state.submenuHoverStart = ImGui.getTime();
            } else if (!state.submenuVisible
                    && ImGui.getTime() - state.submenuHoverStart >= delaySeconds) {
                state.submenuVisible = true;
            }
        }
        if (state.openSubmenu == index) {
            state.submenuAnchorX = max.x + t.space.SPACE_1;
            state.submenuAnchorY = min.y;
            state.parentLeft = windowLeft;
        }
    }

    private static String keyboard(Theme t, State state, List<Entry> entries, String activated) {
        int count = selectableCount(entries);
        if (count > 0) {
            if (ImGui.isKeyPressed(ImGuiKey.DownArrow)) {
                state.selected = (clampSelection(state.selected, count) + 1) % count;
            }
            if (ImGui.isKeyPressed(ImGuiKey.UpArrow)) {
                state.selected = (clampSelection(state.selected, count) - 1 + count) % count;
            }
            if (ImGui.isKeyPressed(ImGuiKey.Enter)) {
                Entry entry = nthSelectable(entries, state.selected);
                if (entry != null && entry.enabled()) {
                    if (entry.kind() == Kind.SUBMENU) {
                        state.openSubmenu = indexOf(entries, entry);
                        state.submenuVisible = true;
                    } else {
                        activated = entry.id();
                    }
                }
            }
            if (ImGui.isKeyPressed(ImGuiKey.RightArrow)) {
                Entry entry = nthSelectable(entries, state.selected);
                if (entry != null && entry.kind() == Kind.SUBMENU) {
                    state.openSubmenu = indexOf(entries, entry);
                    state.submenuVisible = true;
                }
            }
        }
        if (ImGui.isKeyPressed(ImGuiKey.LeftArrow) || ImGui.isKeyPressed(ImGuiKey.Escape)) {
            if (state.submenuVisible) {
                state.submenuVisible = false;
                state.openSubmenu = -1;
            } else {
                state.open = false;
            }
        }
        return activated;
    }

    private static void applyTypeAhead(State state, List<String> labels) {
        boolean changed = false;
        for (int key = ImGuiKey.A; key <= ImGuiKey.Z; key++) {
            if (ImGui.isKeyPressed(key)) {
                state.typeAhead += (char) ('a' + (key - ImGuiKey.A));
                changed = true;
            }
        }
        if (changed) {
            state.typeAheadTime = ImGui.getTime();
        } else if (!state.typeAhead.isEmpty()
                && ImGui.getTime() - state.typeAheadTime > TYPE_AHEAD_TIMEOUT) {
            state.typeAhead = "";
        }
        if (!state.typeAhead.isEmpty()) {
            int index = Ui.typeAheadIndex(labels, state.typeAhead);
            if (index >= 0) {
                state.selected = index;
            }
        }
    }

    /** @return the label of every selectable row (separators excluded), in order. */
    static List<String> selectableLabels(List<Entry> entries) {
        List<String> labels = new ArrayList<>();
        for (Entry entry : entries) {
            if (entry.kind() != Kind.SEPARATOR) {
                labels.add(entry.label());
            }
        }
        return labels;
    }

    private static int selectableCount(List<Entry> entries) {
        int count = 0;
        for (Entry entry : entries) {
            if (entry.kind() != Kind.SEPARATOR) {
                count++;
            }
        }
        return count;
    }

    private static Entry nthSelectable(List<Entry> entries, int ordinal) {
        if (ordinal < 0) {
            return null;
        }
        int index = -1;
        for (Entry entry : entries) {
            if (entry.kind() == Kind.SEPARATOR) {
                continue;
            }
            index++;
            if (index == ordinal) {
                return entry;
            }
        }
        return null;
    }

    private static int indexOf(List<Entry> entries, Entry target) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i) == target) {
                return i;
            }
        }
        return -1;
    }

    private static int clampSelection(int selected, int count) {
        if (count == 0) {
            return -1;
        }
        if (selected < 0) {
            return 0;
        }
        return Math.min(selected, count - 1);
    }

    static float measureWidth(Theme t, List<Entry> entries) {
        float max = MIN_WIDTH;
        float iconRoom = t.size.SIZE_ICON_MD + t.space.SPACE_2;
        float padding = t.space.SPACE_2 * 2f + t.space.SPACE_1 * 2f;
        for (Entry entry : entries) {
            if (entry.kind() == Kind.SEPARATOR) {
                continue;
            }
            float width = padding + iconRoom + ImGui.calcTextSizeX(entry.label())
                    + (entry.showCheck() ? t.size.SIZE_ICON_MD : 0f);
            if (entry.shortcut() != null) {
                width += t.space.SPACE_2 + ImGui.calcTextSizeX(entry.shortcut()) + t.space.SPACE_1 * 2f;
            }
            if (entry.kind() == Kind.SUBMENU) {
                width += t.size.SIZE_ICON_SM + t.space.SPACE_1;
            }
            max = Math.max(max, width);
        }
        return Math.min(max, MAX_WIDTH);
    }

    private static float measureHeight(Theme t, List<Entry> entries) {
        float height = t.space.SPACE_1 * 2f;
        for (Entry entry : entries) {
            height += t.space.SPACE_1;
            height += entry.kind() == Kind.SEPARATOR ? 1f : t.size.SIZE_CONTROL_MD;
        }
        return height;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void drawCheck(ImDrawList draw, float x, float y, float size, int color) {
        draw.addLine(x + size * 0.15f, y + size * 0.55f, x + size * 0.4f, y + size * 0.8f, color,
                1.5f);
        draw.addLine(x + size * 0.4f, y + size * 0.8f, x + size * 0.85f, y + size * 0.2f, color,
                1.5f);
    }

    private static void drawRadio(ImDrawList draw, float cx, float cy, float radius, int color) {
        draw.addCircle(cx, cy, radius, color, 12, 1.5f);
        draw.addCircleFilled(cx, cy, radius * 0.45f, color, 12);
    }

    private static void drawChevron(ImDrawList draw, float x, float y, float size, int color) {
        draw.addLine(x, y - size * 0.35f, x + size * 0.35f, y, color, 1.5f);
        draw.addLine(x + size * 0.35f, y, x, y + size * 0.35f, color, 1.5f);
    }
}
