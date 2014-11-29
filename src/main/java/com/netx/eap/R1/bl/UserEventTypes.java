package com.netx.eap.R1.bl;
import java.util.List;
import com.netx.bl.R1.core.*;


public class UserEventTypes extends Entity<UserEventTypesMetaData,UserEventType> {

	// TYPE:
	// Note: these constants are added by the developer, not automatically generated:
	public static final int ERROR = 1;
	public static final int WARNING = 2;
	public static final int PAGE_DISPLAYED = 3;
	public static final int FUNCTION_USED = 4;
	public static final int INFORMATION_SUBMITTED = 5;
	public static final int LOGGED_IN = 6;
	public static final int LOGIN_FAILED = 7;
	public static final int ACCOUNT_LOCKED = 8;
	public static final int LOGGED_OUT = 9;
	public static final int TRANSGRESSION = 10;
	public static final int PASS_RESET_SUCCESSFUL = 11;
	public static final int PASS_RESET_FAILED = 12;
	public static final int PASS_CHANGE_SUCCESSFULL = 13;
	public static final int PASS_CHANGE_FAILED = 14;
	public static final int IP_ADDRESS_CHANGE_DETECTED = 15;

	public static UserEventTypes getInstance() {
		return EAP.getUserEventTypes();
	}

	// INSTANCE:
	UserEventTypes() {
		super(new UserEventTypesMetaData());
	}
	
	public List<UserEventType> listAll(Connection c) throws BLException {
		return selectAll(c);
	}	
}
