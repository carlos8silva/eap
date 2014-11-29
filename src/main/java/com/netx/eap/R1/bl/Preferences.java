package com.netx.eap.R1.bl;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;

import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.bl.R1.core.*;
import com.netx.eap.R1.core.TableSettings;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;


public class Preferences extends Entity<PreferencesMetaData,Preference> {

	// TYPE:
	public static Preferences getInstance() {
		return EAP.getPreferences();
	}

	// INSTANCE:
	Preferences() {
		super(new PreferencesMetaData());
	}

	public void save(Connection c, Preference p) throws BLException {
		insertOrUpdate(c, p);
	}

	public void save(Connection c, String tableId, Long userId, TableSettings ts) throws BLException, IOException {
		Checker.checkEmpty(tableId, "tableId");
		Checker.checkNull(userId, "userId");
		Checker.checkNull(ts, "ts");
		Preference pTs = new Preference(userId, tableId);
		// TODO get a writer to the field instead (when BL functionality is implemented)
		StringWriter sw = new StringWriter();
		OutputFormat format = OutputFormat.createCompactFormat();
		XMLWriter xmlWriter = new XMLWriter(sw, format);
		xmlWriter.write(ts.toXML());
		sw.close();
		pTs.setValue(sw.toString());
		insertOrUpdate(c, pTs);
	}

	public TableSettings getTableSettingsFor(Connection c, Long userId, String tableId) throws BLException {
		try {
			Preference p = get(c, userId, tableId);
			if(p == null) {
				return null;
			}
	        SAXReader reader = new SAXReader();
	        Document doc = reader.read(new StringReader(p.getValue()));
			TableSettings ts = new TableSettings();
			ts.apply(doc);
			return ts;
		}
		catch(DocumentException de) {
			throw new IntegrityException(de);
		}
	}
}
