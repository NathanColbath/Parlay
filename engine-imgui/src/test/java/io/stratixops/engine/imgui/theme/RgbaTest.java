package io.stratixops.engine.imgui.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class RgbaTest {

    @Test
    void parsesShortHex() {
        assertThat(Rgba.parse("#FFF")).isEqualTo(Rgba.pack(255, 255, 255, 255));
        assertThat(Rgba.parse("#000")).isEqualTo(Rgba.pack(0, 0, 0, 255));
    }

    @Test
    void parsesLongHex() {
        assertThat(Rgba.parse("#0B0E14")).isEqualTo(Rgba.pack(11, 14, 20, 255));
        assertThat(Rgba.red(Rgba.parse("#0B0E14"))).isEqualTo(11);
        assertThat(Rgba.green(Rgba.parse("#0B0E14"))).isEqualTo(14);
        assertThat(Rgba.blue(Rgba.parse("#0B0E14"))).isEqualTo(20);
    }

    @Test
    void parsesHexWithAlpha() {
        assertThat(Rgba.parse("#11223344")).isEqualTo(Rgba.pack(0x11, 0x22, 0x33, 0x44));
    }

    @Test
    void parsesRgbaFractionalAlpha() {
        assertThat(Rgba.parse("rgba(8,10,14,0.72)")).isEqualTo(Rgba.pack(8, 10, 14, 184));
        assertThat(Rgba.parse("rgba(255,255,255,0.05)")).isEqualTo(Rgba.pack(255, 255, 255, 13));
        assertThat(Rgba.alpha(Rgba.parse("rgba(0,0,0,1)"))).isEqualTo(255);
    }

    @Test
    void parsesRgbAsOpaque() {
        assertThat(Rgba.parse("rgb(10,20,30)")).isEqualTo(Rgba.pack(10, 20, 30, 255));
    }

    @Test
    void rejectsGarbage() {
        assertThatThrownBy(() -> Rgba.parse("not-a-color"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Rgba.parse("#12345"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void roundTripsToFloatsAndHex() {
        int color = Rgba.parse("#14C8A8");
        assertThat(Rgba.toHex(color)).isEqualTo("#14C8A8FF");
        float[] floats = Rgba.toFloats(color);
        assertThat(floats[0]).isCloseTo(0x14 / 255f, within());
        assertThat(floats[3]).isEqualTo(1f);
    }

    private static org.assertj.core.data.Offset<Float> within() {
        return org.assertj.core.data.Offset.offset(0.0001f);
    }
}
