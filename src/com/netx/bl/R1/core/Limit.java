package com.netx.bl.R1.core;

public class Limit {

	public final Integer offset;
	public final Integer numRows;
	
	public Limit(Integer offset, Integer numRows) {
		this.offset = offset;
		this.numRows = numRows;
	}
	
	public int getParameterCount() {
		int paramCount = 0;
		if(offset == null) {
			paramCount++;
		}
		if(numRows == null) {
			paramCount++;
		}
		return paramCount;
	}
	
	public String toString() {
		return "LIMIT "+offset+", "+numRows;
	}
}
