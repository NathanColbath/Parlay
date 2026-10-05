package io.stratixops.engine.editor.ui;

/**
 * One row in the command palette (STR-40): a stable id, a display label, a
 * category used for grouping, an enabled flag, and the action to run.
 *
 * @param id      stable command id
 * @param label   display label
 * @param group   category header
 * @param enabled whether the command can run now
 * @param action  the action
 */
record PaletteCommand(String id, String label, String group, boolean enabled, Runnable action) {

    PaletteCommand {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("command id must not be blank");
        }
        label = label == null ? "" : label;
        group = group == null || group.isBlank() ? "General" : group;
        action = action == null ? () -> {
        } : action;
    }
}
