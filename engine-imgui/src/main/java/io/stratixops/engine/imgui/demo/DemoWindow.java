package io.stratixops.engine.imgui.demo;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import io.stratixops.engine.imgui.theme.Icon;
import io.stratixops.engine.imgui.theme.Theme;
import io.stratixops.engine.imgui.widget.AssetBrowser;
import io.stratixops.engine.imgui.widget.AssetItem;
import io.stratixops.engine.imgui.widget.AssetKind;
import io.stratixops.engine.imgui.widget.AssetStatus;
import io.stratixops.engine.imgui.widget.Breadcrumb;
import io.stratixops.engine.imgui.widget.CommandPalette;
import io.stratixops.engine.imgui.widget.ContextMenu;
import io.stratixops.engine.imgui.widget.CreateMenu;
import io.stratixops.engine.imgui.widget.PropertyRow;
import io.stratixops.engine.imgui.widget.Toast;
import io.stratixops.engine.imgui.widget.TreeRow;
import io.stratixops.engine.imgui.widget.Ui;
import java.util.List;

/**
 * Demo window for the design-system kit (design system AC2/AC4). Renders the
 * widget kit and every interaction state so the kit can be reviewed at each DPI
 * scale in dark and light themes without a scene.
 */
public final class DemoWindow {

    public static final String TITLE = "StratixOps UI Kit";

    private static final ImBoolean BOOL_VALUE = new ImBoolean(true);
    private static final ImFloat FLOAT_VALUE = new ImFloat(3.5f);
    private static final float[] VEC3 = {1f, 2f, 0f};
    private static final ImString STRING_VALUE = new ImString("Player", 64);
    private static final ImInt ENUM_INDEX = new ImInt(1);
    private static final float[] COLOR_VALUE = {0.08f, 0.78f, 0.66f, 1f};
    private static final String[] ENUM_OPTIONS = {"Default", "Sprite", "Animated"};
    private static final ImString PALETTE_QUERY = new ImString(64);
    private static final ImInt PALETTE_SELECTION = new ImInt(1);
    private static final List<String> PALETTE_RESULTS = List.of(
            "\u0000Window",
            "Toggle Scene",
            "Toggle Inspector",
            "\u0000GameObject",
            "Create Empty");
    private static final CreateMenu.State CREATE_MENU = new CreateMenu.State();
    private static final List<CreateMenu.Item> CREATE_ITEMS = List.of(
            CreateMenu.Item.quick("empty", "Create Empty", Icon.ADD),
            CreateMenu.Item.quick("empty-child", "Create Empty Child", Icon.CREATE_CHILD),
            CreateMenu.Item.of("sprite", "Sprite Renderer", "Rendering", Icon.SPRITE_RENDERER),
            CreateMenu.Item.of("camera", "Camera", "Rendering", Icon.CAMERA),
            CreateMenu.Item.of("animator", "Animator", "Animation", Icon.ANIMATOR),
            CreateMenu.Item.of("collider", "Box Collider 2D", "Physics", Icon.COLLIDER_2D),
            CreateMenu.Item.of("audio", "AudioSource", "Audio", Icon.AUDIO_SOURCE),
            CreateMenu.Item.of("behaviour", "MonoBehaviour", "Scripts", Icon.BEHAVIOUR));
    private static final ContextMenu.State CONTEXT_MENU = new ContextMenu.State();
    private static final List<ContextMenu.Entry> CONTEXT_ITEMS = List.of(
            ContextMenu.Entry.item("duplicate", "Duplicate", Icon.DUPLICATE, "Ctrl+D"),
            ContextMenu.Entry.item("rename", "Rename", Icon.RENAME, "F2"),
            ContextMenu.Entry.submenu("add", "Add Component", Icon.ADD, List.of(
                    ContextMenu.Entry.item("sprite", "Sprite Renderer", Icon.SPRITE_RENDERER),
                    ContextMenu.Entry.item("animator", "Animator", Icon.ANIMATOR),
                    ContextMenu.Entry.separator(),
                    ContextMenu.Entry.item("audio", "AudioSource", Icon.AUDIO_SOURCE))),
            ContextMenu.Entry.separator(),
            ContextMenu.Entry.checkable("gizmos", "Show gizmos", Icon.SNAP, true),
            ContextMenu.Entry.radio("move", "Move tool", Icon.GIZMO_MOVE, true),
            ContextMenu.Entry.radio("rotate", "Rotate tool", Icon.GIZMO_ROTATE, false),
            ContextMenu.Entry.disabled("delete", "Delete", Icon.DELETE, "Locked by prefab"));

    static {
        CREATE_MENU.recents.add("camera");
    }

    private static final AssetBrowser ASSET_BROWSER = new AssetBrowser();
    private static final List<Breadcrumb.Segment> ASSET_PATH = List.of(
            new Breadcrumb.Segment("Project", ""),
            new Breadcrumb.Segment("assets", "assets"),
            new Breadcrumb.Segment("sprites", "assets/sprites"));
    private static final List<AssetItem> ASSET_ITEMS = List.of(
            AssetItem.folder("assets/sprites/characters", "characters"),
            new AssetItem("assets/sprites/player.png", "player_idle.png", AssetKind.IMAGE,
                    24_576L, 1_752_000_000L, AssetStatus.READY),
            new AssetItem("assets/sprites/enemy.png", "enemy_run.png", AssetKind.IMAGE,
                    18_432L, 1_751_900_000L, AssetStatus.READY),
            new AssetItem("assets/sprites/gem.png", "gem.png", AssetKind.IMAGE,
                    4_096L, 1_751_800_000L, AssetStatus.MISSING),
            new AssetItem("assets/sprites/portal.png", "portal.png", AssetKind.IMAGE,
                    0L, 0L, AssetStatus.UNIMPORTED),
            AssetItem.of("assets/scenes/gem-dash.scene", "gem-dash.scene", AssetKind.SCENE),
            AssetItem.of("assets/prefabs/spikebot.prefab", "spikebot.prefab", AssetKind.PREFAB),
            AssetItem.of("assets/scripts/PlayerController.java", "PlayerController.java",
                    AssetKind.SCRIPT),
            AssetItem.of("assets/audio/theme.ogg", "theme.ogg", AssetKind.AUDIO),
            AssetItem.of("assets/fonts/pixel.ttf", "pixel.ttf", AssetKind.FONT));

