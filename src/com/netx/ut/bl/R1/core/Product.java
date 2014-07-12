package com.netx.ut.bl.R1.core;
import com.netx.bl.R1.core.*;


public class Product extends TimedInstance<ProductsMetaData,Products> {

	public Product(String productId) throws ValidationException {
		setPrimaryKey(getMetaData().productId, productId);
	}

	public Products getEntity() {
		return Products.getInstance();
	}
	
	public String getProductId() {
		return (String)getValue(getMetaData().productId);
	}

	public String getName() {
		return (String)getValue(getMetaData().name);
	}

	public Product setName(String name) throws ValidationException {
		safelySetValue(getMetaData().name, name);
		return this;
	}

	public Double getPrice() {
		return (Double)getValue(getMetaData().price);
	}

	public Product setPrice(Double price) throws ValidationException {
		safelySetValue(getMetaData().price, price);
		return this;
	}
}
