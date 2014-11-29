package com.netx.bl.R1.sql;
import com.netx.generics.R1.translation.Token;


public class SymbolOrderBy {

	public SymbolField field = null;
	public Token pOrder = null;
	
	public String toSQL(boolean addEntityName) {
		StringBuilder sb = new StringBuilder();
		sb.append(field.toSQL(addEntityName));
		sb.append(" ");
		if(pOrder == null) {
			sb.append("ASC");
		}
		else {
			sb.append(pOrder.getValue());
		}
		return sb.toString();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(field.toString());
		sb.append(" ");
		if(pOrder == null) {
			sb.append("ASC");
		}
		else {
			sb.append(pOrder.getValue());
		}
		return sb.toString();
	}
}
