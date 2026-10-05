package io.stratixops.engine.inspect;

import io.stratixops.engine.scene.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Immutable metadata for one serializable/inspectable field (ADR-0007).
 *
 * <p>Built once per class and cached by {@link ComponentDescriptor}; the frame
 * loop performs no reflection. {@link #get(Component)} / {@link #set(Component,
 * Object)} are used only during load/save and Inspector edits.
 */
public final class FieldDescriptor {

    /** Sort key for fields with no explicit {@link Order}. */
    static final int NO_ORDER = Integer.MAX_VALUE;

    private final Field field;
    private final String name;
    private final String displayName;
    private final PropertyKind kind;
    private final PropertyKind elementKind;
    private final Class<?> elementType;
    private final int order;
    private final int declarationIndex;
    private final boolean serialized;
    private final String header;
    private final Float rangeMin;
    private final Float rangeMax;
    private final String tooltip;
    private final Object defaultValue;

    private FieldDescriptor(Field field, String name, String displayName, PropertyKind kind,
                            PropertyKind elementKind, Class<?> elementType, int order, int declarationIndex,
                            boolean serialized, String header, Float rangeMin, Float rangeMax,
                            String tooltip, Object defaultValue) {
        this.field = field;
        this.name = name;
        this.displayName = displayName;
        this.kind = kind;
        this.elementKind = elementKind;
        this.elementType = elementType;
        this.order = order;
        this.declarationIndex = declarationIndex;
        this.serialized = serialized;
        this.header = header;
        this.rangeMin = rangeMin;
        this.rangeMax = rangeMax;
        this.tooltip = tooltip;
        this.defaultValue = defaultValue;
    }

    static FieldDescriptor build(Field field, int declarationIndex, Object prototype) {
        field.setAccessible(true);
        int modifiers = field.getModifiers();
        boolean isStatic = Modifier.isStatic(modifiers);
        boolean isTransient = Modifier.isTransient(modifiers);
        boolean isFinal = Modifier.isFinal(modifiers);
        boolean isPublic = Modifier.isPublic(modifiers);
        boolean optIn = field.isAnnotationPresent(SerializeField.class);
        boolean optOut = field.isAnnotationPresent(NonSerialized.class);

        Color color = field.getAnnotation(Color.class);
        PropertyKind kind = PropertyKind.fromType(field.getType(), color != null && isInt(field.getType()));

        PropertyKind elementKind = null;
        Class<?> elementType = null;
        if (kind == PropertyKind.LIST) {
            elementType = elementTypeOf(field);
            elementKind = elementType == null ? PropertyKind.UNSUPPORTED
                    : PropertyKind.fromType(elementType, false);
            if (elementKind == PropertyKind.UNSUPPORTED) {
                kind = PropertyKind.UNSUPPORTED;
            }
        }

        boolean serialized = !isStatic && !isTransient && !optOut && kind.isSupported()
                && ((isPublic && !isFinal) || optIn);

        Header header = field.getAnnotation(Header.class);
        Range range = field.getAnnotation(Range.class);
        Tooltip tooltip = field.getAnnotation(Tooltip.class);
        Order order = field.getAnnotation(Order.class);

        return new FieldDescriptor(
                field,
                field.getName(),
                humanize(field.getName()),
                kind,
                elementKind,
                elementType,
                order == null ? NO_ORDER : order.value(),
                declarationIndex,
                serialized,
                header == null ? null : header.value(),
                range == null ? null : range.min(),
                range == null ? null : range.max(),
                tooltip == null ? null : tooltip.value(),
                prototype == null ? null : readDefault(field, prototype));
    }

    private static Class<?> elementTypeOf(Field field) {
        Type generic = field.getGenericType();
        if (generic instanceof ParameterizedType parameterized) {
            Type[] args = parameterized.getActualTypeArguments();
            if (args.length == 1 && args[0] instanceof Class<?> elementType) {
                return elementType;
            }
        }
        return null;
    }

    private static boolean isInt(Class<?> type) {
        return type == int.class || type == Integer.class;
    }

    private static Object readDefault(Field field, Object prototype) {
        try {
            return field.get(prototype);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private static String humanize(String raw) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                sb.append(' ');
            }
            sb.append(i == 0 ? Character.toUpperCase(c) : c);
        }
        return sb.toString();
    }

    /**
     * @return the serialized field name
     */
    public String name() {
        return name;
    }

    /**
     * @return the declared field type
     */
    public Class<?> type() {
        return field.getType();
    }

    /**
     * @return the human-readable Inspector label
     */
    public String displayName() {
        return displayName;
    }

    /**
     * @return the value kind
     */
    public PropertyKind kind() {
        return kind;
    }

    /**
     * @return the element kind for {@link PropertyKind#LIST}, else {@code null}
     */
    public PropertyKind elementKind() {
        return elementKind;
    }

    /**
     * @return the element class for {@link PropertyKind#LIST}, else {@code null}
     */
    public Class<?> elementType() {
        return elementType;
    }

    /**
     * @return the explicit {@link Order} key, or {@link Integer#MAX_VALUE}
     */
    public int order() {
        return order;
    }

    /**
     * @return the declaration position within the class hierarchy
     */
    public int declarationIndex() {
        return declarationIndex;
    }

    /**
     * @return true when this field participates in serialization
     */
    public boolean serialized() {
        return serialized;
    }

    /**
     * @return the optional {@link Header} text
     */
    public String header() {
        return header;
    }

    /**
     * @return the optional {@link Range} minimum
     */
    public Float rangeMin() {
        return rangeMin;
    }

    /**
     * @return the optional {@link Range} maximum
     */
    public Float rangeMax() {
        return rangeMax;
    }

    /**
     * @return the optional {@link Tooltip} text
     */
    public String tooltip() {
        return tooltip;
    }

    /**
     * @return true when a {@link Range} hint is present
     */
    public boolean hasRange() {
        return rangeMin != null && rangeMax != null;
    }

    /**
     * @return the value read from a prototype instance at build time
     */
    public Object defaultValue() {
        return defaultValue;
    }

    /**
     * Reads this field from a component instance.
     *
     * @param target the component
     * @return the value
     * @throws IllegalArgumentException if the field cannot be read
     */
    public Object get(Component target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("cannot read field " + name, e);
        }
    }

    /**
     * Writes this field on a component instance.
     *
     * @param target the component
     * @param value  the value
     * @throws IllegalArgumentException if the field cannot be written
     */
    public void set(Component target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new IllegalArgumentException("cannot write field " + name, e);
        }
    }

    /**
     * @return true when the field type is {@code List}
     */
    boolean isList() {
        return field.getType() == List.class;
    }
}
