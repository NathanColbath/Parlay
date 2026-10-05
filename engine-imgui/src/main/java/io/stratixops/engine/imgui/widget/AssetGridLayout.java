package io.stratixops.engine.imgui.widget;

/**
 * Pure geometry for the asset grid (design system section 3.4): fixed-size
 * tiles flowing left-to-right, wrapped to the available content width. Kept
 * ImGui-free so the wrap math is unit-testable and cannot drift from the
 * render loop.
 */
public final class AssetGridLayout {

    private final float tile;
    private final float gap;
    private final float cellHeight;

    /**
     * @param tile       the square tile edge ({@code size.thumb.lg})
     * @param gap        the inter-tile gap ({@code space.2})
     * @param labelHeight the two-line filename block height below a tile
     */
    public AssetGridLayout(float tile, float gap, float labelHeight) {
        this.tile = Math.max(tile, 1f);
        this.gap = Math.max(gap, 0f);
        this.cellHeight = this.tile + this.gap + Math.max(labelHeight, 0f);
    }

    public float tile() {
        return tile;
    }

    public float gap() {
        return gap;
    }

    public float cellHeight() {
        return cellHeight;
    }

    /** @return how many tiles fit in {@code available} px, at least one. */
    public int columns(float available) {
        if (available <= tile) {
            return 1;
        }
        return Math.max(1, (int) Math.floor((available + gap) / (tile + gap)));
    }

    /** @return the x of a tile's left edge, relative to {@code originX}. */
    public float tileX(int index, int columns, float originX) {
        int col = columns <= 0 ? 0 : index % columns;
        return originX + col * (tile + gap);
    }

    /** @return the y of a tile's top edge, relative to {@code originY}. */
    public float tileY(int index, int columns, float originY) {
        int row = columns <= 0 ? 0 : index / columns;
        return originY + row * (cellHeight + gap);
    }

    /**
     * Hit-tests a point (relative to the grid origin) to an item index.
     *
     * @return the index, or {@code -1} when the point falls in a gap or gutter
     */
    public int indexAt(float localX, float localY, int columns) {
        if (columns <= 0 || localX < 0f || localY < 0f) {
            return -1;
        }
        int col = (int) Math.floor(localX / (tile + gap));
        int row = (int) Math.floor(localY / (cellHeight + gap));
        if (col < 0 || col >= columns || row < 0) {
            return -1;
        }
        if (localX - col * (tile + gap) > tile || localY - row * (cellHeight + gap) > cellHeight) {
            return -1;
        }
        return row * columns + col;
    }

    /** @return the full content height for {@code count} items. */
    public float contentHeight(int count, int columns) {
        if (count <= 0) {
            return 0f;
        }
        int rows = (count + Math.max(columns, 1) - 1) / Math.max(columns, 1);
        return rows * cellHeight + (rows - 1) * gap;
    }
}
