package com.netx.bl.R1.core;
import com.netx.basic.R1.eh.IntegrityException;


public class CacheConfig {

	// TYPE:
	public static enum CACHE_POLICY {
		NONE,
		LIMITED,
		FULL
	}
	
	// INSTANCE:
	private CACHE_POLICY _cachePolicy;
	private int _cacheSize;
	
	// Default constructor, if there is no explicit configuration:
	CacheConfig(MetaData.DATA_TYPE dataType) {
		if(dataType.equals(MetaData.DATA_TYPE.REFERENCE)) {
			_cachePolicy = CACHE_POLICY.FULL;
			_cacheSize = -1;
		}
		else if(dataType.equals(MetaData.DATA_TYPE.TRANSACTIONAL)) {
			_cachePolicy = CACHE_POLICY.LIMITED;
			_cacheSize = 10000;
		}
		else {
			throw new IntegrityException(dataType);
		}
	}
	
	// Used when the cache policy is specified in a configuration file:
	CacheConfig(CACHE_POLICY cachePolicy, int cacheSize) {
		if(cachePolicy.equals(CACHE_POLICY.FULL) && cacheSize != -1) {
			throw new IntegrityException(cacheSize);
		}
		if(cachePolicy.equals(CACHE_POLICY.NONE) && cacheSize != 0) {
			throw new IntegrityException(cacheSize);
		}
		if(cachePolicy.equals(CACHE_POLICY.LIMITED) && cacheSize <= 0) {
			throw new IntegrityException(cacheSize);
		}
		_cachePolicy = cachePolicy;
		_cacheSize = cacheSize;
	}

	public int getCacheSize() {
		return _cacheSize;
	}

	public boolean cachePolicyNone() {
		return _cachePolicy.equals(CACHE_POLICY.NONE);
	}

	public boolean cachePolicyFull() {
		return _cachePolicy.equals(CACHE_POLICY.FULL);
	}

	public boolean cachePolicyLimited() {
		return _cachePolicy.equals(CACHE_POLICY.LIMITED);
	}
	
	public String toString() {
		if(cachePolicyLimited()) {
			return _cachePolicy+"("+_cacheSize+")";
		}
		else {
			return _cachePolicy.toString();
		}
	}
}
