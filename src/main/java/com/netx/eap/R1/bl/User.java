package com.netx.eap.R1.bl;
import com.netx.basic.R1.eh.Checker;
import com.netx.bl.R1.core.*;
import com.netx.generics.R1.time.Timestamp;
import com.netx.basic.R1.eh.IntegrityException;
// USER DEFINED IMPORTS:


public class User extends HolderInstance<UsersMetaData,Users> {

	// TYPE:
	public static enum LockedReason implements AllowedValue<String> {
		FAILED_LOGINS("FL"), USER_MANAGER("UM");
		private final String _value;
		private LockedReason(String value) { _value = value; }
		public String getCode() { return _value; }
	};

	// INSTANCE:
	public User() {
	}

	public User(Long userId) throws ValidationException {
		setPrimaryKey(getMetaData().userId, userId);
	}

	public Users getEntity() {
		return Users.getInstance();
	}

	public Long getUserId() {
		return (Long)getValue(getMetaData().userId);
	}

	public String getUsername() {
		return (String)getValue(getMetaData().username);
	}

	public User setUsername(String value) throws ValidationException {
		safelySetValue(getMetaData().username, value);
		return this;
	}

	public String getOldUsername() {
		return (String)getValue(getMetaData().oldUsername);
	}

	public User setOldUsername(String value) throws ValidationException {
		safelySetValue(getMetaData().oldUsername, value);
		return this;
	}

	public String getPassword() {
		return (String)getValue(getMetaData().password);
	}

	public User setPassword(String value) throws ValidationException {
		safelySetValue(getMetaData().password, value);
		return this;
	}

	public String getFirstName() {
		return (String)getValue(getMetaData().firstName);
	}

	public User setFirstName(String value) throws ValidationException {
		safelySetValue(getMetaData().firstName, value);
		return this;
	}

	public String getLastName() {
		return (String)getValue(getMetaData().lastName);
	}

	public User setLastName(String value) throws ValidationException {
		safelySetValue(getMetaData().lastName, value);
		return this;
	}

	public Character getMiddleInitial() {
		return (Character)getValue(getMetaData().middleInitial);
	}

	public User setMiddleInitial(String value) throws ValidationException {
		safelySetValue(getMetaData().middleInitial, value);
		return this;
	}

	public Boolean getHelpOn() {
		return (Boolean)getValue(getMetaData().helpOn);
	}

	public User setHelpOn(Boolean value) throws ValidationException {
		safelySetValue(getMetaData().helpOn, value);
		return this;
	}

	public Timestamp getTimeLocked() {
		return (Timestamp)getValue(getMetaData().timeLocked);
	}

	public User setTimeLocked(Timestamp value) throws ValidationException {
		safelySetValue(getMetaData().timeLocked, value);
		return this;
	}

	public Timestamp getTimeDisabled() {
		return (Timestamp)getValue(getMetaData().timeDisabled);
	}

	public User setTimeDisabled(Timestamp value) throws ValidationException {
		safelySetValue(getMetaData().timeDisabled, value);
		return this;
	}

	public LockedReason getLockedReason() {
		String lockedReason = (String)getValue(getMetaData().lockedReason);
		return (LockedReason)getAllowedValue(LockedReason.class, lockedReason);
	}

	public User setLockedReason(String value) throws ValidationException {
		safelySetValue(getMetaData().lockedReason, value);
		return this;
	}
	
	public User setLockedReason(LockedReason value) throws ValidationException {
		return setLockedReason(value == null ? null : value.getCode());
	}	
	
	public Integer getFailedLoginAttempts() {
		return (Integer)getValue(getMetaData().failedLoginAttempts);
	}
	
	public User setFailedLoginAttempts(Integer value) throws ValidationException {
		safelySetValue(getMetaData().failedLoginAttempts, value);
		return this;
	}

	public Integer getSessionTimeoutTime() {
		return (Integer)getValue(getMetaData().sessionTimeoutTime);
	}

	public User setSessionTimeoutTime(Integer value) throws ValidationException {
		safelySetValue(getMetaData().sessionTimeoutTime, value);
		return this;
	}

	public Timestamp getPasswordChangeTime() {
		return (Timestamp)getValue(getMetaData().passwordChangeTime);
	}

	public User setPasswordChangeTime(Timestamp value) throws ValidationException {
		safelySetValue(getMetaData().passwordChangeTime, value);
		return this;
	}

	public Integer getSecurityQuestion1() {
		return (Integer)getValue(getMetaData().securityQuestion1);
	}

	public SecurityQuestion getSecurityQuestion1(Connection c) throws BLException {
		Integer id = getSecurityQuestion1();
		if(id == null) {
			return null;
		}
		return SecurityQuestions.getInstance().get(c, id);
	}

