package com.netx.ut.bl.R1.core;
import com.netx.bl.R1.core.*;


public class LineItem extends AssociationInstance<LineItemsMetaData,LineItems,Order> {

	// For Order and DAFacade:
	public LineItem(Long orderId, String productId) throws ValidationException {
		setPrimaryKey(getMetaData().orderId, orderId);
		setPrimaryKey(getMetaData().productId, productId);
	}

	public LineItems getEntity() {
		return LineItems.getInstance();
	}

	public Long getOrderId() throws BLException {
		return (Long)getValue(getMetaData().orderId);
	}

	public Order getOrder(Connection c) throws BLException {
		return getHolder(c);
	}

	public Order getHolder(Connection c) throws BLException {
		Long orderId = getOrderId();
		if(orderId == null) {
			return null;
		}
		return Orders.getInstance().get(c, orderId);
	}

	public Product getAssociatedInstance(Connection c) throws BLException {
		return getProduct(c);
	}

	public String getProductId() {
		return (String)getValue(getMetaData().productId);
	}

	public Product getProduct(Connection c) throws BLException {
		String value = getProductId();
		return value == null ? null : Products.getInstance().get(c, value);
	}

	public Integer getQuantity() {
		return (Integer)getValue(getMetaData().quantity);
	}

	public LineItem setQuantity(Integer quantity) throws ValidationException {
		safelySetValue(getMetaData().quantity, quantity);
		return this;
	}
}
