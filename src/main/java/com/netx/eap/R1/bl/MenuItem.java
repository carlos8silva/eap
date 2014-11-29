package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class MenuItem extends TimedInstance<MenuItemsMetaData,MenuItems> {
	
	public MenuItem(Long menuId, String name) throws ValidationException {
		setPrimaryKey(getMetaData().menuId, menuId);
		setPrimaryKey(getMetaData().name, name);
	}
	
	public MenuItems getEntity() {
		return MenuItems.getInstance();
	}

	public Long getMenuId() {
		return (Long)getValue(getMetaData().menuId);
	}

	public Menu getMenu(Connection c) throws BLException {
		Long id = getMenuId();
		if(id == null) {
			return null;
		}
		return Menus.getInstance().get(c, id);
	}

	public String getName() {
		return (String)getValue(getMetaData().name);
	}

	public String getFunctionId() {
		return (String)getValue(getMetaData().functionId);
	}

	public FunctionInstance getFunction(Connection c) throws BLException {
		String id = getFunctionId();
		if(id == null) {
			return null;
		}
		return Functions.getInstance().get(c, id);
	}

	public String getFunctionArgs() {
		return (String)getValue(getMetaData().functionArgs);
	}

	public Integer getListOrder() {
		return (Integer)getValue(getMetaData().listOrder);
	}
}
