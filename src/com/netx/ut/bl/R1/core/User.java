package com.netx.ut.bl.R1.core;
import com.netx.bl.R1.core.*;


public class User extends TimedInstance<UsersMetaData,Users> {

	public User(String name) throws ValidationException {
		setPrimaryKey(getMetaData().name, name);
	}

	public Users getEntity() {
		return Users.getInstance();
	}

	public String getName() {
		return (String)getValue(getMetaData().name);
	}

	public User setName(String name) throws ValidationException {
		safelySetValue(getMetaData().name, name);
		return this;
	}

	public String getCity() {
		return (String)getValue(getMetaData().city);
	}

	public User setCity(String city) throws ValidationException {
		safelySetValue(getMetaData().city, city);
		return this;
	}

	public Integer getAge() {
		return (Integer)getValue(getMetaData().age);
	}

	public User setAge(Integer age) throws ValidationException {
		safelySetValue(getMetaData().age, age);
		return this;
	}

	public String getUsername() {
		return (String)getValue(getMetaData().username);
	}

	public User setUsername(String username) throws ValidationException {
		safelySetValue(getMetaData().username, username);
		return this;
	}
}
