package com.netx.ut.lib.external;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.IOException;
import java.sql.Connection;
import java.util.Iterator;

import com.netx.generics.R1.util.UnitTester;


public class NTApache extends UnitTester {

	public static void main(String[] args) throws Throwable {
		NTApache nt = new NTApache();
		nt.setUp();
		//nt.dbcp_testDbcpPool();
		nt.poi_readExcelSpreadsheet();
		nt.println("done.");
	}

	public void setUp() throws Exception {
		// Load MySQL JDBC driver:
		Class.forName("com.mysql.jdbc.Driver");
	}

	public void dbcp_testDbcpPool() throws Exception {
		GenericObjectPool connectionPool = new GenericObjectPool(null);
		// These two method calls are needed:
		DriverManagerConnectionFactory connectionFactory = new DriverManagerConnectionFactory("jdbc:mysql://localhost:3306/unit_tests?autoReconnect=true", "root", "eagle");
		new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);
		PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
		connectionPool.setMaxActive(2);
		Connection c = null;
		c = dataSource.getConnection();
		c = dataSource.getConnection();
		c.close();
		println(connectionPool.getNumActive());
		println(connectionPool.getNumIdle());
		println(connectionPool.getMaxActive());
	}

	public void poi_readExcelSpreadsheet() throws IOException {
		POIFSFileSystem fs = new POIFSFileSystem(getClass().getClassLoader().getResourceAsStream("L10n-store.xls"));
	    HSSFWorkbook wb = new HSSFWorkbook(fs);
	    HSSFSheet sheet = wb.getSheetAt(0);
	    HSSFRow row = sheet.getRow(0);
	    println("first row has "+row.getLastCellNum()+" columns");
	    for(Iterator<?> it = row.cellIterator(); it.hasNext(); ) {
		    HSSFCell cell = (HSSFCell)it.next();
	    	println(cell.getRichStringCellValue().toString());
	    }
	}
}
