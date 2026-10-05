package io.stratixops.engine.editor.command;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * The editor undo/redo stack. Executing a command clears the redo branch,
 * matching standard editor behavior.
 */
public final class CommandStack {

    private final Deque<Command> undo = new ArrayDeque<>();
    private final Deque<Command> redo = new ArrayDeque<>();
    private int limit = 256;

    /**
     * Sets the maximum number of remembered commands.
     *
     * @param limit the cap, at least 1
     */
    public void setLimit(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be >= 1");
        }
        this.limit = limit;
        trim();
    }

    /**
     * Applies a command and pushes it onto the undo stack.
     *
     * @param command the command
     */
    public void execute(Command command) {
        Objects.requireNonNull(command, "command");
        command.apply();
        undo.push(command);
        redo.clear();
        trim();
    }

    /**
     * Reverts the most recent command.
     *
     * @return the reverted command, or {@code null} when nothing to undo
     */
    public Command undo() {
        if (undo.isEmpty()) {
            return null;
        }
        Command command = undo.pop();
        command.undo();
        redo.push(command);
        return command;
    }

    /**
     * Re-applies the most recently undone command.
     *
     * @return the applied command, or {@code null} when nothing to redo
     */
    public Command redo() {
        if (redo.isEmpty()) {
            return null;
        }
        Command command = redo.pop();
        command.apply();
        undo.push(command);
        return command;
    }

    /**
     * @return true when an undo is available
     */
    public boolean canUndo() {
        return !undo.isEmpty();
    }

    /**
     * @return true when a redo is available
     */
    public boolean canRedo() {
        return !redo.isEmpty();
    }

    /**
     * @return the label of the next undo, or {@code null}
     */
    public String undoName() {
        return undo.isEmpty() ? null : undo.peek().name();
    }

    /**
     * @return the label of the next redo, or {@code null}
     */
    public String redoName() {
        return redo.isEmpty() ? null : redo.peek().name();
    }

    /**
     * @return the number of undoable commands
     */
    public int undoCount() {
        return undo.size();
    }

    /**
     * @return the number of redoable commands
     */
    public int redoCount() {
        return redo.size();
    }

    /**
     * Clears both branches.
     */
    public void clear() {
        undo.clear();
        redo.clear();
    }

    private void trim() {
        while (undo.size() > limit) {
            undo.removeLast();
        }
    }
}
