package com.netx.eap.R1.core;
import java.io.Reader;
import java.io.PrintWriter;
import java.util.List;
import com.netx.generics.R1.translation.BasicMessageFormatter;
import com.netx.generics.R1.translation.ErrorList;
import com.netx.generics.R1.translation.MessageFormatter;
import com.netx.generics.R1.translation.MessagePrinter;
import com.netx.generics.R1.util.CountingWriter;
import com.netx.generics.R1.util.HtmlWriter;
import com.netx.generics.R1.util.StackTraceReader;
import com.netx.generics.R1.util.Strings;
import com.netx.basic.R1.io.ExtendedReader;
import com.netx.basic.R1.io.BasicIOException;
import com.netx.basic.R1.io.ReadWriteException;
import com.netx.basic.R1.eh.ErrorHandler;
import com.netx.basic.R1.eh.ErrorListException;
import com.netx.bl.R1.core.BLException;
import com.netx.bl.R1.spi.DatabaseException;


class UI {
	
	private final static String _STREAM_NAME = "exception";
	
	public static void showNotInitializedError(EapResponse response) throws ReadWriteException {
		response.setContentType(MimeTypes.TEXT_HTML);
		response.setDisableCache();
		PrintWriter out = response.getWriter();
		HtmlErrorPrinter printer = new HtmlErrorPrinter(out);
		printer.printHtmlStart();
		printer.printInRed("APPLICATION NOT INITIALIZED");
		printer.println();
		printer.println();
		printer.println("The following errors were found during application initialization:");
		List<InitError> initErrors = response.getEapContext().getInitErrors();
		for(InitError ie : initErrors) {
			printer.printInRed("In servlet ["+ie.servletName+"]: ");
			if(ie.message != null) {
				printer.println(ie.message);
			}
			else {
				if(ie.t instanceof ErrorListException) {
					printer.println(ie.t.getMessage());
					ErrorList el = ((ErrorListException)ie.t).getErrorList();
					MessageFormatter mf = new BasicMessageFormatter(true, true);
					MessagePrinter p = new MessagePrinter(printer.getHtmlWriter());
					p.print(el.getAll(mf), 1);
				}
				else {
					printer.println("exception "+ie.t.getClass().getSimpleName()+" thrown");
					ie.t.printStackTrace(printer.getHtmlWriter());
				}
			}
		}
		printer.printHtmlEnd();
		out.close();
	}
	
	public static void showNotFoundError(EapRequest request, EapResponse response, String requestURI) throws BasicIOException, BLException {
		Template page = request.getTemplate("templates/all-fs-error-not-found.html");
		Values v = page.getValues();
		v.set("app-name", request.getApplicationName());
		v.set("url", requestURI);
		// TODO this logic does not work; see defect # 11
		boolean hasSession = request.getUserSession() != null;
		Values vIfSession = v.setIf("has-session", hasSession);
		if(!hasSession) {
			vIfSession.set("app-name", request.getApplicationName());
		}
		response.setEnableCache();
		page.render(MimeTypes.TEXT_HTML, response);
	}

	public static void showForbiddenError(EapRequest request, EapResponse response) throws BasicIOException, BLException {
		Template page = request.getTemplate("templates/all-fs-error-forbidden.html");
		Values v = page.getValues();
		v.set("app-name", request.getContextPath().substring(1));
		response.setDisableCache();
		page.render(MimeTypes.TEXT_HTML, response);
	}

	public static void showFault(EapRequest request, EapResponse response, String message, Throwable t) throws BasicIOException, BLException {
		Template page = request.getTemplate("templates/all-fs-error-fault.html");
		Values v = page.getValues();
		if(message == null) {
			message = _getExceptionMessage(t);
		}
		v.set("error-msg", message);
		v.set("app-name", request.getApplicationName());
		// TODO this logic does not work; see defect # 11
		boolean hasSession = request.getUserSession() != null;
		Values vIfSession = v.setIf("has-session", hasSession);
		if(!hasSession) {
			vIfSession.set("app-name", request.getApplicationName());
		}
		if(t == null) {
			v.setIf("throwable", false);
		}
		else {
			Values vTrace = v.setIf("throwable", true);
			ExtendedReader reader = new ExtendedReader(new StackTraceReader(t), _STREAM_NAME);
			vTrace.set("ex-header", reader.readLine());
			vTrace.set("stack-trace", reader);
		}
		response.setDisableCache();
		page.render(MimeTypes.TEXT_HTML, response);
	}

