package com.netx.bl.R1.spi;
import java.sql.SQLException;
import java.text.DateFormat;
import com.netx.bl.R1.core.Argument;
import com.netx.bl.R1.core.BLException;
import com.netx.generics.R1.util.Version;


public interface DatabaseDriver {

	public String getJdbcDriverName();
	public Version getMinSupportedProductVersion();
	public Version getMaxSupportedProductVersion();
	public String getNativeByte();
	public String getNativeShort();
	public String getNativeInt();
	public String getNativeLong();
	public String getNativeFloat();
	public String getNativeDouble();
	public String getNativeText(int length);
	public String getNativeBinary(int length);
	public String getNativeDateTime();
	public String getNativeDate();
	public String getNativeTime();
	public DateFormat getDateTimeFormat();
	public DateFormat getDateFormat();
	public DateFormat getTimeFormat();
	public BLException translateException(SQLException sqle, String query, Argument[] args);
	public boolean supportsPreparedStatements();
	public boolean supportsLimitClause();
}
