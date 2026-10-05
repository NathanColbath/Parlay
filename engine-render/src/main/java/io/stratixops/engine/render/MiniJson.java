package io.stratixops.engine.render;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A tiny, dependency-free JSON reader used for atlas metadata
 * ({@code { "image": "atlas.png", "regions": { name: {x,y,w,h} } }}).
 *
 * <p>The engine deliberately adds no JSON library (spec section 5.3). This
 * parser covers the standard value grammar: objects, arrays, strings, numbers,
 * booleans and {@code null}. It is pure and headless-testable.
 *
 * <p>Result mapping: object to {@code Map<String,Object>}, array to
 * {@code List<Object>}, string to {@code String}, number to {@code Double},
 * boolean to {@code Boolean}, and null to {@code null}.
 */
public final class MiniJson {

    private final String text;
    private int index;

    private MiniJson(String text) {
        this.text = text;
    }

    /**
     * Parses a JSON document.
     *
     * @param text the document
     * @return the decoded value tree
     * @throws IllegalArgumentException if the document is malformed
     */
    public static Object parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("JSON text must not be null");
        }
        MiniJson reader = new MiniJson(text);
        reader.skipWhitespace();
        Object value = reader.readValue();
        reader.skipWhitespace();
        if (reader.index != text.length()) {
            throw reader.error("trailing content");
        }
        return value;
    }

    private Object readValue() {
        if (index >= text.length()) {
            throw error("unexpected end of input");
        }
        char c = text.charAt(index);
        switch (c) {
            case '{':
                return readObject();
            case '[':
                return readArray();
            case '"':
                return readString();
            case 't':
                expectLiteral("true");
                return Boolean.TRUE;
            case 'f':
                expectLiteral("false");
                return Boolean.FALSE;
            case 'n':
                expectLiteral("null");
                return null;
            default:
                if (c == '-' || (c >= '0' && c <= '9')) {
                    return readNumber();
                }
                throw error("unexpected character '" + c + "'");
        }
    }

    private Map<String, Object> readObject() {
        Map<String, Object> object = new LinkedHashMap<>();
        index++;
        skipWhitespace();
        if (peek() == '}') {
            index++;
            return object;
        }
        while (true) {
            skipWhitespace();
            if (peek() != '"') {
                throw error("expected object key");
            }
            String key = readString();
            skipWhitespace();
            if (peek() != ':') {
                throw error("expected ':' after object key");
            }
            index++;
            skipWhitespace();
            object.put(key, readValue());
            skipWhitespace();
            char next = peek();
            if (next == ',') {
                index++;
                continue;
            }
            if (next == '}') {
                index++;
                return object;
            }
            throw error("expected ',' or '}' in object");
        }
    }

    private List<Object> readArray() {
        List<Object> array = new ArrayList<>();
        index++;
        skipWhitespace();
        if (peek() == ']') {
            index++;
            return array;
        }
        while (true) {
            skipWhitespace();
            array.add(readValue());
            skipWhitespace();
            char next = peek();
            if (next == ',') {
                index++;
                continue;
            }
            if (next == ']') {
                index++;
                return array;
            }
            throw error("expected ',' or ']' in array");
        }
    }

    private String readString() {
        index++;
        StringBuilder builder = new StringBuilder();
        while (true) {
            if (index >= text.length()) {
                throw error("unterminated string");
            }
            char c = text.charAt(index++);
            if (c == '"') {
                return builder.toString();
            }
            if (c == '\\') {
                if (index >= text.length()) {
                    throw error("unterminated escape");
                }
                char esc = text.charAt(index++);
                switch (esc) {
                    case '"':
                        builder.append('"');
                        break;
                    case '\\':
                        builder.append('\\');
                        break;
                    case '/':
                        builder.append('/');
                        break;
                    case 'b':
                        builder.append('\b');
                        break;
                    case 'f':
                        builder.append('\f');
                        break;
                    case 'n':
                        builder.append('\n');
                        break;
                    case 'r':
                        builder.append('\r');
                        break;
                    case 't':
                        builder.append('\t');
                        break;
                    case 'u':
                        if (index + 4 > text.length()) {
                            throw error("bad unicode escape");
                        }
                        builder.append((char) Integer.parseInt(text.substring(index, index + 4), 16));
                        index += 4;
                        break;
                    default:
                        throw error("invalid escape '\\" + esc + "'");
                }
            } else {
                builder.append(c);
            }
        }
    }

    private Double readNumber() {
        int start = index;
        if (peek() == '-') {
            index++;
        }
        while (index < text.length() && Character.isDigit(text.charAt(index))) {
            index++;
        }
        if (index < text.length() && text.charAt(index) == '.') {
            index++;
            while (index < text.length() && Character.isDigit(text.charAt(index))) {
                index++;
            }
        }
        if (index < text.length() && (text.charAt(index) == 'e' || text.charAt(index) == 'E')) {
            index++;
            if (index < text.length() && (text.charAt(index) == '+' || text.charAt(index) == '-')) {
                index++;
            }
            while (index < text.length() && Character.isDigit(text.charAt(index))) {
                index++;
            }
        }
        try {
            return Double.valueOf(text.substring(start, index));
        } catch (NumberFormatException e) {
            throw error("invalid number");
        }
    }

    private void expectLiteral(String literal) {
        if (!text.startsWith(literal, index)) {
            throw error("expected '" + literal + "'");
        }
        index += literal.length();
    }

    private char peek() {
        if (index >= text.length()) {
            throw error("unexpected end of input");
        }
        return text.charAt(index);
    }

    private void skipWhitespace() {
        while (index < text.length()) {
            char c = text.charAt(index);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                index++;
            } else {
                break;
            }
        }
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException("Malformed JSON at index " + index + ": " + message);
    }
}
