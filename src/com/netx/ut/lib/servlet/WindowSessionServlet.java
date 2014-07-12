package com.netx.ut.lib.servlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
import com.netx.generics.R1.util.Tools;


// mapping: /wsession
// Servlet para testar o comportamento de cookies.
// Resultados: a cookie é conhecida em todas as janelas.
// Se for aberta uma nova janela do browser, a cookie é vista.
public class WindowSessionServlet extends HttpServlet {

	private int windowID;

	public WindowSessionServlet() {
		super();
		windowID = 0;
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
		Cookie client = getCookie("netx.client", request);
		if(client == null) {
			print(out, "No cookie set.");
		}
		else {
			Cookie window = getCookie("netx.window", request);
			if(window == null) {
				window = new Cookie("netx.window", ""+(++windowID));
				response.addCookie(window);
			}
			print(out, "<b>Client ID</b>: "+client.getValue()+"<br>");
			print(out, "<b>Window ID</b>: "+window.getValue()+"<br>");
			refreshClientCookie(client, response);
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
			Cookie client = new Cookie("netx.client", request.getRemoteAddr());
			refreshClientCookie(client, response);
			response.sendRedirect("wsession");
		}
		else if(button.equals("delete")) {
			deleteCookie(getCookie("netx.client", request), response);
			deleteCookie(getCookie("netx.window", request), response);
			response.sendRedirect("wsession");
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
	
	private void refreshClientCookie(Cookie cookie, HttpServletResponse response) {
		int minutes = 10;
		cookie.setMaxAge(60*minutes);
		response.addCookie(cookie);
	}

	private void deleteCookie(Cookie c, HttpServletResponse response) {
		if(c != null) {
			c.setMaxAge(0);
			response.addCookie(c);
		}
	}
}
