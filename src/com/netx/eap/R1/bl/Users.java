package com.netx.eap.R1.bl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import com.netx.basic.R1.eh.Checker;
import com.netx.bl.R1.core.*;


public class Users extends HolderEntity<UsersMetaData,User> {

	// TYPE:
	private static final String _POSSIBLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	public static Users getInstance() {
		return EAP.getUsers();
	}

	public static String generatePassword(int length) {
		char[] buffer = new char[length];
		Random random = new Random();
		for(int i=0; i<length; i++) {
			buffer[i] = _POSSIBLE.charAt(random.nextInt(_POSSIBLE.length()));
		}
		return new String(buffer);
	}

	// INSTANCE:
	private Select _qSelectUserByUsername;
	//private Select _qSelectUsersByRole;

	Users() {
		super(new UsersMetaData());
	}
	
	protected void onLoad() {
		// Initialize queries:
		_qSelectUserByUsername = createSelect("select-user-by-username", "SELECT * FROM eap_users WHERE username = ?");
		_qSelectUserByUsername.setUpdatesCache(true);
		// TODO use a global query for this
		//_qSelectUsersByRole = createSelect("select-users-by-role", "SELECT * FROM eap_users JOIN eap_user_roles ON eap_users.user_id=eap_user_roles.user_id WHERE eap_user_roles.role_id = ?");
		_qSelectUserByUsername.setUpdatesCache(true);
	}

	public void create(Connection c, User u, AssociationMap<UserRole> roles, AssociationMap<UserPermission> permissions) throws BLException, ReadOnlyFieldException {
		Checker.checkNull(u, "u");
		// Automatically generate user password:
		u.resetPassword();
		// Validations:
		_validate(c, roles, permissions);
		// Commit changes:
		synchronized(c) {
			c.startTransaction();
			insert(c, u);
			UserRoles.getInstance().save(c, roles);
			if(permissions != null) {
				UserPermissions.getInstance().save(c, permissions);
			}
			c.commit();
		}
	}

	public void save(Connection c, User u, AssociationMap<UserRole> roles, AssociationMap<UserPermission> permissions) throws BLException, ReadOnlyFieldException {
		Checker.checkNull(u, "u");
		// Validations:
		if(roles != null) {
			// Note: permissions may be null
			_validate(c, roles, permissions);
		}
		// Save changes:
		if(roles == null && permissions == null) {
			updateInstance(c, u);
		}
		else {
			synchronized(c) {
				c.startTransaction();
				updateInstance(c, u);
				if(roles != null) {
					UserRoles.getInstance().save(c, roles);
					// Note: we only update permissions if roles are provided as well.
					// Otherwise, we could be inserting duplicate permissions (since in
					// the absence of user roles, we cannot check duplicate permissions).
					if(permissions != null) {
						UserPermissions.getInstance().save(c, permissions);
					}
				}
				c.commit();
			}
		}
	}
	
	public User getUserByUsername(Connection c, String username) throws BLException {
		Checker.checkEmpty(username, "username");
		return selectInstance(c, _qSelectUserByUsername, username);
	}

	public List<User> listUsersByRole(Connection c, Long roleId) throws BLException {
		Checker.checkNull(roleId, "roleId");
		// TODO use a global query for this
		/*
		Argument[] args = new Argument[] {
			new Argument(EAP.getUserRolesMetaData().role_id, roleId)
		};
		return selectList(c, _qSelectUsersByRole, args);
		*/
		Checker.checkNull(roleId, "roleId");
		List<User> users = new ArrayList<User>();
		for(User u : selectAll(c)) {
			AssociationMap<UserRole> uRoles = u.getUserRoles(c);
			for(UserRole ur : uRoles) {
				if(ur.getRoleId().equals(roleId)) {
					users.add(u);
					break;
				}
			}
		}
		return users;
	}

	private void _validate(Connection c, AssociationMap<UserRole> roles, AssociationMap<UserPermission> permissions) throws BLException {
		// 1) at least one role
		if(roles.size() == 0) {
			throw new FunctionalValidationException(L10n.EAP_VAL_ONE_ROLE_REQUIRED); 
		}
		// 2) one role and only one can be primary
		int primaryCount = 0;
		Map<String,Long> permissionMap = new HashMap<String,Long>();
		for(UserRole ur : roles) {
			if(ur.getPrimaryRole()) {
				primaryCount++;
			}
			// Get all of the role's permissions:
			Role r = ur.getRole(c);
			for(RolePermission rp : r.getRolePermissions(c)) {
				permissionMap.put(rp.getPermissionId(), r.getRoleId());
			}
		}
		if(primaryCount != 1) {
			throw new FunctionalValidationException(L10n.EAP_VAL_ONE_PRIMARY_ROLE); 
		}
		// Permissions:
		if(permissions != null) {
			for(UserPermission up : permissions) {
				// 3) no duplicate permissions
				Long roleId = permissionMap.get(up.getPermissionId());
				if(roleId != null) {
					throw new FunctionalValidationException(L10n.EAP_VAL_DUPLICATE_USER_PERMISSION,
						up.getPermission(c).getName(),
						Roles.getInstance().get(c, roleId).getName()
					);
				}
			}
		}
	}
}
