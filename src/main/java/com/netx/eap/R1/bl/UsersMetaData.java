package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class UsersMetaData extends TimedMetaData {

	// Fields:
	public final Field userId = new FieldLong(this, "userId", "user_id", null, true, true, true, null, null);
	public final Field username = new FieldText(this, "username", "username", null, true, false, 2, 20, true, null, new Validators.Username());
	public final Field oldUsername = new FieldText(this, "oldUsername", "old_username", null, false, false, 2, 20, true, null, new Validators.Username());
	public final Field password = new FieldText(this, "password", "password", null, true, false, 6, 20, false, null, new Validators.Password());
	public final Field firstName = new FieldText(this, "firstName", "first_name", null, true, false, 0, 50, true, null, new Validators.AlphaText());
	public final Field lastName = new FieldText(this, "lastName", "last_name", null, true, false, 0, 50, true, null, new Validators.AlphaText());
	public final Field middleInitial = new FieldChar(this, "middleInitial", "middle_initial", null, false, false, true);
	public final Field helpOn = new FieldBoolean(this, "helpOn", "help_on", new Boolean(true), true, false);
	public final Field timeLocked = new FieldDateTime(this, "timeLocked", "time_locked", null, false, false);
	public final Field timeDisabled = new FieldDateTime(this, "timeDisabled", "time_disabled", null, false, false);
	// TODO add validator
	public final Field lockedReason = new FieldText(this, "lockedReason", "locked_reason", null, false, false, 2, 2, false, null, null);
	public final Field failedLoginAttempts = new FieldInt(this, "failedLoginAttempts", "failed_login_attempts", new Integer(0), true, false, null, null);
	public final Field sessionTimeoutTime = new FieldInt(this, "sessionTimeoutTime", "session_timeout_time", new Integer(10), false, false, null, null);
	public final Field passwordChangeTime = new FieldDateTime(this, "passwordChangeTime", "password_change_time", null, false, false);
	public final Field securityQuestion1 = new FieldInt(this, "securityQuestion1", "security_question_1", null, false, false, null, null);
	public final Field securityQuestion2 = new FieldInt(this, "securityQuestion2", "security_question_2", null, false, false, null, null);
	public final Field securityAnswer1 = new FieldText(this, "securityAnswer1", "security_answer_1", null, false, false, 0, 50, true, null, new Validators.ReadableText());
	public final Field securityAnswer2 = new FieldText(this, "securityAnswer2", "security_answer_2", null, false, false, 0, 50, true, null, new Validators.ReadableText());

	public UsersMetaData() {
		super("Users", "eap_users");
		addPrimaryKeyField(userId);
		addField(username);
		addField(oldUsername);
		addField(password);
		addField(firstName);
		addField(lastName);
		addField(middleInitial);
		addField(helpOn);
		addField(timeLocked);
		addField(timeDisabled);
		addField(lockedReason);
		addField(failedLoginAttempts);
		addField(sessionTimeoutTime);
		addField(passwordChangeTime);
		addField(securityQuestion1);
		addField(securityQuestion2);
		addField(securityAnswer1);
		addField(securityAnswer2);
		addDefaultFields();
		addUnique(username);
	}
	
	public final DATA_TYPE getDataType() {
		return DATA_TYPE.REFERENCE;
	}

	public Class<User> getInstanceClass() {
		return User.class;
	}

	public Field getAutonumberKeyField() {
		return userId;
	}
}