    private DemoWindow() {
    }

    /** The DPI scales the demo is verified at (design system section 6, AC2). */
    public static float[] verifiedDpiScales() {
        return new float[] {1f, 1.5f, 2f};
    }

    /** Renders the whole kit for a theme and DPI scale. */
    public static void render(Theme t, float dpiScale) {
        ImGui.setNextWindowSize(760f, 680f, ImGuiCond.FirstUseEver);
        if (ImGui.begin(TITLE + " (" + dpiScale + "x)")) {
            header(t, dpiScale);
            swatches(t);
            hierarchySection(t);
            inspectorSection(t);
            assetSection(t);
        }
        ImGui.end();

        // The create and context menus are popup surfaces; the gallery pins them
        // open at fixed anchors so both render at every theme and DPI scale.
        CREATE_MENU.open = true;
        CREATE_MENU.anchorX = 40f;
        CREATE_MENU.anchorY = 72f;
        CONTEXT_MENU.open = true;
        CONTEXT_MENU.x = ImGui.getIO().getDisplaySizeX() - 420f;
        CONTEXT_MENU.y = 160f;

        CommandPalette.render(t, PALETTE_QUERY, PALETTE_RESULTS, PALETTE_SELECTION);
        CreateMenu.render(t, CREATE_MENU, CREATE_ITEMS);
        ContextMenu.render(t, CONTEXT_MENU, CONTEXT_ITEMS);
        Toast.render(t, List.of(
                new Toast.Message("Scene saved", "gem-dash.scene", Toast.Variant.SUCCESS),
                new Toast.Message("Missing asset", "player.png is not imported", Toast.Variant.ERROR),
                new Toast.Message("Importing", "3 of 12 assets", Toast.Variant.PROGRESS)));
    }

    private static void header(Theme t, float dpiScale) {
        Ui.pushColor(imgui.flag.ImGuiCol.Text, t.color.TEXT_PRIMARY);
        ImGui.textUnformatted("StratixOps editor surface \u2014 " + t.mode.id() + " \u00b7 "
                + dpiScale + "x");
        Ui.popColor();
        ImGui.separator();
    }

    private static void swatches(Theme t) {
        ImDrawList draw = ImGui.getWindowDrawList();
        float x = ImGui.getCursorScreenPosX();
        float y = ImGui.getCursorScreenPosY();
        float size = t.size.SIZE_COLOR_SWATCH;
        int[] colors = {
            t.color.BG_PANEL, t.color.BG_ELEVATED, t.color.ACCENT_DEFAULT, t.color.SURFACE_SELECTED,
            t.color.STATE_SUCCESS, t.color.STATE_WARN, t.color.STATE_ERROR, t.color.STATE_INFO
        };
        for (int i = 0; i < colors.length; i++) {
            float cx = x + i * (size + t.space.SPACE_1);
            draw.addRectFilled(cx, y, cx + size, y + size, Ui.u32(colors[i]), t.radius.RADIUS_SM);
            draw.addRect(cx, y, cx + size, y + size, Ui.u32(t.color.BORDER_DEFAULT),
                    t.radius.RADIUS_SM, 1f);
        }
        ImGui.dummy(colors.length * (size + t.space.SPACE_1), size);
        ImGui.spacing();
    }

    private static void hierarchySection(Theme t) {
        PropertyRow.section(t, "Hierarchy", true);
        TreeRow.row(t, "root", "Scene", Icon.SCENE, 0, true, true, false, false, false);
        TreeRow.row(t, "player", "Player", Icon.SPRITE_RENDERER, 1, false, false, true, true, false);
        TreeRow.row(t, "camera", "Main Camera", Icon.CAMERA, 1, false, false, false, false, false);
        TreeRow.row(t, "audio", "AudioSource", Icon.AUDIO_SOURCE, 2, false, false, false, false, true);
        ImGui.spacing();
    }

    private static void inspectorSection(Theme t) {
        PropertyRow.section(t, "Inspector \u2014 SpriteRenderer", true);
        ImGui.pushID("inspector");
        PropertyRow.boolField(t, "Enabled", BOOL_VALUE);
        PropertyRow.stringField(t, "Name", STRING_VALUE);
        PropertyRow.enumField(t, "Mode", ENUM_OPTIONS[ENUM_INDEX.get()], ENUM_OPTIONS, ENUM_INDEX);
        PropertyRow.floatField(t, "Opacity", FLOAT_VALUE, 0.01f);
        PropertyRow.vec3Field(t, "Position", VEC3);
        PropertyRow.colorField(t, "Tint", COLOR_VALUE);
        PropertyRow.readonly(t, "Guid", "8f2c\u2026a19d");
        ImGui.popID();
    }

    private static void assetSection(Theme t) {
        PropertyRow.section(t, "Project \u2014 asset browser", true);
        ASSET_BROWSER.render(t, ASSET_ITEMS, ASSET_PATH, ASSET_PATH.size() - 1);
        ImGui.spacing();
    }
}
