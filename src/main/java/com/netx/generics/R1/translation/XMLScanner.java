package com.netx.generics.R1.translation;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.IOException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;


public abstract class XMLScanner extends FirstStep {

	protected XMLScanner() {
		super();
	}
	
	public Object performWork(Object o, ErrorList el) {
		try {
			Reader reader = (Reader)o;
			if(!(reader instanceof BufferedReader)) {
				reader = new BufferedReader(reader);
			}
			Document config = new SAXReader().read(reader);
			reader.close();
			return config;
		}
		catch(DocumentException de) {
			el.addError(de.getNestedException().getMessage());
			return null;
		}
		catch(IOException io) {
			el.addError(io.getMessage());
			return null;
		}
	}
}
