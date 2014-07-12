package com.netx.ut.lib.servlet;
import javax.servlet.http.*;
import javax.servlet.*;
import com.netx.generics.R1.util.Tools;
import java.io.*;


// mapping: /cookie
// Servlet para testar o comportamento de cookies.
// Resultados: a cookie é conhecida em todas as janelas.
// Se for aberta uma nova janela do browser, a cookie é vista.
public class CookieServlet extends HttpServlet {

	private int index;
	private final String[] values;

	public CookieServlet() {
		super();
		index = 0;
		values = new String[] {
			"one", "two", "three", "four", "five", "six", "seven", "eight"
		};
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
		Cookie cookie = getCookie("carlos-cookie-test", request);
		if(cookie == null) {
			print(out, "No cookie set.");
		}
		else {
			print(out, "<b>Cookie ID</b>: "+cookie.getName()+"<br>");
			print(out, "<b>Value</b>: "+cookie.getValue()+"<br>");
			refreshCookie(cookie, response);
		}
		out.println("  <br>");
		out.println("  <form method=\"post\">");
		out.println("   <input type=\"submit\" value=\"create\" name=\"button\"><br><br>");
		out.println("   <input type=\"submit\" value=\"delete\" name=\"button\"><br><br>");
		out.println("   <input type=\"submit\" value=\"no-pass\" name=\"button\"><br>");
		out.println("  </form>");
		out.println(" </body>");
		out.println("</html>");
		out.close();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String button = request.getParameter("button");
		if(button.equals("create")) {
			Cookie cookie = new Cookie("carlos-cookie-test", "");
			refreshCookie(cookie, response);
			response.sendRedirect("cookie");
		}
		else if(button.equals("no-pass")) {
			response.sendRedirect("cookie");
		}
		else if(button.equals("delete")) {
			Cookie cookie = getCookie("carlos-cookie-test", request);
			if(cookie != null) {
				cookie.setMaxAge(0);
				response.addCookie(cookie);
			}
			response.sendRedirect("cookie");
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

	private Cookie getCookie(String name, HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
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
	
	private void refreshCookie(Cookie cookie, HttpServletResponse response) {
		cookie.setValue(values[index++]);
		if(index == 8) {
			index = 0;
		}
		int minutes = 10;
		cookie.setMaxAge(60*minutes);
		response.addCookie(cookie);
	}

}
