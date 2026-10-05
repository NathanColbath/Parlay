package io.stratixops.engine.editor.command;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommandStackTest {

    private static Command append(List<String> log, String name) {
        return new SimpleCommand(name, () -> log.add(name), () -> log.remove(log.size() - 1));
    }

    @Test
    void executeUndoRedo() {
        List<String> log = new ArrayList<>();
        CommandStack stack = new CommandStack();

        stack.execute(append(log, "a"));
        stack.execute(append(log, "b"));
        assertThat(log).containsExactly("a", "b");
        assertThat(stack.canUndo()).isTrue();
        assertThat(stack.undoName()).isEqualTo("b");

        stack.undo();
        assertThat(log).containsExactly("a");
        assertThat(stack.canRedo()).isTrue();
        assertThat(stack.redoName()).isEqualTo("b");

        stack.redo();
        assertThat(log).containsExactly("a", "b");
    }

    @Test
    void executeClearsRedoBranch() {
        List<String> log = new ArrayList<>();
        CommandStack stack = new CommandStack();
        stack.execute(append(log, "a"));
        stack.undo();
        assertThat(stack.canRedo()).isTrue();

        stack.execute(append(log, "c"));
        assertThat(stack.canRedo()).isFalse();
        assertThat(log).containsExactly("c");
    }

    @Test
    void emptyStackReportsNoOps() {
        CommandStack stack = new CommandStack();
        assertThat(stack.undo()).isNull();
        assertThat(stack.redo()).isNull();
        assertThat(stack.canUndo()).isFalse();
        assertThat(stack.canRedo()).isFalse();
    }

    @Test
    void limitDropsOldestUndoEntries() {
        List<String> log = new ArrayList<>();
        CommandStack stack = new CommandStack();
        stack.setLimit(2);
        stack.execute(append(log, "a"));
        stack.execute(append(log, "b"));
        stack.execute(append(log, "c"));

        assertThat(stack.undoCount()).isEqualTo(2);
        stack.undo();
        stack.undo();
        assertThat(stack.canUndo()).isFalse();
        assertThat(log).containsExactly("a");
    }

    @Test
    void clearEmptiesBothBranches() {
        List<String> log = new ArrayList<>();
        CommandStack stack = new CommandStack();
        stack.execute(append(log, "a"));
        stack.undo();
        stack.clear();
        assertThat(stack.undoCount()).isZero();
        assertThat(stack.redoCount()).isZero();
    }
}
