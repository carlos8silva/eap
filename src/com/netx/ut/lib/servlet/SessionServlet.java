package com.netx.ut.lib.servlet;
import javax.servlet.http.*;
import javax.servlet.*;

import com.netx.generics.R1.util.Tools;

import java.io.*;
import java.util.*;


// mapping: /session
// Servlet para testar o comportamento de HttpSession.
// Resultados: a sessão só é transmitida de janela em janela.
// Se for aberta uma nova janela do browser, a sessão não é mantida.
public class SessionServlet extends HttpServlet {

	public SessionServlet() {
		super();
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html; charset=UTF-8");
		Tools.sendDisableCacheHeaders(response);
		final PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println(" <body>");
		HttpSession session = request.getSession(false);
		if(session == null) {
			print(out, "No session set.");
		}
		else {
			print(out, "<b>Session ID</b>: "+session.getId()+"<br>");
			print(out, "<b>Creation time</b>: "+session.getCreationTime()+"<br>");
			print(out, "<b>Attributes</b>:<br>");
			Enumeration<?> attrs = session.getAttributeNames();
			while(attrs.hasMoreElements()) {
				String next = attrs.nextElement().toString();
				print(out, " <b>"+next+"</b>: "+session.getAttribute(next)+"<br>");
			}
		}
		out.println("  <br>");
		out.println("  <form method=\"post\">");
		out.println("   <input type=\"submit\" value=\"create\" name=\"button\"><br><br>");
		out.println("   <input type=\"submit\" value=\"delete\" name=\"button\"><br>");
		out.println("  </form>");
		out.println(" </body>");
		out.println("</html>");
		out.close();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String button = request.getParameter("button");
		if(button.equals("create")) {
			request.getSession();
			response.sendRedirect("session");
		}
		else if(button.equals("delete")) {
			HttpSession session = request.getSession(false);
			if(session != null) {
				session.invalidate();
			}
			response.sendRedirect("session");
		}
		else {
			throw new RuntimeException("unknown action: "+button);
		}
	}

	private void print(PrintWriter out, String s) {
		out.print("<font face=\"Verdana\" size=\"2\">");
		out.print(s);
		out.print("</font>");
	}
}
