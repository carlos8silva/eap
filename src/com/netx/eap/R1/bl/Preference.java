package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class Preference extends TimedInstance<PreferencesMetaData,Preferences> {

	public Preference(Long userId, String property) throws ValidationException {
		setPrimaryKey(getMetaData().userId, userId);
		setPrimaryKey(getMetaData().property, property);
	}

	public Preferences getEntity() {
		return Preferences.getInstance();
	}

	public Long getUserId() {
		return (Long)getValue(getMetaData().userId);
	}

	public User getUser(Connection c) throws BLException{
		Long id = getUserId();
		return id == null ? null : Users.getInstance().get(c, id);
	}

	public String getProperty() {
		return (String)getValue(getMetaData().property);
	}

	public String getValue() {
		return (String)getValue(getMetaData().value);
	}

	public Preference setValue(String value) throws ValidationException {
		safelySetValue(getMetaData().value, value);
		return this;
	}
}
