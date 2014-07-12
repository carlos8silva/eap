package com.netx.ut.bl.R1.core;
import java.util.List;
import com.netx.bl.R1.core.*;


public class Customers extends Entity<CustomersMetaData,Customer> {

	// TYPE:
	public static Customers getInstance() {
		return Seller.getCustomers();
	}
	
	// INSTANCE:
	private Select _qSelectActiveCustomers = null;
	private Select _qSelectFirstCustomerId = null;

	Customers() {
		super(new CustomersMetaData());
	}

	protected void onLoad() {
		_qSelectActiveCustomers = createSelect("select-active-customers", "SELECT * FROM customers WHERE active='T' ORDER BY name");
		_qSelectActiveCustomers.setUpdatesCache(true);
		_qSelectFirstCustomerId = getRepository().createSelect("select-first-customer-id", "SELECT customer_id FROM customers ORDER BY customer_id ASC LIMIT 0, 1");
	}

	public void create(Connection c, Customer customer) throws BLException, ValidationException {
		insert(c, customer);
	}

	public void update(Connection c, Customer customer) throws BLException {
		updateInstance(c, customer);
	}

	public void delete(Connection c, Customer customer) throws BLException {
		deleteInstance(c, customer);
	}

	public int clear(Connection c) throws BLException {
		return deleteAll(c);
	}

	public List<Customer> listActiveCustomers(Connection c) throws BLException {
		return selectList(c, _qSelectActiveCustomers);
	}

	public Long getFirstCustomerId(Connection c) throws BLException {
		Results customerIds = c.select(_qSelectFirstCustomerId);
		if(customerIds.getRowCount() == 0) {
			throw new IllegalStateException("no customers found");
		}
		else {
			return customerIds.getRows().get(0).getLong(1);
		}
	}
	
	public void testWrongQuery(Connection c) throws BLException {
		selectList(c, Orders.getInstance().qSelectOrdersByStatus);
	}
}
