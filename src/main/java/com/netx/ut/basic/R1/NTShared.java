package com.netx.ut.basic.R1;
import java.util.Locale;
import com.netx.generics.R1.util.UnitTester;
import com.netx.basic.R1.shared.*;


public class NTShared extends UnitTester {

	public static void main(String[] args) throws Throwable {
		final NTShared nt = new NTShared();
		nt.t01_ChangeLocales();
	}

	public void t01_ChangeLocales() {
		println("Application locale: "+Globals.getApplicationLocale());
		println("System locale: "+Globals.getSystemLocale());
		Globals.setApplicationLocale(new Locale("en", "GB"));
		Globals.setSystemLocale(new Locale("en", "US"));
		println("Application locale: "+Globals.getApplicationLocale());
		println("System locale: "+Globals.getSystemLocale());
	}
}
