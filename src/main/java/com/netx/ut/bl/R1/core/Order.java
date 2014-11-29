package com.netx.ut.bl.R1.core;
import com.netx.generics.R1.time.Date;
import com.netx.bl.R1.core.*;


public class Order extends HolderInstance<OrdersMetaData,Orders> {

	// TYPE:
	public static enum Status implements AllowedValue<Character> {
		NEW('N'), PAID('P'), DISPATCHED('D'), RECEIVED('R');
		private final Character _value;
		private Status(Character value) { _value = value; }
		public Character getCode() { return _value; }
	};
	
	// INSTANCE:
	public Order() {
	}

	public Order(Long orderId) throws ValidationException {
		setPrimaryKey(getMetaData().orderId, orderId);
	}

	public Orders getEntity() {
		return Orders.getInstance();
	}

	public Long getOrderId() {
		return (Long)getValue(getMetaData().orderId);
	}

	public Customer getCustomer(Connection c) throws BLException {
		Long id = getCustomerId();
		if(id == null) {
			return null;
		}
		return Customers.getInstance().get(c, id);
	}

	public Long getCustomerId() throws BLException {
		return (Long)getValue(getMetaData().customerId);
	}

	public Order setCustomerId(Long value) throws ValidationException {
		safelySetValue(getMetaData().customerId, value);
		return this;
	}
	
	public Order setCustomer(Customer value) throws ValidationException {
		return setCustomerId(value == null ? null : value.getCustomerId());
	}

	public Date getDateMade() {
		return (Date)getValue(getMetaData().dateMade);
	}

	public Order setDateMade(Date value) throws ValidationException {
		safelySetValue(getMetaData().dateMade, value);
		return this;
	}

	public Status getStatus() {
		Character value = (Character)getValue(getMetaData().status);
		return (Status)getAllowedValue(Status.class, value);
	}

	public Order setStatus(Character value) throws ValidationException {
		safelySetValue(getMetaData().status, value);
		return this;
	}
	
	public Order setStatus(Status value) throws ValidationException {
		return setStatus(value == null ? null : value.getCode());
	}	

	protected LineItem createAssociationFor(MetaData metaData, Comparable<?> ... targetKey) throws ValidationException {
		return new LineItem(getOrderId(), (String)targetKey[0]);
	}

	public AssociationMap<LineItem> getLineItems(Connection c) throws BLException {
		return LineItems.getInstance().getAssociationsFor(c, this);
	}
}
