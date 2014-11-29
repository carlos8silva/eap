package com.netx.generics.R1.sql;

public abstract class PoolListenerAdapter implements PoolListener {

	public void onCreate(Object obj) {}
	public void onRetrieve(Object obj) {}
	public void onReturn(Object obj) {}
	public void onDestroy(Object obj) {}
}
