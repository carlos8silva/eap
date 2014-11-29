package com.netx.bl.R1.spi;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import com.netx.generics.R1.sql.JdbcDriver;
import com.netx.generics.R1.util.Version;
import com.netx.basic.R1.eh.Checker;


public class DriverRegistry {

	// TYPE:
	private static final Map<String,List<DatabaseDriver>> _registry = new HashMap<String,List<DatabaseDriver>>();

	// Automatically register supported drivers:
	static {
		registerDriver(new MySQL41Driver());
	}
	
	public static void registerDriver(DatabaseDriver driver) {
		Checker.checkNull(driver, "driver");
		if(driver.getMaxSupportedProductVersion().getVersionNumbers().length > 2) {
			throw new IllegalArgumentException("can only specify major and minor version numbers for supported database products, specified "+driver.getMaxSupportedProductVersion());
		}
		if(driver.getMinSupportedProductVersion().getVersionNumbers().length > 2) {
			throw new IllegalArgumentException("can only specify major and minor version numbers for supported database products, specified "+driver.getMinSupportedProductVersion());
		}
		List<DatabaseDriver> drivers = _registry.get(driver.getJdbcDriverName());
		if(drivers != null) {
			for(DatabaseDriver d : drivers) {
				// Equality:
				if(driver.getMinSupportedProductVersion().compareTo(d.getMinSupportedProductVersion()) == 0) {
					_throwDriverAlreadyExists(driver.getJdbcDriverName(), driver.getMinSupportedProductVersion());
				}
				if(driver.getMaxSupportedProductVersion().compareTo(d.getMaxSupportedProductVersion()) == 0) {
					_throwDriverAlreadyExists(driver.getJdbcDriverName(), driver.getMaxSupportedProductVersion());
				}
				if(driver.getMaxSupportedProductVersion().compareTo(d.getMinSupportedProductVersion()) == 0) {
					_throwDriverAlreadyExists(driver.getJdbcDriverName(), driver.getMaxSupportedProductVersion());
				}
				if(driver.getMinSupportedProductVersion().compareTo(d.getMaxSupportedProductVersion()) == 0) {
					_throwDriverAlreadyExists(driver.getJdbcDriverName(), driver.getMinSupportedProductVersion());
				}
				// Overlap:
				if(driver.getMaxSupportedProductVersion().before(d.getMaxSupportedProductVersion()) && driver.getMaxSupportedProductVersion().after(d.getMinSupportedProductVersion())) {
					_throwOverlap(d);
				}
				if(driver.getMinSupportedProductVersion().after(d.getMinSupportedProductVersion()) && driver.getMinSupportedProductVersion().before(d.getMaxSupportedProductVersion())) {
					_throwOverlap(d);
				}
				if(driver.getMaxSupportedProductVersion().after(d.getMaxSupportedProductVersion()) && driver.getMinSupportedProductVersion().before(d.getMinSupportedProductVersion())) {
					_throwOverlap(d);
				}
			}
		}
		else {
			drivers = new ArrayList<DatabaseDriver>();
			_registry.put(driver.getJdbcDriverName(), drivers);
		}
		drivers.add(driver);
	}
	
	public static DatabaseDriver getDriverFor(JdbcDriver jdbcDriver, Version v) {
		List<DatabaseDriver> drivers = _registry.get(jdbcDriver.getName());
		if(drivers == null) {
			return null;
		}
		for(DatabaseDriver driver : drivers) {
			if(v.compareTo(driver.getMaxSupportedProductVersion()) <= 0 && v.compareTo(driver.getMinSupportedProductVersion()) >= 0) {
				return driver;
			}
		}
		return null;
	}

	public static DatabaseDriver getLatestDriverFor(JdbcDriver jdbcDriver) {
		List<DatabaseDriver> drivers = _registry.get(jdbcDriver.getName());
		if(drivers == null || drivers.isEmpty()) {
			return null;
		}
		Iterator<DatabaseDriver> it = drivers.iterator();
		DatabaseDriver latest = it.next();
		while(it.hasNext()) {
			DatabaseDriver d = it.next();
			if(latest.getMaxSupportedProductVersion().compareTo(d.getMaxSupportedProductVersion()) < 0) {
				latest = d;
			}
		}
		return latest;
	}

	private static void _throwDriverAlreadyExists(String driverName, Version v) {
		throw new IllegalArgumentException("there is already a database driver registered for "+driverName+" ["+v+"]");
	}
	
	private static void _throwOverlap(DatabaseDriver driver) {
		throw new IllegalArgumentException("attempted to register driver for "+driver.getJdbcDriverName()+" which overlaps with existing driver registered for versions ["+driver.getMinSupportedProductVersion()+"-"+driver.getMaxSupportedProductVersion()+"]");
	}
}
