package com.netx.ut.bl.R1.core;
import com.netx.bl.R1.core.*;


public class UsersMetaData extends TimedMetaData {

	// Fields:
	public final Field name = new FieldText(this, "name", "name", null, true, true, 0, 50, true, null, null);
	public final Field city = new FieldText(this, "city", "city", null, true, false, 0, 50, false, null, null);
	public final Field age = new FieldInt(this, "age", "age", null, true, false, null, null);
	public final Field username = new FieldText(this, "username", "username", null, true, true, 0, 15, true, null, new Validators.Username());

	public UsersMetaData() {
		super("Users", "users");
		addPrimaryKeyField(name);
		addField(city);
		addField(age);
		addField(username);
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
		return null;
	}
}
