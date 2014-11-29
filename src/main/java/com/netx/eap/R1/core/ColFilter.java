package com.netx.eap.R1.core;
import com.netx.basic.R1.eh.IntegrityException;


public class ColFilter {

	public final String field;
	public final String expr;
	public final String start;
	public final String end;
	public final String[] values;
	public final boolean includeBlanks;
	
	public ColFilter(String field, String expr, boolean includeBlanks) {
		this.field = field;
		this.expr = expr.replace('*', '%');
		start = end = null;
		values = null;
		this.includeBlanks = includeBlanks;
	}

	public ColFilter(String field, String start, String end, boolean includeBlanks) {
		this.field = field;
		expr = null;
		this.start = start;
		this.end = end;
		values = null;
		this.includeBlanks = includeBlanks;
	}

	public ColFilter(String field, String[] values) {
		this.field = field;
		expr = start = end = null;
		this.values = values;
		this.includeBlanks = false;
	}
	
	public boolean hasValue(String value) {
		if(values == null) {
			throw new IntegrityException();
		}
		for(String s : values) {
			if(value.equals(s)) {
				return true;
			}
		}
		return false;
	}

	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		if(this == o) {
			return true;
		}
		if(!(o instanceof ColFilter)) {
			return false;
		}
		return field.equals(((ColFilter)o).field);
	}
}
