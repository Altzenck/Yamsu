package me.altzenck.yml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class YamlBase extends Section {
	
	protected Section def;

	protected YamlBase(Map<String, Object> yaml) {
	    super(yaml, new ArrayList<String>(), null);
	    def = new Section(null, null, null, true) {};
	}
	
	public void setDefaults() {
		setDefaults(this);
	}
	
	/**
	 * Sets the parent section of a section instance as the default value source for the parent section of the current instance.
	 * <br></br>
	 * If the specified instance is the current instance, a clone will be created and established.
	 * 
	 * @param def The section instance from which its parent section will be set.
	 */
	public void setDefaults(@Nonnull Section def) {
		if(def != this) {
			this.def = def;
			return;
		}
        this.def = new Section(null, null, null, true) {};
		this.def.current.putAll(def.current);
	}
	
	/**
	 * Gets the default section for the parent section of this instance.
	 * 
	 * @return an immutable instance of the parent section that represents the default values for the current section.
	 */ 
	public Section getDefaults() {
	    return def;
	}
	
	

}
