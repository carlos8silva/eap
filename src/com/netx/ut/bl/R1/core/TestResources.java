package com.netx.ut.bl.R1.core;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import com.netx.bl.R1.core.Argument;
import com.netx.bl.R1.core.BLException;
import com.netx.bl.R1.spi.DatabaseDriver;
import com.netx.generics.R1.util.Version;


public class TestResources {

	public static abstract class DriverBase implements DatabaseDriver {

		// Keep the Strings as static variables to avoid creating them
		// at each method invocation:
		private static final String _DRIVER_NAME = "SuperDatabase";
		private static final String _NATIVE_BOOLEAN = "TINYINT";
		private static final String _NATIVE_BYTE = "TINYINT UNSIGNED";
		private static final String _NATIVE_SHORT = "SMALLINT";
		private static final String _NATIVE_INT = "INT";
		private static final String _NATIVE_LONG = "BIGINT";
		private static final String _NATIVE_FLOAT = "FLOAT";
		private static final String _NATIVE_DOUBLE = "DOUBLE";
		private static final String _NATIVE_TEXT_256 = "VARCHAR(";
		private static final String _NATIVE_TEXT_64K = "TEXT";
		private static final String _NATIVE_TEXT_16M = "MEDIUMTEXT";
		private static final String _NATIVE_TEXT_4G = "LONGTEXT";
		private static final String _NATIVE_BINARY_256 = " BINARY";
		private static final String _NATIVE_BINARY_64K = "BLOB";
		private static final String _NATIVE_BINARY_16M = "MEDIUMBLOB";
		private static final String _NATIVE_BINARY_4G = "LARGEBLOB";
		private static final String _NATIVE_MOMENT = "DATETIME";
		private static final String _NATIVE_DATE = "DATE";
		private static final String _NATIVE_TIME = "TIME";
		private static final String _MOMENT_FORMAT = "yyyy-MM-dd HH:mm:ss";
		private static final String _DATE_FORMAT = "yyyy-MM-dd";
		private static final String _TIME_FORMAT = "HH:mm:ss";

		public DriverBase() {
		}
		
		public String getJdbcDriverName() {
			return _DRIVER_NAME;
		}

		public String getNativeBoolean() {
			return _NATIVE_BOOLEAN;
		}

		public String getNativeByte() {
			return _NATIVE_BYTE;
		}

		public String getNativeShort() {
			return _NATIVE_SHORT;
		}

		public String getNativeInt() {
			return _NATIVE_INT;
		}

		public String getNativeLong() {
			return _NATIVE_LONG;
		}

		public String getNativeFloat() {
			return _NATIVE_FLOAT;
		}

		public String getNativeDouble() {
			return _NATIVE_DOUBLE;
		}

		public String getNativeText(int length) {
			if(length < Math.pow(2, 8)) { // 256
				return _NATIVE_TEXT_256+length+')';
			}
			else if(length < Math.pow(2, 16)) { // 65536, 64K
				return _NATIVE_TEXT_64K;
			}
			else if(length < Math.pow(2, 24)) { // 16777216, 16M
				return _NATIVE_TEXT_16M;
			}
			else if(length < Math.pow(2, 32)) { // 4294967296, 4G
				return _NATIVE_TEXT_4G;
			}
			else {
				throw new IllegalArgumentException("invalid field length: "+length);
			}
		}

		public String getNativeBinary(int length) {
			if(length < Math.pow(2, 8)) { // 256
				return _NATIVE_TEXT_256+length+')'+_NATIVE_BINARY_256;
			}
			else if(length < Math.pow(2, 16)) { // 65536, 64K
				return _NATIVE_BINARY_64K;
			}
			else if(length < Math.pow(2, 24)) { // 16777216, 16M
				return _NATIVE_BINARY_16M;
			}
			else if(length < Math.pow(2, 32)) { // 4294967296, 4G
				return _NATIVE_BINARY_4G;
			}
			else {
				throw new IllegalArgumentException("invalid field length: "+length);
			}
		}

		public String getNativeDateTime() {
			return _NATIVE_MOMENT;
		}

		public String getNativeDate() {
			return _NATIVE_DATE;
		}

		public String getNativeTime() {
			return _NATIVE_TIME;
		}

		public DateFormat getDateTimeFormat() {
			return new SimpleDateFormat(_MOMENT_FORMAT);
		}

		public DateFormat getDateFormat() {
			return new SimpleDateFormat(_DATE_FORMAT);
		}

		public DateFormat getTimeFormat() {
			return new SimpleDateFormat(_TIME_FORMAT);
		}
		
		public BLException translateException(SQLException sqle, String query, Argument[] args) {
			return null;
		}

		public boolean supportsPreparedStatements() {
			return true;
		}
		
		public boolean supportsLimitClause() {
			return true;
		}

		public String toString() {
			return getJdbcDriverName() + " ["+this.getMinSupportedProductVersion()+"-"+this.getMaxSupportedProductVersion()+"]";
		}
	}

	public static class Driver_42_49 extends DriverBase {

		public Version getMinSupportedProductVersion() {
			return new Version(4, 2);
		}

		public Version getMaxSupportedProductVersion() {
			return new Version(4, 9);
		}
	}

	public static class Driver_42_51 extends DriverBase {

		public Version getMinSupportedProductVersion() {
			return new Version(4, 2);
		}

		public Version getMaxSupportedProductVersion() {
			return new Version(5, 1);
		}
	}

	public static class Driver_50_50 extends DriverBase {

		public Version getMinSupportedProductVersion() {
			return new Version(5, 0);
		}

		public Version getMaxSupportedProductVersion() {
			return new Version(5, 0);
		}
	}
	
	public static class Driver_51_55 extends DriverBase {

		public Version getMinSupportedProductVersion() {
			return new Version(5, 1);
		}

		public Version getMaxSupportedProductVersion() {
			return new Version(5, 5);
		}
	}

	public static class Driver_50_60 extends DriverBase {

		public Version getMinSupportedProductVersion() {
			return new Version(5, 0);
		}

		public Version getMaxSupportedProductVersion() {
			return new Version(6, 0);
		}
	}

	public static class Driver_55_60 extends DriverBase {

		public Version getMinSupportedProductVersion() {
			return new Version(5, 5);
		}

		public Version getMaxSupportedProductVersion() {
			return new Version(6, 0);
		}
	}

	public static class Driver_56_60 extends DriverBase {

		public Version getMinSupportedProductVersion() {
			return new Version(5, 6);
		}

		public Version getMaxSupportedProductVersion() {
			return new Version(6, 0);
		}
	}
	
	public static class Driver_39_70 extends DriverBase {

		public Version getMinSupportedProductVersion() {
			return new Version(3, 9);
		}

		public Version getMaxSupportedProductVersion() {
			return new Version(7, 0);
		}
	}

	public static class Driver_422 extends DriverBase {

		public Version getMinSupportedProductVersion() {
			return new Version(4, 2, 2);
		}

		public Version getMaxSupportedProductVersion() {
			return new Version(4, 2, 2);
		}
	}
}
