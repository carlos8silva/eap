package com.netx.generics.R1.sql;
import java.util.Set;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.io.PrintWriter;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.shared.Globals;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;


public class ConnectionPool implements DataSource {

	private final GenericObjectPool _pool;
	private final PoolingDataSource _dataSource;
	private final InternalConnectionFactory _factory;
	private final Set<Object> _openConns;
	private boolean _closed;
	
	public ConnectionPool(Database details) {
		Checker.checkNull(details, "details");
		_pool = new GenericObjectPool(null);
		_pool.setMaxActive(100);
		_pool.setMaxIdle(100);
		_pool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
		_pool.setMaxWait(10*1000);
		_pool.setMinEvictableIdleTimeMillis(60*1000);
		DriverManagerConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
			details.getConnectionString(),
			details.getUsername(),
			details.getPassword()
		);
		// TODO configure PreparedStatement pooling
		_factory = new InternalConnectionFactory(connectionFactory, _pool);
		_dataSource = new PoolingDataSource(_pool);
		//_dataSource.setAccessToUnderlyingConnectionAllowed(true);
		_closed = false;
		_openConns = new HashSet<Object>();
		// Add a listener to hold track of all open connections,
		// so that we can close them all when pool.close() is called:
		addPoolListener(new PoolListenerAdapter() {
			public void onRetrieve(Object obj) {
				_openConns.add(obj);
			}
			public void onReturn(Object obj) {
				_openConns.remove(obj);
			}
		});
	}

	public Connection getConnection() throws SQLException {
		_checkClosed();
		try {
			return _dataSource.getConnection();
		}
		catch(NoSuchElementException nsee) {
			throw new PoolExhaustedException();
		}
		catch(SQLException sqle) {
			if(sqle.getMessage().indexOf("pool exhausted") != -1) {
				throw new PoolExhaustedException();
			}
			else {
				throw sqle;
			}
		}
	}

	public Connection getConnection(String username, String password) throws SQLException {
		_checkClosed();
		try {
			return _dataSource.getConnection(username, password);
		}
		catch(NoSuchElementException nsee) {
			throw new PoolExhaustedException();
		}
		catch(SQLException sqle) {
			if(sqle.getMessage().indexOf("pool exhausted") != -1) {
				throw new PoolExhaustedException();
			}
			else {
				throw sqle;
			}
		}
	}

	public void addPoolListener(PoolListener listener) {
		Checker.checkNull(listener, "listener");
		_factory.addPoolListener(listener);
	}

	public int getLoginTimeout() {
		_checkClosed();
		return _dataSource.getLoginTimeout();
	}
	
	public PrintWriter getLogWriter() {
		_checkClosed();
		return _dataSource.getLogWriter();
	}
	
	public void setLoginTimeout(int seconds) {
		_checkClosed();
		_dataSource.setLoginTimeout(seconds);
	}
	
	public void setLogWriter(PrintWriter writer) {
		_checkClosed();
		_dataSource.setLogWriter(writer);
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		_checkClosed();
		return _dataSource.getParentLogger();
	}

	public int getMaxActive() {
		_checkClosed();
		return _pool.getMaxActive();
	}

	public void setMaxActive(int maxActive) {
		_checkClosed();
		Checker.checkIllegalValue(maxActive, 0, "maxActive");
		_pool.setMaxActive(maxActive);
	}

	public int getMaxIdle() {
		_checkClosed();
		return _pool.getMaxIdle();
	}

	public void setMaxIdle(int maxIdle) {
		_checkClosed();
		_pool.setMaxIdle(maxIdle);
	}

	public int getMaxWait() {
		_checkClosed();
		return (int)_pool.getMaxWait()/1000;
	}

	public void setMaxWait(int maxWait) {
		_checkClosed();
		Checker.checkMinValue(maxWait, 0, "maxWait");
		Checker.checkIllegalValue(maxWait, 0, "maxWait");
		_pool.setMaxWait(maxWait*1000);
	}

	public int getRemoveAbandonedTimeout() {
		_checkClosed();
		return (int)_pool.getMinEvictableIdleTimeMillis()/1000;
	}

	public void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
		_checkClosed();
		Checker.checkMinValue(removeAbandonedTimeout, 0, "removeAbandonedTimeout");
		Checker.checkIllegalValue(removeAbandonedTimeout, 0, "removeAbandonedTimeout");
		_pool.setMinEvictableIdleTimeMillis(removeAbandonedTimeout*1000);
	}
	
	public int getNumActiveConnections() {
		_checkClosed();
		return _pool.getNumActive();
	}

	public int getNumIdleConnections() {
		_checkClosed();
		return _pool.getNumIdle();
	}
	
	public void close() throws SQLException {
		SQLException chain = null;
		try {
			if(!_closed) {
				_closed = true;
				// Check if there are any open connections:
				if(!_openConns.isEmpty()) {
					Globals.getLogger().warn("found "+_openConns.size()+" open connections when closing the pool");
					// Cannot iterate over _openConns directly, otherwise
					// pool listener will generate a ConcurrentModificationException:
					Object[] array = _openConns.toArray();
					for(Object o : array) {
						// Attempt to close all the the connections:
						try {
							((Connection)o).close();
						}
						catch(SQLException sqle) {
							if(chain == null) {
								chain = sqle;
							}
							else {
								chain.setNextException(sqle);
							}
						}
					}
				}
				// Calling close should have cleaned up _openConns, just check:
				if(!_openConns.isEmpty()) {
					throw new IntegrityException();
				}
				_pool.close();
			}
		}
		catch(SQLException sqle) {
			if(chain == null) {
				chain = sqle;
			}
			else {
				chain.setNextException(sqle);
			}
			throw chain;
		}
		catch(Exception e) {
			// We are not supposed to get any exceptions other than 
			// SQLException with GenericObjectPool.close():
			throw new IntegrityException(e);
		}
	}
	
	public boolean isWrapperFor(Class<?> iface) {
		return false;
	}
	
	public <T> T unwrap(Class<T> iface) {
		return null;
	}

	private void _checkClosed() {
		if(_closed) {
			throw new IllegalStateException("this connection pool has already been closed");
		}
	}
}
