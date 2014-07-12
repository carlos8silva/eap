package com.netx.config.R1;
import java.util.Map;


public abstract class ComplexTypeLoader<T> {

	public abstract T onLoad(Map<String,Object> values, PropertyDef pDef) throws TypeLoadException;
	public abstract void onChange(T arg, Map<String,Object> changes, PropertyDef pDef) throws TypeLoadException;
}
