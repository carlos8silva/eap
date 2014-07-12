package com.netx.bl.R1.sql;
import com.netx.generics.R1.util.UnitTester;
import com.netx.generics.R1.translation.ParseException;
import com.netx.bl.R1.core.*;
import com.netx.ut.bl.R1.core.Seller;
import com.netx.eap.R1.bl.EAP;
import com.netx.rit.R1.bl.RIT;
import org.testng.annotations.Test;


public class UnitTest extends UnitTester {

	// TYPE:
	public static void main(String[] args) {
		UnitTest ut = new UnitTest();
		ut.testEntityQueries();
		ut.testGlobalQueries();
		ut.testUpdates();
		ut.println("done.");
	}

	// INSTANCE:
	private final Repository _rep;

	// TODO remove this with JDK7
	@SuppressWarnings("unchecked")
	public UnitTest() {
		_rep = Repository.load(RIT.class, Seller.class);
	}

	public void testEntityQueries() {
		Select select = null;
		// Base SELECT queries:
		_testWrongSelect("UPDATE users", Seller.getUsers().getMetaData());
		_testWrongSelect("SELECT & FROM users", Seller.getUsers().getMetaData());
		_testWrongSelect("SELECT * FROM", Seller.getUsers().getMetaData());
		_testWrongSelect("SELECT *", Seller.getUsers().getMetaData());
		_testWrongSelect("SELECT username", Seller.getUsers().getMetaData());
		_testWrongSelect("FROM users", Seller.getUsers().getMetaData());
		_testWrongSelect("SELECT * FROM <> users", Seller.getUsers().getMetaData());
		_testWrongSelect("SELECT * FROM users 9", Seller.getUsers().getMetaData());
		select = _parseSelect("SELECT * FROM users", Seller.getUsers().getMetaData(), 0);
		assertNull(select.getWhereClause());
		_parseSelect("SELECT * FROM users u", Seller.getUsers().getMetaData(), 0);
		_parseSelect("SELECT u.* FROM users u", Seller.getUsers().getMetaData(), 0);
		_testWrongSelect("SELECT * FROM users u vv", Seller.getUsers().getMetaData());
		// TODO for next 6 queries: change UT to validate that parsing succeeds, but analyzing fails
		_testWrongSelect("SELECT orders.* FROM users u", Seller.getUsers().getMetaData());
		_testWrongSelect("SELECT u.*, orders.* FROM users u", Seller.getUsers().getMetaData());
		_testWrongSelect("SELECT u.name FROM users u", Seller.getUsers().getMetaData());
		_testWrongSelect("SELECT customer_id FROM customers ORDER BY customer_id ASC LIMIT 0, 1", Seller.getCustomers().getMetaData());
		_testWrongSelect("SELECT username, password FROM users", Seller.getUsers().getMetaData());
		_testWrongSelect("SELECT u.username, u.password FROM users u", Seller.getUsers().getMetaData());
		_testWrongSelect("SELECT orders.* FROM users", Seller.getUsers().getMetaData());
		_testWrongSelect("SELECT orders.* FROM orders", Seller.getCustomers().getMetaData());
		select = _parseSelect("SELECT * FROM users u WHERE u.username = ?", Seller.getUsers().getMetaData(), 1, true);
		assertEquals(select.getParsedSQL(), "SELECT name, city, age, username, time_created, time_updated FROM users WHERE username = ?");
		_parseSelect("SELECT * FROM users u WHERE age = 11283", Seller.getUsers().getMetaData(), 0);
		_parseSelect("SELECT u.* FROM users u WHERE age = 11283", Seller.getUsers().getMetaData(), 0);
		_testWrongSelect("SELECT *.* FROM users u WHERE user_id = 11283", Seller.getUsers().getMetaData());
		_parseSelect("SELECT users.* FROM users WHERE users.age = 11283", Seller.getUsers().getMetaData(), 0);
		// TODO for next 1 queries: change UT to validate that parsing succeeds, but analyzing fails
		_testWrongSelect("SELECT username, users.* FROM users WHERE users.user_id = 11283", Seller.getUsers().getMetaData());
		_testWrongSelect("SELECT username, * FROM users WHERE users.user_id = 11283", Seller.getUsers().getMetaData());
		// Multiple WHERE clauses:
		_parseSelect("SELECT * FROM users u WHERE u.username=? AND u.city=NULL OR u.age=50", Seller.getUsers().getMetaData(), 1);
		_testWrongSelect("SELECT * FROM users WHERE username=? ANDD city IS NOT NULL OR age=50", Seller.getUsers().getMetaData());
		_testWrongSelect("SELECT * FROM users WHERE username=? AND city IS NOT NULL IS age=50", Seller.getUsers().getMetaData());
		_parseSelect("SELECT * FROM users u WHERE u.username=? AND u.city=NULL OR u.age=50", Seller.getUsers().getMetaData(), 1);
		_parseSelect("SELECT * FROM users u WHERE u.username=? AND u.city IS NULL OR u.age=50", Seller.getUsers().getMetaData(), 1);
		_parseSelect("SELECT * FROM users u WHERE u.username=? AND u.city IS NOT NULL OR u.age=50", Seller.getUsers().getMetaData(), 1);
		_parseSelect("SELECT * FROM users WHERE username=? AND city IS NOT NULL OR age=50", Seller.getUsers().getMetaData(), 1);
		_parseSelect("SELECT * FROM users WHERE username=? AND age=19", Seller.getUsers().getMetaData(), 1);
		// Parentheses:
		_parseSelect("SELECT * FROM users WHERE (username=? AND age=25)", Seller.getUsers().getMetaData(), 1);
		_parseSelect("SELECT * FROM users WHERE username=? AND (city IS NOT NULL OR age=25)", Seller.getUsers().getMetaData(), 1);
		_parseSelect("SELECT * FROM users WHERE (username=? AND city IS NOT NULL) OR age=25", Seller.getUsers().getMetaData(), 1);
		// Semantic errors:
		_testWrongSelect("SELECT * FROM orderz o", Seller.getUsers().getMetaData());
		_testWrongSelect("SELECT * FROM orders, users", Seller.getOrders().getMetaData());
		_testWrongSelect("SELECT * FROM orders o, users u", Seller.getOrders().getMetaData());
		_testWrongSelect("SELECT * FROM orders", Seller.getUsers().getMetaData());
		_testWrongSelect("SELECT orderz.* FROM users", Seller.getUsers().getMetaData());
		_testWrongSelect("SELECT users.* FROM users u", Seller.getUsers().getMetaData());
		// TODO for next (all wrong) queries: change UT to validate that parsing succeeds, but analyzing fails
		_testWrongSelect("SELECT orders.* FROM orders o JOIN customers c ON orders.customer_id=customers.customer_id WHERE customers.active='F'", Seller.getOrders().getMetaData());
		_testWrongSelect("SELECT o.* FROM orders o JOIN customerz c, orders o ON o.customer_id=c.customer_id WHERE c.active='F'", Seller.getOrders().getMetaData());
		_testWrongSelect("SELECT o.* FROM orders o JOIN customerz c ON o.customer_id=c.customer_id WHERE c.active='F'", Seller.getOrders().getMetaData());
		_testWrongSelect("SELECT o.* FROM orders o JOIN customers c ON o.customer_id=customers.customer_id WHERE c.active='F'", Seller.getOrders().getMetaData());
		_testWrongSelect("SELECT o.* FROM orders o JOIN line_items li ON order_id = 5", Seller.getOrders().getMetaData());
		_parseSelect("SELECT orders.* FROM orders JOIN customers ON orders.customer_id=customers.customer_id AND customers.active='F'", Seller.getOrders().getMetaData(), 0);
		_parseSelect("SELECT orders.* FROM orders JOIN customers ON orders.customer_id=customers.customer_id AND active='F'", Seller.getOrders().getMetaData(), 0);
		// Data type errors:
		_testWrongSelect("SELECT o.* FROM orders o JOIN customers c ON o.customer_id=c.active WHERE active='F'", Seller.getOrders().getMetaData());
		_testWrongSelect("SELECT o.* FROM orders o JOIN customers c ON o.customer_id=c.customer_id WHERE active=100", Seller.getOrders().getMetaData());
		_testWrongSelect("SELECT o.* FROM orders o JOIN customers c ON o.customer_id=c.customer_id WHERE active='FALSE'", Seller.getOrders().getMetaData());
		_parseSelect("SELECT o.* FROM orders o JOIN customers c ON o.customer_id=c.customer_id WHERE active='T'", Seller.getOrders().getMetaData(), 0);
		_testWrongSelect("SELECT o.* FROM orders o JOIN customers c ON o.customer_id=c.customer_id WHERE active='A'", Seller.getOrders().getMetaData());
		_testWrongSelect("SELECT * FROM orders WHERE status = 'DA'", Seller.getOrders().getMetaData());
		_testWrongSelect("SELECT u.* FROM users u WHERE name = 19799", Seller.getUsers().getMetaData());
		_parseSelect("SELECT u.* FROM users u WHERE name = '19799'", Seller.getUsers().getMetaData(), 0, true);
		_testWrongSelect("SELECT * FROM orders WHERE order_id = 'D'", Seller.getOrders().getMetaData());
		_testWrongSelect("SELECT * FROM orders WHERE order_id = 9.3", Seller.getOrders().getMetaData());
		_testWrongSelect("SELECT * FROM orders WHERE order_id = 979872983797123984723", Seller.getOrders().getMetaData());
		_testWrongSelect("SELECT u.* FROM users u WHERE age = 'F'", Seller.getUsers().getMetaData());
		_parseSelect("SELECT * FROM products WHERE price = 2134.723", Seller.getProducts().getMetaData(), 0);
		_parseSelect("SELECT * FROM products WHERE price = 2134", Seller.getProducts().getMetaData(), 0);
		_testWrongSelect("SELECT u.* FROM users u WHERE time_created >= '2001-03-25'", Seller.getUsers().getMetaData());
		_parseSelect("SELECT u.* FROM users u WHERE time_created >= '2001-03-25 00:00:00'", Seller.getUsers().getMetaData(), 0);
		_parseSelect("SELECT u.* FROM users u WHERE time_created = '[now()]'", Seller.getUsers().getMetaData(), 0);
		// LiKE operator:
		_parseSelect("SELECT users.* FROM users WHERE username LIKE 'c%'", Seller.getUsers().getMetaData(), 0);
		_parseSelect("SELECT users.* FROM users WHERE username LIKE ?", Seller.getUsers().getMetaData(), 1);
		// Order by:
		select = _parseSelect("SELECT u.* FROM users u WHERE name LIKE ? ORDER BY username", Seller.getUsers().getMetaData(), 1);
		assertNotNull(select.getOrderByClause());
		assertEquals(select.getOrderByClause().length, 1);
		select = _parseSelect("SELECT u.* FROM users u WHERE name LIKE ? ORDER BY username, age", Seller.getUsers().getMetaData(), 1);
		assertNotNull(select.getOrderByClause());
		assertEquals(select.getOrderByClause().length, 2);
		_testWrongSelect("SELECT u.* FROM users u WHERE name LIKE ? ORDER BY username,", Seller.getUsers().getMetaData());
		_testWrongSelect("SELECT u.* FROM users u WHERE name LIKE ? ORDER BY 9", Seller.getUsers().getMetaData());
		select = _parseSelect("SELECT u.* FROM users u WHERE name LIKE ? ORDER BY username ASC, age DESC", Seller.getUsers().getMetaData(), 1);
		OrderBy[] orderBy = select.getOrderByClause();
		assertEquals(orderBy.length, 2);
		assertEquals(orderBy[0].column, -1);
		assertEquals(orderBy[0].field, Seller.getUsers().getMetaData().username);
		assertEquals(orderBy[0].order, OrderBy.ORDER.ASC);
		assertEquals(orderBy[1].column, -1);
		assertEquals(orderBy[1].field, Seller.getUsers().getMetaData().age);
		assertEquals(orderBy[1].order, OrderBy.ORDER.DESC);
		_parseSelect("SELECT u.* FROM users u WHERE name LIKE ? ORDER BY username ASC, age", Seller.getUsers().getMetaData(), 1);
		_testWrongSelect("SELECT u.* FROM users u WHERE name LIKE ? ORDER BY username ASC age", Seller.getUsers().getMetaData());
		_parseSelect("SELECT u.* FROM users u WHERE name LIKE ? ORDER BY u.username ASC, u.age DESC", Seller.getUsers().getMetaData(), 1);
		_parseSelect("SELECT * FROM users WHERE users.name LIKE ? ORDER BY users.username ASC, users.age DESC", Seller.getUsers().getMetaData(), 1);
		_parseSelect("SELECT li.* FROM line_items li JOIN orders o ON li.order_id=o.order_id WHERE status=? ORDER BY status", Seller.getLineItems().getMetaData(), 1);
		// Limit:
		_parseSelect("SELECT u.* FROM users u LIMIT 1, 10", Seller.getUsers().getMetaData(), 0);
		_parseSelect("SELECT u.* FROM users u LIMIT 10", Seller.getUsers().getMetaData(), 0);
		_parseSelect("SELECT u.* FROM users u WHERE name LIKE ? LIMIT 100, 200", Seller.getUsers().getMetaData(), 1);
		_parseSelect("SELECT u.* FROM users u WHERE name LIKE ? LIMIT ?, ?", Seller.getUsers().getMetaData(), 3);
		_parseSelect("SELECT u.* FROM users u WHERE name LIKE ? ORDER BY username ASC, age LIMIT 0, 1", Seller.getUsers().getMetaData(), 1);
		_testWrongSelect("SELECT u.* FROM users u WHERE name LIKE ? LIMIT 100 ORDER BY username ASC, age", Seller.getUsers().getMetaData());
		// Joins:
		_parseSelect("SELECT orders.* FROM orders JOIN customers ON orders.customer_id=customers.customer_id", Seller.getOrders().getMetaData(), 0);
		_parseSelect("SELECT orders.* FROM orders JOIN customers ON orders.customer_id=customers.customer_id WHERE customers.active='F'", Seller.getOrders().getMetaData(), 0);
		_parseSelect("SELECT orders.* FROM orders JOIN customers ON orders.customer_id=customers.customer_id AND orders.customer_id=customers.customer_id WHERE customers.active='F'", Seller.getOrders().getMetaData(), 0);
		_parseSelect("SELECT o.* FROM orders o JOIN customers c ON o.customer_id=c.customer_id WHERE c.active='F'", Seller.getOrders().getMetaData(), 0);
		_parseSelect("SELECT o.* FROM orders o JOIN customers c ON o.customer_id=c.customer_id WHERE active='F'", Seller.getOrders().getMetaData(), 0);
		_parseSelect("SELECT o.* FROM orders o JOIN customers c ON o.customer_id=c.customer_id WHERE order_id=? AND o.customer_id=c.customer_id", Seller.getOrders().getMetaData(), 1, true);
		_parseSelect("SELECT li.* FROM line_items li JOIN orders o ON li.order_id=o.order_id WHERE status='D'", Seller.getLineItems().getMetaData(), 0);
		_testWrongSelect("SELECT li.* FROM line_items li JOIN orders o ON li.order_id <> o.order_id", Seller.getLineItems().getMetaData());
		// Ensure JOIN is done using foreign keys:
		_testWrongSelect("SELECT o.* FROM orders o JOIN line_items li ON li.order_id = 5", Seller.getOrders().getMetaData());
		_testWrongSelect("SELECT o.* FROM orders o JOIN customers c ON c.customer_id IS NULL WHERE active='T'", Seller.getOrders().getMetaData());
		_testWrongSelect("SELECT o.* FROM orders o JOIN customers c ON o.order_id=c.customer_id WHERE c.active='F'", Seller.getOrders().getMetaData());
		_testWrongSelect("SELECT c.* FROM customers c JOIN orders o ON o.customer_id=c.customer_id AND c.active='F'", Seller.getCustomers().getMetaData());
		_testWrongSelect("SELECT o.* FROM orders o JOIN line_items li ON li.order_id=o.order_id AND li.quantity>5", Seller.getOrders().getMetaData());
		_parseSelect("SELECT li.* FROM line_items li JOIN orders o ON li.order_id=o.order_id AND li.quantity>5 AND o.status='N'", Seller.getLineItems().getMetaData(), 0);
		// JOIN in one-to-many relationships:
		_testWrongSelect("SELECT li.* FROM line_items li JOIN customers c ON li.order_id=c.customer_id", Seller.getLineItems().getMetaData());
		_testWrongSelect("SELECT * FROM eap_users JOIN eap_user_roles ON eap_users.user_id=eap_user_roles.user_id WHERE eap_user_roles.role_id = ?", EAP.getUsers().getMetaData());
		_testWrongSelect("SELECT * FROM customers c JOIN orders o ON o.customer_id=c.customer_id WHERE o.order_id = ?", Seller.getCustomers().getMetaData());
		// Unique results:
		_parseSelect("SELECT * FROM users u WHERE u.username=? AND u.city=? AND u.age=50", Seller.getUsers().getMetaData(), 2);
		_parseSelect("SELECT o.* FROM orders o JOIN customers c ON o.customer_id=c.customer_id WHERE o.order_id=?", Seller.getOrders().getMetaData(), 1, true);
		_parseSelect("SELECT * FROM users u WHERE u.username=? AND u.name=?", Seller.getUsers().getMetaData(), 2, true);
		_parseSelect("SELECT * FROM eap_users WHERE username = ?", EAP.getUsers().getMetaData(), 1, true);
		_parseSelect("SELECT * FROM eap_roles WHERE name = ?", EAP.getRoles().getMetaData(), 1, true);
		_parseSelect("SELECT * FROM rit_user_projects WHERE user_id = ? AND project_id = ?", RIT.getUserProjects().getMetaData(), 2, true);
	}

