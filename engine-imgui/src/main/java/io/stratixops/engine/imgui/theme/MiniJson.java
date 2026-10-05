package io.stratixops.engine.imgui.theme;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal recursive-descent JSON reader for the theme token document.
 *
 * <p>engine-imgui intentionally carries no JSON dependency (ADR-0008: only
 * engine-serialize depends on JSON); this reader exists solely to load the
 * shipped {@code tokens.json} resource. It handles the full JSON grammar that
 * the token file uses and fails loudly on anything malformed.
 */
final class MiniJson {

    private final String source;
    private int index;

    private MiniJson(String source) {
        this.source = source;
    }

    static Object parse(String source) {
        MiniJson parser = new MiniJson(source);
        parser.skipWhitespace();
        Object value = parser.parseValue();
        parser.skipWhitespace();
        if (parser.index != source.length()) {
            throw parser.error("unexpected trailing content");
        }
        return value;
    }

    private Object parseValue() {
        char c = peek();
        return switch (c) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> parseString();
            case 't' -> parseLiteral("true", Boolean.TRUE);
            case 'f' -> parseLiteral("false", Boolean.FALSE);
            case 'n' -> parseLiteral("null", null);
            default -> parseNumber();
        };
    }

    private Map<String, Object> parseObject() {
        expect('{');
        Map<String, Object> object = new LinkedHashMap<>();
        skipWhitespace();
        if (peek() == '}') {
            index++;
            return object;
        }
        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            object.put(key, parseValue());
            skipWhitespace();
            char c = next();
            if (c == '}') {
                return object;
            }
            if (c != ',') {
                throw error("expected ',' or '}' in object");
            }
        }
    }

    private List<Object> parseArray() {
        expect('[');
        List<Object> array = new ArrayList<>();
        skipWhitespace();
        if (peek() == ']') {
            index++;
            return array;
        }
        while (true) {
            skipWhitespace();
            array.add(parseValue());
            skipWhitespace();
            char c = next();
            if (c == ']') {
                return array;
            }
            if (c != ',') {
                throw error("expected ',' or ']' in array");
            }
        }
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = next();
            if (c == '"') {
                return sb.toString();
            }
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            char esc = next();
            switch (esc) {
                case '"' -> sb.append('"');
                case '\\' -> sb.append('\\');
                case '/' -> sb.append('/');
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 'n' -> sb.append('\n');
                case 'r' -> sb.append('\r');
                case 't' -> sb.append('\t');
                case 'u' -> {
                    sb.append((char) Integer.parseInt(source.substring(index, index + 4), 16));
                    index += 4;
                }
                default -> throw error("bad escape: \\" + esc);
            }
        }
    }

    private Object parseLiteral(String literal, Object value) {
        if (!source.startsWith(literal, index)) {
            throw error("expected '" + literal + "'");
        }
        index += literal.length();
        return value;
    }

    private Double parseNumber() {
        int start = index;
        while (index < source.length()) {
            char c = source.charAt(index);
            if (c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E'
                    || (c >= '0' && c <= '9')) {
                index++;
            } else {
                break;
            }
        }
        if (start == index) {
            throw error("expected a value");
        }
        try {
            return Double.valueOf(source.substring(start, index));
        } catch (NumberFormatException e) {
            throw error("bad number: " + source.substring(start, index));
        }
    }

    private void skipWhitespace() {
        while (index < source.length() && Character.isWhitespace(source.charAt(index))) {
            index++;
        }
    }

    private char peek() {
        if (index >= source.length()) {
            throw error("unexpected end of input");
        }
        return source.charAt(index);
    }

    private char next() {
        char c = peek();
        index++;
        return c;
    }

    private void expect(char expected) {
        if (next() != expected) {
            throw error("expected '" + expected + "'");
        }
    }

    private IllegalStateException error(String message) {
        return new IllegalStateException("JSON at offset " + index + ": " + message);
    }
}
