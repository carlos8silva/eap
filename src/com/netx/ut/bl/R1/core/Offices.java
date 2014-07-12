package com.netx.ut.bl.R1.core;
import com.netx.bl.R1.core.*;


public class Offices extends Entity<OfficesMetaData,Office> {

	// TYPE:
	public static Offices getInstance() {
		return Seller.getOffices();
	}
	
	// INSTANCE:
	Offices() {
		super(new OfficesMetaData());
	}
}
