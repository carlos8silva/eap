package com.netx.eap.R1.core;
import java.io.PrintWriter;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import com.netx.generics.R1.time.Timestamp;
import com.netx.basic.R1.io.File;
import com.netx.basic.R1.io.BasicIOException;
import com.netx.basic.R1.io.ReadWriteException;


public interface EapResponse extends HttpServletResponse {

	// TYPE:
	public final static String STREAM_NAME = "HTTP response";
	
	// INSTANCE:
	public ServletContext getServletContext();
	public EapContext getEapContext();
	// Overriden methods:
	public ServletOutputStream getOutputStream() throws ReadWriteException;
	public PrintWriter getWriter() throws ReadWriteException;
	public void sendError(int code, String path) throws BasicIOException;
	public void sendRedirect(String location) throws BasicIOException;
	// Header setting methods:
	public void setContentType(String contentType);
	public void setLastModified(Timestamp timestamp);
	public void setLastModified(long timestamp);
	public void setDisableCache();
	public void setEnableCache();
	// Content sending methods:
	public void sendFile(File file) throws BasicIOException;
	public void sendBinaryFile(File file) throws BasicIOException;
	public void sendTextFile(File file) throws BasicIOException;
	// TODO reimplement
	public void sendRedirectPage(String location) throws IOException;
}
