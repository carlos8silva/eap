package com.netx.bl.R1.core;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


class LockUtils {

	public static void obtainLocksFor(Repository rep, Collection<MetaData> entities) throws CacheLockTimeoutException {
		boolean locksAcquired = false;
		int totalNumTries = 0;
		OuterLoop:
		while(totalNumTries < rep.getConfig().getNumberOfLockAttemptsBeforeFailure()) {
			List<MetaData> participants = new ArrayList<MetaData>();
			participants.addAll(entities);
			List<MetaData> locked = new ArrayList<MetaData>();
			int numTries = 0;
			while(numTries < rep.getConfig().getNumberOfLockAttemptsBeforeRelease()) {
				Iterator<MetaData> it = participants.iterator();
				while(it.hasNext()) {
					MetaData m = it.next();
					Cache cache = rep.getCacheFor(m);
					// Skip caches which are configured not to hold data in memory:
					if(cache.getConfig().cachePolicyNone()) {
						it.remove();
						locked.add(m);
					}
					else {
						if(cache.getWriteLock().tryLock()) {
							it.remove();
							locked.add(m);
						}
					}
				}
				if(participants.isEmpty()) {
					locksAcquired = true;
					break OuterLoop;
				}
				else {
					numTries++;
	                // This gives other threads some time to release
	                // locks before we try to lock again:
					try {
						Thread.sleep(rep.getConfig().getSleepTimeBeforeLockRetry().milliseconds());
					}
					catch(InterruptedException ie) {
						// noop;
					}
				}
			}
	        // If we have reached this point, we have exceeded our number of tries and
	        // need to allow some time for other transactions to release their locks:
			for(MetaData m : locked) {
				rep.getCacheFor(m).getWriteLock().unlock();
			}
			// Sleep for some time:
			try {
				Thread.sleep(rep.getConfig().getSleepTimeAfterLockRelease().milliseconds());
			}
			catch(InterruptedException ie) {
				// noop;
			}
			totalNumTries++;
		}
		// If we have not managed to obtain all the locks after the
		// configured maximum number of attempts, we need to fail (which
		// will rollback the transaction and release all write locks):
		if(!locksAcquired) {
			throw new CacheLockTimeoutException();
		}
	}

	// This will attempt to release all locks for the specified
	// entities, without throwing any exceptions.
	public static void releaseLocksFor(Repository rep, Collection<MetaData> entities) {
		for(MetaData m : entities) {
			Cache cache = rep.getCacheFor(m);
			try {
				if(!cache.getConfig().cachePolicyNone()) {
					// Attempt to unlock:
					cache.getWriteLock().unlock();
				}
			}
			catch(IllegalMonitorStateException ime) {
				// This can happen if the lock is held by another thread.
				// We just log a warning for reference:
				rep.getConfig().getLogger().warn("attempted to unlock a wrong write lock", ime);
			}
		}
	}
}
