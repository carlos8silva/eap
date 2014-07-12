package com.netx.ut.eap.R1;
import java.io.PrintWriter;
import com.netx.generics.R1.sql.Database;
import com.netx.generics.R1.sql.JdbcDriver;
import com.netx.generics.R1.util.UnitTester;
import com.netx.basic.R1.io.Directory;
import com.netx.basic.R1.io.FileNotFoundException;
import com.netx.basic.R1.io.AccessDeniedException;
import com.netx.basic.R1.io.ReadWriteException;
import com.netx.bl.R1.core.MetaData;
import com.netx.bl.R1.core.Repository;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.BLException;
import com.netx.bl.R1.core.RepositoryConfig;
import com.netx.bl.R1.core.Validator;
import com.netx.bl.R1.core.Field;
import com.netx.eap.R1.core.*;
import com.netx.eap.R1.bl.*;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;



public class UnitTest extends UnitTester {

	// TYPE:
	public static void main(String[] args) throws Exception {
		UnitTest ut = new UnitTest();
		for(MetaData m : Functions.getInstance().getMetaData().getLinkedEntities()) {
			ut.println(m.getTableName());
		}
		ut.println("done.");
	}

	// INSTANCE:
	private Repository _rep;
	
	@BeforeClass
	// TODO remove this with JDK7
	@SuppressWarnings("unchecked")
	public void setUp() throws BLException {
		Database details = new Database(JdbcDriver.MYSQL, "localhost", 3306, "cubigraf_r3", "root", "eagle");
		//ConnectionDetails details = new ConnectionDetails(JdbcDriver.MYSQL, "eap.dyndns-remote.com", 3306, "cubigraf_r3", "root", "eagle");
		RepositoryConfig config = new RepositoryConfig();
		config.setCacheEnabled(true);
		config.setUsePreparedStatements(true);
		_rep = Repository.load(EAP.class);
		_rep.connect(details, config);
	}

	@AfterClass
	public void exit() throws BLException {
		_rep.disconnect();
		println();
	}

	@Test
	public void testTemplate() throws FileNotFoundException, AccessDeniedException, ReadWriteException {
		// Load the template file:
		Directory dir = getTestResourceLocation().getDirectory("eap.R1.core");
		Template tplt = new Template(dir.getFile("template.txt"), null, false);
		// Set values:
		Values v = tplt.getValues();
		v.set("var", "this-is-a-variable");
		// Try setting it again:
		try {
			v.set("var", "whatever");
			fail();
		}
		catch(IllegalArgumentException iae) {
			println(iae);
		}
		ValueList vl = v.getList("a-list");
		for(int i=1; i<=3; i++) {
			Values listValues = vl.next();
			listValues.set("i", i+"");
		}
		boolean ifTest = false;
		Values vIf = v.setIf("an-if", ifTest);
		if(ifTest == false) {
			vIf.set("thing", "apples");
		}
		else {
			// Test that it breaks:
			try {
				vIf.set("thing", "apples");
				fail();
			}
			catch(IllegalArgumentException iae) {
				println(iae);
			}
		}
		try {
			vIf = v.setIf("an-if", true);
			fail();
		}
		catch(IllegalArgumentException iae) {
			println(iae);
		}
		
		// Person 1:
		vl = v.getList("people");
		Values person = vl.next();
		person.set("name", "Isabel");
		ValueList children = person.getList("children");
		children.next().set("child-name", "Carlos");
		children.next().set("child-name", "Nando");
		children.next().set("child-name", "Jorge");
		// Person 2:
		person = vl.next();
		person.set("name", "Fernando");
		person.setIf("male", true);
		children = person.getList("children");
		children.next().set("child-name", "Filipa");
		children.next().set("child-name", "Luís");
		// TODO assertions to make sure that the file output is what we expect
		// Traverse the data structure:
		println("Traversing template:");
		tplt.render(new PrintWriter(System.out));
	}

	@Test
	public void testValidators() {
		Validator username = new Validators.Username();
		Validator password = new Validators.Password();
		Validator alphaText = new Validators.AlphaText();
		Validator readText = new Validators.ReadableText();
		Validator codeIdentifier = new Validators.CodeIdentifier();
		_validate("9asd", username, EAP.getUsers().getMetaData().username, false);
		_validate("asd[]ooo", username, EAP.getUsers().getMetaData().username, false);
		_validate("abc$", username, EAP.getUsers().getMetaData().username, false);
		_validate("carlos silva", username, EAP.getUsers().getMetaData().username, false);
		_validate("silvac", username, EAP.getUsers().getMetaData().username, true);
		_validate(" abc", password, EAP.getUsers().getMetaData().password, false);
		_validate("abc ", password, EAP.getUsers().getMetaData().password, false);
		_validate(" dg5td&%_kk ", password, EAP.getUsers().getMetaData().password, false);
		_validate("dg5td&%_kk", password, EAP.getUsers().getMetaData().password, true);
		_validate("dg5td&%_kk", alphaText, EAP.getUsers().getMetaData().firstName, false);
		_validate("aaa9", alphaText, EAP.getUsers().getMetaData().firstName, false);
		_validate("aaa9", readText, EAP.getUsers().getMetaData().firstName, true);
		_validate("Dara O'Brien", alphaText, EAP.getUsers().getMetaData().firstName, true);
		_validate("Dara O'Brien", codeIdentifier, EAP.getUsers().getMetaData().firstName, false);
		_validate("Dara >O'Brien", codeIdentifier, EAP.getUsers().getMetaData().firstName, false);
	}

	private void _validate(String expr, Validator v, Field f, boolean succeed) {
		String msg = v.validate(f, expr);
		if(msg == null && !succeed) {
			fail();
		}
		if(msg != null) {
			println(msg);
		}
	}

	@Test
	public void testData() throws BLException {
		final Connection c = _rep.getConnection();
		// Update user and ensure it is reflected in the cache:
		String username = "gengis.kahn";
		User gengis = Users.getInstance().getUserByUsername(c, username);
		String newPass = Users.generatePassword(10);
		gengis.setPassword(newPass);
		Users.getInstance().save(c, gengis, null, null);
		gengis = Users.getInstance().getUserByUsername(c, username);
		assertEquals(gengis.getPassword(), newPass);
		// Test hasUpdates:
		User jeitoso = Users.getInstance().getUserByUsername(c, "jeitoso.tenebroso");
		jeitoso.setFirstName("Jeitoso");
		jeitoso.setLastName("Tenebroso");
		jeitoso.setMiddleInitial(null);
		assertFalse(jeitoso.hasUpdates());
		// Test scrambled text with prepared statements:
		username = "carlos.silva";
		User carlos = Users.getInstance().getUserByUsername(c, username);
		carlos.getPrimaryRole(c);
		Role tmp = Roles.getInstance().getRoleByName(c, "Temporary Role");
		String description = tmp.getDescription();
		tmp.setDescription("Some scrambled text: \"'%&");
		Roles.getInstance().update(c, tmp, null);
		tmp.setDescription(description);
		Roles.getInstance().update(c, tmp, null);
		// Test scrambled text without prepared statements:
		// TODO create test case to ensure that this closes every open/cached PSTMT
		c.getRepository().getConfig().setUsePreparedStatements(false);
		tmp.setDescription("Some scrambled text: \"'%&");
		Roles.getInstance().update(c, tmp, null);
		tmp.setDescription(description);
		Roles.getInstance().update(c, tmp, null);
		c.close();
	}
}
