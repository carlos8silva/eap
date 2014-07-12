package com.netx.ut.lib.servlet;
import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.List;


// This class is used to read HTTP responses.
// To see raw data provided by an HTTP server, use the readSocket() method.
public class ReadServlet {

	public static void main(String[] args) throws Exception {
		ReadServlet rs = new ReadServlet();
		rs.readSocket();
		//rs.readSocketForOptions();
		//rs.readURL();
	}

	public void readSocket() throws Exception {
		Socket s = new Socket("localhost", 8080);
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
		BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
		//writer.println("GET /cubigraf3/login HTTP/1.1");
		//writer.println("GET /cubigraf3/icon-warn.png HTTP/1.1");
		//writer.println("GET /cubigraf3/files/templates/all-frm-table.html HTTP/1.1");
		//writer.println("GET /cubigraf3/um-edit-user.x HTTP/1.1");
		//writer.println("GET /cubigraf3/generics.R1.js HTTP/1.1");
		//writer.println("GET /cubigraf3/files/buttons-normal.png HTTP/1.1");
		//writer.println("GET /cubigraf3/files/external/jquery.mousewheel.js HTTP/1.1");
		writer.println("GET /cubigraf3/files/start-menu.swf HTTP/1.1");
		writer.println("Host: localhost:8080");
		writer.println("Connection: Close");
		writer.println();
		int bytesRead = -1;
		boolean messageBody = false;
		String line = reader.readLine();
		while(line != null) {
			System.out.println(line);
			if(messageBody) {
				bytesRead += line.length();
				bytesRead += 1;
			}
			if(line.equals("")) {
				messageBody = true;
			}
			line = reader.readLine();
		}
		s.close();
		writer.close();
		reader.close();
		System.out.println("[bytes read: "+bytesRead+"]");
	}

	public void readSocketForOptions() throws Exception {
		Socket s = new Socket("localhost", 8080);
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
		BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
		writer.println("OPTIONS /cubigraf3/login HTTP/1.1");
		writer.println("Host: localhost:8080");
		writer.println("Connection: Close");
		writer.println();
		String line = reader.readLine();
		while(line != null) {
			System.out.println(line);
			line = reader.readLine();
		}
		s.close();
		writer.close();
		reader.close();
	}

	public void readURL() throws Exception {
		URL url = new URL("http://www.google.com");
		//URL url = new URL("http://localhost:8080/cubigraf3/icon-warn.png");
		URLConnection c = url.openConnection();
		Map<String,List<String>> headers = c.getHeaderFields();
		for(String s : headers.keySet()) {
			System.out.println(s+": "+headers.get(s));
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		String line = reader.readLine();
		while(line != null) {
			System.out.println(line);
			line = reader.readLine();
		}
		reader.close();
	}
}
