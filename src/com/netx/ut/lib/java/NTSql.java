package com.netx.ut.lib.java;
import java.sql.*;


public class NTSql extends com.netx.generics.R1.util.UnitTester {

	public static void main(String[] args) throws Throwable {
		NTSql nt = new NTSql();
		//nt.testResultSetEmpty();
		nt.testResultSetWrongType();
		//nt.viewCatalogsAndSchemas();
		//nt.viewVersionNumbers();
		//nt.testPreparedStatement();
		nt.println("done.");
	}

	public void testResultSetEmpty() throws Throwable {
		Connection c = _openConnection("unit_tests");
		Statement stmt = c.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE user_id=4");
		rs.next();
		println("getInt: "+rs.getInt("inteiro"));
		println("getString: "+rs.getString("inteiro"));
		c.close();
	}

	public void testResultSetWrongType() throws Throwable {
		Connection c = _openConnection("unit_tests");
		Statement stmt = c.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM users");
		rs.next();
		println("getInt: "+rs.getInt("username"));
		c.close();
	}

	public void testRetrievePrimaryKey() throws Throwable {
		Connection c = _openConnection("unit_tests");
		Statement stmt = c.createStatement();
		stmt.execute("INSERT INTO users(nome, inteiro) VALUES (\"eee\", 2)", Statement.RETURN_GENERATED_KEYS);
		ResultSet rs = stmt.getGeneratedKeys();
		rs.next();
		println(rs.getInt(1));
		c.close();
	}

	public void testRetrieveUpdateCount() throws Throwable {
		Connection c = _openConnection("data_module_test");
		Statement stmt = c.createStatement();
		int count = stmt.executeUpdate("UPDATE utilizadores SET nome=\"AAA\" WHERE nome=\"BBB\"");
		println(count);
		c.close();
	}

	public void viewCatalogsAndSchemas() throws Throwable {
		Connection c = _openConnection("test_sql");
		DatabaseMetaData metadata = c.getMetaData();
		showObjectProperty(metadata, "getCatalogTerm");
		showObjectProperty(metadata, "getSchemaTerm");
		showObjectProperty(metadata, "isCatalogAtStart");
		showObjectProperty(metadata, "getCatalogSeparator");
		showObjectProperty(metadata, "getMaxCatalogNameLength");
		showObjectProperty(metadata, "supportsCatalogsInDataManipulation");
		showObjectProperty(metadata, "supportsCatalogsInIndexDefinitions");
		showObjectProperty(metadata, "supportsCatalogsInPrivilegeDefinitions");
		showObjectProperty(metadata, "supportsCatalogsInProcedureCalls");
		showObjectProperty(metadata, "supportsCatalogsInTableDefinitions");
		ResultSet rs = metadata.getCatalogs();
		println("[database catalogs:]");
		while(rs.next()) {
			println(rs.getString(1));
		}
		rs.close();
		rs = metadata.getSchemas();
		println("[database schemas:]");
		while(rs.next()) {
			println(rs.getString(1));
		}
		rs.close();
		rs = metadata.getTableTypes();
		println("[table types:]");
		while(rs.next()) {
			println(rs.getString(1));
		}
		rs.close();
		c.close();
	}

	public void viewTableNames() throws Throwable {
		Connection c = _openConnection("framework");
		DatabaseMetaData meta = c.getMetaData();
		String catalog = null;
		String schemaPattern = null;
		String tableNamePattern = null;
		String[] types = null;
		ResultSet rs = meta.getTables(catalog, schemaPattern, tableNamePattern, types);
		while(rs.next()) {
			println(rs.getString(3));
		}
		c.close();
	}

	public void testDatabaseMetaData() throws Throwable {
		Connection c = _openConnection("jdbc_test");
		DatabaseMetaData meta = c.getMetaData();
		ResultSet rs = meta.getColumns(null, null, "users", null);
		while(rs.next()) {
			println(rs.getString(4)+"\t"+rs.getString(6)+"\t"+rs.getInt(7));
		}
		c.close();
	}
	
	public void testConnection() throws Throwable {
		Connection c = _openConnection("jdbc_test");
		print("transaction isolation level: ");
		int tIsolation = c.getTransactionIsolation();
		if(tIsolation == Connection.TRANSACTION_NONE) {
			println("TRANSACTION_NONE");
		}
		else if(tIsolation == Connection.TRANSACTION_READ_COMMITTED) {
			println("TRANSACTION_READ_COMMITTED");
		}
		else if(tIsolation == Connection.TRANSACTION_READ_UNCOMMITTED) {
			println("TRANSACTION_READ_UNCOMMITTED");
		}
		else if(tIsolation == Connection.TRANSACTION_REPEATABLE_READ) {
			println("TRANSACTION_REPEATABLE_READ");
		}
		else if(tIsolation == Connection.TRANSACTION_SERIALIZABLE) {
			println("TRANSACTION_SERIALIZABLE");
		}
		println("catalog: "+c.getCatalog());
		c.setCatalog("cubigraf");
		println("catalog: "+c.getCatalog());
		c.close();
	}

	public void viewVersionNumbers() throws SQLException {
		Connection c = _openConnection("business_logic");
		DatabaseMetaData dmd = c.getMetaData();
		showObjectProperty(dmd, "getDatabaseMajorVersion");
		showObjectProperty(dmd, "getDatabaseMinorVersion");
		showObjectProperty(dmd, "getDatabaseProductName");
		showObjectProperty(dmd, "getDriverMajorVersion");
		showObjectProperty(dmd, "getDriverMinorVersion");
		showObjectProperty(dmd, "getDriverName");
		showObjectProperty(dmd, "getDriverVersion");
		showObjectProperty(dmd, "getJDBCMajorVersion");
		showObjectProperty(dmd, "getJDBCMinorVersion");
		c.close();
	}

	public void testPreparedStatement() throws SQLException {
		Connection c = _openConnection("unit_tests");
		String sql = "SELECT * FROM users WHERE name LIKE '%re%'";
		PreparedStatement ps = c.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		println("--- First try ---");
		while(rs.next()) {
			println(rs.getString("name"));
		}
		println("--- Second try ---");
		rs = ps.executeQuery("SELECT * FROM users");
		while(rs.next()) {
			println(rs.getString("name"));
		}
		c.close();
	}

	private Connection _openConnection(String database) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			return DriverManager.getConnection("jdbc:mysql://localhost:3306/"+database+"?autoReconnect=true", "root", "eagle");
		}
		catch(Exception e) {
			println("error opening database connection: "+e);
			return null;
		}
	}
}
