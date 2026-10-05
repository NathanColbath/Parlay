package io.stratixops.engine.editor.view;

import java.util.ArrayList;
import java.util.List;

/**
 * Reference-grid geometry for the Scene view (STR-41). The grid keeps a stable
 * world-space period while zooming by doubling/halving the visible spacing so
 * lines never collapse into a solid fill or drift apart. Pure and
 * headless-testable.
 */
public final class ViewGrid {

    /** Base world period in pixels (every {@value} px at zoom 1). */
    public static final float BASE_SPACING = 32f;

    /** Minimum on-screen spacing in pixels before the period doubles. */
    public static final float MIN_SCREEN_SPACING = 8f;

    /** Every Nth line is drawn as a major line. */
    public static final int MAJOR_EVERY = 5;

    private ViewGrid() {
    }

    /**
     * The world-space spacing to draw at the given zoom.
     *
     * @param zoom the camera zoom (screen pixels per world pixel)
     * @return the effective spacing in world pixels
     */
    public static float spacing(float zoom) {
        float spacing = BASE_SPACING;
        if (!(zoom > 0f)) {
            return spacing;
        }
        while (spacing * zoom < MIN_SCREEN_SPACING) {
            spacing *= 2f;
        }
        return spacing;
    }

    /**
     * The vertical grid lines intersecting a world x range, inclusive of the
     * nearest lines outside the range so the grid fills the viewport.
     *
     * @param minX    world left edge
     * @param maxX    world right edge
     * @param spacing world spacing (from {@link #spacing(float)})
     * @return the line x positions, ascending
     */
    public static List<Float> verticalLines(float minX, float maxX, float spacing) {
        return lines(minX, maxX, spacing);
    }

    /**
     * The horizontal grid lines intersecting a world y range.
     *
     * @param minY    world top edge
     * @param maxY    world bottom edge
     * @param spacing world spacing (from {@link #spacing(float)})
     * @return the line y positions, ascending
     */
    public static List<Float> horizontalLines(float minY, float maxY, float spacing) {
        return lines(minY, maxY, spacing);
    }

    /**
     * @param coordinate world coordinate of a line
     * @param spacing    the world spacing
     * @return true when the line is a major grid line
     */
    public static boolean isMajor(float coordinate, float spacing) {
        if (!(spacing > 0f)) {
            return false;
        }
        long index = Math.round(coordinate / spacing);
        return index % MAJOR_EVERY == 0;
    }

    private static List<Float> lines(float min, float max, float spacing) {
        List<Float> out = new ArrayList<>();
        if (!(spacing > 0f) || max < min) {
            return out;
        }
        float first = (float) Math.ceil(min / spacing) * spacing;
        for (float value = first; value <= max + 1e-4f; value += spacing) {
            out.add(value);
        }
        return out;
    }
}
