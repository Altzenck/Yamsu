package me.altzenck.yml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import me.altzenck.utils.YmlUtils;

@SuppressWarnings("unchecked")
public abstract class Section {

	/**
	 * The yaml object parsed as a Map. It can be manually modified and later exported.
	 */
	public final Map<String,Object> yaml = new HashMap<String,Object>();
	
	private Section def;
	private boolean isDefault;
	
	protected final ArrayList<String> cpath = new ArrayList<String>();
	private Map<String,Object> current = null;
	
	protected static final String SEPARATOR = ".", SEPARATOR_RGX = "\\" + SEPARATOR;
	
	protected Section() {
	}
	
	/**
	 * Sets the parent section of a section instance as the default value source for the parent section of the current instance. If the specified instance is the current instance, a clone will be created and established.
	 * @param def The section instance from which its parent section will be set.
	 */
	public void setDefaults(@Nonnull Section def) {
		if(def == this) {
			this.def = new Section() {};
			this.def.yaml.putAll(def.yaml);
		}
		if(isDefault) return;
		this.def = def;
		this.def.isDefault = true;
	}
	
	/**
	 * Gets the default section for the parent section of this instance.
	 * 
	 * @return an immutable instance of the parent section that represents the default values for the current section.
	 * 
	 */
    public Section getDefaults() {
	    return def;
	}
	
    /**
     * Assigns or adds a default section value from the key path starting from the current section.
     * 
     * @param path The path to the default section key starting from the current section.
     * @param replace If even such a path exists in the current section, its value must be replaced.
     */
	public void addDefaults(@Nonnull String path, boolean replace) {
		Object dvalue = (isDefault)? null: getDefault(path);
		Object cvalue = get(path);
		if((cvalue == null || replace) && dvalue != null)
		  set(path, dvalue);
	}
    
	/**
	 * Compares all paths in the current section and replaces/adds default values where necessary.
	 * 
	 * @param replace Whether to replace path keys that already exist.
	 */
	public void addAllDefaults(boolean replace) {
		aADSetter(getCurrent(), def.getSection(def.yaml, cpath), replace);
	}
	
	public void aADSetter(Map<String, Object> m, Map<String, Object> d, boolean replace) {
		for(String key: d.keySet()) {
			Object dvalue = d.get(key);
			boolean c = m.containsKey(key), ins = dvalue instanceof Map;
			if((c && replace && !ins) || !c)
			  m.put(key, dvalue);
			try {
			  aADSetter((Map<String, Object>) m.get(key), (Map<String, Object>) dvalue, replace);
			} catch (ClassCastException e) {}
		}
	}
	
	/**
	 * Gets a section of the parsed yaml object (A "section" is understood to be any key that has a {@link Map} instance as its value).
	 * 
	 * @return the current instance but with the section assigned, or <code>null</code> if the specified path does not exist or is not a section.
	 */
	public Section getSection(@Nonnull String section) {
		if(!setSection(getCurrent(), parsePath(section))) return null;
		return this;
	}
	
	/**
	 * Go back to the previous section from the current section.
	 * 
	 * @return the current instance with the new section set, or the current instance itself if the current section is the parent section.
	 */
	public Section prevSection() {
		return prevSections(1);
	}
	
	/**
	 * Go back to the parent section
	 * 
	 * @return the current instance with the new section set, or the current instance itself if the current section is the parent section.
	 */
	public Section prevSections() {
		return prevSections(0);
	}
	
	/**
	 * Go back a certain number of sections. Specifying 0 for amount will backtrack to the parent section.
	 * 
	 * @return the current instance with the new section set, or the current instance itself if the current section is the parent section, or <code>null</code> if a negative quantity is specified.
	 */
    public Section prevSections(int amount) {
    	ArrayList<String> temp = new ArrayList<String>(cpath);
    	if(amount < 0) return null;
    	if(temp.isEmpty()) return this;
    	if(amount == 0)
    		temp.clear();
		while(amount > 0) {
			try {
			  YmlUtils.removeLast(temp);
			} catch(IndexOutOfBoundsException e) {break;}
			amount--;		
		}
		Map<String, Object> sec;
		if((sec = getSection(yaml, temp)) == null) return null;
		cpath.clear();
		cpath.addAll(temp);
		current = sec;
		return this;
	}
	
