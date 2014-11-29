package com.netx.ut.generics.R1;
import java.sql.*;
import com.netx.generics.R1.sql.Database;
import com.netx.generics.R1.sql.ConnectionPool;
import com.netx.generics.R1.sql.JdbcDriver;
import com.netx.generics.R1.sql.PoolListenerAdapter;
import com.netx.generics.R1.util.UnitTester;


// TODO use assertions
// TODO use unit_test database
public class NTSql extends UnitTester {

	// TYPE:
	public static void main(String[] args) throws Throwable {
		NTSql nt = new NTSql();
		//ut.testConnectionPool();
		//ut.testIdleConnections();
		nt.testClosePool();
		//ut.testConcurrentUpdate();
	}
	
	// INSTANCE:
	public void testConnectionPool() throws SQLException {
		println("testConnectionPool:");
		ConnectionPool pool = _createConnectionPool();
		Connection c1 = pool.getConnection();
		println("c1: "+c1);
		Connection c2 = pool.getConnection();
		println("c2: "+c2);
		Connection c3 = pool.getConnection();
		println("c3: "+c3);
		c1.close();
		Connection c4 = pool.getConnection();
		println("c4: "+c4+" (should be equal to c1)");
		c2.close();
		c3.close();
		c4.close();
	}

	public void testIdleConnections() throws SQLException {
		println("testIdleConnections:");
		ConnectionPool pool = _createConnectionPool();
		// TODO move this to its own test case
		pool.addPoolListener(new PoolListenerAdapter() {
			public void onDestroy(Object obj) {
				println("connection released: "+obj);
			}
		});
		pool.setMaxActive(2);
		// If we close two connections, one of them will be phisically closed.
		pool.setMaxIdle(1);
		Connection c1 = pool.getConnection();
		println("c1: "+c1);
		Connection c2 = pool.getConnection();
		println("c2: "+c2);
		c1.close();
		c2.close();
		println("one connection should be new:");
		c1 = pool.getConnection();
		println("c1: "+c1);
		c2 = pool.getConnection();
		println("c2: "+c2);
		c1.close();
		c2.close();
		println("closing pool.");
		pool.close();
	}

	// See how any open connections are automatically closed by the pool:
	public void testClosePool() throws SQLException {
		println("testClosePool:");
		ConnectionPool pool = _createConnectionPool();
		pool.getConnection();
		pool.getConnection();
		pool.getConnection();
		pool.close();
	}

	public void testConcurrentUpdate() throws SQLException {
		println("testConcurrentUpdate:");
		ConnectionPool pool = _createConnectionPool();
		Connection c1 = pool.getConnection();
		Connection c2 = pool.getConnection();
		//
		// If we uncomment the lines below, the application will stop
		// (because there is a lock on the table caused by the first update)
		// Please note, however, that if we change the query for connection to 2 to be
		// a SELECT, it goes ahead without waiting on the update to be committed.
		//
		//c1.setAutoCommit(false);
		Statement stmt = c1.createStatement();
		stmt.executeUpdate("UPDATE products SET name = 'Baked Beans' WHERE product_id = 'P01'");
		stmt = c2.createStatement();
		stmt.executeUpdate("UPDATE products SET name = 'Regular Beans' WHERE product_id = 'P01'");
		//c1.commit();
		c1.close();
		c2.close();
	}
	
	private ConnectionPool _createConnectionPool() {
		Database db = new Database(JdbcDriver.MYSQL, "localhost", 3306, "unit_tests", "root", "eagle");
		return new ConnectionPool(db);
	}
}
