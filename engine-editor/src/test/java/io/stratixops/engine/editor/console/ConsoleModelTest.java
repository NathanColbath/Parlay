package io.stratixops.engine.editor.console;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleModelTest {

    @Test
    void filtersByMinimumLevel() {
        ConsoleModel console = new ConsoleModel();
        console.info("i");
        console.warn("w");
        console.error("e");

        console.setMinLevel(LogLevel.WARNING);
        assertThat(console.filtered())
                .extracting(ConsoleEntry::message)
                .containsExactly("w", "e");
    }

    @Test
    void filtersBySearchOverMessageAndSource() {
        ConsoleModel console = new ConsoleModel();
        console.info("loading atlas");
        console.warn("missing texture");
        console.setSearch("TEX");

        assertThat(console.filtered())
                .extracting(ConsoleEntry::message)
                .containsExactly("missing texture");
    }

    @Test
    void evictsOldestBeyondCapacity() {
        ConsoleModel console = new ConsoleModel(3);
        for (int i = 0; i < 5; i++) {
            console.info("m" + i);
        }
        assertThat(console.entries()).hasSize(3);
        assertThat(console.entries().get(0).message()).isEqualTo("m2");
    }

    @Test
    void capturesThrowableSourceAndStack() {
        ConsoleModel console = new ConsoleModel();
        console.error("failed", boom());

        ConsoleEntry entry = console.entries().get(0);
        assertThat(entry.file()).isNotNull();
        assertThat(entry.line()).isPositive();
        assertThat(entry.stack()).contains("RuntimeException");
    }

    @Test
    void countsByLevelAndClears() {
        ConsoleModel console = new ConsoleModel();
        console.info("a");
        console.info("b");
        console.error("c");

        assertThat(console.count(LogLevel.INFO)).isEqualTo(2);
        assertThat(console.counts().get(LogLevel.ERROR)).isEqualTo(1);

        console.clear();
        assertThat(console.entries()).isEmpty();
    }

    private static RuntimeException boom() {
        return new RuntimeException("boom");
    }
}
