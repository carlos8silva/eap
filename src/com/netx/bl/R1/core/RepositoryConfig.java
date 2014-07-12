package com.netx.bl.R1.core;
import com.netx.generics.R1.time.TimeValue;
import com.netx.basic.R1.logging.Logger;
import com.netx.config.R1.Context;
import com.netx.config.R1.ContextWrapper;


public class RepositoryConfig extends ContextWrapper {

	// TYPE:
	public final static String REPOSITORY_CONFIG_ID = "bl";
	public final static String CACHE_ENABLED = "cache-enabled";
	public final static String CACHE_POLICIES = "cache-policies";
	public final static String NUMBER_OF_LOCK_ATTEMPTS_BEFORE_FAILURE = "number-of-lock-attempts-before-failure";
	public final static String NUMBER_OF_LOCK_ATTEMPTS_BEFORE_RELEASE = "number-of-lock-attempts-before-release";
	public final static String SLEEP_TIME_BEFORE_LOCK_RETRY = "sleep-time-before-lock-retry";
	public final static String SLEEP_TIME_AFTER_LOCK_RELEASE = "sleep-time-after-lock-release";
	public final static String DISABLE_CACHE_DAEMON_DELAY = "disable-cache-daemon-delay";
	public final static String LOGGER = "logger";
	public final static String USE_PREPARED_STATEMENTS = "use-prepared-statements";

	// INSTANCE:
	private Repository _rep;
	
	// Initialize the config context from an external source:
	public RepositoryConfig(Context ctx) {
		super(ctx, REPOSITORY_CONFIG_ID);
	}

	// Create a config context initialized to defaults:
	public RepositoryConfig() {
		this(new Context(REPOSITORY_CONFIG_ID));
	}

	public Repository getRepository() {
		return _rep;
	}

	public Logger getLogger() {
		return (Logger)getContext().getObject(LOGGER);
	}

	public void setLogger(Logger value) {
		getContext().setProperty(LOGGER, value);
	}

	public boolean getCacheEnabled() {
		return getContext().getBoolean(CACHE_ENABLED);
	}

	public RepositoryConfig setCacheEnabled(boolean value) {
		getContext().setProperty(CACHE_ENABLED, value);
		return this;
	}

	public CacheConfig getCacheConfigFor(Entity<?,?> e) {
		CacheConfig config = (CacheConfig)getContext().getMapObject(CACHE_POLICIES, e.getMetaData().getName());
		if(config == null) {
			config = new CacheConfig(e.getMetaData().getDataType());
			getContext().setProperty(CACHE_POLICIES, e.getMetaData().getName(), config);
		}
		return config;
	}

	public TimeValue getDisableCacheDaemonDelay() {
		return (TimeValue)getContext().getObject(DISABLE_CACHE_DAEMON_DELAY);
	}

	public RepositoryConfig setDisableCacheDaemonDelay(TimeValue value) {
		getContext().setProperty(DISABLE_CACHE_DAEMON_DELAY, value);
		return this;
	}

	public int getNumberOfLockAttemptsBeforeFailure() {
		return getContext().getInteger(NUMBER_OF_LOCK_ATTEMPTS_BEFORE_FAILURE);
	}

	public RepositoryConfig setNumberOfLockAttemptsBeforeFailure(int value) {
		getContext().setProperty(NUMBER_OF_LOCK_ATTEMPTS_BEFORE_FAILURE, value);
		return this;
	}

	public int getNumberOfLockAttemptsBeforeRelease() {
		return getContext().getInteger(NUMBER_OF_LOCK_ATTEMPTS_BEFORE_RELEASE);
	}

	public RepositoryConfig setNumberOfLockAttemptsBeforeRelease(int value) {
		getContext().setProperty(NUMBER_OF_LOCK_ATTEMPTS_BEFORE_RELEASE, value);
		return this;
	}

	public TimeValue getSleepTimeBeforeLockRetry() {
		return (TimeValue)getContext().getObject(SLEEP_TIME_BEFORE_LOCK_RETRY);
	}

	public RepositoryConfig setSleepTimeBeforeLockRetry(TimeValue value) {
		getContext().setProperty(SLEEP_TIME_BEFORE_LOCK_RETRY, value);
		return this;
	}
	
	public TimeValue getSleepTimeAfterLockRelease() {
		return (TimeValue)getContext().getObject(SLEEP_TIME_AFTER_LOCK_RELEASE);
	}

	public RepositoryConfig setSleepTimeAfterLockRelease(TimeValue value) {
		getContext().setProperty(SLEEP_TIME_AFTER_LOCK_RELEASE, value);
		return this;
	}

	public boolean getUsePreparedStatements() {
		return getContext().getBoolean(USE_PREPARED_STATEMENTS);
	}

	public RepositoryConfig setUsePreparedStatements(boolean value) {
		getContext().setProperty(USE_PREPARED_STATEMENTS, value);
		return this;
	}
	
	// For Repository:
	void setRepository(Repository rep) {
		_rep = rep;
	}
}
