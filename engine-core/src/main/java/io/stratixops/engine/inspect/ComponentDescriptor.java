package io.stratixops.engine.inspect;

import io.stratixops.engine.scene.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cached, immutable metadata for one {@link Component} type (ADR-0007).
 *
 * <p>Built once by reflection and shared by the serializer and the Inspector,
 * so the file format and the UI can never disagree about which fields are
 * serialized.
 */
public final class ComponentDescriptor {

    private final Class<? extends Component> type;
    private final String typeId;
    private final String displayName;
    private final String category;
    private final List<FieldDescriptor> fields;
    private final List<FieldDescriptor> serializedFields;
    private final Map<String, FieldDescriptor> byName;

    private ComponentDescriptor(Class<? extends Component> type, String typeId, String displayName,
                               String category, List<FieldDescriptor> fields) {
        this.type = type;
        this.typeId = typeId;
        this.displayName = displayName;
        this.category = category;
        this.fields = Collections.unmodifiableList(fields);

        List<FieldDescriptor> serialized = new ArrayList<>();
        Map<String, FieldDescriptor> index = new LinkedHashMap<>();
        for (FieldDescriptor field : fields) {
            if (field.serialized()) {
                serialized.add(field);
            }
            index.put(field.name(), field);
        }
        this.serializedFields = Collections.unmodifiableList(serialized);
        this.byName = Collections.unmodifiableMap(index);
    }

    /**
     * Builds a descriptor, validating the presence of a usable no-arg
     * constructor.
     *
     * @param type   the component class
     * @param typeId the stable serialized type id
     * @return the descriptor
     * @throws IllegalStateException if no no-arg constructor exists
     */
    public static ComponentDescriptor build(Class<? extends Component> type, String typeId) {
        Constructor<?> constructor;
        try {
            constructor = type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "component " + type.getName() + " must declare a no-arg constructor", e);
        }
        constructor.setAccessible(true);

        Object prototype;
        try {
            prototype = constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "component " + type.getName() + " no-arg constructor is not usable", e);
        }

        List<FieldDescriptor> fields = new ArrayList<>();
        int index = 0;
        for (Class<?> cursor = type; cursor != null && cursor != Component.class; cursor = cursor.getSuperclass()) {
            for (Field field : cursor.getDeclaredFields()) {
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                fields.add(FieldDescriptor.build(field, index++, prototype));
            }
        }
        fields.sort(Comparator
                .comparingInt(FieldDescriptor::order)
                .thenComparingInt(FieldDescriptor::declarationIndex));

        String category = type.getPackageName().startsWith("io.stratixops.engine")
                ? "Engine" : "Scripts";
        return new ComponentDescriptor(type, typeId, humanize(type.getSimpleName()), category, fields);
    }

    private static String humanize(String simpleName) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < simpleName.length(); i++) {
            char c = simpleName.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                sb.append(' ');
            }
            sb.append(i == 0 ? Character.toUpperCase(c) : c);
        }
        String out = sb.toString();
        return out.endsWith("2 D") ? out.replace("2 D", "2D") : out;
    }

    /**
     * @return the component class
     */
    public Class<? extends Component> type() {
        return type;
    }

    /**
     * @return the stable serialized type id
     */
    public String typeId() {
        return typeId;
    }

    /**
     * @return the Inspector display name
     */
    public String displayName() {
        return displayName;
    }

    /**
     * @return the Inspector category
     */
    public String category() {
        return category;
    }

    /**
     * @return every inspectable field, in display order
     */
    public List<FieldDescriptor> fields() {
        return fields;
    }

    /**
     * @return only the fields that participate in serialization
     */
    public List<FieldDescriptor> serializedFields() {
        return serializedFields;
    }

    /**
     * Looks up a field by serialized name.
     *
     * @param name the field name
     * @return the descriptor, or {@code null}
     */
    public FieldDescriptor field(String name) {
        return byName.get(name);
    }

    /**
     * Creates a new instance through the validated no-arg constructor.
     *
     * @return a fresh component
     */
    public Component newInstance() {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot instantiate " + type.getName(), e);
        }
    }
}
