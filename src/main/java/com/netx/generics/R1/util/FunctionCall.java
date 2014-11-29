package com.netx.generics.R1.util;


public interface FunctionCall {

	public String getFunctionName();
	public Class<?>[][] getParameters();
	public Object call(Object[] args) throws Exception;
}
