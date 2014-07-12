package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.Domain;


public class EAP extends Domain {

	// TYPE:
	// Entities must be put in a specific order to prevent 
	// initialization problems with foreign keys. The order is:
	// - Reference data before transactional data
	// - Entities with fields relating to other entities must come last
	// Immutable data:
	private static final SecurityQuestions _securityQuestions = new SecurityQuestions();
	// Reference data:
	private static final Permissions _permissions = new Permissions();
	private static final Functions _functionEntries = new Functions();
	private static final UserInterfaces _userInterfaces = new UserInterfaces();
	private static final Users _users = new Users();
	private static final Roles _roles = new Roles();
	private static final UserRoles _userRoles = new UserRoles();
	private static final UserPermissions _userPermissions = new UserPermissions();
	private static final RolePermissions _rolePermissions = new RolePermissions();
	private static final Menus _menus = new Menus();
	private static final MenuItems _menuItems = new MenuItems();
	private static final UserEventTypes _userEventTypes = new UserEventTypes();
	// Transactional data:
	private static final Sessions _sessions = new Sessions();
	private static final UserEvents _userEvents = new UserEvents();
	private static final Preferences _preferences = new Preferences();
	
	// Entity getters:
	public static SecurityQuestions getSecurityQuestions() {
		return _securityQuestions;
	}

	public static Permissions getPermissions() {
		return _permissions;
	}

	public static Functions getFunctions() {
		return _functionEntries;
	}

	public static UserInterfaces getUserInterfaces() {
		return _userInterfaces;
	}

	public static UserEventTypes getUserEventTypes() {
		return _userEventTypes;
	}

	public static Roles getRoles() {
		return _roles;
	}

	public static RolePermissions getRolePermissions() {
		return _rolePermissions;
	}

	public static UserRoles getUserRoles() {
		return _userRoles;
	}

	public static UserPermissions getUserPermissions() {
		return _userPermissions;
	}

	public static Users getUsers() {
		return _users;
	}

	public static Sessions getSessions() {
		return _sessions;
	}

	public static UserEvents getUserEvents() {
		return _userEvents;
	}

	public static Menus getMenus() {
		return _menus;
	}

	public static MenuItems getMenuItems() {
		return _menuItems;
	}

	public static Preferences getPreferences() {
		return _preferences;
	}
}
