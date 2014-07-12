package com.netx.bl.R1.core;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.TimerTask;


public class DisableCacheDaemon extends TimerTask {

	private final Repository _rep;
	
	public DisableCacheDaemon(Repository rep) {
		_rep = rep;
	}
	
	public void run() {
		try {
			// Obtain write locks for all entities:
			Collection<Entity<?,?>> entities = _rep.getEntities();
			List<MetaData> mdList = new ArrayList<MetaData>(entities.size());
			for(Entity<?,?> e : entities) {
				mdList.add(e.getMetaData());
			}
			LockUtils.obtainLocksFor(_rep, mdList);
			// Clear all caches:
			for(MetaData m : mdList) {
				Cache cache = _rep.getCacheFor(m); 
				cache.clearSelf();
				if(!cache.getConfig().cachePolicyNone()) {
					cache.getWriteLock().unlock();
					cache.disableLock();
				}
			}
		}
		catch(Exception e) {
			_rep.getConfig().getLogger().error(e);
		}
		finally {
			// Let the database know that the disable has finished:
			_rep.notifyCacheDisableFinished();
		}
	}
}
