package com.netx.generics.R1.sql;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import java.io.PrintWriter;

import com.netx.basic.R1.eh.Checker;


public class DirectDataSource implements DataSource {

	private final Database _details;
	private int _timeout;
	private PrintWriter _logWriter;

	public DirectDataSource(Database details) {
		Checker.checkNull(details, "details");
		_details = details;
		_timeout = 0;
		_logWriter = null;
	}

	public Connection getConnection(String username, String password) throws SQLException {
		return DriverManager.getConnection(_details.getConnectionString(), username, password);
	}

	public Connection getConnection() throws SQLException {
		return getConnection(_details.getUsername(), _details.getPassword());
	}

	public int getLoginTimeout() {
		return _timeout;
	}
	
	public void setLoginTimeout(int timeout) {
		this._timeout = timeout;
	}
	
	public PrintWriter getLogWriter() {
		return _logWriter;
	}

	public void setLogWriter(PrintWriter p) {
		_logWriter = p;
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return Logger.getGlobal();
	}

	public boolean isWrapperFor(Class<?> iface) {
		return false;
	}
	
	public <T> T unwrap(Class<T> iface) {
		return null;
	}
}
