package com.netx.ut.bl.R1.core;
import com.netx.bl.R1.core.*;


public class InterestedParties extends Association<InterestedPartiesMetaData,InterestedParty> {

	// TYPE:
	public static InterestedParties getInstance() {
		return Seller.getInterestedParties();
	}
	
	// INSTANCE:
	InterestedParties() {
		super(new InterestedPartiesMetaData(), Seller.getInterests(), Seller.getOffices());
	}

	// For Interests:
	protected int save(Connection c, AssociationMap<InterestedParty> interestedParties) throws BLException {
		return insertOrUpdate(c, interestedParties);
	}
}