	@Test
	public void testGlobalQueries() {
		Select select = null;
		String sql = "SELECT u.user_id FROM eap_users u, eap_roles r, eap_user_roles ur WHERE u.user_id=ur.user_id AND r.role_id=ur.role_id AND r.role_id = ?";
		select = _parseGlobal(sql, 1);
		assertNull(select.getName());
		assertNull(select.getFullName());
		assertNull(select.getWhereClause());
		assertNull(select.getOrderByClause());
		assertNull(select.getLimitClause());
		assertEquals(select.getParsedSQL(), sql);
		select = _parseGlobal("SELECT u.user_id FROM eap_users u, eap_roles r, eap_user_roles ur WHERE u.user_id=ur.user_id AND r.role_id=ur.role_id AND r.role_id = ? ORDER BY u.user_id", 1);
		assertNull(select.getWhereClause());
		assertNotNull(select.getOrderByClause());
		assertEquals(select.getOrderByClause().length, 1);
		assertNull(select.getLimitClause());
		assertEquals(select.getParsedSQL(), sql);
		_parseGlobal("SELECT u.user_id FROM eap_users u, eap_roles r, eap_user_roles ur WHERE u.user_id=ur.user_id AND r.role_id=ur.role_id AND r.role_id = ? ORDER BY user_id", 1);
		_parseGlobal("SELECT u.user_id, r.user_id FROM eap_users u, eap_roles r, eap_user_roles ur WHERE u.user_id=ur.user_id AND r.role_id=ur.role_id AND r.role_id = ? ORDER BY u.user_id", 1);
		_testWrongGlobal("SELECT u.user_id, r.user_id FROM eap_users u, eap_roles r, eap_user_roles ur WHERE u.user_id=ur.user_id AND r.role_id=ur.role_id AND r.role_id = ? ORDER BY user_id");
		select = _parseGlobal("SELECT u.user_id FROM eap_users u, eap_roles r, eap_user_roles ur WHERE u.user_id=ur.user_id AND r.role_id=ur.role_id AND r.role_id = ? ORDER BY user_id LIMIT 0, 10", 1);
		assertNull(select.getWhereClause());
		assertNotNull(select.getOrderByClause());
		assertEquals(select.getOrderByClause().length, 1);
		assertNotNull(select.getLimitClause());
		assertEquals(select.getParsedSQL(), sql+" ORDER BY user_id ASC");
		select = _parseGlobal("SELECT u.user_id FROM eap_users u, eap_roles r, eap_user_roles ur WHERE u.user_id=ur.user_id AND r.role_id=ur.role_id AND r.role_id = ? ORDER BY u.user_id LIMIT 0, 10", 1);
		assertNull(select.getWhereClause());
		assertNotNull(select.getOrderByClause());
		assertEquals(select.getOrderByClause().length, 1);
		assertNotNull(select.getLimitClause());
		assertEquals(select.getParsedSQL(), sql+" ORDER BY u.user_id ASC");
		select = _parseGlobal("SELECT u.user_id FROM eap_users u, eap_roles r, eap_user_roles ur WHERE u.user_id=ur.user_id AND r.role_id=ur.role_id AND r.role_id = ? LIMIT 0, 10", 1);
		assertNull(select.getWhereClause());
		assertNull(select.getOrderByClause());
		assertNotNull(select.getLimitClause());
		assertEquals(select.getParsedSQL(), sql);
		select = _parseGlobal("SELECT u.user_id FROM eap_users u, eap_roles r, eap_user_roles ur WHERE u.user_id=ur.user_id AND r.role_id=ur.role_id AND r.role_id = ? LIMIT 0, ?", 2);
		Limit limit = select.getLimitClause();
		assertEquals(limit.offset, 0);
		assertNull(limit.numRows);
		select = _parseGlobal("SELECT u.user_id FROM eap_users u, eap_roles r, eap_user_roles ur WHERE u.user_id=ur.user_id AND r.role_id=ur.role_id AND r.role_id = ? LIMIT ?", 2);
		limit = select.getLimitClause();
		assertEquals(limit.offset, 0);
		assertNull(limit.numRows);
		String s = "SELECT r.item_id, r.title, r.description, r.status, r.priority, r.raised_by, r.assigned_to, r.time_created, r.date_due, r.mitigating_actions, u.time_updated, u.updated_by, u.description";
		String from = "FROM rit_items_v r LEFT JOIN rit_item_updates_v u ON r.item_id=u.item_id";
		String where = "WHERE (u.update_id IN (SELECT MAX(u2.update_id) FROM rit_item_updates u2 WHERE u2.item_id=r.item_id) OR u.update_id IS NULL)";
		_parseGlobal(s+" "+from+" "+where, 0);
		// Ensure only queries with ORDER BY are parsed:
		_parseGlobal("SELECT u.* FROM users u WHERE name LIKE ?", 1);
		_testWrongGlobal("SELECT u.* FROM users u WHERE name LIKE ? ORDER BY username ASC, age DESC");
		_parseGlobal("SELECT * FROM customers", 0);
		_testWrongGlobal("SELECT * FROM customers ORDER BY user_id");
		_parseGlobal("SELECT COUNT(*) FROM customers", 0);
		select = _parseGlobal("SELECT COUNT(*) FROM customers LIMIT 0,2", 0);
		limit = select.getLimitClause();
		assertNotNull(limit);
		assertEquals(limit.offset, 0);
		assertEquals(limit.numRows, 2);
		// Check complex queries:
		select = _parseGlobal("SELECT COUNT(*) AS count FROM customers ORDER BY count DESC", 0);
		assertEquals(select.getParsedSQL(), "SELECT COUNT(*) AS count FROM customers");
		assertNotNull(select.getOrderByClause());
		OrderBy[] orderBy = select.getOrderByClause();
		assertEquals(orderBy.length, 1);
		assertNull(orderBy[0].field);
		assertEquals(orderBy[0].column, 1);
		assertNull(select.getLimitClause());
		select = _parseGlobal("SELECT u.name, u.username, u.address, u.age FROM users u WHERE name LIKE ? ORDER BY username ASC, age DESC", 1);
		orderBy = select.getOrderByClause();
		assertNotNull(orderBy);
		assertEquals(orderBy.length, 2);
		assertEquals(orderBy[0].column, 2);
		assertEquals(orderBy[0].field, null);
		assertEquals(orderBy[0].order, OrderBy.ORDER.ASC);
		assertEquals(orderBy[1].column, 4);
		assertEquals(orderBy[1].field, null);
		assertEquals(orderBy[1].order, OrderBy.ORDER.DESC);
		select = _parseGlobal("SELECT u.name, u.username, u.address, u.age FROM users u WHERE name LIKE ? ORDER BY username ASC, age DESC LIMIT ?, ?", 3);
		assertNotNull(select.getOrderByClause());
		assertNotNull(select.getLimitClause());
		s = "SELECT uv.user_id, uv.username, uv.full_name, uv.status, r.name, uv.time_created";
		from = "FROM eap_users_v uv, eap_user_roles ur, eap_roles r";
		where = "WHERE uv.username != \"su\" AND ur.user_id=uv.user_id AND ur.role_id=r.role_id AND ur.primary_role=\"T\"";
		_parseGlobal(s+" "+from+" "+where, 0);
		select = _parseGlobal("SELECT product_id, SUM(quantity) AS total FROM line_items GROUP BY product_id ORDER BY total DESC", 0);
		orderBy = select.getOrderByClause();
		assertNotNull(orderBy);
		assertEquals(orderBy.length, 1);
		assertEquals(orderBy[0].column, 2);
		assertEquals(orderBy[0].field, null);
		assertEquals(orderBy[0].order, OrderBy.ORDER.DESC);
	}

