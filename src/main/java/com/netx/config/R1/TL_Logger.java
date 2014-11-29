package com.netx.config.R1;
import java.util.Map;
import java.io.IOException;

import com.netx.basic.R1.io.Directory;
import com.netx.basic.R1.logging.LogFile;
import com.netx.basic.R1.logging.Logger;


public class TL_Logger extends ComplexTypeLoader<Logger> {

	public Logger onLoad(Map<String,Object> values, PropertyDef pDef) throws TypeLoadException {
		Logger logger = null;
		String filename = (String)values.get("filename");
		if(filename.equals("System.out")) {
			logger = new Logger(System.out);
		}
		else if(filename.equals("System.err")) {
			logger = new Logger(System.err);
		}
		else {
			// File-based logger:
			Directory location = (Directory)values.get("location");
			Integer numDays = (Integer)values.get("num-days");
			try {
				LogFile file = new LogFile(location, filename, numDays);
				logger = new Logger(file);
			}
			catch(IOException io) {
				// TODO i18n
				throw new TypeLoadException(io);
			}
		}
		logger.setLevel((Logger.LEVEL)values.get("level"));
		return logger;
	}

	public void onChange(Logger arg, Map<String,Object> changes, PropertyDef pDef) {
		Logger.LEVEL level = (Logger.LEVEL)changes.get("level");
		if(!arg.getLevel().equals(level)) {
			arg.setLevel(level);
		}
	}
}
