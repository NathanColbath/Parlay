package io.stratixops.engine.render;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ColorsTest {

    @Test
    void unpacksChannelsInOrder() {
        float[] out = Colors.unpack(0x80402010);
        assertThat(out).containsExactly(
            0x80 / 255f, 0x40 / 255f, 0x20 / 255f, 0x10 / 255f);
    }

    @Test
    void whiteIsOpaque() {
        assertThat(Colors.unpack(Colors.WHITE)).containsExactly(1f, 1f, 1f, 1f);
    }

    @Test
    void unpacksIntoCallerArray() {
        float[] out = new float[4];
        Colors.unpack(0xFF0000FF, out);
        assertThat(out).containsExactly(1f, 0f, 0f, 1f);
    }
}
