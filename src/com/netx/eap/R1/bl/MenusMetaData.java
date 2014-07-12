package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class MenusMetaData extends TimedMetaData {

	// Fields:
	public final Field menuId = new FieldLong(this, "menuId", "menu_id", null, true, true, true, null, null);
	public final Field name = new FieldText(this, "name", "name", null, true, true, 0, 50, true, null, new Validators.ReadableText());
	public final Field listOrder = new FieldInt(this, "listOrder", "list_order", null, true, true, null, null);
	
	public MenusMetaData() {
		super("Menus", "eap_menus");
		addPrimaryKeyField(menuId);
		addField(name);
		addField(listOrder);
		addDefaultFields();
	}
	
	public final DATA_TYPE getDataType() {
		return DATA_TYPE.REFERENCE;
	}

	public Class<Menu> getInstanceClass() {
		return Menu.class;
	}
	
	public Field getAutonumberKeyField() {
		return menuId;
	}
}