	@Test
	public void testUpdates() {
		// Update statements:
		Update update = null;
		update = _parseUpdate("UPDATE orders SET status = ? WHERE customer_id = ? AND status = ?", Seller.getOrders().getMetaData(), 3);
		assertNotNull(update.getWhereClause());
		_parseUpdate("UPDATE orders o SET o.status = ? WHERE o.customer_id = ? AND o.status = ?", Seller.getOrders().getMetaData(), 3);
		_parseUpdate("UPDATE orders o SET status = ? WHERE o.customer_id = ? AND o.status = ?", Seller.getOrders().getMetaData(), 3);
		_parseUpdate("UPDATE orders o SET status = ? WHERE customer_id = ? AND status = 'D'", Seller.getOrders().getMetaData(), 2);
		update = _parseUpdate("UPDATE orders o SET status = ?", Seller.getOrders().getMetaData(), 1);
		assertNull(update.getWhereClause());
		_parseUpdate("UPDATE orders SET status = 'D' WHERE customer_id = 100 AND status = 'F'", Seller.getOrders().getMetaData(), 0);
		_testWrongUpdate("UPDATE orders SET status = 'D' WHERE customer_id = 'D' AND status = 'F'", Seller.getOrders().getMetaData());
		_parseUpdate("UPDATE orders SET status = ?, date_made = ? WHERE customer_id = ? AND status = ?", Seller.getOrders().getMetaData(), 4);
		_parseUpdate("UPDATE orders SET status = ?, date_made = [date()] WHERE customer_id = ? AND status = ?", Seller.getOrders().getMetaData(), 3);
		_testWrongUpdate("UPDATE orders SET status = ?, date_made = [now()] WHERE customer_id = ? AND status = ?", Seller.getOrders().getMetaData());
		// Delete statements:
		update = _parseUpdate("DELETE FROM orders", Seller.getOrders().getMetaData(), 0);
		assertNull(update.getWhereClause());
		_testWrongUpdate("DELETE FROM orders SET", Seller.getOrders().getMetaData());
		update = _parseUpdate("DELETE FROM orders WHERE order_id = ?", Seller.getOrders().getMetaData(), 1);
		assertNotNull(update.getWhereClause());
	}

