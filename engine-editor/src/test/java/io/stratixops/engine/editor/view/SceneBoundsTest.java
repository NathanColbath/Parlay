package io.stratixops.engine.editor.view;

import io.stratixops.engine.Sprite;
import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Guid;
import io.stratixops.engine.scene.SpriteRenderer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SceneBoundsTest {

    private static final float EPS = 1e-3f;

    @Test
    void defaultBoundsUseEditorCellAtWorldPosition() {
        GameObject object = new GameObject(Guid.random(), "Empty");
        object.transform().localPosition().set(10f, 20f);

        ViewRect bounds = SceneBounds.of(object);

        assertThat(bounds.minX()).isCloseTo(-6f, within(EPS));
        assertThat(bounds.maxX()).isCloseTo(26f, within(EPS));
        assertThat(bounds.minY()).isCloseTo(4f, within(EPS));
        assertThat(bounds.maxY()).isCloseTo(36f, within(EPS));
    }

    @Test
    void scaleMultipliesSpriteSize() {
        GameObject object = new GameObject(Guid.random(), "Sprite");
        SpriteRenderer renderer = object.addComponent(new SpriteRenderer());
        renderer.setSpriteHandle(sprite(40f, 20f));
        object.transform().localScale().set(2f, 3f);

        ViewRect bounds = SceneBounds.of(object);

        assertThat(bounds.width()).isCloseTo(80f, within(EPS));
        assertThat(bounds.height()).isCloseTo(60f, within(EPS));
    }

    @Test
    void subtreeUnionsChildrenUsingWorldPositions() {
        GameObject parent = new GameObject(Guid.random(), "Parent");
        GameObject child = new GameObject(Guid.random(), "Child");
        child.transform().localPosition().set(100f, 0f);
        parent.children().add(child);
        child.setParent(parent);

        ViewRect bounds = SceneBounds.subtree(parent);

        assertThat(bounds.minX()).isCloseTo(-16f, within(EPS));
        assertThat(bounds.maxX()).isCloseTo(116f, within(EPS));
    }

    @Test
    void unionOfListIsNullWhenEmpty() {
        assertThat(SceneBounds.union(List.of())).isNull();
    }

    @Test
    void childWorldPositionHonoursParentScale() {
        GameObject parent = new GameObject(Guid.random(), "Parent");
        parent.transform().localScale().set(2f, 2f);
        GameObject child = new GameObject(Guid.random(), "Child");
        child.transform().localPosition().set(50f, 0f);
        parent.children().add(child);
        child.setParent(parent);

        ViewRect bounds = SceneBounds.of(child);

        // Child world x = 0 + 50 * 2 = 100.
        assertThat(bounds.centerX()).isCloseTo(100f, within(EPS));
    }

    private static Sprite sprite(float width, float height) {
        return new Sprite() {
            @Override
            public float width() {
                return width;
            }

            @Override
            public float height() {
                return height;
            }
        };
    }
}
