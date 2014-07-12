package com.netx.bl.R1.core;
import com.netx.bl.R1.core.Query.TYPE;


class TxUpdate {

	public final TYPE operation;
	public final EntityInstance<?,?> ei;
	public final WhereExpr where;
	public final ValidatedArgument[] updateArgs;
	public final Argument[] whereArgs;
	
	public TxUpdate(TYPE operation, EntityInstance<?,?> ei) {
		this.operation = operation;
		this.ei = ei;
		where = null;
		updateArgs = null;
		whereArgs = null;
	}
	
	public TxUpdate(TYPE operation, WhereExpr where, ValidatedArgument[] updateArgs, Argument[] whereArgs) {
		this.operation = operation;
		ei = null;
		this.where = where;
		this.updateArgs = updateArgs;
		this.whereArgs = whereArgs;
	}
}
