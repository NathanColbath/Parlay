package io.stratixops.engine.editor.assets;

import io.stratixops.engine.imgui.widget.AssetItem;
import io.stratixops.engine.imgui.widget.AssetKind;
import io.stratixops.engine.imgui.widget.AssetStatus;
import io.stratixops.engine.imgui.widget.Breadcrumb;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AssetBrowserModelTest {

    @TempDir
    Path root;

    private void touch(String relative) throws IOException {
        Path path = root.resolve(relative);
        Files.createDirectories(path.getParent());
        Files.writeString(path, "x");
    }

    @Test
    void foldersComeFirstThenFilesAlphabetically() throws IOException {
        touch("zebra.png");
        touch("alpha.scene");
        Files.createDirectories(root.resolve("misc"));
        AssetBrowserModel model = new AssetBrowserModel(root);

        List<AssetItem> items = model.list(null);

        assertThat(items).extracting(AssetItem::name)
                .containsExactly("misc", "alpha.scene", "zebra.png");
        assertThat(items.get(0).isFolder()).isTrue();
    }

    @Test
    void sidecarsAreHiddenAndMarkImportedAssetsReady() throws IOException {
        touch("sprites/player.png");
        touch("sprites/player.png.meta");
        AssetBrowserModel model = new AssetBrowserModel(root);

        List<AssetItem> items = model.list(root.resolve("sprites"));

        assertThat(items).extracting(AssetItem::name).containsExactly("player.png");
        assertThat(items.get(0).status()).isEqualTo(AssetStatus.READY);
        assertThat(items.get(0).kind()).isEqualTo(AssetKind.IMAGE);
    }

    @Test
    void recognisedAssetWithoutSidecarIsUnimported() throws IOException {
        touch("audio/theme.ogg");
        AssetBrowserModel model = new AssetBrowserModel(root);

        AssetItem item = model.list(root.resolve("audio")).get(0);
        assertThat(item.status()).isEqualTo(AssetStatus.UNIMPORTED);
        assertThat(item.kind()).isEqualTo(AssetKind.AUDIO);
    }

    @Test
    void breadcrumbWalksFromRootToFolder() throws IOException {
        Files.createDirectories(root.resolve("sprites/characters"));
        AssetBrowserModel model = new AssetBrowserModel(root);

        List<Breadcrumb.Segment> segments = model.breadcrumb(root.resolve("sprites/characters"));

        assertThat(segments).extracting(Breadcrumb.Segment::label)
                .containsExactly("Project", "sprites", "characters");
        assertThat(segments.get(2).path()).isEqualTo("sprites/characters");
        assertThat(segments.get(0).path()).isEmpty();
    }

    @Test
    void resolveRoundTripsRootRelativeIds() throws IOException {
        touch("a/b/gem.png");
        AssetBrowserModel model = new AssetBrowserModel(root);
        AssetItem item = model.list(root.resolve("a/b")).get(0);

        assertThat(model.resolve(item)).isEqualTo(root.resolve("a/b/gem.png").toAbsolutePath().normalize());
        assertThat(model.resolve(null)).isEqualTo(root);
    }

    @Test
    void missingDirectoryListsNothing() {
        AssetBrowserModel model = new AssetBrowserModel(root);
        assertThat(model.list(root.resolve("nope"))).isEmpty();
    }

    @Test
    void kindMappingCoversTheProjectAssetTypes() {
        assertThat(AssetBrowserModel.kindOf("level.scene")).isEqualTo(AssetKind.SCENE);
        assertThat(AssetBrowserModel.kindOf("bot.prefab")).isEqualTo(AssetKind.PREFAB);
        assertThat(AssetBrowserModel.kindOf("Main.java")).isEqualTo(AssetKind.SCRIPT);
        assertThat(AssetBrowserModel.kindOf("player.png")).isEqualTo(AssetKind.IMAGE);
        assertThat(AssetBrowserModel.kindOf("theme.ogg")).isEqualTo(AssetKind.AUDIO);
        assertThat(AssetBrowserModel.kindOf("pixel.ttf")).isEqualTo(AssetKind.FONT);
        assertThat(AssetBrowserModel.kindOf("README")).isNull();
    }
}
