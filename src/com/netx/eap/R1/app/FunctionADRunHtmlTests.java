package com.netx.eap.R1.app;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import com.netx.generics.R1.util.Tools;
import com.netx.basic.R1.io.File;
import com.netx.basic.R1.io.BasicIOException;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.core.Function;
import com.netx.eap.R1.core.EapRequest;
import com.netx.eap.R1.core.EapResponse;
import com.netx.eap.R1.core.MimeTypes;
import com.netx.eap.R1.core.NotAuthorizedException;
import com.netx.eap.R1.core.IllegalParameterException;
import com.netx.eap.R1.core.IllegalRequestException;
import com.netx.eap.R1.bl.User;


public class FunctionADRunHtmlTests extends Function {

	protected void doGet(EapRequest request, EapResponse response) throws IOException, BLException {
		final String doAction = request.getParameter("do");
		if(doAction == null) {
			File file = request.getFile("ad-run-html-tests-1.html");
			response.sendTextFile(file);
		}
		else if(doAction.equals("headers")) {
			// TODO use a template, or text/plain
			response.setContentType(MimeTypes.TEXT_HTML);
			response.setDisableCache();
			final PrintWriter out = response.getWriter();
			out.println("<html>");
			out.println("<head><link href=\"files/styles.css\" rel=\"stylesheet\" type=\"text/css\" /></head>");
			out.println(" <body class=\"fnt-text\">");
			out.print("    <b>REQUEST HEADERS:</b> <br><br>");
			Enumeration<?> names = request.getHeaderNames();
			while(names.hasMoreElements()) {
				String name = (String)names.nextElement();
				out.print("    <b>"+name+":</b>&nbsp; ");
				Enumeration<?> values = request.getHeaders(name);
				while(values.hasMoreElements()) {
					String value = (String)values.nextElement();
					out.print(value);
					if(values.hasMoreElements()) {
						out.print(" | ");
					}
				}
				out.println(" <br>");
			}
			out.println(" </body>");
			out.println("</html>");
			out.close();
		}
		else if(doAction.equals("paths")) {
			response.setDisableCache();
			response.setContentType(MimeTypes.TEXT_HTML);
			final PrintWriter out = response.getWriter();
			out.println("<html>");
			out.println("<head><link href=\"files/styles.css\" rel=\"stylesheet\" type=\"text/css\" /></head>");
			out.println("<body class=\"fnt-text\">");
			out.print("<b>getRequestURL:</b> "+request.getRequestURL()+"<br>");
			out.print("<b>getRequestURI:</b> "+request.getRequestURI()+"<br>");
			out.print("<b>getContextPath:</b> "+request.getContextPath()+"<br>");
			out.print("<b>getServletPath:</b> "+request.getServletPath()+"<br>");
			out.print("<b>getPathInfo:</b> "+request.getPathInfo()+"<br>");
			out.print("<b>getPathTranslated:</b> "+request.getPathTranslated()+"<br>");
			out.print("<b>getQueryString:</b> "+request.getQueryString()+"<br>");
			out.print("<b>getCompleteRequestURL(true):</b> "+request.getCompleteRequestURL(true)+"<br>");
			out.print("<b>getCompleteRequestURL(false):</b> "+request.getCompleteRequestURL(false)+"<br>");
			out.println("</body>");
			out.println("</html>");
			out.close();
		}
		else if(doAction.equals("input")) {
			File file = request.getFile("ad-run-html-tests-input.html");
			response.sendTextFile(file);
		}
		else if(doAction.equals("js")) {
			// TODO there is a defect with sending the JS file in case there is not enough padding at the end
			File file = request.getFile("ad-run-html-tests-js.html");
			response.sendTextFile(file);
		}
		else if(doAction.equals("form")) {
			File file = request.getFile("ad-run-html-tests-form.html");
			response.sendTextFile(file);
		}
		else if(doAction.equals("403")) {
			throw new NotAuthorizedException();
		}
		else if(doAction.equals("400")) {
			throw new IllegalRequestException("example-parameter");
		}
		else if(doAction.equals("500")) {
			throw new IOException("the application failed miserably");
		}
		else if(doAction.equals("error-validation")) {
			Connection c = request.getConnection();
			User u = request.getUserSession().getUser(c);
			u.setUsername("1abc");
		}
		else {
			throw new IllegalParameterException("do", doAction);
		}
	}
	
	protected void doPost(EapRequest request, EapResponse response) throws BasicIOException, BLException {
		final String action = request.getParameter("action", true);
		response.setContentType(MimeTypes.TEXT_PLAIN);
		response.setDisableCache();
		PrintWriter out = response.getWriter();
		if(action.equals("input")) {
			out.println("Checkbox values:");
			String[] values = request.getParameterValues("fruits");
			if(values == null) {
				out.println("no values");
			}
			else {
				for(String value : values) {
					out.println(value);
				}
			}
		}
		else if(action.equals("form")) {
			// Make the response wait so that the progress button remains visible:
			Tools.sleep(3000);
			// Print the number:
			out.println("Number: "+request.getParameter("number"));
		}
		else {
			throw new IllegalParameterException("action", action);
		}
		out.close();
	}
}
