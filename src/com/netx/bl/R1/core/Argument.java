package com.netx.bl.R1.core;
import com.netx.generics.R1.collections.Entry;


public class Argument extends Entry<Field,Comparable<?>> {

	public Argument(Field field, Comparable<?> value) {
		super(field, value);
		if(value != null) {
			if(value instanceof AllowedValue) {
				@SuppressWarnings("unchecked")
				Comparable<?> o = ((AllowedValue<Comparable<?>>)value).getCode();
				setValue(o);
			}
		}
	}

	public boolean isSet() {
		return getValue() != null;
	}

	public String toString() {
		return getKey().getName()+'='+getValue();
	}
}
