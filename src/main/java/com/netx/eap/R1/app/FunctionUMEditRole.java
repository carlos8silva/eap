package com.netx.eap.R1.app;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import com.netx.generics.R1.util.Strings;
import com.netx.basic.R1.io.BasicIOException;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.AssociationMap;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.bl.Permissions;
import com.netx.eap.R1.bl.Permission;
import com.netx.eap.R1.bl.Roles;
import com.netx.eap.R1.bl.Role;
import com.netx.eap.R1.bl.Users;
import com.netx.eap.R1.bl.User;
import com.netx.eap.R1.bl.UserRole;
import com.netx.eap.R1.bl.RolePermission;
import com.netx.eap.R1.bl.UserInterfaces;
import com.netx.eap.R1.bl.UserInterface;
import com.netx.eap.R1.bl.Session.EndReason;
import com.netx.eap.R1.core.UserSession;
import com.netx.eap.R1.core.Constants;
import com.netx.eap.R1.core.Function;
import com.netx.eap.R1.core.EapRequest;
import com.netx.eap.R1.core.EapResponse;
import com.netx.eap.R1.core.Template;
import com.netx.eap.R1.core.Values;
import com.netx.eap.R1.core.ValueList;
import com.netx.eap.R1.core.XmlResponse;
import com.netx.eap.R1.core.MimeTypes;
import com.netx.eap.R1.core.IllegalParameterException;


public class FunctionUMEditRole extends Function {

	private final static Long _ZERO = new Long(0);
	
	protected void doGet(EapRequest request, EapResponse response) throws BasicIOException, BLException {
		response.setDisableCache();
		// Retrieve data from the database:
		Connection c = request.getConnection();
		final String doAction = request.getParameter("do", true);
		if(doAction.equals("check-role-name")) {
			request.setXml(true);
			XmlResponse xml = null;
			String roleName = request.getParameter("name", true);
			Long roleId = request.getLongParameter("id");
			Role role = Roles.getInstance().getRoleByName(c, roleName);
			if(role == null) {
				xml = new XmlResponse("OK");
			}
			else if(role.getRoleId().equals(roleId)) {
				xml = new XmlResponse("OK");
			}
			else {
				xml = new XmlResponse("ERROR");
				xml.getRoot().addElement("error-code").setText("unavailable");
				xml.getRoot().addElement("message").setText("Role '"+roleName+"' already exists");
			}
			xml.render(response);
			return;
		}
		// Other actions result in an HTML response:
		final Template page;
		if(doAction.equals("create")) {
			page = _showAddForm(request, response, c);
		}
		else if(doAction.equals("confirm")) {
			page = _showConfirmForm(request, response, c);
		}
		else {
			// Populate template with data:
			final Role role = Roles.getInstance().get(c, request.getLongParameter("id", true));
			if(role.getSystemRole()) {
				page = _showSystemForm(request, response, c, role);
			}
			else {
				if(doAction.equals("edit")) {
					page = _showEditForm(request, response, c, role);
				}
				else if(doAction.equals("delete")) {
					page = _showDeleteForm(request, response, c, role);
				}
				else {
					throw new IllegalParameterException("do", doAction);
				}
			}
		}
		page.render(MimeTypes.TEXT_HTML, response);
	}
	
