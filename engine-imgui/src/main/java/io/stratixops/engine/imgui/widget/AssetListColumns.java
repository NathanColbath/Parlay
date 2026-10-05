package io.stratixops.engine.imgui.widget;

/**
 * Column model for the list view header (design system section 3.4):
 * Name is flexible, Type/Size/Modified have resizable widths with a 60 px
 * minimum. Widths start at the documented 80/80/140 px defaults; the two
 * literals are layout constants, not theme tokens, mirroring
 * {@code PropertyRow}'s label clamp. Pure so resizing is unit-testable.
 */
public final class AssetListColumns {

    /** Default Type column width in logical px (design system 3.4). */
    public static final float DEFAULT_TYPE = 80f;
    /** Default Size column width in logical px. */
    public static final float DEFAULT_SIZE = 80f;
    /** Default Modified column width in logical px. */
    public static final float DEFAULT_MODIFIED = 140f;
    /** Resizable minimum (design system 3.4). */
    public static final float MIN_WIDTH = 60f;
    /** Resizable maximum; keeps a very wide column from hiding Name entirely. */
    public static final float MAX_WIDTH = 480f;
    /** Hit tolerance around a column boundary, in logical px. */
    public static final float HANDLE_TOLERANCE = 3f;

    /** Number of resizable fixed columns (Type, Size, Modified). */
    public static final int HANDLE_COUNT = 3;

    private float typeWidth = DEFAULT_TYPE;
    private float sizeWidth = DEFAULT_SIZE;
    private float modifiedWidth = DEFAULT_MODIFIED;

    public float typeWidth() {
        return typeWidth;
    }

    public float sizeWidth() {
        return sizeWidth;
    }

    public float modifiedWidth() {
        return modifiedWidth;
    }

    /** @return the flexible Name column width for the available row width. */
    public float nameWidth(float available) {
        return Math.max(MIN_WIDTH, available - (typeWidth + sizeWidth + modifiedWidth));
    }

    /**
     * @return the left x of each of the four columns
     *         {@code [Name, Type, Size, Modified]} relative to the row start
     */
    public float[] columnStarts(float available) {
        float name = nameWidth(available);
        return new float[] {0f, name, name + typeWidth, name + typeWidth + sizeWidth};
    }

    /**
     * @return the handle index ({@code 0=Type, 1=Size, 2=Modified}) whose
     *         boundary is within {@link #HANDLE_TOLERANCE} of {@code localX},
     *         or {@code -1}
     */
    public int handleAt(float localX, float available) {
        float name = nameWidth(available);
        float[] boundaries = {
            name,
            name + typeWidth,
            name + typeWidth + sizeWidth
        };
        int best = -1;
        float bestDistance = HANDLE_TOLERANCE;
        for (int i = 0; i < boundaries.length; i++) {
            float distance = Math.abs(localX - boundaries[i]);
            if (distance <= bestDistance) {
                bestDistance = distance;
                best = i;
            }
        }
        return best;
    }

    /** Resizes the fixed column behind a handle by {@code delta} logical px. */
    public void resize(int handle, float delta) {
        switch (handle) {
            case 0 -> typeWidth = clamp(typeWidth + delta);
            case 1 -> sizeWidth = clamp(sizeWidth + delta);
            case 2 -> modifiedWidth = clamp(modifiedWidth + delta);
            default -> {
                // no boundary hit; nothing to resize
            }
        }
    }

    /** Resets every column to its documented default. */
    public void reset() {
        typeWidth = DEFAULT_TYPE;
        sizeWidth = DEFAULT_SIZE;
        modifiedWidth = DEFAULT_MODIFIED;
    }

    private static float clamp(float value) {
        return Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, value));
    }
}
