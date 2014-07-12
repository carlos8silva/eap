package com.netx.config.R1;
import java.util.Map;
import java.util.HashMap;


public class ContextDef {

	public final String name;
	public final Map<String,PropertyDef> properties;
	public final Map<String,ContextDef> contexts;
	
	public ContextDef(String name) {
		this.name = name;
		properties = new HashMap<String,PropertyDef>();
		contexts = new HashMap<String,ContextDef>();
	}
}
