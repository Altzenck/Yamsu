package me.altzenck.yaml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

@SuppressWarnings("unchecked")
public abstract class Section {

	public final HashMap<String,Object> yaml = new HashMap<String,Object>();
	private LinkedList<String> cpath = new LinkedList<String>();
	protected static final String SEPARATOR = ".", SEPARATOR_RGX = "\\" + SEPARATOR;
	
	protected Section() {}
	
	
	public Section getSection(@Nonnull String section) {
		if(!setSection(getCurrent(), parsePath(section))) return null;
		return this;
	}
	
	public Section prevSection() {
		return prevSections(1);
	}
	
    public Section prevSections(int amount) {
		while(amount > 0) {
			cpath.removeLast();
			amount--;		
		}
		if(getSection(yaml, cpath) == null) return null;
		return this;
	}
	
	private void checkFormat(String text, String cexc) {
		if(!Pattern.matches("^(\\w|[-_çÇ\\*ñÑ])+$", text)) throw new IllegalArgumentException((cexc == null)? "Invalid format! for String: " + text: String.format(cexc, text));
	}
	
	public Object get(String path) {
		List<String> s = parsePath(path);
		if(s.size() < 2)
			return getCurrent().get(path);
		return getSection(getCurrent(), s.subList(0, s.size()-1)).get(s.get(s.size()-1));
	}
	
	public String getString(String key) {
		Object o = get(key);
		if(o == null) return null;
		return o.toString();
	}
	
	private Number parseNumber(Object o) {
		if(o == null) return -1;
		if(o instanceof Number)
			return (Number) o;
		return -1;
	}
	
	private Number getNumber(String key) {
		return parseNumber(get(key));
	}
	
	public int getInt(String key) {
		return (int) getNumber(key);
	}
	
	public long getLong(String key) {
		return (long) getNumber(key);
	}
	
	public float getFloat(String key) {
		return (float) getNumber(key);
	}
	
	public double getDouble(String key) {
		return (double) getNumber(key);
	}
	
	public boolean getBoolean(String key) {
		return Boolean.parseBoolean(getString(key));
	}
	
	public List<Object> getList(String key) {
		Object o = get(key);
		if(o instanceof List) 
			return (List<Object>) o;
		return new ArrayList<>();
	}
	
	public List<String> getListString(String key) {
		Object o = get(key);
		List<String> sL = new ArrayList<String>();
		if(o instanceof List) {
		  for(Object e: ((List<Object>) o)) 
			sL.add(e.toString());
	    }
		return sL;
	}
	
	public List<Integer> getListInt(String key) {
		Object o = get(key);
		List<Integer> sL = new ArrayList<Integer>();
		if(o instanceof List) {
		  for(Object e: ((List<Object>) o)) 
			sL.add((int)parseNumber(e));
	    }
		return sL;
	}
	
	public List<Long> getListLong(String key) {
		Object o = get(key);
		List<Long> sL = new ArrayList<Long>();
		if(o instanceof List) {
		  for(Object e: ((List<Long>) o)) 
			sL.add((long)parseNumber(e));
	    }
		return sL;
	}
	
	public List<Float> getListFloat(String key) {
		Object o = get(key);
		List<Float> sL = new ArrayList<Float>();
		if(o instanceof List) {
		  for(Object e: ((List<Float>) o)) 
			sL.add((float)parseNumber(e));
	    }
		return sL;
	}
	
	public List<Double> getListDouble(String key) {
		Object o = get(key);
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
			 if(!(deep && (value instanceof Map))) continue;
			 keyDeep(((Map<String,Object>) value), ks, path0, true);
		}
	}
	
	public List<String> getKeys(boolean deep) {
		List<String> ks = new LinkedList<String>();
		Map<String,Object> map = getCurrent();
		keyDeep(map, ks, "", deep);
		return ks;
	}
	
	public void set(@Nonnull String path, Object value) {
		List<String> s = parsePath(path);
		if(s.size() < 2)
			getCurrent().replace(path, value);
		getSection(getCurrent(), s.subList(0, s.size()-1)).replace(s.get(s.size()-1), value);
	}
	
	public boolean isSection(String path) {
		return (get(path) instanceof Map);
	}
	
	private Map<String,Object> getSection(Map<String,Object> map, List<String> s){
		if(map == null) return null;
		if(s.size() == 0) {
			return map;}
		String s0 = s.get(0);
		checkFormat(s0, "Invalid section format! For String: %s");
		try {
		  s = s.subList(1, s.size());
		} catch (ArrayIndexOutOfBoundsException|IllegalArgumentException e) {s = new ArrayList<>();}
		try {
		  return getSection((Map<String,Object>) map.get(s0), s);
		} catch (ClassCastException e) {return null;}
	}
	
	private boolean setSection(Map<String,Object> from, List<String> s) {
		boolean i = from == null;
		Map<String,Object> temp = getSection((i)? yaml: from, s);
		if(temp == null) return false;
		if(i) cpath.clear();
		cpath.addAll(s);
		return true;
	}
	
	protected Map<String,Object> getCurrent() {
		return (cpath.isEmpty())? yaml : getSection(yaml, cpath);
	}
	
	private List<String> parsePath(String path) {
		return new LinkedList<String>(Arrays.asList(path.split(SEPARATOR_RGX)));
	}
	
	@Override
	public final String toString() {
	  return getCurrent().toString();
	}
}
