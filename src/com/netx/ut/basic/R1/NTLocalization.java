package com.netx.ut.basic.R1;
import java.io.IOException;
import java.io.InputStream;
import com.netx.generics.R1.util.UnitTester;
import com.netx.generics.R1.util.Tools;
import com.netx.basic.R1.shared.Globals;
import com.netx.basic.R1.l10n.*;


public class NTLocalization extends UnitTester {

	public static void main(String[] args) throws Throwable {
		final NTLocalization nt = new NTLocalization();
		nt.t01_LoadL10n();
		nt.t02_LoadL10nWithAnotherLocale();
	}

	public void t01_LoadL10n() {
		Globals.setApplicationLocale(Tools.getLocale("en_GB"));
		println(L10n.getContent(L10n.BASIC_MSG_EH_FILE_NOT_FOUND));
	}

	public void t02_LoadL10nWithAnotherLocale() throws IOException {
		InputStream in = getClass().getClassLoader().getResourceAsStream("L10n-store.xls");
		ContentStore cs = ContentStore.loadFrom(in, Tools.getLocale("pt_PT"));
		// First un-cache the content segment:
		UT.setSegment(L10n.BASIC_MSG_EH_FILE_NOT_FOUND, null);
		// Now show it:
		println(cs.getContent(L10n.BASIC_MSG_EH_FILE_NOT_FOUND));
	}
}
