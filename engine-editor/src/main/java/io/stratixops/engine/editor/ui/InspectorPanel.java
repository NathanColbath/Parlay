package io.stratixops.engine.editor.ui;

import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import io.stratixops.engine.editor.command.EditCommands;
import io.stratixops.engine.editor.PanelKind;
import io.stratixops.engine.editor.inspect.FieldRow;
import io.stratixops.engine.editor.inspect.InspectorModel;
import io.stratixops.engine.imgui.theme.Icon;
import io.stratixops.engine.imgui.theme.Rgba;
import io.stratixops.engine.imgui.theme.Theme;
import io.stratixops.engine.imgui.widget.CreateMenu;
import io.stratixops.engine.imgui.widget.PropertyRow;
import io.stratixops.engine.inspect.ComponentDescriptor;
import io.stratixops.engine.inspect.FieldDescriptor;
import io.stratixops.engine.inspect.PropertyKind;
import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.scene.Component;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Inspector panel (STR-40): auto-draws rows from the headless
 * {@link InspectorModel} with one {@link PropertyRow} control per
 * {@link PropertyKind}. Every edit is dispatched as an
 * {@code EditCommands.setField} / {@code setEnabled} command so it is undoable,
 * and components are attached from the {@code ComponentRegistry} descriptors
 * (built-ins and discovered behaviours).
 */
final class InspectorPanel {

    private final CreateMenu.State addMenu = new CreateMenu.State();
    private final ImString nameBuffer = new ImString(64);
    private final Map<String, ImString> strings = new HashMap<>();
    private final Map<String, ImFloat> floats = new HashMap<>();
    private final Map<String, float[]> arrays = new HashMap<>();
    private final Set<String> active = new HashSet<>();

    private String nameTarget;
    private boolean nameActive;

    void render(EditorContext ctx) {
        Theme theme = ctx.theme();
        if (!ImGui.begin(PanelKind.INSPECTOR.title())) {
            ImGui.end();
            return;
        }
        if (ImGui.isWindowFocused()) {
            ctx.focusedPanel = PanelKind.INSPECTOR;
        }
        GameObject object = ctx.state.selection().primary();
        if (object == null) {
            ImGui.textDisabled("No selection");
            ImGui.end();
            return;
        }
        objectHeader(ctx, theme, object);
        ImGui.separator();
        if (ImGui.beginChild("##inspector-body")) {
            for (Component component : ctx.state.inspector().componentsOf(object)) {
                componentSection(ctx, theme, object, component);
            }
            addComponentButton(ctx);
        }
        ImGui.endChild();
        renderAddMenu(ctx);
        ImGui.end();
    }

    private void objectHeader(EditorContext ctx, Theme theme, GameObject object) {
        String guid = object.guid().value();
        if (!guid.equals(nameTarget)) {
            nameTarget = guid;
            nameBuffer.set(object.name());
            nameActive = false;
        }
        if (!nameActive) {
            nameBuffer.set(object.name());
        }
        ImGui.setNextItemWidth(-1f);
        PropertyRow.stringField(theme, "Name", nameBuffer);
        if (ImGui.isItemActive()) {
            nameActive = true;
        } else {
            nameActive = false;
        }
        if (ImGui.isItemDeactivatedAfterEdit()) {
            String next = nameBuffer.get();
            if (next != null && !next.isBlank() && !next.equals(object.name())) {
                ctx.execute(EditCommands.rename(object, next));
            }
        }

        ImBoolean activeFlag = new ImBoolean(object.active());
        if (PropertyRow.boolField(theme, "Active", activeFlag)) {
            boolean next = activeFlag.get();
            ctx.execute(new io.stratixops.engine.editor.command.SimpleCommand(
                    (next ? "Activate " : "Deactivate ") + object.name(),
                    () -> object.setActive(next),
                    () -> object.setActive(!next)));
        }
        PropertyRow.readonly(theme, "Guid", guid);
    }

    private void componentSection(EditorContext ctx, Theme theme, GameObject object,
            Component component) {
        ComponentDescriptor descriptor = ctx.state.registry().descriptor(component.getClass());
        if (descriptor == null) {
            return;
        }
        ImGui.pushID(componentKey(component));
        ImGui.separator();
        PropertyRow.section(theme, descriptor.displayName(), true);
        ImGui.sameLine();
        ImBoolean enabled = new ImBoolean(component.enabled());
        if (ImGui.checkbox("##enabled", enabled)) {
            ctx.execute(EditCommands.setEnabled(component, enabled.get()));
        }
        if (!(component instanceof Transform)) {
            ImGui.sameLine(ImGui.getContentRegionAvailX() - ImGui.getFrameHeight());
            if (ImGui.smallButton("x##remove")) {
                ComponentCommands.remove(ctx, object, component);
                ImGui.popID();
                return;
            }
        }

        String lastHeader = null;
        for (FieldRow row : ctx.state.inspector().rows(component)) {
            if (row.header() != null && !row.header().equals(lastHeader)) {
                lastHeader = row.header();
                ImGui.spacing();
                PropertyRow.section(theme, row.header(), true);
            }
            drawField(ctx, theme, row.component(), row.field());
        }
        ImGui.popID();
    }

