package com.netx.bl.R1.sql;
import java.lang.reflect.Method;
import com.netx.generics.R1.translation.Token;
import com.netx.bl.R1.core.Field;


public class SymbolField {

	public Token pFieldName;
	public Token pTableAlias;
	public Field aField;
	public Method aGetter;

	public String toString() {
		if(pFieldName == null) {
			return super.toString();
		}
		if(pTableAlias == null) {
			return pFieldName.toString();
		}
		else {
			return pTableAlias.toString()+"."+pFieldName.toString();
		}
	}

	public String toSQL(boolean addEntityName) {
		if(addEntityName) {
			return aField.getFullColumnName();
		}
		return aField.getColumnName();
	}
}
