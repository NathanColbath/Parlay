package io.stratixops.engine.editor.ui;

import io.stratixops.engine.editor.command.Command;
import io.stratixops.engine.editor.command.SimpleCommand;
import io.stratixops.engine.scene.Component;
import io.stratixops.engine.scene.GameObject;

/**
 * Undoable component attach/detach used by the Hierarchy and Inspector
 * (STR-40). The command keeps one instance across undo/redo so redo re-attaches
 * the same object rather than instantiating a new one.
 */
final class ComponentCommands {

    private ComponentCommands() {
    }

    /**
     * @param ctx     the editor context
     * @param object  the target object
     * @param typeId  the registered component type id
     */
    static void add(EditorContext ctx, GameObject object, String typeId) {
        if (object == null) {
            return;
        }
        Component component = ctx.state.registry().newInstance(typeId);
        Component[] slot = {component};
        Command command = new SimpleCommand("Add " + component.getClass().getSimpleName(),
                () -> object.addComponent(slot[0]),
                () -> object.removeComponent(slot[0]));
        ctx.execute(command);
    }

    /**
     * @param ctx       the editor context
     * @param object    the target object
     * @param component the component to remove
     */
    static void remove(EditorContext ctx, GameObject object, Component component) {
        if (object == null || component == null) {
            return;
        }
        SimpleCommand command = new SimpleCommand("Remove " + component.getClass().getSimpleName(),
                () -> object.removeComponent(component),
                () -> object.addComponent(component));
        ctx.execute(command);
    }
}
