package io.stratixops.engine.editor.view;

import io.stratixops.engine.editor.command.Command;
import io.stratixops.engine.editor.command.EditCommands;
import io.stratixops.engine.inspect.ComponentDescriptor;
import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.inspect.FieldDescriptor;
import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Transform;

import java.util.Objects;

/**
 * Stateful move/rotate/scale gizmo (STR-41). A drag begins by remembering the
 * target's starting TRS; the UI computes a world-space delta each frame and the
 * controller turns it into the new snapped value. Nothing is mutated until
 * {@link #moveCommand}, {@link #rotateCommand} or {@link #scaleCommand} builds
 * an {@link EditCommands#setField} command, so a single drag produces exactly
 * one undoable edit and undo restores the pre-drag value.
 *
 * <p>The controller is headless: it depends only on the scene model, the
 * component registry and the command factory, never on ImGui or GL.
 */
public final class GizmoController {

    private static final String POSITION_FIELD = "localPosition";
    private static final String ROTATION_FIELD = "localRotation";
    private static final String SCALE_FIELD = "localScale";

    private final ComponentRegistry registry;
    private GizmoMode mode = GizmoMode.MOVE;
    private boolean snapping = true;
    private GameObject target;
    private final Vec2 startPosition = new Vec2();
    private final Vec2 startScale = new Vec2(1f, 1f);
    private float startRotation;

    /**
     * @param registry the component registry (provides the Transform descriptor)
     */
    public GizmoController(ComponentRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    /** @return the active gizmo mode */
    public GizmoMode mode() {
        return mode;
    }

    /**
     * Selects the gizmo mode.
     *
     * @param mode the new mode
     */
    public void setMode(GizmoMode mode) {
        this.mode = Objects.requireNonNull(mode, "mode");
    }

    /** @return true when snapping is enabled */
    public boolean snapping() {
        return snapping;
    }

    /**
     * Enables or disables snapping.
     *
     * @param snapping the new state
     */
    public void setSnapping(boolean snapping) {
        this.snapping = snapping;
    }

    /** Flips snapping on/off. */
    public void toggleSnapping() {
        snapping = !snapping;
    }

    /** @return the drag target, or {@code null} when idle */
    public GameObject target() {
        return target;
    }

    /** @return true while a drag target is held */
    public boolean isDragging() {
        return target != null;
    }

    /**
     * Begins a drag on an object, capturing its starting TRS.
     *
     * @param object the object, or {@code null} to cancel
     */
    public void begin(GameObject object) {
        target = object;
        if (object == null) {
            return;
        }
        Transform transform = object.transform();
        Vec2 position = transform.localPosition();
        Vec2 scale = transform.localScale();
        startPosition.set(position.x, position.y);
        startScale.set(scale.x, scale.y);
        startRotation = transform.localRotation();
    }

    /** Discards the drag without producing a command. */
    public void cancel() {
        target = null;
    }

    /**
     * The snapped position the current drag would produce.
     *
     * @param worldDelta the world-space drag delta
     * @return the preview position
     */
    public Vec2 movePreview(Vec2 worldDelta) {
        return GizmoMath.snapMove(new Vec2(startPosition.x + worldDelta.x,
                startPosition.y + worldDelta.y), snapping);
    }

    /**
     * The snapped rotation the current drag would produce.
     *
     * @param angleDelta the world-space angle delta in radians
     * @return the preview rotation in radians
     */
    public float rotatePreview(float angleDelta) {
        return GizmoMath.snapRotation(startRotation + angleDelta, snapping);
    }

    /**
     * The snapped scale the current drag would produce.
     *
     * @param factor the multiplicative scale delta applied to both axes
     * @return the preview scale
     */
    public Vec2 scalePreview(float factor) {
        return scalePreview(factor, factor);
    }

    /**
     * The snapped scale the current drag would produce, per axis.
     *
     * @param factorX horizontal multiplicative delta
     * @param factorY vertical multiplicative delta
     * @return the preview scale
     */
    public Vec2 scalePreview(float factorX, float factorY) {
        return GizmoMath.snapScale(new Vec2(startScale.x * factorX, startScale.y * factorY), snapping);
    }

    /**
     * Builds the undoable command for a move drag.
     *
     * @param worldDelta the world-space drag delta
     * @return the command
     */
    public Command moveCommand(Vec2 worldDelta) {
        return EditCommands.setField(transform(), field(POSITION_FIELD), movePreview(worldDelta));
    }

    /**
     * Builds the undoable command for a rotate drag.
     *
     * @param angleDelta the world-space angle delta in radians
     * @return the command
     */
    public Command rotateCommand(float angleDelta) {
        return EditCommands.setField(transform(), field(ROTATION_FIELD),
                Float.valueOf(rotatePreview(angleDelta)));
    }

    /**
     * Builds the undoable command for a scale drag.
     *
     * @param factor the multiplicative scale delta applied to both axes
     * @return the command
     */
    public Command scaleCommand(float factor) {
        return scaleCommand(factor, factor);
    }

    /**
     * Builds the undoable command for a per-axis scale drag.
     *
     * @param factorX horizontal multiplicative delta
     * @param factorY vertical multiplicative delta
     * @return the command
     */
    public Command scaleCommand(float factorX, float factorY) {
        return EditCommands.setField(transform(), field(SCALE_FIELD), scalePreview(factorX, factorY));
    }

    private Transform transform() {
        if (target == null) {
            throw new IllegalStateException("no gizmo drag is active");
        }
        return target.transform();
    }

    private FieldDescriptor field(String name) {
        ComponentDescriptor descriptor = registry.descriptor(Transform.class);
        FieldDescriptor field = descriptor == null ? null : descriptor.field(name);
        if (field == null) {
            throw new IllegalStateException("Transform has no field '" + name + "'");
        }
        return field;
    }
}
