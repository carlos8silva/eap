package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class UserInterface extends TimedInstance<UserInterfacesMetaData,UserInterfaces> {

	public UserInterface(String uiId) throws ValidationException {
		setPrimaryKey(getMetaData().uiId, uiId);
	}

	public UserInterfaces getEntity() {
		return UserInterfaces.getInstance();
	}

	public String getUiId() {
		return (String)getValue(getMetaData().uiId);
	}

	public String getName() {
		return (String)getValue(getMetaData().name);
	}

	public UserInterface setName(String name) {
		safelySetValue(getMetaData().name, name);
		return this;
	}

	public String getFunctionId() {
		return (String)getValue(getMetaData().functionId);
	}

	public FunctionInstance getFunction(Connection c) throws BLException {
		String id = getFunctionId();
		if(id == null) {
			return null;
		}
		else {
			return Functions.getInstance().get(c, id);
		}
	}

	public UserInterface setFunctionId(String value) {
		safelySetValue(getMetaData().functionId, value);
		return this;
	}

	public UserInterface setFunction(FunctionInstance value) {
		safelySetValue(getMetaData().functionId, value==null ? null : value.getAlias());
		return this;
	}
}
