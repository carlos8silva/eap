package com.netx.eap.R1.bl;
import java.io.StringReader;
import com.netx.generics.R1.time.Timestamp;
import com.netx.bl.R1.core.*;


public class UserEvent extends TimedInstance<UserEventsMetaData,UserEvents> {

	public UserEvent() {
	}

	public UserEvent(Long eventId) throws ValidationException {
		setPrimaryKey(getMetaData().eventId, eventId);
	}

	public UserEvents getEntity() {
		return UserEvents.getInstance();
	}

	public Long getEventId() {
		return (Long)getValue(getMetaData().eventId);
	}

	public Timestamp getTime() {
		return (Timestamp)getValue(getMetaData().time);
	}

	public UserEvent setTime(Timestamp value) throws ValidationException {
		safelySetValue(getMetaData().time, value);
		return this;
	}

	public String getSessionId() {
		return (String)getValue(getMetaData().sessionId);
	}

	public Session getSession(Connection c) throws BLException {
		String sessionId = (String)getValue(getMetaData().sessionId);
		if(sessionId == null) {
			return null;
		}
		else {
			return Sessions.getInstance().get(c, sessionId);
		}
	}

	public UserEvent setSessionId(String value) throws ValidationException {
		safelySetValue(getMetaData().sessionId, value);
		return this;
	}
	
	public UserEvent setSession(Session value) throws ValidationException {
		return setSessionId(value == null ? null : value.getSessionId());
	}

	public String getServerAddress() {
		return (String)getValue(getMetaData().serverAddress);
	}

	public UserEvent setServerAddress(String value) throws ValidationException {
		safelySetValue(getMetaData().serverAddress, value);
		return this;
	}

	public String getClientAddress() {
		return (String)getValue(getMetaData().clientAddress);
	}

	public UserEvent setClientAddress(String value) throws ValidationException {
		safelySetValue(getMetaData().clientAddress, value);
		return this;
	}

	public String getBrowser() {
		return (String)getValue(getMetaData().browser);
	}

	public UserEvent setBrowser(String value) throws ValidationException {
		safelySetValue(getMetaData().browser, value);
		return this;
	}

	public Integer getTypeId() {
		return (Integer)getValue(getMetaData().type);
	}

	public UserEventType getType(Connection c) throws BLException {
		Integer type = (Integer)getValue(getMetaData().type);
		if(type == null) {
			return null;
		}
		else {
			return UserEventTypes.getInstance().get(c, type);
		}
	}

	public UserEvent setTypeId(Integer value) throws ValidationException {
		safelySetValue(getMetaData().type, value);
		return this;
	}
	
	public UserEvent setType(UserEventType value) throws ValidationException {
		return setTypeId(value == null ? null : value.getEventTypeId());
	}

	public String getPage() {
		return (String)getValue(getMetaData().page);
	}

	public UserEvent setPage(String value) throws ValidationException {
		safelySetValue(getMetaData().page, value);
		return this;
	}

	public String getFunctionId() {
		return (String)getValue(getMetaData().functionId);
	}

	public FunctionInstance getFunction(Connection c) throws BLException {
		String functionId = getFunctionId();
		if(functionId == null) {
			return null;
		}
		else {
			return Functions.getInstance().get(c, functionId);
		}
	}

	public UserEvent setFunction(String value) throws ValidationException {
		safelySetValue(getMetaData().functionId, value);
		return this;
	}
	
	public UserEvent setFunction(FunctionInstance value) throws ValidationException {
		return setSessionId(value == null ? null : value.getAlias());
	}

	public String getDetails() {
		return (String)getValue(getMetaData().details);
	}

	public UserEvent setDetails(String value) throws ValidationException {
		// TODO details should be converted to a stream
		safelySetValue(getMetaData().details, value);
		return this;
	}

	public UserEvent setDetails(StringReader value) throws ValidationException {
		// TODO impl
		return this;
	}

	// TODO this should return a BufferedReader
	public String getStackTrace() {
		return (String)getValue(getMetaData().stack_trace);
	}

	public UserEvent setStackTrace(String value) throws ValidationException {
		// TODO stack trace should be converted to a stream
		safelySetValue(getMetaData().stack_trace, value);
		return this;
	}

	public UserEvent setStackTrace(StringReader value) throws ValidationException {
		// TODO impl
		return this;
	}
}
