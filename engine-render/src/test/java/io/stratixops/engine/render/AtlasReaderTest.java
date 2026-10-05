package io.stratixops.engine.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class AtlasReaderTest {

    private static final String VALID = "{"
        + "\"image\": \"sheet.png\","
        + "\"regions\": {"
        + "  \"player\": { \"x\": 0, \"y\": 0, \"w\": 16, \"h\": 16 },"
        + "  \"gem\": { \"x\": 16, \"y\": 0, \"w\": 8, \"h\": 8 }"
        + "}}";

    @Test
    void parsesImageAndRegions() {
        AtlasMetadata metadata = AtlasReader.parse(VALID);
        assertThat(metadata.image()).isEqualTo("sheet.png");
        assertThat(metadata.regions()).containsKeys("player", "gem");
        assertThat(metadata.regions().get("gem").x()).isEqualTo(16);
    }

    @Test
    void resolvesRegionAgainstImageSize() {
        AtlasMetadata metadata = AtlasReader.parse(VALID);
        AtlasRegion region = metadata.resolve("player", 64, 64);
        assertThat(region).isNotNull();
        assertThat(region.width()).isEqualTo(16);
        assertThat(metadata.resolve("missing", 64, 64)).isNull();
    }

    @Test
    void rejectsNonObjectRoot() {
        assertThatIllegalArgumentException().isThrownBy(() -> AtlasReader.parse("[]"));
    }

    @Test
    void rejectsMissingImage() {
        assertThatIllegalArgumentException().isThrownBy(
            () -> AtlasReader.parse("{ \"regions\": {} }"));
    }

    @Test
    void rejectsEmptyImage() {
        assertThatIllegalArgumentException().isThrownBy(
            () -> AtlasReader.parse("{ \"image\": \"\", \"regions\": {} }"));
    }

    @Test
    void rejectsMissingRegions() {
        assertThatIllegalArgumentException().isThrownBy(
            () -> AtlasReader.parse("{ \"image\": \"a.png\" }"));
    }

    @Test
    void rejectsNonObjectRegion() {
        assertThatIllegalArgumentException().isThrownBy(
            () -> AtlasReader.parse("{ \"image\": \"a.png\", \"regions\": { \"p\": 4 } }"));
    }

    @Test
    void rejectsMissingRegionField() {
        assertThatIllegalArgumentException().isThrownBy(
            () -> AtlasReader.parse("{ \"image\": \"a.png\", \"regions\": { \"p\": { \"x\": 0, \"y\": 0, \"w\": 8 } } }"));
    }

    @Test
    void rejectsNonIntegerRegionField() {
        assertThatIllegalArgumentException().isThrownBy(
            () -> AtlasReader.parse("{ \"image\": \"a.png\", \"regions\": { \"p\": { \"x\": 0.5, \"y\": 0, \"w\": 8, \"h\": 8 } } }"));
    }
}
