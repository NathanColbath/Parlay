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
import io.stratixops.engine.imgui.widget.CommandPalette;
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
        }
        ImGui.end();

        CommandPalette.render(t, PALETTE_QUERY, PALETTE_RESULTS, PALETTE_SELECTION);
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
}
