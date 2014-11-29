package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;
import com.netx.generics.R1.util.ByteValue;
import com.netx.generics.R1.util.ByteValue.MEASURE;


public class PreferencesMetaData extends TimedMetaData {

	// Fields:
	public final Field userId = new FieldForeignKey(this, "userId", "user_id", null, true, true, EAP.getUsers().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.CASCADE);
	public final Field property = new FieldText(this, "property", "property", null, true, true, 0, 100, true, null, null); // TODO need to add text identifier
	public final Field value = new FieldText(this, "value", "value", null, true, false, 0, (long)ByteValue.convert(16, MEASURE.MEGABYTES, MEASURE.BYTES), false, null, null);

	public PreferencesMetaData() {
		super("Preferences", "eap_preferences");
		addPrimaryKeyField(userId);
		addPrimaryKeyField(property);
		addField(value);
		addDefaultFields();
	}
	
	public final DATA_TYPE getDataType() {
		return DATA_TYPE.TRANSACTIONAL;
	}

	public Class<Preference> getInstanceClass() {
		return Preference.class;
	}

	public Field getAutonumberKeyField() {
		return null;
	}
}