	@SuppressWarnings("unchecked")
	protected void doPost(EapRequest request, EapResponse response) throws BasicIOException, BLException {
		final String action = request.getParameter("action", true);
		final Connection c = request.getConnection();
		if(action.equals("confirm")) {
			Role role = (Role)request.getUserSession().getAttribute(Constants.SATTR_ROLE);
			AssociationMap<RolePermission> newPerms = (AssociationMap<RolePermission>)request.getUserSession().getAttribute(Constants.SATTR_ROLE_PERMS);
			// Terminate affected sessions if user manager has requested it:
			String confirmation = request.getParameter("confirmation", true);
			if(confirmation.equals("logout")) {
				// Close all open sessions:
				List<Long> userIds = Roles.getInstance().listUsersWithRole(c, role.getRoleId());
				List<UserSession> sessions = request.getEapContext().getSessions(c, userIds);
				for(UserSession s : sessions) {
					request.getEapContext().endSession(request, response, c, s, EndReason.FORCED_OUT_SA, "your access permissions have been modified");
				}
			}
			// Update role:
			Roles.getInstance().update(c, role, newPerms);
			response.sendRedirect(getAliasURL()+"?do=edit&id="+role.getRoleId());
			return;
		}
		Long roleId = null;
		if(action.equals("create") || action.equals("update")) {
			String params = "";
			final String name = request.getParameter("name", true);
			final String description = request.getParameter("description", true);
			final String baseUI = request.getParameter("base_ui");
			final String[] permissions = Shared.parseStringList(request, "permissions", false);
			if(action.equals("update")) {
				roleId = request.getLongParameter("id", true);
				Role role = Roles.getInstance().get(c, roleId);
				// Set base information:
				role.setName(name);
				role.setDescription(description);
				if(baseUI != null) {
					// Base UI may be null if this is a system role
					role.setBaseUIId(baseUI);
				}
				// Check if permissions have changed:
				AssociationMap<RolePermission> savedPerms = role.getRolePermissions(c);
				if(!role.getSystemRole()) {
					savedPerms.clear();
					for(String pId : permissions) {
						savedPerms.put(new Permission(pId));
					}
				}
				if(savedPerms.hasUpdates()) {
					// Check if there are logged in users who are affected by this change:
					List<Long> userIds = Roles.getInstance().listUsersWithRole(c, roleId);
					List<UserSession> sessions = request.getEapContext().getSessions(c, userIds);
					if(!sessions.isEmpty()) {
						// We have users affected and need to ask the user manager if they should be logged off:
						c.close();
						UserSession s = request.getUserSession();
						s.setAttribute(Constants.SATTR_ROLE, role);
						s.setAttribute(Constants.SATTR_ROLE_PERMS, savedPerms);
						s.setAttribute(Constants.SATTR_USERS_AFFECTED, new Integer(sessions.size()));
						response.sendRedirect(getAliasURL()+"?do=confirm");
						return;
					}
				}
				// Permissions have not been modified, or if they have
				// there are no users logged in who are affected by the change.
				// We can proceed with the update.
				Roles.getInstance().update(c, role, savedPerms);
			}
			else if(action.equals("create")) {
				Role role = new Role(); 
				role.setName(name);
				role.setDescription(description);
				role.setBaseUIId(baseUI);
				AssociationMap<RolePermission> newPerms = role.getRolePermissions(c);
				for(String pId : permissions) {
					newPerms.put(new Permission(pId));
				}
				Roles.getInstance().create(c, role, newPerms);
				roleId = role.getRoleId();
				params += "&event=created";
			}
			else {
				throw new IntegrityException(action);
			}
			c.close();
			response.sendRedirect(getAliasURL()+"?do=edit&id="+roleId+params);
		}
		else if(action.equals("delete")) {
			roleId = request.getLongParameter("id", true);
			String conflicts = request.getParameter("conflicts", true);
			if(conflicts.equals("none")) {
				// Close all open sessions:
				List<Long> userIds = Roles.getInstance().listUsersWithRole(c, roleId);
				List<UserSession> sessions = request.getEapContext().getSessions(c, userIds);
				for(UserSession s : sessions) {
					request.getEapContext().endSession(request, response, c, s, EndReason.FORCED_OUT_SA, "your access permissions have been modified");
				}
				Roles.getInstance().delete(c, Roles.getInstance().get(c, roleId));
				c.close();
				response.sendRedirect("um-list-roles.x");
			}
			else {
				// Parse conflicts parameter:
				String[] array = conflicts.split("[,]");
				// Note: we do not want to create a transaction here. If updates
				// to individual user accounts succeed, they should be persisted.
				for(String s : array) {
					Long userId = _ZERO;
					Long primaryRoleId = _ZERO;
					try {
						String[] params = s.split("[|]");
						if(params.length != 2) {
							throw new IllegalParameterException("conflicts", conflicts);
						}
						userId = new Long(params[0]);
						primaryRoleId = new Long(params[1]);
					}
					catch(Exception e) {
						throw new IllegalParameterException("conflicts", conflicts);
					}
					if(primaryRoleId.equals(_ZERO)) {
						continue;
					}
					// Add the new primary role to the user in case they do not have it yet:
					User user = Users.getInstance().get(c, userId);
					AssociationMap<UserRole> map = user.getUserRoles(c);
					UserRole primary = map.get(primaryRoleId);
					if(primary == null) {
						map.put(new Role(primaryRoleId));
					}
					// Update primary role:
					for(UserRole ur : map) {
						if(ur.getRoleId().equals(primaryRoleId)) {
							ur.setPrimaryRole(true);
						}
						else {
							ur.setPrimaryRole(false);
						}
					}
					Users.getInstance().save(c, user, map, null);
				}
				c.close();
				response.sendRedirect(getAliasURL()+"?do=delete&id="+roleId);
			}
		}
		else {
			throw new IllegalParameterException("action", action);
		}
	}
	
