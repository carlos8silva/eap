package com.netx.eap.R1.core;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import com.netx.basic.R1.io.File;
import com.netx.basic.R1.io.BasicIOException;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.BLException;


public interface EapRequest extends HttpServletRequest {

	public static enum METHOD {GET, POST, DELETE, PUT, TRACE, OPTIONS};
	
	public ServletContext getServletContext();
	public EapContext getEapContext();
	public Connection getConnection() throws BLException;
	public UserSession getUserSession();
	public Cookie getCookie(String name);

	public String		getParameter(String name);
	public String		getParameter(String name, boolean required);
	public Byte			getByteParameter(String name);
	public Byte			getByteParameter(String name, boolean required);
	public Short		getShortParameter(String name);
	public Short		getShortParameter(String name, boolean required);
	public Integer		getIntParameter(String name);
	public Integer		getIntParameter(String name, boolean required);
	public Long			getLongParameter(String name);
	public Long			getLongParameter(String name, boolean required);
	public Float		getFloatParameter(String name);
	public Float		getFloatParameter(String name, boolean required);
	public Double		getDoubleParameter(String name);
	public Double		getDoubleParameter(String name, boolean required);
	public Boolean		getBooleanParameter(String name, boolean required);
	public Boolean		getBooleanParameter(String name);
	
	public String getRequestPath();
	public String getApplicationName();
	public String getQueryString(boolean decode);
	public String getCompleteRequestURL();
	public String getCompleteRequestURL(boolean decode);
	public File getFile(String path) throws BasicIOException, BLException;
	public Template getTemplate(String path) throws BasicIOException, BLException;
	public Template getTemplate(String path, Map<String, Template> snippets) throws BasicIOException, BLException;

	public boolean getXml();
	public void setXml(boolean xml);
}
