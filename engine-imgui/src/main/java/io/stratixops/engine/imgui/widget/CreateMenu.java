package io.stratixops.engine.imgui.widget;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiHoveredFlags;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiSelectableFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import io.stratixops.engine.imgui.theme.Icon;
import io.stratixops.engine.imgui.theme.Theme;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Searchable create menu (design system section 3.3).
 *
 * <p>A popup surface with an autofocused search field, grouped fuzzy-filtered
 * results (matched characters highlighted in {@code accent.text}), pinned quick
 * actions and a Recents group when the query is empty, and full keyboard
 * navigation. The editor supplies the candidate items; the kit owns the surface
 * and returns the activated item id.
 *
 * <p>Implemented as a no-decoration overlay window rather than an ImGui popup so
 * every color and size is read from {@link Theme} and the state is trivially
 * drivable from a headless frame.
 */
public final class CreateMenu {

    /** Spec width (design system 3.3): min 280. */
    static final float WIDTH = 280f;
    /** Spec max height (design system 3.3). */
    static final float MAX_HEIGHT = 400f;
    private static final int MAX_RECENTS = 5;

    private static final String WINDOW_ID = "##create-menu";
    private static final String QUICK_GROUP = "Quick actions";

    /** One creatable entry. */
    public record Item(String id, String label, String category, Icon icon, String subtitle,
            String shortcut, boolean pinned, boolean enabled, String disabledReason) {

        public Item {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("item id must not be blank");
            }
            label = label == null ? "" : label;
            category = category == null || category.isBlank() ? "General" : category;
            icon = icon == null ? Icon.PLACEHOLDER : icon;
        }

        public static Item of(String id, String label, String category, Icon icon) {
            return new Item(id, label, category, icon, null, null, false, true, null);
        }

        public static Item quick(String id, String label, Icon icon) {
            return new Item(id, label, QUICK_GROUP, icon, null, null, true, true, null);
        }

        public static Item action(String id, String label, String category, Icon icon,
                String shortcut) {
            return new Item(id, label, category, icon, null, shortcut, false, true, null);
        }

