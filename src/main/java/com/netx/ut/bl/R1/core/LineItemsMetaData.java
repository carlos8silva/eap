package com.netx.ut.bl.R1.core;
import com.netx.bl.R1.core.*;


public class LineItemsMetaData extends TimedMetaData {

	// Fields:
	public final Field orderId	= new FieldForeignKey(this, "orderId", "order_id", null, true, true, Seller.getOrders().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.CASCADE);
	public final Field productId	= new FieldForeignKey(this, "productId", "product_id", null, true, true, Seller.getProducts().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.CASCADE);
	public final Field quantity = new FieldInt(this, "quantity", "quantity", Field.NULL, true, false, null, null);

	// For Entities:
	LineItemsMetaData() {
		super("LineItems", "line_items");
		addPrimaryKeyField(orderId);
		addPrimaryKeyField(productId);
		addField(quantity);
		addDefaultFields();
	}

	public final DATA_TYPE getDataType() {
		return DATA_TYPE.TRANSACTIONAL;
	}

	public Class<LineItem> getInstanceClass() {
		return LineItem.class;
	}

	public Field getAutonumberKeyField() {
		return null;
	}
}
