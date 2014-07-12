package com.netx.config.R1;
import java.util.Map;
import java.util.HashMap;


class TypeDef {

	public final Class<?> id;
	public final SimpleTypeLoader<?> simpleTL;
	public final ComplexTypeLoader<?> complexTL;
	public final Map<String,PropertyDef> subProps;
	public final Map<String,Object> defaultValue;
	
	public TypeDef(Class<?> id, SimpleTypeLoader<?> stl) {
		this.id = id;
		this.simpleTL = stl;
		this.complexTL = null;
		this.subProps = null;
		this.defaultValue = null;
	}

	public TypeDef(Class<?> id, ComplexTypeLoader<?> ctl, Map<String,PropertyDef> subProps) {
		this.id = id;
		this.simpleTL = null;
		this.complexTL = ctl;
		this.subProps = subProps;
		// Load the default value:
		Map<String,Object> values = new HashMap<String,Object>();
		for(PropertyDef def : subProps.values()) {
			Object value = def.defaultValue;
			if(value != null) {
				values.put(def.name, value);
			}
		}
		if(values.isEmpty()) {
			defaultValue = null;
		}
		else {
			defaultValue = values;
		}
	}
}
