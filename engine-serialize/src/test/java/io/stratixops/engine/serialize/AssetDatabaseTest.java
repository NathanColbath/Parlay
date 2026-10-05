package io.stratixops.engine.serialize;

import io.stratixops.engine.scene.Guid;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AssetDatabaseTest {

    @Test
    void importCreatesSidecarWithGuidAndImporter(@TempDir Path dir) throws Exception {
        Path png = dir.resolve("hero.png");
        Files.writeString(png, "fake");
        AssetDatabase database = new AssetDatabase(dir);

        Asset asset = database.importAsset(png);
        assertThat(asset.guid()).isNotNull();
        assertThat(asset.meta().importer()).isEqualTo("png");
        assertThat(asset.meta().importerVersion()).isEqualTo(1);
        assertThat(asset.meta().settings()).containsEntry("filter", "nearest");
        assertThat(Files.exists(database.metaPath(png))).isTrue();
        assertThat(database.resolve(asset.guid())).contains(asset);
    }

    @Test
    void reimportingKeepsTheSameGuid(@TempDir Path dir) throws Exception {
        Path png = dir.resolve("hero.png");
        Files.writeString(png, "fake");
        AssetDatabase database = new AssetDatabase(dir);

        Asset first = database.importAsset(png);
        Asset second = database.importAsset(png);
        assertThat(second.guid()).isEqualTo(first.guid());
    }

    @Test
    void movingAnAssetPreservesIdentity(@TempDir Path dir) throws Exception {
        Path png = dir.resolve("hero.png");
        Files.writeString(png, "fake");
        AssetDatabase database = new AssetDatabase(dir);
        Asset original = database.importAsset(png);

        Path moved = dir.resolve("sprites").resolve("hero.png");
        Asset after = database.moveAsset(png, moved);

        assertThat(after.guid()).isEqualTo(original.guid());
        assertThat(after.path()).isEqualTo(moved.toAbsolutePath().normalize());
        assertThat(Files.exists(moved)).isTrue();
        assertThat(Files.exists(database.metaPath(moved))).isTrue();
        assertThat(Files.exists(png)).isFalse();
        assertThat(database.resolve(original.guid())).isPresent();
        assertThat(database.resolve(original.guid()).orElseThrow().path()).isEqualTo(moved);
    }

    @Test
    void scanIndexesExistingSidecars(@TempDir Path dir) throws Exception {
        Path png = dir.resolve("hero.png");
        Files.writeString(png, "fake");
        AssetDatabase importer = new AssetDatabase(dir);
        Asset imported = importer.importAsset(png);

        AssetDatabase reopened = new AssetDatabase(dir);
        assertThat(reopened.resolve(imported.guid())).isEmpty();
        assertThat(reopened.scan()).isEqualTo(1);
        assertThat(reopened.resolve(imported.guid())).isPresent();
    }

    @Test
    void scanOnMissingRootReturnsZero(@TempDir Path dir) {
        AssetDatabase database = new AssetDatabase(dir.resolve("missing"));
        assertThat(database.scan()).isZero();
    }

    @Test
    void unknownExtensionGetsRawImporter(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("data.xyz");
        Files.writeString(file, "raw");
        AssetDatabase database = new AssetDatabase(dir);
        Asset asset = database.importAsset(file);
        assertThat(asset.meta().importer()).isEqualTo("raw");
        assertThat(asset.meta().importerVersion()).isZero();
    }

    @Test
    void reimportBumpsWhenImporterVersionChanges(@TempDir Path dir) throws Exception {
        Path png = dir.resolve("hero.png");
        Files.writeString(png, "fake");
        AssetDatabase original = new AssetDatabase(dir);
        Asset imported = original.importAsset(png);

        MetaFile downgraded = new MetaFile(imported.guid(), "png", 0, Map.of("filter", "nearest"));
        JsonFiles.writeAtomic(original.metaPath(png), downgraded.toJson());

        ImporterRegistry registry = new ImporterRegistry();
        registry.register(new Importer() {
            @Override
            public String id() {
                return "png";
            }

            @Override
            public int version() {
                return 5;
            }

            @Override
            public boolean supports(String extension) {
                return "png".equals(extension);
            }

            @Override
            public Map<String, Object> defaultSettings() {
                return Map.of("filter", "nearest");
            }
        });
        AssetDatabase upgraded = new AssetDatabase(dir, registry);
        Asset result = upgraded.reimport(png);

        assertThat(result.guid()).isEqualTo(imported.guid());
        assertThat(result.meta().importerVersion()).isEqualTo(5);
    }

    @Test
    void resolvingUnknownGuidIsEmpty(@TempDir Path dir) {
        AssetDatabase database = new AssetDatabase(dir);
        assertThat(database.resolve(Guid.random())).isEmpty();
        assertThat(database.root()).isEqualTo(dir.toAbsolutePath().normalize());
        assertThat(database.importers()).isNotNull();
    }
}
