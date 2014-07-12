package com.netx.bl.R1.sql;
import com.netx.generics.R1.translation.Token;
import com.netx.bl.R1.core.WhereExpr;


public class SymbolWhere {

	public Object left;
	public Token op;
	public Object right;
	public WhereExpr expr;
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("WHERE[");
		sb.append(left == null ? "<left>" : left.toString());
		sb.append(" ");
		sb.append(op == null ? "<op>" : op.toString());
		sb.append(" ");
		sb.append(right == null ? "<right>" : right.toString());
		sb.append("]");
		return sb.toString();
	}

	public String toSQL(boolean addEntityName) {
		StringBuilder sb = new StringBuilder();
		// Left:
		boolean isOr = op.getValue(true).equals("or");
		if(isOr) {
			sb.append("(");
		}
		if(left instanceof SymbolWhere) {
			SymbolWhere sw = (SymbolWhere)left;
			sb.append(sw.toSQL(addEntityName));
		}
		else if(left instanceof SymbolField) {
			SymbolField sf = (SymbolField)left;
			sb.append(sf.toSQL(addEntityName));
		}
		else {
			SymbolExpr se = (SymbolExpr)left;
			sb.append(se.toSQL());
		}
		if(isOr) {
			sb.append(")");
		}
		// Operator:
		sb.append(" ");
		sb.append(op.getValue());
		sb.append(" ");
		if(isOr) {
			sb.append("(");
		}
		// Right:
		if(right instanceof SymbolWhere) {
			SymbolWhere sw = (SymbolWhere)right;
			sb.append(sw.toSQL(addEntityName));
		}
		else if(right instanceof SymbolField) {
			SymbolField sf = (SymbolField)right;
			sb.append(sf.toSQL(addEntityName));
		}
		else {
			SymbolExpr se = (SymbolExpr)right;
			sb.append(se.toSQL());
		}
		if(isOr) {
			sb.append(")");
		}
		return sb.toString();
	}
}
