package com.netx.bl.R1.core;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.shared.Constants;
import com.netx.bl.R1.sql.Parser;
import com.netx.generics.R1.time.Timestamp;
import com.netx.generics.R1.util.ConstructionException;
import com.netx.generics.R1.util.Strings;
import com.netx.generics.R1.util.Tools;
import com.netx.generics.R1.collections.IMap;


public abstract class Entity<M extends MetaData, EI extends EntityInstance<M,?>> {
	
	private final M _metaData;
	private final Map<String,Query> _queries;
	private Cache _cache;
	private Repository _rep = null;
	// Queries:
	private Insert _qInsert;
	private Update _qUpdateByPK;
	private Update _qDeleteByPK;
	private Update _qTruncate;
	private Select _qSelectByPK;
	private Select _qSelectAll;
	
	protected Entity(M metaData) {
		_metaData = metaData;
		_queries = new HashMap<String,Query>();
	}

	public M getMetaData() {
		return _metaData;
	}

	// For Repository:
	void load(Repository rep) {
		_rep = rep;
		// Insert:
		_qInsert = new Insert(getMetaData());
		_queries.put(_qInsert.getName(), _qInsert);
		// Update by PK:
		_qUpdateByPK = new Update(Query.TYPE.UPDATE, getMetaData());
		_queries.put(_qUpdateByPK.getName(), _qUpdateByPK);
		// Delete by PK:
		_qDeleteByPK = new Update(Query.TYPE.DELETE, getMetaData());
		_queries.put(_qDeleteByPK.getName(), _qDeleteByPK);
		// Truncate:
		_qTruncate = new Update(Query.TYPE.TRUNCATE, getMetaData());
		_queries.put(_qTruncate.getName(), _qTruncate);
		// Select by PK:
		_qSelectByPK = createSelect("select-by-pk", "SELECT * FROM "+getMetaData().getTableName()+" WHERE "+WhereExpr.toSQL(getMetaData().getPrimaryKeyFields()));
		_qSelectByPK.setUpdatesCache(true);
		// Select all:
		_qSelectAll = createSelect("select-all", "SELECT * FROM "+getMetaData().getTableName());
		// _qSelectAll.updatesCache is false otherwise performance would be affected.
	}

	protected void onLoad() {
	}
	
	public Repository getRepository() {
		return _rep;
	}

	protected Select createSelect(String name, String sql) {
		_checkArgs(name, sql);
		_checkStatus();
		Select s = Parser.parseSelect(name, sql, getMetaData(), _rep);
		_queries.put(name, s);
		return s;
	}

	protected Update createUpdate(String name, String sql) {
		_checkArgs(name, sql);
		_checkStatus();
		Update u = Parser.parseUpdate(name, sql, getMetaData(), _rep);
		_queries.put(name,  u);
		return u;
	}

	protected Select getSelect(String name) {
		Checker.checkEmpty("name", name);
		Query q = _queries.get(name);
		if(q == null) {
			return null;
		}
		if(q.getType() != Query.TYPE.SELECT) {
			throw new IllegalArgumentException("query '"+name+"' is not a SELECT query");
		}
		return (Select)q;
	}

	protected Update getUpdate(String name) {
		Checker.checkEmpty("name", name);
		Query q = _queries.get(name);
		if(q == null) {
			return null;
		}
		if(q.getType() != Query.TYPE.UPDATE && q.getType() != Query.TYPE.UPDATE) {
			throw new IllegalArgumentException("query '"+name+"' is not an UPDATE or DELETE query");
		}
		return (Update)q;
	}

	// For Repository:
	IMap<String,Query> getQueries() {
		return new IMap<String,Query>(_queries);
	}

	private void _checkArgs(String name, String sql) {
		Checker.checkEmpty(name, "name");
		Checker.checkEmpty(name, "sql");
		if(_queries.containsKey(name)) {
			throw new IllegalArgumentException(name+": query already exists");
		}
	}

