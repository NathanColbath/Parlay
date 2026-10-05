package io.stratixops.engine.scene;

import io.stratixops.engine.testutil.RecordingComponent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameObjectTest {

    @Test
    void defaultsHaveAGuidTransformAndState() {
        GameObject object = new GameObject();
        assertThat(object.guid()).isNotNull();
        assertThat(object.name()).isEqualTo("Untitled");
        assertThat(object.transform()).isNotNull();
        assertThat(object.components()).containsExactly(object.transform());
        assertThat(object.active()).isTrue();
        assertThat(object.activeInHierarchy()).isTrue();
        assertThat(object.layer()).isZero();
        assertThat(object.tags()).isEmpty();
        assertThat(object.children()).isEmpty();
        assertThat(object.parent()).isNull();
        assertThat(object.toString()).contains("Untitled");
    }

    @Test
    void explicitIdentityAndSetters() {
        Guid guid = Guid.random();
        GameObject object = new GameObject(guid, "Player");
        object.setName("Hero");
        object.setActive(false);
        object.setLayer(3);
        object.tags().add("Player");
        assertThat(object.guid()).isEqualTo(guid);
        assertThat(object.name()).isEqualTo("Hero");
        assertThat(object.active()).isFalse();
        assertThat(object.activeInHierarchy()).isFalse();
        assertThat(object.layer()).isEqualTo(3);
        assertThat(object.tags()).containsExactly("Player");
    }

    @Test
    void addGetRemoveComponents() {
        GameObject object = new GameObject();
        RecordingComponent component = object.addComponent(new RecordingComponent());
        assertThat(component.gameObject()).isSameAs(object);
        assertThat(object.getComponent(RecordingComponent.class)).isSameAs(component);
        assertThat(object.getComponent(Camera.class)).isNull();
        assertThat(object.removeComponent(component)).isTrue();
        assertThat(object.removeComponent(component)).isFalse();
        assertThat(object.removeComponent(null)).isFalse();
        assertThat(component.gameObject()).isNull();
    }

    @Test
    void onlyOneTransformIsAllowed() {
        GameObject object = new GameObject();
        assertThatThrownBy(() -> object.addComponent(new Transform()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parentingMaintainsBothSidesAndRejectsCycles() {
        GameObject parent = new GameObject();
        GameObject child = new GameObject();
        child.setParent(parent);
        assertThat(parent.children()).containsExactly(child);
        assertThat(child.parent()).isSameAs(parent);
        assertThat(child.activeInHierarchy()).isTrue();

        parent.setActive(false);
        assertThat(child.activeInHierarchy()).isFalse();

        assertThatThrownBy(() -> parent.setParent(child))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> parent.setParent(parent))
                .isInstanceOf(IllegalArgumentException.class);

        child.setParent(null);
        assertThat(parent.children()).isEmpty();
        assertThat(child.parent()).isNull();
    }

    @Test
    void inactiveParentHidesActiveChild() {
        GameObject parent = new GameObject();
        GameObject child = new GameObject();
        child.setParent(parent);
        parent.setActive(false);
        assertThat(child.active()).isTrue();
        assertThat(child.activeInHierarchy()).isFalse();
    }
}
