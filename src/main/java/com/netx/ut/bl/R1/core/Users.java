package com.netx.ut.bl.R1.core;
import java.util.List;
import com.netx.basic.R1.eh.Checker;
import com.netx.bl.R1.core.*;


public class Users extends Entity<UsersMetaData,User> {

	// TYPE:
	public static Users getInstance() {
		return Seller.getUsers();
	}

	// INSTANCE:
	private Select _qSelectUsersByAge = null;
	private Select _qSelectUsersByAgeWithLimit = null;
	private Select _qSelectUsersByAgeWithParametrizedLimit = null;
	private Select _qSelectUserByUsername = null;
	private Select _qSelectUsersByName = null;

	Users() {
		super(new UsersMetaData());
	}

	protected void onLoad() {
		_qSelectUsersByAge = createSelect("select-users-by-age", "SELECT * FROM users ORDER BY age, city, name");
		// TODO add a use case to ensure that:
		// 1) when we run this query with cache set to anything other than FULL, ORDER BY and LIMIT are done in memory
		// 2) when we run this query with any other parameter, SQL has the ORDER BY and LIMIT clauses
		_qSelectUsersByAgeWithLimit = createSelect("select-users-by-age-with-limit", "SELECT * FROM users ORDER BY age, city, name LIMIT 2, 5");
		_qSelectUsersByAgeWithParametrizedLimit = createSelect("select-users-by-age-with-param-limit", "SELECT * FROM users ORDER BY age, city, name LIMIT ?, ?");
		_qSelectUserByUsername = createSelect("select-user-by-username", "SELECT * FROM users WHERE username = ?");
		_qSelectUserByUsername.setUpdatesCache(true);
		_qSelectUsersByName = createSelect("select-users-by-name", "SELECT * FROM users WHERE name LIKE ? ORDER BY name");
	}

	public List<User> listUsersByAge(Connection c) throws BLException {
		return selectList(c, _qSelectUsersByAge);
	}

	public List<User> listUsersByAgeWithLimit(Connection c) throws BLException {
		return selectList(c, _qSelectUsersByAgeWithLimit);
	}

	public List<User> listUsersByAgeOnRange(Connection c, int offset, int numRows) throws BLException {
		return selectList(c, _qSelectUsersByAgeWithParametrizedLimit, offset, numRows);
	}
	
	public User getUserByUsername(Connection c, String username) throws BLException {
		Checker.checkEmpty(username, "username");
		return selectInstance(c, _qSelectUserByUsername, username);
	}
	
	public List<User> findUsersByName(Connection c, String searchPattern) throws BLException {
		Checker.checkEmpty(searchPattern, "searchPattern");
		return selectList(c, _qSelectUsersByName, searchPattern);
	}
}
