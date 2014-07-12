package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class Role extends HolderInstance<RolesMetaData,Roles> {

	public Role() {
	}

	public Role(Long roleId) throws ValidationException {
		setPrimaryKey(getMetaData().roleId, roleId);
	}

	public Roles getEntity() {
		return Roles.getInstance();
	}

	public Long getRoleId() {
		return (Long)getValue(getMetaData().roleId);
	}

	public String getName() {
		return (String)getValue(getMetaData().name);
	}

	public Role setName(String value) throws ValidationException {
		safelySetValue(getMetaData().name, value);
		return this;
	}

	public String getDescription() {
		return (String)getValue(getMetaData().description);
	}

	public Role setDescription(String value) throws ValidationException {
		safelySetValue(getMetaData().description, value);
		return this;
	}

	public Boolean getSystemRole() {
		return (Boolean)getValue(getMetaData().systemRole);
	}

	public String getBaseUIId() {
		return (String)getValue(getMetaData().baseUi);
	}

	public UserInterface getBaseUI(Connection c) throws BLException {
		String uiId = getBaseUIId();
		if(uiId == null) {
			return null;
		}
		else {
			return UserInterfaces.getInstance().get(c, uiId);
		}
	}

	public Role setBaseUIId(String value) throws ValidationException {
		safelySetValue(getMetaData().baseUi, value);
		return this;
	}

	public Role setBaseUI(UserInterface value) throws ValidationException {
		return setBaseUIId(value == null ? null : value.getUiId());
	}

	protected RolePermission createAssociationFor(MetaData metaData, Comparable<?> ... targetKey) throws ValidationException {
		return new RolePermission(getRoleId(), (String)targetKey[0]);
	}
	
	public AssociationMap<RolePermission> getRolePermissions(Connection c) throws BLException {
		return RolePermissions.getInstance().getAssociationsFor(c, this);
	}
}
