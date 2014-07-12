package com.netx.ut.bl.R1.core;
import com.netx.bl.R1.core.*;


public class Products extends Entity<ProductsMetaData,Product> {

	// TYPE:
	public static Products getInstance() {
		return Seller.getProducts();
	}
	
	// INSTANCE:
	Products() {
		super(new ProductsMetaData());
	}

	public void create(Connection c, Product p) throws BLException, ValidationException {
		insert(c, p);
	}

	public void save(Connection c, Product p) throws BLException, ValidationException {
		insertOrUpdate(c, p);
	}

	public int clear(Connection c) throws BLException {
		return deleteAll(c);
	}
}
