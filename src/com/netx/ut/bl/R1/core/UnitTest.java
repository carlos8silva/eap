package com.netx.ut.bl.R1.core;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import com.netx.generics.R1.sql.Database;
import com.netx.generics.R1.sql.JdbcDriver;
import com.netx.generics.R1.time.Date;
import com.netx.generics.R1.time.Timestamp;
import com.netx.generics.R1.util.Tools;
import com.netx.generics.R1.util.UnitTester;
import com.netx.generics.R1.util.Version;
import com.netx.basic.R1.eh.IllegalUsageException;
import com.netx.basic.R1.logging.Logger.LEVEL;
import com.netx.basic.R1.shared.Globals;
import com.netx.bl.R1.core.*;
import com.netx.bl.R1.spi.DatabaseDriver;
import com.netx.bl.R1.spi.DriverRegistry;
import com.netx.bl.R1.spi.ForeignKeyConstraintException;
import com.netx.bl.R1.spi.LoginFailedException;
import com.netx.bl.R1.spi.DatabaseUnavailableException;
import com.netx.ut.bl.R1.core.Interest.BenefitType;
import com.netx.ut.bl.R1.core.Interest.InterestType;
import com.netx.ut.bl.R1.core.Office.OfficeType;
import com.netx.ut.bl.R1.core.Order.Status;
import com.netx.eap.R1.bl.EAP;
import com.netx.rit.R1.bl.RIT;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;


// TODO TEST CASES:
// Changes to AssociationMap
// Attempt to changed cached entity instances to ensure cloning is working fine
// Disable cache and do the same select test (results should be the same)
// Ensure that the size of the cache is really limited (i.e. put a small limit on orders and observe)
// Decrease the max size of the cache to force one large cache empty.
// Change configuration to have cache.FULL and cache.NONE in all entities
// Validation errors
// Update with no updates
// Update with no updates on a transaction (e.g. HEI)
// Memory search using IS NULL and IS NOT NULL operators
// Entity.deleteList operations

public class UnitTest extends UnitTester {

	// TYPE:
	public static void main(String[] args) throws Exception {
		UnitTest ut = new UnitTest();
		ut.setUp();
		ut.testRepository();
		ut.exit();
		ut.println("done.");
	}

	// INSTANCE:
	private final List<Connection> _cList = new ArrayList<Connection>();
	private Repository _rep;
	private Long _orderId;
	private Long _customerId;

	// TODO remove this with JDK7
	@SuppressWarnings("unchecked")
	@BeforeClass
	public void setUp() throws BLException {
		println("starting...");
		// Settings:
		Globals.setApplicationLocale(Tools.getLocale("en", "GB"));
		// Database and connection:
		Database db = new Database(JdbcDriver.MYSQL, "localhost", 3306, "unit_tests", "root", "eagle");
		RepositoryConfig config = new RepositoryConfig();
		config.setCacheEnabled(true);
		config.setUsePreparedStatements(true);
		config.getLogger().setLevel(LEVEL.INFO);
		_rep = Repository.load(Seller.class);
		_rep.connect(db, config);
	}

	@AfterClass
	public void exit() throws BLException {
		println("exiting...");
		if(!_cList.isEmpty()) {
			fail("found "+_cList.size()+" connections open when attempting to shutdown database");
		}
		_rep.disconnect();
		println();
	}