	private void checkFormat(String text, String cexc) {
		if(!Pattern.matches("^(\\w|[-_çÇ\\*ñÑ])+$", text)) throw new IllegalArgumentException((cexc == null)? "Invalid format! for String: " + text: String.format(cexc, text));
	}
	
	/**
	 * Gets the value of the specified key path starting from the current section.
	 * 
	 * @param path The key path to search.
	 * @return the value assigned to this key path, or the default value specified by the default section if the path does not exist in the current section, or <code>null</code> if it does not exist in either.
	 */
	public Object get(@Nonnull String path) {
		StringBuilder sb = new StringBuilder();
		Map<String,Object> sec = secureKeyPathHandler(path, sb, false);
		String key = sb.toString();
		Object value = (sec == null)? null: sec.get(key);
		if(value != null || isDefault)
		  return value;
		return getDefault(path);
	}
	
	private Object getDefault(@Nonnull String path) {
		return def.get(YmlUtils.stringJoinFromObject(SEPARATOR, cpath, path));
	}
	
	/**
	 * Gets the value of the specified key path starting from the current section, interpreted as a <code>String</code>.
	 * 
	 * @param path The key path to search.
	 * @return the value obtained after {@link #get(String)} but interpreted as <code>String</code>, or null if the value is an invalid object (<code>null</code> or {@link Map},{@link List} instance).
	 */
	public String getString(String path) {
		Object o = get(path);
		if(o == null || o instanceof List || o instanceof Map) return null;
		return o.toString();
	}
	
	private Number parseNumber(Object o) {
		if(o == null) return -1;
		if(o instanceof Number)
			return (Number) o;
		return -1;
	}
	
	private Number getNumber(String path) {
		return parseNumber(get(path));
	}
	
	/**
	 * Gets the value of the specified key path starting from the current section, interpreted as a <code>int</code>.
	 * 
	 * @param path The key path to search.
	 * @return the value obtained after {@link #get(String)} but interpreted as <code>int</code>, or <code>-1</code> if the path does not exist or the value does not represent a valid number.
	 */
	public int getInt(String path) {
		return (int) getNumber(path);
	}
	
	/**
	 * Gets the value of the specified key path starting from the current section, interpreted as a <code>long</code>.
	 * 
	 * @param path The key path to search.
	 * @return the value obtained after {@link #get(String)} but interpreted as <code>long</code>, or <code>-1</code> if the path does not exist or the value does not represent a valid number.
	 */
	public long getLong(String path) {
		return (long) getNumber(path);
	}
	
	/**
	 * Gets the value of the specified key path starting from the current section, interpreted as a <code>float</code>.
	 * 
	 * @param path The key path to search.
	 * @return the value obtained after {@link #get(String)} but interpreted as <code>float</code>, or <code>-1</code> if the path does not exist or the value does not represent a valid number.
	 */
	public float getFloat(String path) {
		return (float) getNumber(path);
	}
	
	/**
	 * Gets the value of the specified key path starting from the current section, interpreted as a <code>double</code>.
	 * 
	 * @param path The key path to search.
	 * @return the value obtained after {@link #get(String)} but interpreted as <code>int</code>, or <code>-1</code> if the path does not exist or the value does not represent a valid number.
	 */
	public double getDouble(String path) {
		return (double) getNumber(path);
	}
	
	/**
	 * Gets the value of the specified key path starting from the current section, interpreted as a <code>boolean</code>.
	 * 
	 * @param path The key path to search.
	 * @return <code>true</code> if the path exists and represents "true" textually, <code>false</code> otherwise or the path does not exist.
	 */
	public boolean getBoolean(String path) {
		return Boolean.parseBoolean(getString(path));
	}
	
	/**
	 * Gets the value of the specified key path starting from the current section, interpreted as a <code>Object</code> list.
	 * 
	 * @param path The key path to search.
	 * @return the value obtained after {@link #get(String)} but interpreted as a <code>Object</code> list, or a empty list if the path does not exist.
	 */
	public List<Object> getList(String path) {
		Object o = get(path);
		if(o instanceof List) 
			return (List<Object>) o;
		return new ArrayList<>();
	}
	
