package io.stratixops.engine.serialize;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.stratixops.engine.inspect.ComponentDescriptor;
import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.inspect.FieldDescriptor;
import io.stratixops.engine.scene.Component;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Guid;
import io.stratixops.engine.scene.Transform;
import io.stratixops.engine.scene.UnsupportedComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes the flat GameObject graph shared by scenes and prefabs
 * (ADR-0006 §5/§6). Objects are written pre-order into one flat array, each
 * carrying a {@code children} list of GUIDs; parenting is rebuilt by GUID, so
 * files are acyclic and diff-stable.
 */
final class GameObjectJson {

    private GameObjectJson() {
    }

    static JsonArray writeAll(Collection<GameObject> roots, ComponentRegistry registry) {
        JsonArray array = new JsonArray();
        for (GameObject root : roots) {
            append(root, array, registry);
        }
        return array;
    }

    private static void append(GameObject object, JsonArray out, ComponentRegistry registry) {
        out.add(writeObject(object, registry));
        for (GameObject child : object.children()) {
            append(child, out, registry);
        }
    }

    private static JsonObject writeObject(GameObject object, ComponentRegistry registry) {
        JsonObject json = new JsonObject();
        json.addProperty("guid", object.guid().value());
        json.addProperty("name", object.name());
        json.addProperty("active", object.active());
        json.addProperty("layer", object.layer());

        JsonArray tags = new JsonArray();
        for (String tag : object.tags()) {
            tags.add(tag);
        }
        json.add("tags", tags);

        JsonArray children = new JsonArray();
        for (GameObject child : object.children()) {
            children.add(child.guid().value());
        }
        json.add("children", children);

        JsonArray components = new JsonArray();
        for (Component component : object.components()) {
            components.add(writeComponent(component, registry));
        }
        json.add("components", components);
        return json;
    }

    private static JsonObject writeComponent(Component component, ComponentRegistry registry) {
        if (component instanceof UnsupportedComponent unsupported) {
            return JsonParser.parseString(unsupported.rawJson()).getAsJsonObject();
        }
        ComponentDescriptor descriptor = registry.descriptor(component.getClass());
        if (descriptor == null) {
            throw new SerializationException(
                    "component " + component.getClass().getName() + " is not registered");
        }
        JsonObject json = new JsonObject();
        json.addProperty("typeId", descriptor.typeId());
        json.addProperty("enabled", component.enabled());
        JsonObject properties = new JsonObject();
        for (FieldDescriptor field : descriptor.serializedFields()) {
            properties.add(field.name(), ValueCodec.write(field, component));
        }
        json.add("properties", properties);
        return json;
    }

    static List<GameObject> readAll(JsonArray array, ComponentRegistry registry) {
        Map<Guid, GameObject> byGuid = new LinkedHashMap<>();
        List<GameObject> order = new ArrayList<>();
        List<JsonObject> raw = new ArrayList<>();

        for (var element : array) {
            JsonObject json = element.getAsJsonObject();
            Guid guid = Guid.of(json.get("guid").getAsString());
            GameObject object = new GameObject(guid, json.get("name").getAsString());
            object.setActive(!json.has("active") || json.get("active").getAsBoolean());
            object.setLayer(json.has("layer") ? json.get("layer").getAsInt() : 0);
            if (json.has("tags")) {
                for (var tag : json.getAsJsonArray("tags")) {
                    object.tags().add(tag.getAsString());
                }
            }
            readComponents(json.getAsJsonArray("components"), object, registry);
            byGuid.put(guid, object);
            order.add(object);
            raw.add(json);
        }

        for (int i = 0; i < order.size(); i++) {
            GameObject object = order.get(i);
            JsonArray children = raw.get(i).getAsJsonArray("children");
            if (children == null) {
                continue;
            }
            for (var childGuid : children) {
                GameObject child = byGuid.get(Guid.of(childGuid.getAsString()));
                if (child != null) {
                    child.setParent(object);
                }
            }
        }

        List<GameObject> roots = new ArrayList<>();
        for (GameObject object : order) {
            if (object.parent() == null) {
                roots.add(object);
            }
        }
        return roots;
    }

    private static void readComponents(JsonArray components, GameObject object, ComponentRegistry registry) {
        if (components == null) {
            return;
        }
        for (var element : components) {
            JsonObject json = element.getAsJsonObject();
            String typeId = json.get("typeId").getAsString();
            boolean enabled = !json.has("enabled") || json.get("enabled").getAsBoolean();

            ComponentDescriptor descriptor = registry.descriptor(typeId);
            if (descriptor == null) {
                UnsupportedComponent placeholder = new UnsupportedComponent(typeId, JsonFiles.toJson(json));
                placeholder.setEnabled(enabled);
                object.addComponent(placeholder);
                continue;
            }
            if (descriptor.type() == Transform.class) {
                applyProperties(object.transform(), json, descriptor);
                object.transform().setEnabled(enabled);
                continue;
            }
            Component component = descriptor.newInstance();
            applyProperties(component, json, descriptor);
            component.setEnabled(enabled);
            object.addComponent(component);
        }
    }

    private static void applyProperties(Component component, JsonObject json, ComponentDescriptor descriptor) {
        if (!json.has("properties")) {
            return;
        }
        for (var entry : json.getAsJsonObject("properties").entrySet()) {
            FieldDescriptor field = descriptor.field(entry.getKey());
            if (field == null || !field.serialized()) {
                continue;
            }
            ValueCodec.readInto(field, component, entry.getValue());
        }
    }
}
