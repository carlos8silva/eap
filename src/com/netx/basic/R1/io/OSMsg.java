package com.netx.basic.R1.io;
import java.io.IOException;

import com.netx.basic.R1.l10n.ContentID;
import com.netx.basic.R1.l10n.ContentStore;
import com.netx.basic.R1.shared.Globals;
import com.netx.basic.R1.eh.IntegrityException;


class OSMsg {

	// Constants:
	private static final String _OS_MSGS_FILENAME = "os-messages.xls";
	private static final String _M_IO = "IO";
	private static final String _TYPE_WINDOWS = "WINDOWS";
	public static final ContentID IO_WINDOWS_ACCESS_DENIED = new ContentID(_M_IO, _TYPE_WINDOWS, "access-denied");
	public static final ContentID IO_WINDOWS_FILE_LOCKED_1 = new ContentID(_M_IO, _TYPE_WINDOWS, "file-locked-1");
	public static final ContentID IO_WINDOWS_FILE_LOCKED_2 = new ContentID(_M_IO, _TYPE_WINDOWS, "file-locked-2");
	public static final ContentID IO_WINDOWS_FILE_LOCKED_3 = new ContentID(_M_IO, _TYPE_WINDOWS, "file-locked-3");
	// Internal variables:
	public static final ContentStore cs;
	
	static {
		try {
			cs = ContentStore.loadFrom(OSMsg.class.getResourceAsStream(_OS_MSGS_FILENAME), Globals.getSystemLocale());
		}
		catch(IOException io) {
			throw new IntegrityException(io);
		}
	}
}
