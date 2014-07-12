package com.netx.bl.R1.sql;

public class SymbolJoin {

	public SymbolTable table;
	public SymbolWhere where;
	
	public String toString() {
		if(table == null) {
			return super.toString();
		}
		return "JOIN["+table+"["+(where==null?"<where>":where)+"]]";
	}
}
