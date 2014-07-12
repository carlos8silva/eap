package com.netx.eap.R1.core;
import javax.servlet.ServletConfig;
import org.dom4j.Element;
import com.netx.basic.R1.io.ReadWriteException;


public class SrvApplicationInfo extends EapServlet {

	public SrvApplicationInfo() {
		super(false);
	}

	public void init(ServletConfig config) {
		super.init(config);
	}

	public void doGet(EapRequest request, EapResponse response) throws ReadWriteException {
		// Create XML document:
		XmlResponse xml = new XmlResponse("OK");
		xml.getRoot().addElement("application-name").setText(Config.APP_NAME);
		// TODO L10n
		String[] weekDays = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
		Element elemWeekDays = xml.getRoot().addElement("week-days");
		for(String day : weekDays) {
			elemWeekDays.addElement("day").setText(day);
		}
		// Write response:
		response.setEnableCache();
		xml.render(response);
	}

	public void doPost(EapRequest request, EapResponse response) {
		throw new UnsupportedOperationException();
	}
}
