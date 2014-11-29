package com.netx.config.R1;
import com.netx.basic.R1.eh.Checker;


public class ContextWrapper {

	private final Context _ctx;
	
	protected ContextWrapper(Context ctx, String id) {
		Checker.checkNull(ctx, "ctx");
		Checker.checkEmpty(id, "id");
		_ctx = ctx;
		ContextDef def = Dictionary.getContextDef(id);
		if(def == null) {
			throw new IllegalArgumentException("could not find context definition for id '"+id+"'");
		}
		if(ctx.getDef() != def) {
			throw new IllegalArgumentException("ctx does not have the expected context type");
		}
		_ctx.lock();
	}
	
	public Context getContext() {
		return _ctx;
	}
}
