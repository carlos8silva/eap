package com.netx.ut.bl.R1.core;
import com.netx.bl.R1.core.*;


public class OrdersMetaData extends TimedMetaData {

	// Fields:
	public final Field orderId	= new FieldLong(this, "orderId", "order_id", null, true, true, true, null, null);
	public final Field customerId	= new FieldForeignKey(this, "customerId", "customer_id", null, true, false, Seller.getCustomers().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.CASCADE);
	public final Field dateMade = new FieldDate(this, "dateMade", "date_made", "[date()]", true, true);
	public final Field status = new FieldChar(this, "status", "status", "N", true, false, true);

	// For Entities:
	OrdersMetaData() {
		super("Orders", "orders");
		addPrimaryKeyField(orderId);
		addField(customerId);
		addField(dateMade);
		addField(status);
		addDefaultFields();
	}

	public final DATA_TYPE getDataType() {
		return DATA_TYPE.TRANSACTIONAL;
	}

	public Class<Order> getInstanceClass() {
		return Order.class;
	}
	
	public Field getAutonumberKeyField() {
		return orderId;
	}
}
