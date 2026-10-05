package io.stratixops.engine.imgui.widget;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import io.stratixops.engine.imgui.theme.Theme;
import imgui.type.ImFloat;

/**
 * Inspector property row (design system section 3.2): a clamped label column
 * and a flexible field column, one widget per inspected kind. Callers supply
 * the backing value; field kinds match the frozen descriptor model (ADR-0007).
 */
public final class PropertyRow {

    private static final float LABEL_MIN = 96f;
    private static final float LABEL_MAX = 200f;
    private static final float LABEL_RATIO = 0.4f;

    private PropertyRow() {
    }

    /** @return the clamped label column width for the current content region. */
    public static float labelWidth() {
        float available = Math.max(ImGui.getContentRegionAvailX(), 1f);
        return Math.min(Math.max(available * LABEL_RATIO, LABEL_MIN), LABEL_MAX);
    }

    private static void label(Theme t, String text) {
        float width = labelWidth();
        Ui.pushColor(ImGuiCol.Text, t.color.TEXT_SECONDARY);
        ImGui.text(Ui.ellipsize(text, width - t.space.SPACE_2));
        Ui.popColor();
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip(text);
        }
        ImGui.sameLine(width + t.space.SPACE_2);
    }

    public static boolean boolField(Theme t, String name, ImBoolean value) {
        label(t, name);
        ImGui.setNextItemWidth(-1f);
        return ImGui.checkbox("##" + name, value);
    }

    public static boolean floatField(Theme t, String name, ImFloat value, float step) {
        label(t, name);
        ImGui.setNextItemWidth(-1f);
        return ImGui.dragFloat("##" + name, value.getData(), step);
    }

    public static boolean intField(Theme t, String name, float[] holder, float step) {
        label(t, name);
        ImGui.setNextItemWidth(-1f);
        return ImGui.dragFloat("##" + name, holder, step);
    }

    public static boolean sliderField(Theme t, String name, ImFloat value, float min, float max) {
        label(t, name);
        ImGui.setNextItemWidth(-1f);
        return ImGui.sliderFloat("##" + name, value.getData(), min, max);
    }

    public static boolean vec2Field(Theme t, String name, float[] xy) {
        label(t, name);
        ImGui.setNextItemWidth(-1f);
        return ImGui.dragFloat2("##" + name, xy);
    }

    public static boolean vec3Field(Theme t, String name, float[] xyz) {
        label(t, name);
        ImGui.setNextItemWidth(-1f);
        return ImGui.dragFloat3("##" + name, xyz);
    }

    public static boolean stringField(Theme t, String name, ImString value) {
        label(t, name);
        ImGui.setNextItemWidth(-1f);
        return ImGui.inputText("##" + name, value, 0);
    }

    public static boolean multilineField(Theme t, String name, ImString value) {
        label(t, name);
        ImGui.setNextItemWidth(-1f);
        return ImGui.inputTextMultiline("##" + name, value, -1f, 60f, 0);
    }

    public static boolean colorField(Theme t, String name, float[] rgba) {
        label(t, name);
        ImGui.setNextItemWidth(-1f);
        return ImGui.colorEdit4("##" + name, rgba);
    }

    public static boolean enumField(Theme t, String name, String current, String[] options,
            ImInt index) {
        label(t, name);
        ImGui.setNextItemWidth(-1f);
        boolean changed = false;
        if (ImGui.beginCombo("##" + name, current)) {
            for (int i = 0; i < options.length; i++) {
                boolean selected = i == index.get();
                if (ImGui.selectable(options[i], selected)) {
                    index.set(i);
                    changed = true;
                }
                if (selected) {
                    ImGui.setItemDefaultFocus();
                }
            }
            ImGui.endCombo();
        }
        return changed;
    }

    /** A computed/read-only row: muted, unframed, disabled interaction. */
    public static void readonly(Theme t, String name, String value) {
        label(t, name);
        Ui.pushColor(ImGuiCol.Text, t.color.TEXT_MUTED);
        ImGui.textUnformatted(value);
        Ui.popColor();
    }

    /** A section header ({@code type.mdMedium}) with an optional collapse chevron. */
    public static boolean section(Theme t, String title, boolean expanded) {
        Ui.pushColor(ImGuiCol.Text, t.color.TEXT_PRIMARY);
        ImGui.text("\u25be " + title);
        Ui.popColor();
        boolean clicked = ImGui.isItemClicked();
        return clicked;
    }
}