	private Select _parseSelect(String sql, MetaData md, int params, boolean unique) {
		Select select = Parser.parseSelect("anonymous", sql, md, _rep);
		assertTrue(select.uniqueResult() == unique);
		assertEquals(select.getParameterCount(), params);
		return select;
	}
	
	private Select _parseSelect(String sql, MetaData md, int params) {
		return _parseSelect(sql, md, params, false);
	}

	private void _testWrongSelect(String sql, MetaData md) {
		try {
			Parser.parseSelect("anonymous", sql, md, _rep);
			fail();
		}
		catch(ParseException pe) {
			println(pe.getMessage()+" ["+sql+"]");
		}
	}

	private Select _parseGlobal(String sql, int params) {
		Select select = Parser.parseGlobal(null, sql, _rep);
		assertEquals(select.getParameterCount(), params);
		return select;
	}

	private void _testWrongGlobal(String sql) {
		try {
			Parser.parseGlobal(null, sql, _rep);
			fail();
		}
		catch(ParseException pe) {
			println(pe.getMessage()+" ["+sql+"]");
		}
	}

	private Update _parseUpdate(String sql, MetaData md, int params) {
		Update update = Parser.parseUpdate(null, sql, md, _rep);
		assertEquals(update.getParameterCount(), params);
		return update;
	}

	private void _testWrongUpdate(String sql, MetaData md) {
		try {
			Parser.parseUpdate("anonymous", sql, md, _rep);
			fail();
		}
		catch(ParseException pe) {
			println(pe.getMessage()+" ["+sql+"]");
		}
	}
}
