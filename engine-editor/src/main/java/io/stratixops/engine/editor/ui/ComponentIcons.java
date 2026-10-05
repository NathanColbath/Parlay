package io.stratixops.engine.editor.ui;

import io.stratixops.engine.imgui.theme.Icon;
import io.stratixops.engine.scene.Animator;
import io.stratixops.engine.scene.AudioSource;
import io.stratixops.engine.scene.Behaviour;
import io.stratixops.engine.scene.Camera;
import io.stratixops.engine.scene.Collider2D;
import io.stratixops.engine.scene.Component;
import io.stratixops.engine.scene.SpriteRenderer;
import io.stratixops.engine.scene.Transform;

/**
 * Maps a component type to its design-system icon (STR-40). Shared by the
 * Hierarchy tree, the create menu, and the Inspector's add-component menu so a
 * type always looks the same everywhere.
 */
final class ComponentIcons {

    private ComponentIcons() {
    }

    /**
     * @param type the component class
     * @return the semantic icon, or {@link Icon#GAME_OBJECT} for an unknown type
     */
    static Icon of(Class<? extends Component> type) {
        if (type == SpriteRenderer.class) {
            return Icon.SPRITE_RENDERER;
        }
        if (type == Animator.class) {
            return Icon.ANIMATOR;
        }
        if (type == Camera.class) {
            return Icon.CAMERA;
        }
        if (type == AudioSource.class) {
            return Icon.AUDIO_SOURCE;
        }
        if (type == Collider2D.class) {
            return Icon.COLLIDER_2D;
        }
        if (type == Transform.class) {
            return Icon.TRANSFORM;
        }
        if (Behaviour.class.isAssignableFrom(type)) {
            return Icon.BEHAVIOUR;
        }
        return Icon.GAME_OBJECT;
    }
}