	private Template _showAddForm(EapRequest request, EapResponse response, Connection c) throws BasicIOException, BLException {
		Template page = request.getTemplate("templates/um-edit-role-1.tpt.html");
		Values v = page.getValues();
		v.set("title", "Add New");
		v.set("action", "create");
		v.set("role-id", "");
		v.set("name", "");
		v.set("description", "");
		_listUserInterfaces(c, null, v);
		List<Permission> availablePermissions = Permissions.getInstance().listAll(c);
		ValueList pList2 = v.getList("p-available");
		for(Permission af : availablePermissions) {
			Values pValues = pList2.next();
			pValues.set("id", af.getPermissionId());
			pValues.set("name", af.getName());
			pValues.set("description", af.getDescription());
		}
		return page;
	}

	private Template _showEditForm(EapRequest request, EapResponse response, Connection c, Role role) throws BasicIOException, BLException {
		Template page = request.getTemplate("templates/um-edit-role-1.tpt.html");
		Values v = page.getValues();
		v.set("title", "Edit");
		v.set("action", "update");
		v.setIf("can-delete", true).set("role-id", role.getRoleId());
		// Pane 1:
		v.set("role-id", role.getRoleId());
		v.set("name", role.getName());
		v.set("description", role.getDescription());
		String event = request.getParameter("event");
		if(event != null) {
			if(event.equals("created")) {
				v.setIf("event-created", true);
			}
		}
		_listUserInterfaces(c, role, v);
		// Pane 2:
		AssociationMap<RolePermission> pCol = role.getRolePermissions(c);
		List<Permission> assignedPerms = new ArrayList<Permission>();
		for(RolePermission up : pCol) {
			assignedPerms.add(up.getPermission(c));
		}
		List<Permission> availablePermissions = Permissions.getInstance().listAll(c);
		availablePermissions.removeAll(assignedPerms);
		ValueList pList1 = v.getList("p-assigned");
		for(Permission af : assignedPerms) {
			Values pValues = pList1.next();
			pValues.set("id", af.getPermissionId());
			pValues.set("name", af.getName());
			pValues.set("description", Strings.addSlashes(af.getDescription()));
		}
		ValueList pList2 = v.getList("p-available");
		for(Permission af : availablePermissions) {
			Values pValues = pList2.next();
			pValues.set("id", af.getPermissionId());
			pValues.set("name", af.getName());
			pValues.set("description", Strings.addSlashes(af.getDescription()));
		}
		return page;
	}

	private void _listUserInterfaces(Connection c, Role role, Values v) throws BLException {
		List<UserInterface> uiList = UserInterfaces.getInstance().listAll(c);
		ValueList vUiList = v.getList("uinterfaces");
		for(UserInterface ui : uiList) {
			Values vUI = vUiList.next();
			vUI.set("ui-id", ui.getUiId());
			String uiName = ui.getName();
			if(ui.getUiId().equals("eap")) {
				uiName = uiName + " (default)";
			}
			vUI.set("ui-name", uiName);
			if(role == null) {
				vUI.set("selected", ui.getUiId().equals("eap") ? "selected" : "");
			}
			else {
				vUI.set("selected", ui.getUiId().equals(role.getBaseUIId()) ? "selected" : "");
			}
		}
	}

