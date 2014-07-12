package com.netx.config.R1;
import java.io.IOException;
import com.netx.basic.R1.io.FileSystem;
import com.netx.basic.R1.io.Directory;
import com.netx.basic.R1.io.FileNotFoundException;


public class TL_Directory extends SimpleTypeLoader<Directory> {
	
	public Directory parse(String value, PropertyDef pDef) throws TypeLoadException {
		try {
			return new FileSystem(value);
		}
		catch(IllegalArgumentException iae) {
			throw new TypeLoadException(L10n.CONFIG_MSG_CFG_WRONG_FORMAT, pDef.name, pDef.type.id, value);
		}
		catch(FileNotFoundException fnfe) {
			throw new TypeLoadException(fnfe);
		}
		catch(IOException io) {
			// TODO i18n
			throw new TypeLoadException(io);
		}
	}
}
