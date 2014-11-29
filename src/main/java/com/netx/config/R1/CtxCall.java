package com.netx.config.R1;
import com.netx.generics.R1.util.FunctionCall;


public class CtxCall implements FunctionCall {
	
	public String getFunctionName() {
		return "ctx";
	}
	
	public Class<?>[][] getParameters() {
		return new Class[][] {{String.class}};
	}

	public String call(Object[] args) {
		Object o = Context.getRoot().getObject(args[0].toString());
		if(o == null) {
			return null;
		}
		return o.toString();
	}
}