	public User setSecurityQuestion1(Integer value) throws ValidationException {
		safelySetValue(getMetaData().securityQuestion1, value);
		return this;
	}
	
	public User setSecurityQuestion1(SecurityQuestion value) throws ValidationException {
		return setSecurityQuestion1(value == null ? null : value.getSeqQuestionId());
	}

	public Integer getSecurityQuestion2() {
		return (Integer)getValue(getMetaData().securityQuestion2);
	}

	public SecurityQuestion getSecurityQuestion2(Connection c) throws BLException {
		Integer id = getSecurityQuestion2();
		if(id == null) {
			return null;
		}
		return SecurityQuestions.getInstance().get(c, id);
	}

	public User setSecurityQuestion2(Integer value) throws ValidationException {
		safelySetValue(getMetaData().securityQuestion2, value);
		return this;
	}
	
	public User setSecurityQuestion2(SecurityQuestion value) throws ValidationException {
		return setSecurityQuestion2(value == null ? null : value.getSeqQuestionId());
	}

	public String getSecurityAnswer1() {
		return (String)getValue(getMetaData().securityAnswer1);
	}

	public User setSecurityAnswer1(String value) throws ValidationException {
		safelySetValue(getMetaData().securityAnswer1, value);
		return this;
	}

	public String getSecurityAnswer2() {
		return (String)getValue(getMetaData().securityAnswer2);
	}

	public User setSecurityAnswer2(String value) throws ValidationException {
		safelySetValue(getMetaData().securityAnswer2, value);
		return this;
	}

	protected AssociationInstance<?,?,?> createAssociationFor(MetaData metaData, Comparable<?> ... targetKey) throws ValidationException {
		if(metaData == EAP.getUserRoles().getMetaData()) {
			return new UserRole(getUserId(), (Long)targetKey[0]);
		}
		if(metaData == EAP.getUserPermissions().getMetaData()) {
			return new UserPermission(getUserId(), (String)targetKey[0]);
		}
		throw new IntegrityException(metaData);
	}

	public AssociationMap<UserRole> getUserRoles(Connection c) throws BLException {
		return UserRoles.getInstance().getAssociationsFor(c, this);
	}

	public AssociationMap<UserPermission> getUserPermissions(Connection c) throws BLException {
		return UserPermissions.getInstance().getAssociationsFor(c, this);
	}

	// USER DEFINED METHODS:
	public static enum STATUS {ACTIVE, LOCKED, DISABLED};

	public STATUS getStatus() {
		if(getTimeDisabled() != null) {
			return STATUS.DISABLED;
		}
		if(getTimeLocked() != null) {
			return STATUS.LOCKED;
		}
		return STATUS.ACTIVE;
	}

	public String getFullName() {
		return getFullName(true);
	}

	public String getFullName(boolean lastFirst) {
		StringBuilder sb = new StringBuilder();
		Character middleInitial = getMiddleInitial();
		if(lastFirst) {
			sb.append(getLastName());
			sb.append(", ");
			sb.append(getFirstName());
			if(middleInitial != null) {
				sb.append(" ");
				sb.append(middleInitial);
			}
		}
		else {
			sb.append(getFirstName());
			if(middleInitial != null) {
				sb.append(" ");
				sb.append(middleInitial);
			}
			sb.append(" ");
			sb.append(getLastName());
		}
		return sb.toString();
	}

	public Role getPrimaryRole(Connection c) throws BLException {
		for(UserRole ur : getUserRoles(c)) {
			if(ur.getPrimaryRole()) {
				return ur.getRole(c);
			}
		}
		throw new IntegrityException();
	}
	
	public User lock(LockedReason reason) {
		Checker.checkNull(reason, "reason");
		setTimeLocked(new Timestamp());
		setLockedReason(reason);
		return this;
	}

	public User unlock() {
		setTimeLocked((Timestamp)null);
		setFailedLoginAttempts(0);
		setLockedReason((String)null);
		return this;
	}

	public User resetPassword() {
		setPassword(Users.generatePassword(8));
		setPasswordChangeTime((Timestamp)null);
		return this;
	}

	public User disable() throws BLException {
		setOldUsername(getUsername());
		setUsername("user-"+getUserId());
		setTimeDisabled(new Timestamp());
		setFailedLoginAttempts(0);
		setTimeLocked((Timestamp)null);
		setLockedReason((String)null);
		setSecurityQuestion1((Integer)null);
		setSecurityAnswer1(null);
		setSecurityQuestion2((Integer)null);
		setSecurityAnswer2(null);
		return this;
	}

	public User enable(String newUsername) throws BLException {
		setUsername(newUsername);
		setOldUsername(null);
		setTimeDisabled((Timestamp)null);
		setTimeLocked((Timestamp)null);
		setFailedLoginAttempts(0);
		return this;
	}
}
