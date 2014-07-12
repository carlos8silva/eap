package com.netx.ut.bl.R1.core;
import com.netx.bl.R1.core.*;


public class Customer extends TimedInstance<CustomersMetaData,Customers> {

	public Customer() {
		super();
	}
	
	public Customer(Long customerId) throws ValidationException {
		setPrimaryKey(getMetaData().customerId, customerId);
	}

	public Customers getEntity() {
		return Customers.getInstance();
	}

	public Long getCustomerId() {
		return (Long)getValue(getMetaData().customerId);
	}

	public String getName() {
		return (String)getValue(getMetaData().name);
	}

	public Customer setName(String value) throws ValidationException {
		safelySetValue(getMetaData().name, value);
		return this;
	}

	public String getAddress() {
		return (String)getValue(getMetaData().address);
	}

	public Customer setAddress(String value) throws ValidationException {
		safelySetValue(getMetaData().address, value);
		return this;
	}

	public Boolean getActive() {
		return (Boolean)getValue(getMetaData().active);
	}

	public Customer setActive(Boolean value) throws ValidationException {
		safelySetValue(getMetaData().active, value);
		return this;
	}
}
