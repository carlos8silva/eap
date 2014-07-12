package com.netx.rit.R1.bl;
import com.netx.bl.R1.core.*;
import com.netx.eap.R1.bl.*;


public class ItemsMetaData extends TimedMetaData {

	// Fields:
	public final Field itemId = new FieldLong(this, "itemId", "item_id", null, true, true, true, null, null);
	public final Field projectId = new FieldForeignKey(this, "projectId", "project_id", null, true, true, RIT.getProjects().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.CASCADE);
	public final Field title = new FieldText(this, "title", "title", null, true, false, 0, 200, true, null, new Validators.ReadableText());
	public final Field dateDue = new FieldDate(this, "dateDue", "date_due", null, false, false);
	
	public ItemsMetaData() {
		super("Items", "rit_items");
		addPrimaryKeyField(itemId);
		addField(projectId);
		addField(title);
		addField(dateDue);
		addDefaultFields();
	}
	
	public final DATA_TYPE getDataType() {
		return DATA_TYPE.TRANSACTIONAL;
	}

	public Class<Item> getInstanceClass() {
		return Item.class;
	}
	
	public Field getAutonumberKeyField() {
		return itemId;
	}
}