	private Template _showSystemForm(EapRequest request, EapResponse response, Connection c, Role role) throws BasicIOException, BLException {
		Template page = request.getTemplate("templates/um-edit-sys-role.tpt.html");
		Values v = page.getValues();
		v.set("role-id", role.getRoleId());
		v.set("title", "Edit");
		v.set("action", "update");
		v.set("name", role.getName());
		v.set("description", role.getDescription());
		v.set("ui", role.getBaseUI(c).getName());
		ValueList vPermDescs = v.getList("perm-descs");
		ValueList vPerms = v.getList("perms");
		AssociationMap<RolePermission> rolePerms = role.getRolePermissions(c);
		Iterator<RolePermission> it = rolePerms.iterator();
		for(int i=1; it.hasNext(); i++) {
			RolePermission rp = it.next();
			Permission af = rp.getPermission(c);
			Values permValues = vPerms.next();
			Values permDescValues = vPermDescs.next();
			permValues.set("i", i+"");
			permValues.set("perm-name", af.getName());
			permDescValues.set("i", i+"");
			permDescValues.set("perm-name", af.getName());
			permDescValues.set("perm-desc", Strings.addSlashes(af.getDescription()));
		}
		return page;
	}

	private Template _showConfirmForm(EapRequest request, EapResponse response, Connection c) throws BasicIOException, BLException {
		final Template page = request.getTemplate("templates/um-edit-role-2.tpt.html");
		Role role = (Role)request.getUserSession().getAttribute(Constants.SATTR_ROLE);
		Integer numUsers = (Integer)request.getUserSession().getAttribute(Constants.SATTR_USERS_AFFECTED);
		Values v = page.getValues();
		v.set("role-name", role.getName());
		v.set("num-users", numUsers);
		return page;
	}

	private Template _showDeleteForm(EapRequest request, EapResponse response, Connection c, Role role) throws BasicIOException, BLException {
		final Template page;
		List<Long> userIds = Roles.getInstance().listUsersWithPrimaryRole(c, role.getRoleId());
		if(!userIds.isEmpty()) {
			page = request.getTemplate("templates/um-delete-role-1.tpt.html");
			Values v = page.getValues();
			v.set("role-id", role.getRoleId());
			v.set("name", role.getName());
			v.set("num-users", userIds.size());
			ValueList vlJsUsers = v.getList("js-users");
			ValueList vlUsers = v.getList("users");
			for(Long userId : userIds) {
				User u = Users.getInstance().get(c, userId);
				Values vJsUsers = vlJsUsers.next();
				vJsUsers.set("user-id", userId);
				Values vUsers = vlUsers.next();
				vUsers.set("user-id", u.getUserId());
				vUsers.set("full-name", u.getFullName());
				vUsers.set("role-name", role.getName());
				// Show list of assigned and available roles:
				List<Role>[] roles = Roles.getInstance().breakRolesFor(c, u, false);
				if(!roles[0].isEmpty()) {
					Values vIfHas = vUsers.setIf("has-assigned-roles", true);
					vIfHas.set("user-id", userId);
					ValueList vlAssigned = vIfHas.getList("assigned-roles");
					for(Role assignedRole : roles[0]) {
						Values vRole = vlAssigned.next();
						vRole.set("role-id", assignedRole.getRoleId());
						vRole.set("role-name", assignedRole.getName());
					}
				}
				ValueList vlAvailable = vUsers.getList("available-roles");
				for(Role availableRole : roles[1]) {
					Values vRole = vlAvailable.next();
					vRole.set("role-id", availableRole.getRoleId());
					vRole.set("role-name", availableRole.getName());
				}
			}
		}
		else {
			page = request.getTemplate("templates/um-delete-role-2.tpt.html");
			Values v = page.getValues();
			v.set("role-id", role.getRoleId());
			v.set("role-name", role.getName());
			// Find users who are assigned with this role:
			List<User> users = Users.getInstance().listUsersByRole(c, role.getRoleId());
			userIds = new ArrayList<Long>(users.size());
			for(User u : users) {
				userIds.add(u.getUserId());
			}
			// Find users with this role that are logged in:
			List<UserSession> sessions = request.getEapContext().getSessions(c, userIds);
			Values vIfLoggedIn = v.setIf("has-logged-in-users", !sessions.isEmpty());
			vIfLoggedIn.set("role-name", role.getName());
			if(!sessions.isEmpty()) {
				vIfLoggedIn.set("num-users", sessions.size());
			}
		}
		return page;
	}
}
