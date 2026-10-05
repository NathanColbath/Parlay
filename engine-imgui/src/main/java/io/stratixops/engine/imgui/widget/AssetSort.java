package io.stratixops.engine.imgui.widget;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Sort state for the list view header (design system section 3.4): click a
 * column to sort, click again to flip direction. Folders always lead, then the
 * active column comparator, with a case-insensitive name tie-break.
 */
public final class AssetSort {

    /** Sortable list columns. */
    public enum Column {
        NAME("Name"),
        TYPE("Type"),
        SIZE("Size"),
        MODIFIED("Modified");

        private final String label;

        Column(String label) {
            this.label = label;
        }

        /** @return the header label. */
        public String label() {
            return label;
        }
    }

    /** Sort direction. */
    public enum Direction {
        ASC,
        DESC
    }

    private Column column = Column.NAME;
    private Direction direction = Direction.ASC;

    /** @return the active column. */
    public Column column() {
        return column;
    }

    /** @return the active direction. */
    public Direction direction() {
        return direction;
    }

    /**
     * Activates a column: same column flips direction, a new column starts
     * ascending (the conventional table behavior).
     */
    public void toggle(Column next) {
        if (next == column) {
            direction = direction == Direction.ASC ? Direction.DESC : Direction.ASC;
        } else {
            column = next;
            direction = Direction.ASC;
        }
    }

    /** @return a sorted copy; folders first, never mutating the input. */
    public List<AssetItem> sort(List<AssetItem> items) {
        List<AssetItem> result = new ArrayList<>(items);
        result.sort(comparator());
        return result;
    }

    /** @return the full comparator including the folder-first rule. */
    public Comparator<AssetItem> comparator() {
        Comparator<AssetItem> byFolders = Comparator.comparing(
                item -> item.isFolder() ? 0 : 1);
        Comparator<AssetItem> byName = Comparator.comparing(
                AssetItem::name, String.CASE_INSENSITIVE_ORDER);
        Comparator<AssetItem> byColumn = switch (column) {
            case NAME -> byName;
            case TYPE -> Comparator
                    .comparing((AssetItem item) -> item.kind().label())
                    .thenComparing(byName);
            case SIZE -> Comparator.comparingLong(AssetItem::sizeBytes).thenComparing(byName);
            case MODIFIED -> Comparator.comparingLong(AssetItem::modifiedEpochSeconds)
                    .thenComparing(byName);
        };
        if (direction == Direction.DESC) {
            byColumn = byColumn.reversed();
        }
        return byFolders.thenComparing(byColumn).thenComparing(byName);
    }
}