	/**
	 * Gets the value of the specified key path starting from the current section, interpreted as a <code>String</code> list.
	 * 
	 * @param path The key path to search.
	 * @return the value obtained after {@link #get(String)} but interpreted as a <code>String</code> list, or a empty list if the path does not exist.
	 */
	public List<String> getListString(String path) {
		Object o = get(path);
		List<String> sL = new ArrayList<String>();
		if(o instanceof List) {
		  for(Object e: ((List<Object>) o)) 
			sL.add(e.toString());
	    }
		return sL;
	}
	
	/**
	 * Gets the value of the specified key path starting from the current section, interpreted as a <code>Integer</code> list.
	 * 
	 * @param path The key path to search.
	 * @return the value obtained after {@link #get(String)} but interpreted as a <code>Integer</code> list, or a empty list if the path does not exist.
	 */
	public List<Integer> getListInt(String path) {
		Object o = get(path);
		List<Integer> sL = new ArrayList<Integer>();
		if(o instanceof List) {
		  for(Object e: ((List<Object>) o)) 
			sL.add((int)parseNumber(e));
	    }
		return sL;
	}
	
	/**
	 * Gets the value of the specified key path starting from the current section, interpreted as a <code>Long</code> list.
	 * 
	 * @param path The key path to search.
	 * @return the value obtained after {@link #get(String)} but interpreted as a <code>Long</code> list, or a empty list if the path does not exist.
	 */
	public List<Long> getListLong(String path) {
		Object o = get(path);
		List<Long> sL = new ArrayList<Long>();
		if(o instanceof List) {
		  for(Object e: ((List<Long>) o)) 
			sL.add((long)parseNumber(e));
	    }
		return sL;
	}
	
	/**
	 * Gets the value of the specified key path starting from the current section, interpreted as a <code>Float</code> list.
	 * 
	 * @param path The key path to search.
	 * @return the value obtained after {@link #get(String)} but interpreted as a <code>Float</code> list, or a empty list if the path does not exist.
	 */
	public List<Float> getListFloat(String path) {
		Object o = get(path);
		List<Float> sL = new ArrayList<Float>();
		if(o instanceof List) {
		  for(Object e: ((List<Float>) o)) 
			sL.add((float)parseNumber(e));
	    }
		return sL;
	}
	
	/**
	 * Gets the value of the specified key path starting from the current section, interpreted as a <code>Double</code> list.
	 * 
	 * @param path The key path to search.
	 * @return the value obtained after {@link #get(String)} but interpreted as a <code>Double</code> list, or a empty list if the path does not exist.
	 */
	public List<Double> getListDouble(String path) {
		Object o = get(path);
		List<Double> sL = new ArrayList<Double>();
		if(o instanceof List) {
		  for(Object e: ((List<Double>) o)) 
			sL.add((double)parseNumber(e));
	    }
		return sL;
	}
	
	private void keyDeep(Map<String, Object> map, List<String> ks, String path, boolean deep) {
		for(String key: map.keySet()){
			 String path0 = ((path.isEmpty())? "" : path + ".") + key;
			 ks.add(path0);
			 Object value = map.get(key);
			 if(!((value instanceof Map) && deep)) continue;
			 keyDeep(((Map<String,Object>) value), ks, path0, true);
		}
	}
	
	/**
	 * Gets the list of child sections of the current section.
	 * 
	 * @param deep If the sections of the child sections should also be searched and successively.
	 * @return the list of child sections of this section.
	 */
	public List<String> getKeys(boolean deep) {
		List<String> ks = new ArrayList<String>();
		Map<String,Object> current = getCurrent();
		keyDeep(current, ks, "", deep);
		return ks;
	}
	
	
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
	public boolean add(@Nonnull String path, Object value) {
		return set(path, value, false) == null;
	}
	
	/**
	 * Assigns a value to a specified key path starting from the current section.<br></br>
	 * If the path already exists and has a value, the new value will be overwritten.<br>
	 * If the specified path contains sections that do not exist, they will be created automatically.
	 * 
	 * @param path The path to the key that will be assigned a value.
	 * @param value The value to set.
	 * 
	 * @param the previous value of the key path (if it already existed, <code>null</code> otherwise).
	 */
	public Object set(@Nonnull String path, Object value) {
		return set(path, value, true);
	}
	
