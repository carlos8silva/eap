package com.netx.config.R1;
import java.util.Iterator;

import com.netx.basic.R1.shared.Constants;
import com.netx.generics.R1.translation.TranslationStep;
import com.netx.generics.R1.translation.ErrorList;
import org.dom4j.Document;
import org.dom4j.Element;


//TODO error messages must come from i18n
class ConfigParser extends TranslationStep {

	public ConfigParser(ConfigScanner scanner) {
		super(scanner);
	}

	public Object performWork(Object o, ErrorList el) {
		SymbolRoot sRoot = new SymbolRoot();
		Element root = ((Document)o).getRootElement();
		sRoot.pVersion = root.attributeValue("version");
		Iterator<?> it = root.elements().iterator();
		while(it.hasNext()) {
			SymbolContext sCtx = _readContext((Element)it.next(), el);
			if(sCtx != null) {
				sRoot.pSubContexts.add(sCtx);
			}
		}
		return sRoot;
	}
	
	private SymbolContext _readContext(Element e, ErrorList el) {
		SymbolContext sCtx = new SymbolContext();
		sCtx.pName = e.getName();
		Iterator<?> it = e.elements().iterator();
		while(it.hasNext()) {
			Element elem = (Element)it.next();
			if(elem.getName().equals("property")) {
				SymbolProperty sProp = _readProperty(elem, el);
				if(sProp != null) {
					sCtx.pProperties.add(sProp);
				}
			}
			else {
				SymbolContext sSubCtx = _readContext(elem, el);
				if(sSubCtx != null) {
					sCtx.pSubContexts.add(sSubCtx);
				}
			}
		}
		return sCtx;
	}
	
	private SymbolProperty _readProperty(Element e, ErrorList el) {
		SymbolProperty sProp = new SymbolProperty();
		sProp.pName = e.attributeValue("name");
		// Simple type:
		String s = _getText(e);
		if(s != null) {
			sProp.pValue = s;
		}
		// Complex type:
		else {
			Iterator<?> it = e.elementIterator();
			while(it.hasNext()) {
				Element elem = (Element)it.next();
				SymbolProperty sArg = new SymbolProperty();
				sArg.pName = elem.getName();
				// TODO make recursive
				sArg.pValue = _getText(elem);
				sProp.pArguments.add(sArg);
			}
		}
		return sProp;
	}
	
	private String _getText(Element e) {
		String text = e.getText().trim();
		if(text.equals(Constants.EMPTY)) {
			return null;
		}
		else {
			return text;
		}
	}
}
