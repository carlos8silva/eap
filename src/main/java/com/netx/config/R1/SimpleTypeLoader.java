package com.netx.config.R1;


public abstract class SimpleTypeLoader<T> {

	public abstract T parse(String value, PropertyDef pDef) throws TypeLoadException;
}
