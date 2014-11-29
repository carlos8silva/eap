package com.netx.bl.R1.sql;
import com.netx.generics.R1.translation.Token;
import com.netx.generics.R1.util.Expr;

public class SymbolExpr {

	public Token pValue = null;
	public Token not = null;
	public String pRegexp = null;
	public Object aValue = null;
	
	public String toString() {
		if(pValue == null) {
			return super.toString();
		}
		return not==null ? pValue.toString() : not.toString()+" "+pValue.toString();
	}

	public String toSQL() {
		if(pRegexp != null) {
			System.err.println(pRegexp);
			return Expr.evaluate(pRegexp).toString();
		}
		return not==null ? pValue.getRawValue() : not.toString()+" "+pValue.getRawValue();
	}
}
