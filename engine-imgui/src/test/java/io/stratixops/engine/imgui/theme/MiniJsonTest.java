package io.stratixops.engine.imgui.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MiniJsonTest {

    @SuppressWarnings("unchecked")
    @Test
    void parsesNestedObjects() {
        Map<String, Object> parsed = (Map<String, Object>) MiniJson.parse(
                "{\"a\": {\"b\": 1}, \"c\": \"x\"}");
        assertThat(parsed).containsKeys("a", "c");
        assertThat(((Map<String, Object>) parsed.get("a")).get("b")).isEqualTo(1.0);
        assertThat(parsed.get("c")).isEqualTo("x");
    }

    @Test
    void parsesArraysAndNumbers() {
        Object parsed = MiniJson.parse("[0.2, 0, 0, 1]");
        assertThat(parsed).isInstanceOf(List.class);
        assertThat((List<Object>) parsed).containsExactly(0.2, 0.0, 0.0, 1.0);
    }

    @Test
    void parsesEscapesAndLiterals() {
        assertThat(MiniJson.parse("\"a\\nb\\t\\\"c\\\"\"")).isEqualTo("a\nb\t\"c\"");
        assertThat(MiniJson.parse("true")).isEqualTo(Boolean.TRUE);
        assertThat(MiniJson.parse("false")).isEqualTo(Boolean.FALSE);
        assertThat(MiniJson.parse("null")).isNull();
    }

    @Test
    void parsesUnicodeEscape() {
        assertThat(MiniJson.parse("\"\\u0041\"")).isEqualTo("A");
    }

    @Test
    void rejectsMalformedInput() {
        assertThatThrownBy(() -> MiniJson.parse("{\"a\": }"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> MiniJson.parse("[1,2"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> MiniJson.parse("{} trailing"))
                .isInstanceOf(IllegalStateException.class);
    }
}
