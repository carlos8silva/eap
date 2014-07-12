package com.netx.config.R1;
import java.util.Map;
import java.util.HashMap;
import com.netx.generics.R1.util.Expr;
import com.netx.basic.R1.eh.IntegrityException;


public class PropertyDef {

	// TYPE:
	public static final Object NULL = new Object();
	
	// INSTANCE:
	public final String name;
	public final boolean isMap;
	public final TypeDef type;
	public final boolean mandatory;
	public final boolean readOnly;
	public final Object defaultValue;
	
	public PropertyDef(String name, boolean isMap, TypeDef type, boolean mandatory, boolean readOnly, Object defaultValue) {
		this.name = name;
		this.isMap = isMap;
		this.type = type;
		this.mandatory = mandatory;
		this.readOnly = readOnly;
		this.defaultValue = defaultValue;
	}
	
	@SuppressWarnings("unchecked")
	public Object loadDefault(Object source) throws TypeLoadException {
		if(type.simpleTL != null) {
			if(source == null) {
				return null;
			}
			String s = (String)source;
			if(type.id == String.class) {
				return Expr.evaluate(s);
			}
			else {
				return type.simpleTL.parse(s, this);
			}
		}
		else if(type.complexTL != null) {
			if(source == null) {
				return null;
			}
			else {
				Map<String,Object> loaded = new HashMap<String,Object>();
				Map<String,Object> srcValues = (Map<String,Object>)source;
				for(PropertyDef def : type.subProps.values()) {
					loaded.put(def.name, def.loadDefault(srcValues.get(def.name)));
				}
				// Now we need to load the complex value:
				return type.complexTL.onLoad(loaded, this);
			}
		}
		else {
			throw new IntegrityException();
		}
	}
}
