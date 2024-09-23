package me.altzenck.yml;

import lombok.NonNull;
import me.altzenck.yml.util.YmlUtils;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class MemoryList<T> extends ArrayList<T> {

    private final Section section;
    private final String key;
    boolean check = true;


    MemoryList(@NonNull Section section, String key, int initialCapacity) {
        super(initialCapacity);
        this.section = section;
        YmlUtils.requireNotVoid(key);
        this.key = key;
        if(initialCapacity > 0)
            checkPresence();
    }

    MemoryList(@NotNull Section section, String key) {
        this(section, key, 0);
    }

    @Override
    public boolean add(T t) {
        boolean r = super.add(t);
        if(r)
            checkPresence();
        return r;
    }

    @Override
    public void add(int index, T element) {
        super.add(index, element);
        checkPresence();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean r = super.addAll(c);
        if(r)
            checkPresence();
        return r;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        boolean r = super.addAll(index, c);
        if(r)
            checkPresence();
        return r;
    }

    private void checkPresence() {
        if(check && !section.contains(key, List.class)) {
            section.set(key, this);
            check = false;
        }
    }
}
