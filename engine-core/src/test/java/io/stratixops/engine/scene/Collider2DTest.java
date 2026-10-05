package io.stratixops.engine.scene;

import io.stratixops.engine.math.Vec2;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Collider2DTest {

    private Collider2D colliderAt(float x, float y) {
        GameObject object = new GameObject();
        object.transform().localPosition().set(x, y);
        return object.addComponent(new Collider2D());
    }

    @Test
    void defaults() {
        Collider2D collider = new Collider2D();
        assertThat(collider.size().x).isEqualTo(16f);
        assertThat(collider.size().y).isEqualTo(16f);
        assertThat(collider.trigger()).isFalse();
        collider.setTrigger(true);
        assertThat(collider.trigger()).isTrue();
    }

    @Test
    void overlapQuery() {
        Collider2D a = colliderAt(0f, 0f);
        Collider2D b = colliderAt(10f, 0f);
        Collider2D far = colliderAt(100f, 100f);
        assertThat(a.overlaps(b)).isTrue();
        assertThat(a.overlaps(far)).isFalse();
        assertThat(a.overlaps(a)).isTrue();
    }

    @Test
    void touchingEdgesDoNotOverlap() {
        Collider2D a = colliderAt(0f, 0f);
        Collider2D b = colliderAt(16f, 0f);
        assertThat(a.overlaps(b)).isFalse();
    }

    @Test
    void containsPointAndOffsetAndScale() {
        GameObject object = new GameObject();
        object.transform().localPosition().set(50f, 50f);
        Collider2D collider = object.addComponent(new Collider2D());
        collider.offset().set(0f, 0f);

        assertThat(collider.contains(new Vec2(50f, 50f))).isTrue();
        assertThat(collider.contains(new Vec2(0f, 0f))).isFalse();

        object.transform().localScale().set(2f, 2f);
        assertThat(collider.contains(new Vec2(65f, 50f))).isTrue();

        assertThat(collider.contains(new Vec2(100f, 100f))).isFalse();
    }
}
