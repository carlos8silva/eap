package com.netx.rit.R1.bl;
import com.netx.bl.R1.core.*;


public class Items extends Entity<ItemsMetaData,Item> {

	// TYPE:
	public static Items getInstance() {
		return RIT.getItems();
	}

	// INSTANCE:
	Items() {
		super(new ItemsMetaData());
	}
}
