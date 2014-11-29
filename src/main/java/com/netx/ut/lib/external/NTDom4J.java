package com.netx.ut.lib.external;
import java.util.Iterator;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import com.netx.generics.R1.util.UnitTester;
import com.netx.basic.R1.io.FileSystemException;


public class NTDom4J extends UnitTester {

	public static void main(String[] args) throws Throwable {
		NTDom4J nt = new NTDom4J();
		nt.t01_ReadDocument();
		nt.t02_ValidateErrorWithDTD();
		nt.t03_ValidateErrorWithSchema1();
		/*
		nt.t04_ValidateErrorWithSchema2();
		nt.t05_ValidateWithDTD();
		nt.t06_ValidateWithSchema();
		*/
		nt.println("done.");
	}
	
	public void t01_ReadDocument() throws FileSystemException {
		try {
	        SAXReader reader = new SAXReader();
	        Document document = reader.read(_getPath("aDocument.xml"));
	        Element root = document.getRootElement();
	        for(Iterator<?> i = root.elementIterator(); i.hasNext(); ) {
	            Element element = (Element)i.next();
	            println(element.getName());
	        }
	    }
		catch(DocumentException ex) {
			org.xml.sax.SAXParseException t = (org.xml.sax.SAXParseException)ex.getNestedException();
			println(t.getMessage());
			println(t.getLineNumber());
			println(t.getColumnNumber());
			println(t.getPublicId());
			println(t.getSystemId());
		}
	}

	// TODO running this test requires an XML file with a DTD
	public void t02_ValidateErrorWithDTD() throws Exception {
		SAXReader reader = new SAXReader();
		reader.setValidation(true);
		reader.setErrorHandler(new SimpleErrorHandler());
		reader.read("res/xml/contacts1.xml");
	}

	// Note: running this method requires pointing to the Xerces JAR files
	// using VM option -Djava.endorsed.dirs=<path>
	public void t03_ValidateErrorWithSchema1() throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		SAXParser parser = factory.newSAXParser();
		parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
		SAXReader reader = new SAXReader(parser.getXMLReader());
		reader.setValidation(true);
		reader.setErrorHandler(new SimpleErrorHandler());
		reader.read("res/xml/contacts1.xml");
	}

	public void t04_ValidateErrorWithSchema2() throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		Schema schema = schemaFactory.newSchema(new Source[] {new StreamSource("res/xml/contacts1.xsd")});
		factory.setSchema(schema);
		SAXParser parser = factory.newSAXParser();
		SAXReader reader = new SAXReader(parser.getXMLReader());
		reader.setValidation(false);
		reader.setErrorHandler(new SimpleErrorHandler());
		reader.read("res/xml/contacts1.xml");
	}

	public void t05_ValidateWithDTD() throws Exception {
		SAXReader reader = new SAXReader();
		reader.setValidation(true);
		reader.setErrorHandler(new SimpleErrorHandler());
		reader.read("res/xml/contacts2.xml");
	}

	public void t06_ValidateWithSchema() throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		SAXParser parser = factory.newSAXParser();
		parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
		SAXReader reader = new SAXReader(parser.getXMLReader());
		reader.setValidation(true);
		reader.setErrorHandler(new SimpleErrorHandler());
		reader.read("res/xml/contacts2.xml");
	}
	
	private String _getPath(String filename) throws FileSystemException {
		return getTestResourceLocation().getDirectory("lib.external").getAbsolutePath()+"/"+filename;
	}
}
