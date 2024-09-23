package me.altzenck.util;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import java.util.AbstractMap;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
public class ArrayMap<K, V> extends AbstractMap<K, V> {

    @Getter
    private final boolean allowNullKeys,
            allowNullValues;
    private final Set<Entry<K, V>> entrySet = new ArraySet<>();

    public ArrayMap() {
        this(true);
    }

    public ArrayMap(boolean allowNullKeys) {
        this(allowNullKeys, true);
    }

    @Override
    public V put(K key, V value) {
        V oldValue = get(key);
        if(allowNullKeys && key == null && containsKey(null))
            return oldValue;
        entrySet.add(new MapEntry(key, value));
        return oldValue;
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return entrySet;
    }

    private class MapEntry extends AbstractMap.SimpleEntry<K, V>{

        public MapEntry(K key, V value) {
            super((allowNullKeys? key : Objects.requireNonNull(key)), value);
        }

        @Override
        public V setValue(V value) {
            return super.setValue(allowNullValues? value : Objects.requireNonNull(value));
        }
    }
}

