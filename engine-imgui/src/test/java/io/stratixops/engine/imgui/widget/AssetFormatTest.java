package io.stratixops.engine.imgui.widget;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Design system 3.4: list metadata formatting. */
class AssetFormatTest {

    @Test
    void formatsByteSizes() {
        assertThat(AssetFormat.size(0L)).isEqualTo("0 B");
        assertThat(AssetFormat.size(512L)).isEqualTo("512 B");
        assertThat(AssetFormat.size(1024L)).isEqualTo("1.0 KB");
        assertThat(AssetFormat.size(1536L)).isEqualTo("1.5 KB");
        assertThat(AssetFormat.size(1024L * 1024L)).isEqualTo("1.0 MB");
        assertThat(AssetFormat.size(1024L * 1024L * 1024L)).isEqualTo("1.0 GB");
    }

    @Test
    void formatsTimestamps() {
        assertThat(AssetFormat.modified(0L)).isEqualTo("--");
        assertThat(AssetFormat.modified(1_700_000_000L)).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}");
    }
}
