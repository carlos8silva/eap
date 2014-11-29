package com.netx.generics.R1.sql;
import java.sql.SQLException;


public class PoolExhaustedException extends SQLException {

	private static String _MESSAGE = "connection pool is exhausted";
	
	PoolExhaustedException() {
		super(_MESSAGE);
	}
}
