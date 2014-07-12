package com.netx.bl.R1.core;
import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Timer;
import java.util.TimerTask;
import com.netx.generics.R1.collections.ICollection;
import com.netx.generics.R1.sql.Database;
import com.netx.generics.R1.sql.ConnectionPool;
import com.netx.generics.R1.sql.PoolListenerAdapter;
import com.netx.generics.R1.util.Tools;
import com.netx.generics.R1.util.Version;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.bl.R1.spi.DatabaseDriver;
import com.netx.bl.R1.spi.DriverRegistry;
import com.netx.bl.R1.sql.Parser;
import com.netx.config.R1.Context;
import com.netx.config.R1.Property;
import com.netx.config.R1.PropertyListener;


// TODO when generating the database structure for MySQL we need
// to set the appropriate character set for each field. For example:
// Case insensitive: products.name varchar(50) binary character set latin1 collate latin1_bin NOT NULL default ''
// Case sensitive: products.name varchar(50) character set latin1 collate latin1_general_ci NOT NULL default ''
public class Repository {

	// TYPE:
	private static Map<Class<? extends Domain>,Repository> _repositories = new HashMap<Class<? extends Domain>,Repository>();
	// This is used to help caching bl.Connection objects. Because of the way that DBCP works,
	// we cannot use the pooled sql.Connection object as the key. Since the object added as key
	// in the PoolListener is a DelegateConnection and the object retrieved in _getConnection is
	// a PoolGuardConnection, they are not compatible to be used as keys in _openConns
	// (please find more details in http://commons.apache.org/dbcp/configuration.html).
	// But because both objects return the same identifier on toString(), this is used as the key
	// instead. Further, to reduce the size of the key and speed up matching on _openConn, we
	// remove the constant part of the String and leave only the object's memory address.
	private final static int _DBCP_CONN_PREFIX_LENGTH = "org.apache.commons.dbcp.PoolableConnection@".length();
	
	@SuppressWarnings("unchecked")
	// TODO with JDK 7: @SuppressWarnings("varargs")
	public static Repository load(Class<? extends Domain> ... classes) {
		Checker.checkEmpty(classes, "cs");
		Checker.checkNullElements(classes, "cs");
		// Check if any of the classes is already there:
		for(Class<? extends Domain> cs : classes) {
			if(_repositories.containsKey(cs)) {
				throw new IllegalArgumentException("domain class '"+cs.getSimpleName()+"' has already been loaded");
			}
		}
		// TODO refactor this code to ensure that loading happens in the correct order. Add logging
		// Add classes:
		Map<String,Entity<?,?>> entities = new HashMap<String,Entity<?,?>>();
		Repository r = new Repository(entities);
		for(Class<? extends Domain> cs : classes) {
			Deque<Class<? extends Domain>> deque = new ArrayDeque<Class<? extends Domain>>();
			// 
			while(cs != Domain.class) {
				_repositories.put(cs, r);
				deque.addFirst(cs);
				cs = (Class<? extends Domain>)cs.getSuperclass();
			}
			while(!deque.isEmpty()) {
				cs = deque.removeFirst();
				// Get all entities for class:
				for(Method m : cs.getDeclaredMethods()) {
					if(Entity.class.isAssignableFrom(m.getReturnType())) {
						try {
							Entity<?,?> e = (Entity<?,?>)m.invoke(null);
							// TODO check if they already exist and throw ex if so
							r._entities.put(e.getMetaData().getName().toLowerCase(), e);
							r._byTableName.put(e.getMetaData().getTableName().toLowerCase(), e);
						}
						catch(Exception ex) {
							throw new IntegrityException(ex);
						}
					}
				}
			}
		}
		// Load entities:
		for(Entity<?,?> e : r.getEntities()) {
			e.load(r);
			e.onLoad();
		}
		return r;
	}

	public static Repository getRepositoryFor(Class<? extends Domain> cs) {
		return _repositories.get(cs);
	}

