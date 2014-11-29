package com.netx.eap.R1.bl;
import java.util.List;
import com.netx.bl.R1.core.*;


public class UserInterfaces extends Entity<UserInterfacesMetaData,UserInterface> {

	// TYPE:
	public static UserInterfaces getInstance() {
		return EAP.getUserInterfaces();
	}

	// INSTANCE:
	UserInterfaces() {
		super(new UserInterfacesMetaData());
	}
	
	public List<UserInterface> listAll(Connection c) throws BLException {
		return selectAll(c);
	}
}
