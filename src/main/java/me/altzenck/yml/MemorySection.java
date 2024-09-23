package me.altzenck.yml;

import java.util.*;
import lombok.NonNull;
import javax.annotation.Nullable;
import lombok.Getter;
import me.altzenck.util.Trifunction;
import me.altzenck.yml.util.YmlUtils;

@SuppressWarnings("unchecked")
public abstract class MemorySection implements Section {

	static final String SECTION_LIST_NAME_RGX = "^[^\\s'_]+_'\\d+'$";

	@Getter
	private final Section parent;
	@Getter
	private final Yml root;
	private Section def;

	protected final Path.Provider pathProvider;
	@Getter
	protected final Path path;

	private final Map<String, MemorySection> loadedSections = new HashMap<>();
	protected final Map<String, Object> map;


	protected MemorySection(@Nullable Map<String, Object> map) {
		this(map, null);
	}

	protected MemorySection(@Nullable Map<String, Object> map, @Nullable String name) {
		this(map, name, null);
	}

	protected MemorySection(@Nullable Map<String, Object> map, @Nullable String name, @Nullable Section parent) {
		this.map = map == null ? new HashMap<>() : map;
		this.pathProvider = Path.Provider.newInstance();
		this.path = (parent == null) ? pathProvider.of(name) : parent.getPath().appendNewCopy(name);
		this.parent = parent;
		this.root = Section.super.getRoot();
		if (parent instanceof DefaultSection || parent == null) return;
		Section parentDefault = parent.getDefaults();
		if (parentDefault != null) def = parentDefault.getDefaults().getSection(name);
	}


	@Override
	public Section getDefaults() {
		return def;
	}

	@Override
	public Section createDefaults() {
		if (def != null) return def;
		Section def1 = null, parent = this;
		String[] keys = new String[0];
		while (def1 == null) {
			keys = Arrays.copyOf(keys, keys.length + 1);
			keys[keys.length - 1] = parent.getPath().getLastKey();
			def1 = parent.getDefaults();
			parent = parent.getParent();
		}
		Arrays.sort(keys, Collections.reverseOrder());
		String path = String.join(pathProvider.separator(), keys);
		return def = Objects.requireNonNull(def1.getSection(path));
	}

	@Override
	public Section getSection(@NonNull String section) {
		return loadSection(pathProvider.of(section), this);
	}

	@Override
	public List<Section> getListSection(@NonNull String path) {
		Object listObj = get(path);
		if (!(listObj instanceof List)) return new MemoryList<>(this, path);
		Iterator<?> it = ((List<?>) listObj).iterator();
		List<Section> newList = new ArrayList<>();
		for (int i = 0; it.hasNext(); i++) {
			Object o = it.next();
			if (o instanceof Map)
				newList.add(getSection(path + pathProvider.separator() + i));
		}
		return newList;
	}

	protected Trifunction<Map<String, Object>, String, Section, ? extends MemorySection> creation() {
		return (m, s, p)-> new MemorySection(m, s, p) {};
	}

	private Section loadSection(Path path, MemorySection parent) {

		// Gets the Path tree, If its length is zero, it is assumed that the current parent is the value to return (target).
		String[] tree = path.tree;
		if(tree.length == 0)
			return parent;

		// Gets the first declared section name, which will attempt to load.
		String firstSection = tree[0];

		// Sets the loaded section field with a first operation that attempts to obtain the section reference.
		MemorySection loaded = loadedSections.get(path.getName());

		// If section was not previously loaded (no reference found), then try to Load from Map.
		if(loaded == null) {
			// The object value to obtain, must be a Map
			Object value = null;

			// Check if this section key has a list format, then try to get the section from a List.
			Object[] sln = parseSectionListName(firstSection);
			if(sln.length == 2) {
				// Get the parsed section list elements:
				// listKey is the key of List to obtain.
				// index is the order where section is located.
				String listKey = sln[0].toString();
				int index = (int) sln[1];

				// Gets the value and checks if the object is a List instance.
				Object listObj = get(listKey);
				if(listObj instanceof List<?>) {
					Iterator<?> it = ((List<?>)listObj).iterator();

					// Iterates over List elements, breaks when there are no more elements to iterate or when the section index is located.
					for (int i = 0; it.hasNext(); i++) {
						Object e = it.next();
						// if the iteration index equals section index, the value that is presumably a section.
						if(i == index) {
							value = e;
							break;
						}
					}
				}
			}

			// If the map value is null, sets any value that matches the key.
			if(value == null) value = get(firstSection);

			// If there are no values for the assigned key (value remains null), then the operation ends and return null.
			if(value == null) return null;
			try {
				// Try to cast the map value creating a new section reference for this value.
				loaded = creation().apply((Map<String, Object>) value, firstSection, parent);

			// If value isn't a Map, then the operation ends and return null.
			} catch (ClassCastException e) {
				return null;
			}
		}

		// If all operations are successful, then add the section in the parent set reference for fast access before.
		parent.loadedSections.put(firstSection, loaded);

		// return a new operation cutting the path to remove the previous section name and move it to the next section name,
		// and setting the previously loaded section as parent. This load cycle will attempt to load all sections specified
		// in the path, until reaching the last section (which is the target).
		return loadSection(pathProvider.of(Arrays.copyOfRange(tree, Math.min(1, tree.length-1), tree.length)), loaded);
	}

