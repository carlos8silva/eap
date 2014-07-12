package com.netx.eap.R1.app;
import java.util.List;
import java.util.ArrayList;
import com.netx.generics.R1.util.Strings;
import com.netx.basic.R1.io.BasicIOException;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.AssociationMap;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.bl.Users;
import com.netx.eap.R1.bl.User;
import com.netx.eap.R1.bl.Permissions;
import com.netx.eap.R1.bl.Permission;
import com.netx.eap.R1.bl.UserRole;
import com.netx.eap.R1.bl.Roles;
import com.netx.eap.R1.bl.Role;
import com.netx.eap.R1.bl.RolePermission;
import com.netx.eap.R1.bl.UserPermission;
import com.netx.eap.R1.bl.User.LockedReason;
import com.netx.eap.R1.bl.Session.EndReason;
import com.netx.eap.R1.core.Constants;
import com.netx.eap.R1.core.Function;
import com.netx.eap.R1.core.EapRequest;
import com.netx.eap.R1.core.EapResponse;
import com.netx.eap.R1.core.Template;
import com.netx.eap.R1.core.Values;
import com.netx.eap.R1.core.ValueList;
import com.netx.eap.R1.core.XmlResponse;
import com.netx.eap.R1.core.UserSession;
import com.netx.eap.R1.core.MimeTypes;
import com.netx.eap.R1.core.IllegalParameterException;
import com.netx.eap.R1.core.IllegalRequestException;
import org.dom4j.Element;


public class FunctionUMEditUser extends Function {

	protected void doGet(EapRequest request, EapResponse response) throws BasicIOException, BLException {
		response.setDisableCache();
		final String doAction = request.getParameter("do", true);
		final Connection c = request.getConnection();
		// Requests that result in an HTML response:
		if(doAction.equals("confirm")) {
			Template page = _showConfirmForm(request, response, c);
			page.render(MimeTypes.TEXT_HTML, response);
			return;
		}
		if(doAction.equals("edit") || doAction.equals("create") || doAction.equals("enable") || doAction.equals("confirm")) {
			response.setContentType(MimeTypes.TEXT_HTML);
			final User user;
			if(doAction.equals("create")) {
				user = new User();
			}
			else {
				Long userId = request.getLongParameter("id", true);
				user = Users.getInstance().get(c, userId);
				if(user == null) {
					// TODO  need to create an exception type / extend IllegalRequestException to allow
					// us to specify a custom message. In this case: user with ID <id> could not be found
					throw new IllegalRequestException("id");
				}
			}
			final Template page;
			if(doAction.equals("edit") && user.getStatus() == User.STATUS.DISABLED) {
				page = _showReadOnlyForm(request, response, c, user);
			}
			else {
				page = _showEditForm(request, response, c, user);
			}
			page.render(MimeTypes.TEXT_HTML, response);
			return;
		}
		// Other actions result in an XML response:
		request.setXml(true);
		XmlResponse xml = null;
		if(doAction.equals("check-username")) {
			String username = request.getParameter("username", true);
			Long userId = request.getLongParameter("id");
			User user = Users.getInstance().getUserByUsername(c, username);
			if(user == null) {
				xml = new XmlResponse("OK");
			}
			else if(user.getUserId().equals(userId)) {
				xml = new XmlResponse("OK");
			}
			else {
				xml = new XmlResponse("ERROR");
				xml.getRoot().addElement("error-code").setText("unavailable");
				xml.getRoot().addElement("message").setText("Username '"+username+"' is already taken");
			}
		}
		else if(doAction.equals("check-permissions")) {
			Long[] roles = Shared.parseLongList(request, "roles", true);
			xml = new XmlResponse("OK");
			Element e = xml.getRoot().addElement("contents");
			for(Long roleId : roles) {
				Role role = Roles.getInstance().get(c, roleId);
				Element eRole = e.addElement("role");
				eRole.addAttribute("id", roleId.toString());
				eRole.addAttribute("name", role.getName());
				for(RolePermission rp : role.getRolePermissions(c)) {
					Element erp = eRole.addElement("permission");
					Permission af = rp.getPermission(c);
					erp.addAttribute("id", af.getPermissionId().toString());
					erp.addAttribute("name", af.getName());
				}
			}
		}
		else {
			throw new IllegalParameterException("do", doAction);
		}
		xml.render(response);
	}

