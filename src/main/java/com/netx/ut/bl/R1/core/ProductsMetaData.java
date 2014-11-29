package com.netx.ut.bl.R1.core;
import com.netx.bl.R1.core.*;


public class ProductsMetaData extends TimedMetaData {

	// Fields:
	public final Field productId = new FieldText(this, "productId", "product_id", null, true, false, 0, 10, true, null, null);
	public final Field name = new FieldText(this, "name", "name", null, true, false, 0, 50, false, null, null);
	public final Field price = new FieldDouble(this, "price", "price", null, true, false, null, null);

	// For Entities:
	ProductsMetaData() {
		super("Products", "products");
		addPrimaryKeyField(productId);
		addField(name);
		addField(price);
		addDefaultFields();
	}

	public final DATA_TYPE getDataType() {
		return DATA_TYPE.REFERENCE;
	}

	public Class<Product> getInstanceClass() {
		return Product.class;
	}
	
	public Field getAutonumberKeyField() {
		return null;
	}
}