	@Override
	public Object get(@NonNull String pathName) {
		Object o = pathProvider.of(pathName).toLast(this, (section, s)->section.getMap().get(s));
		if(o == null) {
			try {
				return def.get(pathName);
			} catch (NullPointerException ignored) {
			}
		}
		return o;
	}

	private Number getNumber(String path, Class<? extends Number> clazz) {
		return YmlUtils.asNumber(get(path), clazz);
	}

	@Override
	public boolean isLong(@NonNull String path) {
		return getNumber(path, Long.class) instanceof Long;
	}

	@Override
	public boolean isFloat(@NonNull String path) {
		return getNumber(path, Float.class) instanceof Float;
	}

	@Override
	public boolean isDouble(@NonNull String path) {
		return getNumber(path, Double.class) instanceof Double;
	}

    @Override
	public boolean isShort(@NonNull String path) {
		return getNumber(path, Short.class) instanceof Short;
	}

	@Override
	public boolean isByte(@NonNull String path) {
		return getNumber(path, Byte.class) instanceof Byte;
	}

	@Override
	public Map<String, Object> getMap(String path) {
	    if(path.isEmpty()) return map;
		Object o = get(path);
		if(o instanceof Map)
			return (Map<String, Object>) o;
		return null;
	}

	@Override
	public List<String> getKeys(boolean deep) {
		return Collections.unmodifiableList(getKeys(new ArrayList<>(), deep, this, new StringBuilder()));
	}

	private List<String> getKeys(List<String> storage, boolean deep, MemorySection section, StringBuilder sb) {
		for (String k : section.map.keySet()) {
			MemorySection v = (MemorySection) section.getSection(k);
			if(v == null) continue;
			sb.append((sb.toString().isEmpty()? "" : ".") + v.getPath().getLastKey());
			storage.add(sb.toString());
			if(deep)
				return getKeys(storage, true, v, sb);
		}
		return storage;
	}

	@Override
	public boolean add(@NonNull String path, Object value) {
		return set(path, value, false) == null;
	}

	@Override
	public Object set(@NonNull String path, Object value) {
		return set(path, value, true);
	}

	private Object set(@NonNull String path, Object value, boolean replace) {
		return pathProvider.of(path).toLast(this, ((section, s) -> {
			Object o = section.get(s);
			if(o == null || replace)
				section.getMap().put(s, value);
			return o;
		}));
	}

	@Override
	public Object remove(@NonNull String path) {
		return pathProvider.of(path).toLast(this, ((section, s) -> {
			Object o = section.get(s);
			section.getMap().remove(s);
			return o;
		}));
	}

	@Override
	public Section createSection(String path) {
		return pathProvider.of(path).forEach(this,
			(section, k) -> {

			    // cast to a MemorySection instance
			    MemorySection ms = (MemorySection) section;
				Runnable creation = () -> {
					if(ms.getSection(k) == null)
						ms.set(k, new HashMap<>());
				};

				// check if key has a List format
				Object[] sln = parseSectionListName(k);
				if(sln.length == 2) {
					// Divide the formated key in two elements:
					// listKey (key to access to the List)
					// and index (order where section will be located)
					String listKey = sln[0].toString();
					int index = (int) sln[1];

					try {
						// Try to cast the object obtained to a List, otherwise execute catch lines (run creation).
						List<Object> list = (List<Object>) get(listKey);

						// If the value is null, then create a new List.
						if(list == null)
							ms.set(listKey, list = new ArrayList<>());

						// Iterates over list elements to create the section, creating empty sections if is necessary.
						Iterator<Object> it = list.iterator();
						for(int i = 0; i <= index; i++) {
							if(!it.hasNext())
								list.add(new HashMap<>());
							it.next();
						}

					// if object isn't a List, run section creation.
					} catch (ClassCastException e) {
						creation.run();
					}
				} else {
					// run section creation directly if key hasn't List format.
					creation.run();
				}

				// This operation always must be successful.
				return true;
			}
		);
	}

	private static Object[] parseSectionListName(String sectionName) {
		if(sectionName.matches(SECTION_LIST_NAME_RGX)) {
			String[] ss = sectionName.replaceAll("'$", "").split("_'");
		    return new Object[]{ss[0], Integer.parseInt(ss[1])};
		}
		return new Object[0];
	}

	@Override
	public String toString() {
	  return getMap().toString();
	}
}