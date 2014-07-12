package com.netx.generics.R1.sql;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.shared.Constants;


public class Database {

	private final JdbcDriver _driver;
	private final String _connectionString;
	private final String _username;
	private final String _password;
	
	public Database(JdbcDriver driver, String server, int port, String schema, String username, String password) {
		Checker.checkNull(driver, "driver");
		Checker.checkEmpty(server, "server");
		Checker.checkMinValue(port, 0, "port");
		Checker.checkEmpty(username, "username");
		Checker.checkEmpty(password, "password");
		if(Constants.EMPTY.equals(schema)) {
			schema = null;
		}
		// Load the JDBC driver:
		try {
			Class.forName(driver.getClassName());
		}
		catch(ClassNotFoundException cnfe) {
			throw new IllegalArgumentException(driver.getClassName()+": driver class not found");
		}
		_driver = driver;
		// If the port is 0, we will assume the default port of the driver:
		if(port == 0) {
			port = driver.getDefaultPort();
		}
		// Create the connection String:
		String tmp = driver.getURL();
		tmp = tmp.replace("<server>", server);
		tmp = tmp.replace("<port>", port+"");
		if(schema != null) {
			tmp = tmp.replace("<schema>", schema);
		}
		_connectionString = tmp;
		_username = username;
		_password = password;
	}

	public Database(JdbcDriver driver, String server, String schema, String username, String password) {
		this(driver, server, 0, schema, username, password);
	}

	public Database(JdbcDriver driver, String server, int port, String username, String password) {
		this(driver, server, port, username, username, password);
	}

	public Database(JdbcDriver driver, String server, String username, String password) {
		this(driver, server, 0, username, username, password);
	}

	public JdbcDriver getJdbcDriver() {
		return _driver;
	}

	public String getConnectionString() {
		return _connectionString;
	}

	public String getUsername() {
		return _username;
	}

	public String getPassword() {
		return _password;
	}
	
	public String toString() {
		return "[driver="+_driver.getName()+"] [connection="+_connectionString+"]";
	}

	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if(o == null) {
			return false;
		}
		if(o instanceof Database) {
			Database cd = (Database)o;
			return _connectionString.equals(cd.getConnectionString());
		}
		else {
			return false;
		}
	}

	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + _driver.hashCode();
		hash = 31 * hash + _connectionString.hashCode();
		hash = 31 * hash + _username.hashCode();
		hash = 31 * hash + _password.hashCode();
		return hash;
	}
}