	private Object set(@Nonnull String path, Object value, boolean replace) {
		StringBuilder sb = new StringBuilder();
		Map<String,Object> sec = secureKeyPathHandler(path, sb, true);
		String key = sb.toString();
		Object old;
		if(!replace && (old = sec.get(key)) != null) return old;
		return sec.put(key, value);
	}
	
	/**
	 * Removes a key or section from a specified path starting from the current section.
	 * 
	 * @param path The path to the key or section to delete.
	 * 
	 * @return the value set to this key/section path prior to deletion (If it existed, <code>null</code> otherwise).
	 */
	public Object remove(@Nonnull String path) {
		StringBuilder sb = new StringBuilder();
		Map<String,Object> sec = secureKeyPathHandler(path, sb, false);
		String key = sb.toString();
		return (sec == null)? null: sec.remove(key);
	}
	
	/**
	 * Check if the specified path (starting from the current section) is a Section.<br></br>
     * Keys in the YAML format can contain another series of keys and values, which in turn can also contain more keys and values, and so on. These container keys are referred to as Sections.<br>
     * When parsing a YAML object, the sections are interpreted as a map of keys and values, that is, {@link #get(SomeSection)} will be an instance of {@link Map}.
	 *
	 * @param path The path to check.
	 * @return <code>true</code> if the path exists and is a section, <code>false</code> otherwise.
	 */
	public boolean isSection(@Nonnull String path) {
		return (get(path) instanceof Map);
	}
	
	
	@SuppressWarnings("unused")
	private Map<String,Object> getSection(List<String> s){
		return getSection(getCurrent(), s);
	}
	
    private Map<String,Object> getSection(Map<String,Object> map, List<String> s){
		return getSection(map, s, true);
	}
	
	private Map<String,Object> getSection(Map<String,Object> map, List<String> s, boolean p){
		if(p) {s = new ArrayList<String>(s);}
		if(map == null) return null;
		if(s.size() == 0) {
			return map;
		}
		String key = s.remove(0);
		checkFormat(key, "Invalid section format! For String: %s");
		try {
		  return getSection((Map<String,Object>) map.get(key), s, false);
		} catch (ClassCastException e) {return null;}
	}
	
	/**
	 * Creates an empty section from the specified path starting from the current section.<br></br>
	 * If there are key paths in conflict with the specified path, they will be overwritten.
	 * 
	 * @param path The path to the section to create.
	 */
	public void createSection(String path) {
		if(isDefault) return;
		createSection(parsePath(path), true);
	}
	
	private Map<String, Object> createSection(List<String> s, boolean replace){
		Map<String, Object> temp = getCurrent();
		for(int i = 0; i < s.size(); i++) {
		  String key = s.get(i);
		  Object value = temp.get(key);
		  if(value != null && value instanceof Map) {
			  temp = (Map<String, Object>) value;
			  continue;
		  }
		  if(value != null && !replace) return null;
		  temp.put(key, new HashMap<String, Object>());
		  temp = (Map<String, Object>) temp.get(key);
		}
		return temp;
	}
	
	private boolean setSection(Map<String,Object> from, List<String> s) {
		boolean i = from == null;
		Map<String,Object> sec = getSection((i)? yaml: from, s);
		if(sec == null) return false;
		if(i) cpath.clear();
		current = sec;
		cpath.addAll(s);
		return true;
	}
	
	@Nonnull
	private Map<String,Object> getCurrent() {
		return (cpath.isEmpty())? yaml : current;
	}
	
	/**
	 * Gets the path name to the current section of this instance.
	 * 
	 * @return the path name of the current section.
	 */
	public String getCurrentPath() {
	  return String.join(SEPARATOR, cpath);
	}
	
	private ArrayList<String> parsePath(String path) {
		return new ArrayList<String>(Arrays.asList(path.split(SEPARATOR_RGX)));
	}
	
	private Map<String, Object> secureKeyPathHandler(String path, StringBuilder sb, boolean deepRep) {
		List<String> s = parsePath(path);
		String key = YmlUtils.removeLast(s);
		if(s.size() == 0) {
			sb.replace(0, sb.length(), path);
			return getCurrent();
		}
		sb.replace(0, sb.length(), key);
		return createSection(s, deepRep);
	}
	
	/**
	 * Gets the current section interpreted as a String.
	 * 
	 * @return a String representation of the current section.
	 */
	@Override
	public final String toString() {
	  return getCurrent().toString();
	}
}
