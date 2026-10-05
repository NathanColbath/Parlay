package io.stratixops.engine.scene;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransformTest {

    @Test
    void rootWorldEqualsLocal() {
        GameObject object = new GameObject();
        Transform transform = object.transform();
        transform.localPosition().set(10f, 20f);
        transform.localScale().set(2f, 3f);
        transform.setLocalRotation(1.5f);

        assertThat(transform.worldPosition().x).isEqualTo(10f);
        assertThat(transform.worldPosition().y).isEqualTo(20f);
        assertThat(transform.worldScale().x).isEqualTo(2f);
        assertThat(transform.worldScale().y).isEqualTo(3f);
        assertThat(transform.worldRotation()).isEqualTo(1.5f);
    }

    @Test
    void childComposesParentPositionAndScale() {
        GameObject parent = new GameObject();
        parent.transform().localPosition().set(100f, 50f);
        parent.transform().localScale().set(2f, 2f);
        parent.transform().setLocalRotation(0.5f);

        GameObject child = new GameObject();
        child.setParent(parent);
        child.transform().localPosition().set(5f, 5f);
        child.transform().localScale().set(0.5f, 0.5f);
        child.transform().setLocalRotation(0.25f);

        assertThat(child.transform().worldPosition().x).isEqualTo(110f);
        assertThat(child.transform().worldPosition().y).isEqualTo(60f);
        assertThat(child.transform().worldScale().x).isEqualTo(1f);
        assertThat(child.transform().worldScale().y).isEqualTo(1f);
        assertThat(child.transform().worldRotation()).isEqualTo(0.75f);
    }
}
