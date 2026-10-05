package io.stratixops.engine.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MiniJsonTest {

    @Test
    void parsesObjectWithMixedValues() {
        Object value = MiniJson.parse("{ \"a\": 1, \"b\": \"x\", \"c\": true, \"d\": null, \"e\": [1, 2] }");
        assertThat(value).isInstanceOf(Map.class);
        Map<?, ?> map = (Map<?, ?>) value;
        assertThat(map.get("a")).isEqualTo(1.0);
        assertThat(map.get("b")).isEqualTo("x");
        assertThat(map.get("c")).isEqualTo(Boolean.TRUE);
        assertThat(map.get("d")).isNull();
        assertThat((List<Object>) map.get("e")).containsExactly(1.0, 2.0);
    }

    @Test
    void parsesNestedObjectsAndWhitespace() {
        Object value = MiniJson.parse("{\n  \"regions\": { \"p\": { \"x\": 0 } }\n}");
        Map<?, ?> root = (Map<?, ?>) value;
        Map<?, ?> regions = (Map<?, ?>) root.get("regions");
        Map<?, ?> p = (Map<?, ?>) regions.get("p");
        assertThat(p.get("x")).isEqualTo(0.0);
    }

    @Test
    void parsesStringEscapes() {
        Object value = MiniJson.parse("\"a\\n\\t\\\"b\\\"\\\\\"");
        assertThat(value).isEqualTo("a\n\t\"b\"\\");
    }

    @Test
    void parsesNegativeAndExponentNumbers() {
        assertThat(MiniJson.parse("-1.5e3")).isEqualTo(-1500.0);
    }

    @Test
    void parsesEmptyContainers() {
        assertThat((Map<?, ?>) MiniJson.parse("{}")).isEmpty();
        assertThat((List<?>) MiniJson.parse("[]")).isEmpty();
    }

    @Test
    void rejectsTrailingContent() {
        assertThatIllegalArgumentException().isThrownBy(() -> MiniJson.parse("{} x"));
    }

    @Test
    void rejectsUnterminatedString() {
        assertThatIllegalArgumentException().isThrownBy(() -> MiniJson.parse("\"abc"));
    }

    @Test
    void rejectsBadEscape() {
        assertThatIllegalArgumentException().isThrownBy(() -> MiniJson.parse("\"\\q\""));
    }

    @Test
    void rejectsMissingColon() {
        assertThatIllegalArgumentException().isThrownBy(() -> MiniJson.parse("{\"a\" 1}"));
    }

    @Test
    void rejectsUnexpectedCharacter() {
        assertThatIllegalArgumentException().isThrownBy(() -> MiniJson.parse("@"));
    }

    @Test
    void rejectsNullInput() {
        assertThatIllegalArgumentException().isThrownBy(() -> MiniJson.parse(null));
    }

    @Test
    void rejectsMissingObjectValueSeparator() {
        assertThatIllegalArgumentException().isThrownBy(() -> MiniJson.parse("{\"a\":1 \"b\":2}"));
    }

    @Test
    void rejectsMissingArraySeparator() {
        assertThatIllegalArgumentException().isThrownBy(() -> MiniJson.parse("[1 2]"));
    }
}
