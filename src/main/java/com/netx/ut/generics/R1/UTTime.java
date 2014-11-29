package com.netx.ut.generics.R1;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import com.netx.generics.R1.util.UnitTester;
import com.netx.generics.R1.time.*;
import com.netx.basic.R1.shared.Globals;
import com.netx.basic.R1.shared.DATE_ORDER;
import org.testng.annotations.Test;


// TODO use assertions
public class UTTime extends UnitTester {

	@Test
	public void testDate() {
		// 1) Test date parsing:
		assertEquals(Date.parse("20080102").toString(), "02-Jan-2008");
		assertEquals(Date.parse("2008-01-02").toString(), "02-Jan-2008");
		assertEquals(Date.parse("2008/01/02").toString(), "02-Jan-2008");
		Globals.setDateOrder(DATE_ORDER.EU);
		assertEquals(Date.parse("02-01-2008").toString(), "02-Jan-2008");
		assertEquals(Date.parse("02/01/2008").toString(), "02-Jan-2008");
		Globals.setDateOrder(DATE_ORDER.US);
		assertEquals(Date.parse("02-01-2008").toString(), "01-Feb-2008");
		assertEquals(Date.parse("02/01/2008").toString(), "01-Feb-2008");
		// 2) Test changing the date:
		Date toChange = new Date(2007, 10, 15);
		assertEquals(toChange.toString(), "15-Oct-2007");
		toChange = toChange.setDay(31);
		assertEquals(toChange.toString(), "31-Oct-2007");
		try {
			toChange = toChange.setDay(32);
			fail();
		}
		catch(IllegalArgumentException iae) {
			println(iae);
		}
		// 3) Now change the date again while it is lenient:
		toChange.setLenient(true);
		toChange = toChange.setDay(32);
		assertEquals(toChange.toString(), "01-Nov-2007");
		toChange = toChange.setMonth(10).setDay(10);
		assertEquals(toChange.toString(), "10-Oct-2007");
		toChange = toChange.setDay(toChange.getDay()-15);
		assertEquals(toChange.toString(), "25-Sep-2007");
		toChange = toChange.setMonth(15);
		assertEquals(toChange.toString(), "25-Mar-2008");
		toChange = toChange.setDay(toChange.getDay()-25);
		// 2008 is a leap year:
		assertEquals(toChange.toString(), "29-Feb-2008");
		// 4) Comparisons
		// TODO
	}
	
	@Test
	public void testTimeFormat() {
		DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
		assertEquals(Time.parse("15:21:59.343").format(df), "15:21:59.343");
		assertEquals(Time.parse("15:21:59").format(df), "15:21:59.000");
		assertEquals(Time.parse("15:21").format(df), "15:21:00.000");
	}
	
	@Test
	public void testTimestampFormat() {
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
		assertEquals(Timestamp.parse("20080102 15:21:59.343").format(df), "02/01/2008 15:21:59.343");
		assertEquals(Timestamp.parse("20080102 15:21:59").format(df), "02/01/2008 15:21:59.000");
		assertEquals(Timestamp.parse("20080102 15:21").format(df), "02/01/2008 15:21:00.000");
	}
	
	// TODO add tests for TimeValue
}