	public static ICollection<Class<? extends Domain>> getDomains() {
		return new ICollection<Class<? extends Domain>>(_repositories.keySet());
	}
	
	// INSTANCE:
	private final Map<String,Entity<?,?>> _entities;
	private final Map<String,Entity<?,?>> _byTableName = new HashMap<String,Entity<?,?>>();
	private RepositoryConfig _config = null;
	private Database _details = null;
	private ConnectionPool _pool = null;
	private DatabaseDriver _driver = null;
	private final Map<MetaData,Cache> _caches = new HashMap<MetaData,Cache>();
	private final Map<String,Connection> _openConns = new HashMap<String,Connection>();
	private final JdbcPropertyCache _propertyCache = new JdbcPropertyCache();
	private int _numDatabaseHits = 0;
	private int _numCacheHits = 0;
	private boolean _connected = false;
	private boolean _disconnected = false;
	private volatile TimerTask _cacheDisable = null;
	private final Map<String,Select> _gQueries = new HashMap<String,Select>();
	private final Map<String,Select> _aQueries = new HashMap<String,Select>();
	// TODO move to config
	private boolean _trackAnonymousQueries = false;
	
	private Repository(Map<String,Entity<?,?>> entities) {
		_entities = entities;
	}
	
	public ICollection<Entity<?,?>> getEntities() {
		return new ICollection<Entity<?,?>>(_entities.values());
	}
	
	public Entity<?,?> getEntity(String name) {
		Checker.checkEmpty(name, "name");
		return _entities.get(name.toLowerCase());
	}

	public Entity<?,?> getEntityByTableName(String name) {
		Checker.checkEmpty(name, "name");
		return _byTableName.get(name.toLowerCase());
	}

	public boolean getTrackAnonymousQueries() {
		return _trackAnonymousQueries;
	}

	public Repository setTrackAnonymousQueries(boolean value) {
		_trackAnonymousQueries = value;
		return this;
	}

	public Select createSelect(String sql) {
		return createSelect(null, sql);
	}

	public Select createSelect(String queryName, String sql) {
		Checker.checkEmpty(sql, "sql");
		if(queryName != null) {
			Checker.checkEmpty(queryName, "queryName");
			if(_gQueries.containsKey(queryName)) {
				throw new IllegalArgumentException(queryName+": query already exists");
			}
		}
		if(queryName == null && _trackAnonymousQueries) {
			Select tmp = _aQueries.get(sql);
			if(tmp != null) {
				_config.getLogger().warn("anonymous query has already been called: "+sql);
				return tmp;
			}
			Select s = Parser.parseGlobal(queryName, sql, this);
			_aQueries.put(queryName, s);
			return s;
		}
		else {
			Select s = Parser.parseGlobal(queryName, sql, this);
			_gQueries.put(queryName, s);
			return s;
		}
	}

	public Map<String,Query> getGlobalQueries() {
		Map<String,Query> queries = new HashMap<String,Query>();
		for(Query q : _gQueries.values()) {
			queries.put(q.getFullName(), q);
		}
		return queries;
	}

	public Map<String,Query> getEntityQueries() {
		Map<String,Query> queries = new HashMap<String,Query>();
		for(Entity<?,?> e : getEntities()) {
			for(Query q : e.getQueries().values()) {
				queries.put(q.getFullName(), q);
			}
		}
		return queries;
	}

	public Map<String,Query> getNamedQueries() {
		Map<String,Query> queries = getGlobalQueries();
		queries.putAll(getEntityQueries());
		return queries;
	}

	public Set<Select> getAnonymousQueries() {
		return new HashSet<Select>(_aQueries.values());
	}

