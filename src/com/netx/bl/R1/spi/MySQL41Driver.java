package com.netx.bl.R1.spi;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import com.netx.bl.R1.core.Argument;
import com.netx.bl.R1.core.BLException;
import com.netx.generics.R1.util.Version;


// TODO change the name of the driver, since this supports more than just 4.1.
public class MySQL41Driver implements DatabaseDriver {
	
	// Keep the Strings as static variables to avoid creating them
	// at each method invocation:
	private static final String _DRIVER_NAME = "MySQL";
	private static final String _TO_STRING = "MySQL 4.1";
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
	// We use DATETIME instead of TIMESTAMP because MySQL's TIMESTAMP only
	// supports dates from '1970-01-01 00:00:01' to '2038-01-19 03:14:07',
	// while DATETIME supports from ''1000-01-01 00:00:00' to '9999-12-31 23:59:59'
	// http://dev.mysql.com/doc/refman/5.0/en/datetime.html
	private static final String _NATIVE_DATETIME = "DATETIME";
	private static final String _NATIVE_DATE = "DATE";
	private static final String _NATIVE_TIME = "TIME";
	private static final String _TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String _DATE_FORMAT = "yyyy-MM-dd";
	private static final String _TIME_FORMAT = "HH:mm:ss";

	// MySQL error messages:
	// MSG: Server connection failure during transaction. Due to underlying exception: 'java.net.SocketException: java.net.ConnectException: Connection refused: connect'.
	private static final String _E_DB_UNAVAILABLE_01 = "Connection refused: connect";
	// MSG: com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException: Could not create connection to database server. Attempted reconnect 3 times. Giving up.
	private static final String _E_DB_UNAVAILABLE_02 = "Could not create connection to database server";
	// MSG: Caused by: java.sql.SQLException: Server connection failure during transaction. Due to underlying exception: 'java.sql.SQLException: Access denied for user 'root'@'localhost' (using password: YES)'.
	private static final String _E_LOGIN_FAILED = "Access denied for user";
	// MSG: Cannot delete or update a parent row: a foreign key constraint fails
	private static final String _E_CONSTRAINT_FRGNK = "a foreign key constraint fails";
	// MSG: Duplicate entry 'MHRA' for key 2
	// MSG: Duplicate entry '100-100-C' for key 1
	private static final String _E_CONSTRAINT_UNIQUE = "Duplicate entry";
	// MSG: Table 'business_logic.lineitems' doesn't exist
	private static final String _E_MALFORMED_QUERY_01_01 = "Table";
	private static final String _E_MALFORMED_QUERY_01_02 = "doesn't exist";
	// MSG: Column not found, message from server: "Unknown column 'codigo' in 'order clause'"
	// MSG: Unknown column 'registration_date' in 'field list'
	private static final String _E_MALFORMED_QUERY_02 = "Unknown column";
	// MSG: You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near '3, 5' at line 1
	private static final String _E_MALFORMED_QUERY_03 = "You have an error in your SQL syntax";
	// MSG = Lock wait timeout exceeded; try restarting transaction
	private static final String _E_LOCK_TIMEOUT = "Lock wait timeout exceeded; try restarting transaction";
	// MSG = Unknown database 'business_logic'
	private static final String _E_UNKNOWN_DATABASE = "Unknown database";
	// MSG = Data truncated for column 'middle_initial' at row 1
	private static final String _E_DATA_TRUNCATED = "Data truncated";

	
	public MySQL41Driver() {
		super();
	}
	
	public String getJdbcDriverName() {
		return _DRIVER_NAME;
	}

	public Version getMinSupportedProductVersion() {
		return new Version(4, 1);
	}

	public Version getMaxSupportedProductVersion() {
		return new Version(5, 5);
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
		return _NATIVE_DATETIME;
	}

	public String getNativeDate() {
		return _NATIVE_DATE;
	}

	public String getNativeTime() {
		return _NATIVE_TIME;
	}

	public DateFormat getDateTimeFormat() {
		return new SimpleDateFormat(_TIMESTAMP_FORMAT);
	}

	public DateFormat getDateFormat() {
		return new SimpleDateFormat(_DATE_FORMAT);
	}

	public DateFormat getTimeFormat() {
		return new SimpleDateFormat(_TIME_FORMAT);
	}
	
	public BLException translateException(SQLException sqle, String query, Argument[] args) {
		String msg = sqle.getMessage();
		if(msg.contains(_E_DB_UNAVAILABLE_01) || msg.contains(_E_DB_UNAVAILABLE_02)) {
			// This can also be a failed login attempt:
			Throwable cause = sqle.getCause();
			if(cause != null) {
				if(cause.getMessage().contains(_E_LOGIN_FAILED)) {
					return new LoginFailedException(sqle, query);
				}
			}
			return new DatabaseUnavailableException(sqle);
		}
		else if(msg.contains(_E_UNKNOWN_DATABASE)) {
			return new DatabaseNotFoundException(sqle);
		}
		else if(msg.contains(_E_LOGIN_FAILED)) {
			return new LoginFailedException(sqle, query);
		}
		else if((msg.contains(_E_MALFORMED_QUERY_01_01) && msg.contains(_E_MALFORMED_QUERY_01_02)) || msg.contains(_E_MALFORMED_QUERY_02) || msg.contains(_E_MALFORMED_QUERY_03)) {
			return new MalformedSQLException(sqle, query);
		}
		else if(msg.contains(_E_CONSTRAINT_UNIQUE)) {
			String[] array = msg.split("[']");
			String fieldValue = array[1];
			if(fieldValue.contains("-")) {
				// Get violated fields when composed key:
				array = fieldValue.split("[-]");
				List<Argument> argList = new LinkedList<Argument>();
				for(Argument arg : args) {
					argList.add(arg);
				}
				Argument[] violatingArgs = new Argument[array.length];
				boolean found = false;
				for(int i=0; i<violatingArgs.length; i++) {
					Iterator<Argument> it = argList.iterator();
					while(it.hasNext()) {
						Argument arg = it.next();
						if(array[i].equals(arg.getValue().toString())) {
							violatingArgs[i] = arg;
							found = true;
							it.remove();
							break;
						}
					}
				}
				if(found) {
					return new UniqueKeyConstraintException(sqle, query, violatingArgs);
				}
				else {
					return new UniqueKeyConstraintException(sqle, query);
				}
			}
			else {
				// Get violated field when regular key:
				for(Argument arg : args) {
					if(fieldValue.equals(arg.getValue())) {
						return new UniqueKeyConstraintException(sqle, query, new Argument[] {arg});
					}
				}
				return new UniqueKeyConstraintException(sqle, query);
			}
		}
		else if(msg.contains(_E_CONSTRAINT_FRGNK)) {
			return new ForeignKeyConstraintException(sqle, query);
		}
		else if(msg.contains(_E_LOCK_TIMEOUT)) {
			return new LockTimeoutException(sqle);
		}
		else if(msg.contains(_E_DATA_TRUNCATED)) {
			return new DataTruncatedException(sqle, query);
		}
		else {
			return new UnknownDatabaseException(sqle, query);
		}
	}
	
	// TODO we may want to change this to false.
	public boolean supportsPreparedStatements() {
		return true;
	}

	public boolean supportsLimitClause() {
		return true;
	}

	public String toString() {
		return _TO_STRING;
	}
}