    private void drawField(EditorContext ctx, Theme theme, Component component,
            FieldDescriptor field) {
        Object value = field.get(component);
        String key = componentKey(component) + "/" + field.name();
        switch (field.kind()) {
            case BOOLEAN -> {
                ImBoolean buffer = new ImBoolean(Boolean.TRUE.equals(value));
                if (PropertyRow.boolField(theme, field.displayName(), buffer)) {
                    dispatch(ctx, component, field, buffer.get());
                }
            }
            case INT, LONG -> {
                float[] buffer = arrays.computeIfAbsent(key, k -> new float[1]);
                if (refresh(key)) {
                    buffer[0] = value instanceof Number number ? number.floatValue() : 0f;
                }
                PropertyRow.intField(theme, field.displayName(), buffer, 1f);
                boolean nowActive = ImGui.isItemActive();
                boolean edited = ImGui.isItemDeactivatedAfterEdit();
                touch(key, nowActive);
                if (edited) {
                    Object next = field.kind() == PropertyKind.LONG
                            ? (Object) (long) buffer[0] : (Object) (int) buffer[0];
                    dispatch(ctx, component, field, next);
                }
            }
            case FLOAT, DOUBLE -> {
                ImFloat buffer = floats.computeIfAbsent(key, k -> new ImFloat(0f));
                if (refresh(key)) {
                    buffer.set(value instanceof Number number ? number.floatValue() : 0f);
                }
                PropertyRow.floatField(theme, field.displayName(), buffer, 0.01f);
                boolean nowActive = ImGui.isItemActive();
                boolean edited = ImGui.isItemDeactivatedAfterEdit();
                touch(key, nowActive);
                if (edited) {
                    Object next = field.kind() == PropertyKind.DOUBLE
                            ? (Object) (double) buffer.get() : (Object) buffer.get();
                    dispatch(ctx, component, field, next);
                }
            }
            case STRING -> {
                ImString buffer = strings.computeIfAbsent(key, k -> new ImString(256));
                if (refresh(key)) {
                    buffer.set(value == null ? "" : value.toString());
                }
                PropertyRow.stringField(theme, field.displayName(), buffer);
                boolean nowActive = ImGui.isItemActive();
                boolean edited = ImGui.isItemDeactivatedAfterEdit();
                touch(key, nowActive);
                if (edited) {
                    dispatch(ctx, component, field, buffer.get());
                }
            }
            case ENUM -> {
                Object[] constants = field.type().getEnumConstants();
                if (constants == null || constants.length == 0) {
                    PropertyRow.readonly(theme, field.displayName(), String.valueOf(value));
                    break;
                }
                String[] names = new String[constants.length];
                int index = 0;
                for (int i = 0; i < constants.length; i++) {
                    names[i] = constants[i].toString();
                    if (constants[i].equals(value)) {
                        index = i;
                    }
                }
                ImInt selected = new ImInt(index);
                if (PropertyRow.enumField(theme, field.displayName(), names[selected.get()], names,
                        selected)) {
                    dispatch(ctx, component, field, constants[selected.get()]);
                }
            }
            case VEC2 -> {
                float[] buffer = arrays.computeIfAbsent(key, k -> new float[2]);
                if (refresh(key) && value instanceof Vec2 vec) {
                    buffer[0] = vec.x;
                    buffer[1] = vec.y;
                }
                PropertyRow.vec2Field(theme, field.displayName(), buffer);
                boolean nowActive = ImGui.isItemActive();
                boolean edited = ImGui.isItemDeactivatedAfterEdit();
                touch(key, nowActive);
                if (edited) {
                    dispatch(ctx, component, field, new Vec2(buffer[0], buffer[1]));
                }
            }
            case COLOR -> {
                float[] buffer = arrays.computeIfAbsent(key, k -> new float[4]);
                if (refresh(key) && value instanceof Integer packed) {
                    float[] rgba = Rgba.toFloats(packed);
                    System.arraycopy(rgba, 0, buffer, 0, 4);
                }
                PropertyRow.colorField(theme, field.displayName(), buffer);
                boolean nowActive = ImGui.isItemActive();
                boolean edited = ImGui.isItemDeactivatedAfterEdit();
                touch(key, nowActive);
                if (edited) {
                    int packed = Rgba.pack(Math.round(buffer[0] * 255f), Math.round(buffer[1] * 255f),
                            Math.round(buffer[2] * 255f), Math.round(buffer[3] * 255f));
                    dispatch(ctx, component, field, packed);
                }
            }
            default -> PropertyRow.readonly(theme, field.displayName(),
                    value == null ? "None" : value.toString());
        }
    }

    private void addComponentButton(EditorContext ctx) {
        ImGui.spacing();
        if (ImGui.button("Add Component")) {
            addMenu.openAt(ImGui.getMousePosX(), ImGui.getMousePosY());
        }
    }

    private void renderAddMenu(EditorContext ctx) {
        List<CreateMenu.Item> items = new ArrayList<>();
        for (ComponentDescriptor descriptor : ctx.state.registry().descriptors()) {
            if (descriptor.type() == Transform.class) {
                continue;
            }
            items.add(CreateMenu.Item.of("comp:" + descriptor.typeId(), descriptor.displayName(),
                    descriptor.category(), iconFor(descriptor)));
        }
        String activated = CreateMenu.render(ctx.theme(), addMenu, items);
        if (activated != null && activated.startsWith("comp:")) {
            ComponentCommands.add(ctx, ctx.state.selection().primary(),
                    activated.substring("comp:".length()));
        }
    }

    private void dispatch(EditorContext ctx, Component component, FieldDescriptor field,
            Object value) {
        ctx.execute(EditCommands.setField(component, field, value));
    }

    private boolean refresh(String key) {
        return !active.contains(key);
    }

    private void touch(String key, boolean wasActive) {
        if (wasActive) {
            active.add(key);
        } else {
            active.remove(key);
        }
    }

    private static String componentKey(Component component) {
        return Integer.toHexString(System.identityHashCode(component));
    }

    private static Icon iconFor(ComponentDescriptor descriptor) {
        return io.stratixops.engine.editor.ui.ComponentIcons.of(descriptor.type());
    }
}