	@SuppressWarnings("unchecked")
	protected void doPost(EapRequest request, EapResponse response) throws BasicIOException, BLException {
		final String action = request.getParameter("action", true);
		final Connection c = request.getConnection();
		if(action.equals("confirm")) {
			User user = (User)request.getUserSession().getAttribute(Constants.SATTR_USER);
			AssociationMap<UserRole> newRoles = (AssociationMap<UserRole>)request.getUserSession().getAttribute(Constants.SATTR_USER_ROLES);
			AssociationMap<UserPermission> newPerms = (AssociationMap<UserPermission>)request.getUserSession().getAttribute(Constants.SATTR_USER_PERMS);
			// Terminate the user session if user manager has requested it:
			String confirmation = request.getParameter("confirmation", true);
			if(confirmation.equals("logout")) {
				// Close any open sessions for the affected user:
				List<UserSession> userSessions = request.getEapContext().getSessionsFor(c, user.getUserId());
				if(userSessions != null) {
					for(UserSession userSession : userSessions) {
						request.getEapContext().endSession(request, response, c, userSession, EndReason.FORCED_OUT_SA, "your access permissions have been modified");
					}
				}
			}
			// Update user:
			Users.getInstance().save(c, user, newRoles, newPerms);
			response.sendRedirect(getAliasURL()+"?do=edit&id="+user.getUserId());
			return;
		}
		final Long userId;
		final User user;
		if(action.equals("create")) {
			userId = null;
			user = new User();
		}
		else {
			userId = request.getLongParameter("id", true);
			user = Users.getInstance().get(c, userId);
		}
		String params = "&do=edit";
		AssociationMap<UserRole> roles = user.getUserRoles(c);
		AssociationMap<UserPermission> permissions = user.getUserPermissions(c);
		if(action.equals("create") || action.equals("update") || action.equals("enable")) {
			// Set personal information:
			user.setUsername(request.getParameter("username", true));
			user.setFirstName(request.getParameter("first_name", true));
			user.setMiddleInitial(request.getParameter("middle_initial"));
			user.setLastName(request.getParameter("last_name", true));
			if(action.equals("enable")) {
				user.enable(user.getUsername());
				params += "&event=enabled";
			}
			// Update roles:
			Long[] newRoles = Shared.parseLongList(request, "roles", true);
			Long primary = request.getLongParameter("primary", true);
			roles.clear();
			for(Long roleId : newRoles) {
				UserRole ur = roles.put(new Role(roleId));
				if(roleId.equals(primary)) {
					ur.setPrimaryRole(true);
				}
				else {
					ur.setPrimaryRole(false);
				}
			}
			// Update permissions:
			String[] newPerms = Shared.parseStringList(request, "permissions", false);
			permissions.clear();
			for(String pId : newPerms) {
				permissions.put(new Permission(pId));
			}
			// Check if the user is currently logged in:
			if(user.getUserId() != null && (roles.hasUpdates() || permissions.hasUpdates())) {
				List<UserSession> userSessions = request.getEapContext().getSessionsFor(c, userId);
				if(userSessions != null) {
					UserSession s = request.getUserSession();
					if(s.getUserId().equals(userId)) {
						Users.getInstance().save(c, user, roles, permissions);
						// Modifying own session, we just need to warn the user:
						response.sendRedirect(getAliasURL()+"?do=edit&id="+user.getUserId()+"&event=changed-own-permissions");
					}
					else {
						// We need to ask the user manager if the user should be logged off:
						s.setAttribute(Constants.SATTR_USER, user);
						s.setAttribute(Constants.SATTR_USER_ROLES, roles);
						s.setAttribute(Constants.SATTR_USER_PERMS, permissions);
						response.sendRedirect(getAliasURL()+"?do=confirm");
					}
					return;
				}
			}
		}
		else if(action.equals("lock")) {
			user.lock(LockedReason.USER_MANAGER);
			List<UserSession> sessions = request.getEapContext().getSessionsFor(c, user.getUserId());
			if(sessions != null) {
				// End any active sessions if the user is currently logged in:
				for(UserSession s : sessions) {
					request.getEapContext().endSession(request, response, c, s, EndReason.FORCED_OUT_SA, "account has been locked");
				}
			}
			params += "&event=locked";
		}
		else if(action.equals("unlock")) {
			user.unlock();
			params += "&event=unlocked";
		}
		else if(action.equals("reset")) {
			user.resetPassword();
			params += "&event=reset";
		}
		else if(action.equals("disable")) {
			user.disable();
			List<UserSession> sessions = request.getEapContext().getSessionsFor(c, user.getUserId());
			if(sessions != null) {
				// End any active session if the user is currently logged in:
				for(UserSession s : sessions) {
					request.getEapContext().endSession(request, response, c, s, EndReason.FORCED_OUT_SA, "account has been disabled");
				}
			}
			params += "&event=disabled";
		}
		else {
			throw new IllegalParameterException("action", action);
		}
		if(action.equals("create")) {
			params += "&event=created";
			Users.getInstance().create(c, user, roles, permissions);
		}
		else {
			Users.getInstance().save(c, user, roles, permissions);
		}
		response.sendRedirect(getAliasURL()+"?id="+user.getUserId()+params);
	}
	
