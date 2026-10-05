package io.stratixops.engine.imgui.widget;

import java.util.ArrayList;
import java.util.List;

/**
 * Asset filtering for the Project panel (design system section 3.4): the
 * {@code All · Scene · Prefab · Script · Image · Audio · Font} chips plus the
 * search query. Pure functions so the behavior is unit-testable without ImGui.
 */
public final class AssetFilter {

    private AssetFilter() {
    }

    /** A filter chip: {@link #ALL} accepts every kind, the rest map 1:1 to a kind. */
    public enum Criterion {
        ALL("All", null),
        SCENE("Scene", AssetKind.SCENE),
        PREFAB("Prefab", AssetKind.PREFAB),
        SCRIPT("Script", AssetKind.SCRIPT),
        IMAGE("Image", AssetKind.IMAGE),
        AUDIO("Audio", AssetKind.AUDIO),
        FONT("Font", AssetKind.FONT);

        private final String label;
        private final AssetKind kind;

        Criterion(String label, AssetKind kind) {
            this.label = label;
            this.kind = kind;
        }

        /** @return the chip label. */
        public String label() {
            return label;
        }

        /** @return whether an item passes this criterion. */
        public boolean accepts(AssetItem item) {
            if (this == ALL) {
                return true;
            }
            return item.kind() == kind;
        }
    }

    /**
     * Applies the criterion and an optional case-insensitive name query.
     *
     * <p>A specific criterion hides folders (they are containers, not the
     * requested kind); {@link Criterion#ALL} keeps everything.
     *
     * @return a new list in input order
     */
    public static List<AssetItem> apply(List<AssetItem> items, Criterion criterion, String query) {
        List<AssetItem> result = new ArrayList<>();
        String needle = query == null ? "" : query.strip().toLowerCase(java.util.Locale.ROOT);
        for (AssetItem item : items) {
            if (!criterion.accepts(item)) {
                continue;
            }
            if (!needle.isEmpty() && !item.name().toLowerCase(java.util.Locale.ROOT).contains(needle)) {
                continue;
            }
            result.add(item);
        }
        return result;
    }
}
