package com.netx.eap.R1.bl;
import java.util.List;
import com.netx.bl.R1.core.*;


public class MenuItems extends Entity<MenuItemsMetaData,MenuItem> {

	// TYPE:
	public static MenuItems getInstance() {
		return EAP.getMenuItems();
	}

	// INSTANCE:
	MenuItems() {
		super(new MenuItemsMetaData());
	}
	
	public List<MenuItem> listMenuItems(Connection c) throws BLException {
		return selectAll(c);
	}
}
