package com.netx.config.R1;


public interface PropertyListener {

	public void onBeforeSet(Context parent, String key, Object oldValue, Object newValue) throws Exception;
	public void onAfterSet(Context parent, String key, Object value) throws Exception;
}
