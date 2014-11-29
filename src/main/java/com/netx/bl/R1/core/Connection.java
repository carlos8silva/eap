package com.netx.bl.R1.core;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.netx.generics.R1.util.Tools;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;


public class Connection {

	private final Repository _rep;
	private java.sql.Connection _c;
	private boolean _open;
	private Transaction _tx;
	private final Map<Query,PreparedStatement> _pstmtCache;
	
	// For Repository:
	Connection(Repository rep, java.sql.Connection c) throws BLException {
		_rep = rep;
		_tx = null;
		if(_rep.getConfig().getUsePreparedStatements()) {
			_pstmtCache = new HashMap<Query,PreparedStatement>();
		}
		else {
			_pstmtCache = null;
		}
		activate(c);
	}

	public Repository getRepository() {
		_checkClosed();
		return _rep;
	}

	public boolean transactionStarted() {
		return _tx != null;
	}
	
	public void startTransaction() throws BLException {
		// Perform initial checks:
		// (these do not need to cater for multiple thread access because
		// ConnectionPool ensures one connection object is not shared across threads).
		_checkClosed();
		_checkTransactionDoesNotExist();
		_tx = new Transaction();
		try {
			_c.setAutoCommit(false);
		}
		catch(SQLException sqle) {
			// This shouldn't happen:
			throw new IntegrityException(sqle);
		}
	}
	
	public void commit() throws BLException {
		_checkClosed();
		_checkTransactionExists();
		try {
			// If we do not have any updates to do, we can just commit:
			if(!_tx.hasUpdates()) {
				_c.commit();
				_cleanupTransaction();
				return;
			}
			// Otherwise we need to perform an atomic commit on the database and memory:
			final boolean usingCache = getRepository().getConfig().getCacheEnabled();
			Collection<MetaData> txParticipants = null;
			if(usingCache) {
				txParticipants = _tx.getParticipants();
				LockUtils.obtainLocksFor(getRepository(), txParticipants);
			}
			// First do the commit on the database:
			_c.commit();
			// Now update the cache as well:
			if(usingCache) {
				try {
					for(MetaData m : txParticipants) {
						List<TxUpdate> updates = _tx.getUpdatesFor(m);
						if(updates == null) {
							// This happens when one of the participants is an association involved
							// in the transaction because the holder entity is being deleted.
							continue;
						}
						Cache cache = _rep.getCacheFor(m);
						for(TxUpdate update : updates) {
							if(update == null) {
								throw new IntegrityException();
							}
							else if(update.operation.equals(Query.TYPE.INSERT)) {
								cache.putInstance(update.ei);
							}
							else if(update.operation.equals(Query.TYPE.UPDATE)) {
								if(update.ei != null) {
									cache.updateInstance(update.ei);
									// Flush updates:
									update.ei.flushUpdates();
								}
								else {
									cache.updateList(this, update.where, update.updateArgs, update.whereArgs);
								}
							}
							else if(update.operation.equals(Query.TYPE.DELETE)) {
								if(update.ei != null) {
									cache.removeInstance(this, update.ei.getPrimaryKey());
								}
								else {
									cache.removeList(this, update.where, update.whereArgs);
								}
							}
							else if(update.operation.equals(Query.TYPE.TRUNCATE)) {
								cache.clear(this);
							}
							else {
								throw new IntegrityException();
							}
						}
					}
				}
				finally {
					// Release locks:
					LockUtils.releaseLocksFor(_rep, txParticipants);
				}
			}
			// Finish transaction:
			_cleanupTransaction();
		}
		catch(Exception e) {
			recover();
			throw translate(e);
		}
	}

	public void rollback() throws BLException {
		_checkClosed();
		_checkTransactionExists();
		// Attempt a rollback:
		try {
			_c.rollback();
			// Release any held locks:
			LockUtils.releaseLocksFor(_rep, _tx.getParticipants());
			// Finish transaction:
			_cleanupTransaction();
		}
		catch(Exception e) {
			recover();
			throw translate(e);
		}
	}

	public void close() throws BLException {
		if(_open) {
			try {
				if(_tx != null) {
					throw new IllegalStateException("connection has a transaction currently open");
				}
				if(_c.getAutoCommit() == false) {
					throw new IntegrityException();
				}
				_open = false;
				_c.close();
				// We do not clear the PreparedStatement cache, so that ps's can 
				// be used by threads that reclaim this connection in the future.
			}
			catch(SQLException sqle) {
				recover();
				throw translate(sqle);
			}
		}
	}

