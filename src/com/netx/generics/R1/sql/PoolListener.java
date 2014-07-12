package com.netx.generics.R1.sql;

public interface PoolListener {

	// Called when getConnection() is called a a new Connection needs to be created.
	public void onCreate(Object obj);
	// Called when getConnection() is called (whether or not a new object is created).
	public void onRetrieve(Object obj);
	// Called when the Connection is returned to the pool by calling close().
	public void onReturn(Object obj);
	// Called when the Connection is released from the pool and physically closed by:
	// 1) The pool's limit of idle connections has been reached;
	// 2) The connection has spent more than minEvictableIdleTimeMillis sitting idle.
	public void onDestroy(Object obj);
}
