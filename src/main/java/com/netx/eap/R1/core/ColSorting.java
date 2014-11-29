package com.netx.eap.R1.core;


public class ColSorting {

	public final String field;
	public final String order;
	
	public ColSorting(String field, String order) {
		this.field = field;
		this.order = order;
	}
	
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		if(this == o) {
			return true;
		}
		if(!(o instanceof ColSorting)) {
			return false;
		}
		return field.equals(((ColSorting)o).field);
	}

	public int hashCode() {
		return field.hashCode();
	}
}
