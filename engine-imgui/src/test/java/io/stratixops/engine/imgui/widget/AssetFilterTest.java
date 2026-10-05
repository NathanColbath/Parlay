package io.stratixops.engine.imgui.widget;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Design system 3.4: filter chips and the search query semantics. */
class AssetFilterTest {

    private static final List<AssetItem> ITEMS = List.of(
            AssetItem.folder("characters", "characters"),
            AssetItem.of("player.png", "player.png", AssetKind.IMAGE),
            AssetItem.of("PlayerAttack.java", "PlayerAttack.java", AssetKind.SCRIPT),
            AssetItem.of("theme.ogg", "theme.ogg", AssetKind.AUDIO),
            AssetItem.of("gem.scene", "gem.scene", AssetKind.SCENE));

    @Test
    void allKeepsEveryKindIncludingFolders() {
        assertThat(AssetFilter.apply(ITEMS, AssetFilter.Criterion.ALL, ""))
                .containsExactlyElementsOf(ITEMS);
    }

    @Test
    void specificCriterionHidesOtherKindsAndFolders() {
        assertThat(AssetFilter.apply(ITEMS, AssetFilter.Criterion.IMAGE, ""))
                .extracting(AssetItem::id)
                .containsExactly("player.png");
        assertThat(AssetFilter.apply(ITEMS, AssetFilter.Criterion.SCRIPT, ""))
                .extracting(AssetItem::id)
                .containsExactly("PlayerAttack.java");
    }

    @Test
    void queryMatchesCaseInsensitively() {
        assertThat(AssetFilter.apply(ITEMS, AssetFilter.Criterion.ALL, "player"))
                .extracting(AssetItem::id)
                .containsExactly("player.png", "PlayerAttack.java");
        assertThat(AssetFilter.apply(ITEMS, AssetFilter.Criterion.ALL, "PLAYER"))
                .extracting(AssetItem::id)
                .containsExactly("player.png", "PlayerAttack.java");
    }

    @Test
    void queryIsTrimmedAndCombinableWithCriterion() {
        assertThat(AssetFilter.apply(ITEMS, AssetFilter.Criterion.IMAGE, "  attack "))
                .isEmpty();
        assertThat(AssetFilter.apply(ITEMS, AssetFilter.Criterion.SCRIPT, "  attack "))
                .extracting(AssetItem::id)
                .containsExactly("PlayerAttack.java");
    }

    @Test
    void nullQueryIsTreatedAsEmpty() {
        assertThat(AssetFilter.apply(ITEMS, AssetFilter.Criterion.ALL, null)).hasSize(ITEMS.size());
    }
}
