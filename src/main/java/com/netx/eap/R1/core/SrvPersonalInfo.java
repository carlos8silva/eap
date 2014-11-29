package com.netx.eap.R1.core;
import java.io.IOException;
import java.util.List;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.bl.Users;
import com.netx.eap.R1.bl.User;
import com.netx.eap.R1.bl.Menus;
import com.netx.eap.R1.bl.MenuItem;
import com.netx.eap.R1.bl.UserMenu;
import org.dom4j.Element;


public class SrvPersonalInfo extends EapServlet {
	
	public SrvPersonalInfo() {
		super(true);
	}

	protected void doGet(EapRequest request, EapResponse response) throws IOException, BLException {
		request.setXml(true);
		final String doAction = request.getParameter("do", true);
		XmlResponse xml = null;
		if(doAction.equals("get-top-info")) {
			Connection c = request.getConnection();
			User user = request.getUserSession().getUser(c);
			c.close();
			xml = new XmlResponse("OK");
			xml.getRoot().addElement("screen-name").setText(user.getFullName());
			xml.getRoot().addElement("help-on").setText(user.getHelpOn().toString());
		}
		else if(doAction.equals("get-menu")) {
			xml = new XmlResponse("OK");
			Element xmlMenu = null;
			Connection c = request.getConnection();
			List<UserMenu> menus = Menus.getInstance().listMenuFor(c, request.getUserSession());
			c.close();
			for(UserMenu menu : menus) {
				xmlMenu = xml.getRoot().addElement("menu");
				// TODO upper case must be managed by Flash as it is presentation
				xmlMenu.addAttribute("name", menu.menu.getName().toUpperCase());
				for(MenuItem item : menu.menuItems) {
					Element xmlButton = xmlMenu.addElement("button");
					xmlButton.addAttribute("name", item.getName());
					String link = item.getFunctionId() + Constants.URL_FUNCTION_SUFFIX;
					if(item.getFunctionArgs() != null) {
						link = link + '?' + item.getFunctionArgs();
					}
					xmlButton.addAttribute("link", link);
				}
			}
		}
		else if(doAction.equals("validate-password")) {
			// TODO this is not used anywhere now
			final String password = request.getParameter("password", true);
			Connection c = request.getConnection();
			User user = request.getUserSession().getUser(c);
			c.close();
			if(!user.getPassword().equals(password)) {
				xml = new XmlResponse("ERROR");
				xml.getRoot().addElement("message").setText("Password incorrect, please try again");
			}
			else {
				xml = new XmlResponse("OK");
			}
		}
		else if(doAction.equals("disable-help")) {
			_updateHelp(request, false);
			xml = new XmlResponse("OK");
		}
		else if(doAction.equals("enable-help")) {
			_updateHelp(request, true);
			xml = new XmlResponse("OK");
		}
		else {
			xml = new XmlResponse("ERROR");
			xml.getRoot().addElement("message").setText("illegal value for 'do' parameter: "+doAction);
		}
		response.setDisableCache();
		xml.render(response);
	}
	
	private void _updateHelp(EapRequest request, boolean value) throws BLException {
		Connection c = request.getConnection();
		User user = request.getUserSession().getUser(c);
		user.setHelpOn(value);
		Users.getInstance().save(c, user, null, null);
		c.close();
	}
}
