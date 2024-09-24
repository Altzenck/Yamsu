package me.altzenck.yml;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Sections {

    /**
     * Gets an unmodifiable copy of the specified Section.<br>
     * Unmodifiable Sections cannot reassign values to the current instance or default values, cannot get Map references using the {@link MemorySection#get(String)} method, or get the {@link Yml} root.
     * Trying to perform these actions will always throw {@link UnsupportedOperationException}.
     *
     * @apiNote This method is very experimental. Unmodifiable sections have slower operations because all their values ​​must be verified as immutable. This can lead to instability and performance issues.
     * @param section a Section.
     * @return A copy of the instance but unmodifiable.
     */
    public static Section unmodifiableSection(@Nonnull Section section) {
        return new UnmodifiableSection(section);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class UnmodifiableSection implements Section {

        private final Section original;
        private Section unModDefault;

        @Override
        public Section getSection(@NonNull String section) {
            return original.getSection(section);
        }

        @Override
        public List<Section> getListSection(@NonNull String path) {
            return original.getListSection(path).stream()
                    .map(Sections::unmodifiableSection)
                    .collect(Collectors.toUnmodifiableList());
        }

        /*
           Leaked Mutable Values: Unsupported Section Set and More Objects.
         */
        @Override
        public Object get(@NonNull String path) {
            Object o = original.get(path);
            if(o instanceof List) return Collections.unmodifiableList((List<?>) o);
            if(o instanceof Set) return Collections.unmodifiableSet((Set<?>) o);
            return o;
        }

        @Override
        public Path getPath() {
            return original.getPath();
        }

        @Override
        public Section createSection(String path) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isLong(@NonNull String path) {
            return original.isLong(path);
        }

        @Override
        public boolean isFloat(@NonNull String path) {
            return original.isFloat(path);
        }

        @Override
        public boolean isDouble(@NonNull String path) {
            return original.isDouble(path);
        }

        @Override
        public boolean isShort(@NonNull String path) {
            return original.isShort(path);
        }

        @Override
        public boolean isByte(@NonNull String path) {
            return original.isByte(path);
        }

        /*
          Leaked mutable data when getting a Map
         */
        @Override
        public Map<String, Object> getMap(String path) {
            return Collections.unmodifiableMap(original.getMap(path));
        }

        @Override
        public List<String> getKeys(boolean deep) {
            return original.getKeys(deep);
        }

        @Override
        public boolean add(@NonNull String path, Object value) {
            return false;
        }

        @Override
        public Object set(@NonNull String path, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object remove(@NonNull String path) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Section getDefaults() {
            return unModDefault == null? (unModDefault = new UnmodifiableSection(original.getDefaults()) {
                @Override
                public Section getDefaults() {
                    return this;
                }
            }) : unModDefault;
        }

        @Override
        public Section createDefaults() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Section getParent() {
            return (original.getParent() == null)? null : unmodifiableSection(original.getParent());
        }

        @Override
        public Yml getRoot() {
            throw new UnsupportedOperationException();
        }
    }
}
