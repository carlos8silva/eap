package com.netx.ut.bl.R1.core;
import com.netx.bl.R1.core.*;


public class CustomersMetaData extends TimedMetaData {

	// Fields:
	public final Field customerId	= new FieldLong(this, "customerId", "customer_id", null, true, true, true, null, null);
	public final Field name = new FieldText(this, "name", "name", null, true, false, 0, 50, true, null, null);
	public final Field address = new FieldText(this, "address", "address", null, false, false, 0, 200, false, null, null);
	public final Field active = new FieldBoolean(this, "active", "active", new Boolean(true), true, false);

	// For Entities:
	CustomersMetaData() {
		super("Customers", "customers");
		addPrimaryKeyField(customerId);
		addField(name);
		addField(address);
		addField(active);
		addDefaultFields();
	}

	public final DATA_TYPE getDataType() {
		return DATA_TYPE.REFERENCE;
	}

	public Class<Customer> getInstanceClass() {
		return Customer.class;
	}

	public Field getAutonumberKeyField() {
		return customerId;
	}
}