	// TODO database connection details must come as part of the config object
	public void connect(Database details, RepositoryConfig config) throws BLException {
		Checker.checkNull(details, "details");
		Checker.checkNull(config, "config");
		if(_connected) {
			throw new IllegalStateException("repository has already been initialized");
		}
		// Initialize objects:
		_config = config;
		_config.setRepository(this);
		_details = details;
		_pool = new ConnectionPool(_details);
		_pool.addPoolListener(new PoolListenerAdapter() {
			public void onDestroy(Object obj) {
				// We use the memory address of the connection as the hash key:
				Connection conn = _openConns.remove(obj.toString().substring(_DBCP_CONN_PREFIX_LENGTH));
				if(conn == null) {
					throw new IntegrityException();
				}
			}
		});
		// Load driver:
		DatabaseDriver driver = DriverRegistry.getLatestDriverFor(_details.getJdbcDriver());
		if(driver == null) {
			throw new DatabaseDriverNotFoundException(_details.getJdbcDriver().getName());
		}
		try {
			java.sql.Connection nativeConn = _pool.getConnection();
			DatabaseMetaData dmd = nativeConn.getMetaData();
			Version v = new Version(dmd.getDatabaseMajorVersion(), dmd.getDatabaseMinorVersion());
			nativeConn.close();
			driver = DriverRegistry.getDriverFor(_details.getJdbcDriver(), v);
			if(driver == null) {
				throw new DatabaseDriverNotFoundException(_details.getJdbcDriver().getName(), v);
			}
			// Ok, got the right database driver:
			_driver = driver;
			// Cache relevant JDBC driver properties:
			_propertyCache.nullsAreSortedAtEnd = dmd.nullsAreSortedAtEnd();
			_propertyCache.nullsAreSortedAtStart = dmd.nullsAreSortedAtStart();
			_propertyCache.nullsAreSortedHigh = dmd.nullsAreSortedHigh();
			_propertyCache.nullsAreSortedLow = dmd.nullsAreSortedLow();
		}
		catch(SQLException sqle) {
			// Note: we need to use the 'driver' variable here.
			// In case a SQLException is thrown when attempting to connect to the database,
			// the _driver variable will not be initialized and getDriver() returns null.
			throw driver.translateException(sqle, null, null);
		}
		// Check prepared statement usage:
		if(_config.getUsePreparedStatements() && !_driver.supportsPreparedStatements()) {
			_config.getLogger().warn("configuration is set to use prepared statements but driver "+_driver+" does not support them");
		}
		// Load caches:
		Connection c = _getConnection();
		//TODO Map<String,Object> cachePolicies = _localCfg.getMap(CFG_CACHE_POLICIES);
		Iterator<Entity<?,?>> it = _entities.values().iterator();
		while(it.hasNext()) {
			Entity<?,?> e = it.next();
			// Initialize a Cache object (this is created even if the cache is disabled):
			CacheConfig cfg = _config.getCacheConfigFor(e);
			Cache cache = new Cache(cfg, _config.getLogger());
			_caches.put(e.getMetaData(), cache);
			// Load cache data if necessary:
			if(_config.getCacheEnabled()) {
				_loadCache(c, cache, e);
			}
		}
		// Set property listener for cacheEnabled:
		Property prop = _config.getContext().getProperty(RepositoryConfig.CACHE_ENABLED);
		prop.setListener(new PropertyListener() {
			public void onBeforeSet(Context parent, String key, Object oldValue, Object newValue) throws BLException {
				// Enable cache:
				if(newValue.equals(Boolean.TRUE)) {
					_checkState();
					_checkCacheDisableFinished();
					if(oldValue.equals(Boolean.TRUE)) {
						throw new IllegalStateException("cache is already enabled");
					}
					Connection c = _getConnection();
					for(Entity<?,?> e : getEntities()) {
						_loadCache(c, _caches.get(e.getMetaData()), e);
					}
					c.close();
				}
				// Disable cache:
				else if(newValue.equals(Boolean.FALSE)){
					_checkState();
					_checkCacheDisableFinished();
					if(oldValue.equals(Boolean.FALSE)) {
						throw new IllegalStateException("cache is already disabled");
					}
				}
				else {
					throw new IntegrityException(newValue);
				}
			}
			
			public void onAfterSet(Context parent, String key, Object value) {
				// Disable cache:
				if(value.equals(Boolean.FALSE)) {
					// Schedule the cache disabling daemon:
					_cacheDisable = new DisableCacheDaemon(_config.getRepository());
					new Timer(true).schedule(_cacheDisable, _config.getDisableCacheDaemonDelay().milliseconds());
				}
			}
		});
		_connected = true;
		c.close();
	}
	
