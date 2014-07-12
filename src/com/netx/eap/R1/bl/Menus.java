package com.netx.eap.R1.bl;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import com.netx.bl.R1.core.*;
import com.netx.eap.R1.core.UserSession;
import com.netx.generics.R1.collections.Collections;


public class Menus extends Entity<MenusMetaData,Menu> {

	// TYPE:
	public static Menus getInstance() {
		return EAP.getMenus();
	}

	// INSTANCE:
	Menus() {
		super(new MenusMetaData());
	}
	
	public List<UserMenu> listMenuFor(Connection c, UserSession session) throws BLException {
		List<UserMenu> menus = new ArrayList<UserMenu>();
		List<MenuItem> menuItems = MenuItems.getInstance().listMenuItems(c);
		// Get all menus and menu items that can be used by this user as per permissions cached in the session:
		for(MenuItem menuItem : menuItems) {
			boolean canBeAdded = false;
			Permission p = menuItem.getFunction(c).getPermission(c);
			if(session.getRolePermissions().containsKey(p.getPermissionId())) {
				canBeAdded = true;
			}
			if(!canBeAdded) {
				if(session.getUserPermissions().containsKey(p.getPermissionId())) {
					canBeAdded = true;
				}
			}
			if(canBeAdded) {
				UserMenu tmp = new UserMenu(menuItem.getMenu(c));
				UserMenu userMenu = (UserMenu)Collections.find(menus, tmp);
				if(userMenu == null) {
					userMenu = tmp;
					menus.add(userMenu);
				}
				userMenu.menuItems.add(menuItem);
			}
		}
		// Sort the menus by list_order:
		java.util.Collections.sort(menus, new Comparator<UserMenu> () {
			public int compare(UserMenu menu1, UserMenu menu2) {
				return menu1.menu.getListOrder().compareTo(menu2.menu.getListOrder());
			}
		});
		// Sort the menu items by list_order:
		for(UserMenu userMenu : menus) {
			java.util.Collections.sort(userMenu.menuItems, new Comparator<MenuItem> () {
				public int compare(MenuItem item1, MenuItem item2) {
					return item1.getListOrder().compareTo(item2.getListOrder());
				}
			});
		}
		return menus;
	}
}
