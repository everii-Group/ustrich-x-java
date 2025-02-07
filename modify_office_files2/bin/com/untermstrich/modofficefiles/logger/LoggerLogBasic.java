package com.untermstrich.modofficefiles.logger;

import com.untermstrich.modofficefiles.ModOfficeFilesConfig;

public class LoggerLogBasic extends LoggerImplementation {

	@Override
	protected void log(LoggerLevels level, String message, Throwable t) {
		do_log(level, message+" - "+t.getMessage());
	}

	@Override
	protected void log(LoggerLevels level, String message) {
		do_log(level, message);
	}
	
	private void do_log(LoggerLevels level, String message) {
		boolean inServerMode = ModOfficeFilesConfig.DEFAULT_LOGGER.equals("org.apache.logging.log4j.untermstrich.server");
		
		switch (level) {
		case TRACE:
			break;
		case DEBUG:
		case INFO:
		case WARN:
			if (inServerMode) {
				System.out.println(level.name().toLowerCase()+": "+message);
			}
			break;
		case ERROR:
		case FATAL:
			System.out.println(level.name()+": "+message);
			break;
		}
		//FATAL, ERROR, WARN, INFO, DEBUG, TRACE
				
	}

}
