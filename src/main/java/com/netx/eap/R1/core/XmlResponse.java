package com.netx.eap.R1.core;
import java.io.PrintWriter;
import java.io.IOException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import com.netx.basic.R1.io.Translator;
import com.netx.basic.R1.io.ReadWriteException;


public class XmlResponse {

	private final Element _root;
	
	public XmlResponse(String status) {
		Document document = DocumentHelper.createDocument();
		_root = document.addElement("eap-response");
		_root.addAttribute("version", "1.0");
		_root.addElement("status").addText(status);
	}
	
	public Element getRoot() {
		return _root;
	}

	public void render(EapResponse response) throws ReadWriteException {
		try {
			response.setContentType(MimeTypes.TEXT_XML);
			PrintWriter writer = response.getWriter();
			OutputFormat format = OutputFormat.createCompactFormat();
			XMLWriter xmlWriter = new XMLWriter(writer, format);
			xmlWriter.write(_root.getDocument());
			writer.close();
		}
		catch(IOException io) {
			throw Translator.translateIOE(io, EapResponse.STREAM_NAME);
		}
	}
}
