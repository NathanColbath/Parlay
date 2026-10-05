package io.stratixops.engine.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class QuadBatchTest {

    private static final float[] XY = {0f, 0f, 10f, 0f, 10f, 20f, 0f, 20f};
    private static final float[] UV = {0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f};

    @Test
    void rejectsNonPositiveCapacity() {
        assertThatIllegalArgumentException().isThrownBy(() -> new QuadBatch(0));
    }

    @Test
    void reportsCapacityAndCounts() {
        QuadBatch batch = new QuadBatch(2);
        assertThat(batch.capacityQuads()).isEqualTo(2);
        assertThat(batch.isEmpty()).isTrue();
        assertThat(batch.addQuad(XY, UV, 0xFFFFFFFF)).isTrue();
        assertThat(batch.quadCount()).isEqualTo(1);
        assertThat(batch.vertexCount()).isEqualTo(4);
        assertThat(batch.indexCount()).isEqualTo(6);
        assertThat(batch.isEmpty()).isFalse();
    }

    @Test
    void writesInterleavedVerticesInCornerOrder() {
        QuadBatch batch = new QuadBatch(1);
        batch.addQuad(XY, UV, 0xFF0000FF);
        float[] v = batch.vertexArray();
        assertThat(v[0]).isEqualTo(0f);
        assertThat(v[1]).isEqualTo(0f);
        assertThat(v[2]).isEqualTo(0f);
        assertThat(v[3]).isEqualTo(0f);
        assertThat(v[4]).isEqualTo(1f);
        assertThat(v[5]).isEqualTo(0f);
        assertThat(v[6]).isEqualTo(0f);
        assertThat(v[7]).isEqualTo(1f);
        assertThat(v[8]).isEqualTo(10f);
        assertThat(v[9]).isEqualTo(0f);
        assertThat(v[10]).isEqualTo(1f);
        assertThat(v[11]).isEqualTo(0f);
    }

    @Test
    void indexPatternFormsTwoTriangles() {
        QuadBatch batch = new QuadBatch(1);
        batch.addQuad(XY, UV, 0xFFFFFFFF);
        assertThat(batch.indexArray()).containsExactly(0, 1, 2, 0, 2, 3);
    }

    @Test
    void returnsFalseWhenFull() {
        QuadBatch batch = new QuadBatch(1);
        assertThat(batch.addQuad(XY, UV, 0xFFFFFFFF)).isTrue();
        assertThat(batch.addQuad(XY, UV, 0xFFFFFFFF)).isFalse();
        assertThat(batch.quadCount()).isEqualTo(1);
    }

    @Test
    void resetReusesCapacity() {
        QuadBatch batch = new QuadBatch(2);
        batch.addQuad(XY, UV, 0xFFFFFFFF);
        batch.reset();
        assertThat(batch.quadCount()).isZero();
        assertThat(batch.isEmpty()).isTrue();
        assertThat(batch.addQuad(XY, UV, 0xFFFFFFFF)).isTrue();
    }

    @Test
    void secondQuadIndicesAreOffset() {
        QuadBatch batch = new QuadBatch(2);
        batch.addQuad(XY, UV, 0xFFFFFFFF);
        batch.addQuad(XY, UV, 0xFFFFFFFF);
        assertThat(batch.indexArray()).containsExactly(0, 1, 2, 0, 2, 3, 4, 5, 6, 4, 6, 7);
    }
}
