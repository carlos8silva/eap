package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class Menu extends TimedInstance<MenusMetaData,Menus> {

	public Menu() {
	}
	
	public Menu(Long menuId) throws ValidationException {
		setPrimaryKey(getMetaData().menuId, menuId);
	}
	
	public Menus getEntity() {
		return Menus.getInstance();
	}

	public Long getMenuId() {
		return (Long)getValue(getMetaData().menuId);
	}

	public String getName() {
		return (String)getValue(getMetaData().name);
	}

	public Integer getListOrder() {
		return (Integer)getValue(getMetaData().listOrder);
	}
}