	public static void showValidationError(EapRequest request, EapResponse response, String message) throws BasicIOException, BLException {
		Template page = request.getTemplate("templates/all-fs-error-validation.html");
		Values v = page.getValues();
		v.set("error-msg", message);
		response.setDisableCache();
		page.render(MimeTypes.TEXT_HTML, response);
	}

	public static void showIllegalRequestError(EapRequest request, EapResponse response, SecurityCheckException sce) throws BasicIOException, BLException {
		Template page = request.getTemplate("templates/all-fs-error-illegal-request.html");
		Values v = page.getValues();
		v.set("app-name", request.getContextPath().substring(1));
		v.set("details", sce.getMessage());
		response.setDisableCache();
		page.render(MimeTypes.TEXT_HTML, response);
	}

	public static void showXmlError(EapResponse response, String message, String className) throws ReadWriteException {
		XmlResponse xml = new XmlResponse("ERROR");
		xml.getRoot().addElement("message").setText(message);
		xml.getRoot().addElement("exception").setText(className);
		response.setDisableCache();
		xml.render(response);
	}

	public static void showErrorStackTrace(EapResponse response, Reader stackTraceReader) throws BasicIOException {
		response.setContentType(MimeTypes.TEXT_HTML);
		response.setDisableCache();
		PrintWriter out = response.getWriter();
		HtmlErrorPrinter printer = new HtmlErrorPrinter(out);
		printer.printHtmlStart();
		ExtendedReader reader = new ExtendedReader(stackTraceReader, _STREAM_NAME);
		String line = reader.readLine();
		// Print the first exception line n red:
		printer.printInRed(line);
		printer.println();
		line = reader.readLine();
		while(line != null) {
			printer.println(line);
			line = reader.readLine();
		}
		printer.printHtmlEnd();
		reader.close();
		out.close();
	}

	public static void showLoginPage(EapRequest request, EapResponse response, String message, String username, String action, String style) throws BasicIOException, BLException {
		Template loginPage = request.getTemplate("templates/all-fs-login.html");
		Values v = loginPage.getValues();
		v.set("message", message);
		v.set("username", username);
		v.set("action", action);
		v.set("style", style);
		response.setDisableCache();
		loginPage.render(MimeTypes.TEXT_HTML, response);
	}
	
	private static String _getExceptionMessage(Throwable t) {
		StringBuilder message = new StringBuilder(ErrorHandler.getMessage(t));
		if(t instanceof DatabaseException) {
			DatabaseException dbe = (DatabaseException)t;
			String query = dbe.getQuery();
			if(query != null) {
				message.append("<br>");
				message.append("Query: ");
				message.append(query);
			}
		}
		return message.toString();
	}

	// TODO this may have to be removed after StackTraceReader in Template
	private static class HtmlErrorPrinter {

		private final CountingWriter _txtOut;
		private final HtmlWriter _htmlOut;
		
		public HtmlErrorPrinter(PrintWriter out) {
			_txtOut = new CountingWriter(out);
			_htmlOut = new HtmlWriter(out);
		}

		public HtmlWriter getHtmlWriter() {
			return _htmlOut;
		}

		public void printHtmlStart() {
			_txtOut.println("<html><head><title>ERROR</title></head>");
			_txtOut.println("<body bgcolor=\"#000000\"><font face=\"Verdana\" size=\"2\" color=\"#FFFFFF\">");
		}

		public void printHtmlEnd() {
			if(Config.SEND_STATUS_CODES) {
				// IE browser issue: if the application is configured to send HTTP error codes, the IE
				// setting "show friendly error pages" is set to true, and the response is < 342* chars  
				// in length, IE will display it's default error page instead of the server response. 
				// To prevent this, we force the response to have at least 342* chars.
				// *This has changed; now, we use 600 chars.
				long charsWritten = _txtOut.getWrittenCharCount() + _htmlOut.getWrittenCharCount();
				long minimumChars = 600;
				if(charsWritten < minimumChars) {
					_txtOut.println(Strings.repeat(" ", (int)(minimumChars - charsWritten)));
				}
			}
			_txtOut.println("</font></body></html>");
		}

		public void println(String line) {
			_htmlOut.println(line);
		}

		public void println() {
			_htmlOut.println();
		}

		public void printInRed(String line) {
			_txtOut.print("<font color=\"#FF0000\">");
			_htmlOut.print(line);
			_txtOut.println("</font>");
		}
	}
}