        public static Item disabled(String id, String label, String category, Icon icon,
                String reason) {
            return new Item(id, label, category, icon, null, null, false, false, reason);
        }
    }

    /** Caller-owned open/filter/selection state. */
    public static final class State {
        public boolean open;
        public float anchorX;
        public float anchorY;
        /** Autofocus the search field on the next frame. */
        public boolean focusSearch;
        public final ImString query = new ImString(64);
        public int selected;
        public final List<String> recents = new ArrayList<>();

        /** Opens at an anchor and requests search focus. */
        public void openAt(float x, float y) {
            this.open = true;
            this.anchorX = x;
            this.anchorY = y;
            this.focusSearch = true;
            this.selected = 0;
            this.query.clear();
        }
    }

    /** One rendered line: a group header ({@code item == null}) or an item row. */
    record Option(String header, Item item, FuzzyMatcher.Match match) {
    }

    private CreateMenu() {
    }

    /**
     * Renders the menu when {@link State#open}.
     *
     * @return the activated item id, or {@code null}. Activation closes the menu.
     */
    public static String render(Theme t, State state, List<Item> items) {
        if (!state.open) {
            return null;
        }
        Ui.elevation(t, t.elev.ELEV_1, state.anchorX, state.anchorY, WIDTH, MAX_HEIGHT,
                t.radius.RADIUS_MD);

        ImGui.setNextWindowPos(state.anchorX, state.anchorY, ImGuiCond.Always);
        ImGui.setNextWindowSize(WIDTH, MAX_HEIGHT, ImGuiCond.Always);

        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, t.radius.RADIUS_MD);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, t.space.SPACE_1, t.space.SPACE_1);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 1f);
        Ui.pushColor(ImGuiCol.WindowBg, t.color.BG_ELEVATED);
        Ui.pushColor(ImGuiCol.Border, t.color.BORDER_DEFAULT);
        Ui.pushColor(ImGuiCol.FrameBg, t.color.BG_INPUT);
        Ui.pushColor(ImGuiCol.Text, t.color.TEXT_PRIMARY);

        int flags = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoSavedSettings
                | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoNav;

        String activated = null;
        if (ImGui.begin(WINDOW_ID, flags)) {
            activated = body(t, state, items);
        }
        ImGui.end();

        Ui.popColor();
        Ui.popColor();
        Ui.popColor();
        Ui.popColor();
        ImGui.popStyleVar(3);

        if (activated != null) {
            state.open = false;
            recordRecent(state, activated);
        }
        return activated;
    }

    private static String body(Theme t, State state, List<Item> items) {
        if (state.focusSearch) {
            ImGui.setKeyboardFocusHere();
            state.focusSearch = false;
        }
        Ui.pushColor(ImGuiCol.Text, t.color.TEXT_MUTED);
        ImGui.textUnformatted(Ui.iconGlyph(Icon.SEARCH));
        Ui.popColor();
        ImGui.sameLine();
        ImGui.setNextItemWidth(-1f);
        boolean submitted = ImGui.inputTextWithHint("##create-search", "Search\u2026", state.query,
                ImGuiInputTextFlags.EnterReturnsTrue);
        ImGui.separator();

        List<Option> options = buildOptions(items, state.query.get(), state.recents);
        int count = 0;
        for (Option option : options) {
            if (option.item() != null) {
                count++;
            }
        }
        clampSelection(state, count);
        navigate(state, count);

        String activated = null;
        int ordinal = -1;
        for (Option option : options) {
            if (option.header() != null) {
                Ui.pushColor(ImGuiCol.Text, t.color.TEXT_MUTED);
                ImGui.textUnformatted(option.header().toUpperCase(Locale.ROOT));
                Ui.popColor();
                continue;
            }
            ordinal++;
            if (row(t, option.item(), option.match(), ordinal == state.selected)) {
                activated = option.item().id();
            }
        }

        if (count == 0) {
            Ui.pushColor(ImGuiCol.Text, t.color.TEXT_MUTED);
            ImGui.textUnformatted("No results for \"" + state.query.get() + "\"");
            Ui.popColor();
        } else if (submitted) {
            Item selected = itemAt(options, state.selected);
            if (selected != null && selected.enabled()) {
                activated = selected.id();
            }
        }
        return activated;
    }

    private static void clampSelection(State state, int count) {
        if (count == 0) {
            state.selected = -1;
        } else if (state.selected < 0) {
            state.selected = 0;
        } else if (state.selected >= count) {
            state.selected = count - 1;
        }
    }

    private static void navigate(State state, int count) {
        if (count > 0) {
            if (ImGui.isKeyPressed(ImGuiKey.DownArrow)) {
                state.selected = (state.selected + 1) % count;
            }
            if (ImGui.isKeyPressed(ImGuiKey.UpArrow)) {
                state.selected = (state.selected - 1 + count) % count;
            }
            if (ImGui.isKeyPressed(ImGuiKey.Tab)) {
                state.selected = (state.selected + 1) % count;
            }
        }
        if (ImGui.isKeyPressed(ImGuiKey.Escape)) {
            state.open = false;
        }
    }

    private static boolean row(Theme t, Item item, FuzzyMatcher.Match match, boolean selected) {
        float rowHeight = t.size.SIZE_CONTROL_MD;
        boolean disabled = !item.enabled();
        if (disabled) {
            ImGui.beginDisabled();
        }
        boolean clicked = ImGui.selectable("##create-" + item.id(), selected,
                ImGuiSelectableFlags.None, 0f, rowHeight);
        ImVec2 min = ImGui.getItemRectMin();
        ImVec2 max = ImGui.getItemRectMax();
        boolean hovered = ImGui.isItemHovered(
                disabled ? ImGuiHoveredFlags.AllowWhenDisabled : ImGuiHoveredFlags.None);
        if (disabled) {
            ImGui.endDisabled();
        }

        ImDrawList draw = ImGui.getWindowDrawList();
        int textColor = disabled ? t.color.TEXT_DISABLED : t.color.TEXT_PRIMARY;
        float textY = Ui.textBaseline(min.y, rowHeight);
        float x = min.x + t.space.SPACE_2;
        Ui.drawIcon(t, draw, x, textY, disabled ? t.color.TEXT_DISABLED : t.color.TEXT_SECONDARY,
                item.icon());
        x += t.size.SIZE_ICON_MD + t.space.SPACE_2;

        float hintLeft = max.x - t.space.SPACE_2;
        if (item.shortcut() != null) {
            hintLeft = Ui.keyHint(t, draw, hintLeft, textY, item.shortcut()) - t.space.SPACE_2;
        }
        float labelRoom = Math.max(hintLeft - x, 1f);
        String label = Ui.ellipsize(item.label(), labelRoom);
        draw.addText(x, textY, Ui.u32(textColor), label);
        highlight(t, draw, x, textY, label, match);

        if (item.subtitle() != null) {
            float subX = x + ImGui.calcTextSizeX(label) + t.space.SPACE_2;
            if (subX < hintLeft) {
                draw.addText(subX, textY, Ui.u32(t.color.TEXT_MUTED),
                        Ui.ellipsize(item.subtitle(), hintLeft - subX));
            }
        }
        if (disabled && item.disabledReason() != null && hovered) {
            ImGui.setTooltip(item.disabledReason());
        }
        return clicked;
    }

    private static void highlight(Theme t, ImDrawList draw, float x, float textY, String label,
            FuzzyMatcher.Match match) {
        if (match == null || match.positions().length == 0) {
            return;
        }
        float cursor = x;
        for (int i = 0; i < label.length(); i++) {
            String glyph = label.substring(i, i + 1);
            if (match.at(i)) {
                draw.addText(cursor, textY, Ui.u32(t.color.ACCENT_TEXT), glyph);
            }
            cursor += ImGui.calcTextSizeX(glyph);
        }
    }

    private static Item itemAt(List<Option> options, int ordinal) {
        int index = -1;
        for (Option option : options) {
            if (option.item() == null) {
                continue;
            }
            index++;
            if (index == ordinal) {
                return option.item();
            }
        }
        return null;
    }

    /**
     * Builds the render order (design system 3.3): pinned quick actions, then the
     * grouped list, then Recents when the query is empty; otherwise the fuzzy
     * matches grouped by category. Pure so the ordering is unit-testable.
     */
    static List<Option> buildOptions(List<Item> items, String query, List<String> recents) {
        List<Option> options = new ArrayList<>();
        String q = query == null ? "" : query.trim();
        if (q.isEmpty()) {
            for (Item item : items) {
                if (item.pinned()) {
                    options.add(new Option(null, item, null));
                }
            }
            Map<String, List<Item>> groups = new LinkedHashMap<>();
            for (Item item : items) {
                if (!item.pinned()) {
                    groups.computeIfAbsent(item.category(), key -> new ArrayList<>()).add(item);
                }
            }
            appendGroups(options, groups);
            appendRecents(options, items, recents);
        } else {
            List<Scored> matches = new ArrayList<>();
            for (Item item : items) {
                FuzzyMatcher.Match byLabel = FuzzyMatcher.match(q, item.label());
                FuzzyMatcher.Match byCategory = byLabel == null
                        ? FuzzyMatcher.match(q, item.category()) : null;
                if (byLabel != null) {
                    matches.add(new Scored(item, byLabel, byLabel.score()));
                } else if (byCategory != null) {
                    matches.add(new Scored(item, null, byCategory.score()));
                }
            }
            matches.sort((a, b) -> {
                int byScore = Integer.compare(b.score(), a.score());
                return byScore != 0 ? byScore : a.item().label().compareToIgnoreCase(b.item().label());
            });
            Map<String, List<Scored>> grouped = new LinkedHashMap<>();
            for (Scored scored : matches) {
                grouped.computeIfAbsent(scored.item().category(), key -> new ArrayList<>())
                        .add(scored);
            }
            for (Map.Entry<String, List<Scored>> entry : grouped.entrySet()) {
                options.add(new Option(entry.getKey(), null, null));
                for (Scored scored : entry.getValue()) {
                    options.add(new Option(null, scored.item(), scored.match()));
                }
            }
        }
        return options;
    }

    private static void appendGroups(List<Option> options, Map<String, List<Item>> groups) {
        for (Map.Entry<String, List<Item>> entry : groups.entrySet()) {
            options.add(new Option(entry.getKey(), null, null));
            for (Item item : entry.getValue()) {
                options.add(new Option(null, item, null));
            }
        }
    }

    private static void appendRecents(List<Option> options, List<Item> items,
            List<String> recents) {
        if (recents == null || recents.isEmpty()) {
            return;
        }
        List<Item> resolved = new ArrayList<>();
        for (String id : recents) {
            for (Item item : items) {
                if (item.id().equals(id)) {
                    resolved.add(item);
                    break;
                }
            }
        }
        if (!resolved.isEmpty()) {
            options.add(new Option("Recent", null, null));
            for (Item item : resolved) {
                options.add(new Option(null, item, null));
            }
        }
    }

    private static void recordRecent(State state, String id) {
        state.recents.remove(id);
        state.recents.add(0, id);
        while (state.recents.size() > MAX_RECENTS) {
            state.recents.remove(state.recents.size() - 1);
        }
    }

    private record Scored(Item item, FuzzyMatcher.Match match, int score) {
    }
}
