package io.stratixops.engine.editor.command;

import java.util.Objects;

/**
 * A command defined by a pair of runnables, used for the editor's small edits.
 */
public final class SimpleCommand implements Command {

    private final String name;
    private final Runnable apply;
    private final Runnable undo;

    /**
     * @param name  the label
     * @param apply the forward edit
     * @param undo  the reverse edit
     */
    public SimpleCommand(String name, Runnable apply, Runnable undo) {
        this.name = Objects.requireNonNull(name, "name");
        this.apply = Objects.requireNonNull(apply, "apply");
        this.undo = Objects.requireNonNull(undo, "undo");
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void apply() {
        apply.run();
    }

    @Override
    public void undo() {
        undo.run();
    }
}
