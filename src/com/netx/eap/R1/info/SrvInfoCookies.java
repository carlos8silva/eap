package com.netx.eap.R1.info;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

import com.netx.generics.R1.util.Tools;

import java.io.*;

// Results:
// Cookie behaviour is now consistent across IE8.0, Chrome and Firefox.
// Cookies set with an expiry date are known across windows (even if a new instance of the
// browser is started). They remain known after all instances of the browser are closed.
// Cookies set with a negative expiry date are known across windows as well. However,
// if all browser windows are closed the cookie disappears.
public class SrvInfoCookies extends HttpServlet {

	// TYPE:
	private static final String _SRV_URL = "cookies";
	private static final String _CLIENT_ID = "client-id";
	private static final String _WINDOW_ID = "window-id";
	
	// INSTANCE:
	public SrvInfoCookies() {
		super();
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html; charset=UTF-8");
		Tools.sendDisableCacheHeaders(response);
		
		// Print current cookies:
		final PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println(" <body>");
		out.println("    <b>CURRENT COOKIES: </b><br>");
		Cookie[] cookies = request.getCookies();
		if(cookies == null) {
			out.println("    No cookies set.<br>");
		}
		else {
			for(Cookie c : cookies) {
				out.println(c.getName()+" = '"+c.getValue()+"'<br>");
			}
		}
		out.println("<br><br>");
		
		// Show cookie setting form:
		out.println("<form id=\"form1\" name=\"form1\" method=\"post\">");
		out.println("<table width=\"480\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
		
		// Client ID:
		out.println("<tr><td width=\"81\" height=\"30\">Client ID: </td>");
		out.println("<td width=\"154\">");
		Cookie clientID = getCookie(_CLIENT_ID, request);
		if(clientID == null) {
			out.println("<input type=\"text\" name=\"client_id\" />");
		}
		else {
			out.println("<input type=\"text\" name=\"client_id\" value=\""+clientID.getValue()+"\"/>");
		}
		out.println("</td>");
		out.println("<td width=\"109\"><input name=\"button\" type=\"submit\" id=\"set_client_id\" value=\"set client ID\" /></td>");
		out.println("<td width=\"136\"><input name=\"button\" type=\"submit\" id=\"delete_client_id\" value=\"delete client ID\" /></td>");
		out.println("</tr>");

		// Window ID:
		out.println("<tr><td height=\"30\">Window ID: </td>");
		out.println("<td>");
		Cookie windowID = getCookie(_WINDOW_ID, request);
		if(windowID == null) {
			out.println("<input type=\"text\" name=\"window_id\" />");
		}
		else {
			out.println("<input type=\"text\" name=\"window_id\" value=\""+windowID.getValue()+"\"/>");
		}
		out.println("</td>");
		out.println("<td><input name=\"button\" type=\"submit\" id=\"set_window_id\" value=\"set window ID\" /></td>");
		out.println("<td><input name=\"button\" type=\"submit\" id=\"delete_window_id\" value=\"delete window ID\" /></td>");
		out.println("</tr>");
		
		out.println("</table></form>");
		out.println("</body></html>");
		out.close();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String button = request.getParameter("button");
		if(button.equals("set client ID")) {
			Cookie cookie = new Cookie(_CLIENT_ID, request.getParameter("client_id"));
			int minutes = 60;
			cookie.setMaxAge(60*minutes);
			response.addCookie(cookie);
			response.sendRedirect(_SRV_URL+"?action=set-client-id");
		}
		else if(button.equals("set window ID")) {
			Cookie cookie = new Cookie(_WINDOW_ID, request.getParameter("window_id"));
			cookie.setMaxAge(-1);
			response.addCookie(cookie);
			response.sendRedirect(_SRV_URL+"?action=set-window-id");
		}
		else if(button.equals("delete client ID")) {
			Cookie cookie = getCookie(_CLIENT_ID, request);
			if(cookie != null) {
				cookie.setMaxAge(0);
				response.addCookie(cookie);
			}
			response.sendRedirect(_SRV_URL+"?action=delete-client-id");
		}
		else if(button.equals("delete window ID")) {
			Cookie cookie = getCookie(_WINDOW_ID, request);
			if(cookie != null) {
				cookie.setMaxAge(0);
				response.addCookie(cookie);
			}
			response.sendRedirect(_SRV_URL+"?action=delete-window-id");
		}
		else {
			throw new RuntimeException("unknown action: "+button);
		}
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
}
