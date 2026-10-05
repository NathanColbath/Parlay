package io.stratixops.engine.serialize;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.stratixops.engine.inspect.FieldDescriptor;
import io.stratixops.engine.inspect.PropertyKind;
import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.scene.AssetRef;
import io.stratixops.engine.scene.Component;
import io.stratixops.engine.scene.Guid;
import io.stratixops.engine.scene.ObjectRef;
import io.stratixops.engine.scene.PrefabRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Translates a component field to and from JSON, driven entirely by
 * {@link FieldDescriptor} metadata (ADR-0006 §2). No reflective auto-binding:
 * the mapping, and therefore the on-disk shape, is explicit and stable.
 *
 * <p>Final mutable fields ({@code Vec2} positions, {@code List} frames) are
 * mutated in place rather than reassigned, because reflection cannot write
 * final fields.
 */
final class ValueCodec {

    private static final String REF = "$ref";
    private static final String ASSET = "$asset";

    private ValueCodec() {
    }

    static JsonElement write(FieldDescriptor field, Component component) {
        Object value = field.get(component);
        if (field.kind() == PropertyKind.LIST) {
            JsonArray array = new JsonArray();
            if (value instanceof Iterable<?> iterable) {
                for (Object element : iterable) {
                    array.add(writeScalar(field.elementKind(), element));
                }
            }
            return array;
        }
        return writeScalar(field.kind(), value);
    }

    static void readInto(FieldDescriptor field, Component component, JsonElement element) {
        switch (field.kind()) {
            case VEC2 -> {
                Object existing = field.get(component);
                Vec2 vec = existing instanceof Vec2 v ? v : new Vec2();
                if (!(existing instanceof Vec2)) {
                    field.set(component, vec);
                }
                Vec2 read = readVec2(element);
                vec.set(read.x, read.y);
            }
            case LIST -> {
                List<Object> decoded = new ArrayList<>();
                if (element != null && element.isJsonArray()) {
                    for (JsonElement item : element.getAsJsonArray()) {
                        decoded.add(readScalar(field.elementKind(), field.elementType(), item));
                    }
                }
                Object existing = field.get(component);
                if (existing instanceof List<?> list) {
                    @SuppressWarnings("unchecked")
                    List<Object> raw = (List<Object>) list;
                    raw.clear();
                    raw.addAll(decoded);
                } else {
                    field.set(component, decoded);
                }
            }
            default -> field.set(component, readScalar(field.kind(), field.type(), element));
        }
    }

    private static JsonElement writeScalar(PropertyKind kind, Object value) {
        if (kind == null) {
            return JsonNull.INSTANCE;
        }
        return switch (kind) {
            case BOOLEAN -> new JsonPrimitive((Boolean) value);
            case INT -> new JsonPrimitive((Number) value);
            case LONG -> new JsonPrimitive((Number) value);
            case FLOAT -> new JsonPrimitive((Number) value);
            case DOUBLE -> new JsonPrimitive((Number) value);
            case STRING -> value == null ? JsonNull.INSTANCE : new JsonPrimitive((String) value);
            case ENUM -> new JsonPrimitive(((Enum<?>) value).name());
            case VEC2 -> vec2((Vec2) value);
            case COLOR -> new JsonPrimitive(colorString((Integer) value));
            case ASSET -> ref(value instanceof AssetRef r ? r.guid() : null, ASSET);
            case GAMEOBJECT -> ref(value instanceof ObjectRef r ? r.guid() : null, REF);
            case PREFAB -> ref(value instanceof PrefabRef r ? r.guid() : null, REF);
            default -> JsonNull.INSTANCE;
        };
    }

    private static Object readScalar(PropertyKind kind, Class<?> type, JsonElement element) {
        if (kind == null || element == null || element.isJsonNull()) {
            if (kind == PropertyKind.STRING) {
                return null;
            }
            if (kind == PropertyKind.ASSET) {
                return new AssetRef(null);
            }
            if (kind == PropertyKind.GAMEOBJECT) {
                return new ObjectRef(null);
            }
            if (kind == PropertyKind.PREFAB) {
                return new PrefabRef(null);
            }
            if (kind == null || kind == PropertyKind.VEC2 || kind == PropertyKind.LIST) {
                return null;
            }
        }
        return switch (kind) {
            case BOOLEAN -> element.getAsBoolean();
            case INT -> boxInt(type, element.getAsInt());
            case LONG -> element.getAsLong();
            case FLOAT -> element.getAsFloat();
            case DOUBLE -> element.getAsDouble();
            case STRING -> element.getAsString();
            case ENUM -> readEnum(type, element.getAsString());
            case VEC2 -> readVec2(element);
            case COLOR -> parseColor(element.getAsString());
            case ASSET -> new AssetRef(guid(element, ASSET));
            case GAMEOBJECT -> new ObjectRef(guid(element, REF));
            case PREFAB -> new PrefabRef(guid(element, REF));
            default -> null;
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object readEnum(Class<?> type, String name) {
        if (type == null || !type.isEnum()) {
            return null;
        }
        return Enum.valueOf((Class<? extends Enum>) type, name);
    }

    private static Object boxInt(Class<?> type, int value) {
        if (type == byte.class || type == Byte.class) {
            return (byte) value;
        }
        if (type == short.class || type == Short.class) {
            return (short) value;
        }
        return value;
    }

    private static JsonObject vec2(Vec2 value) {
        JsonObject object = new JsonObject();
        object.addProperty("x", value.x);
        object.addProperty("y", value.y);
        return object;
    }

    private static Vec2 readVec2(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        return new Vec2(object.get("x").getAsFloat(), object.get("y").getAsFloat());
    }

    static String colorString(int value) {
        return String.format(Locale.ROOT, "#%08x", value);
    }

    static int parseColor(String text) {
        try {
            String hex = text.startsWith("#") ? text.substring(1) : text;
            return (int) Long.parseLong(hex, 16);
        } catch (NumberFormatException e) {
            throw new SerializationException("not a valid color: " + text, e);
        }
    }

    private static JsonElement ref(Guid guid, String key) {
        if (guid == null) {
            return JsonNull.INSTANCE;
        }
        JsonObject object = new JsonObject();
        object.addProperty(key, guid.value());
        return object;
    }

    private static Guid guid(JsonElement element, String key) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        JsonObject object = element.getAsJsonObject();
        if (!object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }
        return Guid.of(object.get(key).getAsString());
    }
}
