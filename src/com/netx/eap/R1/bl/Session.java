package com.netx.eap.R1.bl;
import com.netx.generics.R1.time.Timestamp;
import com.netx.bl.R1.core.*;


public class Session extends TimedInstance<SessionsMetaData,Sessions> {

	// TYPE:
	public static enum EndReason implements AllowedValue<String> {
		LOGGED_OUT("LG"), ABORTED("AB"), FORCED_OUT_SA("FA"), FORCED_OUT_SYS("FY"), STOPPED("ST"), IP_ADDRESS("IP"), TIMED_OUT("TO"), PASSWORD("PT");
		private final String _value;
		private EndReason(String value) { _value = value; }
		public String getCode() { return _value; }
	};
	
	// INSTANCE:
	public Session(String sessionId) throws ValidationException {
		setPrimaryKey(getMetaData().sessionId, sessionId);
	}

	public Sessions getEntity() {
		return Sessions.getInstance();
	}

	public String getSessionId() {
		return (String)getValue(getMetaData().sessionId);
	}

	public Long getUserId() {
		return (Long)getValue(getMetaData().userId);
	}

	public User getUser(Connection c) throws BLException {
		Long userId = (Long)getValue(getMetaData().userId);
		if(userId == null) {
			return null;
		}
		else {
			return Users.getInstance().get(c, userId);
		}
	}

	public Session setUserId(Long value) throws ValidationException {
		safelySetValue(getMetaData().userId, value);
		return this;
	}
	
	public Session setUser(User value) throws ValidationException {
		return setUserId(value == null ? null : value.getUserId());
	}

	public String getIpAddress() {
		return (String)getValue(getMetaData().ipAddress);
	}

	public Session setIpAddress(String value) throws ValidationException {
		safelySetValue(getMetaData().ipAddress, value);
		return this;
	}

	public String getBrowser() {
		return (String)getValue(getMetaData().browser);
	}

	public Session setBrowser(String value) throws ValidationException {
		safelySetValue(getMetaData().browser, value);
		return this;
	}

	public Timestamp getStartTime() {
		return (Timestamp)getValue(getMetaData().startTime);
	}

	public Session setStartTime(Timestamp value) throws ValidationException {
		safelySetValue(getMetaData().startTime, value);
		return this;
	}

	public Timestamp getEndTime() {
		return (Timestamp)getValue(getMetaData().endTime);
	}

	public Session setEndTime(Timestamp value) throws ValidationException {
		safelySetValue(getMetaData().endTime, value);
		return this;
	}

	public String getEndReasonId() {
		return (String)getValue(getMetaData().endReason);
	}

	public EndReason getEndReason() throws BLException {
		String endReason = (String)getValue(getMetaData().endReason);
		return (EndReason)getAllowedValue(EndReason.class, endReason);
	}

	public Session setEndReason(String value) throws ValidationException {
		safelySetValue(getMetaData().endReason, value);
		return this;
	}
	
	public Session setEndReason(EndReason value) throws ValidationException {
		return setEndReason(value == null ? null : value.getCode());
	}	

	public String getEndMessage() {
		return (String)getValue(getMetaData().endMessage);
	}

	public Session setEndMessage(String value) throws ValidationException {
		safelySetValue(getMetaData().endMessage, value);
		return this;
	}
}
