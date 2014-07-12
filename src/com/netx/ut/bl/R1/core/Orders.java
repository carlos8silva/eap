package com.netx.ut.bl.R1.core;
import java.util.List;
import com.netx.basic.R1.eh.Checker;
import com.netx.bl.R1.core.*;
import com.netx.ut.bl.R1.core.Order.Status;


public class Orders extends HolderEntity<OrdersMetaData,Order> {

	// TYPE:
	public static Orders getInstance() {
		return Seller.getOrders();
	}

	// INSTANCE:
	private Select _qSelectOrdersFromInactiveCustomers = null;
	private Select _qSelectOrdersForCustomer = null;
	private Update _qUpdateStatusFor = null;
	private Update _qDeleteOrdersByStatus = null;
	// Note: this query is public to be used as a negative test condition (see Customers)
	public Select qSelectOrdersByStatus = null;

	Orders() {
		super(new OrdersMetaData());
	}

	protected void onLoad() {
		qSelectOrdersByStatus = createSelect("select-orders-by-status", "SELECT * FROM orders WHERE status = ?");
		qSelectOrdersByStatus.setUpdatesCache(true);
		_qSelectOrdersFromInactiveCustomers = createSelect("select-orders-from-inactive-customers", "SELECT orders.* FROM orders JOIN customers ON orders.customer_id=customers.customer_id WHERE customers.active='F'");
		_qSelectOrdersForCustomer = createSelect("select-orders-for-customer", "SELECT * FROM orders WHERE customer_id = ?");
		_qUpdateStatusFor = createUpdate("updateStatusFor", "UPDATE orders SET status = ? WHERE customer_id = ? AND status = ?");
		_qDeleteOrdersByStatus = createUpdate("deleteOrder", "DELETE FROM orders WHERE status = ?");
	}

	public void create(Connection c, Order order, AssociationMap<LineItem> lineItems) throws BLException, ValidationException {
		synchronized(c) {
			c.startTransaction();
			insert(c, order);
			if(lineItems != null) {
				LineItems.getInstance().save(c, lineItems);
			}
			c.commit();
		}
	}

	public void update(Connection c, Order order, AssociationMap<LineItem> lineItems) throws BLException, ReadOnlyFieldException { 
		synchronized(c) {
			c.startTransaction();
			updateInstance(c, order);
			if(lineItems != null) {
				LineItems.getInstance().save(c, lineItems);
			}
			c.commit();
		}
	}
	
	public void delete(Connection c, Order order) throws BLException {
		synchronized(c) {
			c.startTransaction();
			deleteInstance(c, order);
			c.commit();
		}
	}

	public int clear(Connection c) throws BLException {
		return deleteAll(c);
	}

	public void dispatchOrdersFor(Connection c, Long customerId) throws BLException, ValidationException {
		Checker.checkNull(customerId, "customerId");
		_updateStatusFor(c, Status.DISPATCHED, customerId, Status.NEW);
	}

	public void payOrdersFor(Connection c, Long customerId) throws BLException, ValidationException {
		Checker.checkNull(customerId, "customerId");
		_updateStatusFor(c, Status.PAID, customerId, Status.DISPATCHED);
	}

	public List<Order> listOrdersByStatus(Connection c, Status status) throws BLException {
		Checker.checkNull(status, "status");
		return selectList(c, qSelectOrdersByStatus, status);
	}

	public List<Order> listOrdersFromInactiveCustomers(Connection c) throws BLException {
		return selectList(c, _qSelectOrdersFromInactiveCustomers);
	}

	public List<Order> listOrdersForCustomer(Connection c, Long customerId) throws BLException {
		Checker.checkNull(customerId, "customerId");
		return selectList(c, _qSelectOrdersForCustomer, customerId);
	}
	
	public void testWrongQueryType(Connection c) throws BLException {
		updateList(c, _qDeleteOrdersByStatus);
	}

	private int _updateStatusFor(Connection c, Status newStatus, Long customerId, Status currentStatus) throws BLException, ValidationException {
		Checker.checkNull(newStatus, "newStatus");
		Checker.checkNull(customerId, "customerId");
		Checker.checkNull(currentStatus, "currentStatus");
		return updateList(c, _qUpdateStatusFor, newStatus, customerId, currentStatus);
	}
}
