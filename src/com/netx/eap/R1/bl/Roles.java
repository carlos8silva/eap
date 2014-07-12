package com.netx.eap.R1.bl;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import com.netx.basic.R1.eh.Checker;
import com.netx.bl.R1.core.*;


public class Roles extends HolderEntity<RolesMetaData,Role> {

	// TYPE:
	public static Roles getInstance() {
		return EAP.getRoles();
	}

	// INSTANCE:
	private Select _qSelectRoleByName = null;
	private Select _qSelectUsersWithPrimaryRole = null;
	private Select _qSelectUsersWithRole = null;

	Roles() {
		super(new RolesMetaData());
	}
	
	protected void onLoad() {
		_qSelectRoleByName = createSelect("select-role-by-name", "SELECT * FROM eap_roles WHERE name = ?");
		_qSelectRoleByName.setUpdatesCache(true);
		_qSelectUsersWithRole = null; //getRepository().createQuery("select-users-with-role", "SELECT u.user_id FROM eap_users u, eap_roles r, eap_user_roles ur WHERE u.user_id=ur.user_id AND r.role_id=ur.role_id AND r.role_id = ?");
		_qSelectUsersWithPrimaryRole = null; //getRepository().createQuery("select-users-with-primary-role", "SELECT u.user_id FROM eap_users u, eap_roles r, eap_user_roles ur WHERE u.user_id=ur.user_id AND r.role_id=ur.role_id AND ur.primary_role='T' AND r.role_id = ?");
	}

	public void create(Connection c, Role r, AssociationMap<RolePermission> permissions) throws BLException, ReadOnlyFieldException {
		Checker.checkNull(r, "r");
		// Validations:
		_validate(c, r, permissions);
		// Commit changes:
		synchronized(c) {
			c.startTransaction();
			insert(c, r);
			if(permissions != null) {
				RolePermissions.getInstance().save(c, permissions);
			}
			c.commit();
		}
	}

	public void update(Connection c, Role r, AssociationMap<RolePermission> permissions) throws BLException, ReadOnlyFieldException {
		Checker.checkNull(r, "r");
		// Validations:
		if(permissions != null) {
			_validate(c, r, permissions);
		}
		// Commit changes:
		synchronized(c) {
			c.startTransaction();
			updateInstance(c, r);
			if(permissions != null) {
				RolePermissions.getInstance().save(c, permissions);
			}
			c.commit();
		}
	}

	public void delete(Connection c, Role role) throws BLException {
		Checker.checkNull(role, "role");
		synchronized(c) {
			c.startTransaction();
			// Delete all UserRole records:
			// TODO this is a waste of performance. However, given the current BL architecture,
			// we need to delete each association explicitly. If we use CASCADE fk's, the User
			// instance in memory will be inconsistent with the database
			List<Long> userIds = listUsersWithRole(c, role.getRoleId());
			for(Long userId : userIds) {
				User user = Users.getInstance().get(c, userId);
				// TODO since we have refactored associations, we may be able to drop this code:
				AssociationMap<UserRole> userRoles = user.getUserRoles(c);
				userRoles.remove(role.getPrimaryKey());
				Users.getInstance().save(c, user, userRoles, null);
			}
			userIds = listUsersWithRole(c, role.getRoleId());
			// Delete role:
			deleteInstance(c, role);
			c.commit();
		}
	}

	public Role getRoleByName(Connection c, String roleName) throws BLException {
		Checker.checkEmpty(roleName, "roleName");
		return selectInstance(c, _qSelectRoleByName, roleName);
	}

	public List<Role> listAll(Connection c) throws BLException {
		return selectAll(c);
	}
	
	public List<Long> listUsersWithPrimaryRole(Connection c, Long roleId) throws BLException {
		Checker.checkNull(c, "c");
		Checker.checkNull(roleId, "roleId");
		Results r = c.select(_qSelectUsersWithPrimaryRole, roleId);
		List<Long> userIds = new ArrayList<Long>(r.getRowCount());
		for(Row row : r.getRows()) {
			userIds.add(row.getLong(1));
		}
		return userIds;
	}

	public List<Long> listUsersWithRole(Connection c, Long roleId) throws BLException {
		Checker.checkNull(c, "c");
		Checker.checkNull(roleId, "roleId");
		Results r = c.select(_qSelectUsersWithRole, roleId);
		List<Long> userIds = new ArrayList<Long>(r.getRowCount());
		for(Row row : r.getRows()) {
			userIds.add(row.getLong(1));
		}
		return userIds;
	}

	@SuppressWarnings("unchecked")
	public List<Role>[] breakRolesFor(Connection c, User user, boolean includePrimary) throws BLException {
		List<Role> assignedRoles = new ArrayList<Role>();
		Iterator<UserRole> userRoles = user.getUserRoles(c).iterator();
		int primaryIndex = 0;
		for(int i=0; userRoles.hasNext(); i++) {
			UserRole ur = userRoles.next();
			assignedRoles.add(ur.getRole(c));
			if(ur.getPrimaryRole()) {
				primaryIndex = i;
			}
		}
		List<Role> availableRoles = Roles.getInstance().listAll(c);
		availableRoles.removeAll(assignedRoles);
		if(!includePrimary) {
			assignedRoles.remove(primaryIndex);
		}
		return new List[] {assignedRoles, availableRoles};
	}

	private void _validate(Connection c, Role r, AssociationMap<RolePermission> permissions) throws BLException {
		// At least one permission:
		if(permissions.size() == 0) {
			throw new FunctionalValidationException(L10n.EAP_VAL_ONE_PERMISSION_REQUIRED); 
		}
	}
}
