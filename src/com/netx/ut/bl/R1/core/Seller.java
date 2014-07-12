package com.netx.ut.bl.R1.core;
import com.netx.bl.R1.core.Domain;


public class Seller extends Domain {

	// TYPE:
	// Entities must be put in a specific order to prevent 
	// initialization problems with foreign keys. The order is:
	// - Reference data before transactional data
	// - Entities with fields relating to other entities must come last
	// Reference data:
	private static final Customers _customers = new Customers();
	private static final Products _products = new Products();
	private static final Users _users = new Users();
	private static final Offices _offices = new Offices();
	// Transactional data:
	private static final Orders _orders = new Orders();
	private static final LineItems _lineItems = new LineItems();
	private static final Interests _interests = new Interests();
	private static final InterestedParties _interestedParties = new InterestedParties();

	// Entity getters:
	public static Customers getCustomers() {
		return _customers;
	}

	public static Products getProducts() {
		return _products;
	}

	public static Users getUsers() {
		return _users;
	}

	public static Offices getOffices() {
		return _offices;
	}

	public static Orders getOrders() {
		return _orders;
	}

	public static LineItems getLineItems() {
		return _lineItems;
	}

	public static Interests getInterests() {
		return _interests;
	}

	public static InterestedParties getInterestedParties() {
		return _interestedParties;
	}
}
