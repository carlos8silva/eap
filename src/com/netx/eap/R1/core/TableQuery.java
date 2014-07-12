package com.netx.eap.R1.core;

public class TableQuery {

	final String baseSQL;
	final String fromSQL;
	final String whereSQL;
	final String countSQL;
	
	public TableQuery(String baseSQL, String countSQL, String fromSQL, String whereSQL) {
		this.baseSQL = baseSQL;
		this.countSQL = countSQL;
		this.fromSQL = fromSQL;
		this.whereSQL = whereSQL;
	}
}
