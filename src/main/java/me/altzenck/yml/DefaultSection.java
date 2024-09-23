package me.altzenck.yml;

import me.altzenck.util.Trifunction;
import org.jetbrains.annotations.Nullable;
import java.util.Map;

public class DefaultSection extends MemorySection {

    protected DefaultSection(@Nullable Map<String, Object> map) {
        super(map);
    }

    protected DefaultSection(@Nullable Map<String, Object> map, @Nullable String name) {
        super(map, name);
    }

    protected DefaultSection(@Nullable Map<String, Object> map, @Nullable String name, @Nullable Section parent) {
        super(map, name, parent);
    }

    @Override
    public Section getDefaults() {
        return this;
    }

    @Override
    public Section createDefaults() {
        return getDefaults();
    }

    @Override
    public boolean addDefault(String path, Object value) {
        return add(path, value);
    }

    @Override
    public void setDefault(String path, Object value) {
        set(path, value);
    }

    @Override
    protected Trifunction<Map<String, Object>, String, Section, ? extends MemorySection> creation() {
        return DefaultSection::new;
    }
}
