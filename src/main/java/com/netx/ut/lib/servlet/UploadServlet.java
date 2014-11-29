package com.netx.ut.lib.servlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
//import org.apache.commons.fileupload.*;


public class UploadServlet extends HttpServlet {

	public UploadServlet() {
		super();
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		PrintWriter writer = response.getWriter();
		writer.println("<html><body><form enctype=\"multipart/form-data\" method=\"post\">");
		writer.println("");
		writer.println("");
		writer.println("");
		writer.println("</form></body></html>");
		writer.close();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		PrintWriter writer = response.getWriter();
		writer.println("");
		writer.close();
	}
}
