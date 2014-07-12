package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class MenuItemsMetaData extends TimedMetaData {

	// Fields:
	public final Field menuId = new FieldLong(this, "menuId", "menu_id", null, true, true, true, null, null);
	public final Field name = new FieldText(this, "name", "name", null, true, true, 0, 50, true, null, new Validators.ReadableText());
	public final Field functionId = new FieldForeignKey(this, "functionId", "function_id", null, false, true, EAP.getFunctions().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.RESTRICT);
	public final Field functionArgs = new FieldText(this, "functionArgs", "function_args", null, false, true, 0, 100, true, null, new Validators.ReadableText());
	public final Field listOrder = new FieldInt(this, "listOrder", "list_order", null, true, true, null, null);
	
	public MenuItemsMetaData() {
		super("MenuItems", "eap_menu_items");
		addPrimaryKeyField(menuId);
		addPrimaryKeyField(name);
		addField(functionId);
		addField(functionArgs);
		addField(listOrder);
		addDefaultFields();
	}
	
	public final DATA_TYPE getDataType() {
		return DATA_TYPE.REFERENCE;
	}

	public Class<MenuItem> getInstanceClass() {
		return MenuItem.class;
	}

	public Field getAutonumberKeyField() {
		return menuId;
	}
}
