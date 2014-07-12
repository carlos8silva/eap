package com.netx.bl.R1.core;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.netx.generics.R1.collections.Chain;
import com.netx.generics.R1.collections.Node;
import com.netx.basic.R1.logging.Logger;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.shared.Globals;
import com.netx.bl.R1.core.FieldForeignKey.ON_DELETE_CONSTRAINT;


// TODO restrict access to methods which are not needed by specific DAFacades.

// Cloning of entity instances is controlled by this class. getInstance always returns
// a cloned EI, and putInstance always stores a clone of the EI.
public class Cache {

	// TODO make all private
	public final Map<PrimaryKey,EntityInstance<?,?>> _objects;
	private final CacheConfig _config;
	private final Logger _logger;
	public final Chain<EntityInstance<?,?>> _ctrl;
	private ReadWriteLock _lock;
	
	// For Repository:
	Cache(CacheConfig config, Logger logger) {
		_config = config;
		_logger = logger == null ? Globals.getLogger() : logger;
		_objects = new HashMap<PrimaryKey,EntityInstance<?,?>>();
		if(config.cachePolicyLimited()) {
			_ctrl = new Chain<EntityInstance<?,?>>();
		}
		else {
			_ctrl = null;
		}
		_lock = null;
	}

	public CacheConfig getConfig() {
		return _config;
	}

	public Lock getReadLock() {
		return _lock.readLock();
	}

	public Lock getWriteLock() {
		return _lock.writeLock();
	}

	// For Repository:
	void enableLock() {
		_lock = new ReentrantReadWriteLock();
	}

	// For DisableCacheDaemon:
	void disableLock() {
		// TODO can we kill all hanging threads?
		_lock = null;
	}

	public EntityInstance<?,?> getInstance(PrimaryKey primaryKey) {
		EntityInstance<?,?> ei = _objects.get(primaryKey);
		if(ei == null) {
			return null;
		}
		_log("cache hit", ei);
		if(_config.cachePolicyLimited()) {
			_ctrlUpdateAccessTime(ei);
		}
		return ei.clone();
	}

	// For Repository:
	// TODO for entities where caching is limited, this does not seem to limit
	// the amount of rows that get initialized
	void init(List<EntityInstance<?,?>> initialValues) {
		for(EntityInstance<?,?> ei : initialValues) {
			_objects.put(ei.getPrimaryKey(), ei);
			// Also initialize the control map:
			if(_config.cachePolicyLimited()) {
				_ctrlPutNew(ei);
			}
		}
	}

	public void putInstance(EntityInstance<?,?> ei) {
		// Log:
		_log("cache insert", ei);
		// Put a clone of the EI on cache:
		EntityInstance<?,?> cachedEI = ei.clone();
		_objects.put(ei.getPrimaryKey(), cachedEI);
		// Enforce any configured size limit:
		if(_config.cachePolicyLimited()) {
			_ctrlPutNew(cachedEI);
		}
	}

	// Updates an existing element in the cache, instead of putting a clone:
	public void updateInstance(EntityInstance<?,?> ei) {
		// Log:
		_log("cache update", ei);
		EntityInstance<?,?> cachedEI = _objects.get(ei.getPrimaryKey());
		if(cachedEI != null) {
			// There is no need to lock access to th EI itself: this method is only called
			// when a write lock is active. SELECT threads will be blocked trying to access 
			// the cache, so we can change the cached element directly.
			cachedEI.updateFrom(ei);
			// Update the EI's last access time:
			if(_config.cachePolicyLimited()) {
				// There are two possible situations. Either the EI has been retrieved from
				// cache and then updated, or it has been created on the application level
				// and then updated. If it has been retrieved, we do not move it up on the
				// chain because that has already been done by getInstance:
				if(ei.getChainNode() == null) {
					_ctrlUpdateAccessTime(cachedEI);
				}
			}
		}
	}

