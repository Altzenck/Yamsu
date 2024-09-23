package me.altzenck.util;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class ArraySet<T> extends ArrayList<T> implements Set<T> {

    public ArraySet(int initialCapacity) {
        super(initialCapacity);
    }

    public ArraySet() {
    }

    public ArraySet(@NotNull Collection<? extends T> c) {
        super(c);
    }
}
