package com.netx.eap.R1.core;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import com.netx.generics.R1.util.Strings;
import com.netx.basic.R1.io.File;
import com.netx.basic.R1.io.BasicIOException;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.bl.R1.core.Repository;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.BLException;


// TODO implement finalize method that closes all connections
class EapRequestImpl extends HttpServletRequestWrapper implements EapRequest {

	private final ServletContext _srvCtx;
	private final List<Connection> _cList;

	EapRequestImpl(HttpServletRequest request, ServletContext srvCtx) {
		super(request);
		_srvCtx = srvCtx;
		_cList = new ArrayList<Connection>();
	}

	public ServletContext getServletContext() {
		return _srvCtx;
	}

	public EapContext getEapContext() {
		return (EapContext)getServletContext().getAttribute(Constants.SRVCTX_EAP_CTX);
	}
	
	public Connection getConnection() throws BLException {
		Repository rep = getEapContext().getRepository();
		if(rep == null) {
			// This can happen if there was a problem initializing the database:
			throw new IllegalStateException("database not initialized");
		}
		Connection c = rep.getConnection();
		_cList.add(c);
		return c;
	}

	// For EapServlet:
	void closeConnections() {
		for(Connection c : _cList) {
			try {
				c.close();
			}
			catch(Throwable t) {
				// Non-recoverable:
				Config.LOGGER.error(t);
			}
		}
		_cList.clear();
	}
	
	public UserSession getUserSession() {
		return (UserSession)getAttribute(Constants.RATTR_USER_SESSION);
	}

	public Cookie getCookie(String name) {
		Checker.checkEmpty(name, "name");
		Cookie[] cookies = getCookies();
		if(cookies == null) {
			return null;
		}
		for(int i=0; i<cookies.length; i++) {
			if(cookies[i].getName().equals(name)) {
				return cookies[i];
			}
		}
		return null;
	}

	public String getRequestPath() {
		return getRequestURI().substring(getContextPath().length()+1);
	}

	public String getApplicationName() {
		return getContextPath().substring(1);
	}

	public String getQueryString(boolean decode) {
		if(decode) {
			String qs = super.getQueryString();
			if(qs == null) {
				return null;
			}
			else {
				try {
					return URLDecoder.decode(qs, Config.CHARSET);
				}
				catch(UnsupportedEncodingException uee) {
					throw new IntegrityException(uee);
				}
			}
		}
		else {
			return super.getQueryString();
		}
	}

	public String getCompleteRequestURL(boolean decode) {
		String query = getQueryString(decode);
		return getRequestURL().append(query==null?"":"?"+query).toString();
	}

	public String getCompleteRequestURL() {
		return getCompleteRequestURL(false);
	}

	public String getParameter(String name) {
		Checker.checkEmpty(name, "name");
		String param = super.getParameter(name);
		return Strings.isEmpty(param) ? null : param.trim();
	}

	public String getParameter(String name, boolean required) {
		String param = getParameter(name);
		if(required && param == null) {
			throw new IllegalRequestException(name);
		}
		else {
			return param;
		}
	}

	public Byte getByteParameter(String name, boolean required) {
		String param = getParameter(name, required);
		if(param == null) {
			return null;
		}
		else {
			try {
				return new Byte(param);
			}
			catch(NumberFormatException nfe) {
				throw new IllegalParameterException(name, param);
			}
		}
	}

	public Byte getByteParameter(String name) {
		return getByteParameter(name, false);
	}

	public Short getShortParameter(String name, boolean required) {
		String param = getParameter(name, required);
		if(param == null) {
			return null;
		}
		else {
			try {
				return new Short(param);
			}
			catch(NumberFormatException nfe) {
				throw new IllegalParameterException(name, param);
			}
		}
	}

	public Short getShortParameter(String name) {
		return getShortParameter(name, false);
	}

	public Integer getIntParameter(String name, boolean required) {
		String param = getParameter(name, required);
		if(param == null) {
			return null;
		}
		else {
			try {
				return new Integer(param);
			}
			catch(NumberFormatException nfe) {
				throw new IllegalParameterException(name, param);
			}
		}
	}

	public Integer getIntParameter(String name) {
		return getIntParameter(name, false);
	}

	public Long getLongParameter(String name, boolean required) {
		String param = getParameter(name, required);
		if(param == null) {
			return null;
		}
		else {
			try {
				return new Long(param);
			}
			catch(NumberFormatException nfe) {
				throw new IllegalParameterException(name, param);
			}
		}
	}

	public Long getLongParameter(String name) {
		return getLongParameter(name, false);
	}

	public Float getFloatParameter(String name, boolean required) {
		String param = getParameter(name, required);
		if(param == null) {
			return null;
		}
		else {
			try {
				return new Float(param);
			}
			catch(NumberFormatException nfe) {
				throw new IllegalParameterException(name, param);
			}
		}
	}

	public Float getFloatParameter(String name) {
		return getFloatParameter(name, false);
	}

	public Double getDoubleParameter(String name, boolean required) {
		String param = getParameter(name, required);
		if(param == null) {
			return null;
		}
		else {
			try {
				return new Double(param);
			}
			catch(NumberFormatException nfe) {
				throw new IllegalParameterException(name, param);
			}
		}
	}

	public Double getDoubleParameter(String name) {
		return getDoubleParameter(name, false);
	}

	public Boolean getBooleanParameter(String name, boolean required) {
		String param = getParameter(name, required);
		if(param == null) {
			return null;
		}
		else {
			if(param.equalsIgnoreCase("true")) {
				return Boolean.TRUE;
			}
			else if(param.equalsIgnoreCase("false")) {
				return Boolean.FALSE;
			}
			else {
				throw new IllegalParameterException(name, param);
			}
		}
	}
	
	public Boolean getBooleanParameter(String name) {
		return getBooleanParameter(name, false);
	}

	public File getFile(String path) throws BasicIOException, BLException {
		return getEapContext().getFile(this, path);
	}

	public Template getTemplate(String path) throws BasicIOException, BLException {
		return getEapContext().getTemplate(this, path);
	}

	public Template getTemplate(String path, Map<String, Template> snippets) throws BasicIOException, BLException {
		return getEapContext().getTemplate(this, path, snippets);
	}

	public boolean getXml() {
		Boolean xmlMode = (Boolean)getAttribute(Constants.RATTR_XML_MODE);
		if(xmlMode == null) {
			return false;
		}
		else {
			return xmlMode;
		}
	}

	public void setXml(boolean xml) {
		setAttribute(Constants.RATTR_XML_MODE, new Boolean(xml));
	}
}
