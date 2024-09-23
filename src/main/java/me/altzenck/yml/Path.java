package me.altzenck.yml;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import me.altzenck.Void;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.function.*;

public class Path {

    public final Provider provider;
    private final String path;
    final String[] tree;
    private Path parent;

    private Path(Provider provider, @NonNull String path) {
        Preconditions.checkArgument(!path.isBlank(), "path cant be black or empty");
        this.provider = provider;
        this.path = path;
        tree = path.split("[" + provider.separator + "]");
    }

    private Path(Provider provider, @NonNull String[] sectionsInOrder) {
        this.provider = provider;
        tree = Arrays.copyOf(sectionsInOrder, sectionsInOrder.length);
        path = String.join(provider.separator(), tree);
    }

    public static class Provider {

        private static final char DEFAULT_SEPARATOR = '.';
        private String separator;
        private boolean inmutable;

        private Provider() {
        }

        public Provider setSeparator(char separator) {
            if(inmutable) throw new UnsupportedOperationException();
            this.separator = Character.toString(separator);
            return this;
        }

        public String separator() {
            return separator;
        }

        public static final Provider DEFAULT = newInstance().inmutable();

        public static Provider newInstance() {
            return new Provider().setSeparator(DEFAULT_SEPARATOR);
        }

        public Provider inmutable() {
            inmutable = true;
            return this;
        }

        public Path of(String... tree) {
            return Path.of(this, tree);
        }
    }

    private static Path of(Provider provider, @NotNull String... tree) {
        switch (tree.length) {
            case 0:
                return new Path(provider, "");
            case 1:
                return new Path(provider, tree[0]);
            default:
                return new Path(provider, tree);
        }
    }

    @Override
    public String toString() {
        return path;
    }

    public String getName() {
        return toString();
    }

    Path appendNewCopy(String path) {
        return appendNewCopy(new Path(provider, path));
    }

    Path appendNewCopy(Path path) {
        int pl = getTreeSize(),
            ol = path.getTreeSize();
        String[] ns = new String[ol + pl];
        System.arraycopy(this.tree, 0, ns, 0, pl);
        System.arraycopy(path.tree, 0, ns, pl+1, ol);
        return new Path(provider, ns);
    }

    public Path getParent() {
        if(getTreeSize() == 1) return this;
        if(parent != null) return parent;
        return parent = new Path(provider, Arrays.copyOf(tree, getTreeSize()-1));
    }

    public String getLastKey() {
        return tree[getTreeSize()-1];
    }

    public Object toLast(Section section, BiFunction<Section, String, Object> action) {
        for(int i = 0; i < getTreeSize(); i++)  {
            if(section == null) break;
            if(i == getTreeSize()-1) return action.apply(section, getLastKey());
            section = section.getSection(tree[i]);
        }
        return Void.VALUE;
    }

    public Section forEach(Section section, BiPredicate<Section, String> action) {
        for(String s: tree) {
            if(section == null) return null;
            if(!action.test(section, s)) break;
            section = section.getSection(s);
        }
        return section;
    }

    public int getTreeSize() {
        return tree.length;
    }

    @Override
    public boolean equals(Object obj) {
        return path.equals(obj instanceof Path? ((Path)obj).path : obj.toString());
    }
}