	private void _checkStatus() {
		if(_rep == null) {
			throw new IllegalStateException("entity has not been loaded yet. Move code to onLoad method");
		}
	}

	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		return o == this;
	}
	
	public String toString() {
		return "Entity: "+getMetaData().getName();
	}

	public EI get(Connection c, Comparable<?> ... values) throws BLException {
		_checkConnection(c);
		Checker.checkNullElements(values, "values");
		return selectInstance(c, _qSelectByPK, values);
	}

	// Used directly by DAFacades to insert a new entity instance.
	protected void insert(Connection c, EI ei) throws BLException, ValidationException {
		_checkConnection(c);
		Checker.checkNull(ei, "ei");
		try {
			final boolean usingCache = _checkIfUsingCache(c);
			Transaction tx = c.getTransaction();
			if(tx != null) {
				// Inside transaction:
				_doDatabaseInsert(c, ei);
				if(usingCache) {
					tx.storeInsert(ei);
				}
			}
			else {
				// Insert on the db and cache must be atomic otherwise there
				// is a possibility that a select creates the EI before the
				// db insert (i.e. old data) and puts it on the cache afterwards.
				// A lock on cache.writeLock will force us to wait for any select
				// to finish (and possibly update the cache), and any select
				// to wait for us to update both db and cache.
				try {
					if(usingCache) {
						_getCache(c).getWriteLock().lock();
					}
					_doDatabaseInsert(c, ei);
					if(usingCache) {
						_getCache(c).putInstance(ei);
					}
				}
				finally {
					// This ensures we always release the write lock:
					if(usingCache) {
						_getCache(c).getWriteLock().unlock();
					}
				}
			}
		}
		catch(ValidationException ve) {
			c.recover();
			throw ve;
		}
		catch(Exception e) {
			c.recover();
			throw c.translate(e);
		}
	}

	private void _doDatabaseInsert(Connection c, EI ei) throws WrappedSQLException, ValidationException {
		long key = _qInsert.execute(c, ei);
		if(getMetaData().hasAutonumberKey()) {
			// Calling 'setPrimaryKey' will:
			// 1) For regular entities, simply set the automatically generated primary key;
			// 2) For holder entities, sets its own key and the associations' related key as well.
			ei.setPrimaryKey(getMetaData().getAutonumberKeyField(), new Long(key));
		}
		ei.setFullInformation(true);
		// We do not need tracked updates anymore:
		ei.flushUpdates();
	}

	// Used directly by Entities to update an entity instance. This method dynamically
	// generates the UPDATE query based on the fields updated by the application level. 
	protected void updateInstance(Connection c, EI ei) throws BLException, ReadOnlyFieldException {
		_checkConnection(c);
		Checker.checkNull(ei, "ei");
		// Check whether the entity instance has a primary key set:
		_checkPrimaryKey(ei);
		// If the entity instance hasn't been updated, don't run a query:
		if(!ei.hasUpdates()) {
			return;
		}
		// It has updated, lets set the time_updated field:
		if(getMetaData() instanceof TimedMetaData) {
			ei.safelySetValue(((TimedMetaData)getMetaData()).timeUpdated, new Timestamp());
		}
		try {
			final boolean usingCache = _checkIfUsingCache(c);
			// 1) Prepare a query for the update (and validate read-only fields):
			// We need to dynamically create the UPDATE query:
			Transaction tx = c.getTransaction();
			if(tx != null) {
				// Inside transaction:
				long result = _qUpdateByPK.execute(c, ei);
				_checkUpdateResult(result, 0, 1);
				if(result == 0) {
					// This can happen if a previous update deleted the EI:
					throw new InstanceDeletedException(ei);
				}
				if(usingCache) {
					tx.storeUpdate(ei);
				}
			}
			else {
				// Outside of transaction:
				try {
					if(usingCache) {
						// Update on the DB and cache must be atomic otherwise there
						// is a possibility that a select creates the EI before the
						// DB update (i.e. old data) and puts it on the cache afterwards.
						// A lock on cache.writeLock will force us to wait for any select
						// to finish (and possibly update the cache), and any select
						// to wait for us to update both DB and cache.
						_getCache(c).getWriteLock().lock();
					}
					// Do database update:
					long result = _qUpdateByPK.execute(c, ei);
					_checkUpdateResult(result, 0, 1);
					if(result == 0) {
						// This can happen if a previous update deleted the EI:
						throw new InstanceDeletedException(ei);
					}
					if(usingCache) {
						// Update the cached element, if any:
						_getCache(c).updateInstance(ei);
					}
					// We cannot flush the updates if there is a transaction set,
					// because they will be used during commit to update the cache.
					// So this line needs to be here rather than after the entire if block.
					ei.flushUpdates();
				}
				finally {
					if(usingCache) {
						_getCache(c).getWriteLock().unlock();
					}
				}
			}
		}
		catch(ReadOnlyFieldException rofe) {
			c.recover();
			throw rofe;
		}
		catch(Exception e) {
			c.recover();
			throw c.translate(e);
		}
	}

	// Used by Entities and exposed with the name defined in the DED file.
	// Note: ValidatedArgument already checks if attempting to update a read-only field.
	protected int updateList(Connection c, Update query, Comparable<?> ... values) throws BLException {
		_checkConnection(c);
		_checkQuery(query, Query.TYPE.UPDATE);
		ValidatedArgument[] updateArgs = query.prepareUpdateArgs(values);
		Argument[] whereArgs = query.prepareWhereArgs(values);
		try {
			final boolean usingCache = _checkIfUsingCache(c);
			Transaction tx = c.getTransaction();
			if(tx != null) {
				// Inside transaction:
				int result = query.execute(c, updateArgs, whereArgs);
				if(result != 0 && usingCache) {
					tx.storeUpdate(getMetaData(), query.getWhereClause(), updateArgs, whereArgs);
				}
				return result;
			}
			else {
				try {
					if(usingCache) {
						_getCache(c).getWriteLock().lock();
					}
					int result = query.execute(c, updateArgs, whereArgs);
					if(result != 0 && usingCache) {
						_getCache(c).updateList(c, query.getWhereClause(), updateArgs, whereArgs);
					}
					return result;
				}
				finally {
					if(usingCache) {
						_getCache(c).getWriteLock().unlock();
					}
				}
			}
		}
		catch(Exception e) {
			c.recover();
			throw c.translate(e);
		}
	}

	protected void insertOrUpdate(Connection c, EI ei) throws BLException, ValidationException {
		Checker.checkNull(ei, "ei");
		EI existing = selectInstance(c, _qSelectByPK, ei.getPrimaryKey().getValues());
		if(existing == null) {
			insert(c, ei);
		}
		else {
			updateInstance(c, ei);
		}
	}

	// Used directly by Entities.
	protected void deleteInstance(Connection c, EI ei) throws BLException {
		_checkConnection(c);
		Checker.checkNull(ei, "ei");
		// Check whether the entity instance has a primary key set:
		_checkPrimaryKey(ei);
		try {
			final boolean usingCache = _checkIfUsingCache(c);
			// Prepare a query for the delete:
			Argument[] args = _prepareArgsForDelete(c, ei);
			// We do not need tracked updates:
			ei.flushUpdates();
			Transaction tx = c.getTransaction();
			if(tx != null) {
				// Inside transaction:
				long result = _qDeleteByPK.execute(c, args);
				_checkUpdateResult(result, 0, 1);
				if(result == 0) {
					// This can happen if a previous update deleted the EI:
					throw new InstanceDeletedException(ei);
				}
				if(usingCache) {
					tx.storeDelete(ei);
				}
			}
			else {
				try {
					if(usingCache) {
						_getCache(c).getWriteLock().lock();
					}
					long result = _qDeleteByPK.execute(c, args);
					_checkUpdateResult(result, 0, 1);
					if(result == 0) {
						// This can happen if a previous update deleted the EI:
						throw new InstanceDeletedException(ei);
					}
					if(usingCache) {
						_getCache(c).removeInstance(c, ei.getPrimaryKey());
					}
				}
				finally {
					if(usingCache) {
						_getCache(c).getWriteLock().unlock();
					}
				}
			}
		}
		catch(Exception e) {
			c.recover();
			throw c.translate(e);
		}
	}

	private Argument[] _prepareArgsForDelete(Connection c, EI ei) {
		Argument[] args = new Argument[getMetaData().getPrimaryKeyFields().size()];
		Iterator<Field> itFields = getMetaData().getPrimaryKeyFields().iterator();
		for(int i=0; itFields.hasNext(); i++) {
			Field f = itFields.next();
			args[i] = new Argument(f, ei.getValue(f));
		}
		return args;
	}

	protected int deleteList(Connection c, Update query, Comparable<?> ... values) throws BLException {
		_checkConnection(c);
		_checkQuery(query, query==_qTruncate ? Query.TYPE.TRUNCATE : Query.TYPE.DELETE);
		Argument[] whereArgs = query.prepareWhereArgs(values);
		try {
			final boolean usingCache = _checkIfUsingCache(c);
			Transaction tx = c.getTransaction();
			if(tx != null) {
				// Inside transaction:
				int results = query.execute(c, whereArgs);
				if(usingCache) {
					if(query == _qTruncate) {
						tx.storeTruncate(getMetaData());
					}
					else {
						tx.storeDelete(getMetaData(), query.getWhereClause(), whereArgs);
					}
				}
				return results;
			}
			else {
				int results = -1;
				try {
					if(usingCache) {
						_getCache(c).getWriteLock().lock();
					}
					results = query.execute(c, whereArgs);
					if(usingCache) {
						if(query == _qTruncate) {
							_getCache(c).clear(c);
						}
						else {
							_getCache(c).removeList(c, query.getWhereClause(), whereArgs);
						}
					}
				}
				finally {
					if(usingCache) {
						_getCache(c).getWriteLock().unlock();
					}
				}
				return results;
			}
		}
		catch(Exception e) {
			c.recover();
			throw c.translate(e);
		}
	}

	protected int deleteAll(Connection c) throws BLException {
		_checkConnection(c);
		return deleteList(c, _qTruncate);
	}
	
	// Used by Entities to run queries that return a single result, which may be:
	// 1) Based on the equality of the primary key (i.e. _qSelectByPK);
	// 2) Based on the equality of a combination of primary key and unique fields.
	@SuppressWarnings("unchecked")
	protected EI selectInstance(Connection c, Select query, Comparable<?> ... values) throws BLException {
		_checkConnection(c);
		_checkQuery(query, Query.TYPE.SELECT);
		if(!query.uniqueResult()) {
			throw new IllegalArgumentException("query must return an unique result. Use selectList instead");
		}
		Argument[] whereArgs = query.prepareArgs(values);
		try {
			final boolean usingCache = _checkIfUsingCache(c);
			if(usingCache) {
				_getCache(c).getReadLock().lock();
				EntityInstance<?,?> ei = null;
				if(query == _qSelectByPK) {
					// Direct retrieval via primary key:
					ei = _getCache(c).getInstance(new PrimaryKey(whereArgs));
				}
				else if(query.uniqueResult()) {
					// Search based on combination of PK + unique value(s):
					ei = _getCache(c).searchInstance(c, query.getWhereClause(), whereArgs);
				}
				else {
					// Should have been captured as a pre-condition:
					throw new IntegrityException();
				}
				_getCache(c).getReadLock().unlock();
				if(ei != null) {
					query.countCacheHit(_rep);
					return (EI)ei;
				}
				if(_getCache(c).getConfig().cachePolicyFull()) {
					query.countCacheHit(_rep);
					return null;
				}
			}
			// If we’ve reached this section, the EI has not been found and this table is not
			// configured with full memory caching (meaning we need to hit the database):
			if(usingCache && query.getUpdatesCache()) {
				_getCache(c).getWriteLock().lock();
			}
			QueryResults qr = query.execute(c, whereArgs, null);
			ResultSet rs = qr.getResultSet();
			EI ei = null;
			if(rs.next()) {
				ei = (EI)_createEI(c, rs);
			}
			if(usingCache && query.getUpdatesCache()) {
				if(ei != null) {
					_getCache(c).putInstance(ei);
				}
				_getCache(c).getWriteLock().unlock();
			}
			rs.close();
			qr.close();
			return ei;
		}
		catch(Exception e) {
			c.recover();
			throw c.translate(e);
		}
	}

	@SuppressWarnings("unchecked")
	// Used for Entity queries that return multiple results.
	protected List<EI> selectList(Connection c, Select query, Comparable<?> ... values) throws BLException {
		_checkConnection(c);
		_checkQuery(query, Query.TYPE.SELECT);
		Argument[] whereArgs = query.prepareArgs(values);
		Limit limit = query.prepareLimit(values);
		try {
			final boolean usingCache = _checkIfUsingCache(c);
			// The cache may be empty in the first call of this method,
			// in which case we need to hit the database.
			if(usingCache && _getCache(c).getConfig().cachePolicyFull()) {
				_getCache(c).getReadLock().lock();
				// Search in memory:
				List<EI> results = (List<EI>)_getCache(c).search(c, query.getWhereClause(), whereArgs, query.getOrderByClause(), limit);
				_getCache(c).getReadLock().unlock();
				// Apply sorting:
				if(query.getOrderByClause() != null && query.getLimitClause() == null) {
					// Sorting is only applied if LIMIT clause is null. Otherwise,
					// it has already been applied in the Cache.search method.
					Sorter.sort(results, query.getOrderByClause(), c.getRepository().getJdbcPropertyCache());
				}
				// Applying limits is not necessary since they have already been applied in Cache.search.
				query.countCacheHit(_rep);
				return results;
			}
			// If we reached this section of the code, we need to hit the database:
			List<EI> results = new ArrayList<EI>();
			
			// Lock read access to cache if needed:
			if(usingCache && query.getUpdatesCache()) {
				_getCache(c).getWriteLock().lock();
			}

			// Run the query and create results:
			QueryResults qr = query.execute(c, whereArgs, limit);
			ResultSet rs = qr.getResultSet();
			
			// Skip upper limit records if the database does not support the limit clause:
			if(limit != null && !c.getRepository().getDriver().supportsLimitClause()) {
				if(limit.offset != null) {
					for(int i=0; i<limit.offset; i++) {
						rs.next();
					}
				}
			}
			
			// Store results:
			for(int i=0; rs.next(); i++) {
				if(limit != null && i == limit.numRows) {
					break;
				}
				EI ei = (EI)_createEI(c, rs);
				results.add(ei);
				if(usingCache && query.getUpdatesCache()) {
					// Only update the cache if it does not have the EI there already:
					if(_getCache(c).getInstance(ei.getPrimaryKey()) == null) {
						_getCache(c).putInstance(ei);
					}
				}
			}
			rs.close();
			qr.close();

			// Release cache lock:
			if(usingCache && query.getUpdatesCache()) {
				_getCache(c).getWriteLock().unlock();
			}
			
			// Sort results if needed:
			if(query.getOrderByClause() != null && limit == null) {
				Sorter.sort(results, query.getOrderByClause(), c.getRepository().getJdbcPropertyCache());
			}

			return results;
		}
		catch(Exception e) {
			c.recover();
			throw c.translate(e);
		}
	}

	protected List<EI> selectAll(Connection c) throws BLException {
		return selectList(c, _qSelectAll);
	}

	// For Repository:
	List<EntityInstance<?,?>> doInitialSelect(Connection c, CacheConfig config) throws BLException {
		try {
			QueryResults qr = _qSelectAll.execute(c, (Argument[])null, null);
			ResultSet rs = qr.getResultSet();
			List<EntityInstance<?,?>> results = new ArrayList<EntityInstance<?,?>>();
			for(int i=1; rs.next(); i++) {
				results.add(_createEI(c, rs));
				if(config.cachePolicyLimited() && i == config.getCacheSize()) {
					break;
				}
			}
			rs.close();
			qr.close();
			return results;
		}
		catch(Exception e) {
			c.recover();
			throw c.translate(e);
		}
	}

	@SuppressWarnings("unchecked")
	private EI _createEI(Connection c, ResultSet rs) throws BLException, SQLException {
		try {
			// 1) Retrieve all the primary key fields:
			// (we can do this because the code generation mechanism ensures 
			// that the primary key fields are the first ones in q.selectedFields
			// and that they are in the correct sequence):
			Object[] primKey = new Object[getMetaData().getPrimaryKeyFields().size()];
			Iterator<Field> it = getMetaData().getFields().iterator();
			int i = 0;
			for(; i<primKey.length; i++) {
				primKey[i] = it.next().toObject(rs.getString(i+1), c.getRepository().getDriver());
			}
			// 2) Create the entity instance:
			EI ei = (EI)Tools.createObject(getMetaData().getInstanceClass(), primKey);
			// 3) Initialize all the other fields retrieved in the query:
			for(; it.hasNext(); i++) {
				Field f = it.next();
				String value = rs.getString(i+1);
				if(Strings.isEmpty(value) || Constants.NULL_UC.equals(value)) {
					ei.setWithoutTrackingUpdates(f, Field.NULL);
				}
				else {
					ei.setWithoutTrackingUpdates(f, f.toObject(value, c.getRepository().getDriver()));
				}
			}
			// 5) fullInformation:
			// This is needed to differentiate between EI's that have been retrieved from the
			// database (and therefore have all of their fields set) and EI's that are created
			// by the application level with partial information only, to be used in updates
			ei.setFullInformation(true);
			return ei;
		}
		catch(ConstructionException ce) {
			// This error only happens if the entity instance does not have a constructor with the
			// primary key. Considering the code is automatically generated, this should not really 
			// happen (otherwise its an error on the application and needs to be propagated):
			throw new IntegrityException(ce);
		}
		catch(ValidationException ve) {
			throw new IntegrityException(ve);
		}
	}

	private void _checkConnection(Connection c) {
		Checker.checkNull(c, "c");
		if(c.getRepository() != getRepository()) {
			throw new IllegalArgumentException("must use a connection from the same Repository where this entity was loaded");
		}
	}

	private void _checkQuery(Query query, Query.TYPE type) {
		Checker.checkNull(query, "query");
		if(query.getMetaData() != getMetaData()) {
			throw new IllegalArgumentException("expected query for entity '"+getMetaData().getName()+"', found '"+query.getMetaData().getName()+"'");
		}
		if(query.getType() != type) {
			throw new IllegalArgumentException("expected "+type+" query, found: "+query.getType());
		}
	}

	private void _checkPrimaryKey(EI ei) {
		if(ei.getPrimaryKey() == null) {
			throw new IllegalArgumentException("entity instance must have the primary key set");
		}
	}

	// This is used to throw an exception only when the result is not what we expect.
	private void _checkUpdateResult(long result, long ... allowedResults) {
		for(long allowed : allowedResults) {
			if(result == allowed) {
				return;
			}
		}
		throw new IntegrityException(result);
	}

	private boolean _checkIfUsingCache(Connection c) {
		return c.getRepository().getConfig().getCacheEnabled() && !_getCache(c).getConfig().cachePolicyNone();
	}

	private Cache _getCache(Connection c) {
		if(_cache == null) {
			_cache = c.getRepository().getCacheDirectly(getMetaData());
		}
		return _cache;
	}
}