	private Template _showEditForm(EapRequest request, EapResponse response, Connection c, User user) throws BasicIOException, BLException {
		Template page = request.getTemplate("templates/um-edit-user-1.tpt.html");
		Values v = page.getValues();
		// Pane 1:
		if(request.getParameter("do").equals("enable")) {
			v.set("title", "Enable");
			v.setIf("do-enable", true);
		}
		else if(request.getParameter("do").equals("create")) {
			v.set("title", "Add");
			v.setIf("do-create", true);
		}
		else {
			v.set("title", "Edit");
			Values vIfEnableUser = v.setIf("enable-user", user.getStatus() != User.STATUS.DISABLED);
			if(vIfEnableUser != null) {
				vIfEnableUser.setIf("is-locked", user.getStatus() == User.STATUS.LOCKED);
				vIfEnableUser.set("username", user.getUsername());
			}
		}
		v.setIf("is-locked", user.getStatus() == User.STATUS.LOCKED);
		String event = request.getParameter("event");
		if(event != null) {
			if(event.equals("created")) {
				Values vEventCreated = v.setIf("event-created", true);
				vEventCreated.set("password", user.getPassword());
				vEventCreated.set("name", user.getFirstName());
			}
			if(event.equals("unlocked")) {
				v.setIf("event-unlocked", true);
			}
			if(event.equals("reset")) {
				Values vEventReset = v.setIf("event-reset", true);
				vEventReset.set("password", user.getPassword());
				vEventReset.set("name", user.getFirstName());
			}
			if(event.equals("enabled")) {
				v.setIf("event-enabled", true);
			}
			if(event.equals("changed-own-permissions")) {
				v.setIf("event-changed-permissions", true);
			}
		}
		v.set("user-id", user.getUserId());
		if(user.getStatus() == User.STATUS.DISABLED) {
			v.set("username", user.getOldUsername());
			v.set("action", "enable");
		}
		else {
			v.set("username", user.getUsername());
			if(user.getUserId() == null) {
				v.set("action", "create");
			}
			else {
				v.set("action", "update");
			}
		}
		v.set("first-name", user.getFirstName());
		v.set("middle-initial", user.getMiddleInitial());
		v.set("last-name", user.getLastName());
		// Pane 2:
		// TODO use Roles.breakRolesFor
		AssociationMap<UserRole> rCol = user.getUserRoles(c);
		List<Role> assignedRoles = new ArrayList<Role>();
		Long primary = null;
		for(UserRole ur : rCol) {
			assignedRoles.add(ur.getRole(c));
			if(ur.getPrimaryRole()) {
				primary = ur.getRoleId();
				v.set("primary", primary);
			}
		}
		// Happens on do="create"
		if(primary == null) {
			v.set("primary", "");
		}
		List<Role> availableRoles = Roles.getInstance().listAll(c);
		availableRoles.removeAll(assignedRoles);
		ValueList rList1 = v.getList("r-assigned");
		for(Role r : assignedRoles) {
			Values rValues = rList1.next();
			rValues.set("id", r.getRoleId());
			rValues.set("name", r.getName());
			rValues.set("description", Strings.addSlashes(r.getDescription()));
			if(r.getRoleId().equals(primary)) {
				rValues.set("primary", Boolean.TRUE);
			}
			else {
				rValues.set("primary", Boolean.FALSE);
			}
		}
		ValueList rList2 = v.getList("r-available");
		for(Role r : availableRoles) {
			Values rValues = rList2.next();
			rValues.set("id", r.getRoleId());
			rValues.set("name", r.getName());
			rValues.set("description", Strings.addSlashes(r.getDescription()));
		}
		// Pane 3:
		AssociationMap<UserPermission> pCol = user.getUserPermissions(c);
		List<Permission> assignedAfs = new ArrayList<Permission>();
		for(UserPermission up : pCol) {
			assignedAfs.add(up.getPermission(c));
		}
		List<Permission> availablePermissions = Permissions.getInstance().listAll(c);
		availablePermissions.removeAll(assignedAfs);
		ValueList pList1 = v.getList("p-assigned");
		for(Permission af : assignedAfs) {
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
		if(user.getUserId() != null) {
			// TODO expose this information as an AJAX call (on request).
			// As it stands, we need to check whether the user is logged in EVERY time we display the page.
			List<UserSession> sessions = request.getEapContext().getSessionsFor(c, user.getUserId());
			if(sessions != null) {
				v.set("user-logged-in", "true");
			}
			else {
				v.set("user-logged-in", "false");
			}
		}
		else {
			v.set("user-logged-in", "false");
		}
		return page;
	}

	private Template _showReadOnlyForm(EapRequest request, EapResponse response, Connection c, User user) throws BasicIOException, BLException {
		Template page = request.getTemplate("templates/um-disabled-user.tpt.html");
		Values v = page.getValues();
		v.set("user-id", user.getUserId());
		v.set("full-name", user.getFullName(false));
		v.set("date-created", user.getTimeCreated());
		v.set("date-disabled", user.getTimeDisabled());
		v.set("username", user.getOldUsername());
		String event = request.getParameter("event");
		if(event != null) {
			if(event.equals("disabled")) {
				v.setIf("event-disabled", true);
			}
		}
		// Roles:
		ValueList vRoles = v.getList("roles");
		ValueList vRoleDescs = v.getList("role-descs");
		final AssociationMap<UserRole> userRoles = user.getUserRoles(c);
		int i = 1;
		for(UserRole ur : userRoles) {
			Role role = ur.getRole(c);
			Values roleValues = vRoles.next();
			Values roleDescValues = vRoleDescs.next();
			roleValues.set("i", i+"");
			roleValues.set("role-name", role.getName());
			roleDescValues.set("i", i+"");
			roleDescValues.set("role-name", role.getName());
			roleDescValues.set("role-desc", Strings.addSlashes(role.getDescription()));
			i++;
		}
		// Permissions:
		AssociationMap<UserPermission> userPerms = user.getUserPermissions(c);
		if(userPerms.isEmpty()) {
			v.setIf("has-individual-perms", false);
		}
		else {
			Values ifValues = v.setIf("has-individual-perms", true);
			ValueList vPerms = ifValues.getList("perms");
			ValueList vPermDescs = v.getList("perm-descs");
			i = 1;
			for(UserPermission up : userPerms) {
				Permission af = up.getPermission(c);
				Values permValues = vPerms.next();
				Values permDescValues = vPermDescs.next();
				permValues.set("i", i+"");
				permValues.set("perm-name", af.getName());
				permDescValues.set("i", i+"");
				permDescValues.set("perm-name", af.getName());
				permDescValues.set("perm-desc", Strings.addSlashes(af.getDescription()));
				i++;
			}
		}
		return page;
	}

	private Template _showConfirmForm(EapRequest request, EapResponse response, Connection c) throws BasicIOException, BLException {
		final Template page = request.getTemplate("templates/um-edit-user-2.tpt.html");
		User user = (User)request.getUserSession().getAttribute(Constants.SATTR_USER);
		Values v = page.getValues();
		v.set("first-name", user.getFirstName());
		v.set("full-name", user.getFullName(false));
		return page;
	}
}
