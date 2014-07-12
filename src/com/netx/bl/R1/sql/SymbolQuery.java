package com.netx.bl.R1.sql;
import java.util.List;
import java.util.ArrayList;
import com.netx.bl.R1.core.Field;


public abstract class SymbolQuery {

	public String queryName;
	public String originalSQL;
	public final List<Field> whereParams = new ArrayList<Field>();
	public SymbolWhere where;
	
	public String toString() {
		if(where == null) {
			return super.toString();
		}
		return where.toString();
	}
}