	public long updateList(Connection c, WhereExpr where, ValidatedArgument[] updateArgs, Argument[] whereArgs) throws BLException {
		Iterator<EntityInstance<?,?>> it = _objects.values().iterator();
		long numUpdates = 0;
		while(it.hasNext()) {
			EntityInstance<?,?> cachedEI = null;
			if(whereArgs == null) {
				cachedEI = it.next();
				cachedEI.updateFrom(updateArgs);
				numUpdates++;
			}
			else {
				cachedEI = it.next();
				if(where.evaluate(c, cachedEI, whereArgs)) {
					cachedEI.updateFrom(updateArgs);
					numUpdates++;
				}
			}
			// Update the EI's last access time:
			if(cachedEI != null && _config.cachePolicyLimited()) {
				_ctrlUpdateAccessTime(cachedEI);
			}
		}
		return numUpdates;
	}

	public boolean removeInstance(Connection c, PrimaryKey primKey) throws BLException {
		EntityInstance<?,?> ei = _objects.remove(primKey);
		if(ei != null) {
			_removeCtrlNode(ei);
			// Recursively remove linked entities from other caches:
			_updateLinkedEntities(c, ei);
			return true;
		}
		else {
			return false;
		}
	}
	
	public long removeList(Connection c, WhereExpr where, Argument[] whereArgs) throws BLException {
		Iterator<EntityInstance<?,?>> it = _objects.values().iterator();
		long numUpdates = 0;
		while(it.hasNext()) {
			EntityInstance<?,?> cachedEI = it.next();
			if(where.evaluate(c, cachedEI, whereArgs)) {
				it.remove();
				_removeCtrlNode(cachedEI);
				numUpdates++;
				// Recursively remove linked entities from other caches:
				_updateLinkedEntities(c, cachedEI);
			}
		}
		return numUpdates;
	}

	public EntityInstance<?,?> searchInstance(Connection c, WhereExpr where, Argument[] args) throws BLException {
		for(EntityInstance<?,?> ei : _objects.values()) {
			if(where.evaluate(c, ei, args)) {
				// Clone also copies associations.
				return ei.clone();
			}
		}
		return null;
	}

	public List<EntityInstance<?,?>> search(Connection c, WhereExpr where, Argument[] args, OrderBy[] orderBy, Limit limit) throws BLException {
		List<EntityInstance<?,?>> results = new ArrayList<EntityInstance<?,?>>();
		Iterator<EntityInstance<?,?>> it = _objects.values().iterator();
		for(int i=0; it.hasNext(); i++) {
			if(limit != null && orderBy == null) {
				if(limit.offset != null && i < limit.offset) {
					continue;
				}
				if(results.size() == limit.numRows) {
					break;
				}
			}
			EntityInstance<?,?> ei = it.next();
			if(where != null) {
				if(!where.evaluate(c, ei, args)) {
					continue;
				}
			}
			// Clone also copies associations.
			results.add(ei.clone());
		}
		// Sorting when there is no LIMIT clause is applied outside of this method 
		// so that memory locks are released before any sorting. However, when there
		// is also an ORDER BY clause, both sorting and limits must be applied before
		// returning the results.
		if(limit != null && orderBy != null) {
			Sorter.sort(results, orderBy, c.getRepository().getJdbcPropertyCache());
			int fromIndex = 0;
			if(limit.offset != null) {
				fromIndex = limit.offset;
			}
			int toIndex = limit.offset + limit.numRows;
			if(toIndex >= results.size()) {
				toIndex = results.size();
			}
			results = results.subList(fromIndex, toIndex);
		}
		return results;
	}

	public boolean isEmpty() {
		return _objects.isEmpty();
	}

	public void clear(Connection c) throws BLException {
		// TODO clear other caches as well
		for(EntityInstance<?,?> ei : _objects.values()) {
			_updateLinkedEntities(c, ei);
		}
		clearSelf();
	}

	// For DisableCacheDaemon:
	void clearSelf() {
		_objects.clear();
		if(_ctrl != null) {
			_ctrl.clear();
		}
	}

	private void _log(String msg, EntityInstance<?,?> ei) {
		if(_logger.getLevel().equals(Logger.LEVEL.INFO)) {
			// We check if the level is INFO to avoid running ei.toString() all the time
			_logger.info(msg+": "+ei);
		}
	}

