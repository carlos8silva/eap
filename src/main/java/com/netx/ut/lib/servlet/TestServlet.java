package com.netx.ut.lib.servlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//import java.util.Timer;
//import java.util.TimerTask;


// TODO create a main page and add a build file to deploy these servlets
// mapping: /test
// Servlet for multiple javax.servlet package tests.
// Use parameter cmd=<method-name> to choose between methods.
public class TestServlet extends HttpServlet {

	public TestServlet() {
		super();
	}
	
	private class ResponseWriter extends PrintWriter {

		public ResponseWriter(PrintWriter p) {
			super(p);
		}

		public void println(String msg) {
			super.println("<font face=\"Verdana\" size=\"2\">");
			super.println(msg+"<br>");
			super.println("</font>");
		}
	}
	
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String method = request.getParameter("method");
		response.setContentType("text/html");
		ResponseWriter out = new ResponseWriter(response.getWriter());
		if(method == null) {
			out.println("No method specified.");
		}
		else {
			Method[] methods = getClass().getDeclaredMethods();
			boolean found = false;
			for(int i=0; i < methods.length; i++) {
				if(methods[i].getName().equals(method)) {
					found = true;
					try {
						methods[i].invoke(this, new Object[]{request, response});
					}
					catch(Throwable t) {
						out.println("Exception executing method \""+method+"\"");
						t.printStackTrace(out);
					}
				}
			}
			if(!found) {
				out.println("\""+method+"\": no such method.");
			}
		}
		out.close();
	}

	/*
	private void testPaths(HttpServletRequest request, HttpServletResponse response) throws Throwable {
		ResponseWriter out = new ResponseWriter(response.getWriter());

		out.println("<b>getPathInfo():</b> "+request.getPathInfo());
		out.println("<b>getContextPath():</b> "+request.getContextPath());
		out.println("<b>getRequestURI():</b> "+request.getRequestURI());
		out.println("<b>getRequestURL():</b> "+request.getRequestURL());
		out.println("<b>getServletPath():</b> "+request.getServletPath());

		out.println("<b>getServerName():</b> "+request.getServerName());
	}

	//! organize this
	boolean mark = false;
	private void doTimer(HttpServletRequest req, PrintWriter out) {
		int seconds = 3;
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				mark = true;
				timer.cancel();
			}
		}, seconds*1000);
		
		while(true) {
			if(mark) {
				break;
			}
		}
		mark = false;
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
		"<servico><ref>0018</ref><nome>Flóres lindinhas</nome><cliente>Desicolor</cliente></servico>");
	}
	*/
}