	public void disconnect() throws BLException {
		_caches.clear();
		_disconnected = true;
		// Close the connection pool (this will close all open
		// connections and force Repository to clear _openConns).
		try {
			_pool.close();
		}
		catch(SQLException sqle) {
			throw getDriver().translateException(sqle, null, null);
		}
		if(_openConns.size() != 0) {
			throw new IntegrityException(_openConns.size());
		}
	}

	// TODO should this be public?
	public Cache getCacheFor(MetaData m) {
		Checker.checkNull(m, "m");
		_checkState();
		return _caches.get(m);
	}
	
	public RepositoryConfig getConfig() {
		return _config;
	}

	public DatabaseDriver getDriver() {
		return _driver;
	}

	public Connection getConnection() throws BLException {
		_checkState();
		return _getConnection();
	}

	public int getDatabaseHitCount() {
		return _numDatabaseHits;
	}
	
	public int getCacheHitCount() {
		return _numCacheHits;
	}

	public int getTotalHitCount() {
		return _numDatabaseHits + _numCacheHits;
	}

	public boolean cacheDisableFinished() {
		return _cacheDisable == null;
	}
	
	public synchronized void waitForCacheDisable() {
		while(_cacheDisable != null) {
			Tools.sleep(500, _config.getLogger());
		}
	}

	// For DAFacade:
	// This allows the DAFacade to retrieve the appropriate
	// entity cache before the database is fully initialized.
	Cache getCacheDirectly(MetaData m) {
		return _caches.get(m);
	}

	// For DAFacade:
	JdbcPropertyCache getJdbcPropertyCache() {
		return _propertyCache;
	}

	// For DisableCacheDaemon:
	void notifyCacheDisableFinished() {
		_cacheDisable = null;
	}
	
	// For Query:
	void countDatabaseHit() {
		_numDatabaseHits++;
	}

	// For Query:
	void countCacheHit() {
		_numCacheHits++;
	}

	private void _checkState() {
		if(!_connected) {
			throw new IllegalStateException("repository has not been initialized yet");
		}
		if(_disconnected) {
			throw new IllegalStateException("repository has already been disconnected");
		}
	}

	private void _checkCacheDisableFinished() {
		if(_cacheDisable != null) {
			throw new IllegalStateException("a previous cache disable is still running");
		}
	}

	private Connection _getConnection() throws BLException {
		try {
			// Note: _pool.getConnection is thread-safe, no need for synchronization here.
			java.sql.Connection nativeConn = _pool.getConnection();
			Connection c = _openConns.get(nativeConn);
			if(c != null) {
				c.activate(nativeConn);
			}
			else {
				c = new Connection(this, nativeConn);
				// We use the memory address of the connection as the hash key:
				_openConns.put(nativeConn.toString().substring(_DBCP_CONN_PREFIX_LENGTH), c);
			}
			return c;
		}
		catch(SQLException sqle) {
			throw getDriver().translateException(sqle, null, null);
		}
	}
	
	private void _loadCache(Connection c, Cache cache, Entity<?,?> e) throws BLException {
		if(!cache.getConfig().cachePolicyNone()) {
			List<EntityInstance<?,?>> results = e.doInitialSelect(c, cache.getConfig());
			cache.init(results);
			cache.enableLock();
		}
	}
}