	// We don't need synchronised access to each of the CTRL
	// methods because we have obtained a write lock already.
	private void _ctrlPutNew(EntityInstance<?,?> ei) {
		// We use a while here to guarantee the correct behaviour
		// in case the maximum size is decreased in the meantime:
		while(_objects.size() > _config.getCacheSize()) {
			// Get rid of the oldest EI in cache:
			EntityInstance<?,?> old = _ctrl.getHead().remove();
			if(_objects.remove(old.getPrimaryKey()) == null) {
				// Inconsistent cache:
				_clearAndExit();
			}
			_log("removed oldest instance from cache", old);
		}
		// Put this EI into the control chain:
		_putEiInCtrl(ei);
	}

	// We don't need synchronised access to each of the CTRL
	// methods because we have obtained a write lock already.
	private void _ctrlUpdateAccessTime(EntityInstance<?,?> ei) {
		// We need to move the chain's node from its
		// current location to the end of the chain:
		Node<EntityInstance<?,?>> current = ei.getChainNode();
		current.remove();
		_putEiInCtrl(ei);
	}

	private void _putEiInCtrl(EntityInstance<?,?> ei) {
		Node<EntityInstance<?,?>> node = null;
		if(_ctrl.isEmpty()) {
			node = _ctrl.add(ei);
		}
		else {
			node = _ctrl.getTail().addAfter(ei);
		}
		ei.setChainNode(node);
		// Just a check:
		if(_objects.size() != _ctrl.size()) {
			_clearAndExit();
		}
	}

	private void _removeCtrlNode(EntityInstance<?,?> ei) {
		_log("cache remove", ei);
		Node<?> node = ei.getChainNode();
		if(node != null) {
			node.remove();
		}
	}

	// This method is called in situations where the cache has been found
	// to be inconsistent. This really should not happen in a production
	// system, but if it does it will rollback any transaction and force the
	// cache to be reloaded to prevent inconsistencies.
	private void _clearAndExit() {
		clearSelf();
		throw new IntegrityException();
	}

	private void _updateLinkedEntities(Connection c, EntityInstance<?,?> ei) throws BLException {
		List<MetaData> linkedEntities = ei.getMetaData().getLinkedEntities();
		if(!linkedEntities.isEmpty()) {
			MetaData mDeleted = ei.getMetaData();
			for(MetaData mLinked : linkedEntities) {
				List<FieldForeignKey> fkList = mLinked.getForeignKeysTo(mDeleted);
				Cache cLinked = c.getRepository().getCacheFor(mLinked);
				// Create WHERE expression:
				WhereExpr where = WhereExpr.toExpr(fkList);
				// Create arguments for WHERE expression:
				Argument[] whereArgs = new Argument[fkList.size()];
				Iterator<FieldForeignKey> it = fkList.iterator();
				for(int i=0; it.hasNext(); i++) {
					FieldForeignKey f = it.next();
					whereArgs[i] = new Argument(f, ei.getValue(f.getForeignField()));
				}
				// Foreign key constraints are guaranteed to be the same across
				// all foreign key fields, so we only need to check the first one:
				FieldForeignKey fk = fkList.get(0);
				if(fk.getOnDeleteConstraint() == ON_DELETE_CONSTRAINT.CASCADE) {
					cLinked.removeList(c, where, whereArgs);
				}
				else if(fk.getOnDeleteConstraint() == ON_DELETE_CONSTRAINT.RESTRICT) {
					// This should not happen; the database constraint should
					// already have caused the commit operation to fail.
					throw new IntegrityException();
				}
				else {
					// Create arguments for UPDATE expression:
					ValidatedArgument[] updateArgs = new ValidatedArgument[fkList.size()];
					it = fkList.iterator();
					for(int i=0; it.hasNext(); i++) {
						FieldForeignKey f = it.next();
						Comparable<?> value = null;
						if(fk.getOnDeleteConstraint() == ON_DELETE_CONSTRAINT.SET_NULL) {
							value = Field.NULL;
						}
						else if(fk.getOnDeleteConstraint() == ON_DELETE_CONSTRAINT.SET_DEFAULT) {
							value = f.getDefault();
						}
						else {
							throw new IntegrityException(f.getOnDeleteConstraint());
						}
						updateArgs[i] = new ValidatedArgument(f, value);
					}
					cLinked.updateList(c, where, updateArgs, whereArgs);
				}
			}
		}
	}
}
