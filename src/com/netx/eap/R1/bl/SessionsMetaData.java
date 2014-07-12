package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class SessionsMetaData extends TimedMetaData {

	// Fields:
	public final Field sessionId = new FieldText(this, "sessionId", "session_id", null, true, true, 0, 12, false, null, null);
	public final Field userId = new FieldForeignKey(this, "userId", "user_id", null, true, true, EAP.getUsers().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.CASCADE);
	public final Field ipAddress = new FieldText(this, "ipAddress", "ip_address", null, true, true, 0, 15, false, null, null);
	public final Field browser = new FieldText(this, "browser", "browser", null, true, true, 0, 2, false, null, null);
	public final Field startTime = new FieldDateTime(this, "startTime", "start_time", "now", true, true);
	public final Field endTime = new FieldDateTime(this, "endTime", "end_time", null, false, false);
	// TODO add validator
	public final Field endReason = new FieldText(this, "endReason", "end_reason", null, false, false, 2, 2, false, null, null);
	// TODO change text size to 16M
	public final Field endMessage = new FieldText(this, "endMessage", "end_message", null, false, false, 0, 500, false, null, null);

	public SessionsMetaData() {
		super("Sessions", "eap_sessions");
		addPrimaryKeyField(sessionId);
		addField(userId);
		addField(ipAddress);
		addField(browser);
		addField(startTime);
		addField(endTime);
		addField(endReason);
		addField(endMessage);
		addDefaultFields();
	}
	
	public final DATA_TYPE getDataType() {
		return DATA_TYPE.TRANSACTIONAL;
	}

	public Class<Session> getInstanceClass() {
		return Session.class;
	}
	
	public Field getAutonumberKeyField() {
		return null;
	}
}