	@Test
	public void testBasics() {
		// Test toString:
		assertEquals(Seller.getCustomers().getMetaData().customerId.toString(), "[Field name=customerId type=LONG owner=Customers]");
		// Test PrimaryKey:
		Map<PrimaryKey,Order> map = new HashMap<PrimaryKey,Order>();
		Order order1 = new Order(100L);
		order1.setStatus(Status.NEW);
		map.put(order1.getPrimaryKey(), order1);
		Order order2 = new Order(90L);
		assertFalse(order1.getPrimaryKey().equals(order2.getPrimaryKey()));
		Order order3 = new Order();
		try {
			order3.getPrimaryKey();
			fail();
		}
		catch(IllegalUsageException iue) {
			println(iue);
		}
		UT.setPrimaryKey(order3, Seller.getOrders().getMetaData().orderId, 100L);
		assertEquals(order1.getPrimaryKey(), order3.getPrimaryKey());
		// TODO change this to fullyEquals
		assertEquals(map.get(order3.getPrimaryKey()), order1);
		// Test Foreign Key:
		assertEquals(Seller.getOrders().getMetaData().orderId, ((FieldForeignKey)Seller.getLineItems().getMetaData().orderId).getForeignField());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testRepository() throws BLException {
		// Load EAP and RIT:
		Repository r1 = Repository.load(RIT.class, EAP.class, TempDomain.class);
		Collection<Class<? extends Domain>> domains = Repository.getDomains();
		// Note: Seller.class has already been loaded
		assertEquals(domains.size(), 4);
		assertTrue(domains.contains(RIT.class));
		assertTrue(domains.contains(EAP.class));
		assertTrue(domains.contains(TempDomain.class));
		try {
			Repository.load(EAP.class);
			fail();
		}
		catch(IllegalArgumentException iae) {
			println(iae);
		}
		println(r1.getEntities());
		Repository r2 = Repository.getRepositoryFor(RIT.class);
		assertEquals(r2, r1);
		r2 = Repository.getRepositoryFor(EAP.class);
		assertEquals(r2, r1);
		r2 = Repository.getRepositoryFor(TempDomain.class);
		assertEquals(r2, r1);
		// Check UnitTest (loading is already done on setUp):
		Repository r3 = Repository.getRepositoryFor(Seller.class);
		assertTrue(Repository.getDomains().contains(Seller.class));
		assertNotEquals(r2, r3);
		Collection<Entity<?,?>> entities = r3.getEntities();
		assertEquals(entities.size(), 8);
		assertTrue(entities.contains(Users.getInstance()));
		assertTrue(entities.contains(Customers.getInstance()));
		assertTrue(entities.contains(Orders.getInstance()));
		assertTrue(entities.contains(LineItems.getInstance()));
		assertTrue(entities.contains(Products.getInstance()));
		assertTrue(entities.contains(Interests.getInstance()));
		assertTrue(entities.contains(InterestedParties.getInstance()));
		assertTrue(entities.contains(Offices.getInstance()));
		// Ensure Entities can only be used with connection from correct repository:
		Database db = new Database(JdbcDriver.MYSQL, "localhost", 3306, "cubigraf_r3", "root", "eagle");
		r1.connect(db, new RepositoryConfig());
		// EAP and RIT connection:
		Connection c1 = r1.getConnection();
		// Seller connection:
		Connection c3 = r3.getConnection();
		EAP.getRoles().get(c1, 2L);
		try {
			EAP.getRoles().get(c3, 2L);
			fail();
		}
		catch(IllegalArgumentException iae) {
			println(iae);
		}
		c1.close();
		c3.close();
	}

	//@Test
	public void tFailDatabaseUnavailable() throws BLException {
		try {
			// Port number is wrong:
			Database details = new Database(JdbcDriver.MYSQL, "localhost", 3307, "unit_tests", "root", "somePassword");
			// TODO remove this with JDK7
			@SuppressWarnings("unchecked")
			Repository rep = Repository.load(Seller.class);
			rep.connect(details, new RepositoryConfig());
			fail();
		}
		catch(DatabaseUnavailableException due) {
			println(due);
		}
	}

	//@Test
	public void tFailWrongCredentials() throws BLException {
		try {
			Database details = new Database(JdbcDriver.MYSQL, "localhost", 3306, "unit_tests", "root", "somePassword");
			// TODO remove this with JDK7
			@SuppressWarnings("unchecked")
			Repository rep = Repository.load(Seller.class);
			rep.connect(details, new RepositoryConfig());
			fail();
		}
		catch(LoginFailedException lfe) {
			println(lfe);
		}
	}

	@Test
	public void tFailQueryPreconditions() throws BLException {
		Connection c = _rep.getConnection();
		// Wrong entity for query:
		try {
			Customers.getInstance().testWrongQuery(c);
			fail();
		}
		catch(IllegalArgumentException iae) {
			println(iae);
		}
		// Non-global query for Connection:
		try {
			c.select(Orders.getInstance().qSelectOrdersByStatus);
			fail();
		}
		catch(IllegalArgumentException iae) {
			println(iae);
		}
		// Wrong query type:
		try {
			Orders.getInstance().testWrongQueryType(c);
			fail();
		}
		catch(IllegalArgumentException iae) {
			println(iae);
		}
		c.close();
	}

	@Test
	public void testDatabaseDrivers() {
		_registerRightDriver(new TestResources.Driver_50_50());
		// Register the same driver:
		_registerWrongDriver(new TestResources.Driver_50_50());
		// Another valid driver:
		_registerRightDriver(new TestResources.Driver_51_55());
		// Register a series of overlapping drivers:
		_registerWrongDriver(new TestResources.Driver_42_51());
		_registerWrongDriver(new TestResources.Driver_50_60());
		_registerWrongDriver(new TestResources.Driver_55_60());
		_registerWrongDriver(new TestResources.Driver_39_70());
		// More valid drivers:
		_registerRightDriver(new TestResources.Driver_42_49());
		_registerRightDriver(new TestResources.Driver_56_60());
		// Driver with version number too long:
		_registerWrongDriver(new TestResources.Driver_422());
		// Check if registered database drivers are what we expect:
		assertEquals(DriverRegistry.getDriverFor(JdbcDriver.MYSQL, new Version(4, 1)).toString(), "MySQL 4.1");
		JdbcDriver newDriver = new JdbcDriver("SuperDatabase", "com.mysql.jdbc.Driver", "jdbc:mysql://<server>:<port>/<schema>?autoReconnect=true", 3306);
		assertEquals(DriverRegistry.getLatestDriverFor(newDriver).toString(), "SuperDatabase [5.6-6.0]");
	}

	private void _registerRightDriver(DatabaseDriver driver) {
		print("registering driver "+driver.getClass().getSimpleName()+"... ");
		DriverRegistry.registerDriver(driver);
		println("ok");
	}

	private void _registerWrongDriver(DatabaseDriver driver) {
		try {
			print("registering driver "+driver.getClass().getSimpleName()+"... ");
			DriverRegistry.registerDriver(driver);
			fail();
		}
		catch(IllegalArgumentException iae) {
			println(iae);
		}
	}

	@Test
	public void initializeData() throws BLException {
		Connection c = _rep.getConnection();
		// 1) Delete transactional data:
		Orders.getInstance().clear(c);
		Interests.getInstance().clear(c);
		// TODO check here if Orders, Interests and LineItems (including cache) are emptied

		// 2) Delete reference data:
		Customers customers = Customers.getInstance();
		Products products = Products.getInstance();
		customers.clear(c);
		products.clear(c);
		// TODO check if ref data has been deleted

		// 3) Create reference data:
		// Create customers:
		customers.create(c, new Customer().setName("Cubigraf").setAddress("Barcelos").setActive(true));
		customers.create(c, new Customer().setName("PT PRO").setAddress("Lisboa").setActive(false));
		customers.create(c, new Customer().setName("Via Verde").setAddress("Carcavelos").setActive(true));
		customers.create(c, new Customer().setName("EBookers").setAddress("Aldersgate, London, UK").setActive(false));
		customers.create(c, new Customer().setName("LOCOG").setAddress("Canary Wharf, London, UK").setActive(false));
		customers.create(c, new Customer().setName("Markit").setAddress("Southbank, London, UK").setActive(true));
		customers.create(c, new Customer().setName("MHRA").setAddress("Vauxhall, London, UK").setActive(true));
		// Create products:
		products.create(c, new Product("R01").setName("Rice (Carolino)").setPrice(0.90));
		products.create(c, new Product("R02").setName("Rice (Risotto)").setPrice(1.25));
		products.create(c, new Product("R03").setName("Rice (Basmati)").setPrice(0.76));
		products.create(c, new Product("P01").setName("Penne Pasta").setPrice(1.33));
		products.create(c, new Product("P02").setName("Spaghetti").setPrice(1.01));
		products.create(c, new Product("P03").setName("Tagliatelli").setPrice(0.98));
		products.create(c, new Product("P04").setName("Fusilli").setPrice(1.20));
		products.create(c, new Product("V01").setName("Tomatoes").setPrice(1.70));
		products.create(c, new Product("V02").setName("Rocket").setPrice(0.37));
		products.create(c, new Product("V03").setName("Spinach").setPrice(0.44));
		products.create(c, new Product("V04").setName("Mushrooms").setPrice(1.50));
		// Test insertOrUpdate for this last product:
		Product p1 = new Product("F01").setName("Sofa").setPrice(1200.0);
		products.save(c, p1);
		Product p2 = products.get(c, "F01");
		assertNotNull(p2);
		assertEquals(p2.getPrice(), 1200.0);
		p1.setPrice(2500.0);
		products.save(c, p1);
		p2 = products.get(c, "F01");
		assertEquals(p2.getPrice(), 2500.0);
		_customerId = customers.getFirstCustomerId(c);

		// 4) Create transactional data:
		Orders orders = Orders.getInstance();
		// Create orders:
		Customer c1 = customers.get(c, _customerId);
		assertTrue(c1.fullInformation());
		Customer c2 = customers.get(c, _customerId+1);
		assertTrue(c2.fullInformation());
		Order order = new Order();
		assertFalse(order.fullInformation());
		assertFalse(order.hasUpdates());
		order.setCustomer(c1);
		AssociationMap<LineItem> lineItems = order.getLineItems(c);
		lineItems.put(new Product("V02")).setQuantity(5);
		lineItems.put(new Product("R01")).setQuantity(1);
		lineItems.put(new Product("R03")).setQuantity(1);
		assertFalse(order.fullInformation());
		assertTrue(order.hasUpdates());
		orders.create(c, order, lineItems);
		assertTrue(order.fullInformation());
		_orderId = order.getOrderId();
		order = new Order().setCustomer(c1);
		lineItems = order.getLineItems(c);
		lineItems.put(new Product("P01")).setQuantity(2);
		orders.create(c, order, lineItems);
		println("test data created");
		c.close();
	}

	@Test(dependsOnMethods={"initializeData"})
	public void testInsertInterests() throws BLException, ValidationException {
		Connection c = _rep.getConnection();
		Interest interest = new Interest(100L, BenefitType.UNEMPLOYMENT, InterestType.CLERICAL);
		AssociationMap<InterestedParty> iParties = interest.getInterestedParties(c);
		Office newcastleO = Offices.getInstance().get(c, "newcastle", OfficeType.OWNING);
		iParties.put(newcastleO);
		Office newcastleB = Offices.getInstance().get(c, "newcastle", OfficeType.BROADCAST);
		iParties.put(newcastleB);
		Interests.getInstance().create(c, interest, iParties);
		// Check whether the interest has been created
		Interest check = Interests.getInstance().get(c, 100L, BenefitType.UNEMPLOYMENT, InterestType.CLERICAL);
		assertNotNull(check);
		c.close();
	}

	@Test(dependsOnMethods={"initializeData"})
	public void tFailAttemptIllegalUpdates() throws BLException, ValidationException {
		Connection c = _rep.getConnection();
		Customers customers = Customers.getInstance();
		// 1) Attempt to insert with primary key set
		Customer customer = new Customer(20L);
		try {
			customers.create(c, customer);
			fail();
		}
		catch(IllegalArgumentException iae) {
			println(iae);
		}
		// 2) Attempt to insert duplicate name
		try {
			customer = new Customer().setName("MHRA").setAddress("whatever");
			customers.create(c, customer);
			fail();
		}
		catch(BLException de) {
			println(de);
		}
		// 3) Attempt to change read only field:
		Order order1 = new Order(_orderId);
		order1.setDateMade(new Date());
		try {
			Orders.getInstance().update(c, order1, null);
			fail();
		}
		catch(ReadOnlyFieldException rofe) {
			println(rofe);
		}
		// 4) Wrong username:
		User user = new User("Jeitoso Tenebroso");
		user.setUsername("j.tenebroso-01");
		try {
			user.setUsername("j.tenebroso-01%");
			fail();
		}
		catch(WrongFormatException wfe) {
			println(wfe);
		}
		// 5) Non-existing foreign key:
		try {
			order1 = new Order(_orderId);
			order1.setCustomerId(Long.MAX_VALUE);
			Orders.getInstance().update(c, order1, null);
			fail();
		}
		catch(ForeignKeyConstraintException fkce) {
			// TODO how can we figure out which field is failing?
			println(fkce);
		}
		// 6) Attempt to delete a non-existing element:
		try {
			Orders.getInstance().delete(c, new Order(Long.MAX_VALUE));
			fail();
		}
		catch(InstanceDeletedException ide) {
			println(ide);
		}
		// 7) Invalid Boolean:
		try {
			customer = Customers.getInstance().get(c, _customerId);
			customer.setValue(customer.getMetaData().active, "F");
			customer.setValue(customer.getMetaData().active, "0");
			fail();
		}
		catch(WrongFormatException wfe) {
			println(wfe);
		}
		c.close();
	}

	@Test(dependsOnMethods={"initializeData"})
	public void testInsertsAndUpdates() throws BLException {
		Connection c = _rep.getConnection();
		_updateCustomers(c);
		_updateOrders(c);
		_updateLineItems(c);
		_updateConcurrently(c);
		c.close();
	}

	private void _updateCustomers(Connection c) throws BLException {
		// Test hasUpdates and time_created / time_updated:
		Customer customer = new Customer();
		customer.setName("TheCustomerWithoutAddress").setAddress(null).setActive(false);
		assertTrue(customer.hasUpdates());
		Timestamp now = new Timestamp();
		Customers.getInstance().create(c, customer);
		assertFalse(customer.hasUpdates());
		assertEquals(customer.getTimeCreated().getDate(), now.getDate());
		// TODO create Timestamp.compareTo(Timestamp t, int threshold) / equals(Timestamp t, int threshold)
		//assertEquals(customer.getTimeCreated().getTime().setMilliseconds(0), now.getTime().setMilliseconds(0));
		//assertEquals(customer.getTimeUpdated(), customer.getTimeCreated());
		customer.setName("Whatever");
		assertTrue(customer.hasUpdates());
		customer.setName("TheCustomerWithoutAddress");
		assertFalse(customer.hasUpdates());
		customer.setAddress(null);
		assertFalse(customer.hasUpdates());
		// Test hasUpdates with NULL:
		Long cId = customer.getCustomerId();
		customer = Customers.getInstance().get(c, cId);
		customer.setAddress(null);
		assertFalse(customer.hasUpdates());
		// Test again, but this time forcing the cache to be disabled. This checks for a
		// defect in hasUpdates found only when the EI was loaded directly from the database. 
		c.getRepository().getConfig().setCacheEnabled(false);
		c.getRepository().waitForCacheDisable();
		customer = Customers.getInstance().get(c, cId);
		customer.setAddress(null);
		assertFalse(customer.hasUpdates());
		c.getRepository().getConfig().setCacheEnabled(true);
		// Test time_updated:
		Tools.sleep(1);
		now = new Timestamp();
		customer.setAddress("2700 Travis Street");
		assertTrue(customer.hasUpdates());
		Customers.getInstance().update(c, customer);
		assertFalse(customer.hasUpdates());
		assertEquals(customer.getTimeUpdated().getDate(), now.getDate());
		assertEquals(customer.getTimeUpdated().getTime().setMilliseconds(0), now.getTime().setMilliseconds(0));
	}

	private void _updateOrders(Connection c) throws BLException, ValidationException {
		// Attempt to get dispatched orders:
		List<Order> dispatchedOrders = Orders.getInstance().listOrdersByStatus(c, Status.DISPATCHED);
		assertEquals(dispatchedOrders.size(), 0);
		// Dispatch them:
		Orders.getInstance().dispatchOrdersFor(c, _customerId);
		dispatchedOrders = Orders.getInstance().listOrdersByStatus(c, Status.DISPATCHED);
		assertEquals(dispatchedOrders.size(), 2);
		for(Order o : dispatchedOrders) {
			assertEquals(o.getStatus(), Status.DISPATCHED);
			assertEquals(o.getCustomer(c).getCustomerId(), _customerId);
		}
		// Change their status to paid:
		Orders.getInstance().payOrdersFor(c, _customerId);
		List<Order> paidOrders = Orders.getInstance().listOrdersByStatus(c, Status.PAID);
		assertEquals(paidOrders.size(), 2);
		for(Order o : paidOrders) {
			assertEquals(o.getStatus(), Status.PAID);
			assertEquals(o.getCustomer(c).getCustomerId(), _customerId);
		}
	}

	private void _updateLineItems(Connection c) throws BLException, ValidationException {
		// Add a new order
		Order order = new Order();
		order.setCustomer(Customers.getInstance().get(c, _customerId+3));
		order.setStatus(Status.RECEIVED);
		order.setDateMade(Date.parse("2003-04-04"));
		// Add some line items to the order, remove some, re-add
		Products products = Products.getInstance();
		AssociationMap<LineItem> lineItems = order.getLineItems(c);
		lineItems.put(products.get(c, "V01")).setQuantity(10);
		lineItems.put(products.get(c, "V02"));
		lineItems.put(products.get(c, "V03")).setQuantity(1);
		lineItems.put(products.get(c, "V04")).setQuantity(1);
		lineItems.remove("V01");
		lineItems.remove("V02");
		lineItems.put(products.get(c, "V01"));
		assertEquals(lineItems.get("V01").getQuantity(), new Integer(10));
		// Send to database
		Orders.getInstance().create(c, order, lineItems);
		println(order);
		// Add more, remove an existing one (should generate DELETE query), update another
		lineItems.remove("V04");
		lineItems.put(Products.getInstance().get(c, "P02")).setQuantity(31);
		assertEquals(lineItems.get("P02").getQuantity(), new Integer(31));
		assertEquals(lineItems.get("V01").getQuantity(), new Integer(10));
		lineItems.get("V03").setQuantity(20);
		assertEquals(lineItems.size(), 3);
		assertEquals(UT.getCurrent(lineItems).size(), 2);
		assertEquals(UT.getAdded(lineItems).size(), 1);
		assertEquals(UT.getRemoved(lineItems).size(), 1);
		Orders.getInstance().update(c, order, lineItems);
		assertEquals(lineItems.size(), 3);
		assertEquals(UT.getCurrent(lineItems).size(), 3);
		assertTrue(UT.getAdded(lineItems).isEmpty());
		assertTrue(UT.getRemoved(lineItems).isEmpty());
		// After this, our order should have line items V01/10, V03/20, P02/1.
		// Lets re-fetch the order from cache:
		order = Orders.getInstance().get(c, order.getOrderId());
		lineItems = order.getLineItems(c);
		assertEquals(lineItems.size(), 3);
		LineItem li = null;
		li = lineItems.get("V01");
		assertEquals(li.getQuantity(), 10);
		li = lineItems.get("V03");
		assertEquals(li.getQuantity(), 20);
		li = lineItems.get("P02");
		assertEquals(li.getQuantity(), 31);
		// Lets re-fetch the order, but this time without using the cache:
		c.getRepository().getConfig().setCacheEnabled(false);
		c.getRepository().waitForCacheDisable();
		order = Orders.getInstance().get(c, order.getOrderId());
		lineItems = order.getLineItems(c);
		assertEquals(lineItems.size(), 3);
		li = null;
		li = lineItems.get("V01");
		assertEquals(li.getQuantity(), 10);
		li = lineItems.get("V03");
		assertEquals(li.getQuantity(), 20);
		li = lineItems.get("P02");
		assertEquals(li.getQuantity(), 31);
		c.getRepository().getConfig().setCacheEnabled(true);
		// Create a new one:
		Order order2 = new Order();
		order2.setCustomerId(Customers.getInstance().getFirstCustomerId(c));
		lineItems = order2.getLineItems(c);
		lineItems.put(new Product("R01")).setQuantity(1);
		lineItems.put(new Product("R02")).setQuantity(1);
		lineItems.put(new Product("R03")).setQuantity(1);
		Orders.getInstance().create(c, order2, lineItems);
		Long newOrderId = order2.getOrderId();
		// Check AssociationMap.hasUpdates():
		assertFalse(lineItems.hasUpdates());
		lineItems.clear();
		assertTrue(lineItems.hasUpdates());
		lineItems.put(new Product("R01"));
		lineItems.put(new Product("R02"));
		lineItems.put(new Product("R03"));
		assertFalse(lineItems.hasUpdates());
		lineItems.put(new Product("V01"));
		assertTrue(lineItems.hasUpdates());
		lineItems.remove("V01");
		assertFalse(lineItems.hasUpdates());
		// And now clear all the line items:
		lineItems.clear();
		Orders.getInstance().update(c, order2, lineItems);
		order2 = Orders.getInstance().get(c, newOrderId);
		assertEquals(order2.getLineItems(c).size(), 0);
	}
	
	// TODO change this method. The second update should succeed and update the db/cache accordingly.
	// If a LineItem is removed in one of the updates, it should succeed.
	// If inserts are done in either update, they should succeed.
	// If a LineItem is removed on the first update and we try to update it on the second update, we throw an
	// InstanceDeletedException
	private void _updateConcurrently(Connection c) throws BLException, ValidationException {
		Orders orders = Orders.getInstance();
		Products products = Products.getInstance();
		Customers customers = Customers.getInstance();
		// Insert a new order:
		Order order = new Order();
		order.setCustomer(customers.get(c, _customerId+5));
		AssociationMap<LineItem> lineItems = order.getLineItems(c);
		lineItems.put(products.get(c, "V01")).setQuantity(10);
		orders.create(c, order, lineItems);
		// Get it from cache, as a new variable (will be a clone):
		Order orderClone = orders.get(c, order.getOrderId());
		// Update both objects in memory:
		AssociationMap<LineItem> cloneLineItems = orderClone.getLineItems(c);
		cloneLineItems.put(products.get(c, "R02")).setQuantity(2);
		lineItems.get("V01").setQuantity(5);
		// Commit the updates:
		orders.update(c, orderClone, cloneLineItems);
		orders.update(c, order, lineItems);
		// Retrieve the line items again and ensure that both line items were added:
		lineItems = order.getLineItems(c);
		assertEquals(lineItems.size(), 2);
		assertEquals(lineItems.get("V01").getQuantity(), new Integer(5));
		assertEquals(lineItems.get("R02").getQuantity(), new Integer(2));
		// Update again, to ensure there are no probs:
		cloneLineItems.put(products.get(c, "R02")).setQuantity(3);
		orders.update(c, orderClone, cloneLineItems);
		lineItems = order.getLineItems(c);
		assertEquals(lineItems.get("R02").getQuantity(), new Integer(3));
	}

	// TODO allow methods within this method to run in parallel
	@Test(dependsOnMethods={"testInsertsAndUpdates"})
	public void testQueries() throws BLException {
		Connection c = _rep.getConnection();
		// Run all queries with the cache enabled:
		_qSelectOrders(c);
		_qSelectLineItems(c);
		_qSelectActiveCustomers(c);
		_qSelectAllUsers(c);
		_qSelectUsersWithLimit(c);
		_qSelectUsersWithParametrizedLimit(c);
		_qSelectUsersLike(c);
		_qSelectIgnoreCase(c);
		_qGlobalQueries(c);
		// Run all queries with the cache disabled:
		_qDisableCache(c);
		_qSelectOrders(c);
		_qSelectLineItems(c);
		_qSelectActiveCustomers(c);
		_qSelectAllUsers(c);
		_qSelectUsersWithLimit(c);
		_qSelectUsersWithParametrizedLimit(c);
		_qSelectUsersLike(c);
		_qSelectIgnoreCase(c);
		_qGlobalQueries(c);
		// Re-enable cache:
		_qEnableCache(c);
		c.close();
	}

	private void _qDisableCache(Connection c) throws BLException {
		Repository rep = c.getRepository();
		try {
			rep.getConfig().setCacheEnabled(true);
			fail();
		}
		catch(Exception e) {
			println(e);
		}
		assertTrue(rep.cacheDisableFinished());
		rep.getConfig().setCacheEnabled(false);
		try {
			assertFalse(rep.cacheDisableFinished());
			// This generates an exception:
			rep.getConfig().setCacheEnabled(false);
			fail();
		}
		catch(Exception e) {
			println(e);
		}
		// We now want to wait for the disable task to finish:
		Tools.sleep(rep.getConfig().getDisableCacheDaemonDelay().milliseconds()+100, null);
	}

	private void _qEnableCache(Connection c) throws BLException {
		c.getRepository().getConfig().setCacheEnabled(true);
	}

	// TODO falta um teste para verificar se holder entity consegue ir buscar
	// associations qdo a cache esta activada
	private void _qSelectOrders(Connection c) throws BLException, ValidationException {
		// Select a couple of individual orders:
		Order order1 = new Order(_orderId);
		println(order1);
		assertFalse(order1.fullInformation());
		AssociationMap<LineItem> lineItems = order1.getLineItems(c);
		for(LineItem li : lineItems) {
			println(li);
		}
		// Retrieve order from cache (if enabled) and check it is the same one: 
		Order order2 = Orders.getInstance().get(c, _orderId);
		assertTrue(order2.fullInformation());
		assertTrue(order1.equals(order2));
		println(order2);
	}

	private void _qSelectLineItems(Connection c) throws BLException {
		// Note: all of these queries hit the database even when the cache is enabled.
		List<LineItem> results = LineItems.getInstance().listLargerQuantities(c);
		assertEquals(results.size(), 2);
		assertEquals(results.get(0).getQuantity(), new Integer(20));
		assertEquals(results.get(1).getQuantity(), new Integer(31));
		results = LineItems.getInstance().listLineItemsBefore(c, new Timestamp().getDate());
		assertEquals(results.size(), 3);
		LineItem li = null;
		li = results.get(0);
		assertEquals(li.getProductId(), "P02");
		assertEquals(li.getQuantity(), new Integer(31));
		li = results.get(1);
		assertEquals(li.getProductId(), "V01");
		assertEquals(li.getQuantity(), new Integer(10));
		li = results.get(2);
		assertEquals(li.getProductId(), "V03");
		assertEquals(li.getQuantity(), new Integer(20));
		// This one is done on Orders and also uses a memory JOIN.
		// In order for the JOIN to happen on the cache, Orders must
		// be configured to hold its entire contents on the cache.
		// Select orders from inactive customers (this will perform a JOIN in memory):
		List<Order> orders = Orders.getInstance().listOrdersFromInactiveCustomers(c);
		assertEquals(orders.size(), 1);
		assertFalse(orders.get(0).getCustomer(c).getActive());
		assertEquals(orders.get(0).getDateMade(), new Date(2003,4,4));
	}

	private void _qSelectActiveCustomers(Connection c) throws BLException {
		List<Customer> activeCustomers = Customers.getInstance().listActiveCustomers(c);
		assertEquals(activeCustomers.size(), 4);
		assertEquals(activeCustomers.get(0).getName(), "Cubigraf");
		assertEquals(activeCustomers.get(1).getName(), "Markit");
		assertEquals(activeCustomers.get(2).getName(), "MHRA");
		assertEquals(activeCustomers.get(3).getName(), "Via Verde");
	}
	
	private void _qSelectAllUsers(Connection c) throws BLException {
		List<User> users = Users.getInstance().listUsersByAge(c);
		assertEquals(users.size(), 9);
		assertEquals(users.get(0).getName(), "Carlos Silva");
		assertEquals(users.get(1).getName(), "Isabel Figueiredo");
		assertEquals(users.get(2).getName(), "João Madeira");
		assertEquals(users.get(3).getName(), "Darren King");
		assertEquals(users.get(4).getName(), "Johanna Wilson");
		assertEquals(users.get(5).getName(), "Luís Soares");
		assertEquals(users.get(6).getName(), "Rebekah Staunton");
		assertEquals(users.get(7).getName(), "Zhu Malik");
		assertEquals(users.get(8).getName(), "Farrokh Bulsara");
	}

	private void _qSelectUsersWithLimit(Connection c) throws BLException {
		List<User> users = Users.getInstance().listUsersByAgeWithLimit(c);
		assertEquals(users.size(), 5);
		assertEquals(users.get(0).getName(), "João Madeira");
		assertEquals(users.get(1).getName(), "Darren King");
		assertEquals(users.get(2).getName(), "Johanna Wilson");
		assertEquals(users.get(3).getName(), "Luís Soares");
		assertEquals(users.get(4).getName(), "Rebekah Staunton");
	}

	private void _qSelectUsersWithParametrizedLimit(Connection c) throws BLException {
		List<User> users = Users.getInstance().listUsersByAgeOnRange(c, 7, 10);
		assertEquals(users.size(), 2);
		assertEquals(users.get(0).getName(), "Zhu Malik");
		assertEquals(users.get(1).getName(), "Farrokh Bulsara");
		// TODO add test case for when limit is offset=0
	}

	private void _qSelectUsersLike(Connection c) throws BLException {
		String[] expectedNames = {"Farrokh Bulsara", "Isabel Figueiredo"};
		// *sa*:
		List<User> users = Users.getInstance().findUsersByName(c, "%sa%");
		// TODO is there a method in TestNG that does this check for us?
		for(int i=0; i<users.size(); i++) {
			assertEquals(users.get(i).getName(), expectedNames[i]);
		}
		// *so*:
		expectedNames = new String[] {"Johanna Wilson", "Luís Soares"};
		users = Users.getInstance().findUsersByName(c, "%so%");
		for(int i=0; i<users.size(); i++) {
			assertEquals(users.get(i).getName(), expectedNames[i]);
		}
		// *ra:
		expectedNames = new String[] {"Farrokh Bulsara", "João Madeira"};
		users = Users.getInstance().findUsersByName(c, "%ra");
		for(int i=0; i<users.size(); i++) {
			assertEquals(users.get(i).getName(), expectedNames[i]);
		}
		// isa*:
		expectedNames = new String[] {"Isabel Figueiredo"};
		users = Users.getInstance().findUsersByName(c, "isa%");
		for(int i=0; i<users.size(); i++) {
			assertEquals(users.get(i).getName(), expectedNames[i]);
		}
	}

	private void _qSelectIgnoreCase(Connection c) throws BLException {
		// Retrieve products by primary key, which is not case sensitive.
		// This will force a get on the cache based on primary key.
		Products products = Products.getInstance();
		assertNotNull(products.get(c, "P01"));
		assertNotNull(products.get(c, "p01"));
		// Retrieve users by username, which is not case sensitive.
		// This will force a search on cache.
		Users users = Users.getInstance();
		User freddieMercury = users.getUserByUsername(c, "fbulsara");
		assertNotNull(freddieMercury);
		freddieMercury = users.getUserByUsername(c, "FBulsara");
		assertNotNull(freddieMercury);
	}

	private void _qGlobalQueries(Connection c) throws BLException, ValueTruncatedException {
		String[] expectedProducts = new String[] {"P02", "V03", "V01", "V02", "R02", "P01", "R01", "R03"};
		int[] expectedQuantities = new int[] {31, 20, 15, 5, 3, 2, 1, 1};
		// GROUP query:
		// TODO we need to add sorting by product id (after quantity)
		Results totalQuantities = LineItems.getInstance().selectTotalQuantitiesOrdered(c);
		assertEquals(totalQuantities.getColumns().get(0), "product_id");
		assertEquals(totalQuantities.getColumns().get(1), "total");
		Iterator<Row> it = totalQuantities.getRows().iterator();
		for(int i=0; it.hasNext(); i++) {
			Row row = it.next();
			assertEquals(row.getString(1), expectedProducts[i]);
			assertEquals(row.getInt(2), expectedQuantities[i]);
		}
	}
	
	@Test(dependsOnMethods={"testQueries","tFailAttemptIllegalUpdates"})
	public void testDeleteCustomer() throws BLException {
		Connection c = _rep.getConnection();
		Customer customer = Customers.getInstance().get(c, _customerId);
		List<Order> list = Orders.getInstance().listOrdersForCustomer(c, customer.getCustomerId());
		assertFalse(list.isEmpty());
		println(list);
		// Delete this customer:
		Customers.getInstance().delete(c, customer);
		// Ensure all orders have been removed from cache and database:
		for(Order o : list) {
			Order tmp = Orders.getInstance().get(c, o.getOrderId());
			assertNull(tmp);
		}
		c.close();
		// TODO delete tests:
		// 1) Change configuration of LineItems to cache.FULL
		// 2) Clear orders
		// 3) Ensure that both the Orders and LineItems cache have been emptied
	}

	// TODO make test rather than printing
	@Test(dependsOnMethods={"testDeleteCustomer"})
	public void testCheckStatistics() throws BLException {
		Connection c = _rep.getConnection();
		println("* global stats:");
		int dbHits = c.getRepository().getDatabaseHitCount();
		int cacheHits = c.getRepository().getCacheHitCount();
		println("database hits: "+dbHits);
		println("cache hits: "+cacheHits);
		// Check that database hits increase (hitting a non-existing order):
		for(int i=0; i<10; i++) {
			Orders.getInstance().get(c, 1L);
		}
		dbHits += 10;
		assertEquals(c.getRepository().getDatabaseHitCount(), dbHits);
		// Check that cache hits increase:
		for(int i=0; i<10; i++) {
			Orders.getInstance().get(c, 5L);
		}
		cacheHits += 10;
		assertEquals(c.getRepository().getDatabaseHitCount(), dbHits);
		assertEquals(c.getRepository().getCacheHitCount(), cacheHits);
		println("* query stats:");
		Map<String,Query> stats = _rep.getNamedQueries();
		for(Query q : stats.values()) {
			println(q.getFullName()+": "+q.getCacheHitCount()+" cache hits of a total of "+q.getTotalHitCount());
		}
		c.close();
	}
}
