package me.altzenck.yml;

import lombok.NonNull;
import me.altzenck.yml.util.YmlUtils;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public interface Section {

    @SuppressWarnings("unchecked")
    default void setDefaults(Section otherSection) {
        Map<String, Object> map = otherSection.getMap(), defaultMap = createDefaults().getMap();
        defaultMap.clear();
        defaultMap.putAll((Map<String, Object>) YmlUtils.deepClone(map));
    }

    /**
     * Gets a section of the parsed yaml object (A "section" is understood to be any key that has a {@link Map} instance as its value).
     *
     * @return the current instance but with the section assigned, or <code>null</code> if the specified path does not exist or is not a section.
     */
    Section getSection(@NonNull String section);

    /**
     * Gets the value of the specified key path starting from the current section, interpreted as a <code>Section</code> list.
     *
     * @param path The key path to search.
     * @return the value obtained after {@link #get(String)} but interpreted as a <code>Section</code> list, or an immutable empty list if the path does not exist.
     */
    List<Section> getListSection(@NonNull String path);

    /**
     * Gets the value of the specified key path starting from the current section.
     *
     * @param path The key path to search.
     * @return the value assigned to this key path, or the default value specified by the default section if the path does not exist in the current section, or <code>null</code> if it does not exist in either.
     */
    Object get(@NonNull String path);

    default Object get(@NonNull String path, Object defaultValue) {
        return Objects.requireNonNullElse(get(path), defaultValue);
    }

    /**
     * Gets the value of the specified key path starting from the current section, interpreted as a <code>String</code>.
     *
     * @param path The key path to search.
     * @return the value obtained after {@link #get(String)} but interpreted as <code>String</code>, or null if the value is an invalid object (<code>null</code> or {@link Map},{@link List} instance).
     */
    default String getString(@NonNull String path) {
        Object o = get(path);
        if (o == null || o instanceof Collection || o instanceof Map) return null;
        return o.toString();
    }

    /**
     * Gets the path name to the current section of this instance.
     *
     * @return the path name of the current section.
     */
    Path getPath();

    /**
     * Creates an empty section from the specified path starting from the current section.<br></br>
     * If there are key paths in conflict with the specified path, they will be overwritten.
     *
     * @param path The path to the section to create.
     */
    Section createSection(String path);

    default boolean contains(String path) {
        return contains(path, new AtomicReference<>());
    }

    @SuppressWarnings("unchecked")
    default boolean contains(String path, AtomicReference<?> reference) {
        return contains(path, Objects.class, (AtomicReference<Object>) reference);
    }

    default boolean contains(String path, Class<?> clazz) {
        return contains(path, clazz, new AtomicReference<>());
    }

    @SuppressWarnings("unchecked")
    default <T> boolean contains(String path, Class<? extends T> clazz, AtomicReference<T> reference) {
        Object v = get(path);
        if(clazz.isInstance(v)) {
            reference.set((T) v);
            return true;
        }
        return false;

    }

    default Number getNumber(String path) {
        return YmlUtils.asNumber(get(path));
    }

    default Number getNumber(String path, Number defaultValue) {
        return YmlUtils.asNumber(get(path, defaultValue));
    }

    default boolean isNumber(String path) {
        try {
            NumberFormat.getInstance().parse(get(path).toString());
            return true;
        } catch (ParseException ignored) {
            return false;
        }
    }

    /**
     * Gets the value of the specified key path starting from the current section, interpreted as a <code>int</code>.
     *
     * @param path The key path to search.
     * @return the value obtained after {@link #get(String)} but interpreted as <code>int</code>, or <code>0</code> if the path does not exist or the value does not represent a valid number.
     */
    default int getInt(@NonNull String path) {
        return getNumber(path).intValue();
    }

    default int getInt(@NonNull String path, int defaultValue) {
        return getNumber(path, defaultValue).intValue();
    }

    default boolean isInt(@NonNull String path) {
        return isLong(path);
    }

    /**
     * Gets the value of the specified key path starting from the current section, interpreted as a <code>long</code>.
     *
     * @param path The key path to search.
     * @return the value obtained after {@link #get(String)} but interpreted as <code>long</code>, or <code>0</code> if the path does not exist or the value does not represent a valid number.
     */
    default long getLong(@NonNull String path) {
        return getNumber(path).longValue();
    }

    default long getLong(@NonNull String path, long defaultValue) {
        return getNumber(path, defaultValue).longValue();
    }

    boolean isLong(@NonNull String path);

    /**
     * Gets the value of the specified key path starting from the current section, interpreted as a <code>float</code>.
     *
     * @param path The key path to search.
     * @return the value obtained after {@link #get(String)} but interpreted as <code>float</code>, or <code>0</code> if the path does not exist or the value does not represent a valid number.
     */
    default float getFloat(@NonNull String path) {
        return getNumber(path).floatValue();
    }

    default float getFloat(@NonNull String path, float defaultValue) {
        return getNumber(path, defaultValue).floatValue();
    }

    boolean isFloat(@NonNull String path);

    /**
     * Gets the value of the specified key path starting from the current section, interpreted as a <code>double</code>.
     *
     * @param path The key path to search.
     * @return the value obtained after {@link #get(String)} but interpreted as <code>int</code>, or <code>0</code> if the path does not exist or the value does not represent a valid number.
     */
    default double getDouble(@NonNull String path) {
        return getNumber(path).doubleValue();
    }

    default double getDouble(@NonNull String path, double defaultValue) {
        return getNumber(path, defaultValue).doubleValue();
    }

    boolean isDouble(@NonNull String path);

    default short getShort(@NonNull String path) {
        return getNumber(path).shortValue();
    }

    default short getShort(@NonNull String path, short defaultValue) {
        return getNumber(path, defaultValue).shortValue();
    }

    boolean isShort(@NonNull String path);

    default short getByte(@NonNull String path) {
        return getNumber(path).byteValue();
    }

    default short getByte(@NonNull String path, byte defaultValue) {
        return getNumber(path, defaultValue).byteValue();
    }

    boolean isByte(@NonNull String path);

    /**
     * Gets the value of the specified key path starting from the current section, interpreted as a <code>boolean</code>.
     *
     * @param path The key path to search.
     * @return <code>true</code> if the path exists and represents "true" textually, <code>false</code> otherwise or the path does not exist.
     */
    default boolean getBoolean(@NonNull String path) {
        return Boolean.parseBoolean(getString(path));
    }

    default boolean getBoolean(@NonNull String path, boolean defaultValue) {
        try {
            return getString(path).equals("true");
        } catch (NullPointerException ignored) {
            return defaultValue;
        }
    }

    /**
     * Gets the value of the specified key path starting from the current section, interpreted as a <code>Object</code> list.
     *
     * @param path The key path to search.
     * @return the value obtained after {@link #get(String)} but interpreted as a <code>Object</code> list, or an immutable empty list if the path does not exist.
     */
    default List<?> getList(@NonNull String path) {
        return getList(path, new MemoryList<>(this, path));
    }

    default List<?> getList(@NonNull String path, List<?> defaultValue) {
        Object o = get(path);
        if(o instanceof List)
            return (List<?>) o;
        return defaultValue;
    }

    /**
     * Gets the value of the specified key path starting from the current section, interpreted as a <code>String</code> list.
     *
     * @param path The key path to search.
     * @return the value obtained after {@link #get(String)} but interpreted as a <code>String</code> list, or an immutable empty list if the path does not exist.
     */
    default List<String> getListString(@NonNull String path) {
        return getList(path).stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    /**
     * Gets the value of the specified key path starting from the current section, interpreted as a <code>Integer</code> list.
     *
     * @param path The key path to search.
     * @return the value obtained after {@link #get(String)} but interpreted as a <code>Integer</code> list, or an immutable empty list if the path does not exist.
     */
    default List<Integer> getListInt(@NonNull String path) {
        return getList(path).stream()
                .map(o->YmlUtils.asNumber(o).intValue())
                .collect(Collectors.toList());
    }

    /**
     * Gets the value of the specified key path starting from the current section, interpreted as a <code>Long</code> list.
     *
     * @param path The key path to search.
     * @return the value obtained after {@link #get(String)} but interpreted as a <code>Long</code> list, or an immutable empty list if the path does not exist.
     */
    default List<Long> getListLong(@NonNull String path) {
        return getList(path).stream()
                .map(o->YmlUtils.asNumber(o).longValue())
                .collect(Collectors.toList());
    }

    /**
     * Gets the value of the specified key path starting from the current section, interpreted as a <code>Float</code> list.
     *
     * @param path The key path to search.
     * @return the value obtained after {@link #get(String)} but interpreted as a <code>Float</code> list, or an immutable empty list if the path does not exist.
     */
    default List<Float> getListFloat(@NonNull String path) {
        return getList(path).stream()
                .map(o->YmlUtils.asNumber(o).floatValue())
                .collect(Collectors.toList());
    }

    /**
     * Gets the value of the specified key path starting from the current section, interpreted as a <code>Double</code> list.
     *
     * @param path The key path to search.
     * @return the value obtained after {@link #get(String)} but interpreted as a <code>Double</code> list, or an immutable empty list if the path does not exist.
     */
    default List<Double> getListDouble(@NonNull String path) {
        return getList(path).stream()
                .map(o->YmlUtils.asNumber(o).doubleValue())
                .collect(Collectors.toList());
    }

    default List<Short> getListShort(@NonNull String path) {
        return getList(path).stream()
                .map(o->YmlUtils.asNumber(o).shortValue())
                .collect(Collectors.toList());
    }

    default List<Boolean> getListBoolean(@NonNull String path) {
        return getList(path).stream()
                .map(o->Boolean.parseBoolean(o.toString()))
                .collect(Collectors.toList());
    }

    Map<String, Object> getMap(String path);

    default Map<String, Object> getMap() {
        return getMap("");
    }

    /**
     * Gets the list of child sections of the current section.
     *
     * @param deep If the sections of the child sections should also be searched and successively.
     * @return the list of child sections of this section.
     */
    List<String> getKeys(boolean deep);

    /**
     * Assigns a value to a specified key path starting from the current section.<br></br>
     * If the path already exists and has a value, it will not be replaced.<br>
     * If the specified path contains sections that do not exist, they will be created automatically.
     *
     * @param path The path to the key that will be assigned a value.
     * @param value The value to set.
     *
     * @return <code>false</code> if the path already exists and has a value, <code>true</code> otherwise.
     */
    boolean add(@NonNull String path, Object value);

    /**
     * Assigns a value to a specified key path starting from the current section.<br></br>
     * If the path already exists and has a value, the new value will be overwritten.<br>
     * If the specified path contains sections that do not exist, they will be created automatically.
     *
     * @param path The path to the key that will be assigned a value.
     * @param value The value to set.
     *
     * @return the previous value of the key path (if it already existed, <code>null</code> otherwise).
     */
    Object set(@NonNull String path, Object value);

    /**
     * Removes a key or section from a specified path starting from the current section.
     *
     * @param path The path to the key or section to delete.
     *
     * @return the value set to this key/section path prior to deletion (If it existed, <code>null</code> otherwise).
     */
    Object remove(@NonNull String path);

    /**
     * Check if the specified path (starting from the current section) is a Section.<br></br>
     * Keys in the YAML format can contain another series of keys and values, which in turn can also contain more keys and values, and so on. These container keys are referred to as Sections.<br>
     * When parsing a YAML object, the sections are interpreted as a map of keys and values, that is, {@link #get(String)} will be an instance of {@link Map}.
     *
     * @param path The path to check.
     * @return <code>true</code> if the path exists and is a section, <code>false</code> otherwise.
     */
    default boolean isSection(@NonNull String path) {
        return getSection(path) != null;
    }

    Section getDefaults();

    Section createDefaults();

    default boolean addDefault(String path, Object value) {
        return createDefaults().add(path, value);
    }

    default void setDefault(String path, Object value) {
        createDefaults().set(path, value);
    }

    /**
     * Gets the current section interpreted as a String.
     *
     * @return a String representation of the current section.
     */
    String toString();

    Section getParent();

    default Yml getRoot() {
        if(this instanceof Yml) return (Yml) this;
        Section parent = this;
        do {
            parent = parent.getParent();
            if (parent instanceof Yml)
                return (Yml) parent;
        } while (parent != null);
        throw new RuntimeException("Unexpected state: root not found");
    }
}
