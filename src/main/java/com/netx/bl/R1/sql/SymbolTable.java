package com.netx.bl.R1.sql;
import com.netx.generics.R1.translation.Token;
import com.netx.bl.R1.core.MetaData;


public class SymbolTable {

	public Token pTableName = null;
	public Token pTableAlias = null;
	public MetaData aTable = null;
	
	public String toString() {
		if(pTableName == null) {
			return super.toString();
		}
		if(pTableAlias == null) {
			return pTableName.toString();
		}
		else {
			return pTableAlias.toString()+"."+pTableName.toString();
		}
	}
	
	public Token getAlias() {
		if(pTableName == null) {
			return null;
		}
		return pTableAlias == null ? pTableName : pTableAlias;
	}
}
