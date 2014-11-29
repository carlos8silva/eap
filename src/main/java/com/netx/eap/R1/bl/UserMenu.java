package com.netx.eap.R1.bl;
import java.util.List;
import java.util.ArrayList;


public class UserMenu {

	public final Menu menu;
	public final List<MenuItem> menuItems;
	
	public UserMenu(Menu menu) {
		this.menu = menu;
		menuItems = new ArrayList<MenuItem>();
	}
	
	public int hashCode() {
		return menu.getPrimaryKey().hashCode();
	}
	
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		if(o == this) {
			return true;
		}
		if(!(o instanceof UserMenu)) {
			return false;
		}
		return menu.equals(((UserMenu)o).menu);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(menu.getName());
		sb.append(": ");
		for(MenuItem item : menuItems) {
			sb.append(item.getName());
			sb.append(" ");
		}
		return sb.toString();
	}
}
