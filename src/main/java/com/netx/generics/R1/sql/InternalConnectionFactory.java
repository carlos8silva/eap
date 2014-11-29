package com.netx.generics.R1.sql;
import java.util.List;
import java.util.ArrayList;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.pool.ObjectPool;


public class InternalConnectionFactory extends PoolableConnectionFactory {

	private final List<PoolListener> _listeners;
	
	public InternalConnectionFactory(ConnectionFactory connFactory, ObjectPool pool) {
		super(connFactory, pool, null, null, false, true);
		_listeners = new ArrayList<PoolListener>();
	}

	public void addPoolListener(PoolListener listener) {
		_listeners.add(listener);
	}
	
	public Object makeObject() throws Exception {
		Object obj = super.makeObject();
		for(PoolListener listener : _listeners) {
			listener.onCreate(obj);
		}
		return obj;
	}

	public void activateObject(Object obj) throws Exception {
		super.activateObject(obj);
		for(PoolListener listener : _listeners) {
			listener.onRetrieve(obj);
		}
	}

	public void passivateObject(Object obj) throws Exception {
		super.passivateObject(obj);
		for(PoolListener listener : _listeners) {
			listener.onReturn(obj);
		}
	}

	public void destroyObject(Object obj) throws Exception {
		super.destroyObject(obj);
		for(PoolListener listener : _listeners) {
			listener.onDestroy(obj);
		}
	}
}
