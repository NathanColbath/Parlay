package io.stratixops.engine.editor.view;

import io.stratixops.engine.editor.command.Command;
import io.stratixops.engine.editor.command.CommandStack;
import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Guid;
import io.stratixops.engine.scene.Transform;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.within;

class GizmoControllerTest {

    private static final float EPS = 1e-3f;

    private static GizmoController controller() {
        return new GizmoController(ComponentRegistry.withBuiltins());
    }

    private static GameObject objectAt(float x, float y) {
        GameObject object = new GameObject(Guid.random(), "Target");
        object.transform().localPosition().set(x, y);
        return object;
    }

    @Test
    void moveWithSnappingIsUndoable() {
        GameObject object = objectAt(3f, 5f);
        Transform transform = object.transform();
        GizmoController gizmo = controller();
        gizmo.setSnapping(true);
        gizmo.begin(object);

        assertThat(gizmo.isDragging()).isTrue();
        Vec2 preview = gizmo.movePreview(new Vec2(10f, 0f));
        assertThat(preview.x).isEqualTo(16f);
        assertThat(preview.y).isEqualTo(0f);

        CommandStack stack = new CommandStack();
        stack.execute(gizmo.moveCommand(new Vec2(10f, 0f)));
        assertThat(transform.localPosition().x).isEqualTo(16f);
        assertThat(transform.localPosition().y).isEqualTo(0f);

        stack.undo();
        assertThat(transform.localPosition().x).isEqualTo(3f);
        assertThat(transform.localPosition().y).isEqualTo(5f);

        stack.redo();
        assertThat(transform.localPosition().x).isEqualTo(16f);
    }

    @Test
    void moveWithoutSnappingKeepsExactValue() {
        GameObject object = objectAt(3f, 5f);
        GizmoController gizmo = controller();
        gizmo.setSnapping(false);
        gizmo.begin(object);

        CommandStack stack = new CommandStack();
        stack.execute(gizmo.moveCommand(new Vec2(10f, -2f)));

        assertThat(object.transform().localPosition().x).isEqualTo(13f);
        assertThat(object.transform().localPosition().y).isEqualTo(3f);
    }

    @Test
    void rotateSnapsAndUndoes() {
        GameObject object = objectAt(0f, 0f);
        GizmoController gizmo = controller();
        gizmo.setSnapping(true);
        gizmo.begin(object);

        CommandStack stack = new CommandStack();
        stack.execute(gizmo.rotateCommand((float) Math.toRadians(20f)));

        assertThat(object.transform().localRotation())
                .isCloseTo((float) Math.toRadians(15f), within(EPS));

        stack.undo();
        assertThat(object.transform().localRotation()).isEqualTo(0f);
    }

    @Test
    void scaleSnapsPerAxisAndUndoes() {
        GameObject object = objectAt(0f, 0f);
        object.transform().localScale().set(1f, 1f);
        GizmoController gizmo = controller();
        gizmo.setSnapping(true);
        gizmo.begin(object);

        CommandStack stack = new CommandStack();
        stack.execute(gizmo.scaleCommand(2.07f, 0.53f));

        assertThat(object.transform().localScale().x).isCloseTo(2.1f, within(EPS));
        assertThat(object.transform().localScale().y).isCloseTo(0.5f, within(EPS));

        stack.undo();
        assertThat(object.transform().localScale().x).isCloseTo(1f, within(EPS));
        assertThat(object.transform().localScale().y).isCloseTo(1f, within(EPS));
    }

    @Test
    void commandsRequireAnActiveDrag() {
        GizmoController gizmo = controller();
        assertThatIllegalStateException().isThrownBy(() -> gizmo.moveCommand(new Vec2(1f, 1f)));
    }

    @Test
    void modeAndSnappingToggles() {
        GizmoController gizmo = controller();
        assertThat(gizmo.mode()).isEqualTo(GizmoMode.MOVE);
        assertThat(gizmo.snapping()).isTrue();
        gizmo.toggleSnapping();
        assertThat(gizmo.snapping()).isFalse();
        gizmo.setMode(GizmoMode.ROTATE);
        assertThat(gizmo.mode()).isEqualTo(GizmoMode.ROTATE);
    }
}
