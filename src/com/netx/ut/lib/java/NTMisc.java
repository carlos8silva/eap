package com.netx.ut.lib.java;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.nio.charset.Charset;


public class NTMisc extends com.netx.generics.R1.util.UnitTester {

	private static final int length = 15;

	public static void main(String[] args) throws Throwable {
		NTMisc nt = new NTMisc();
		nt.testSecureRandom();
		//nt.testJndiFilesystem();
		nt.testEncodings();
	}
	
	public void testSecureRandom() throws Throwable {
		SecureRandom random = new SecureRandom();
		random.setSeed(Calendar.getInstance().getTimeInMillis());
		byte[] id = new byte[length];
		random.nextBytes(id);
		for(int i=0; i<length; i++) {
			print(id[i]);
		}
	}
	
	// TODO this method does not work since we need a JAR for the JNDI file system provider
	public void testJndiFilesystem() throws Exception {
		String name = "autoexec.bat";
		Hashtable<Object,String> env = new Hashtable<Object,String>(11);
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
		env.put(Context.PROVIDER_URL, "file:/C:\\");
		try {
			InitialContext initCtx = new InitialContext(env);
			Object obj = initCtx.lookup(name);
			println(name + " is bound to: " + obj.getClass().getName());
			initCtx.close();
		} catch (NamingException e) {
			System.err.println("Problem looking up " + name + ": " + e);
		}
	}
	
	public void testEncodings() throws IOException {
		println(System.getProperty("file.encoding"));
		println(new String("âbç".getBytes(),System.getProperty("file.encoding")));
		Iterator<Charset> it = Charset.availableCharsets().values().iterator();
		while(it.hasNext()) {
			println(it.next().name());
		}
	}

}