	public Results select(Select query, Comparable<?> ... args) throws BLException {
		Checker.checkNull(query, "query");
		if(query.getMetaData() != null) {
			throw new IllegalArgumentException("expected global query, found query for entity '"+query.getMetaData().getName()+"'");
		}
		try {
			// Run the query and create results:
			QueryResults qr = query.execute(this, args, query.prepareLimit(args));
			ResultSet rs = qr.getResultSet();

			// Get column names:
			ResultSetMetaData rsmd = rs.getMetaData();
			String[] cols = new String[rsmd.getColumnCount()];
			for(int i=0; i<cols.length; i++) {
				cols[i] = rsmd.getColumnName(i+1);
			}
			Results results = new Results(getRepository().getDriver(), cols);

			// Skip upper limit records if the database does not support the limit clause:
			Limit limit = query.getLimitClause();
			if(limit != null && !getRepository().getDriver().supportsLimitClause()) {
				if(limit.offset != null) {
					for(int i=0; i<limit.offset; i++) {
						rs.next();
					}
				}
			}

			// Get data:
			for(int i=0; rs.next(); i++) {
				if(limit != null && i == limit.numRows) {
					break;
				}
				String[] values = new String[cols.length];
				for(int j=0; j<values.length; j++) {
					values[j] = rs.getString(j+1);
				}
				results.addRow(values);
			}
			rs.close();
			qr.close();

			// Sort results if needed:
			if(query.getOrderByClause() != null && limit == null) {
				Sorter.sort(results.getRows(), query.getOrderByClause(), getRepository().getJdbcPropertyCache());
			}

			return results;
		}
		catch(Exception e) {
			recover();
			throw translate(e);
		}
	}

	public Results select(String sql, Comparable<?> ... values) throws BLException {
		return select(getRepository().createSelect(sql), values);
	}

	public void finalize() throws Exception {
		close();
	}

	// For self and Repository:
	void activate(java.sql.Connection c) {
		_open = true;
		_c = c;
		try {
			// Just to make sure:
			_c.setAutoCommit(true);
		}
		catch(SQLException sqle) {
			// Not supposed to happen:
			// (we don't need to recover at this point)
			throw new IntegrityException(sqle);
		}
	}

	// For DAFacade:
	Transaction getTransaction() {
		return _tx;
	}

	// For self and DAFacade:
	void recover() {
		if(_tx != null) {
			// 1) Rollback:
			try {
				_c.rollback();
			}
			catch(Exception ex) {
				Tools.handleCriticalError(ex);
			}
			// 2) Clear any held locks:
			try {
				LockUtils.releaseLocksFor(_rep, _tx.getParticipants());
			}
			catch(Exception ex) {
				Tools.handleCriticalError(ex);
			}
			// 3) Finish transaction:
			try {
				_cleanupTransaction();
			}
			catch(Exception ex) {
				Tools.handleCriticalError(ex);
			}
		}
	}
	
	// For self and DAFacade:
	BLException translate(Exception e) {
		if(e instanceof SQLException) {
			return _rep.getDriver().translateException((SQLException)e, null, null);
		}
		else if(e instanceof WrappedSQLException) {
			WrappedSQLException wse = (WrappedSQLException)e;
			return _rep.getDriver().translateException((SQLException)wse.getCause(), wse.getSQL(), wse.getArguments());
		}
		else if(e instanceof BLException) {
			return (BLException)e;
		}
		else if(e instanceof RuntimeException) {
			throw (RuntimeException)e;
		}
		else {
			throw new IntegrityException(e);
		}
	}

	// For PreparedQuery:
	Statement createStatement() throws SQLException {
		return _c.createStatement(
			ResultSet.TYPE_FORWARD_ONLY,
			ResultSet.CONCUR_READ_ONLY,
			ResultSet.CLOSE_CURSORS_AT_COMMIT
		);
	}
	
	// For Query:
	PreparedStatement getPreparedStatementFor(Query query, String sql, int autoGeneratedKeys) throws SQLException {
		PreparedStatement ps = _pstmtCache.get(query);
		if(ps == null) {
			ps = _c.prepareStatement(sql, autoGeneratedKeys);
		}
		return ps;
	}

	private void _checkClosed() {
		if(!_open) {
			throw new IllegalStateException("connection is closed");
		}
	}
	
	private void _checkTransactionDoesNotExist() {
		if(_tx != null) {
			throw new IllegalStateException("a transaction has already been set");
		}
	}

	private void _checkTransactionExists() {
		if(_tx == null) {
			throw new IllegalStateException("there is no transaction set");
		}
	}

	private void _cleanupTransaction() {
		_tx.clearChanges();
		_tx = null;
		try {
			_c.setAutoCommit(true);
		}
		catch(SQLException sqle) {
			// Not supposed to happen:
			throw new IntegrityException(sqle);
		}
	}
}
