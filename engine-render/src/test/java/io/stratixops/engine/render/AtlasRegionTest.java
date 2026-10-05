package io.stratixops.engine.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class AtlasRegionTest {

    @Test
    void exposesPixelRectangle() {
        AtlasRegion region = new AtlasRegion(8, 4, 16, 16, 64, 64);
        assertThat(region.x()).isEqualTo(8);
        assertThat(region.y()).isEqualTo(4);
        assertThat(region.width()).isEqualTo(16);
        assertThat(region.height()).isEqualTo(16);
    }

    @Test
    void insetsUvByHalfTexel() {
        AtlasRegion region = new AtlasRegion(0, 0, 16, 16, 32, 32);
        assertThat(region.u0()).isCloseTo(0.5f / 32f, within(1e-6f));
        assertThat(region.v0()).isCloseTo(0.5f / 32f, within(1e-6f));
        assertThat(region.u1()).isCloseTo(15.5f / 32f, within(1e-6f));
        assertThat(region.v1()).isCloseTo(15.5f / 32f, within(1e-6f));
    }

    @Test
    void uvEdgesStayInsideRegion() {
        AtlasRegion region = new AtlasRegion(16, 16, 32, 32, 64, 64);
        assertThat(region.u0()).isLessThan(region.u1());
        assertThat(region.v0()).isLessThan(region.v1());
        assertThat(region.u0()).isBetween(0f, 1f);
        assertThat(region.v1()).isBetween(0f, 1f);
    }

    @Test
    void rejectsNonPositiveImage() {
        assertThatIllegalArgumentException().isThrownBy(() -> new AtlasRegion(0, 0, 8, 8, 0, 32));
    }

    @Test
    void rejectsNonPositiveRegion() {
        assertThatIllegalArgumentException().isThrownBy(() -> new AtlasRegion(0, 0, 0, 8, 32, 32));
    }
}
