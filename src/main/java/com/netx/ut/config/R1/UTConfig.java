package com.netx.ut.config.R1;
import java.util.Iterator;
import com.netx.generics.R1.util.Strings;
import com.netx.generics.R1.util.UnitTester;
import com.netx.generics.R1.translation.Results;
import com.netx.generics.R1.translation.BasicMessageFormatter;
import com.netx.generics.R1.translation.MessagePrinter;
import com.netx.basic.R1.io.File;
import com.netx.basic.R1.io.FileSystemException;
import com.netx.basic.R1.logging.Logger;
import com.netx.basic.R1.shared.OPTIMIZATION;
import com.netx.basic.R1.shared.RUN_MODE;
import com.netx.basic.R1.eh.ObjectNotFoundException;
import com.netx.bl.R1.core.RepositoryConfig;
import com.netx.config.R1.*;
import org.testng.annotations.Test;


public class UTConfig extends UnitTester {

	@Test
	public void testGlobalsContext() throws TypeLoadException {
		// Try to create a context object with a wrong id:
		Context ctx = null;
		try {
			ctx = new Context("anId");
			fail();
		}
		catch(IllegalArgumentException iae) {
			println(iae);
		}
		// This should work now:
		ctx = new Context("globals");
		// Try to get a property before locking:
		try {
			ctx.getInteger("tab-size");
			fail();
		}
		catch(IllegalStateException ise) {
			println(ise);
		}
		// Lock it now:
		ctx.lock();
		// Get a non-existing sub-context:
		try {
			ctx.getContext("sub-ctx");
			fail();
		}
		catch(ObjectNotFoundException onfe) {
			println(onfe);
		}
		// Get a non-existing property:
		try {
			ctx.getObject("a-property-that-doesnt-exist");
			fail();
		}
		catch(ObjectNotFoundException onfe) {
			println(onfe);
		}
		// Get a non-existing property within a path:
		try {
			ctx.getObject("subctx1/subctx2/pop");
			fail();
		}
		catch(ObjectNotFoundException onfe) {
			println(onfe);
		}
		// Check the default values:
		assertEquals(ctx.getInteger("tab-size"), 4);
		assertEquals(ctx.getObject("run-mode"), RUN_MODE.PROD);
		assertEquals(ctx.getString("locale"), "gb_GB");
		assertEquals(ctx.getObject("optimization"), OPTIMIZATION.PROCESSOR);
		assertEquals(ctx.getObject("logger").toString(), "Logger: System.out");
		// Try to set wrong type:
		try {
			ctx.setProperty("tab-size", "100");
			fail();
		}
		catch(UnexpectedTypeException ute) {
			println(ute);
		}
		// Try to set read-only property:
		try {
			ctx.setProperty("optimization", "100");
			fail();
		}
		catch(ReadOnlyPropertyException rope) {
			println(rope);
		}
		// Try to set a mandatory property to null:
		try {
			ctx.setProperty("run-mode", null);
			fail();
		}
		catch(MandatoryPropertyException mpe) {
			println(mpe);
		}
		// Now set a property correctly:
		ctx.setProperty("tab-size", 5);
		// Just use the logger to ensure it is working properly:
		((Logger)ctx.getObject("logger")).info("test 01 finished");
	}
	
	@Test
	public void testDatabaseConfig() {
		RepositoryConfig cfg = new RepositoryConfig();
		// Check whether all defaults are correct:
		assertTrue(cfg.getCacheEnabled());
		assertEquals(cfg.getDisableCacheDaemonDelay().toString(), "2s");
		assertEquals(cfg.getNumberOfLockAttemptsBeforeFailure(), 2);
		assertEquals(cfg.getNumberOfLockAttemptsBeforeRelease(), 3);
		assertEquals(cfg.getSleepTimeBeforeLockRetry().toString(), "1s");
		assertEquals(cfg.getSleepTimeAfterLockRelease().toString(), "1s");
		assertFalse(cfg.getUsePreparedStatements());
	}

	@Test
	public void testMapDefaults() {
		Context ctx = new Context("map-defaults");
		ctx.lock();
		_printHierarchy(ctx, 0);
		assertEquals(ctx.getMapInteger("integers", "one"), 1);
	}
	
	// TODO this test case does not work (we are not locking a context somewhere)
	public void testReadConfigFile() throws FileSystemException {
		final File file = getTestResourceLocation().getFile("config.R1/test.config.xml");
		Results r = Config.loadConfig(file.getInputStream());
		BasicMessageFormatter mf = new BasicMessageFormatter();
		MessagePrinter mp = new MessagePrinter(System.out);
		if(!r.getErrorList().getErrors().isEmpty()) {
			mp.print(r.getErrorList().getErrors(mf));
		}
		if(!r.getErrorList().getWarnings().isEmpty()) {
			mp.print(r.getErrorList().getWarnings(mf));
		}
		_printHierarchy(Context.getRoot(), 0);
	}

	// TODO make this method available to apps.
	private void _printHierarchy(Context ctx, int level) {
		String pad1 = level == 0 ? "" : Strings.repeat(" ", 4*level);
		String pad2 = Strings.repeat(" ", 4*(level+1));
		println(pad1+ctx.getName()+" {");
		Iterator<Property> it = ctx.getProperties().iterator();
		while(it.hasNext()) {
			Property prop = it.next();
			println(pad2+prop.getName()+": "+prop.getValue());
		}
		Iterator<Context> itCtx = ctx.getContexts().iterator();
		while(itCtx.hasNext()) {
			_printHierarchy(itCtx.next(), level + 1);
		}
		println(pad1+"}");
	}
	
	// INSTANCE:
	/*
	private final ConfigContext _ctx = new ConfigContext("aContext", null);

	public void t01_InitContext() {
		assertEquals(_ctx.getName(), "aContext");
		// Add some properties:
		_ctx.setProperty("anInteger", 100);
		_ctx.setProperty("aFloat", new Float(39.6));
		_ctx.setProperty("aString", "the quick brown fox jumped over the lazy dog");
		_ctx.addContext("subCtx");
		_ctx.setProperty("subCtx/subProp1", "aaa");
		// Check properties:
		assertEquals(_ctx.getInteger("anInteger"), 100);
		assertEquals(_ctx.getFloat("aFloat"), 39.6F);
		assertEquals(_ctx.getString("aString", "the quick brown fox jumped over the lazy dog");
		assertEquals(_ctx.getString("subCtx/subProp1", "aaa");
	}
	
	public void t02_TestMaps() {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("key1", "prop1");
		map.put("key2", "prop2");
		map.put("key3", "prop3");
		map.put("key4", "prop4");
		_ctx.setProperty("aMap", map);
		map = _ctx.getMap("aMap");
		// TODO assertions on the map's values
	}
	*/
	
	//public void t99_PrintContext() {
	//	_printHierarchy(_ctx, 0);
	//}
}
